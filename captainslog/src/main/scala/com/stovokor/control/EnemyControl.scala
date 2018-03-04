package com.stovokor.control

import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.stovokor.K
import com.jme3.scene.Node

object EnemyControl {
  def apply() = new EnemyControl
}

class EnemyControl
    extends AbstractControl
    with HasWeapons
    with HasHealth {

  var timeInSpace = 0f

  def controlUpdate(tpf: Float) {
    getSpatial.move(0, -speed * tpf, 0)

    if (isInSlowZone) {
      timeInSpace = timeInSpace + tpf
    }
    shootingPhaser = timeInSpace.toInt % 2 == 1
    updateWeapons(tpf)
  }

  def controlRender(r: RenderManager, vp: ViewPort) {}

  def initialHealth = K.enemyHealth

  def isInSlowZone = {
    val y = getSpatial.getLocalTranslation.y
    y < K.enemyFastZone && y > K.shipPosY
  }

  def speed = {
    if (isInSlowZone) {
      K.enemySlowSpeed
    } else {
      K.enemyFastSpeed
    }
  }

  def changePhaserEnery(d: Float) {}
  def changeTorpedos(n: Int) = {}
  def phaserEnergy = 999f
  def torpedoes = 1
  def getNode = getSpatial.asInstanceOf[Node]

}
