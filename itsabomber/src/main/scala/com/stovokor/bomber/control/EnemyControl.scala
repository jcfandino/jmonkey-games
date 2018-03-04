package com.stovokor.bomber.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.stovokor.bomber.factory.EnemyDef
import com.jme3.scene.Node
import com.stovokor.bomber.state.GameStatus
import com.jme3.bullet.control.RigidBodyControl
import scala.util.Random
import com.jme3.math.Vector3f

object EnemyControl {
  def apply(enemyDef: EnemyDef) = new EnemyControl(enemyDef)
}

class EnemyControl(enemyDef: EnemyDef)
    extends AbstractControl
    with AnimatedSprite
    with Shoots {

  def getSpriteSpatial = getSpatial.asInstanceOf[Node].getChild("enemy-sprite")
  def getPhysicSpatial = getSpatial.asInstanceOf[Node].getChild("enemy-body")

  def sheetSizeX = enemyDef.spriteSheet.tilesX
  def sheetSizeY = enemyDef.spriteSheet.tilesY
  var maxTtl = 0.6f
  var life = enemyDef.life

  val shootDirection = enemyDef.shootDirection
  val timeBetweenShots = enemyDef.timeBetweenShots
  val shootingOffset = new Vector3f(enemyDef.shotOffsetX, enemyDef.shotOffsetY, 0f)
  val shotType = enemyDef.shotType

  var shoot = false
  override def pos = getSpatial.getWorldTranslation

  def controlUpdate(tpf: Float) {
    if (life <= 0f) {
      if (enemyDef.explosive) {
        val pos = getSpatial.getWorldTranslation // enemies are attached to ground
        for (i <- 1 to 4) {
          val expPos = pos.add(-enemyDef.w / 2f + (0.2f * i * enemyDef.w),
            -enemyDef.h / 2f + enemyDef.h * Random.nextFloat(), 0f)
          GameStatus.explode(expPos)
        }
        ttl = 0f // ugly fix
      }
      decreseTtl(tpf)
      if (ttl <= 0f) {
        getSpatial.removeFromParent()
        val body = getPhysicSpatial.getControl(classOf[RigidBodyControl])
        if (body != null) {
          body.setPhysicsSpace(null)
        }
      } else {
        updateTextureCoordinates
      }
    }

    if (enemyDef.flies) {
      getSpatial.move(-1f * tpf, 0f, 0f)
    }
    if (enemyDef.shoots) {
      shoot = (System.currentTimeMillis() / 1000) % 2 == 0
      updateShooting(tpf)
    }
  }

  def hit(points: Float) {
    val alive = life > 0f
    life -= points
    println(s"life is $life")
    if (alive && life <= 0f) {
      GameStatus.earnPoints(enemyDef.score)
      reset
    }
  }
  
  def hitPlayer(plane:PlaneControl) {
    hit(life)  
    plane.hit(enemyDef.crashDamage)
  }
  
  def controlRender(rm: RenderManager, vp: ViewPort) {}

}