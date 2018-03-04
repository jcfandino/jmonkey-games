package com.stovokor.state

import com.jme3.bullet.collision.PhysicsCollisionListener
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.stovokor.control.CarControl

class CarCrashListener extends PhysicsCollisionListener {
  val threshHold = 2000f
  def collision(e: PhysicsCollisionEvent) = {
//    println(s"collision found ${e.getAppliedImpulse} ${e.getNodeA} - ${e.getNodeB}")
    if (e.getAppliedImpulse > threshHold) {
      val a = e.getNodeA
      val b = e.getNodeB
      val both = List(a.getName, b.getName)
      if (both.contains("car") && both.contains("map-root")) {
        val ctrl = List(a,b).map(_.getControl(classOf[CarControl])).find(x => x != null)
        ctrl.foreach(_.crash(e.getAppliedImpulse))
      }
    }
  }
}