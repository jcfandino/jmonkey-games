package com.stovokor.domain

import com.jme3.scene.Node
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.SwitchPushed
import com.jme3.scene.Spatial
import com.stovokor.util.jme.HasSpatialState

class Switch(model: Node, control: SwitchControl)
  extends Entity[SwitchControl](model, control,Some( model),
    (l => { EventHub.subscribe(l, SwitchPushed(control.id)) })) {
  //def spatial = model.getChild(0)
  //  def id = control.id
}

class SwitchControl(val id: String) extends AbstractControl with IsInteractive with HasSpatialState {

  def controlUpdate(tpf: Float) = {
    val pushedTime = this.pushedTime
    if (pushed && ready) {
      pushState(false)
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) = {}

  def push = {
    pushState(true)
    EventHub.trigger(SwitchPushed(id))
    println("Pushed " + id)
  }

  def pushed = is("switchPushed")
  def pushedTime = getInteractionTime
  def pushState(st: Boolean) = set("switchPushed", st)

  // IsInteractive
  def setInteractionTime(t: Long) = set("switchPushedTime", t)
  def getInteractionTime = getOr("switchPushedTime", -1L)
  val minTimeBetweenInteractions = 1000L
  def receiveInteraction = push
}

object SwitchId {
  val levelExitA = "levelExitA"
  val levelExitB = "levelExitB"
  val levelBack = "levelBack"
}