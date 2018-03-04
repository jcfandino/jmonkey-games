package com.stovokor.domain

import com.jme3.scene.Node
import com.jme3.scene.control.AbstractControl
import com.jme3.renderer.ViewPort
import com.jme3.renderer.RenderManager
import com.jme3.bullet.control.RigidBodyControl
import com.stovokor.util.jme.HasSpatialState
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.math.Vector2f
import java.nio.FloatBuffer
import java.util.UUID
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.DoorStateChange

class Door(model: Node, control: DoorControl) extends Entity(model, control, Some(model)) {
}

class DoorControl(val height: Float, val speed: Float, val edgeId: String, val key: Option[String]) extends AbstractControl with IsInteractive with HasSpatialState {

  var initialPosVar: Option[Float] = None
  def initialPos: Float = initialPosVar.getOrElse(0f)
  def toppos = initialPos + height - 0.05f
  def curpos = spatial.getLocalTranslation
  val lighmapUpdater = LightmapUVUpdater

  val doorId = edgeId

  def controlUpdate(tpf: Float) = {
    if (initialPosVar.isEmpty) initialPosVar = Some(curpos.y)
    if (pushed && curpos.y < toppos) {
      val timePassed = (System.currentTimeMillis - getInteractionTime).toFloat
      val vpos = initialPos + (speed * timePassed / 1000f)
      move(curpos.x, Math.min(vpos, toppos), curpos.z)

    }
    if (pushed && curpos.y > initialPos && readyToGoDown) {
      val timePassed = (System.currentTimeMillis - (getInteractionTime +
        timeToTop + timeOnTop)).toFloat
      val vpos = initialPos + height - (speed * timePassed / 1000f)
      move(curpos.x, Math.max(vpos, initialPos), curpos.z)
    }

    //State
    if (pushed && wentUpAndDown) {
      pushState(false)
      move(curpos.x, initialPos, curpos.z)
      EventHub.trigger(DoorStateChange(doorId, true))
    }
  }

  def move(pos: Vector3f): Unit = {
    spatial.setLocalTranslation(pos)
    spatial.getControl(classOf[RigidBodyControl]).setPhysicsLocation(pos)
    lighmapUpdater.updateLightmapUV(pos)
  }
  def move(x: Float, y: Float, z: Float): Unit = move(new Vector3f(x, y, x))

  var orig: Array[Vector2f] = null

  def wentUpAndDown =
    System.currentTimeMillis > getInteractionTime + (2L * timeToTop) + timeOnTop + 1000L // TODO this extra second is a quick fix
  def readyToGoDown =
    System.currentTimeMillis > getInteractionTime + timeToTop + timeOnTop

  def controlRender(rm: RenderManager, vp: ViewPort) = {}

  // IsInteractive
  def setInteractionTime(t: Long) = set("doorPushedTime", t)
  def getInteractionTime = getOr("doorPushedTime", -1L)
  val minTimeBetweenInteractions = timeToTop + timeOnTop
  val timeToTop = 1000L * (height / speed).toLong //3000L
  val timeOnTop = 3000L

  def receiveInteraction {
    if (!pushed) {
      pushState(true)
      EventHub.trigger(DoorStateChange(doorId, false))
    }
  }

  def pushed = getOr("doorPushed", false)
  def pushedTime = getInteractionTime
  def pushState(st: Boolean) = set("doorPushed", st)

  override def clone() = { new DoorControl(height, speed, edgeId, key) }

  /**
   * Factor =
   * - Vertices 0 and 1 = 0.5 + yp/2
   * - Vertices 2 and 3 =   1 + yp/2
   * - Vertices 0 and 1 = 0.5 - yp/2
   * - Vertices 0 and 1 =     - yp/2
   *
   * Where
   *   yp = 0 at bottom, 1 at top
   *
   * New Y Coordinate =
   *   bottomCoordinate + factor * (topCoordinate - bottomCoordinate)
   */
  object LightmapUVUpdater {
    // Mesh
    def m = spatial.asInstanceOf[Node]
      .getChild(0).asInstanceOf[Geometry]
      .getMesh

    // Original UV Coordinates
    lazy val orig = {
      m.getBuffer(Type.TexCoord2)
      val vs = BufferUtils.getVector2Array(m.getBuffer(Type.TexCoord2).getData().asInstanceOf[FloatBuffer])
      vs.clone
    }
    lazy val lmb = orig(6).y // Bottom cordinate
    lazy val lmh = orig(2).y - orig(6).y // Top coordinate
    lazy val (xo, xe) = (orig(0).x, orig(1).x) // X position is fixed

    def ys(pos: Vector3f) = {
      val hyp = (pos.y - initialPos) / height / 2f
      List(
        lmb + (.5f + hyp) * lmh,
        lmb + (1 + hyp) * lmh,
        lmb + (.5f - hyp) * lmh,
        lmb + (-hyp) * lmh)
    }

    def updateLightmapUV(pos: Vector3f) {
      val y = ys(pos)
      m.setBuffer(Type.TexCoord2, 2, BufferUtils.createFloatBuffer(
        xo, y(0),
        xe, y(0),
        xe, y(1),
        xo, y(1),
        xe, y(2),
        xo, y(2),
        xo, y(3),
        xe, y(3)))
    }
  }
}