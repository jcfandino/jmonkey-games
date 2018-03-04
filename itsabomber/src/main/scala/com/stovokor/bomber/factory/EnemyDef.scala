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

class EnemyDef(
  val spriteSheet: SpriteSheet,
  val w: Float,
  val h: Float,
  val physW: Float,
  val physH: Float,
  val flies: Boolean,
  val life: Float,
  val crashDamage: Float,
  val explosive: Boolean,
  val shoots: Boolean,
  val physOffsetX: Float = 0f,
  val physOffsetY: Float = 0f,
  val shotOffsetX: Float = 0f,
  val shotOffsetY: Float = 0f,
  val timeBetweenShots: Float = 0.2f,
  val shootDirection: Vector3f = Vector3f.UNIT_X.negate,
  val shotType: ShotType = ShotType.Light,
  val score: Int = 10)

abstract class ShotType(val damage: Float)
object ShotType {
  object Light extends ShotType(damage = 10f)
  object Heavy extends ShotType(damage = 100f)
  object Flak extends ShotType(damage = 0f)
}
