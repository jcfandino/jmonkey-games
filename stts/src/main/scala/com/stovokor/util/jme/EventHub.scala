package com.stovokor.util.jme

import scala.collection.mutable.ListBuffer
import com.jme3.math.Vector3f
import com.stovokor.domain.Ammo
import com.stovokor.domain.Weapon
import com.stovokor.domain.enemy.EnemyControl
import com.jme3.scene.Spatial

object EventHub {

  var listeners: Map[GameEvent, Set[LogicEventListener]] =
    Map.empty.withDefaultValue(Set())

  var typeListeners: Map[Class[_ <: GameEvent], Set[LogicEventListener]] =
    Map.empty.withDefaultValue(Set())

  def subscribe(listener: LogicEventListener, event: GameEvent) = {
    listeners = listeners.updated(event, listeners(event) + listener)
  }
  def subscribeByType(listener: LogicEventListener, eventType: Class[_ <: GameEvent]) = {
    typeListeners = typeListeners.updated(eventType, typeListeners(eventType) + listener)
  }

  def remove(listener: LogicEventListener, event: GameEvent) = {
    listeners = listeners.updated(event, listeners(event) - listener)
  }

  def removeEvent(event: GameEvent) {
    listeners = listeners.updated(event, Set())
  }

  def removeEvents(clazz: Class[_ <: GameEvent]) {
    typeListeners = typeListeners.updated(clazz, Set())
  }

  def removeEvents(filter: GameEvent => Boolean) {
    for (e <- listeners.keys.filter(filter)) {
      listeners = listeners.updated(e, Set())
    }
  }

  def removeFromAll(listener: LogicEventListener) = {
    println(s"Unsubscribing event listener: $listeners")
    for (event <- listeners.keys) {
      println(s"removing from event $event")
      remove(listener, event)
    }
    for (e <- typeListeners.keys) {
      val currentSet = typeListeners(e)
      typeListeners = typeListeners.updated(e, currentSet - listener)
    }

    println(s"Finished unsubscribing event listener: $listeners")
  }

  def trigger(event: GameEvent) = {
    for (listener <- listeners(event))
      listener.onEvent(event)

    for (listener <- typeListeners(event.getClass))
      if (!listeners.contains(event))
        listener.onEvent(event)
  }
}

trait LogicEventListener {
  def onEvent(event: GameEvent)
}

abstract class GameEvent
case class PlayerShoot(val pressed: Boolean) extends GameEvent
case class PlayerDied() extends GameEvent
case class PlayerInteracts() extends GameEvent
case class SwitchPushed(val id: String) extends GameEvent
case class PickUpAccessCard(val key: String) extends GameEvent
case class LevelChange(val oldLevel: Int, val newLevel: Int) extends GameEvent
case class HealthChange(val newHealth: Int) extends GameEvent
case class ArmorChange(val newArmor: Int) extends GameEvent
case class AmmoChange(val ammoType: Ammo, val amount: Int) extends GameEvent
case class WeaponDrawn(val weapon: Weapon) extends GameEvent
case class DoorStateChange(val id: String, val closed: Boolean) extends GameEvent
case class EnemyDied(enemy: Spatial) extends GameEvent
case class ItemDropped(item: Spatial) extends GameEvent
case class LoudSound(position: Vector3f) extends GameEvent
case class SurfaceHit(particleType: String, position: Vector3f, normal: Vector3f) extends GameEvent
