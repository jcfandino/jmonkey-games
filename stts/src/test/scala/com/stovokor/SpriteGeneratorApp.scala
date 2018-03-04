package com.stovokor

import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import java.io.File

/**
 * @author xuan
 */
object SpriteGeneratorApp extends App {

  override def main(args: Array[String]): Unit = {
    SpriteGenerator.generate()
    FontGenerator.generate()
  }

  trait Constants {
    val width = 256
    val height = 256
    val bigSize = 32
    val smallSize = 16

  }
  object SpriteGenerator extends Constants {

    def filePath = s"cache/sprites.png"
    val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    def draw(x: Int, y: Int, c: Color) {
      img.setRGB(x, y, c.getRGB())
    }

    def generate() {
      for (i <- 0.to(width - 1, bigSize) ++ List(width - 1)) {
        for (j <- 0 to height - 1) {
          draw(i, j, Color.blue)
        }
      }
      for (i <- 0.to(width - 1, smallSize)) {
        for (j <- (height / 2) to height - 1) {
          draw(i, j, Color.blue)
        }
      }
      for (i <- 0 to width - 1) {
        for (j <- 0.to(height / 2, bigSize)) {
          draw(i, j, Color.blue)
        }
      }
      for (i <- 0 to width - 1) {
        for (j <- (height / 2).to(height - 1, smallSize) ++ List(height - 1)) {
          draw(i, j, Color.blue)
        }
      }
      ImageIO.write(img, "png", new File(filePath))
    }
  }

  object FontGenerator extends Constants {
    val bigChars = (width / bigSize) * (height / bigSize) / 2
    val smallChars = (width / smallSize) * (height / smallSize) / 2

    def generate() {
      val bigs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      for ((c, i) <- bigs.zipWithIndex) {
        printBig(c, i)
      }

      val smalls = "abcdefghijklmnopqrstuvwxyz"
      for ((c, i) <- smalls.zipWithIndex) {
        printSmall(c, i)
      }
    }

    def printBig(c: Char, n: Int) {
      val cPerRow = width / bigSize
      val col = n % cPerRow
      val row = n / cPerRow
      val x = bigSize * col
      val y = bigSize * row
      print(c, x, y, bigSize, bigSize)
    }
    def printSmall(c: Char, n: Int) {
      val cPerRow = width / smallSize
      val col = n % cPerRow
      val row = n / cPerRow
      val x = smallSize * col
      val y = smallSize * row + height / 2
      print(c, x, y, smallSize, smallSize)
    }
    def print(c: Char, x: Int, y: Int, width: Int, height: Int) {
      println(s"char id=${c.toInt}    x=$x    y=$y    width=$width    height=$height    xoffset=0    yoffset=0    xadvance=$width     page=0    chnl=0")
    }
  }
}