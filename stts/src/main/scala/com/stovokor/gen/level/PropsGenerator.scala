package com.stovokor.gen.level

import com.jme3.scene.Spatial
import com.stovokor.domain.PropControl
import com.jme3.util.TangentBinormalGenerator
import com.jme3.math.Vector3f
import com.jme3.scene.shape.Box
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.asset.AssetManager
import com.stovokor.domain.Entity
import com.stovokor.domain.Prop

class PropsGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager) extends Generator[Set[Prop]](seed, number) {

  def generate(ctx: GeneratorContext) = {
    val quest = ctx.quest.get
    val boxes = for (v <- quest.verts; p <- v.props) yield {
      buildBox(v.absoluteItemPosition(p))
    }
    boxes.toSet
  }

  def buildBox(location: Vector3f) = {
    val box = new Geometry("box", new Box(2, 2, 2))
    TangentBinormalGenerator.generate(box)
    box.setLocalTranslation(location.add(Vector3f.UNIT_Y.mult(2)))
    val control = PropControl(box, 10)
    box.setMaterial(crateMaterial)
    new Prop(box, control)
  }

  lazy val crateMaterial = {
    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    mat.setTexture("DiffuseMap", assetManager.loadTexture("props/crate-test.jpg"))
    mat
  }
}