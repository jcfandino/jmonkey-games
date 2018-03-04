package com.stovokor.gen.level

import java.util.UUID
import com.jme3.asset.AssetManager
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.Material
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.gen.level.quest._
import jme3tools.optimize.GeometryBatchFactory
import com.stovokor.gen.level.lightmap.LightmapUVBuilder
import com.stovokor.domain.RoomControl
import com.stovokor.domain.CorridorControl
import com.jme3.scene.Spatial.CullHint
import com.stovokor.Settings.Debug
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.material.RenderState.BlendMode
import com.jme3.scene.BatchNode

class LevelMapGenerator(implicit val assetManager: AssetManager)
    extends Generator[Node](1, 1) {

  def uuid = UUID.randomUUID.toString

  def generate(ctx: GeneratorContext) = {
    val factory = new LevelMapFactory(ctx.quest, ctx.bsp.get, ctx.ambiences.get, ctx.uvBuilder)
    factory.create
  }

  class LevelMapFactory(quest: Option[Quest], bsp: BSPNode, ambiences: Ambiences, uvBuilder: LightmapUVBuilder) {
    def create(): Node = {
      val mapNode = new Node("level")

      drawBSPTree(mapNode)

      //      mapNode.addControl(new RigidBodyControl(0))
      //    GeometryBatchFactory.optimize(mapNode)
      mapNode
    }

    def drawBSPTree(node: Node) {
      def corridors = ((for (c <- bsp.corridors) yield List((c.wall1, c), (c.wall2, c)))).flatten
      def corridorPerWall = corridors.groupBy(_._1).map { case (k, v) => (k, v(0)._2) }

      def exits() = {
        val q = quest.get
        val exitA = q.exitARoom.get
        val exitB = q.exitBRoom.get
        val exitC = q.initialRoom.get
        Map(
          (exitA.room, exitA),
          (exitB.room, exitB),
          (exitC.room, exitC))
      }
      val exitCorridors: Map[Room, Vert] = if (quest.isDefined) exits() else Map.empty
      val exitWalls = exitCorridors.map({
        case (r, v) =>
          r.walls
            .filterNot(w => corridorPerWall.contains(w))
            .filter(w => w.normal.y == 0)
            .last // FIXME It may not exist, check when adding exit in quest
      }).toList
      println(s"Exit walls: $exitWalls")

      def drawWall(n: Node, w: Wall) = {
        val mat = w.normal.y match {
          case 0f => ambiences.wallMaterial.apply(w.textureCoor)
          case 1f => ambiences.ceilingMaterial.apply(w.textureCoor)
          case -1f => ambiences.floorMaterial.apply(w.textureCoor)
        }
        attachQuad(n, w.x, w.y, w.z, w.d1, w.d2, w.normal, mat)
      }
      var nodeForWall: Map[Wall, Node] = Map()

      def joiner(n1: Node, n2: Node): Node = {
        val n = new Node(uuid)
        n.attachChild(n1)
        n.attachChild(n2)
        n
      }
      def func(x: BSPNode, n: Node): Node = x match {
        case NonLeafBSPNode(part, c1, c2, _, _, _) =>
          n // newChildOf(n)
        case LeafBSPNode(part, None, ambience) =>
          n
        case LeafBSPNode(part, Some(room), ambience) =>
          val n2 = newChildOf(n, "room" + uuid)
          appendRoomData(room, n2)
          for (w <- x.walls) yield {
            if (exitWalls.contains(w)) {
              val textures = x.rooms(0).textures
              drawExitDoor(n2, w, textures)
            } else if (!corridorPerWall.contains(w)) {
              drawWall(n2, w)
            } else {
              drawWallWithDoor(w, corridorPerWall(w), n2)
              nodeForWall = nodeForWall.updated(w, n2)
            }
          }
          n2.addControl(new RoomControl(x.asInstanceOf[LeafBSPNode]))
          n2
      }
      val n = bsp.walk(func, joiner, new Node(uuid))

      for (c <- bsp.corridors) {
        val corNode = newChildOf(n, "corridor" + uuid)
        // 1. Generate geometry
        drawCorridor(corNode, c)
        // 2. Relate to RoomControls
        val r1 = nodeForWall(c.wall1).getControl(classOf[RoomControl])
        val r2 = nodeForWall(c.wall2).getControl(classOf[RoomControl])
        val ctrl = new CorridorControl(c.id, r1, r2)
        r1.relateTo(ctrl, c.wall1)
        r2.relateTo(ctrl, c.wall2)
        corNode.addControl(ctrl)
        //        GeometryBatchFactory.optimize(corNode)
      }

      //      nodeForWall.values.toSet.foreach(GeometryBatchFactory.optimize)

      node.attachChild(n)
    }

    // Append room data
    def appendRoomData(room: Room, n2: Node) {
      n2.setName(room.id)
      n2.setUserData("room.id", room.id)
      n2.setUserData("room.x", room.x)
      n2.setUserData("room.y", room.y)
      n2.setUserData("room.z", room.z)
      n2.setUserData("room.w", room.width)
      n2.setUserData("room.h", room.height)
      n2.setUserData("room.d", room.depth)
    }

    def drawExitDoor(n2: Node, w: Wall, textures: TextureCoordinates) {
      val width = 9
      val height = 12
      // Find the center of the wall
      val (cx, cz) = (w.normal.x, w.normal.z) match {
        case (0f, m) => (w.x + -1f * m * ((w.d1 + (m * width)) * .5f), w.z)
        case (m, 0f) => (w.x, w.z + m * ((w.d1 - width) * .5f))
        case (n, m) => println(s"WAAAT?? $n $m"); (w.x, w.y)
      }
      val (coorx, coorz) = (w.normal.x, w.normal.z) match {
        case (0f, m) => (w.x + (-1f * m * w.d1 * .5f), w.z)
        case (m, 0f) => (w.x, w.z + m * ((w.d1) * .5f))
        case _ => (0f,0f)
      }
      val cc = new Vector3f(cx, w.y, cz)
      val c = new Corridor(cc.x, cc.y, cc.z, width, height, 0, w.normal, w, w, textures)
      val w2 = drawWallWithDoor(w, c, n2)

      // Create placeholder for the exit door.
      val coor = new Vector3f(coorx, w2.y, coorz)
      val n3 = newChildOf(n2, n2.getName + "-exit")
      n3.setLocalTranslation(coor)
      n3.setUserData("x", coor.x)
      n3.setUserData("y", coor.y)
      n3.setUserData("z", coor.z)
      n3.setUserData("d1", w2.d1)
      n3.setUserData("d1", w2.d2)
      n3.setUserData("nx", -w2.normal.x)
      n3.setUserData("nz", -w2.normal.z)
    }

    //    def drawCorridors(node: Node, tree: BSPNode) {
    //      for (c <- tree.corridors) {
    //        drawCorridor(node, c)
    //      }
    //    }

    def drawWallWithDoor(w: Wall, c: Corridor, node: Node): Wall = {
      //    val node = n //newChildOf(n)
      val mat = ambiences.wallMaterial.apply(w.textureCoor)
      (w.normal.x, w.normal.y, w.normal.z) match {
        case (1, 0, 0) => // X
          attachQuad(node, w.x, w.y, w.z, c.z - w.z - c.width, w.d2, w.normal, mat)
          attachQuad(node, w.x, w.y + c.height, c.z - c.width, c.width, w.d2 - c.height, w.normal, mat)
          attachQuad(node, w.x, w.y, c.z, (w.z + w.d1) - c.z, w.d2, w.normal, mat)
          new Wall(w.x, w.y, c.z - c.width, w.normal, c.width, c.height)
        case (-1, 0, 0) => // -X
          attachQuad(node, w.x, w.y, w.z, w.z - c.z, w.d2, w.normal, mat)
          attachQuad(node, w.x, c.y + c.height, c.z, c.width, w.d2 - c.height, w.normal, mat)
          attachQuad(node, w.x, w.y, c.z - c.width, w.d1 - (w.z - c.z) - c.width, w.d2, w.normal, mat)
          new Wall(w.x, w.y, c.z, w.normal, c.width, c.height)
        case (0, 0, 1) => // Z
          attachQuad(node, w.x, w.y, w.z, w.x - c.x - c.width, w.d2, w.normal, mat)
          attachQuad(node, c.x + c.width, w.y + c.height, w.z, c.width, w.d2 - c.height, w.normal, mat)
          attachQuad(node, c.x, w.y, w.z, c.x - (w.x - w.d1), w.d2, w.normal, mat)
          new Wall(c.x + c.width, w.y, c.z, w.normal, c.width, c.height)
        case (0, 0, -1) => // -Z
          attachQuad(node, w.x, w.y, w.z, c.x - w.x, w.d2, w.normal, mat)
          attachQuad(node, c.x, w.y + c.height, w.z, c.width, w.d2 - c.height, w.normal, mat)
          attachQuad(node, c.x + c.width, w.y, w.z, w.d1 - c.width - (c.x - w.x), w.d2, w.normal, mat)
          new Wall(c.x, w.y, c.z, w.normal, c.width, c.height)
        //case (0, 1, 0) => // Y
        //  attachBox(node, w.x, w.y, w.z, w.d1, 0.1f, w.d2)
        //case (0, -1, 0) => // -Y
        //  attachBox(node, w.x - w.d2, w.y - 0.1f, w.z - w.d1, w.d2, 0.1f, w.d1)
        case _ =>
          new Wall(c.x + c.width, w.y, c.z, w.normal, c.width, c.height)
      }
    }
    def drawCorridor(n: Node, c: Corridor) = {
      //    val (wall, floor, ceiling) = (c.textures.forWalls, c.textures.forFloor, c.textures.forCeiling)
      val wall = ambiences.wallMaterial.apply(c.textures.forWalls)
      val floor = ambiences.floorMaterial.apply(c.textures.forFloor)
      val ceiling = ambiences.ceilingMaterial.apply(c.textures.forCeiling)
      (c.dir.x, c.dir.y, c.dir.z) match {
        case (d, 0, 0) => // X
          attachQuad(n, c.x + c.length, c.y, c.z, c.width, c.length, Vector3f.UNIT_Y.negate, floor)
          attachQuad(n, c.x, c.y, c.z - c.width, c.length, c.height, Vector3f.UNIT_Z.negate, wall) // wall left
          attachQuad(n, c.x, c.y + c.height, c.z - c.width, c.length, c.width, Vector3f.UNIT_Y, ceiling)
          attachQuad(n, c.x + c.length, c.y, c.z, c.length, c.height, Vector3f.UNIT_Z, wall) // wall right
        case (0, d, 0) => // Y
          // TODO - Change to quads
          attachBox(n, c.x - 0.1f, c.y, c.z, 0.1f, c.length, c.height)
          attachBox(n, c.x, c.y, c.z - 0.1f, c.width, c.length, 0.1f)
          attachBox(n, c.x + c.width, c.y, c.z, 0.1f, c.length, c.height)
          attachBox(n, c.x, c.y, c.z + c.height, c.width, c.length, 0.1f)
        case (0, 0, d) => // Z
          attachQuad(n, c.x + c.width, c.y, c.z + c.length, c.length, c.width, Vector3f.UNIT_Y.negate, floor)
          attachQuad(n, c.x, c.y, c.z + c.length, c.length, c.height, Vector3f.UNIT_X.negate, wall) // left
          attachQuad(n, c.x + c.width, c.y, c.z, c.length, c.height, Vector3f.UNIT_X, wall) // right
          attachQuad(n, c.x, c.y + c.height, c.z, c.width, c.length, Vector3f.UNIT_Y, ceiling)
      }
    }

    def newChildOf(n: Node, id: String): Node = {
      val node = new Node(id)
      //      val node = new BatchNode(id)
      n.attachChild(node)
      node
    }
    val quadFactory = new QuadFactory()

    def attachQuad(node: Node, x1: Float, y1: Float, z1: Float, a: Float, b: Float, normal: Vector3f, material: Material, solid: Boolean = false) {
      val geom = quadFactory.attachQuad(material, node, x1, y1, z1, a, b, normal, solid)
      // Collect quad for later
      for (g <- geom) uvBuilder.collectQuad(g, a, b)
      if (geom.isDefined && Debug.debugOcclussion && normal.y == 0) {
        geom.get.setMaterial(debugMaterial)
        geom.get.setQueueBucket(Bucket.Transparent);
      }
    }

    def attachBox(node: Node, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float) {
      val s = 0
      val box = new Box(0.5f * width - s, 0.5f * height - s, 0.5f * depth - s);

      if (width == 0.1f)
        box.scaleTextureCoordinates(new Vector2f(depth / 10f, height / 10f));
      else if (height == 0.1f)
        box.scaleTextureCoordinates(new Vector2f(depth / 10f, width / 10f));
      else if (depth == 0.1f)
        box.scaleTextureCoordinates(new Vector2f(width / 10f, height / 10f));

      TangentBinormalGenerator.generate(box);
      val boxGeometry = new Geometry("Room", box);
      boxGeometry.setMaterial(testMaterial);
      boxGeometry.setLocalTranslation(x + 0.5f * width + s, y + 0.5f * height + s, z + 0.5f * depth + s);

      boxGeometry.addControl(new RigidBodyControl(0))
      node.attachChild(boxGeometry);
    }
    lazy val debugMaterial = {
      val mode = WrapMode.Repeat
      val bricks = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall2.j3m")
      bricks.getTextureParam("DiffuseMap").getTextureValue().setWrap(mode)
      bricks.getTextureParam("NormalMap").getTextureValue().setWrap(mode)
      bricks.setFloat("ParallaxHeight", 0.05f)
      bricks.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
      bricks.getAdditionalRenderState().setAlphaTest(true)
      bricks.getAdditionalRenderState().setAlphaFallOff(0.5f);
      bricks.getAdditionalRenderState().setWireframe(true);
      //    bricks.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off) //TODO enable front
      bricks
    }
    lazy val testMaterial = {
      val mode = WrapMode.Repeat
      val bricks = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall2.j3m")
      bricks.getTextureParam("DiffuseMap").getTextureValue().setWrap(mode)
      bricks.getTextureParam("NormalMap").getTextureValue().setWrap(mode)
      bricks.setFloat("ParallaxHeight", 0.05f)
      //    bricks.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off) //TODO enable front
      bricks
    }
  }
}