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
import com.jme3.bullet.control.BetterCharacterControl
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.stovokor.actor.AskPosition
import com.stovokor.actor.PositionResponse
import java.util.concurrent.Callable
import com.jme3.app.SimpleApplication
import com.stovokor.state.ActorSystemState

//object ActorGeometryControl {
//
//  def apply() = {
//  }
//
//}

class SpatialActorControl(systemState: ActorSystemState, actor: ActorRef)
    extends AbstractControl {

  def controlUpdate(tpf: Float) {
    implicit val timeout = Timeout(1.seconds)
    val answer = actor ? AskPosition
    answer.foreach(pos => {
      val p = pos.asInstanceOf[PositionResponse]
      systemState.app.enqueue(new Callable[Unit]() {
        def call = getSpatial.setLocalTranslation(p.pos)
      })
    })
  }

  def controlRender(rm: RenderManager, vp: ViewPort) {}

}