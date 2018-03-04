package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Spatial
import com.jme3.scene.shape.Box
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.domain.PistolBullet
import com.stovokor.domain.RifleBullet
import com.stovokor.domain.Shell
import com.stovokor.domain.item.AccessCardControl
import com.stovokor.domain.item.AmmoControl
import com.stovokor.domain.item.CanBePickedUp
import com.stovokor.domain.item.Item
import com.stovokor.gen.level.quest.QAmmo
import com.stovokor.gen.level.quest.QCard
import com.stovokor.gen.level.quest.QItem
import com.stovokor.state.CollisionGroups
import com.stovokor.domain.Ammo

class ItemGenerator(implicit assetManager: AssetManager) extends Generator[Set[Item]](1, 1) {

  val factories = List(
    new PistolBulletAmmoItemFactory,
    new RifleBulletAmmoItemFactory,
    new ShellsAmmoItemFactory,
    new HealthItemFactory,
    new ArmorItemFactory,
    new AccessCardItemFactory)

  def generate(ctx: GeneratorContext) = {
    val quest = ctx.quest.get

    val list = for (v <- quest.verts; item <- v.items) yield {
      factories.find(f => f.accept(item)).map(factory => {
        val pos = v.absoluteItemPosition(item)
        println(s"Placing item $item in $pos")
        val it = factory.create(item, pos)
        it
      })
    }
    list.map(_.get).toSet
  }

}

trait ItemFactory {
  def accept(item: QItem): Boolean
  def create(item: QItem, pos: Vector3f): Item

  def createItem(spat: Spatial, mat: Material, ctrl: CanBePickedUp, pos: Vector3f) = {
    TangentBinormalGenerator.generate(spat)
    spat.setLocalTranslation(pos)
    applyTransformation(spat)
    spat.setMaterial(mat)
    spat.addControl(ctrl)
    val cs = new BoxCollisionShape(new Vector3f(2, 4, 2))
    val body = new RigidBodyControl(cs, 0)
    body.setCollisionGroup(CollisionGroups.items)
    body.setCollideWithGroups(CollisionGroups.items)
    spat.addControl(body)
    new Item(spat, ctrl)
  }

  def applyTransformation(spat: Spatial) {
  }

}

abstract class AbstractAmmoItemFactory(ammoType: Ammo) extends ItemFactory {

  def create(item: QItem, pos: Vector3f) = {
    val ammo = item.asInstanceOf[QAmmo]
    createItem(model, material, new AmmoControl(ammoType, ammo.amount), pos)
  }

  def accept(item: QItem) =
    item.isInstanceOf[QAmmo] &&
      item.asInstanceOf[QAmmo].ammoType == ammoType

  def model: Spatial
  def material: Material
}

class PistolBulletAmmoItemFactory(implicit assetManager: AssetManager)
    extends AbstractAmmoItemFactory(PistolBullet) {

  def model = assetManager.loadModel("Models/Items/pistol-bullets.j3o")

  lazy val material = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/Items/pistol-bullets-diffuse.png"))
    material.setColor("Ambient", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f))
    material
  }
}
class RifleBulletAmmoItemFactory(implicit assetManager: AssetManager)
    extends AbstractAmmoItemFactory(RifleBullet) {

  def model = assetManager.loadModel("Models/Items/rifle-bullets.j3o")

  lazy val material = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/Items/rifle-bullets-diffuse.png"))
    material.setTexture("NormalMap", assetManager.loadTexture("Models/Items/rifle-bullets-normal.png"))
    material.setColor("Ambient", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f))
    material
  }

}
class ShellsAmmoItemFactory(implicit assetManager: AssetManager)
    extends AbstractAmmoItemFactory(Shell) {

  def model = new Geometry("shells", new Box(1, 1, 1))

  lazy val material = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap", assetManager.loadTexture("textures/tiles/tiles-014-wfc.jpg"))

    material.setBoolean("UseMaterialColors", true)
    //material.setBoolean("VTangent", true)
    material.setFloat("ParallaxHeight", .9f)
    material.setColor("Diffuse", ColorRGBA.Red)
    material.setColor("Specular", ColorRGBA.White)
    material.setFloat("Shininess", 128f)
    material.setColor("Ambient", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f))
    material
  }
}

class AccessCardItemFactory(implicit assetManager: AssetManager) extends ItemFactory {

  def accept(item: QItem) = item match {
    case QCard(_) => true
    case _ => false
  }

  def model = {
    val asset = "Models/keycard/keycard.j3o"
    val model = assetManager.loadModel(asset)
    model
  }

  def material = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/keycard/keycard-diffuse.png"))
    material
  }

  def material(key: String): Material = {
    KeyMaterialColorizer.colorize(key, material, 1)
  }

  override def create(item: QItem, pos: Vector3f) = item match {
    case QCard(key) =>
      createItem(model, material(key), new AccessCardControl(key), pos)
  }

  override def applyTransformation(spat: Spatial) {
    spat.move(Vector3f.UNIT_Y)
  }
}

