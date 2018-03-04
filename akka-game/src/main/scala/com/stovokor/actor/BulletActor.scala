package com.stovokor.actor

import akka.actor.Actor
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion

class BulletActor(var loc: Vector3f, var dir: Vector3f) extends Actor {
  var ttl: Double = 10
  val speed = 10f

  def receive = {
    case AskPosition => sender ! PositionResponse(loc)
    case Tick(t) => {
      //      val dir = rot.mult(Vector3f.UNIT_X).normalize.mult(speed * t.toFloat)
      loc.addLocal(dir.mult(speed * t.toFloat))
      ttl -= t
      if (ttl <= 0) context.stop(self)
    }
    case CollideWith(other, event) => {
      other ! Hit(1)
      context.stop(self)
    }
  }
}