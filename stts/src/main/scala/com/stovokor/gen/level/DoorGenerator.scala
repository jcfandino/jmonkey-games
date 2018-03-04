package com.stovokor.gen.level
import com.stovokor.gen.level.quest._
import com.jme3.asset.AssetManager
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.Vector2f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.TangentBinormalGenerator
import com.jme3.math.Vector3f
import com.stovokor.domain.DoorControl
import com.stovokor.domain.Door
import com.jme3.material.Material
import com.stovokor.domain.NodeId
import com.jme3.scene.shape.Quad
import com.jme3.math.Quaternion
import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.collision.shapes.MeshCollisionShape
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.scene.Spatial
import com.jme3.scene.VertexBuffer
import com.stovokor.gen.level.lightmap.LightmapUVBuilder
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.scene.shape.Cylinder
import com.jme3.math.FastMath
import com.jme3.math.ColorRGBA

class DoorGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager)
    extends Generator[Set[Door]](seed, number) {

  def generate(ctx: GeneratorContext) = {
    val quest = ctx.quest.get
    val factory = new DoorFactory(ctx.ambiences.get, ctx.uvBuilder)
    val doorPairs = for (edge <- quest.edges) yield {
      val c = edge.corridor
      val cPos = new Vector3f(c.x, c.y, c.z)
      val doorAPos =
        if (edge.supportsTwoDoors) cPos.add(c.dir.mult(.3f))
        else cPos.add(c.dir.mult(c.length * .5f))
      val doorBPos = cPos.add(c.dir.mult(c.length - .3f)) // TODO doorSize =.2f
      val doorA = factory.generateDoorIfDefined(edge.doorA, doorAPos, c, edge.corridor.id)
      val doorB = factory.generateDoorIfDefined(edge.doorB, doorBPos, c, edge.corridor.id)
      List(Option(doorA), Option(doorB))
    }
    doorPairs.flatten.filter(_.isDefined).map(_.get).toSet
  }

  class DoorFactory(ambiences: Ambiences, uvBuilder: LightmapUVBuilder) {

    def generateDoorIfDefined(qd: Option[QDoor], pos: Vector3f, c: Corridor, edge: String) = qd match {
      case Some(door) => generateDoorIn(door, pos, new Vector2f(c.width, c.height), c.dir, edge)
      case None => null
    }

    def generateDoorIn(d: QDoor, o: Vector3f, s: Vector2f, n: Vector3f, edge: String) = {
      val thickness = 0.2f
      val uy = Vector3f.UNIT_Y
      val s3 = new Vector3f(s.x, s.y, thickness)

      val geom = createGeometry(s3)
      geom.setLocalRotation(new Quaternion().fromAxes(uy cross n, uy, n))
      geom.setMaterial(frontMaterial(s, d.key))

      if (d.key.isDefined) {
        println("Creating door with key " + d.key.get)
      }
      val node = new Node(NodeId.door)
      node.setLocalTranslation(o)
      node.attachChild(geom)
      GeometryBatchFactory.optimize(node)

      uvBuilder.collectGeometries(node)

      val ctrl = new DoorControl(s.y, 50f, edge, d.key)
      node.addControl(ctrl)
      node.addControl(new RigidBodyControl(0))
      new Door(node, ctrl)
    }

    def createGeometry(s: Vector3f) = {
      val m = new Mesh
      val (w, h, t) = (s.x, s.y, s.z)
      m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
        new Vector3f(0, 0, t), //  0
        new Vector3f(w, 0, t), //  1
        new Vector3f(w, h, t), //  2
        new Vector3f(0, h, t), //  3
        new Vector3f(w, 0, 0), //  4
        new Vector3f(0, 0, 0), //  5
        new Vector3f(0, h, 0), //  6
        new Vector3f(w, h, 0))) // 7

      m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
        new Vector2f(0, 0),
        new Vector2f(1, 0),
        new Vector2f(1, 1),
        new Vector2f(0, 1),
        new Vector2f(0, 0),
        new Vector2f(1, 0),
        new Vector2f(1, 1),
        new Vector2f(0, 1)))

      m.setBuffer(Type.TexCoord2, 2, BufferUtils.createFloatBuffer(
        new Vector2f(0, .5f), // 0
        new Vector2f(1, .5f), // 1
        new Vector2f(1, 1), //   2
        new Vector2f(0, 1), //   3
        new Vector2f(1, .5f), // 4
        new Vector2f(0, .5f), // 5
        new Vector2f(0, 0), //   6
        new Vector2f(1, 0))) //  7

      m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(
        3, 0, 1,
        1, 2, 3,
        0, 1, 4,
        4, 5, 0,
        7, 4, 5,
        5, 6, 7))

      m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
        Vector3f.UNIT_Z,
        Vector3f.UNIT_Z,
        Vector3f.UNIT_Z,
        Vector3f.UNIT_Z,
        Vector3f.UNIT_Z.negate,
        Vector3f.UNIT_Z.negate,
        Vector3f.UNIT_Z.negate,
        Vector3f.UNIT_Z.negate))

      m.updateBound()

      TangentBinormalGenerator.generate(m)
      new Geometry("door", m)
    }

    lazy val bottomMaterial = {
      val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
      mat.setTexture("DiffuseMap", assetManager.loadTexture("textures/metal/metal-002-wfc.jpg"))
      mat
    }

    def frontMaterial(size: Vector2f, key: Option[String]) = {
      val proportion = size.x / size.y
      val mat = ambiences.doorMaterial(proportion).clone

      if (key.isDefined) {
        KeyMaterialColorizer.colorize(key.get, mat, 1.2f)
      }
      mat
    }

  }
}