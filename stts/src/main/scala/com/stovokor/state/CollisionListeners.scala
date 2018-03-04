package com.stovokor.state

import com.jme3.bullet.collision.PhysicsCollisionListener
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.scene.Spatial
import com.stovokor.domain.PlayerControl
import com.stovokor.domain.enemy.EnemyControl
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.domain.NodeId
import com.stovokor.domain.item.CanBePickedUp
import com.jme3.scene.control.Control
import com.stovokor.domain.PropControl
import com.jme3.bullet.control.GhostControl
import com.jme3.bullet.collision.PhysicsCollisionGroupListener

class PickupItemDetector extends PhysicsCollisionListener {

  def collision(event: PhysicsCollisionEvent) = {
    def notify(item: Spatial, player: Spatial) = {
      val itemControl = item.getControl(classOf[CanBePickedUp])
      val playerControl = player.getParent.getControl(classOf[PlayerControl])
      itemControl.pickUpBy(playerControl)
    }
    def isPickable(c: Spatial) = c.getControl(classOf[CanBePickedUp]) != null
    def isThePlayer(c: Spatial) = c.getName() == NodeId.playerPoint
    val a = event.getNodeA
    val b = event.getNodeB
    if (a != null && b != null) {
      if (isPickable(a) && isThePlayer(b)) notify(a, b)
      if (isThePlayer(a) && isPickable(b)) notify(b, a)
    }
  }
}
class PropCollisionDetector extends PhysicsCollisionListener {

  def collision(event: PhysicsCollisionEvent) = {
    def notify(prop: Spatial) = {
      prop.getControl(classOf[PropControl]).activateProp
    }
    def isProp(c: Spatial) = c.getControl(classOf[PropControl]) != null
    def isNotLevel(c: Spatial) = c.getName() != NodeId.level
    def isNotGhost(o: Any) = !o.isInstanceOf[GhostControl]
    val a = event.getNodeA
    val b = event.getNodeB
    if (a != null && b != null) {
      if (isProp(a) && isNotLevel(b) && isNotGhost(event.getObjectB)) notify(a)
      if (isProp(b) && isNotLevel(a) && isNotGhost(event.getObjectA)) notify(b)
    }
  }

}
