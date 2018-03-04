package com.stovokor.gen.level.lightmap

import com.jme3.scene.Geometry
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer
import com.jme3.util.BufferUtils
import java.lang.Math.min
import java.lang.Math.max
import com.stovokor.util.math.RectanglePacker
import com.stovokor.util.math.RectanglePacker.Rectangle
import com.jme3.scene.Spatial
import com.jme3.scene.Node
import scala.collection.JavaConversions._
import java.nio.FloatBuffer
import com.jme3.scene.Mesh

class LightmapUVBuilder {
  var collectedQuads: Map[Geometry, Vector2f] = Map.empty
  var collectedGeometries: List[Geometry] = List.empty

  val presision = 1000f
  val padding = 2 * presision.toInt
  val geomWidth = 0.01f * presision

  def collectQuad(geom: Geometry, a: Float, b: Float) {
    collectedQuads = collectedQuads.updated(geom, new Vector2f(a, b))
  }
  def collectGeometries(spatial: Spatial) {
    if (spatial.isInstanceOf[Geometry]) {
      val geom = spatial.asInstanceOf[Geometry]
      collectedGeometries = geom :: collectedGeometries
    } else if (spatial.isInstanceOf[Node]) {
      for (child <- spatial.asInstanceOf[Node].getChildren())
        collectGeometries(child)
    }
  }

  def pack = {
    val wquads = collectedQuads.map({ case (g, v) => new QuadRec(toInt(v.x), toInt(v.y), g) })

    val gquads = collectedGeometries.map(g => new GeomRec(toInt(geomWidth), toInt(geomWidth), g))
    // TODO - Complete work with geometries
    val quads = (wquads ++ gquads)
      //    val quads: Seq[UvRec] = wquads
      .toSeq
      .sortBy(q => -q.h)

    val (maxW: Int, maxH: Int, sumW: Int, sumH: Int) = maxAndMins(quads)
    val worst = max(sumW, sumH)
    val ideal = max(maxW, maxH)

    println(s"Trying to pack: ideal: $ideal worst: $worst")
    val (width, recs) = findOptimalPack(ideal, worst, quads, Seq())

    for ((quad, rec) <- recs) yield {
      new QuadUV(quad,
        toFloat(rec.x + padding) / toFloat(width),
        toFloat(rec.y + padding) / toFloat(width),
        toFloat(rec.x + rec.width - (2 * padding)) / toFloat(width),
        toFloat(rec.y + rec.height - (2 * padding)) / toFloat(width))
    }

  }

  abstract class UvRec(val w: Int, val h: Int, val geom: Geometry) {
    def applyUV(uv: QuadUV)
  }
  class QuadRec(w: Int, h: Int, geom: Geometry) extends UvRec(w, h, geom) {
    def applyUV(uv: QuadUV) {
      println(s"Applying uvs to ${uv.rec.geom.getName()}")
      val coors = BufferUtils.createFloatBuffer(
        new Vector2f(uv.a, uv.b),
        new Vector2f(uv.a, uv.d),
        new Vector2f(uv.c, uv.b),
        new Vector2f(uv.c, uv.d))
      uv.rec.geom.getMesh().setBuffer(VertexBuffer.Type.TexCoord2, 2, coors)
      uv.rec.geom.getMesh().updateBound()
      uv.rec.geom.updateModelBound()
      //      println(s"New UVs: (${uv.a},${uv.b})->(${uv.c},${uv.d})")
    }
  }
  class GeomRec(w: Int, h: Int, geom: Geometry) extends UvRec(w, h, geom) {
    def applyUV(uv: QuadUV) {
      println(s"Applying uvs to ${uv.rec.geom.getName()}")
      val m = uv.rec.geom.getMesh
      val cb = getTextCoor(m)
      // It has to be a FloatBuffer
      val fb = cb.getDataReadOnly.asInstanceOf[FloatBuffer]
      val floats = BufferUtils.getVector2Array(fb)
      for (v <- floats) {
        v.x = uv.a + v.x * (uv.c - uv.a)
        v.y = uv.b + v.y * (uv.d - uv.b)
      }
      val coors = BufferUtils.createFloatBuffer(floats: _*)
      m.setBuffer(VertexBuffer.Type.TexCoord2, 2, coors)
      m.updateBound()
      uv.rec.geom.updateModelBound()
    }

    def getTextCoor(m: Mesh) = {
      if (m.getBuffer(VertexBuffer.Type.TexCoord2) != null)
        m.getBuffer(VertexBuffer.Type.TexCoord2)
      else
        m.getBuffer(VertexBuffer.Type.TexCoord)
    }
  }

  class QuadUV(val rec: UvRec, val a: Float, val b: Float, val c: Float, val d: Float) {
    def applyUV() {
      rec.applyUV(this)
    }
  }

  def applyUVs = {
    for (uv <- pack) uv.applyUV()
  }

  def toInt(f: Float) = (f * presision) toInt
  def toFloat(i: Int) = (i toFloat) / presision

  def findOptimalPack(ideal: Int, worst: Int, quads: Seq[UvRec], bestSoFar: Seq[(UvRec, Rectangle)]): (Int, Seq[(UvRec, Rectangle)]) = {
    if (worst - ideal < 2) (worst, bestSoFar) else {

      val pivot = (ideal + worst) / 2
      val packer = new RectanglePacker[UvRec](pivot, pivot, padding)

      def insert(quads: Seq[UvRec], collected: Seq[Rectangle]): Seq[Rectangle] =
        quads match {
          case Seq() => collected
          case q :: qa => {
            val result = packer.insert(q.w, q.h, q)
            if (result != null) insert(qa, result +: collected) else Seq()
          }
        }

      def asPairs: Seq[(UvRec, Rectangle)] = {
        quads.map(q => (q, packer.findRectangle(q)))
      }

      val recs = insert(quads, Seq())
      val (nextIdeal, nextWorst, nextBest) =
        if (recs.isEmpty) { // Failed can't shrink any longer
          (pivot, worst, bestSoFar)
        } else { // Fitted, lets try another time
          (ideal, pivot, asPairs)
        }
      findOptimalPack(nextIdeal, nextWorst, quads, nextBest)
    }
  }

  def maxAndMins(quads: Seq[UvRec]): (Int, Int, Int, Int) =
    maxAndMins(quads, 0, 0, 0, 0)

  def maxAndMins(quads: Seq[UvRec], maxW: Int, maxH: Int, sumW: Int, sumH: Int): (Int, Int, Int, Int) =
    quads match {
      case Seq() => (maxW, maxH, sumW, sumH)
      case q :: qs => maxAndMins(qs, max(maxW, q.w), max(maxH, q.h), sumW + q.w, sumH + q.h)
    }

}
