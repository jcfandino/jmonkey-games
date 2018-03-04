package com.stovokor.factory

import com.stovokor.state.ZoneObjective
import com.stovokor.state.WaitInZoneObjective
import com.stovokor.state.MissionDef
import com.stovokor.state.TimedObjective
import com.jme3.math.Vector2f
import com.stovokor.state.CheckpointObjective

object StoryFactory {

  // recurrent locations
  val lou = v(95f, 25f)
  val micky = v(75f, 85f)
  val ronnie = v(20f, 50f)
  val bank = v(32f, 85f)
  val alley = v(42.5f, 97)
  val guns = v(95f, 5f)

  def create = {
    List(
      // Mission 1: Prove yourself
      MissionDef(List(
        ZoneObjective(v(82.5f, 18f), v(5f, 2f), "Lou: OK Kid, you say you can drive? Show us how fast you are. Go to the starting line."),
        WaitInZoneObjective(v(82.5f, 18f), v(5f, 2f), 10f, "Stay in the green zone. Wait for the signal"),
        TimedObjective(20f, CheckpointObjective(v(78f, 2.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(22.5f, 7f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(18f, 42.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(8f, 62.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(68f, 42.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(82.5f, 18f), v(5f, 5f), "Well done! Now make another round.")),
        TimedObjective(20f, CheckpointObjective(v(78f, 2.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(22.5f, 7f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(18f, 42.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(8f, 62.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(68f, 42.5f), v(5f, 5f), "Go to the next checkpoint")),
        TimedObjective(20f, CheckpointObjective(v(82.5f, 18f), v(5f, 5f), "Go to the next checkpoint")) //  
        )), //
      // Mission 2: Buy guns
      MissionDef(List( //
        ZoneObjective(micky, v(5f, 5f), "Lou: Well done! You've proven yourself. Micky needs to get some guns for our next hit. Go pick him up."),
        TimedObjective(40f, ZoneObjective(guns, v(5f, 5f), "Micky: Nice to meet you kid, Lou says you know how to drive. Lets gear up!")),
        WaitInZoneObjective(guns, v(5f, 5f), 10f, "Micky: Don't go anywhere, I'll be back soon"),
        TimedObjective(40f, ZoneObjective(micky, v(5f, 5f), "Micky: Done. Don't let anyone open your trunk. Take me home.")) //
        )),
      // Mission 3: Convince Ronnie
      MissionDef(List( //
        TimedObjective(30f, ZoneObjective(ronnie, v(5f, 5f), "Lou: Ronnie, our safe box expert is not convinced about the hit.\nTake me to his family so I can give him some... motivation")),
        WaitInZoneObjective(ronnie, v(5f, 5f), 10f, "Lou: Wait here while I deliver the message"),
        TimedObjective(30f, ZoneObjective(guns, v(5f, 5f), "Lou: He's convinced now. Lets get out of here before he changes his mind.")) //
        )),
      // Mission 4: Big Hit
      MissionDef(List( //
        TimedObjective(30f, ZoneObjective(micky, v(5f, 5f), "Lou: Ok Kido, it's time for the big hit, you better be ready.\nLets meet with the fellas.")),
        WaitInZoneObjective(micky, v(5f, 5f), 10f, "Lou: Wait for Micky"),
        TimedObjective(30f, ZoneObjective(ronnie, v(5f, 5f), "Micky: Hi guys, is Ronnie still on? Lets find him.")),
        WaitInZoneObjective(ronnie, v(5f, 5f), 10f, "Micky: Ronnie, my friend! I knew you would help us, what made you change your mind?"),
        TimedObjective(15f, ZoneObjective(bank, v(5f, 5f), "Lou: Lets go to the bank. Fast!")), //
        TimedObjective(10f, ZoneObjective(alley, v(5f, 5f), "Lou: Park in the alley while we do the deed. You better be here when we finish!")), //
        WaitInZoneObjective(alley, v(5f, 5f), 20f, "Wait here, it would look suspicious if you stayed in front of the bank."), //
        TimedObjective(10f, ZoneObjective(bank, v(5f, 5f), "Back to the bank!")), //
        WaitInZoneObjective(bank, v(5f, 5f), 5f, "Wait for them."), //
        TimedObjective(25f, ZoneObjective(lou, v(5f, 5f), "Lou: Thank God you're back! Get us out of here. Fast! The police is on the way")), //
        WaitInZoneObjective(lou, v(5f, 5f), 5f, "Lou: Great Job Kid. Here's your cut. You're rich now!") //
        )))
  }

  def v(x: Float, y: Float) = new Vector2f(x, y)
}