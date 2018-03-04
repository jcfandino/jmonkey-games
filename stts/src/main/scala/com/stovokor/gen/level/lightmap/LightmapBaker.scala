package com.stovokor.gen.level.lightmap

import java.awt.image.BufferedImage
import java.io.File
import java.nio.FloatBuffer
import org.sunflow.SunflowAPI
import org.sunflow.core.camera.PinholeLens
import org.sunflow.core.display.FileDisplay
import org.sunflow.core.light.DirectionalSpotlight
import org.sunflow.core.light.SunSkyLight
import org.sunflow.core.primitive.TriangleMesh
import org.sunflow.core.shader.DiffuseShader
import org.sunflow.image.Color
import org.sunflow.math.Matrix4
import org.sunflow.math.Point3
import org.sunflow.math.Vector3
import com.jme3.light.Light
import com.jme3.light.PointLight
import com.jme3.material.Material
import com.jme3.math.Vector2f
import com.jme3.scene.BatchNode
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.SceneGraphVisitor
import com.jme3.scene.Spatial
import com.jme3.scene.VertexBuffer
import com.jme3.util.BufferUtils
import com.stovokor.Settings.Debug
import com.stovokor.util.sunflow.CompatibleSunflowAPI
import com.stovokor.util.jme.JmeExtensions.SpatialExtensions
import javax.imageio.ImageIO
import com.stovokor.Settings

class LightmapBaker(seed: Long, number: Int) {

  val mapSize = Settings.Lightmap.size

  def fileName = s"lightmap-$seed-$number.png"
  def filePath = s"cache/$fileName"

  def cached = new File(filePath).exists()

  // Only for testing
  def bake(node: Node, lights: Set[Light]) {
    val builder = new LightmapGeometryBuilder
    builder.add(node)
    bake(builder.build, lights)
  }

  def bake(geometry: Geometry, lights: Set[Light], extras: Set[Spatial] = Set()) {
    if (Debug.disableLightmap) return
    // init
    val scene = new SunflowScene
    scene.init(mapSize, mapSize)

    scene.addGeometry(geometry)

    if (!extras.isEmpty) {
      val extraGeomBuilder = new LightmapGeometryBuilder
      for (extra <- extras) {
        println(s"Adding extra object ${extra.getName}")
        extraGeomBuilder.add(extra)
      }
      scene.addGeometry(extraGeomBuilder.build)
    }

    for (l <- lights) {
      if (l.isInstanceOf[PointLight]) {
        val pl = l.asInstanceOf[PointLight]
        scene.addPointLight(pl)
      }
    }

    scene.render
  }

  class GeometryAccessBatchNode extends BatchNode {

    def geometry = {
      batch()
      val batches = this.batches //this.getClass.getDeclaredField("batches")
      val theBatch = batches.get(0)
      val geom = theBatch.getClass.getDeclaredField("geometry")
      geom.setAccessible(true)
      geom.get(theBatch).asInstanceOf[Geometry]
    }
  }
  class SunflowScene {
    val sunflow = new CompatibleSunflowAPI
    val enableBake = true

    var names: Map[Geometry, String] = Map.empty

    def init(width: Int, height: Int) {
      //       Bake
      if (enableBake) {
        sunflow.parameter("baking.instance", "geometry_0.instance")
        sunflow.parameter("baking.viewdep", false)
        sunflow.options(SunflowAPI.DEFAULT_OPTIONS)
      }
      // Image settings
      sunflow.parameter("resolutionX", width);
      sunflow.parameter("resolutionY", height);

      sunflow.parameter("aa.min", 0)
      sunflow.parameter("aa.max", 2)

      sunflow.parameter("samples", 2)
      sunflow.parameter("contrast", 0.1f)
      sunflow.parameter("filter", "gaussian")
      sunflow.parameter("jitter", false)
      sunflow.parameter("caustics", "none")

      sunflow.options(SunflowAPI.DEFAULT_OPTIONS)

      // Shader diffuse
      sunflow.parameter("diffuse", new Color(1f, 1f, 1f))
      sunflow.shader("default", new DiffuseShader)

      // Shader ambient occlusion
      sunflow.parameter("bright", new Color(1f))
      sunflow.parameter("dark", new Color(0f))
      sunflow.parameter("samples", 8)
      sunflow.parameter("maxdist", 10f)
      sunflow.shader("ambocc", "ambient_occlusion");

      // Sunsky
      if (false) {
        sunflow.parameter("up", new Vector3(0, 1, 0))
        sunflow.parameter("east", new Vector3(0, 0, 1))
        sunflow.parameter("sundir", new Vector3(1, 1, 1))
        sunflow.parameter("turbidity", 4f)
        sunflow.parameter("samples", 64)
        val sunsky = new SunSkyLight
        val sunskyName = sunflow.getUniqueName("sunsky")
        sunflow.light(sunskyName, "sunsky") // .
      }

      // Camera
      val eye = new Point3(362f, 100f, 26f)
      val target = new Point3(361f, 96f, 27f)
      val up = new Vector3(0, 1f, 0f)
      //    sunflow.parameter("eye", new Point3(-4f, 6f, 4f))
      //    sunflow.parameter("target", new Point3(0, 1, 0))
      //    sunflow.parameter("up", new Vector3(0, 0f, 1f))
      sunflow.parameter("fov", 500f)
      sunflow.parameter("aspect", width.toFloat / height)
      sunflow.parameter("transform", Matrix4.lookAt(eye, target, up))

      val camName = sunflow.getUniqueName("camera")
      sunflow.camera(camName, new PinholeLens)

      sunflow.parameter("camera", camName);
      sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
    }

    def render {
      val d = new File("cache")
      if (!d.exists()) d.mkdir()
      val display = new FileDisplay(filePath)
      val opt = SunflowAPI.DEFAULT_OPTIONS
      sunflow.render(opt, display)
    }

    def addGeometry(geometry: Geometry) {
      val name = "geometry_" + names.size
      val triangles = new Array[Int](geometry.getMesh().getIndexBuffer().size())

      for (i <- 0 to triangles.length - 1) {
        triangles(i) = geometry.getMesh().getIndexBuffer().get(i)
      }

      val points =
        BufferUtils.getFloatArray(geometry.getMesh().getFloatBuffer(
          VertexBuffer.Type.Position))
      val normals =
        BufferUtils.getFloatArray(geometry.getMesh().getFloatBuffer(
          VertexBuffer.Type.Normal))
      val uvs =
        BufferUtils.getFloatArray(geometry.getMesh().getFloatBuffer(
          VertexBuffer.Type.TexCoord2))

      val matrix = new Array[Float](16)

      geometry.getWorldMatrix().get(matrix, true)
      val transform = new Matrix4(matrix, true)

      sunflow.parameter("shader", "default")
      sunflow.parameter("triangles", triangles)
      sunflow.parameter("points", "point", "vertex", points)
      sunflow.parameter("normals", "vector", "vertex", normals)
      sunflow.parameter("uvs", "texcoord", "vertex", uvs)
      sunflow.geometry(name, new TriangleMesh())

      sunflow.parameter("shaders", "default")
      sunflow.parameter("transform", transform)
      sunflow.instance(name + ".instance", name)

      names = names.updated(geometry, name)
    }

    def addPointLight(pl: PointLight) {
      val plpos = pl.getPosition()
      val pos = new Point3(plpos.x, plpos.y, plpos.z)
      val plcol = pl.getColor()
      val color = new Color(10000f * plcol.r, 10000f * plcol.g, 10000f * plcol.b)

      {
        sunflow.parameter("center", pos)
        sunflow.parameter("power", color)
        val lightName = sunflow.getUniqueName("pointLight")
        sunflow.light(lightName, "point")
      }
      //       Dirlight
      if (false) {
        sunflow.parameter("source", pos)
        sunflow.parameter("dir", new Vector3(0f, -1f, 0f));
        sunflow.parameter("radius", 1000f)
        sunflow.parameter("radiance", color)
        val lightName = sunflow.getUniqueName("dirlight")
        sunflow.light(lightName, new DirectionalSpotlight)
      }
    }

  }
}

class DebugLightmapBaker(seed: Long, number: Int) extends LightmapBaker(seed, number) {

  val width = 8000
  val height = 8000
  val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  // TODO broken, fix if needed, or remove.
  def bake2(node: Node, lights: Set[Light], extras: Set[Spatial]) {

    // draw objects
    node.breadthFirst(s => {
      if (s.isInstanceOf[Geometry])
        drawGeometry(s.asInstanceOf[Geometry])
    })

    ImageIO.write(img, "png", new File(filePath))
  }

  def drawGeometry(geom: Geometry) {
    val buf = geom.getMesh().getBuffer(VertexBuffer.Type.TexCoord2).getData

    val (v1, v2, v3, v4) = (new Vector2f, new Vector2f, new Vector2f, new Vector2f)

    BufferUtils.populateFromBuffer(v1, buf.asInstanceOf[FloatBuffer], 0)
    BufferUtils.populateFromBuffer(v2, buf.asInstanceOf[FloatBuffer], 1)
    BufferUtils.populateFromBuffer(v3, buf.asInstanceOf[FloatBuffer], 2)
    BufferUtils.populateFromBuffer(v4, buf.asInstanceOf[FloatBuffer], 3)

    val minx = (v1.x * width).toInt
    val maxx = (v4.x * width).toInt - 1
    val miny = (v1.y * height).toInt
    val maxy = (v4.y * height).toInt - 1

    println(s"Drawing square ($minx,$miny)-($maxx,$maxy)")
    for (i <- minx to maxx) {
      draw(i, miny, Color.RED)
      draw(i, miny + 1, Color.RED)
      draw(i, maxy, Color.RED)
      draw(i, maxy - 2, Color.RED)
      draw(i, miny + (((i - minx).toFloat / (maxx - minx)) * (maxy - miny)).toInt, Color.RED)
      draw(i, maxy - (((i - minx).toFloat / (maxx - minx)) * (maxy - miny)).toInt, Color.RED)
    }
    for (i <- miny to maxy) {
      draw(minx, i, Color.RED)
      draw(minx + 1, i, Color.RED)
      draw(maxx, i, Color.RED)
      draw(maxx - 1, i, Color.RED)
      draw(minx + (((i - miny).toFloat / (maxy - miny)) * (maxx - minx)).toInt, i, Color.RED)
      draw(maxx - (((i - miny).toFloat / (maxy - miny)) * (maxx - minx)).toInt, i, Color.RED)
    }
  }

  def draw(x: Int, y: Int, c: Color) {
    img.setRGB(x, img.getHeight() - y - 1, c.toRGB())
  }
}