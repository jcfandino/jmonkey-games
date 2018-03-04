package com.stovokor.gen.level.quest

import com.stovokor.gen.level.BSPNode
import com.stovokor.gen.level.Corridor
import com.stovokor.gen.level.Generator
import com.stovokor.gen.level.GeneratorContext
import com.stovokor.gen.level.Room
import com.stovokor.gen.level.Wall
import com.stovokor.util.math.Random
import com.stovokor.domain.PistolBullet
import com.stovokor.domain.Shell
import com.stovokor.domain.RifleBullet
import com.stovokor.Settings.Debug
import com.jme3.math.FastMath

class QuestGenerator(seed: Long, number: Int) extends Generator[Quest](seed, number) {

  class QuestBuilder(c: QuestGenerationContext) {
    def quest = c.quest

    def placeExitDoors() {
      val candidates = c.allVerts.sortBy(_.depth).takeRight(5)
      val shuffled = c.rnd.shuffle(candidates).take(2)
      shuffled(0).exit = Some(QExitA())
      shuffled(1).exit = Some(QExitB())
      c.roomToVert(c.initialRoom).exit = Some(QExitBack())

    }

    def placeCorridorDoors() {
      val pRoom = c.rnd.nextFloat
      for (e <- c.edges) {
        if (c.rnd.nextFloat < pRoom) {
          e.doorA = Some(new QDoor(None))
        }
        if (e.supportsTwoDoors && c.rnd.nextFloat < pRoom) {
          e.doorB = Some(new QDoor(None))
        }
      }
    }

    def placeColumns() {

      def getShape() = {
        c.rnd.shuffle(List(QColumnSquared(), QColumnRounded())).head
        //        QColumnSquared()
      }

      for (r <- c.allVerts) {
        val shape = getShape
        val pOfColumn = c.rnd.nextFloat * .75f
        if (c.rnd.nextFloat < pOfColumn) {
          def placeColumn(x: Int, z: Int) {
            if (r.grid.cell(x, z).isFree) {
              val c = new QColumn(shape)
              r.columns = r.columns ++ List(c)
              r.itemGridPosition = r.itemGridPosition.updated(c, (x, z))
              r.grid.cell(x, z).isFree = false
              r.grid.cell(x, z).canPass = false
            }
          }
          def findEvenlyDivision(l: Int) = {
            val candidates = (for (i <- 2 to l) yield if (l % i == 0) i else l)
            if (candidates.isEmpty) 1 else candidates(c.rnd.nextInt(candidates.size))
          }
          val ratio = 0.1f + 0.9f * c.rnd.nextFloat
          val dx = ((r.grid.sizeX - 2) * (1 - ratio) * 0.5f).toInt + 1
          val dz = ((r.grid.sizeZ - 2) * (1 - ratio) * 0.5f).toInt + 1
          val xlen = r.grid.sizeX - 1 - dx
          val zlen = r.grid.sizeZ - 1 - dz
          for (x <- dx to (xlen, findEvenlyDivision(xlen - dx))) {
            placeColumn(x, dz)
            placeColumn(x, r.grid.sizeZ - 1 - dz)
          }
          for (z <- dz to (zlen, findEvenlyDivision(zlen - dz))) {
            placeColumn(dx, z)
            placeColumn(r.grid.sizeX - 1 - dz, z)
          }
        }
      }
    }

    def placeEnemies = {
      val classes = 2
      val prob = 0.2f
      //      def maxEnePerRoom(r: Vert) = Math.log10(c.rnd.nextInt(r.grid.sizeX * r.grid.sizeZ)).toInt
      def maxEnePerRoom(r: Vert) = 3 * Math.log10(r.grid.sizeX * r.grid.sizeZ).toInt
      def center(r: Vert) = ((r.room.x + r.room.width) / 2f, (r.room.z + r.room.depth) / 2f)
      def placeIn(r: Vert, x: Int, z: Int, idx: Int) {
        val (rx, rz) = center(r)
        // FIXME - Sometimes room is isolated?
        val (px, pz) = r.neighbours.sortBy(_.depth).map(center).head
        val atan = Math.atan((pz - rz) / (px - rx + .0000001f)).toFloat
        val ang = if (px > rx) atan else atan + FastMath.PI
        r.place((x, z), new QEnemy(idx, ang))
      }
      def getIndex(d: Float) = {
        if (d < .4f) 0
        else if (d < .75) 1
        else 2
      }
      for (r <- c.allVerts) {
        val number = (r.coef.intensity * maxEnePerRoom(r)).toInt
        println(s"Placing enemies - Room ${r.room.id} - Enemies $number (${r.coef.intensity}) - Max: ${maxEnePerRoom(r)}")
        for (i <- 1 to number) {
          val index = getIndex(r.coef.difficulty)
          r.grid.getAnyFreePosition(c.rnd) match {
            //c.rnd.nextInt(classes)
            case Some((x, z)) => placeIn(r, x, z, index)
            case None =>
          }
        }
      }
    }

    def placeAccessCards() {
      def placeKeys(pExit: List[Vert]) = {
        // Find paths through the level that don't lead to the exit door A (choose the longer ones)
        val alternatives = for (v <- pExit.drop(1)) yield {
          val fromInit = quest.pathBetween(pExit.head, v)
          val toEnd = quest.pathBetween(v, pExit.last)
          val altPaths = quest.pathsFromInit.filterNot(p => p.startsWith(fromInit)).sortBy(-_.size)
          println(s"PathsFromInit: ${quest.pathsFromInit.size} - AlterPaths: ${altPaths.size}")
          val longest = if (altPaths.isEmpty) List() else altPaths.head
          //(v, longest)
          (longest, v)
        }
        // Avoid more that one path to hold a card
        val map = alternatives.toMap
        val uniqueAlts = map.map(x => (x._2, x._1)).toList.filterNot(_._2.isEmpty)
        println(s"Alternative paths ${alternatives.size} - uniques: ${uniqueAlts.size}")
        // Take the 3 longest
        val keyedDoors = uniqueAlts.sortBy({ case (a, b) => -b.size }).take(3)
        // For each (and a counter)
        for ((kd, i) <- keyedDoors.zipWithIndex) kd match {
          case (room, path) => {
            println(s" -> Path with key: ${path.map(v => v.room.id)}")
            val prevRoom = room.neighbours.sortBy(_.depth).take(1)(0) // Find the room before the locked door
            val edge = c.quest.edges.find(e => { // Get its respective Edge
              val (r1, r2) = e.directed; r1 == prevRoom && r2 == room
            })
            val position = path.last.grid.getAnyFreePosition(c.rnd, 1000)
            if (edge.isDefined && !path.isEmpty && position.isDefined) {
              var key = s"$number-$i"
              val card = QCard(key)
              edge.get.doorA = Some(QDoor(Some(key))) // Lock the door
              path.last.place(position.get, card)
            }
          }
        }
      }
      val pathToExit = quest.pathBetween(quest.initialRoom.get, quest.exitARoom.get)
      placeKeys(pathToExit)
    }

    def placeProps {
      for (r <- c.allVerts; x <- 0 to r.grid.sizeX - 1; z <- 0 to r.grid.sizeZ - 1) {
        //      val pOfBox = 0.2f
        val pOfBox = c.rnd.nextFloat() * 0.01f
        if (c.rnd.nextFloat < pOfBox && r.grid.cell(x, z).isFree) {
          val p = new QProp()
          r.place((x, z), p)
        }
      }
    }

    def placeArmor {
      for (r <- c.allVerts) {
        if (r.coef.restitution > 0.3 && c.rnd.nextBoolean)
          r.grid.getAnyFreePosition(c.rnd).foreach(p => {
            val armor = new QArmor(25)
            r.place(p, armor)
          })
      }
    }

    def placeHealth {
      def maxPerRoom(r: Vert) = (r.coef.restitution * 4).toInt
      for (r <- c.allVerts; _ <- 1 to maxPerRoom(r)) {
        if (c.rnd.nextFloat < 0.5f) {
          r.grid.getAnyFreePosition(c.rnd).foreach(p => {
            val health = new QHealth(25)
            r.place(p, health)
          })
        }
      }
    }

    val ammoTypes = List(PistolBullet, Shell, RifleBullet)
    def placeAmmo {
      def maxPerRoom(r: Vert) = (r.coef.restitution * 4).toInt
      def createAmmo(r: Vert) = {
        if (r.coef.difficulty < 0.3f)
          new QAmmo(PistolBullet, 20)
        else c.rnd.shuffle(List(
          new QAmmo(Shell, 10), new QAmmo(RifleBullet, 50))).head
      }
      for (r <- c.allVerts; _ <- 1 to maxPerRoom(r)) {
        if (c.rnd.nextFloat < .5f) {
          r.grid.getAnyFreePosition(c.rnd).foreach(p => {
            r.place(p, createAmmo(r))
          })
        }
      }
    }

    def calculateCoeficients {
      case class FloatWithExp(i: Float) { def ~(j: Int) = Math.pow(i, j) }
      implicit def floatWithExp(i: Float) = FloatWithExp(i)

      def mainPathIntensity(x: Float) = Math.max(0f, ((250 * x ~ 3 - 500 * x ~ 2 + 315f * x - 38) / 30).toFloat)
      def mainPathDifficulty(x: Float) = Math.max(0f, (.8 * x ~ 2 + .2).toFloat)
      def mainPathRestitution(x: Float) = (26.1905 * x ~ 4 - 46 * x ~ 3 + 21 * x ~ 2 - 0.65 * x + 0.2).toFloat

      def altPathIntensity(x: Float) = (0.416667 * x ~ 3 - 0.625 * x ~ 2 - 0.141667 * x + 0.5).toFloat
      def altPathDifficulty(x: Float) = (1.5625 * x ~ 3 - 0.9375 * x * 2 - 0.625 * x + 0.4).toFloat
      def altPathRestitution(x: Float) = (-4.25 * x ~ 3 + 7.775 * x ~ 2 - 2.625 * x + 0.1).toFloat

      def restrict(f: Float => Float)(x: Float) = Math.max(0f, Math.min(f(x), 1f))
      def applyCoeficients(intensity: Float => Float, difficulty: Float => Float, restitution: Float => Float)(path: List[Vert]) {
        path.zipWithIndex.foreach({
          case (v, i) =>
            if (v.coef.isClean) {
              val x = i.toFloat / path.length
              v.coef.intensity = restrict(intensity)(x)
              v.coef.difficulty = restrict(difficulty)(x)
              v.coef.restitution = restrict(restitution)(x)
              println(s"Setting coef - Room ${v.room.id} - Intensity ${v.coef.intensity} - Diff: ${v.coef.difficulty}")
            } else {
              println(s"Warning, reapplying coef to Room ${v.room.id} - Intensity ${v.coef.intensity} - Diff: ${v.coef.difficulty}")
            }
        })
      }
      def alternativePaths(mainPath: List[Vert]) =
        quest.pathsFromInit
          .filterNot(p => p.last.neighbours.size > 1)
          .map(p => p.filterNot(mainPath.contains))
          .filterNot(_.isEmpty)

      val mainPath = quest.pathBetween(quest.initialRoom.get, quest.exitARoom.get)
      val altPaths = alternativePaths(mainPath)

      applyCoeficients(mainPathIntensity, mainPathDifficulty, mainPathRestitution)(mainPath)
      altPaths.foreach(applyCoeficients(altPathIntensity, altPathDifficulty, altPathRestitution))
    }
  }

  def generate(ctx: GeneratorContext): Quest = {
    val c = QuestGenerationContext(ctx.bsp.get, seed, number)
    val b = new QuestBuilder(c)

    b.placeExitDoors
    b.placeCorridorDoors
    b.placeAccessCards
    b.calculateCoeficients
    b.placeColumns
    b.placeEnemies
    b.placeProps
    b.placeAmmo
    b.placeArmor
    b.placeHealth
    c.quest
  }
}

