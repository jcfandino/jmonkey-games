package com.stovokor.bullet

import com.jme3.bullet.collision.PhysicsCollisionListener
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.scene.Spatial
import com.jme3.bullet.control.GhostControl
import com.jme3.bullet.collision.PhysicsCollisionObject
import akka.actor.ActorRef
import com.stovokor.actor.CollideWith
import com.jme3.bullet.collision.PhysicsCollisionGroupListener

class ActorCollisionListener extends PhysicsCollisionListener {

  def actorOf(body: PhysicsCollisionObject) = {
    if (body.getUserObject != null && body.getUserObject.isInstanceOf[ActorRef])
      Some(body.getUserObject.asInstanceOf[ActorRef])
    else None
  }

  def collision(event: PhysicsCollisionEvent) = {
    val actorA = actorOf(event.getObjectA)
    val actorB = actorOf(event.getObjectB)
    //    if (!event.getObjectB.getUserObject.isInstanceOf[Spatial])
    //      println(s"col ${event.getObjectA.getUserObject} - ${event.getObjectB.getUserObject}")
    for (a <- actorA; b <- actorB) {
      println(s"Collision $a - $b")
      // TODO doc says not to use the event outside this method.
      a ! CollideWith(b, event)
      b ! CollideWith(a, event)
    }
  }

}