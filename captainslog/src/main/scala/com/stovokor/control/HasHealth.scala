package com.stovokor.control

import com.stovokor.K
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint

trait HasHealth {

  def initialHealth: Float

  var health = initialHealth

  def hit(points: Float): Boolean = {
    health = Math.max(0f, health - points)
    health <= 0
  }

  def respawnHealth {
    health = initialHealth
  }
}

trait HasShield extends HasHealth {

  def initialShield: Float
  def getNode: Node

  var shield = initialShield
  var underFire = false

  override def hit(points: Float): Boolean = {
    underFire = true
    changeShield(-points)
    val healthHit = Math.max(0f, points - shield)
    super.hit(healthHit)
  }

  def updateShield(tpf: Float) {
    changeShield(K.shieldRechargeSpeed * tpf)
    showShield(underFire)
    underFire = false
  }

  def changeShield(d: Float) {
    shield = Math.max(0f, Math.min(initialShield, shield + d))
  }

  def showShield(b: Boolean) {
    val geom = getNode.getChild("shield")
    if (b && shield > 0.2f) geom.setCullHint(CullHint.Never)
    else geom.setCullHint(CullHint.Always)
  }

  override def respawnHealth {
    super.respawnHealth
    shield = initialShield
  }

}