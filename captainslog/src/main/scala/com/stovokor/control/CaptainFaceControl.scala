package com.stovokor.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.jme3.scene.Node
import scala.collection.JavaConversions._
import com.jme3.scene.Spatial.CullHint
import com.stovokor.state.CaptainMood
import com.stovokor.state.CaptainMoodState
import com.stovokor.state.CaptainMoodState

class CaptainFaceControl extends AbstractControl {

  def controlUpdate(tpf: Float) {
    val label = CaptainMoodState.mood.label
    val node = getSpatial.asInstanceOf[Node]
    node.getChildren.foreach(c => c.setCullHint(CullHint.Always))
    node.getChild(s"face-$label").setCullHint(CullHint.Never)
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

}