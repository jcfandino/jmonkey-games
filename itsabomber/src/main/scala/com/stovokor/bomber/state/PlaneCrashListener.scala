package com.stovokor.bomber.state

import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.bullet.collision.PhysicsCollisionListener
import com.stovokor.bomber.control.PlaneControl
import com.jme3.scene.Spatial
import com.stovokor.bomber.control.BombControl
import com.jme3.scene.control.AbstractControl
import com.stovokor.bomber.control.EnemyControl
import com.stovokor.bomber.control.ShotControl
import com.stovokor.bomber.control.FlakControl
import com.stovokor.bomber.control.ExplosionControl

object PlaneCrashListener extends PhysicsCollisionListener {

  def collision(e: PhysicsCollisionEvent) = {
    //    println(s"collision found ${e.getAppliedImpulse} ${e.getNodeA} - ${e.getNodeB}")
    val a = e.getNodeA
    val b = e.getNodeB
    if (a != null && b != null) {
      val both = List(a.getName, b.getName)
      lazy val isMap = both.find(n => n.startsWith("map-batch")).isDefined
      lazy val isEnemy = both.find(n => n.startsWith("enemy")).isDefined
      lazy val isPlane = both.contains("plane-node")
      lazy val isExplosion = both.contains("explosion")
      lazy val isShot = both.contains("shot")
      // player crashes
      if (isPlane) {
        if (isMap) {
          GameStatus.planeCrash()
        } else if (isShot) {
          val sc = shotCtrl(a, b)
          if (sc != null) {
            sc.collide()
            val pc = planeCtrl(a, b)
            if (pc != null && !sc.friendly) {
              pc.hit(sc.shotType.damage)
            }
          }
        } else if (isExplosion) {
          val pc = planeCtrl(a, b)
          val ec = explosionCtrl(a, b)
          if (pc != null && !ec.friendly) {
            pc.hit(2)
          }
        }
      }
      // bomb explode
      if (both.contains("bomb") && (isMap || isEnemy)) {
        val bc = bombCtrl(a, b)
        if (bc != null) {
          bc.explode()
        }
        // bullet hit ground
      } else if (isShot && isMap) {
        val sc = shotCtrl(a, b)
        if (sc != null) {
          sc.recycle()
        }
      } else if (isEnemy) {
        // enemy hit by bullet
        if (isShot) {
          val ec = enemyCtrl(a.getParent, b.getParent)
          val sc = shotCtrl(a, b)
          if (ec != null && sc != null && sc.friendly) {
            ec.hit(10)
            sc.recycle()
          }
          // explosion hits enemy
        } else if (isExplosion) {
          //        println(s"enemy burn")
          val ec = enemyCtrl(a.getParent, b.getParent)
          if (ec != null) {
            ec.hit(100)
          }
          // enemy crash against player
        } else if (isPlane) {
          val ec = enemyCtrl(a.getParent, b.getParent)
          val pc = planeCtrl(a, b)
          if (ec != null && pc != null) {
            ec.hitPlayer(pc)
          }
        }
      }
    }
  }

  def getControl[C <: AbstractControl](cla: Class[C], a: Spatial, b: Spatial) = {
    if (a.getControl(cla) != null) a.getControl(cla)
    else b.getControl(cla)
  }
  def bombCtrl(a: Spatial, b: Spatial) = getControl(classOf[BombControl], a, b)
  def shotCtrl(a: Spatial, b: Spatial) = getControl(classOf[ShotControl], a, b)
  def enemyCtrl(a: Spatial, b: Spatial) = getControl(classOf[EnemyControl], a, b)
  def planeCtrl(a: Spatial, b: Spatial) = getControl(classOf[PlaneControl], a, b)
  def explosionCtrl(a: Spatial, b: Spatial) = getControl(classOf[ExplosionControl], a, b)
}