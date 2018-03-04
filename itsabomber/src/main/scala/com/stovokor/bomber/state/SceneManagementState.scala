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

class SceneManagementState(val batchWidth: Float) extends SimpleAppState {

  var mapNode: Node = null
  var mapRoot: Node = null

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
    mapNode = rootNode.getChild("map-batch").asInstanceOf[Node]
    mapRoot = rootNode.getChild("map-root").asInstanceOf[Node]
  }

  var timeSinceDead = 0f
  var timeToRespawh = 2f

  override def update(tpf: Float) {
    val x = mapRoot.getLocalTranslation.x
    val nodes = mapNode.getChildren

    nodes
      .map(n => (n.getName.replace("map-batch-", "").toInt, n))
      .takeWhile(t => t match { case (i, n) => i - 2 <= -x / batchWidth })
      .foreach(t => t match {
        case (i, n) => {
          if (i + 2 >= -x / batchWidth) {
            if (n.getCullHint == CullHint.Always) {
              println(s"activating node $n")
              n.setCullHint(CullHint.Dynamic)
              space.add(n)
              val body = n.getControl(classOf[RigidBodyControl])
              if (body != null) {
                body.setKinematic(true)
              }
            }
          } else {
            println(s"removing node $n")
            val body = n.getControl(classOf[RigidBodyControl])
            if (body != null) {
              body.setPhysicsSpace(null)
            }
            n.removeControl(classOf[RigidBodyControl])
            n.removeFromParent()
          }
        }
      })
  }

}