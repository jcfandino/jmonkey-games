package com.stovokor.state

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.jme3.ai.navmesh.NavMeshPathfinder
import java.util.concurrent.FutureTask
import java.util.concurrent.Callable
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.jme3.app.state.AbstractAppState

/**
 * @author xuan
 */
object PathFindScheduler extends AbstractAppState {
  // Nav mesh is not thread safe, more that one locks the app
  val executor: ExecutorService = Executors.newSingleThreadExecutor()

  val runningNow: HashMap[NavMeshPathfinder, FutureTask[Boolean]] = new HashMap[NavMeshPathfinder, FutureTask[Boolean]]

  val handlerQueue: ListBuffer[Callable[Unit]] = ListBuffer()

  def schedule(task: () => Boolean)(handler: () => Unit)(pf: NavMeshPathfinder) = {
    val future =
      if (runningNow.contains(pf)) {
        val old = runningNow(pf)
        if (old.isDone() || old.isCancelled()) {
          runningNow.remove(pf)
        }
        old
      } else {
        val newF = futureTask(task)(handler)
        executor.execute(newF)
        runningNow.put(pf, newF)
        newF
      }
    future
  }

  def futureTask(task: () => Boolean)(handler: () => Unit) = {
    new FutureTask[Boolean](new Callable[Boolean]() {
      def call(): Boolean = {
        val result = task()
        if (result) {
          enqueue(handler)
        }
        result
      }
    })
  }

  def callableHandler(handler: () => Unit) = {
    new Callable[Unit]() {
      def call = { handler() }
    }
  }

  override def update(tpf: Float) {
    executeHandlers
  }
  override def cleanup = {
    super.cleanup
    clearHandlers
    shutdown
  }
  def enqueue(task: () => Unit) {
    handlerQueue += callableHandler(task)
  }
  def executeHandlers {
    handlerQueue.foreach(_.call)
    handlerQueue.clear
  }
  def clearHandlers {
    handlerQueue.clear
  }

  def shutdown {
    executor.shutdownNow()
  }
}

//trait UpdateTask {
//  def update(tpf:Float)
//}
