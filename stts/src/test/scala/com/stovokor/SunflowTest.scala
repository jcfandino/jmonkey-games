package com.stovokor

import org.sunflow.SunflowAPI
import org.sunflow.core.display.FileDisplay
import org.sunflow.core.shader.DiffuseShader
import org.sunflow.image.Color
import org.sunflow.math.Point3
import org.sunflow.core.light.PointLight
import org.sunflow.core.camera.PinholeLens
import org.sunflow.math.Vector3
import org.sunflow.core.light.DirectionalSpotlight
import org.sunflow.core.light.SunSkyLight
import org.sunflow.core.Shader
import org.sunflow.math.Matrix4
import org.sunflow.system.UI
import com.stovokor.util.sunflow.CompatibleSunflowAPI

object SunflowTest {

  def main(args: Array[String]): Unit = {
    SunflowTest.run
  }

  val sunflow = new CompatibleSunflowAPI

  def run {
    UI.verbosity(4)
    //    val result = sunflow.parse("/home/xuan/progs/sunflow/examples/aliens_shiny.sc");
    //    if (!result) println("Failed to parse")

    // Add stuff in this order:
    // Image Settings
    // Lights
    // Shaders
    // Modifiers
    // Objects
    // Instances

    // Bake
    sunflow.parameter("baking.instance", "box")
    sunflow.parameter("baking.viewdep", false)
    sunflow.options(SunflowAPI.DEFAULT_OPTIONS)
    // Image settings
    sunflow.parameter("resolutionX", 600);
    sunflow.parameter("resolutionY", 600);

    sunflow.parameter("aa.min", 0)
    sunflow.parameter("aa.max", 2)

    sunflow.parameter("samples", 8)
    sunflow.parameter("contrast", 0.1f)
    sunflow.parameter("filter", "gaussian")
    sunflow.parameter("jitter", false)
    sunflow.parameter("caustics", "none")

    sunflow.options(SunflowAPI.DEFAULT_OPTIONS)

    // GI
    //    sunflow.parameter("gi.engine", "irr-cache")
    //    sunflow.parameter("gi.irr-cache.samples", 256)
    //    sunflow.parameter("gi.irr-cache.tolerance", .01f)
    //    sunflow.parameter("gi.irr-cache.min_spacing", 1f)
    //    sunflow.parameter("gi.irr-cache.max_spacing", 1f)
    //    sunflow.parameter("gi.irr-cache.gmap.emit", 1000000)
    //    sunflow.parameter("gi.irr-cache.gmap", "grid")
    //    sunflow.parameter("gi.irr-cache.gmap.gather", 100)
    //    sunflow.parameter("gi.irr-cache.gmap.redius", .5f)
    //    sunflow.options(SunflowAPI.DEFAULT_OPTIONS)

    //    sunflow.parameter("gi.engine", "ambocc")
    //    sunflow.parameter("gi.ambocc.bright", new Color(1f))
    //    sunflow.parameter("gi.ambocc.dark", new Color(0f))
    //    sunflow.parameter("gi.ambocc.samples", 32)
    //    sunflow.parameter("gi.ambocc.maxdist", 3f)
    //    sunflow.options(SunflowAPI.DEFAULT_OPTIONS)

    //gi {
    //type ambocc
    //bright { "sRGB nonlinear" 1 1 1 }
    //dark { "sRGB nonlinear" 0 0 0 } //samples 32
    //maxdist 3.0
    //}

    // Shader
    sunflow.parameter("diffuse", new Color(1f, 1f, 1f))
    sunflow.shader("default", new DiffuseShader)

    sunflow.parameter("bright", new Color(1f))
    sunflow.parameter("dark", new Color(0f))
    sunflow.parameter("samples", 16)
    sunflow.parameter("maxdist", 10f)
    sunflow.shader("ambocc", "ambient_occlusion");
    // Light
    {
      sunflow.parameter("center", new Point3(6f, 10f, 6f))
      sunflow.parameter("power", new Color(10000f, 0, 0))
      val lightName = sunflow.getUniqueName("pointLight")
      sunflow.light(lightName, new PointLight)
    }
    {
      val source = new Point3(-2f, 10f, 4f)
      sunflow.parameter("source", source)
      sunflow.parameter("dir", new Vector3(0f, -1f, 0f));
      sunflow.parameter("radius", 8f)
      sunflow.parameter("radiance", new Color(0, 0, 10000f))
      val lightName = sunflow.getUniqueName("dirlight")
      sunflow.light(lightName, new DirectionalSpotlight)
    }
    {
      sunflow.parameter("up", new Vector3(0, 1, 0))
      sunflow.parameter("east", new Vector3(0, 0, 1))
      sunflow.parameter("sundir", new Vector3(1, 1, 1))
      sunflow.parameter("turbidity", 4f)
      sunflow.parameter("samples", 64)
      val sunsky = new SunSkyLight
      val sunskyName = sunflow.getUniqueName("sunsky")
      //      sunsky.init(sunskyName, sunflow) // .2
      sunflow.light(sunskyName, "sunsky") // .3
    }
    // Camera
    if (true) {
      val eye = new Point3(-4f, 6f, 4f)
      val target = new Point3(0, 1, 0)
      val up = new Vector3(0, 0f, 1f)
      //    sunflow.parameter("eye", new Point3(-4f, 6f, 4f))
      //    sunflow.parameter("target", new Point3(0, 1, 0))
      //    sunflow.parameter("up", new Vector3(0, 0f, 1f))
      sunflow.parameter("fov", 50f)
      sunflow.parameter("aspect", 1.0f)
      sunflow.parameter("transform", Matrix4.lookAt(eye, target, up))

      val camName = sunflow.getUniqueName("camera")
      sunflow.camera(camName, new PinholeLens)

      sunflow.parameter("camera", camName);
      sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
    }
    // Objects
    sunflow.parse("src/test/resources/sunflow/Cube.geo.sc")
    sunflow.parameter("shaders", "default")
    sunflow.instance("box", "Cube")

    sunflow.parse("src/test/resources/sunflow/Plane.geo.sc")
    sunflow.parameter("shaders", "default")
    sunflow.instance("floor", "Plane")

    val display = new FileDisplay("render.png")
    val opt = SunflowAPI.DEFAULT_OPTIONS
    sunflow.render(opt, display)

  }

}