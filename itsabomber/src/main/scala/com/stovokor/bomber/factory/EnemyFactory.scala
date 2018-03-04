package com.stovokor.bomber.factory

import com.stovokor.bomber.tiles.SpriteSheet
import com.stovokor.bomber.tiles.SpriteSheet
import com.jme3.asset.AssetManager
import com.jme3.math.Vector3f
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.stovokor.bomber.control.BombControl
import com.stovokor.bomber.control.EnemyControl
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.scene.Node
import com.jme3.renderer.queue.RenderQueue.Bucket

object EnemyFactory {

  val enemyDefs = List(
    // Soldier
    new EnemyDef(SpriteSheet("Textures/enemies/soldier2.png", 8, 1),
      1f, 1f, .3f, .4f, false, 10f, 0f, false, false,
      score = 10),

    // Spitfire: 200x79
    new EnemyDef(SpriteSheet("Textures/enemies/spitfire.png", 1, 1),
      1.33f, .53f, 0.9f * 1.33f, 0.7f * .53f, true, 50f, crashDamage = 100f, true, true,
      physOffsetY = 0.15f, shotOffsetX = -0.45f * 1.33f - .1f,
      timeBetweenShots = 0.2f, shotType = ShotType.Light,
      score = 100),

    // Sherman 232x99
    new EnemyDef(SpriteSheet("Textures/enemies/sherman.png", 1, 1),
      2.32f, .99f, 1.79f, .84f, false, 100f, crashDamage = 200f, true, true,
      physOffsetX = .25f, shotOffsetX = -.5f * 1.79f - .1f, shotOffsetY = 0.2f,
      timeBetweenShots = .9f, shotType = ShotType.Heavy,
      score = 200),

    // Turret 128x145
    new EnemyDef(SpriteSheet("Textures/enemies/turret.png", 1, 1),
      .85f, .97f, 0.5f * .85f, 0.6f * .97f, false, 50f, crashDamage = 100f, true, true,
      physOffsetX = .15f, shotOffsetX = -.5f * 0.6f * .97f - .1f, shotOffsetY = 0.2f,
      timeBetweenShots = 0.5f, shootDirection = new Vector3f(-1.5f, 2f, 0f), shotType = ShotType.Flak,
      score = 250))

  def create(assetManager: AssetManager, idx: Int) = {
    val edef = enemyDefs(idx)
    val sprite = SpriteFactory.create(assetManager, "enemy-sprite",
      edef.spriteSheet, edef.w, edef.h, new Vector3f)
    sprite.setLocalTranslation(0f, 0f, 0.1f)

    val enemy = new Node("enemy")
    enemy.attachChild(sprite)

    val physNode = new Node("enemy-body")
    val shape = new BoxCollisionShape(new Vector3f(edef.physW / 2f, edef.physH / 2f, 0.1f))
    val body = new RigidBodyControl(shape, 0f)
    physNode.addControl(body)
    body.setKinematic(true)
    enemy.attachChild(physNode)
    physNode.setLocalTranslation(edef.physOffsetX, edef.physOffsetY - edef.h / 2f + edef.physH / 2f, 0f)

    enemy.addControl(EnemyControl(edef))
    enemy
  }
}

