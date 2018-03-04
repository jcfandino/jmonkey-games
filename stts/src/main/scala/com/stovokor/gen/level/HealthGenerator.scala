package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Spatial
import com.stovokor.domain.item.HealthControl
import com.stovokor.gen.level.quest.QHealth
import com.stovokor.gen.level.quest.QItem

class HealthItemFactory(implicit assetManager: AssetManager) extends ItemFactory {

  def accept(item: QItem) = item.isInstanceOf[QHealth]

  def create(item: QItem, pos: Vector3f) = {
    val health = item.asInstanceOf[QHealth]
    createItem(model, material, new HealthControl(health.health), pos)
  }

  def model = assetManager.loadModel("Models/Items/pistol-bullets.j3o")

  def material = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/Items/health-small-diffuse.png"))
    material.setColor("Ambient", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f))
    material
  }

  override def applyTransformation(spat: Spatial) {
    spat.scale(2f)
  }
}

