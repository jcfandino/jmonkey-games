package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.control.VehicleControl
import com.jme3.math.FastMath
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.stovokor.control.CarControl
import com.stovokor.factory.CarFactory
import com.stovokor.factory.ExplosionFactory
import com.stovokor.factory.StoryFactory
import com.stovokor.factory.TestMapFactory

class InGameState extends AbstractAppState {

  var physicsState: BulletAppState = null
  var assetManager: AssetManager = null
  var app: SimpleApplication = null
  var rootNode: Node = null
  var space: PhysicsSpace = null
  var car: Node = null
  def cam = app.getCamera

  val mapWidth = 100
  val mapHeight = 100

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    physicsState = stateManager.getState(classOf[BulletAppState])
    app = simpleApp.asInstanceOf[SimpleApplication]
    assetManager = app.getAssetManager
    rootNode = app.getRootNode
    space = physicsState.getPhysicsSpace

    space.setGravity(new Vector3f(0f, 0f, -10f))
    app.getFlyByCamera.setEnabled(false)

    // map
    val map = TestMapFactory.create(assetManager, mapWidth, mapHeight)
    rootNode.attachChild(map)
    space.add(map)

    // car
    car = CarFactory.create(assetManager, app.getInputManager)
    rootNode.attachChild(car)
    space.add(car)
    physicsState.getPhysicsSpace.addCollisionListener(new CarCrashListener)
    app.getStateManager.attach(CarHealthDisplayState)

    // story
    val story = StoryFactory.create
    app.getStateManager.attach(new MissionState(new MissionDispatcher(story)))
  }

  var timeSinceDead = 0f
  var timeToRespawh = 2f

  override def update(tpf: Float) {
    updateCamera(tpf)
    val cc = car.getControl(classOf[CarControl])
    if (cc.health <= 0) {
      if (timeSinceDead == 0f) {
        cc.setEnabled(false)
        car.getControl(classOf[VehicleControl]).setEnabled(false)
        cc.reset(position = false, resetHealth = false)
        var pos = car.getLocalTranslation
        val exp = ExplosionFactory.create(assetManager, pos.add(0f, 0f, 1f))
        rootNode.attachChild(exp)
      }
      timeSinceDead += tpf
      if (timeSinceDead > timeToRespawh) {
        timeSinceDead = 0f
        car.getControl(classOf[VehicleControl]).setEnabled(true)
        cc.setEnabled(true)
        cc.reset(position = true, resetHealth = true)
      }
    }
  }

  def updateCamera(tpf: Float) {
    val cc = car.getControl(classOf[CarControl])
    val ss = cc.getSpeedSquared
    val ch = Math.max(8, Math.min(20, FastMath.sqr(ss / 20)))
    cam.setLocation(car.getLocalTranslation.add(0f, 0f, ch))
    cam.lookAt(car.getLocalTranslation, Vector3f.UNIT_Y)
    cam.update
  }

}