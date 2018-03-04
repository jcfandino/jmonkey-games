package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.input.InputManager
import com.jme3.math.ColorRGBA
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.stovokor.control.PlayerControl
import com.jme3.math.Vector3f
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.collision.shapes.BoxCollisionShape

object PlayerFactory {

  val playerWidth = 0.5f
  val playerLength = 0.25f
  val playerHeight = 2f

  def create(assetManager: AssetManager) = {
    val box = new Geometry("player", new Box(playerWidth / 2f, playerLength / 2f, playerHeight / 2f))
    box.setMaterial(MaterialFactory.create(assetManager, ColorRGBA.Pink))
    //    box.setQueueBucket(Bucket.Transparent);
    //    val body = new RigidBodyControl(1f)
    //    val bcc = new BetterCharacterControl(playerWidth, playerHeight, 100f)
    //    bcc.setGravity(new Vector3f(0, 0, -10f))

    //    box.addControl(bcc)
    //    box.addControl(PlayerControl(inputManager))

    //    box.move(2, 2, 1)
    box
    //    var carNode = new Node("car")
    //    carNode.attachChild(box)
    //    
  }

  def createPhysics() = {
    //    val body = new PhysicsRigidBody(new SphereCollisionShape(1), 1)
    val body = new PhysicsRigidBody(new BoxCollisionShape(new Vector3f(playerWidth, playerLength, playerHeight).mult(.5f)), 1f)
    body.setKinematic(true)
    body
  }

}