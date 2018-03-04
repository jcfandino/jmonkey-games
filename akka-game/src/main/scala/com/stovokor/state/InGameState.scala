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
import com.stovokor.factory.TestMapFactory
import com.stovokor.factory.PlayerFactory
import com.jme3.scene.Spatial
import com.stovokor.control.PlayerControl
import com.jme3.bullet.control.BetterCharacterControl

class InGameState extends AbstractAppState {

  var physicsState: BulletAppState = null
  var assetManager: AssetManager = null
  var app: SimpleApplication = null
  var rootNode: Node = null
  var space: PhysicsSpace = null
//  var player: Spatial = null
  def cam = app.getCamera

  val mapWidth = 100
  val mapHeight = 100

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    physicsState = stateManager.getState(classOf[BulletAppState])
    app = simpleApp.asInstanceOf[SimpleApplication]
    assetManager = app.getAssetManager
    rootNode = app.getRootNode
    space = physicsState.getPhysicsSpace
    physicsState.setDebugEnabled(true)

    space.setGravity(new Vector3f(0f, 0f, -10f))
    app.getFlyByCamera.setEnabled(false)

    // map
    val map = TestMapFactory.create(assetManager, mapWidth, mapHeight)
    rootNode.attachChild(map)
    space.add(map)
  }

  override def update(tpf: Float) {
  }

}