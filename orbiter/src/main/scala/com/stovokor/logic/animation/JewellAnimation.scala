package com.stovokor.logic.animation

import com.jme3.scene.Spatial
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f

class JewelAnimation(spatial: Spatial) extends Animation {

  def updated(tpf: Float) = {
    spatial.rotate(new Quaternion().fromAngleAxis(4f * tpf, Vector3f.UNIT_Y))
    this
  }

}