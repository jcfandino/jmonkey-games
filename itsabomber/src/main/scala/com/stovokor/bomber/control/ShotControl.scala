package com.stovokor.bomber.control

import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.RenderManager
import com.stovokor.bomber.state.GameStatus
import com.jme3.math.Vector3f
import com.stovokor.bomber.factory.ShotType

object ShotControl {
  def apply() = new ShotControl
}

class ShotControl extends AbstractControl {

  val speedAbs = 10f

  var vector = Vector3f.UNIT_X.mult(speedAbs)
  var shotType = ShotType.Light

  def friendly = vector.x > 0f

  def setDir(v: Vector3f) {
    vector = v.normalize.mult(speedAbs)
  }
  def controlUpdate(tpf: Float) {
    getSpatial.move(vector.mult(tpf))
    val x = getSpatial.getLocalTranslation.x
    if (x < 0f || x > 12f) recycle()
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  def recycle() {
    GameStatus.recycleShot(getSpatial)
  }

  def collide() {
    val flak = getSpatial.getControl(classOf[FlakControl])
    if (flak != null) {
      flak.explode()
    } else {
      recycle()
    }
  }

}
