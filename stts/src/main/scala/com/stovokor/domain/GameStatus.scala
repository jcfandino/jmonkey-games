package com.stovokor.domain

import scala.collection.immutable.Map

import com.jme3.scene.Spatial
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.domain.item.Item
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.LevelChange

class GameStatus(val seed: Long, var playerStatus: PlayerStatus) extends Serializable {

  def this(seed: Long) = this(seed, new PlayerStatus)

  var levelNumber: Int = 1
  var ringAccess: Int = 1
  var accessCards: Set[String] = Set.empty

  def newGame = playerStatus.weapons.isEmpty

  // TODO this has to be a cache containing the current level only
  // Others should be read from file system
  var levelsStatus: Map[Int, Option[LevelStatus]] =
    Map.empty.withDefaultValue(None)

  def getLevelStatus(level: Level) = {
    if (levelsStatus(level.number).isEmpty) {
      val newStatus = new LevelStatus(seed, level)
      levelsStatus = levelsStatus.updated(level.number, Some(newStatus))
    }
    levelsStatus(level.number).get
  }

  def restartLevel = {
    changeLevelTo(levelNumber)
  }
  def advanceExitA = {
    changeLevelTo(levelNumber * 2)
  }
  def advanceExitB = {
    changeLevelTo(levelNumber * 2 + 1)
  }
  def turnBackLevel = {
    changeLevelTo(levelNumber / 2)
  }
  def changeLevelTo(next: Int) = {
    EventHub.trigger(LevelChange(levelNumber, next))
    levelNumber = next
  }

  def addAccessCard(key: String) = {
    accessCards = Set(key) ++ accessCards
    println(s"Access cards: $accessCards")
  }
}

class LevelStatus(
  val seed: Long,
  val level: Level) extends Serializable {

}

class PlayerStatus(
  var health: Int = 0,
  var weapons: List[Weapon] = List(),
  var armor: Int = 0,
  var ammo: Map[Ammo, Int] = Map()) extends Serializable {

  def currentWeapon = weapons.headOption

  def getAmmo(cat: Ammo) = ammo.getOrElse(cat, 0)
  def setAmmo(cat: Ammo, value: Int) = ammo = ammo.updated(cat, value)

}

