package com.stovokor.math

import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import com.jme3.scene.Spatial

trait Trajectory {

  def getPoint(param: Param): Vector3f

  def getRotation(param: Param): Quaternion

  def updateSpatial(spatial: Spatial, param: Param) {
    spatial.setLocalTranslation(getPoint(param))
    spatial.setLocalRotation(getRotation(param))
  }
}