package com.stovokor.domain

import com.jme3.light.Light
import com.jme3.math.Vector3f
import com.jme3.post.FilterPostProcessor
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.stovokor.gen.level.quest.Quest
import com.stovokor.ai.Navigation
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.domain.item.Item
import com.stovokor.gen.level.BSPNode

class Level(
    val number: Int,
    val model: Node,
    val bsp: BSPNode,
    val quest: Quest,
    val enemies: Set[EnemyCharacter],
    val columns: Set[Column],
    val props: Set[Prop],
    val lights: Set[Light],
    val startPoint: Vector3f,
    val switches: Set[Switch],
    val doors: Set[Door],
    val fog: FilterPostProcessor,
    val nav: Navigation,
    val items: Set[Item]) {

  def entities: Set[Entity[_]] = props ++ switches ++ doors ++ items ++ enemies //++ columns
}
