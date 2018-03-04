package com.stovokor.domain

import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.stovokor.gen.level.LeafBSPNode
import com.jme3.scene.Spatial.CullHint
import com.jme3.scene.Spatial
import com.stovokor.gen.level.Wall
import com.jme3.math.Plane
import com.jme3.math.Vector3f
import com.jme3.math.Plane.Side

/**
 * @author xuan
 */
class RoomControl(val bspNode: LeafBSPNode) extends AbstractControl with SceneManaged {

  var related: List[CorridorControl] = Nil
  var internalLimits: Map[Plane, CorridorControl] = Map()

  def relateTo(other: CorridorControl, wall: Wall) {
    if (!related.contains(other)) {
      related = other :: related
      internalLimits = internalLimits.updated(wall.plane, other)
    }
  }

  def activate(point: Vector3f) {
    setEnable(true)
    // Detect if we are inside the partition but outside the room.
    // Means we're inside a corridor
    for ((plane, corridor) <- internalLimits) {
      plane.whichSide(point) match {
        case Side.Positive => {
          println("Outside room, activating corridor")
          corridor.related.foreach(_.propagateAction(_.setEnable(true)))
        }
        case _ =>
      }
    }
  }

  override val propagate = true

  def controlUpdate(tpf: Float): Unit = {
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

  override def clone() = {
    val copy = new RoomControl(bspNode)
    copy.related = related
    copy
  }

}

class CorridorControl(val corridorId: String, val room1: RoomControl, val room2: RoomControl) extends AbstractControl with SceneManaged {

  var doorsClosed = 0 // if no doors consider both open

  def closeDoor = doorsClosed += 1
  def openDoor = doorsClosed -= 1
  override def propagate = doorsClosed == 0

  val related = List(room1, room2)

  def controlUpdate(tpf: Float) {}
  def controlRender(rm: RenderManager, vp: ViewPort) {}

  override def clone() = {
    val copy = new CorridorControl(corridorId, room1, room2)
    copy.doorsClosed = doorsClosed
    copy
  }

  def activate(point: Vector3f) { setEnable(true) }
}

trait SceneManaged {

  var isEnable = true

  def setEnable(enable: Boolean) {
    val hint = if (enable) CullHint.Inherit else CullHint.Always
    getSpatial.setCullHint(hint)
    isEnable = enable
  }

  def propagateAction(f: SceneManaged => Unit, alreadyCalled: List[SceneManaged] = Nil) {
    f(this)
    if (propagate) related
      .filterNot(alreadyCalled.contains)
      .foreach(_.propagateAction(f, this :: alreadyCalled))
  }

  def getSpatial: Spatial
  def related: Seq[SceneManaged]
  def propagate: Boolean
  def activate(playerLoc: Vector3f)
}

