package com.stovokor.state

import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.scene.shape.Box
import com.stovokor.control.CarControl
import com.jme3.ui.Picture
import com.jme3.system.AppSettings
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import java.time.Duration
import com.jme3.font.BitmapText
import com.jme3.font.BitmapFont

class MissionState(dispatcher: MissionDispatcher) extends AbstractAppState {

  var car: Option[CarControl] = None
  var rootNode: Node = null
  var assetManager: AssetManager = null
  var guiNode: Node = null

  var zoneBox: Geometry = null
  var dirArrow: Picture = null
  var messageBox: BitmapText = null

  var screenSize: Vector2f = null

  val arrowWidth = 40
  val arrowHeight = 40

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    val app = simpleApp.asInstanceOf[SimpleApplication]
    rootNode = simpleApp.asInstanceOf[SimpleApplication].getRootNode
    assetManager = simpleApp.asInstanceOf[SimpleApplication].getAssetManager
    guiNode = app.getGuiNode
    val settings = app.getContext.getSettings
    screenSize = new Vector2f(settings.getWidth, settings.getHeight)

    // box
    zoneBox = createZoneCube(assetManager)
    rootNode.attachChild(zoneBox)
    // arrow
    dirArrow = createDirectionArrow()
    guiNode.attachChild(dirArrow)
    // message
    messageBox = createMessageBox(assetManager)
    guiNode.attachChild(messageBox)

  }

  override def update(tpf: Float) {
    getCar.foreach(cc => {
      if (cc.health <= 0f) {
        dispatcher.cancel()
      } else {
        dispatcher.current
          .orElse(dispatcher.startNext(cc))
          .foreach(status => {
            val result = status.update(tpf)
            if (result || cc.health <= 0f) {
              if (status.failed || status.succeded) {
                dispatcher.finished(status, cc)
              }
              hideBox
              hideArrow
            } else {
              updateArrow(tpf)
              updateBox(tpf)
              updateMessage(status)
            }
          })
      }
    })
  }

  def hideBox {
    zoneBox.setCullHint(CullHint.Always)
  }

  def hideArrow {
    dirArrow.setCullHint(CullHint.Always)
  }

  def createDirectionArrow() = {
    val pic = new Picture("arrow")
    pic.setImage(assetManager, "Textures/arrow.png", true)
    pic.setWidth(arrowWidth)
    pic.setHeight(arrowHeight)
    pic.setPosition(screenSize.x / 2, screenSize.y / 2);
    pic.setCullHint(CullHint.Always)
    pic
  }

  def forStatus(f: (MissionStatus => Unit)): Unit = dispatcher.current.foreach(f)

  def updateArrow(tpf: Float) {
    forStatus(status => {
      status.current.flatMap(_.box).foreach(b => (b, car) match {
        case ((o, s), Some(cc)) => {
          var cpos = cc.getSpatial.getLocalTranslation

          val ang = FastMath.atan2(o.y - cpos.y, o.x - cpos.x)
          dirArrow.setLocalRotation(new Quaternion().fromAngleAxis(ang, Vector3f.UNIT_Z))

          var r = 100f
          val x = (arrowWidth / 2) * FastMath.sin(ang) +
            screenSize.x / 2 +
            r * FastMath.cos(ang)

          val y = (arrowHeight / 2) * FastMath.cos(ang) +
            screenSize.y / 2 +
            r * FastMath.sin(ang)

          dirArrow.setLocalTranslation(x, y, 0)

          dirArrow.setCullHint(CullHint.Never)
        }
        case _ =>
      })
    })
  }

  def createMessageBox(assetManager: AssetManager) = {
    val font = assetManager.loadFont("Interface/Fonts/Default.fnt")
    val text = new BitmapText(font, false)
    text.setSize(font.getCharSet().getRenderedSize())
//    text.setColor(new ColorRGBA(0.2f, 0.8f, 0.2f, 1f))
    text.setColor(ColorRGBA.Green)
    text.setText("You can write any string here")
    text.setLocalTranslation(screenSize.x * .05f, screenSize.y - 2f * text.getLineHeight(), 0)
    text
  }

  def updateMessage(st: MissionStatus) {
    forStatus(s => s.current.map(_.message(st)).foreach(m => {
      messageBox.setText(m)
    }))
  }

  def updateBox(tpf: Float) {
    forStatus(s => s.current.flatMap(_.box).foreach(b => b match {
      case (o, s) => {
        zoneBox.setLocalTranslation(o)
        zoneBox.setLocalScale(s)
        zoneBox.setCullHint(CullHint.Inherit)
      }
    }))
  }

  def getCar = {
    if (car.isEmpty) {
      val node = rootNode.getChild("car")
      if (node != null && node.getControl(classOf[CarControl]) != null) {
        car = Some(node.getControl(classOf[CarControl]))
      }
    }
    car
  }

  def createZoneCube(assetManager: AssetManager) = {
    val box = new Box(.5f, .5f, .5f)
    val geom = new Geometry("zone", box)
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", new ColorRGBA(0f, 1f, 0f, 0.3f))
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
    geom.setMaterial(mat)
    geom.setCullHint(CullHint.Always)
    geom.setQueueBucket(Bucket.Transparent);
    geom
  }

}

class MissionDispatcher(val startMissions: List[MissionDef]) {

  var missions = new IntermezzoMission("Are you looking for a Job? See if Lou has any.") :: startMissions

  var current: Option[MissionStatus] = None

  def startNext(car: CarControl): Option[MissionStatus] = {
    if (current.isEmpty) {
      current = missions.headOption.map(mission => {
        missions = missions.tail
        val st = new MissionStatus(mission, car)
        st.start
        st
      })
    }
    current
  }

  def cancel() {
    current.foreach(st => {
      if (!st.mdef.isInstanceOf[IntermezzoMission]) {
        current = None
        missions = IntermezzoMission("You wasted you car. Try again.") :: st.mdef :: missions
      }
    })
  }

  def finished(st: MissionStatus, car: CarControl) {
    current = None
    if (!st.mdef.isInstanceOf[IntermezzoMission]) {
      missions =
        if (st.succeded) {
          if (missions.isEmpty) {
            List(GameCompleteMission)
          } else {
            new IntermezzoMission("Good Job! There're more Jobs for you.") :: missions
          }
        } else if (st.succeded && missions.isEmpty) {
          IntermezzoMission("Good Job! There're more Jobs for you.") :: missions
        } else {
          IntermezzoMission("You Failed! You can retry the job.") :: st.mdef :: missions
        }
    }
  }

}

object MissionDef {
  def apply(objectives: List[Objective]) = new MissionDef(objectives)
}

class MissionDef(val objectives: List[Objective]) {
}

object MissionStatus {
  def apply(mdef: MissionDef, car: CarControl) = new MissionStatus(mdef, car)
}

class MissionStatus(val mdef: MissionDef, val car: CarControl) {
  var time = 0F

  var current: Option[Objective] = None
  var left: List[Objective] = Nil
  var failed = false
  var succeded = false

  def start {
    time = 0L
    left = mdef.objectives
    advance
  }

  //  def car = carOpt.get

  def update(tpf: Float): Boolean = {
    time += tpf
    var message = current.map(_.message(this)).getOrElse("")
    var completed = current.forall(_.isComplete(this))
    var expired = current.forall(_.isExpired(this))
    if (completed) succeded = advance
    else if (expired) end
    completed || expired
  }

  def end {
    current = None
    left = Nil
    failed = true
  }

  def advance: Boolean = {
    time = 0L
    if (left.isEmpty) {
      current = None
      true
    } else {
      current = Some(left.head)
      left = left.tail
      false
    }

  }
}

trait Objective {
  def isComplete(st: MissionStatus): Boolean
  def isExpired(st: MissionStatus): Boolean = false
  def message(st: MissionStatus): String = ""

  def box: Option[(Vector3f, Vector3f)] = None

  // util methods
  def addTimeToMessage(message: String, time: Float) = s"${message} (${formatTime(time)})"

  def formatTime(time: Float) = {
    val left = Duration.ofSeconds(time.toLong)
    val mins = left.getSeconds / 60
    val secs = left.getSeconds % 60
    s"$mins:$secs"
  }
  def carInZone(car: CarControl, origin: Vector2f, margin: Vector2f) = {
    val pos = car.getSpatial.getLocalTranslation
    pos.x > origin.x - margin.x / 2f &&
      pos.x < origin.x + margin.x / 2f &&
      pos.y > origin.y - margin.y / 2f &&
      pos.y < origin.y + margin.y / 2f
  }

}

object TimedObjective {
  def apply(timeOut: Float, obj: Objective) = new TimedObjective(timeOut, obj)
}

class TimedObjective(val timeOut: Float, obj: Objective) extends Objective {
  def isComplete(st: MissionStatus) = obj.isComplete(st)

  override def message(st: MissionStatus) = addTimeToMessage(obj.message(st), timeOut - st.time)
  override def isExpired(st: MissionStatus) = st.time > timeOut
  override def box = obj.box

}

object CheckpointObjective {
  def apply(origin: Vector2f, margin: Vector2f, text: String) = new CheckpointObjective(origin, margin, text)
}

class CheckpointObjective(val origin: Vector2f, val margin: Vector2f, val text: String)
    extends Objective {

  def isComplete(st: MissionStatus) = {
    carInZone(st.car, origin, margin)
  }

  // center, size
  override val box = Some(
    new Vector3f(origin.x, origin.y, 1f),
    new Vector3f(margin.x, margin.y, 2f))

  override def message(st: MissionStatus) = text
}
object ZoneObjective {
  def apply(origin: Vector2f, margin: Vector2f, text: String) = new ZoneObjective(origin, margin, text)
}

class ZoneObjective(val origin: Vector2f, val margin: Vector2f, val text: String)
    extends Objective {

  def isComplete(st: MissionStatus) = {
    st.car.getSpeedSquared < 0.2f &&
      carInZone(st.car, origin, margin)
  }

  // center, size
  override val box = Some(
    new Vector3f(origin.x, origin.y, 1f),
    new Vector3f(margin.x, margin.y, 2f))

  override def message(st: MissionStatus) = text
}

object WaitInZoneObjective {
  def apply(origin: Vector2f, margin: Vector2f, timeOut: Float, text: String) =
    new WaitInZoneObjective(origin, margin, timeOut, text)
}

class WaitInZoneObjective(val origin: Vector2f, val margin: Vector2f, val timeOut: Float, val text: String) extends Objective {

  def isComplete(st: MissionStatus) = {
    carInZone(st.car, origin, margin) && st.time > timeOut
  }

  override def isExpired(st: MissionStatus) = !carInZone(st.car, origin, margin)

  // center, size
  override val box = Some(
    new Vector3f(origin.x, origin.y, 1f),
    new Vector3f(margin.x, margin.y, 2f))

  override def message(st: MissionStatus) = addTimeToMessage(text, timeOut - st.time)
}

object NullObjective {
  def apply(message: String) = new NullObjective(message)
}

class NullObjective(val message: String) extends Objective {
  def isComplete(st: MissionStatus) = false
  override def message(st: MissionStatus) = message
}

object IntermezzoMission {
  def apply(message: String) = new IntermezzoMission(message)
}

class IntermezzoMission(message: String) extends MissionDef(List(
  ZoneObjective(new Vector2f(95f, 25f), new Vector2f(5f, 5f), message))) {}

object GameCompleteMission extends MissionDef(List(
  NullObjective("Congratulations! You completed all missions."))) {}

