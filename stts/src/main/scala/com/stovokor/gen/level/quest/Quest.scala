package com.stovokor.gen.level.quest

import com.stovokor.domain.SwitchId
import com.stovokor.gen.level.Corridor
import com.stovokor.gen.level.Room
import scala.util.Random
import com.jme3.math.Vector3f
import com.stovokor.domain.Ammo

class Quest(val root: Vert, val verts: List[Vert], val edges: List[Edge]) {

  def initialRoom = verts.find(v => v.exit == Some(QExitBack()))
  def exitARoom = verts.find(v => v.exit == Some(QExitA()))
  def exitBRoom = verts.find(v => v.exit == Some(QExitB()))

  def pathBetween(a: Vert, b: Vert): List[Vert] = {
    pathBetween(a, b, List(b))
  }
  def pathBetween(a: Vert, b: Vert, path: List[Vert]): List[Vert] = {
    if (b.depth == a.depth) path
    else {
      val toA = b.neighbours.sortBy(_.depth).take(1)
      pathBetween(a, toA.head, toA ++ path)
    }
  }

  lazy val pathsFromInit: List[List[Vert]] = {
    val o = initialRoom.get
    for (v <- verts) yield pathBetween(o, v)
  }
  // Set depth
  def initDepths() {
    def setDepth(root: Vert) {
      def setDepth(vert: Vert, before: Vert): Unit = {
        vert.depth = before.depth + 1
        for (n <- vert.neighbours) {
          if (n != before) setDepth(n, vert)
        }
      }
      for (n <- root.neighbours) setDepth(n, root)
    }
    setDepth(root)
  }
}

class VertCoef {
  var difficulty: Float = 0
  var intensity: Float = 0
  var restitution: Float = 0
  def isClean = difficulty == 0 && intensity == 0 && restitution == 0
}

class Vert(val room: Room) {
  var neighbours: List[Vert] = Nil

  var enemies: List[QEnemy] = Nil
  var props: List[QProp] = Nil
  var columns: List[QColumn] = Nil
  var exit: Option[QExit] = None
  var items: List[QItem] = Nil

  val coef = new VertCoef
  val cellSize = 4
  val grid = new Grid(room.width.round / cellSize, room.depth.round / cellSize)
  var itemGridPosition: Map[Any, (Int, Int)] = Map.empty

  def absoluteItemPosition(item: Any) = {
    val itemPos = itemGridPosition(item)
    assert(!grid.cell(itemPos._1, itemPos._2).isFree)
    new Vector3f(
      room.x + cellSize * (itemPos._1 + .5f),
      room.y,
      room.z + cellSize * (itemPos._2 + .5f))
  }
  override def equals(o: Any) = {
    o != null && o.isInstanceOf[Vert] && o.asInstanceOf[Vert].room.id == room.id
  }
  override def hashCode = room.id.hashCode

  def place(p: (Int, Int), prop: QProp) = {
    props = props ++ Set(prop)
    itemGridPosition = itemGridPosition.updated(prop, p)
    grid.cell(p).isFree = false
  }
  def place(p: (Int, Int), enemy: QEnemy) = {
    enemies = enemies ++ List(enemy)
    itemGridPosition = itemGridPosition.updated(enemy, p)
    grid.cell(p).isFree = false
  }
  def place(p: (Int, Int), i: QItem) = {
    items = items ++ List(i)
    itemGridPosition = itemGridPosition.updated(i, p)
    grid.cell(p).isFree = false
  }

  var depth = 0 // From initial vert
}

class Edge(val v1: Vert, val v2: Vert, val corridor: Corridor) {
  var doorA: Option[QDoor] = None
  var doorB: Option[QDoor] = None

  def supportsTwoDoors = corridor.length >= 10

  def directed = if (v1.depth < v2.depth) (v1, v2) else (v2, v1)

  def id = v1.room.id + v2.room.id
}

class Grid(val sizeX: Int, val sizeZ: Int) {
  var cells: Map[(Int, Int), Cell] = Map.empty.withDefault(co => new Cell(co._1, co._2))

  def cell(p: (Int, Int)): Cell = cell(p._1, p._2)

  def cell(x: Int, z: Int): Cell = {
    assert(x >= 0 && x < sizeX, s"Bound X: $x / sizeX: $sizeX")
    assert(z >= 0 && z < sizeZ, s"Bound Z: $z / sizeZ: $sizeZ")
    if (!cells.contains(x, z)) cells = cells.updated((x, z), cells(x, z))
    cells(x, z)
  }

  def getAnyFreePosition(rnd: Random, tries: Int = 10000): Option[(Int, Int)] = {
    for (_ <- 0 to tries) {
      val x = rnd.nextInt(sizeX)
      val z = rnd.nextInt(sizeZ)
      if (!cells.contains(x, z) || cell(x, z).isFree) {
        return Some((x, z))
      }
    }
    None
  }
}

class Cell(x: Int, z: Int) {
  var canGoNorth = true
  var canGoSouth = true
  var canGoEast = true
  var canGoWest = true
  var canPass = true
  var isFree = true
}

abstract class QExit() {
  def switchId: String
}
case class QExitBack() extends QExit {
  val switchId = SwitchId.levelBack
}
case class QExitA() extends QExit {
  val switchId = SwitchId.levelExitA
}
case class QExitB() extends QExit {
  val switchId = SwitchId.levelExitB
}

case class QDoor(val key: Option[String]) {}

class QEnemy(val index: Integer, val angle: Float = 0) {}

class QProp() {}

abstract class QItem
class QHealth(val health: Int) extends QItem
class QArmor(val armor: Int) extends QItem
class QAmmo(val ammoType: Ammo, val amount: Int) extends QItem
case class QCard(val key: String) extends QItem

class QColumn(val shape: QColumnShape) {}

abstract class QColumnShape
case class QColumnSquared() extends QColumnShape
case class QColumnRounded() extends QColumnShape

