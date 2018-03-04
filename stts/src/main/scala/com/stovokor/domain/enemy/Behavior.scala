package com.stovokor.domain.enemy

import scala.collection.JavaConversions._
import com.jme3.math.Vector3f
import com.jme3.ai.navmesh.NavMeshPathfinder
import com.stovokor.STTS
import com.jme3.math.Vector2f
import com.stovokor.util.math.TimedUpdate
import com.jme3.math.FastMath
import com.stovokor.Settings.Debug
import com.stovokor.state.PathFindScheduler
import java.util.concurrent.atomic.AtomicBoolean

abstract class Behavior() {
  def pFinder(implicit self: EnemyControl) = self.pathFinder
  def pcon(implicit self: EnemyControl) = self.getPhysicsControl
  def goto(implicit self: EnemyControl) = self.getLastSeenPlayer
  def position(implicit self: EnemyControl) = self.getPosition

  def update(implicit self: EnemyControl)

}
class StandStill extends Behavior() {
  def update(implicit self: EnemyControl) {
    // Do nothing
    pcon.setWalkDirection(Vector3f.ZERO)
  }
}
class DirectApproach() extends Behavior() {

  // Dont put any state here!!!

  val debugEnabled = Debug.pathFinding

  val minDist = 2f
  def lastPosCalculation(implicit self: EnemyControl) = self.lastPosCalculation

  def canAdvanceWaypoint(implicit self: EnemyControl) = {
    val v1 = pFinder.getWaypointPosition
    val v2 = self.getPosition
    (new Vector2f(v1.x, v1.z) distance new Vector2f(v2.x, v2.z)) < minDist &&
      pFinder.getNextWaypoint != pFinder.getPath.getLast
  }

  def calculateNewPath(goto: Vector3f)(implicit self: EnemyControl) = {
    def compute() {
      try {
        lastPosCalculation(self).set(Vector3f.ZERO)
        //        println(s"Computing new path for $self")
        pFinder.setEntityRadius(3f)
        pFinder.setPosition(position)
        //        pFinder.computePath(goal)
        val future = PathFindScheduler.schedule(task)(handler)(pFinder)
      } catch {
        case t: Throwable =>
      }
    }
    def task() = {
      pFinder.computePath(goto)
    }
    def handler() = {
      if (debugEnabled) STTS.navigationDebug.clear(self)
      if (debugEnabled) STTS.navigationDebug.drawPath(self, pFinder.getPath)
      try {
        while (canAdvanceWaypoint) {
          println("Skipping waypoint")
          pFinder.goToNextWaypoint
        }
        lastPosCalculation(self).set(goto)
        println(s"Path From: ${position} To: ${goto}")
      } catch {
        // May happen if enemy dies while computing path
        case npe: NullPointerException => println("Cannot set path. null pointer thrown. Skipping")
        case e: Exception => e.printStackTrace()
      }
    }
    compute()
  }
  def pathAvailable(implicit self: EnemyControl) = !lastPosCalculation(self).equals(Vector3f.ZERO)

  def shouldCalculatePath(goto: Vector3f)(implicit self: EnemyControl) = {
    def noCurrentWaypoint = pFinder.getNextWaypoint == null
    def playerMoved = !goto.equals(lastPosCalculation)
    def playerOnSight = self.canSeePlayer
    //    if (enoughTimePassed) println(s"Should calculate? Moved: $playerMoved - OnSight: $playerOnSight")
    //canCheck && 
    (playerMoved || noCurrentWaypoint) && playerOnSight
  }

  def update(implicit self: EnemyControl) {
    //implicit def self: EnemyControl = ctrl
    val shouldCalculate = shouldCalculatePath(goto)
    if (shouldCalculate) {
      calculateNewPath(goto)
    }
    if (pathAvailable && pFinder.getNextWaypoint != null) { //A path was traced
      while (canAdvanceWaypoint && pFinder.getNextWaypoint != pFinder.getPath.getLast) {
        pFinder.goToNextWaypoint
        println(s"advanced to waypoint: ${pFinder.getNextWaypoint}")
      }
      val wpos = pFinder.getWaypointPosition
      val direction = wpos.subtract(position).normalizeLocal
      pcon.setWalkDirection(direction.setY(0).normalize.mult(self.speed))
      self.turn(direction)

      if (canAdvanceWaypoint && pFinder.getNextWaypoint == pFinder.getPath.getLast) {
        println("Stop, no where to go.")
        pFinder.clearPath()
      }
    } else {
      println(s"No path to follow.?? $self")
      //      println(s"should calculate? ${shouldCalculatePath(goto)}")
      println(s"path available? ${pathAvailable}")
      //      println(s"has waypoint? ${pFinder.getNextWaypoint}")
      println(s"has player moved? ${!goto.equals(lastPosCalculation)}")
      //println(s"player on sight? ${self.canSeePlayer}")
      //          pcon.setWalkDirection(Vector3f.ZERO)
      //          setState(Idle)
    }
  }

}
class StrafeAround extends Behavior() {
  def update(implicit self: EnemyControl) {
    val ortho = Vector3f.UNIT_Y cross self.getDirection
    pcon.setWalkDirection(ortho normalize () mult self.speed divide 2f)

    val direction = self.getLastSeenPlayer.subtract(position).normalizeLocal
    self.turn(direction)
  }
}