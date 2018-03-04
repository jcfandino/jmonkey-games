package com.stovokor

import com.stovokor.gen.level.TextureCatalog

object TexturesTest {

  def main(args: Array[String]): Unit = {
    val defs = TextureCatalog.load.surface

    println(s"${defs.size} textures found")
    for ((c, ts) <- defs) {
      println(c)
      for (t <- ts) println(s" - $t")
    }
  }
}