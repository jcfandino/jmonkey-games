package com.stovokor.bomber.control

import com.jme3.scene.Spatial
import com.stovokor.bomber.state.GameStatus
import com.jme3.math.Vector3f
import com.stovokor.bomber.factory.ShotType

trait Shoots {

  def shootDirection: Vector3f
  def getSpatial: Spatial
  def shoot: Boolean
  def timeBetweenShots: Float
  def shootingOffset: Vector3f
  def shotType: ShotType
  var timeSinceLastShot = -timeBetweenShots

  def updateShooting(tpf: Float) {
    timeSinceLastShot += tpf
    if (shoot && timeSinceLastShot >= timeBetweenShots) {
      val flakAtt = if (shotType == ShotType.Flak) pos.y + 3f else 0f
      GameStatus.addShot(pos.add(shootingOffset), shootDirection, flakAtt)
      timeSinceLastShot = 0f
    }
  }

  def pos = getSpatial.getLocalTranslation
}