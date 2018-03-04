package com.stovokor.gen.level

import com.jme3.light.DirectionalLight
import com.jme3.light.AmbientLight
import com.jme3.light.Light
import scala.collection.mutable.ListBuffer
import com.jme3.light.PointLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.stovokor.util.jme.JmeExtensions.SpatialExtensions
import com.stovokor.util.math.Random
import com.stovokor.util.math.Dist

class LightsGenerator(seed: Long, number: Int) extends Generator[Set[Light]](seed, number) {
  def generate(ctx: GeneratorContext) = {
    val quest = ctx.quest.get
    val node = ctx.levelNode
    val ids = quest.verts.map(v => v.room).map(r => r.id)
    val pointLights = ListBuffer[Light]()
    val rnd = Random(seed, number)
    //    val colors = dList(ColorRGBA.randomColor())
    node.depthFirst(s => {
      if (s.getUserData("room.id") != null) {
        val x: Float = s.getUserData("room.x")
        val y: Float = s.getUserData("room.y")
        val z: Float = s.getUserData("room.z")
        val w: Float = s.getUserData("room.w")
        val h: Float = s.getUserData("room.h")
        val d: Float = s.getUserData("room.d")
        val l = new PointLight
        val offset = 3f
        l.setPosition(new Vector3f(x + (w / 2), y + h - offset, z + (d / 2)))
        l.setRadius(500f)
        l.setColor(ColorRGBA.White.mult((.2f + 10f * rnd.nextFloat())))
        //        l.setColor(ColorRGBA.White.mult(10f))
        l.setName(s.getUserData("room.id"))
        pointLights += l
        println("Generated light " + l.getName())
      }
    })

    // Directional
    val dl = new DirectionalLight
    dl.setColor(ColorRGBA.White.mult(0.8f))
    dl.setDirection(new Vector3f(-1, -1, -1).normalize)
    // Ambient
    val am = new AmbientLight
    am.setColor(ColorRGBA.White.mult(2f))

    //    Set(dl, am)
//        Set(am, dl) ++ pointLights
    Set(am) ++ pointLights
  }
}
