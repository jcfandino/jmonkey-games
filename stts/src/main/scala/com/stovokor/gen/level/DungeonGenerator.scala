package com.stovokor.gen.level

import scala.collection.immutable.List
import com.jme3.math.Vector3f
import scala.collection.immutable.Range
import com.jme3.math.Vector2f
import java.util.UUID
import com.stovokor.util.math.Random
import com.jme3.math.Plane

class DungeonGenerator(seed: Long, number: Int) extends Generator[BSPNode](seed, number) {

  val rnd: Random = Random(seed, number)

  val minRoom = (20 + 80 * rnd.nextFloat).toInt
  val maxRoom = (minRoom + 100 * rnd.nextFloat).toInt
  val deltaShrink = .1f + .9f * rnd.nextFloat
  val corridorWidth = 7f
  val corridorHeight = 10f
  val partitioner = new Partitioner

  def positiveOr(n: Float, d: Float) = if (n > 0) n else d

  def randomBetween(p1: Float, p2: Float, limit: Float) =
    if (p2 - p1 < limit) p1 else (p1 + ((p2 - p1 - limit) * rnd.nextFloat)).toFloat

  def generate(ctx: GeneratorContext): BSPNode = {
    val ambienceIndexes = 0 to (ctx.numAmbiences - 1)
    val whole = new Part(0, 0, 0, ctx.width, ctx.height, ctx.depth)
    partitioner.subPart(new LeafBSPNode(whole, new RoomGenerator(whole, ambienceIndexes).generate, ambienceIndexes))
  }

  class Partitioner {

    def subPart(tree: BSPNode): BSPNode = {
      val xCtor = (base: Part, from: Int, to: Int) => Part(base.x + from, base.y, base.z, to, base.height, base.depth)
      val yCtor = (base: Part, from: Int, to: Int) => Part(base.x, base.y + from, base.z, base.width, to, base.depth)
      val zCtor = (base: Part, from: Int, to: Int) => Part(base.x, base.y, base.z + from, base.width, base.height, to)
      val part = tree.part
      xYOrZ(part) match {
        case 'x' =>
          println(s"Dividing by X: $part")
          divideBy(tree, part.width, Vector3f.UNIT_X)(xCtor)
        case 'y' =>
          println(s"Dividing by Y: $part")
          divideBy(tree, part.height, Vector3f.UNIT_Y)(yCtor)
        case 'z' =>
          println(s"Dividing by Z: $part")
          divideBy(tree, part.depth, Vector3f.UNIT_Z)(zCtor)
        case _ =>
          println("Do not divide")
          tree
      }
    }
    def xYOrZ(part: Part) = {
      val xs = List(canDivideBy(part.width), canDivideBy(part.height), canDivideBy(part.depth))
      val coords = List('x', 'y', 'z') //, 'z')
      val validCoords = (for (i <- 0 to coords.size - 1) yield if (xs(i)) coords(i) else None).filterNot(x => x == None)
      if (validCoords.isEmpty) {
        None
      } else {
        validCoords((validCoords.size.toFloat * rnd.nextFloat) toInt)
      }
    }

    def divideProb(n: Int) = {
      // -(x/100-1)^2+1
      if (n > maxRoom) 1 else {
        val x = n.toFloat
        1f - Math.pow((x / maxRoom.toFloat) - 1f, 2f)
      }
    }

    def canDivideBy(measure: Int) = measure >= 2 * minRoom

    def divideAmbiences(tree: BSPNode) = {
      if (tree.ambiences.size == 1) (tree.ambiences, tree.ambiences)
      else tree.ambiences.splitAt(tree.ambiences.size / 2)
    }

    def calculateCut(base: Part, division: Int, normal: Vector3f) = {
      new Plane(normal,
        (new Vector3f(base.x.toFloat, base.y.toFloat, base.z.toFloat) mult normal length) + division)
    }

    def divideBy(tree: BSPNode, measure: Int, normal: Vector3f)(ctor: (Part, Int, Int) => Part): BSPNode = {
      val doDivide = rnd.nextFloat < divideProb(measure)
      if (doDivide) {
        val (amLeft, amRight) = divideAmbiences(tree)
        val base = tree.part
        val division = minRoom + ((measure - (2 * minRoom)).toFloat * rnd.nextFloat).toInt
        val partLeft = ctor(base, 0, division)
        val partRight = ctor(base, division, measure - division)
        val roomLeft = new RoomGenerator(partLeft, amLeft).generate
        val roomRight = new RoomGenerator(partRight, amRight).generate
        val left = LeafBSPNode(partLeft, roomLeft, amLeft)
        val right = LeafBSPNode(partRight, roomRight, amRight)
        //        println(s" L Part1: ${left.part}")
        //        println(s" L Part2: ${right.part}")
        val subPartLeft = subPart(left)
        val subPartRight = subPart(right)
        val corridors = new CorridorGenerator(base, subPartLeft, subPartRight).gen
        val cut = calculateCut(base, division, normal)
        new NonLeafBSPNode(base, subPartLeft, subPartRight, corridors, tree.ambiences, cut)
      } else {
        tree
      }
    }
  }

  class RoomGenerator(val part: Part, val ambiences: Seq[Int]) {
    val pOfRoom = .9f //.51f // TODO: Fix, produces invalid corridors

    def randomShrink(m: Float, min: Float): Float = {
      val s = (m * (1f - (deltaShrink * rnd.nextFloat))).toFloat
      if (s > min) s else min
    }
    def generate = if (rnd.nextFloat < pOfRoom) Some(room) else None

    def generateTextureIndexes(idx: Int) = {
      val walls = rnd.nextFloat
      val ceiling = rnd.nextFloat
      val floor = rnd.nextFloat
      new TextureCoordinates((idx, walls), (idx, ceiling), (idx, floor))
    }
    def room = {
      val rwidth = randomShrink(part.width, minRoom)
      val rheight = randomShrink(part.height, minRoom)
      val rdepth = randomShrink(part.depth, minRoom)
      val rx = part.x + 0.5f * (part.width - rwidth)
      //    val ry = part.y + 0.5f * (part.height - rheight)
      val ry = 0 // TODO - Enable Y axis
      val rz = part.z + 0.5f * (part.depth - rdepth)

      val textures = generateTextureIndexes(ambiences.head)
      //TextureIndexes(0, 1, 1) // TODO Choose randomly

      val room = Room(rx, ry, rz, rwidth, rheight, rdepth, textures)
      //      println(s"Partition: $this")
      //      println(s"Room:      $room")
      room
    }
  }

  class CorridorGenerator(val part: Part, val left: BSPNode, val right: BSPNode) {

    def gen = {
      def opossedWals(w1: Wall, w2: Wall) = w1.normal.equals(w2.normal.negate)
      def canProject(w1: Wall, w2: Wall) = {
        val proj = calcProjection(w1, w2)
        if (proj != null) {
          val (v1, v2) = proj
          //        println(s"  >> calcProj (${v1.x},${v1.y},${v1.z}) - (${v2.x},${v2.y},${v2.z})")
          val dx = v2.x // - v1.x;
          val dy = v2.y //- v1.y;
          //          val dz = v2.z - v1.z;
          val ret = (dx >= 10f) && (dy >= 10f) //&& (dz >= 0f)
          //        println(s"       dif: ($dx,$dy,$dz) = ${ret}")
          ret
        } else {
          false
        }
      }
      def absNor(v: Vector3f) = if (v.x + v.y + v.z > 0) v else v.negate
      def getDist(ws: (Wall, Wall)) = {
        val (w1, w2) = ws
        val o1 = new Vector3f(w1.x, w1.y, w1.z)
        val o2 = new Vector3f(w2.x, w2.y, w2.z)
        val n1 = absNor(w1.normal)
        val n2 = absNor(w2.normal)
        Math.abs(o1.dot(n1) - o2.dot(n2))
      }
      def getFirst(ws: Seq[(Wall, Wall)]) = ws match { case (w1, w2) :: _ => (w1, w2) }
      def positiveFirst(ws: (Wall, Wall)) = {
        val (w1, w2) = ws
        if (w1.x + w1.y + w1.z > 0) ws else (w2, w1)
      }

      def calcProjection(w1: Wall, w2: Wall): (Vector3f, Vector2f) = {
        val n = w1.normal
        (n.x, n.y, n.z) match {
          case (1, 0, 0) =>
            val oz = Math.max(w1.z, w2.z - w2.d1)
            val ez = Math.min(w1.z + w1.d1, w2.z) - oz
            val o = new Vector3f(
              w1.x,
              Math.max(w1.y, w2.y),
              oz + ez)
            val v = new Vector2f(
              ez,
              Math.min(w1.y + w1.d2, w2.y + w2.d2) - o.y)
            (o, v)
          case (0, 1, 0) => // TODO Untested
            val o = new Vector3f(
              Math.max(w1.x, w2.x),
              w1.y,
              Math.max(w1.z, w2.z))
            val v = new Vector2f(
              Math.min(w1.x + w1.d1, w2.x + w2.d2) - o.x,
              Math.min(w1.z + w1.d2, w2.z + w2.d1) - o.z)
            (o, v)
          case (0, 0, 1) =>
            val o = new Vector3f(
              Math.max(w1.x - w1.d1, w2.x),
              Math.max(w1.y, w2.y),
              w1.z)
            val v = new Vector2f(
              Math.min(w1.x, w2.x + w2.d1) - o.x,
              Math.min(w1.y + w1.d2, w2.y + w2.d2) - o.y)
            (o, v)
          case _ =>
            //          println("error! calcProj")
            null
        }
      }
      def widthNormal(wallN: Vector3f) = new Vector3f(1, 0, 1).mult(Vector3f.UNIT_XYZ.subtract(wallN))

      val ws1 = left.walls
      val ws2 = right.walls
      val walls = for (w1 <- ws1; w2 <- ws2; if opossedWals(w1, w2); if canProject(w1, w2)) yield (w1, w2)
      if (!walls.isEmpty) {
        val (w1, w2) = positiveFirst(getFirst(walls.sortBy(getDist)))
        //        println("Wall Pair to connect:")
        //        println(s" L Wall 1 $w1")
        //        println(s" L Wall 2 $w2")

        val proj = calcProjection(w1, w2)
        if (proj != null) { //TODO fix null
          val (ori, span) = proj
          val len = getDist((w1, w2))
          val dir = w1.normal

          val widthAxis = widthNormal(w1.normal) // Gives the coordinate for the width axis
          val widthPos = widthAxis.mult(ori)
          val widthSpan = widthAxis.mult(span.x)
          val posCorr = randomBetween(widthPos.length, widthPos.length + span.x, corridorWidth)
          val originWidthAxis = widthAxis.mult(posCorr)
          val originWidthMagnitude = randomBetween(corridorWidth, span.x - (posCorr - widthPos.length), corridorWidth)

          val x = positiveOr(widthPos.x, ori.x)
          val z = positiveOr(widthPos.z, ori.z)

          val y = ori.y // TODO Enable Y axis 
          val width = originWidthMagnitude
          val height = randomBetween(corridorHeight, span.y, corridorHeight)

          val textures = TextureCoordinates(w1.textureCoor, w1.textureCoor, w1.textureCoor) // TODO Choose better for floor/ceiling

          val c = Corridor(x, y, z, width, height, len, dir, w1, w2, textures)

          //          println(s"Generated corridor: $c")
          //          println(s" L Wall 1: $w1")
          //          println(s" L Wall 2: $w2")

          c :: left.corridors ++ right.corridors
        } else {
          println("No projection")
          left.corridors ++ right.corridors
        }
      } else {
        println("No walls")
        left.corridors ++ right.corridors
      }
    }
  }

}

case class Part(
    val x: Int, val y: Int, val z: Int,
    val width: Int, val height: Int, val depth: Int) {

  override def toString = s"($x,$y,$z) - ($width,$height,$depth)"

}

abstract class BSPNode {
  def part: Part

  def walk(f: (BSPNode => Any)): Any = {
    def j(a: Any, b: Any): Any = None
    def f2(n: BSPNode, a: Any): Any = f(n)
    walk(f2, j, None)
  }

  def walk[T](f: ((BSPNode, T) => T), join: (T, T) => T, acum: T): T

  def corridors: List[Corridor]

  def rooms: List[Room]

  def walls = rooms.flatMap(r => r.walls)

  def ambiences: Seq[Int]

  def cut: Option[Plane]
}

case class NonLeafBSPNode(val part: Part, val left: BSPNode, val right: BSPNode, val corridors: List[Corridor], val ambiences: Seq[Int], planeCut: Plane) extends BSPNode {

  def walk[T](f: ((BSPNode, T) => T), join: (T, T) => T, acum: T): T = {
    val acum2 = f(this, acum)
    val r1 = left.walk(f, join, acum2)
    val r2 = right.walk(f, join, acum2)
    join(r1, r2)
  }

  def rooms = left.rooms ++ right.rooms

  val cut = Some(planeCut)
}

case class LeafBSPNode(val part: Part, val room: Option[Room], val ambiences: Seq[Int]) extends BSPNode {

  def walk[T](f: ((BSPNode, T) => T), join: (T, T) => T, acum: T): T = f(this, acum)

  val corridors = List()
  def rooms = room.toList
  def ambience = ambiences.head
  def cut = None
}

//////// rooms

case class Room(
    val x: Float, val y: Float, val z: Float,
    val width: Float, val height: Float, val depth: Float,
    val textures: TextureCoordinates) {

  override def toString = s"($x,$y,$z) - ($width,$height,$depth)"

  lazy val walls: List[Wall] = {
    val w1 = Wall(x, y, z, Vector3f.UNIT_Z.negate, width, height, textures.forWalls)
    val w2 = Wall(x + width, y, z, Vector3f.UNIT_X, depth, height, textures.forWalls)
    val w3 = Wall(x + width, y, z + depth, Vector3f.UNIT_Z, width, height, textures.forWalls)
    val w4 = Wall(x, y, z + depth, Vector3f.UNIT_X.negate, depth, height, textures.forWalls)
    val w5 = Wall(x, y + height, z, Vector3f.UNIT_Y, width, depth, textures.forCeiling)
    val w6 = Wall(x + width, y, z + depth, Vector3f.UNIT_Y.negate, depth, width, textures.forFloor)
    List(w1, w2, w3, w4, w5, w6)
  }
  val id = java.lang.Long.toString(UUID.randomUUID.getLeastSignificantBits, 36)
}

case class Wall(val x: Float, val y: Float, val z: Float,
    val normal: Vector3f, val d1: Float, val d2: Float,
    val textureCoor: (Int, Float) = (0, 0)) {

  override def toString = s"O($x,$y,$z) - N(${normal.x},${normal.y},${normal.z}) Size: $d1 x $d2"

  def plane = new Plane(normal, new Vector3f(x, y, z) dot normal)

}

case class Corridor(val x: Float, val y: Float, val z: Float,
    val width: Float, val height: Float, val length: Float,
    val dir: Vector3f, val wall1: Wall, val wall2: Wall,
    val textures: TextureCoordinates) {

  override def toString = s"($x,$y,$z) - S($width,$height) L: $length) D(${dir.x},${dir.y},${dir.z})"

  val id = java.lang.Long.toString(UUID.randomUUID.getLeastSignificantBits, 36)
}

case class TextureCoordinates(val forWalls: (Int, Float), val forCeiling: (Int, Float), val forFloor: (Int, Float))
