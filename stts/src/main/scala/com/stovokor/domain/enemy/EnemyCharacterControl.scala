package com.stovokor.domain.enemy

import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.bullet.PhysicsSpace
import com.stovokor.state.CollisionGroups
import com.jme3.math.Vector3f
import com.stovokor.jme.PortedBetterCharacterControl
import com.jme3.scene.Spatial

/**
 * @author xuan
 */

object EnemyCharacterControl {
  def apply(radius: Float, height: Float, mass: Float) = {
    val control = new EnemyCharacterControl(radius, height, mass)
//    control.getRigidBody.setCollisionGroup(CollisionGroups.level)
//    control.getRigidBody.setCollideWithGroups(CollisionGroups.level)
    control.setGravity(new Vector3f(0, -4000, 0)) //-9.81f // doesnt work???
    control.setPhysicsDamping(0.9f)
    control
  }
}

class EnemyCharacterControl(radius: Float, height: Float, mass: Float)
    extends PortedBetterCharacterControl(radius, height, mass) {
  override def setPhysicsSpace(aSpace: PhysicsSpace) = {
    if (aSpace == null && space != null)
      removePhysics(space)
    else
      super.setPhysicsSpace(aSpace)
  }

  override def checkOnGround() {}
  override def jmeClone() = new EnemyCharacterControl(radius, height, mass)
  
  override def cloneForSpatial(spatial:Spatial) = {
        val control =
                new EnemyCharacterControl(radius, height, mass);
        control.setJumpForce(jumpForce)
        control
    }
}