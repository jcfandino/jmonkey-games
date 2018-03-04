package com.stovokor.bomber.state

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
import com.stovokor.bomber.factory.PlayerFactory
import com.jme3.input.InputManager
import scala.collection.generic.MapFactory
import com.stovokor.bomber.tiles.MapBuilder
import com.stovokor.bomber.tiles.Tile
import com.stovokor.bomber.tiles.MapLoader
import com.stovokor.bomber.tiles.MapLoader
import com.stovokor.bomber.control.MapControl
import scala.collection.JavaConversions._
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.scene.Spatial.CullHint
import com.jme3.system.AppSettings

abstract class SimpleAppState extends AbstractAppState {

  var physicsState: BulletAppState = null
  var assetManager: AssetManager = null
  var app: SimpleApplication = null
  var rootNode: Node = null
  var space: PhysicsSpace = null
  var car: Node = null
  var inputManager: InputManager = null
  var guiNode: Node = null
  var settings: AppSettings = null

  def cam = app.getCamera

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    physicsState = stateManager.getState(classOf[BulletAppState])
    app = simpleApp.asInstanceOf[SimpleApplication]
    assetManager = app.getAssetManager
    rootNode = app.getRootNode
    space = physicsState.getPhysicsSpace
    inputManager = app.getInputManager
    guiNode = app.getGuiNode
    settings = app.getContext.getSettings
  }

  override def update(tpf: Float) {
  }

}