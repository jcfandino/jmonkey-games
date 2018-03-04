package com.stovokor.state

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.collision.PhysicsRayTestResult
import com.jme3.math.Vector3f
import scala.collection.JavaConversions._
import com.jme3.bullet.PhysicsSpace
import java.util.concurrent.TimeUnit
import java.util.ArrayList

/**
 * @author xuan
 */
object PhysicsSchedulingAppState extends AbstractAppState {

  val threads = 2
  val executor = Executors.newFixedThreadPool(threads)

  val periodicExecutor = Executors.newScheduledThreadPool(4)

  var app: SimpleApplication = null
  var stateManager: AppStateManager = null
  def space = stateManager.getState(classOf[BulletAppState]).getPhysicsSpace

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    super.initialize(appStateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
    stateManager = appStateManager
  }

  def scheduleTest(from: Vector3f, to: Vector3f, handler: List[PhysicsRayTestResult] => Unit) = {
    val future = executor.submit(new Callable[List[PhysicsRayTestResult]]() {
      def call = {
        println("Running rayTest")
        val results = space.rayTest(from, to, new ArrayList()).toList
        app.enqueue(new Callable[Unit] { def call = handler(results) })
        results
      }
    })
    future
  }

  def schedulePeriodically[R](task: PhysicsSpace => R, handler: Option[R => Unit], time: Long) {
    val command = new Runnable {
      def run() {
        val result = task(space)
        if(handler.isDefined) {
          app.enqueue(new Callable[Unit] {
            def call = {
              handler.get(result)
            }
          })
        }
      }
    }
    periodicExecutor.scheduleAtFixedRate(command, time, time, TimeUnit.MILLISECONDS)
  }

  override def update(tpf: Float) {
  }
  override def cleanup = {
    executor.shutdownNow()
    periodicExecutor.shutdownNow()
  }
}