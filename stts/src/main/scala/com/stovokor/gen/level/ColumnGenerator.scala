package com.stovokor.gen.level

import com.stovokor.gen.level.quest.Quest
import com.jme3.scene.Spatial
import com.jme3.scene.shape.Cylinder
import com.jme3.util.TangentBinormalGenerator
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.FastMath
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.scene.Node
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.bullet.collision.shapes.CylinderCollisionShape
import com.stovokor.gen.level.lightmap.LightmapUVBuilder
import com.stovokor.gen.level.quest.QColumn
import com.stovokor.gen.level.quest.QColumnSquared
import com.stovokor.gen.level.quest.QColumnRounded
import com.jme3.scene.shape.Box
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.stovokor.domain.Prop
import com.stovokor.domain.Column

class ColumnGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager) extends Generator[Set[Column]](seed, number) {

  val sectionHeight = 6f
  val radius = 1f

  def generate(ctx: GeneratorContext) = {
    val quest = ctx.quest.get
    // TODO manage the column as a batched cluster?
    val columns = for (v <- quest.verts; c <- v.columns) yield {
      val location = v.absoluteItemPosition(c)
      val height = v.room.height
      val material = ctx.ambiences.get.wallMaterial.apply((0, 0))
      ColumnBuilder(c, material, ctx.uvBuilder).buildColumn(radius, height, location)
    }
    columns.toSet
  }

  object ColumnBuilder {
    def apply(column: QColumn, mat: Material, uvBuilder: LightmapUVBuilder) = column.shape match {
      case QColumnRounded() => new RoundedColumnBuilder(mat, uvBuilder)
      case QColumnSquared() => new SquaredColumnBuilder(mat, uvBuilder)
    }
  }

  abstract class ColumnBuilder(columnMaterial: Material, uvBuilder: LightmapUVBuilder) {
    def buildColumn(radius: Float, height: Float, location: Vector3f): Column
  }

  class SquaredColumnBuilder(mat: Material, uvBuilder: LightmapUVBuilder) extends ColumnBuilder(mat, uvBuilder) {
    val qf = new QuadFactory

    def buildColumn(radius: Float, height: Float, location: Vector3f) = {
      val node = new Node("column")
      val hh = height / 2f
      qf.attachQuad(mat, node, -radius, -hh, -radius, 2f * radius, height, Vector3f.UNIT_X, false)
      qf.attachQuad(mat, node, radius, -hh, radius, 2f * radius, height, Vector3f.UNIT_X.negate, false)
      qf.attachQuad(mat, node, radius, -hh, -radius, 2f * radius, height, Vector3f.UNIT_Z, false)
      qf.attachQuad(mat, node, -radius, -hh, radius, 2f * radius, height, Vector3f.UNIT_Z.negate, false)
//      GeometryBatchFactory.optimize(node)
      TangentBinormalGenerator.generate(node)
      node.setLocalTranslation(location.add(0, hh, 0))
      uvBuilder.collectGeometries(node)
      val cs = new BoxCollisionShape(new Vector3f(radius, hh, radius))
      val ctrl = new RigidBodyControl(cs, 0)
      node.addControl(ctrl)
      new Column(node)
    }
  }
  class RoundedColumnBuilder(mat: Material, uvBuilder: LightmapUVBuilder) extends ColumnBuilder(mat, uvBuilder) {
    def buildColumn(radius: Float, height: Float, location: Vector3f) = {
      val node = new Node("column")
      val hh = height / 2f
      val sections = for (h <- 0 to (height / sectionHeight).toInt) yield {
        val m = new Cylinder(2, 8, radius, sectionHeight)
        val section = new Geometry("column", m)
        TangentBinormalGenerator.generate(section)
        section.rotate(FastMath.HALF_PI, 0f, 0f)

        val posH = -hh + h * sectionHeight + sectionHeight / 2
        section.setLocalTranslation(0, posH, 0)
        section.setMaterial(mat)
        node.attachChild(section)
      }
      GeometryBatchFactory.optimize(node)
      TangentBinormalGenerator.generate(node)
      uvBuilder.collectGeometries(node)
      node.setLocalTranslation(location.add(0,hh,0))

      val cs = new CylinderCollisionShape(new Vector3f(radius, hh, radius), 1)
      val ctrl = new RigidBodyControl(cs, 0)
      node.addControl(ctrl)
      new Column(node)
    }
  }

}
