package com.stovokor

import com.stovokor.gen.level.TextureCatalog
import com.stovokor.gen.level.AmbienceGenerator
import com.stovokor.gen.level.TextureDefinition
import scala.util.Random
import com.stovokor.gen.level.GeneratorContext

object AmbiencesTest {

  def main(args: Array[String]): Unit = {
    val gen = new AmbienceGenerator((new Random).nextLong(), 1)
    val ctx = new GeneratorContext
    val ambs = gen.generate(ctx)

    for ((a, i) <- ambs.ambiences.zipWithIndex) {

      println("Ambience " + i)
      print("Walls", a.texturesForWalls)
      print("Ceilings", a.texturesForCeilings)
      print("Floors", a.texturesForFloors)
    }
  }

  def print(use: String, texts: Seq[TextureDefinition]) {
    println("  - " + use)
    for (d <- texts) {
      println(s"     - $d")
    }
  }
}