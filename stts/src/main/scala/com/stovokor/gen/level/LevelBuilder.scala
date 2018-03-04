package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.light.Light
import com.jme3.math.Vector3f
import com.jme3.post.FilterPostProcessor
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.stovokor.ai.Navigation
import com.stovokor.domain.Door
import com.stovokor.domain.Level
import com.stovokor.domain.Switch
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.gen.EnemyClass
import com.stovokor.gen.EnemyGenerator
import com.stovokor.gen.level.lightmap.LightmapGenerator
import com.stovokor.gen.level.lightmap.LightmapUVBuilder
import com.stovokor.gen.level.quest.Quest
import com.stovokor.gen.level.quest.QuestGenerator
import com.stovokor.domain.item.Item
import com.stovokor.domain.Prop
import com.stovokor.domain.Column

class LevelBuilder(seed: Long, number: Int)(implicit val assetManager: AssetManager) {

  val start = System.currentTimeMillis
  val c = new GeneratorContext

  def build = {
    new Level(number,
      levelNode,
      bsp,
      quest,
      enemies,
      columns,
      props,
      lights,
      startPoint,
      switches,
      doors,
      fog,
      navigation,
      items)
  }

  def run[T](cmd: Command[T]): T = {
    cmd.run(c)
  }

  def run[T](gen: Generator[T]): T = {
    run(Command(gen))
  }

  def run[T](gen: Generator[T], setter: (T => Unit)): LevelBuilder = {
    run(Command(gen, setter))
    this
  }

  def bsp(b: BSPNode) { c.bsp = Some(b) }
  def bsp = c.bsp.get

  def quest(q: Quest) { c.quest = Some(q) }
  def quest = c.quest.get

  def width(w: Int) { c.width = w }
  def width = c.width

  def height(h: Int) { c.height = h }
  def height = 0

  def depth(d: Int) { c.depth = d }
  def depth = 0

  def numAmbiences(n: Int) { c.numAmbiences = n }
  def numAmbiences = c.numAmbiences

  def ambiences(a: Ambiences) { c.ambiences = Some(a) }
  def ambiences = c.ambiences.get

  def uvBuilder(b: LightmapUVBuilder) { c.uvBuilder = b }
  def uvBuilder = c.uvBuilder

  def levelNode(n: Node) { c.levelNode = n }
  def levelNode = c.levelNode

  def columns(s: Set[Column]) { c.columns = s }
  def columns = c.columns

  def lights(s: Set[Light]) { c.lights = s }
  def lights = c.lights

  def navigation(n: Navigation) { c.navigation = Some(n) }
  def navigation = c.navigation.get

  def enemies(s: Set[EnemyCharacter]) { c.enemies = s }
  def enemies = c.enemies

  def props(s: Set[Prop]) { c.props = s }
  def props = c.props

  def startPoint(p: Vector3f) { c.startPoint = p }
  def startPoint = c.startPoint

  def switches(s: Set[Switch]) { c.switches = s }
  def switches = c.switches

  def doors(s: Set[Door]) { c.doors = s }
  def doors = c.doors

  def fog(f: FilterPostProcessor) { c.fog = Some(f) }
  def fog = c.fog.get

  def enemyClasses(l: List[EnemyClass]) { c.enemyClasses = l }
  def enemyClasses = c.enemyClasses

  def addItems(is: Set[Item]) { c.items = c.items ++ is }
  def items = c.items
}

object Command {
  def apply[T](gen: Generator[T], setter: (T => Unit)) = new Command(gen, Some(setter))
  def apply[T](gen: Generator[T]) = new Command(gen, None)
}
class Command[T](gen: Generator[T], setter: Option[(T => Unit)]) {
  def run(ctx: GeneratorContext): T = {
    val o = gen.generate(ctx)
    if (setter.isDefined) setter.get.apply(o)
    o
  }
}
