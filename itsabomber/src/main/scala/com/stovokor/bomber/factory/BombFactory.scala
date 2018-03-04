package com.stovokor.bomber.factory

import com.jme3.asset.AssetManager
import com.stovokor.bomber.tiles.SpriteSheet
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.stovokor.bomber.control.BombControl
import com.jme3.bullet.collision.shapes.SphereCollisionShape

object BombFactory {

  val height = 0.2f
  val width = height / 2f
  val bombSheet = SpriteSheet("Textures/bombs/bomb2.png", 4, 1)

  def create(assetManager: AssetManager) = {

  }
  def create(assetManager: AssetManager, pos: Vector3f) = {
    val bomb = SpriteFactory.create(assetManager, "bomb", bombSheet, width, height, pos)

    val shape = new SphereCollisionShape(width / 2f)
    val body = new RigidBodyControl(shape, 1f)
    bomb.addControl(body)
    bomb.addControl(BombControl())
    bomb
  }

}