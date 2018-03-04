package com.stovokor.domain

import com.jme3.bullet.control.RigidBodyControl
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.stovokor.util.jme.HasSpatialState

class Prop(model: Spatial, control: PropControl) extends Entity(model, control, Some(model))

object PropControl {
  def apply(s: Spatial, m: Float): PropControl = {
    val ctrl = new PropControl(m)
    s.addControl(ctrl)
    s.addControl(new RigidBodyControl(0))
    val isPropActive = ctrl.is("propActive")
    if (isPropActive) ctrl.activateProp
    ctrl
  }
}

class PropControl(val activeMass: Float) extends AbstractControl with HasSpatialState {

  def controlRender(x$1: com.jme3.renderer.RenderManager, x$2: com.jme3.renderer.ViewPort): Unit = {}

  def controlUpdate(x$1: Float): Unit = {}

  def isPropActive = is("propActive")
  def activateProp = {
    if (!isPropActive) {
      println("Prop activated")
      set("propActive", true)
      val space = spatial.getControl(classOf[RigidBodyControl]).getPhysicsSpace
      space.remove(spatial)
      spatial.removeControl(classOf[RigidBodyControl])
      spatial.addControl(new RigidBodyControl(activeMass))
      space.add(spatial)
    }
  }
}