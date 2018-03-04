package com.stovokor.bomber.factory

import com.jme3.asset.AssetManager
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.stovokor.bomber.control.ExplosionControl
import com.jme3.bullet.control.GhostControl
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.material.RenderState.BlendMode
import com.stovokor.bomber.tiles.SpriteSheet

object ExplosionFactory {

  val radius = 1.6f

  def create(assetManager: AssetManager, pos: Vector3f) = {
    val sheet = SpriteSheet("Textures/Sheets/Explosion.png", 4, 5)
    val bam = SpriteFactory.create(assetManager, "explosion", sheet, radius, radius, pos)
    bam.addControl(new ExplosionControl(4, 5, 2f))

    val shape = new SphereCollisionShape(1f / 2f)
    bam.addControl(new GhostControl(shape))
    bam
  }

}