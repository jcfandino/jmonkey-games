package com.stovokor.actor

import akka.actor.Props
import com.jme3.math.Vector3f
import com.jme3.math.Quaternion
import akka.actor.ActorRef
import com.jme3.bullet.collision.PhysicsCollisionEvent

case class PlayerMove(delta: Vector3f)
case object AskPosition
case class PositionResponse(pos: Vector3f)
case class Tick(t: Double)
case class Shoot(dir: Vector3f)
case class ShootBullet(pos: Vector3f, dir: Vector3f)
case class LocationUpdate(pos: Vector3f)
case class CollideWith(other: ActorRef, event: PhysicsCollisionEvent)
case class Hit(damage: Float)
