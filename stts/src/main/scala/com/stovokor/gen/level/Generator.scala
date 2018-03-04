package com.stovokor.gen.level

import com.jme3.light.Light
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.stovokor.ai.Navigation
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.gen.level.lightmap.LightmapUVBuilder
import com.stovokor.gen.level.quest.Quest
import com.jme3.post.FilterPostProcessor
import com.stovokor.domain.Door
import com.stovokor.domain.Switch
import com.stovokor.gen.EnemyClass
import com.stovokor.domain.item.Item
import com.stovokor.domain.Prop
import com.stovokor.domain.Column

abstract class Generator[T](seed: Long, number: Int) {
  def generate(context: GeneratorContext): T
}

class GeneratorContext {
  var width: Int = 0
  var height: Int = 0
  var depth: Int = 0
  var numAmbiences: Int = 0
  var bsp: Option[BSPNode] = None
  var quest: Option[Quest] = None
  var ambiences: Option[Ambiences] = None
  var uvBuilder: LightmapUVBuilder = new LightmapUVBuilder
  var levelNode: Node = new Node
  var columns: Set[Column] = Set()
  var lights: Set[Light] = Set()
  var navigation: Option[Navigation] = None
  var enemies: Set[EnemyCharacter] = Set()
  var props: Set[Prop] = Set()
  var startPoint: Vector3f = Vector3f.ZERO
  var switches: Set[Switch] = Set()
  var doors: Set[Door] = Set()
  var fog: Option[FilterPostProcessor] = None
  var enemyClasses: List[EnemyClass] = List()
  var cards: Set[Item] = Set()
  var items: Set[Item] = Set()
  //  var armor: Set[Item] = Set()
  //  var health: Set[Item] = Set()

}