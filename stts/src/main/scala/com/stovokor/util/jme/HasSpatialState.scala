package com.stovokor.util.jme

import com.jme3.math.Vector3f
import com.jme3.scene.Spatial

trait HasSpatialState {
  def getSpatial: Spatial
  def set[T: IsOk](key: String, value: T) { getSpatial.setUserData(key, value) }
  def get[T: IsOk](key: String): T = getSpatial.getUserData(key)
  def getOr[T: IsOk](key: String, default: T): T = Option(get(key)).getOrElse(default)
  def has(key: String) = getSpatial.getUserData(key) != null
  def is(key: String) = getSpatial.getUserData[Boolean](key) == true

  class IsOk[T]
  object IsOk {
    implicit object intIsOk extends IsOk[Int]
    implicit object longIsOk extends IsOk[Long]
    implicit object floatIsOk extends IsOk[Float]
    implicit object booleanIsOk extends IsOk[Boolean]
    implicit object stringIsOk extends IsOk[String]
    implicit object v3fIsOk extends IsOk[Vector3f]
  }
}

