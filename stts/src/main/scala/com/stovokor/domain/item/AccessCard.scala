package com.stovokor.domain.item

import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.PickUpAccessCard
import com.stovokor.domain.PlayerControl

//class AccessCard(val model: Node, val control: AccessCardControl) extends Item {
//}

class AccessCardControl(val key: String) extends AbstractControl with CanBePickedUp {
  def controlUpdate(tpf: Float) = {
  }

  def controlRender(rm: RenderManager, vp: ViewPort) = {}

  def doPickup(player: PlayerControl) = {
    println("Key picked up by player")
    EventHub.trigger(PickUpAccessCard(key))
    true
  }

}