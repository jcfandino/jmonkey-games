package com.stovokor.actor

import akka.actor.Actor
import akka.actor.ActorRef
import com.jme3.asset.AssetManager
import com.jme3.scene.Spatial
import com.jme3.scene.Node
import com.stovokor.state.ActorSystemState
import com.stovokor.control.SpatialActorControl
import java.util.concurrent.Callable
import com.jme3.math.Vector3f
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.BulletAppState
import com.stovokor.bullet.ActorSyncPhysicsListener

case class PhysicsActorSpawnRequest(pos: Vector3f, actor: ActorRef, factory: () => PhysicsRigidBody)

class PhysicsActorSpawner(systemState: ActorSystemState) extends Actor {

  def receive = {
    case PhysicsActorSpawnRequest(pos, actor, factory) => {
      val body = factory()
      //TODO ask the actor for the pos
      systemState.enqueueMain(() => {
        val state = systemState.stateManager.getState(classOf[BulletAppState])
        val space = state.getPhysicsSpace
        body.setUserObject(actor)
        body.setPhysicsLocation(pos)
        space.add(body)
        space.addTickListener(new ActorSyncPhysicsListener(actor, body))
      })
    }
  }
}