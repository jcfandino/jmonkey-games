package com.stovokor.gen.level

import com.jme3.material.Material
import com.jme3.math.ColorRGBA

/**
 * @author xuan
 */
object KeyMaterialColorizer {

  def colorize(key: String, material: Material, intensity: Float = 1f) = {
    val baseColor = new ColorRGBA(0.08f, 0.08f, 0.08f, 1)
      .multLocal(intensity)
    val color = keyColor(key)
      .multLocal(0.2f)
      .addLocal(baseColor)

    material.setBoolean("UseMaterialColors", true)
    material.setColor("Diffuse", ColorRGBA.Black)
    material.setColor("Ambient", color)
    material
  }

  def keyColor(key: String) = key.last match {
    case '0' => ColorRGBA.Red.clone
    case '1' => ColorRGBA.Yellow.clone
    case '2' => ColorRGBA.Blue.clone
    case '3' => ColorRGBA.Green.clone
    case _ => ColorRGBA.White.clone
  }
}