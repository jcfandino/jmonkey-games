package com.stovokor.actor

import akka.actor.Actor
import akka.actor.Props
import com.stovokor.factory.BulletFactory
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion

class WeaponActor extends Actor {

  var bullets = 100

  def receive = {
    case ShootBullet(pos, dir) => {
      if (bullets > 0) {
        println(s"shooting $bullets left")
        bullets -= 1
        val bulPos = pos.add(dir.mult(1f))
        val bullet = context.system.actorOf(
          Props(new BulletActor(bulPos, dir)), "bullet" + System.currentTimeMillis())

        val message1 = SpatialActorSpawnRequest(bulPos, bullet, BulletFactory.create)
        context.actorSelection("/user/spatialSpawner") ! message1

        val message2 = PhysicsActorSpawnRequest(bulPos, bullet, BulletFactory.createPhysics)
        context.actorSelection("/user/physicSpawner") ! message2
      } else {
        println("no more bullets")
      }
    }
  }
}