package com.stovokor.domain.enemy

class EnemyTactic(
  val behaviors: TacticBehaviors,
  val parameters: TacticParameters) {

  def behaviorFor(state: States.State) = state match {
    case States.Idle => behaviors.idleBehavior
    case States.Chase => behaviors.chaseBehavior
    case States.Dead => new StandStill
    case States.Attack => behaviors.attackBehavior
    case _ => throw new RuntimeException(s"Unknown State $state")
  }

}

class TacticBehaviors(val idleBehavior: Behavior,
  val chaseBehavior: Behavior,
  val attackBehavior: Behavior)

class TacticParameters(val distanceToReach: Float)
//  val distanceTolerance = 20f // +/-