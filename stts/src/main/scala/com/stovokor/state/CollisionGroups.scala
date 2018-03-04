package com.stovokor.state

import com.jme3.bullet.collision.PhysicsCollisionObject

/**
 * @author xuan
 */
object CollisionGroups {
  
  val level = PhysicsCollisionObject.COLLISION_GROUP_01
  val ai = PhysicsCollisionObject.COLLISION_GROUP_02
  val items = PhysicsCollisionObject.COLLISION_GROUP_03
}