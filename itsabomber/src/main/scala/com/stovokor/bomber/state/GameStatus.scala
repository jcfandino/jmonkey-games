package com.stovokor.bomber.state

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.bullet.control.GhostControl
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.stovokor.bomber.control.ExplosionControl
import com.stovokor.bomber.control.PlaneControl
import com.stovokor.bomber.factory.BombFactory
import com.stovokor.bomber.factory.ExplosionFactory
import com.stovokor.bomber.factory.ShotFactory
import com.stovokor.bomber.control.ShotControl
import com.stovokor.bomber.control.FlakControl

object GameStatus extends SimpleAppState {

  val initLives = 1
  var lives = initLives

  var plane: PlaneControl = null

  def health = if (plane != null) (100f * plane.health / plane.initialHealth).toInt else 0

  var score = 0
  var notice = ""

  var bombCache: List[Spatial] = List()
  var explosionCache: List[Spatial] = List()
  var bulletCache: List[Spatial] = List()

  var mapRoot: Node = null

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    mapRoot = rootNode.getChild("map-root").asInstanceOf[Node]
    plane = rootNode.getChild("plane-node").getControl(classOf[PlaneControl])
    space.addCollisionListener(PlaneCrashListener)
    preloadObjects()
  }

  override def cleanup {
    space.removeCollisionListener(PlaneCrashListener)
    lives = initLives
    score = 0
    mapRoot = null
    plane = null
  }

  def preloadObjects() {
    if (bombCache.isEmpty) {
      bombCache = (1 to 50)
        .map(i => BombFactory.create(assetManager, Vector3f.ZERO))
        .toList
    }
    if (explosionCache.isEmpty) {
      explosionCache = (1 to 50)
        .map(i => ExplosionFactory.create(assetManager, Vector3f.ZERO))
        .toList
    }
    if (bulletCache.isEmpty) {
      bulletCache = (1 to 200)
        .map(i => ShotFactory.create(assetManager, Vector3f.ZERO))
        .toList
    }
  }

  override def update(tpf: Float) {
    if (plane != null && plane.health <= 0) {
      lives -= 1
    }
  }

  def reset() {
    lives = initLives
    score = 0
    mapRoot = null
    plane = null
  }

  def recycleBomb(b: Spatial) {
    b.removeFromParent()
    val body = b.getControl(classOf[RigidBodyControl])
    if (body != null) {
      body.setLinearVelocity(new Vector3f)
      body.setAngularVelocity(new Vector3f)
      body.setPhysicsRotation(new Quaternion)
      body.setPhysicsSpace(null)
    }
    bombCache = bombCache ++ List(b)
  }

  def explode(pos: Vector3f, friendly: Boolean = true) {
    if (!explosionCache.isEmpty) {
      val exp = explosionCache.head
      explosionCache = explosionCache.tail
      exp.getControl(classOf[ExplosionControl]).reset
      exp.getControl(classOf[ExplosionControl]).setFriendly(friendly)
      mapRoot.attachChild(exp)
      exp.setLocalTranslation(pos.subtract(mapRoot.getLocalTranslation))
      space.add(exp)
      val ghost = exp.getControl(classOf[GhostControl])
      if (ghost != null) {
        ghost.setPhysicsLocation(pos)
      }
    }
  }

  val bombImp = new Vector3f(.05f, 0f, 0f)
  val bombAxis = new Vector3f(.0f, .1f, 0f)

  def dropBomb(pos: Vector3f) {
    if (!bombCache.isEmpty) {
      val bomb = bombCache.head
      bombCache = bombCache.tail
      rootNode.attachChild(bomb)
      space.add(bomb)
      val body = bomb.getControl(classOf[RigidBodyControl])
      if (body != null) {
        body.setPhysicsLocation(pos.subtract(0f, 0.2f, 0f))
        body.clearForces()
        body.applyImpulse(bombImp, bombAxis)
      }
    }
  }

  def addShot(pos: Vector3f, moveRight: Vector3f, flakAttitude: Float = 0f) {
    if (!bulletCache.isEmpty) {
      val bullet = bulletCache.head
      bullet.getControl(classOf[ShotControl]).setDir(moveRight)
      if (flakAttitude > 0f) {
        bullet.addControl(new FlakControl(flakAttitude))
      }
      bulletCache = bulletCache.tail
      rootNode.attachChild(bullet)
      bullet.setLocalTranslation(pos)
      space.add(bullet)
    }
  }

  def recycleShot(shot: Spatial) {
    shot.removeFromParent()
    bulletCache = shot :: bulletCache
    val ghost = shot.getControl(classOf[GhostControl])
    if (ghost != null) {
      ghost.setPhysicsSpace(null)
    }
    val flak = shot.getControl(classOf[FlakControl])
    if (flak != null) {
      shot.removeControl(flak)
    }
  }

  def recycleExplosion(exp: Spatial) {
    exp.removeFromParent()
    explosionCache = exp :: explosionCache
    val ghost = exp.getControl(classOf[GhostControl])
    if (ghost != null) {
      ghost.setPhysicsSpace(null)
    }
  }

  def planeCrash() {
    lives -= 1
    //    plane.resetPosition()
  }

  def earnPoints(p: Int) {
    score += p
  }
}
