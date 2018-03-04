package com.stovokor.control

import com.jme3.scene.Node
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.scene.Spatial
import com.jme3.scene.Spatial.CullHint
import com.stovokor.K
import com.stovokor.factory.TorpedoFactory
import com.stovokor.state.GameStatus
import com.stovokor.state.SoundsState

trait HasWeapons {

  def getNode: Node
  def phaserEnergy: Float
  def changePhaserEnery(d: Float)
  def torpedoes: Int
  def changeTorpedos(n: Int)

  var shootingPhaser, shootingTorpedo = false
  var phaserRecharged = true
  var readyToShootTorpedo = true

  def shootPhaser {
    shootingPhaser = true
  }

  def updateWeapons(tpf: Float) {
    // Phaser
    if (isShootingPhaser) {
      phaserSpatial.setCullHint(CullHint.Never)
      changePhaserEnery(-K.phaserDischargeSpeed * tpf)
      phaserRecharged = phaserEnergy > 0f
      SoundsState.playPhaser
    } else {
      phaserSpatial.setCullHint(CullHint.Always)
      changePhaserEnery(K.phaserRechargeSpeed * tpf)
      phaserRecharged = phaserRecharged || phaserEnergy > K.phaserMinEnergyToShoot
    }
    // Torpedos
    if (shootingTorpedo && readyToShootTorpedo && torpedoes > 0) {
      SoundsState.playTorpedo
      readyToShootTorpedo = false
      changeTorpedos(-1)
      val pos = getNode.getLocalTranslation
      GameStatus.torpedoShot(pos.x, pos.y, true) //TODO
    }
  }

  def isShootingPhaser = shootingPhaser && canShootPhaser

  def canShootPhaser = phaserEnergy > 0 && phaserRecharged

  def phaserSpatial: Spatial = {
    getNode.getChild("phaser")
  }
}