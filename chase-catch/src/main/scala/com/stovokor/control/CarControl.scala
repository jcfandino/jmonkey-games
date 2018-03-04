package com.stovokor.control

import com.jme3.bullet.control.VehicleControl
import com.jme3.input.InputManager
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.math.Matrix3f
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.renderer.ViewPort
import com.jme3.scene.control.AbstractControl
import com.jme3.bullet.control.RigidBodyControl

object CarControl {

  def apply(inputManager: InputManager) = {
    val ctrl = new CarControl(inputManager)
    ctrl.setupKeys
    ctrl
  }

}

class CarControl(inputManager: InputManager)
    extends AbstractControl
    with ActionListener {

  val initialHealth = 20000f
  var health = initialHealth

  def controlUpdate(tpf: Float) {
    if (getSpatial.getLocalTranslation.z < -1f) {
      reset(position = true, resetHealth = true)
    }
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  def setupKeys() {
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

  val steerAmount = 0.6f
  val accelerationAmount = -800f
  val reverseAccelerationAmount = 400f
  val breakAmount = 60f

  var steeringValue = 0f
  var accelerationValue = 0f

  def getSpeedSquared = player.getLinearVelocity.lengthSquared

  def crash(damage: Float) {
    health -= damage
    println(s"Crashed $damage - life $health")
  }

  def reset(position: Boolean, resetHealth: Boolean = true) {
    if (position) {
      player.setPhysicsLocation(new Vector3f(2f, 2f, 0.5f))
    }
    if (resetHealth) {
      health = initialHealth
    }
    player.setPhysicsRotation(new Matrix3f())
    player.setLinearVelocity(Vector3f.ZERO)
    player.setAngularVelocity(Vector3f.ZERO)
    player.resetSuspension()
  }

  def onAction(binding: String, value: Boolean, tpf: Float) {
    (binding, value) match {
      case ("Left", true)   => steeringValue += steerAmount
      case ("Left", false)  => steeringValue -= steerAmount
      case ("Right", true)  => steeringValue -= steerAmount
      case ("Right", false) => steeringValue += steerAmount
      case ("Up", true)     => accelerationValue += accelerationAmount
      case ("Up", false)    => accelerationValue -= accelerationAmount
      case ("Down", true) =>
        if (getSpeedSquared > 1f) {
          player.brake(breakAmount)
        } else {
          player.brake(0f)
          accelerationValue += reverseAccelerationAmount
        }
      case ("Down", false) => {
        player.brake(0)
        if (accelerationValue > 0f)
          accelerationValue -= reverseAccelerationAmount
      }
      case ("Space", true) => {
        player.brake(2, 300f)
        player.brake(3, 300f)
        player.getWheel(2).setFrictionSlip(0.2f)
        player.getWheel(3).setFrictionSlip(0.2f)
        if (getSpeedSquared > 1f) {
          val factor = Math.min(600f, getSpeedSquared * 10f)
          println(s"factor ${factor}")
          val imp = new Vector3f(steeringValue, 0f, 0f)
            .normalizeLocal().multLocal(factor)
          player.applyImpulse(imp, new Vector3f(0f, -.5f, 0))
        }
      }
      case ("Space", false) => {
        player.brake(2, 0f)
        player.brake(3, 0f)
        player.getWheel(2).setFrictionSlip(1.1f)
        player.getWheel(3).setFrictionSlip(1.1f)
      }
      case ("Reset", false) => {
        reset(position = false, resetHealth = false)
      }
      case _ =>
    }
    player.accelerate(accelerationValue)
    player.steer(steeringValue)
  }

  def body = getSpatial.getControl(classOf[RigidBodyControl])
  def player = getSpatial.getControl(classOf[VehicleControl])
}