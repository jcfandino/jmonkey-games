package com.stovokor.bullet

import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.PhysicsSpace
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import com.jme3.bullet.objects.PhysicsRigidBody
import com.stovokor.actor.LocationUpdate
import com.stovokor.actor.AskPosition
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.jme3.math.Vector3f
import com.stovokor.actor.PositionResponse

class ActorSyncPhysicsListener(actor: ActorRef, body: PhysicsRigidBody) extends PhysicsTickListener {

  implicit val timeout = Timeout(1.seconds)

  def physicsTick(space: PhysicsSpace, tpf: Float) = {
    if (body.isKinematic) {
      val answer = actor ? AskPosition
      answer.foreach(pos => {
        val p = pos.asInstanceOf[PositionResponse]
        body.setPhysicsLocation(p.pos)
      })
    } else {
      val loc = body.getPhysicsLocation
      actor ! LocationUpdate(loc)
    }
  }

  def prePhysicsTick(space: PhysicsSpace, tpf: Float) = {
  }

}