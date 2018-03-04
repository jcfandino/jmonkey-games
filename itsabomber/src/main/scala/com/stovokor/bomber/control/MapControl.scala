package com.stovokor.bomber.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.jme3.scene.Node
import com.jme3.bullet.control.RigidBodyControl
import scala.collection.JavaConversions._
import com.jme3.scene.Spatial.CullHint

object MapControl {
  def apply(batchWidth: Float) = new MapControl(batchWidth)
}

class MapControl(batchWidth: Float) extends AbstractControl {

  val speed = 1f
  def controlUpdate(tpf: Float) {
    node.move(-speed * tpf, 0, 0)
  }

  def node = getSpatial.asInstanceOf[Node]

  def controlRender(rm: RenderManager, vp: ViewPort) {}

}