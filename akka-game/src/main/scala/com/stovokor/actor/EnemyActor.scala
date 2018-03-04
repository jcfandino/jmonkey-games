package com.stovokor.actor

import akka.actor.Actor
import com.jme3.math.FastMath
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion

class EnemyActor(var pos: Vector3f, var rot: Quaternion) extends Actor {

  var health: Float = 3f

  def receive = {
    case AskPosition => sender ! PositionResponse(pos)
    case LocationUpdate(newPos) => {
      pos.set(newPos)
    }
    case Tick(t) => {
      //        dirAngle += t * FastMath.QUARTER_PI
      //        x += 10 * t * Math.sin(dirAngle)
      //        y += 10 * t * Math.cos(dirAngle)
    }
    case Hit(damage) => {
      health -= damage
      println(s"Enemy: I'm hit. health left: $health")
      if (health <= 0f) {
        println(s"Enemy: I'm dead!")
        context.stop(self)
      }
    }
  }
}