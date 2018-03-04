package com.stovokor.gen

import com.jme3.asset.AssetManager
import com.jme3.math.Vector3f
import com.stovokor.ai.Navigation
import com.stovokor.domain.Level
import com.stovokor.domain.Switch
import com.stovokor.domain.SwitchControl
import com.stovokor.gen.level.AmbienceGenerator
import com.stovokor.gen.level.ColumnGenerator
import com.stovokor.gen.level.Command
import com.stovokor.gen.level.DoorGenerator
import com.stovokor.gen.level.DungeonGenerator
import com.stovokor.gen.level.ExitSwitchGenerator
import com.stovokor.gen.level.FogGenerator
import com.stovokor.gen.level.Generator
import com.stovokor.gen.level.GeneratorContext
import com.stovokor.gen.level.LevelBuilder
import com.stovokor.gen.level.LevelMapGenerator
import com.stovokor.gen.level.LightsGenerator
import com.stovokor.gen.level.NavigationGenerator
import com.stovokor.gen.level.PropsGenerator
import com.stovokor.gen.level.QuadFactory
import com.stovokor.gen.level.Room
import com.stovokor.gen.level.lightmap.LightmapGenerator
import com.stovokor.gen.level.quest.QExitBack
import com.stovokor.gen.level.quest.Quest
import com.stovokor.gen.level.quest.QuestGenerator
import com.stovokor.util.jme.JmeExtensions.SpatialExtensions
import com.stovokor.util.math.Dist
import com.stovokor.util.math.Random
import com.stovokor.gen.level.ItemGenerator

class LevelGenerator(implicit val assetManager: AssetManager) {

  def generate(seed: Long, number: Int): Level = {
    val builder = new LevelBuilder(seed, number)
    val cmds: List[Command[_]] = List(
      Command(new DungeonGenerator(seed, number), builder.bsp),
      Command(new QuestGenerator(seed, number), builder.quest),
      Command(new AmbienceGenerator(seed, number), builder.ambiences),
      Command(new LevelMapGenerator, builder.levelNode),
      Command(new DoorGenerator(seed, number), builder.doors),
      Command(new LightsGenerator(seed, number), builder.lights),
      Command(new PropsGenerator(seed, number), builder.props),
      Command(new ColumnGenerator(seed, number), builder.columns),
      Command(new LightmapGenerator(seed, number)),
      Command(new ExitSwitchGenerator(seed, number), builder.switches),
      Command(new FogGenerator(seed, number), builder.fog),
      Command(new NavigationGenerator, builder.navigation),
      Command(new EnemyClassGenerator(seed, number), builder.enemyClasses),
      Command(new EnemyGenerator(seed, number), builder.enemies),
      Command(new ItemGenerator(), builder.addItems))

    builder.width(400)
    builder.depth(400)
    builder.height(30)
    builder.numAmbiences(getAmbiencesToGenerate(seed, number))

    for (cmd <- cmds) builder.run(cmd)
    builder.startPoint(findStartingPoint(seed, number, builder.quest))
    builder.build
  }

  def getAmbiencesToGenerate(seed: Long, number: Int) = {
    val rnd: Random = Random(seed, number)
    Dist.normalInt(rnd, 2, 5)
  }
  def findStartingPoint(seed: Long, number: Int, quest: Quest): Vector3f = {
    def roomCenter(room: Room): Vector3f =
      new Vector3f(room.x + (room.width / 2F), room.y + 1f, room.z + (room.depth / 2F))
    val ver = quest.verts.find(v => v.exit.isDefined && v.exit.get == QExitBack())
    val room = ver.get.room
    roomCenter(room)
  }

}
