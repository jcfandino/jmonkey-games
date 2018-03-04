package com.stovokor.state

import java.util.concurrent.Callable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.concurrent.duration.DurationInt

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.math.Vector3f
import com.stovokor.actor.EnemyActor
import com.stovokor.actor.PhysicsActorSpawner
import com.stovokor.actor.PlayerActor
import com.stovokor.actor.SpatialActorSpawnRequest
import com.stovokor.actor.PhysicsActorSpawnRequest
import com.stovokor.actor.SpatialActorSpawner
import com.stovokor.actor.Tick
import com.stovokor.factory.EnemyFactory
import com.stovokor.factory.PlayerFactory

import akka.actor.ActorSystem
import akka.actor.Props
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.math.Quaternion
import com.jme3.bullet.BulletAppState
import com.stovokor.bullet.ActorCollisionListener
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.PhysicsSpace

class ActorSystemState extends BaseState {

  val system = ActorSystem("Game")
  val tickPeriod = (1.0 / 60)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)

    val spatialSpawner = system.actorOf(Props(new SpatialActorSpawner(this)), "spatialSpawner")
    val physicsSpawner = system.actorOf(Props(new PhysicsActorSpawner(this)), "physicSpawner")

    val playerPos = new Vector3f(4, 3, 2)
    val player = system.actorOf(Props(new PlayerActor(playerPos, new Quaternion())), "player")
    spatialSpawner ! SpatialActorSpawnRequest(playerPos, player, PlayerFactory.create)
    physicsSpawner ! PhysicsActorSpawnRequest(playerPos, player, PlayerFactory.createPhysics)

    val enemyPos = new Vector3f(8, 3, 2)
    val enemy = system.actorOf(Props(new EnemyActor(enemyPos, new Quaternion())), "enemy")
    spatialSpawner ! SpatialActorSpawnRequest(enemyPos, enemy, EnemyFactory.create)
    physicsSpawner ! PhysicsActorSpawnRequest(enemyPos, enemy, EnemyFactory.createPhysics)

    system.scheduler.schedule(0.seconds, tickPeriod.seconds, () => {
      system.actorSelection("/user/*") ! Tick(tickPeriod)
    })

    val space = stateManager.getState(classOf[BulletAppState])
      .getPhysicsSpace
    space.addCollisionListener(new ActorCollisionListener)
    space.addTickListener(new PhysicsTickListener {
      def physicsTick(space: PhysicsSpace, tpf: Float) = {
        //space.get
      }

      def prePhysicsTick(space: PhysicsSpace, tpf: Float) = {
      }
    })
  }

  override def update(tpf: Float) {
  }

  override def cleanup() {
    system.terminate()
  }

  def enqueueMain(action: () => Unit) {
    app.enqueue(new Callable[Unit]() {
      def call = action()
    })
  }
  }