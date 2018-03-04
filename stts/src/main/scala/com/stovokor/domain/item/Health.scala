package com.stovokor.domain.item;

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.scene.Spatial
import com.jme3.bullet.control.RigidBodyControl
import com.stovokor.domain.PlayerControl

class HealthControl(healthPoints: Int) extends AbstractControl with CanBePickedUp {

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  def controlUpdate(tpf: Float) {}

  def doPickup(player: PlayerControl) = player.pickupHealth(healthPoints)
}
