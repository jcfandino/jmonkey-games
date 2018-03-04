package com.stovokor.state

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import scala.annotation.migration
import scala.collection.JavaConversions.asScalaBuffer
import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.Plane.Side
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.Spatial.CullHint
import com.stovokor.domain.CorridorControl
import com.stovokor.domain.DoorControl
import com.stovokor.domain.Entity
import com.stovokor.domain.NodeId
import com.stovokor.domain.RoomControl
import com.stovokor.domain.SceneManaged
import com.stovokor.domain.enemy.EnemyControl
import com.stovokor.gen.level.BSPNode
import com.stovokor.gen.level.LeafBSPNode
import com.stovokor.gen.level.NonLeafBSPNode
import com.stovokor.jme.PortedBetterCharacterControl
import com.stovokor.util.jme.DoorStateChange
import com.stovokor.util.jme.EnemyDied
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.ItemDropped
import com.stovokor.util.jme.JmeExtensions.SpatialExtensions
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.LoudSound
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.light.Light
import com.jme3.light.PointLight
import com.stovokor.util.jme.EventHub
import com.jme3.scene.BatchNode

/**
 * @author xuan
 */
class SceneManager(rootNode: Node, bsp: BSPNode) extends AbstractAppState with LogicEventListener {

  var bspToRoom: Map[BSPNode, SceneManaged] = Map.empty
  var playerLoc: Vector3f = Vector3f.ZERO
  var corridors: List[CorridorControl] = Nil
  var roomToEnemies: Map[SceneManaged, List[EnemyControl]] = Map.empty.withDefault(_ => Nil)
  var roomToPhysics: Map[SceneManaged, List[Spatial]] = Map.empty.withDefault(_ => Nil)
  var space: PhysicsSpace = null

  val runLaterExecutor = Executors.newScheduledThreadPool(1)

  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    super.initialize(appStateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
    stateManager = appStateManager

    EventHub.subscribeByType(this, classOf[DoorStateChange])
    EventHub.subscribeByType(this, classOf[EnemyDied])
    EventHub.subscribeByType(this, classOf[ItemDropped])
    EventHub.subscribeByType(this, classOf[LoudSound])
  }

  def setLevel(levelNode: Node) {
    rootNode.attachChild(levelNode)
    levelNode.breadthFirst(s => {
      val rc = s.getControl(classOf[RoomControl])
      val cc = s.getControl(classOf[CorridorControl])
      if (rc != null) {
        bspToRoom = bspToRoom.updated(rc.bspNode, rc)
      }
      if (cc != null) {
        corridors = cc :: corridors
      }
    })
    levelNode.addControl(new RigidBodyControl(0))
    space.add(levelNode)
    optimizeScene
  }

  def setPhysics(space: PhysicsSpace) = {
    this.space = space
  }

  def optimizeScene {
    bspToRoom.values.map(_.getSpatial.asNode).foreach(batchNode)
    corridors.map(_.getSpatial.asNode).foreach(batchNode)
  }

  def getBatchChild(node: Node) = {
    node.childOption("batch").orElse({
      //      val bn = new BatchNode("batch")
      val bn = new Node("batch")
      node.attachChild(bn)
      Some(bn)
    }).get.asInstanceOf[Node]
  }

  def attachAndBatch(spatial: Spatial) = {
    val node = attach(spatial)
    val batch = getBatchChild(node)
    batch.attachChild(spatial)
    batchNode(node)
    //    batch.batch
  }

  val optimizeNodes = !false
  def batchNode(node: Node) {
    if (optimizeNodes) {
      val batch = getBatchChild(node)
      GeometryBatchFactory.optimize(batch)
      //      if (node.isInstanceOf[BatchNode]) node.asInstanceOf[BatchNode].batch()
      //      else GeometryBatchFactory.optimize(node)
    }
  }

  def attach(light: Light) = {
    if (light.isInstanceOf[PointLight]) {
      val node = find(bsp, light.asInstanceOf[PointLight].getPosition)
      node.foreach(_.getSpatial.asNode.addLight(light))
    } else {
      rootNode.addLight(light)
    }
  }

  def attach(spatial: Spatial) = {
    val node =
      if (spatial.getControl(classOf[PortedBetterCharacterControl]) != null) {
        if (spatial.getControl(classOf[EnemyControl]) != null) {
          val ectrl = spatial.getControl(classOf[EnemyControl])
          val room = find(bsp, spatial.getLocalTranslation).get
          roomToEnemies = roomToEnemies.updated(room, ectrl :: roomToEnemies(room))
          spatial.setCullHint(CullHint.Always)
          ectrl.getPhysicsControl.setEnabled(false)
          ectrl.getPhysicsControl.setPhysicsSpace(null)
        }
        find(bsp, spatial.getLocalTranslation).get.getSpatial.asNode
      } else if (spatial.getControl(classOf[DoorControl]) != null) {
        val corId = spatial.getControl(classOf[DoorControl]).doorId
        val corridor = findCorridor(corId)
        corridor.foreach(c => c.closeDoor)
        corridor.get.getSpatial.asNode
      } else {
        val room = find(bsp, spatial.getLocalTranslation)
        if (room.isDefined) room.get.getSpatial.asNode
        else rootNode
      }
    node.attachChild(spatial)
    node
  }

  def attach(entity: Entity[_]) {
    val spatial = entity.spatial
    val node = attach(spatial)
    // Physics
    entity.physicsObject.foreach(o => {
      if (node.getControl(classOf[RoomControl]) == null) {
        space.add(o)
      } else {
        val room = node.getControl(classOf[RoomControl])
        roomToPhysics = roomToPhysics.updated(room, o :: roomToPhysics(room))
      }
    })
  }

  def updatePlayerLocation(loc: Vector3f) {
    playerLoc = loc
  }
  var app: SimpleApplication = null
  var stateManager: AppStateManager = null

  def attachDeadEnemy(spatial: Spatial) {
    val command = new Callable[Unit] {
      def call = {
        app.enqueue(new Callable[Unit] {
          def call = {
            println("re attaching dead enemy")
            spatial.setCullHint(CullHint.Inherit)
            val node = attachAndBatch(spatial)
            //if (node != rootNode) batchNode(node)
          }
        })
      }
    }
    runLaterExecutor.schedule(command, 500, TimeUnit.MILLISECONDS)
  }

  override def cleanup = {
    EventHub.removeFromAll(this)
    runLaterExecutor.shutdownNow()
  }

  def onEvent(event: GameEvent) = event match {
    case DoorStateChange(id, false) => {
      println(s"Door opened $id")
      findCorridor(id).foreach(c => c.openDoor)
      recalculateOcclusion
    }
    case DoorStateChange(id, true) => {
      println(s"Door closed $id")
      findCorridor(id).foreach(c => c.closeDoor)
      recalculateOcclusion
    }
    case EnemyDied(spatial) => {
      attachDeadEnemy(spatial)
    }
    case ItemDropped(spatial) => {
      attach(spatial)
      //      rootNode.attachChild(spatial)
    }
    case LoudSound(position) => {
      alertEnemiesOfLoudSound(position)
    }
    case _ =>
  }

  // TODO This isn't scene management, should be else where
  def alertEnemiesOfLoudSound(position: Vector3f) {
    val enemies = rootNode.getChildren
      .filter(_.hasControl(classOf[EnemyControl]))
      .map(s => (s, s.getLocalTranslation))
    val command = new Callable[Unit] {
      def call = {
        // Find enemies that heard the sound
        val controls = enemies
          .map { case (s, p) => (s, find(bsp, p)) }
          .filter { case (s, n) => (n.isDefined && n.exists(_.isEnable)) }
          .map(_._1.getControl(classOf[EnemyControl]))
        // Notify them in the main thread
        app.enqueue(new Callable[Unit] {
          def call = {
            controls.foreach(_.reactToNoise(position))
          }
        })
      }
    }
    runLaterExecutor.schedule(command, 20, TimeUnit.MILLISECONDS)

  }

  def findCorridor(id: String) = corridors.find(c => c.corridorId == id)

  def recalculateOcclusion {
    val room = find(bsp, playerLoc)
    if (room.isDefined) {
      occludeAll
      enablePlace(room.get)
    } else {
      findOutsideRoom()
    }
  }

  def find(n: BSPNode, loc: Vector3f): Option[SceneManaged] = n match {
    case NonLeafBSPNode(_, left, right, _, _, cut) => {
      cut.whichSide(loc) match {
        case Side.Positive => find(right, loc)
        case _ => find(left, loc)
      }
    }
    case LeafBSPNode(_, _, _) => bspToRoom.get(n)
  }

  def findOutsideRoom() = {
    println("Cannot find player in any room, searching in corridors.")
    corridors.find(cor => {
      List(cor.room1, cor.room2)
        .flatMap(_.internalLimits)
        .filterNot({ case (_, c) => c == cor })
        .map({ case (p, _) => p })
        .forall(p => p.whichSide(playerLoc) == Side.Negative)
    }).foreach(c => {
      occludeAll
      c.setEnable(true)
      if (c.doorsClosed != 2) {
        enablePlace(c.room1)
        enablePlace(c.room2)
      }
    })
  }

  def enablePlace(p: SceneManaged) {
    println("Enabling scene managed " + p)
    p.activate(playerLoc)
    p.propagateAction({ r =>
      r.setEnable(true)
      activatePhysics(r)
      awakeEnemiesInRoom(r)
    })
  }

  def occludeAll {
    bspToRoom.values.foreach(r => r.setEnable(false))
    corridors.foreach(c => c.setEnable(false))
  }

  def activatePhysics(room: SceneManaged) {
    roomToPhysics(room).foreach(s => {
      println(s"activating physics in room $room")
      space.add(s)
    })
    roomToPhysics = roomToPhysics.updated(room, Nil)
  }

  def awakeEnemiesInRoom(room: SceneManaged) {
    roomToEnemies(room).foreach(e => {
      println(s"awakening enemies in $room")
      val spat = e.getSpatial
      spat.setLocalTranslation(spat.getWorldTranslation)
      spat.setLocalRotation(spat.getWorldRotation())
      rootNode.attachChild(spat) // Promote to rootNode to avoid occlusion
      e.setAwake(true) // awake control
      e.getSpatial.setCullHint(CullHint.Inherit)
      e.getPhysicsControl.setEnabled(true)
    })
    roomToEnemies = roomToEnemies.updated(room, Nil)
  }

}

