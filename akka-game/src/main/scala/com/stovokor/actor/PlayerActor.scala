package com.stovokor.actor

import akka.actor.Actor
import akka.actor.Props
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import com.jme3.math.FastMath

class PlayerActor(var pos: Vector3f, rot: Quaternion) extends Actor {

  var weapon = context.actorOf(Props(new WeaponActor()), "weapon")

  override def preStart() {
  }

  def receive = {
    case PlayerMove(delta) => {
      pos.addLocal(delta)
    }
    case AskPosition => sender ! PositionResponse(pos)
    case Shoot(dir) => {
      weapon ! ShootBullet(pos, dir)
    }
  }
}