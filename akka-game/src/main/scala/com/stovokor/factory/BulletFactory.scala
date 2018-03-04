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
import com.jme3.scene.shape.Sphere
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.collision.shapes.BoxCollisionShape

object BulletFactory {

  val bulletWidth = .25f

  def create(assetManager: AssetManager) = {
    val ball = new Geometry("bullet", new Sphere(6, 6, bulletWidth))
    ball.setMaterial(MaterialFactory.create(assetManager, ColorRGBA.Yellow))
    //debug
    ball.addControl(new RigidBodyControl(new SphereCollisionShape(bulletWidth), 1f))
    ball
  }

  def createPhysics() = {
    val body = new PhysicsRigidBody(new BoxCollisionShape(new Vector3f(bulletWidth, bulletWidth, bulletWidth)), 1f)
    body.setKinematic(true)
    body
  }
}