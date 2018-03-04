package com.stovokor.domain

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.stovokor.util.jme.PlayerShoot
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.GameEvent
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.math.Quaternion
import com.jme3.math.FastMath
import com.stovokor.util.jme.HasSpatialState
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.AmmoChange
import com.stovokor.util.jme.WeaponDrawn
import com.jme3.scene.Spatial.CullHint
import com.stovokor.state.MovementControl
import com.jme3.scene.Spatial
import com.stovokor.util.jme.JmeExtensions._

class WeaponStashControl(status: PlayerStatus, pointingAt: () => (Vector3f, Vector3f))
    extends AbstractControl
    with LogicEventListener {

  def getAmmo(cat: Ammo) = status.getAmmo(cat)

  def setAmmo(cat: Ammo, count: Int) = {
    val value = Math.max(0, Math.min(cat.max, count))
    status.setAmmo(cat, value)
    EventHub.trigger(AmmoChange(cat, value))
  }

  def increaseAmmo(cat: Ammo, count: Int): Boolean = {
    val ammoBefore = getAmmo(cat)
    setAmmo(cat, getAmmo(cat) + count)
    ammoBefore < cat.max
  }

  def decreaseAmmo(cat: Ammo, count: Int) = setAmmo(cat, getAmmo(cat) - count)

  var weapons: List[Weapon] = List()

  /**
   * Returns true if by storing this weapon the player increased its ammo
   * or it's a new weapon.
   */
  def store(w: Weapon): Boolean = {
    val weaponType = w.tech.ammoType
    val ammoIncreased = increaseAmmo(weaponType, weaponType.default)
    if (!alreadyHave(w)) {
      weapons = weapons ::: List(w)
      addWeaponControl(w)
      if (weapons.size == 1) {
        drawWeapon(current)
      }
      true
    } else ammoIncreased
  }

  def addWeaponControl(w: Weapon) {
    val ctrl = new PlayerWeaponControl(this, pointingAt)
    w.spatial.addControl(ctrl)
    ctrl.setEnabled(false)
  }

  def alreadyHave(w: Weapon) = weapons.exists(e => e.id == w.id)

  def current = weapons.head
  def shiftLeft = weapons = weapons.tail ::: List(current)
  def shiftRight = weapons = List(weapons.last) ::: weapons.dropRight(1)

  // Weapon switching
  def switchWeaponNext = {
    println("next:", weapons)
    if (weapons.size > 1) {
      val oldWep = current
      shiftLeft
      val newWep = current
      switchWeapons(oldWep, newWep)
    }
  }

  def switchWeaponPrev = {
    println("prev:", weapons)
    if (weapons.size > 1) {
      val oldWep = current
      shiftRight
      val newWep = current
      switchWeapons(oldWep, newWep)
    }
  }

  def switchWeapons(oldWep: Weapon, newWep: Weapon) = {
    val oldSpatial = oldWep.spatial
            oldWep.weaponControl.setEnabled(false)
    oldSpatial.getParent != null && oldSpatial.removeFromParent
    drawWeapon(newWep)
    EventHub.trigger(WeaponDrawn(newWep))
  }

  def drawWeapon(newWep: Weapon) {
    val wepSpat = newWep.spatial
    wepSpat.setLocalTranslation(Vector3f.ZERO)
    wepSpat.setLocalRotation(Quaternion.DIRECTION_Z)
    getWeaponNode.attachChild(wepSpat)
    newWep.weaponControl.setEnabled(true)
  }

  def getWeaponNode = {
    val cam = spatial.asNode.getChild(NodeId.camera).asNode
    val playerWeapon = cam.getChild(NodeId.playerWeapon)
    if (playerWeapon == null) {
      val newNode = new Node(NodeId.playerWeapon)
      cam.attachChild(newNode)
      newNode.rotate(0, FastMath.HALF_PI, 0)
      newNode
    } else playerWeapon.asNode
  }

  def weaponControl(w: Weapon) = {
    val ctrl = w.weaponControl
    ctrl
  }

  def onEvent(event: GameEvent) = {
    println(s"Weapon ($this) event: $event")
    event match {
      case PlayerShoot(true) => current.weaponControl.pullTheTrigger
      case PlayerShoot(false) => current.weaponControl.looseTheTrigger
    }
  }

  def controlUpdate(tpf: Float) = {}
  def controlRender(rm: RenderManager, vp: ViewPort) = {}

}

