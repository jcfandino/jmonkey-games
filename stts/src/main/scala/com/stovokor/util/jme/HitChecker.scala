package com.stovokor.util.jme

import com.jme3.bullet.collision.PhysicsRayTestResult
import com.jme3.bullet.BulletAppState
import com.jme3.scene.Spatial
import com.jme3.math.Vector3f
import scala.collection.JavaConversions._
import java.util.ArrayList

object HitChecker {
  def checkHit(
    physicsState: BulletAppState,
    location: Vector3f,
    direction: Vector3f,
    maxDistance: Float,
    action: Spatial => Unit): Boolean = {
    val rayTest = physicsState
      .getPhysicsSpace()
      .rayTest(location, location.add(direction.mult(maxDistance)), new ArrayList())
    if (rayTest.size() > 0)
      validateHit(location, direction, sort(rayTest.toList), action)
    else false
  }

  def sort(list: List[PhysicsRayTestResult]) = {
    list
      .sortBy(n => n.getHitFraction)
      .filter(p => p.getCollisionObject.getUserObject.isInstanceOf[Spatial])
  }
  def headAsSpatial(list: List[PhysicsRayTestResult]) = {
    list.head.getCollisionObject.getUserObject.asInstanceOf[Spatial]
  }

  def validateHit(location: Vector3f, direction: Vector3f, result: List[PhysicsRayTestResult], action: Spatial => Unit): Boolean = {
    if (!result.isEmpty) {
      action(headAsSpatial(result))
      true
    } else false
  }

}