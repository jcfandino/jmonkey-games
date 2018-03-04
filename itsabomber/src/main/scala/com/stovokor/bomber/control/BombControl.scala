package com.stovokor.bomber.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.jme3.bullet.control.RigidBodyControl
import com.stovokor.bomber.state.GameStatus

object BombControl {
  def apply() = new BombControl
}

class BombControl extends AbstractControl {

  def controlUpdate(tpf: Float) {
    val pos = getSpatial.getLocalTranslation
    if (pos.y < -1f) {
      println(s"killing bomb $pos")
      GameStatus.recycleBomb(getSpatial)
      //      val body = getSpatial.getControl(classOf[RigidBodyControl])
      //      if (body != null) body.setPhysicsSpace(null)
      //      getSpatial.removeFromParent()
    }
  }
  
  def explode() {
    GameStatus.explode(getSpatial.getLocalTranslation)
    GameStatus.recycleBomb(getSpatial)
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

}