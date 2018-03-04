package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.scene.Node
import com.jme3.scene.SceneGraphVisitor
import com.jme3.scene.Spatial
import com.stovokor.domain.NodeId
import com.stovokor.domain.enemy.EnemyControl
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.LevelChange
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.math.TimedUpdate

class ProximityDetectionState() extends AbstractAppState with LogicEventListener with TimedUpdate {

  def rootNode = app.getRootNode
  var app: SimpleApplication = null

  val distance = 200L
  val checkSpan = 100L
  var player: Option[Node] = None
  var enemies: List[Node] = List()

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    app = simpleApp.asInstanceOf[SimpleApplication]
    EventHub.subscribeByType(this, classOf[LevelChange])
  }

  override def cleanup = {
    super.cleanup
  }

  def onEvent(event: GameEvent) = {
    if (isEnabled) {
      event match {
        case LevelChange(old, next) => {
          player = None
          enemies = List()
        }
        case _ =>
      }
    }
  }

  def initNodes() = {
    rootNode.breadthFirstTraversal(new SceneGraphVisitor() {
      def visit(s: Spatial) {
        if (s.isInstanceOf[Node]) {
          if (s.getName == NodeId.player) {
            player = Some(s.asInstanceOf[Node])
          } else if (s.getName == NodeId.enemy) {
            enemies = s.asInstanceOf[Node] :: enemies
          }
        }
      }
    })
  }

  def checkProximity {
    val playerPos = player.get.getWorldTranslation
    enemies.map(e => e.getControl(classOf[EnemyControl]))
      .filter(c => c != null && c.isAwake && !c.isDead).foreach({ ctrl =>
        def d = ctrl.getSpatial.getWorldTranslation.distance(playerPos)
        if (d < distance) ctrl.seePlayer(player.get)
      })
  }

  override def update(tpf: Float) = {
    if (!player.isDefined) initNodes
    if (canCheck) checkProximity
  }

}
