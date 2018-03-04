package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.stovokor.domain.item.ArmorControl
import com.stovokor.domain.item.Item
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.gen.level.quest.QArmor
import com.stovokor.gen.level.quest.QItem
import com.jme3.scene.Spatial

class ArmorItemFactory(implicit assetManager: AssetManager) extends ItemFactory {

  def accept(item: QItem) = item.isInstanceOf[QArmor]

  def create(item: QItem, pos: Vector3f) = {
    val armor = item.asInstanceOf[QArmor]
    createItem(model, material, new ArmorControl(armor.armor), pos)
  }

  def model = {
    def objectFile = "Models/Character/robot-parts-1/robot-parts-1-chest.j3o"
    assetManager.loadModel(objectFile)
  }

  def material = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/Character/robot-parts-1/robot-parts-1-diffuse.png"))
    material.setTexture("NormalMap", assetManager.loadTexture("Models/Character/robot-parts-1/robot-parts-1-normal.png"))

    material.setBoolean("UseMaterialColors", true)
    //material.setBoolean("VTangent", true)
    material.setFloat("ParallaxHeight", .9f)
    material.setColor("Diffuse", ColorRGBA.Yellow)
    material.setColor("Specular", ColorRGBA.Orange)
    material.setFloat("Shininess", 128f)
    material.setColor("Ambient", ColorRGBA.Brown)
    material
  }

  override def applyTransformation(s: Spatial) {
    s.move(0, 2, 0)
  }
}

