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

case class SpatialActorSpawnRequest(pos: Vector3f, actor: ActorRef, factory: AssetManager => Spatial)

class SpatialActorSpawner(systemState: ActorSystemState) extends Actor {

  def receive = {
    case SpatialActorSpawnRequest(pos, actor, factory) => {
      val spatial = factory(systemState.assetManager)
      systemState.enqueueMain(() => {
        spatial.addControl(new SpatialActorControl(systemState, actor))
        spatial.setLocalTranslation(pos)
        systemState.rootNode.attachChild(spatial)
      })
    }
  }
}