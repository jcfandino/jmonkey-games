package com.stovokor.bomber.factory

import com.jme3.asset.AssetManager
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.math.Vector3f
import com.stovokor.bomber.control.ShotControl
import com.stovokor.bomber.tiles.SpriteSheet

object ShotFactory {

  val width = 0.1f
  val height = 0.05f

  def create(assetManager: AssetManager, pos: Vector3f) = {
    val sheet = SpriteSheet("Textures/bullet.png", 1, 1)
    val shot = SpriteFactory.create(assetManager, "shot", sheet, width, height, pos)
    shot.addControl(new ShotControl())

    // TODO Not the best, passes through walls   
    val shape = new SphereCollisionShape(height / 2f)
    shot.addControl(new GhostControl(shape))
    shot
  }

}