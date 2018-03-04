package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.Material
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.Node
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.Settings.Debug
import com.stovokor.util.debug.DebugArrow

/**
 * Vertices are arranged like this:
 *
 * 2(vd)------3(vc)
 * | \    <-- |
 * |   \    | |
 * |  |  \  | |
 * |  |    \  |
 * |  -->    \|
 * 0(vb)------1(va)
 *
 * Notation is index(vertex).
 * Indexes are [2,0,1] and [1,3,2]
 */
class QuadFactory(implicit val assetManager: AssetManager) {

  def attachQuad(
    material: Material,
    node: Node,
    x1: Float,
    y1: Float,
    z1: Float,
    a: Float,
    b: Float,
    normal: Vector3f,
    solid: Boolean = true): Option[Geometry] = {
    if (a * b == 0) return None

    val (n1, n2) = getComplementaryNormals(normal)
    val o = new Vector3f(x1, y1, z1)
    val unit = Vector3f.UNIT_XYZ
    val va = o
    val vb = o.add(n1.mult(a))
    val vc = o.add(n2.mult(b))
    val vd = o.add(n1.mult(a).add(n2.mult(b)))

    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      va, vb, vc, vd))

    val (tx, ty) = (10f, 10f)
    val (uo, vo) = (n1.mult(o).dot(unit), n2.mult(o).dot(unit))
    val (ox, oy) = (((uo % tx) / tx), (vo % ty) / ty)
    val (ex, ey) = (ox + a / tx, oy + b / ty)
    //    println(s"Origin: $o N: $normal  UV: ($uo,$vo) - Dimension ($a, $b)")
    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(ox, oy),
      new Vector2f(ex, oy),
      new Vector2f(ox, ey),
      new Vector2f(ex, ey)))

    //    println(s"Texture O($ox,$oy) E($ex,$ey)")
    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    val negNormal = normal.negate
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(negNormal, negNormal, negNormal, negNormal))
    m.updateBound()
    TangentBinormalGenerator.generate(m)
    val name = getGeometryName(normal)
    val geom = new Geometry(name, m)
    geom.setMaterial(material)
    node.attachChild(geom)

    if (solid) {
      geom.addControl(new RigidBodyControl(0))
    }
    // Debug
    if (Debug.quadNormals) {
      new DebugArrow(assetManager, (vb add vc) mult 0.5f, normal).draw(node)
      new DebugArrow(assetManager, vb, normal).draw(node)
    }
    Some(geom)
  }
  def getComplementaryNormals(n: Vector3f) = {
    (n.x, n.y, n.z) match {
      case (1, 0, 0) => (Vector3f.UNIT_Z, Vector3f.UNIT_Y)
      case (0, 1, 0) => (Vector3f.UNIT_X, Vector3f.UNIT_Z)
      case (0, 0, 1) => (Vector3f.UNIT_X.negate, Vector3f.UNIT_Y)
      case (-1, 0, 0) => (Vector3f.UNIT_Z.negate, Vector3f.UNIT_Y)
      case (0, -1, 0) => (Vector3f.UNIT_Z.negate, Vector3f.UNIT_X.negate)
      case (0, 0, -1) => (Vector3f.UNIT_X, Vector3f.UNIT_Y)
    }
  }

  def getGeometryName(n: Vector3f) = {
    n.y match {
      case 1 => "Floor"
      case -1 => "Ceiling"
      case 0 => "Wall"
    }
  }
}