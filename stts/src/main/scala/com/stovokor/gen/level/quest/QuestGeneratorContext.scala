package com.stovokor.gen.level.quest

import com.stovokor.gen.level.BSPNode
import com.stovokor.gen.level.Wall
import com.stovokor.gen.level.Corridor
import com.stovokor.gen.level.Room
import com.stovokor.util.math.Random

object QuestGenerationContext {

  // Initialize the Quest and helper structures
  def apply(bsp: BSPNode, seed: Long, number: Int) = {
    var wallToRoom: Map[Wall, Room] = Map()
    for (r <- bsp.rooms; w <- r.walls) wallToRoom = wallToRoom.updated(w, r)

    var corrToRooms: Map[Corridor, (Room, Room)] = Map()
    var roomToCorrs: Map[Room, Set[Corridor]] = Map().withDefaultValue(Set())
    for (c <- bsp.corridors) {
      val room1 = wallToRoom(c.wall1)
      val room2 = wallToRoom(c.wall2)
      corrToRooms = corrToRooms.updated(c, (room1, room2))
      roomToCorrs = roomToCorrs.updated(room1, Set(c) ++ roomToCorrs(room1))
      roomToCorrs = roomToCorrs.updated(room2, Set(c) ++ roomToCorrs(room2))
    }
    // Choose any room
    val initialRoom = roomToCorrs.keys.iterator.next

    // Init rooms
    val roomToVert = bsp.rooms.map(r => (r, new Vert(r))).toMap
    var allVerts = roomToVert.map(_._2).toList

    // Init edges
    val edges = for (c <- bsp.corridors) yield {
      val (roomA, roomB) = corrToRooms(c)
      val (from, to) = (roomToVert(roomA), roomToVert(roomB))
      from.neighbours = to :: from.neighbours
      to.neighbours = from :: to.neighbours
      new Edge(from, to, c)
    }

    // Create quest
    val quest = new Quest(roomToVert(initialRoom), allVerts, edges)
    quest.initDepths

    new QuestGenerationContext(bsp, quest, allVerts, roomToVert, initialRoom, edges, seed, number)
  }
}
class QuestGenerationContext(
    val bsp: BSPNode,
    val quest: Quest,
    val allVerts: List[Vert],
    val roomToVert: Map[Room, Vert],
    val initialRoom: Room,
    val edges: List[Edge],
    val seed: Long,
    val number: Int) {

  val rnd = Random(seed, number)

}
