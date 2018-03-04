package com.stovokor.domain

import com.jme3.animation.SkeletonControl
import com.jme3.bullet.collision.shapes.SimplexCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.math.FastMath
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.Control
import com.stovokor.ai.Navigation
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.domain.enemy.EnemyCharacterControl
import com.stovokor.domain.enemy.EnemyControl
import com.stovokor.domain.enemy.EnemySpecs
import com.stovokor.domain.enemy.EnemyTactic
import com.stovokor.state.CollisionGroups
import com.stovokor.state.MovementControl
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.PlayerShoot
import com.jme3.scene.BatchNode

object CharacterBuilder {
  def apply() = new CharacterBuilder
}

class Container {
  var model: Spatial = null
  var collisionRad: Float = 5
  var collisionHeight: Float = 5
  var pos: Vector3f = Vector3f.ZERO
  var angle: Float = 0
  var massVal: Float = 0
  var weaponTemplate: Weapon = null
  var mvCtrl: MovementControl = null
  var nav: Navigation = null
  var tactic: EnemyTactic = null
  var health: Int = 10
  var armor: Int = 0
  var speed: Int = 10
  var status: PlayerStatus = null
}
class CharacterBuilder {
  val c = new Container()

  val baseGroup = CollisionGroups.level
  val iaGroup = CollisionGroups.ai
  val itemGroup = CollisionGroups.items

  def on(coor: Vector3f) = { c.pos = coor; this }
  def angle(ang: Float) = { c.angle = ang; this }
  def collision(r: Float, h: Float) = { c.collisionRad = r; c.collisionHeight = h; this }
  def collision(r: Float) = { c.collisionRad = r; c.collisionHeight = r; this }
  def spatialModel(mod: Spatial) = { c.model = mod; this }
  def mass(m: Float) = { c.massVal = m; this }
  def weapon(w: Weapon) = { c.weaponTemplate = w; this }
  def withMovementControl(ctrl: MovementControl) = { c.mvCtrl = ctrl; this }
  def navigation(n: Navigation) = { c.nav = n; this }
  def tactic(t: EnemyTactic) = { c.tactic = t; this }
  def health(h: Int) = { c.health = h; this }
  def armor(a: Int) = { c.armor = a; this }
  def speed(s: Int) = { c.speed = s; this }
  def playerStatus(s: PlayerStatus) = { c.status = s; this }

  def asEnemy: EnemyCharacter = {
    def builder(m: Node, point: Control): EnemyCharacter = {
      val cloned = c.model.clone
      //      cloned.asInstanceOf[BatchNode].batch()

      val skeletonControl = cloned
        .asInstanceOf[Node].getChild("skeleton")
        .getControl(classOf[SkeletonControl])
      val hand = skeletonControl.getAttachmentsNode("Palm.R")

      //Weapon
      val weapon = c.weaponTemplate.clone
      val wcon = new EnemyWeaponControl(
        weapon,
        // TODO implement this f() in the ctrl
        // It is duplicated in here
        () => {
          val location = m.getLocalTranslation().add(0, 5, 0) //hand.getWorldTranslation()
          (location, m.getLocalRotation.getRotationColumn(2))
        })
      weapon.spatial.addControl(wcon)

      weapon.spatial.rotate(0, 0, -FastMath.PI / 2)
      hand.attachChild(weapon.spatial);

      m.attachChild(cloned)

      val specs = new EnemySpecs(c)
      val control = EnemyControl(weapon, cloned, c.nav, specs)
      m.addControl(control)

      // Character control
      val charControl = EnemyCharacterControl(c.collisionRad, c.collisionHeight, c.massVal)
      m.addControl(charControl)

      // Set View Direction
      val dirX = if (c.angle.abs < FastMath.HALF_PI) 1f else -1f
      val dirZ = Math.tan(c.angle).toFloat
      charControl.setViewDirection(new Vector3f(dirX, 0, dirZ).normalizeLocal())

      new EnemyCharacter(m, control, weapon, point)
    }
    build(NodeId.enemy, builder)
  }

  def asPlayer(): PlayerCharacter = {
    def builder(node: Node, point: Control): PlayerCharacter = {

      // Character control
      val charControl = PlayerCharacterControl(c.collisionRad, c.collisionHeight, c.massVal)
      node.addControl(charControl)

      // Movement control
      node.addControl(c.mvCtrl)

      // Player control
      val control = new PlayerControl
      node.addControl(control)

      // Camera node
      val camNode = new Node(NodeId.camera)
      node.attachChild(camNode)

      // Weapon stash
      val stash = new WeaponStashControl(c.status, c.mvCtrl.positionAndDirection _)
      EventHub.subscribe(stash, PlayerShoot(true))
      EventHub.subscribe(stash, PlayerShoot(false))
      node.addControl(stash)

      // Weapon
      if (c.weaponTemplate != null) {
        c.weaponTemplate.spatial.removeControl(classOf[WeaponControl])
        val weapon = c.weaponTemplate.clone
        //        val weaponControl = new PlayerWeaponControl(
        //          stash, () => c.mvCtrl.positionAndDirection)

        // Add to the playerWeapon node
//        val playerWeapon = new Node(NodeId.playerWeapon)
//        camNode.attachChild(playerWeapon)
//        playerWeapon.rotate(0, FastMath.HALF_PI, 0)
        //        playerWeapon.addControl(weaponControl)
        stash.store(weapon)
      }

      // Center point for ia and items
      val pointNode = new Node(NodeId.playerPoint)
      pointNode.setLocalTranslation(0, 1f, 0)
      pointNode.addControl(point)
      node.attachChild(pointNode)

      val pc = new PlayerCharacter(node, control, stash, List(point))
      pc.control.setHealth(c.health)
      pc.control.setArmor(c.armor)
      pc
    }
    build(NodeId.player, builder)
  }

  private def build[T <: Entity[_]](id: String, f: (Node, Control) => T): T = {
    val node = new Node(id)
    node.setLocalTranslation(c.pos)

    // Create another ghost for items
    val point = new GhostControl(new SimplexCollisionShape(new Vector3f(0, 0, 0)))
    point.setCollisionGroup(itemGroup)
    point.setCollideWithGroups(itemGroup)

    f(node, point)
  }

}

