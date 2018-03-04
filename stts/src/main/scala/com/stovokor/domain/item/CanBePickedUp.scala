package com.stovokor.domain.item

import scala.collection.JavaConversions.iterableAsScalaIterable
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.collision.CollisionResults
import com.jme3.math.Ray
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.Control
import com.stovokor.domain.NodeId
import com.stovokor.domain.PlayerControl
import com.stovokor.domain.Entity
import com.stovokor.state.CollisionGroups
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.ItemDropped
import scala.collection.JavaConversions._
import java.util.ArrayList

class Item(model: Spatial, control: CanBePickedUp)
  extends Entity[CanBePickedUp](model, control, Some(model)) {}

trait CanBePickedUp extends Control {

  def pickUpBy(player: PlayerControl) {
    if (doPickup(player)) disappear
  }

  def disappear {
    val body = getSpatial.getControl(classOf[RigidBodyControl])
    if (body != null) {
      body.getPhysicsSpace().remove(body)
      getSpatial.removeControl(body)
    }
    getSpatial.removeFromParent()
    getSpatial.removeControl(this)
  }

  def dropItem(spatial: Spatial, dropTo: Node, physicsSpace: PhysicsSpace) = {
    dropTo.attachChild(spatial)
    val body = new RigidBodyControl(collisionShape, 0)
    body.setCollisionGroup(CollisionGroups.items)
    body.setCollideWithGroups(CollisionGroups.items)
    spatial.addControl(body)
    spatial.addControl(new ItemDroppedControl(physicsSpace))
    physicsSpace.add(spatial)
  }

  // To implement by classes
  val collisionShape = new SphereCollisionShape(4f)
  //new BoxCollisionShape(new Vector3f(2.5f, 2, 1))

  def doPickup(player: PlayerControl): Boolean
  def getSpatial: Spatial
}

class ItemDroppedControl(space: PhysicsSpace) extends AbstractControl {

  val floatingHeight = 1.5f

  override def controlUpdate(tpf: Float) {
    moveDownUntilHittingFloor(tpf)
  }

  def moveDownUntilHittingFloor(tpf: Float) = {
    val pos = spatial.getLocalTranslation
    val results = space.rayTest(pos, pos.subtract(0f, 100f, 0f), new ArrayList())
    val hitFloor = results.find(result => {
      println(s"ray test result ${result.getCollisionObject.getUserObject} - ${result.getHitFraction}")
      (result.getCollisionObject.getCollisionGroup & CollisionGroups.level) != 0
    })
    if (hitFloor.isDefined) {
      val distance = hitFloor.get.getHitFraction * 100f
      if (distance > floatingHeight) {
        move(-10f * tpf)
      } else {
        println("arrived to floor")
        move(floatingHeight - distance)
        EventHub.trigger(ItemDropped(spatial))
        spatial.removeControl(this)
      }
    }
  }

  def move(y: Float) {
    spatial.move(0f, y, 0f)
    spatial.getControl(classOf[RigidBodyControl]).setPhysicsLocation(spatial.getWorldTranslation)
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}
}
