package com.stovokor.state

import java.util.concurrent.Callable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import com.jme3.app.Application
import com.jme3.app.state.AppStateManager
import com.jme3.input.InputManager
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.math.Vector3f
import com.stovokor.actor.AskPosition
import com.stovokor.actor.PlayerMove
import com.stovokor.actor.PositionResponse
import com.stovokor.actor.Shoot
import akka.pattern._

import akka.util.Timeout

class PlayerMovementState extends BaseState with ActionListener {

  var (up, down, left, right, shoot) = (false, false, false, false, false)

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    setupKeys(inputManager)
  }

  def system = app.getStateManager.getState(classOf[ActorSystemState]).system
  def player = system.actorSelection("/user/player") //.resolveOne(1.second)

  var lastDir: Vector3f = Vector3f.UNIT_X

  override def update(tpf: Float) {
    val direction = new Vector3f()
    if (left) direction.addLocal(-1, 0, 0)
    if (right) direction.addLocal(1, 0, 0)
    if (up) direction.addLocal(0, 1, 0)
    if (down) direction.addLocal(0, -1, 0)
    direction.multLocal(10 * tpf)
    if (direction.length() > 0f) {
      player ! PlayerMove(direction)
      lastDir = direction.normalize
    }
    if (shoot) {
      shoot = false
      player ! Shoot(lastDir)
    }
  }

  def setupKeys(inputManager: InputManager) {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT))
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT))
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP))
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN))
    inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_H))
    inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R), new KeyTrigger(KeyInput.KEY_RETURN))
    inputManager.addListener(this, "Left")
    inputManager.addListener(this, "Right")
    inputManager.addListener(this, "Up")
    inputManager.addListener(this, "Down")
    inputManager.addListener(this, "Space")
    inputManager.addListener(this, "Reset")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) {
    (binding, value) match {
      case ("Left", _)  => left = value
      case ("Right", _) => right = value
      case ("Up", _)    => up = value
      case ("Down", _)  => down = value
      case ("Space", _) => shoot = value
      case _            =>
    }
  }

}