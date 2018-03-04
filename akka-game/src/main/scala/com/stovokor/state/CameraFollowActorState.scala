package com.stovokor.state

import com.jme3.app.state.AppStateManager
import com.jme3.app.SimpleApplication
import com.jme3.app.Application
import com.stovokor.actor.AskPosition
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.pattern._
import akka.util.Timeout
import com.jme3.math.Vector3f
import com.stovokor.actor.PositionResponse
import java.util.concurrent.Callable

class CameraFollowActorState extends BaseState {

  override def initialize(stateManager: AppStateManager, simpleApp: Application) {
    super.initialize(stateManager, simpleApp)
  }

  def system = stateManager.getState(classOf[ActorSystemState]).system

  override def update(tpf: Float) {
    implicit val timeout = Timeout(1.seconds)
    val answer = system.actorSelection("/user/player") ? AskPosition
    answer.foreach(pos => {
      val p = pos.asInstanceOf[PositionResponse]
      app.enqueue(new Callable[Unit]() {
        def call = {
          val ppos = new Vector3f(p.pos.getX, p.pos.getY, 0f)
          cam.setLocation(ppos.add(1f, -2f, 20f))
          cam.lookAt(ppos, Vector3f.UNIT_Y)
          cam.update
        }
      })
    })
  }
}
