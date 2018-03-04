package com.stovokor.util.sunflow

import org.sunflow.core.camera.PinholeLens
import org.sunflow.SunflowAPI
import org.sunflow.core.light.DirectionalSpotlight
import org.sunflow.core.light.PointLight
import org.sunflow.core.Shader
import org.sunflow.image.Color
import org.sunflow.core.primitive.TriangleMesh

class CompatibleSunflowAPI extends SunflowAPI {

  def parameter(name: String, color: Color): Unit = {
    val rgb = color.getRGB()
    parameter(name, null, rgb(0), rgb(1), rgb(2))
  }
  def shader(name: String, s: Shader): Unit = {
    shader(name, "diffuse")
  }

  var objectNames: Map[String, Int] = Map.empty.withDefault(_ => 0)

  def getUniqueName(name: String) = {
    val i = objectNames(name)
    objectNames = objectNames.updated(name, i + 1)
    s"@sc_1::${name}_$i"
  }

  def light(name: String, l: PointLight): Unit = light(name, "point")
  def light(name: String, l: DirectionalSpotlight): Unit = light(name, "directional")

  def camera(name: String, cam: PinholeLens): Unit = camera(name, "pinhole")
  def parse(file: String): Unit = include(file)

  def geometry(name: String, m: TriangleMesh): Unit = geometry(name, "triangle_mesh")
}
