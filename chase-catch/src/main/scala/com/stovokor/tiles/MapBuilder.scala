package com.stovokor.tiles

import com.jme3.asset.AssetManager
import com.jme3.scene.Geometry
import com.jme3.math.Vector3f
import com.jme3.scene.Mesh
import com.jme3.math.Vector2f
import com.jme3.scene.VertexBuffer.Type
import com.jme3.util.BufferUtils
import com.jme3.material.Material
import com.jme3.texture.Texture
import com.jme3.material.RenderState.BlendMode
import com.jme3.scene.Node
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.math.Quaternion
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.scene.Spatial.CullHint
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.texture.Texture.WrapMode
import com.jme3.scene.BatchNode
import com.jme3.bullet.control.RigidBodyControl
import com.stovokor.factory.MaterialFactory
import scala.collection.Map
import com.jme3.bullet.collision.shapes.MeshCollisionShape

object MapBuilder {
  def apply(assetManager: AssetManager) = new MapBuilder(assetManager)
}

class MapBuilder(val assetManager: AssetManager) {

  val tileWidth = 1f // X
  val tileHeight = 1f // y 
  val tileThickness = 0.1f // z

  type Cell = (Tile, Geometry)
  var geometries: Array[Array[Cell]] = Array()

  def init(x: Int, y: Int) = {
    geometries = new Array(x)
    for (i <- 0 to x - 1) {
      geometries(i) = new Array(y)
    }
    this
  }

  def setTile(x: Int, y: Int, tile: Tile) = {
    val mesh = createFloorMesh(tile)
    val geom = new Geometry(s"tile($x,$y)", mesh)
    geom.setLocalTranslation(x * tileWidth, y * tileHeight, tile.height * tileThickness)
    geom.setMaterial(getMaterial(tile.textureIndex))
    geometries(x)(y) = (tile, geom)
    this
  }

  def build() = {
    val nodeRoot = new Node("map-root")
    val nodeBatch = new Node("map-batch")
    addFloorMesh(nodeBatch)
    addWallsMesh(nodeBatch)
    nodeRoot.attachChild(nodeBatch)
    val newNode = GeometryBatchFactory.optimize(nodeRoot)
    newNode.addControl(new RigidBodyControl(0))
    newNode
  }

  def addWallsMesh(node: Node) {
    val transitions = geometries
      .zipWithIndex
      .map(gs => (gs._1.zipWithIndex, gs._2))
      .flatMap(t => t._1.map(t2 => (t._2, t2._2, t2._1._1)))
      .map(t => t match {
        case (x, y, tile) => {
          List() ++ {
            if (geometries.size > x + 1)
              List((tile, geometries(x + 1)(y)._1, false, x, y))
            else List()
          } ++ {
            if (geometries(x).size > y + 1)
              List((tile, geometries(x)(y + 1)._1, true, x, y))
            else
              List()
          }
        }
      })
      .flatten
      .filterNot(t => t match { case (t1, t2, _, _, _) => t1.height == t2.height })
      .map(t => t match { case (t1, t2, v, x, y) => createWallMesh(t1, t2, v, x, y) })
      .foreach(node.attachChild)
  }

  def createWallMesh(tile1: Tile, tile2: Tile, vert: Boolean, x: Int, y: Int) = {
    val height = tileThickness * (tile1.height - tile2.height).abs
    val orientation = if (vert) {
      if (tile1.height > tile2.height) Vector3f.UNIT_X else Vector3f.UNIT_X.negate
    } else {
      if (tile1.height > tile2.height) Vector3f.UNIT_Y else Vector3f.UNIT_Y.negate
    }
    val orientationPos = orientation.mult(orientation).normalize
    val width = if (orientation.x == 0) tileHeight else tileWidth
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      new Vector3f(0f, 0f, 0f),
      new Vector3f(0f, width, 0f),
      new Vector3f(0f, 0f, height),
      new Vector3f(0f, width, height)))

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(0f, 0f),
      new Vector2f(1f, 0f),
      new Vector2f(0f, height / tileHeight),
      new Vector2f(1f, height / tileHeight)))

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      orientationPos, orientationPos, orientationPos, orientationPos))
    m.updateBound()
    val geom = new Geometry("wall", m)
    geom.rotateUpTo(orientationPos)
    geom.setLocalTranslation(tileWidth * (x + orientation.y.abs),
      tileHeight * (y + orientation.x.abs),
      tileThickness * Math.min(tile1.height, tile2.height))
    val mat = getWallMaterial(tile1.wallTextureIndex)
    mat.getAdditionalRenderState.setFaceCullMode(FaceCullMode.Off) // TODO inverted normals
    geom.setMaterial(mat)
    geom
  }

  def addFloorMesh(node: Node) = {
    geometries.flatMap(a => a).foreach(x => x match { case (t, g) => node.attachChild(g) })
    node
  }

  def createFloorMesh(tile: Tile) = {
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      new Vector3f(0f, 0f, 0f),
      new Vector3f(tileWidth, 0f, 0f),
      new Vector3f(0f, tileHeight, 0f),
      new Vector3f(tileWidth, tileHeight, 0f)))

    val (xo, xe, yo, ye) = TileCoordinatesResolver.resolve(tile.textureIndex)

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(xo, yo),
      new Vector2f(xe, yo),
      new Vector2f(xo, ye),
      new Vector2f(xe, ye))) // Set up from sprite sheet

    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      Vector3f.UNIT_Z, Vector3f.UNIT_Z, Vector3f.UNIT_Z, Vector3f.UNIT_Z))
    m.updateBound()
    m
  }

  var materialCache: Map[String, Material] = Map()

  val colorsMap: Array[ColorRGBA] = Array(
    new ColorRGBA(1f, 1f, 1f, 1f), // nothing
    new ColorRGBA(.4f, .4f, .4f, 1f), // asp
    new ColorRGBA(.6f, .7f, .1f, 1f), // tiles
    new ColorRGBA(.3f, .2f, .05f, 1f), // roof
    new ColorRGBA(.9f, .9f, .9f, 1f)) // wall

  def getWallMaterial(textureIndex: Integer) = {
    val file = s"Textures/Walls/wll$textureIndex.png"
    if (materialCache.get(file).isEmpty) {
      val mat = MaterialFactory.create(assetManager, file)
      materialCache = materialCache.updated(file, mat)
    }
    materialCache(file)
  }

  def getMaterial(textureIndex: Integer) = {
    val file = TileCoordinatesResolver.sheet(textureIndex).file
    if (materialCache.get(file).isEmpty) {
      val mat = MaterialFactory.create(assetManager, file)
      materialCache = materialCache.updated(file, mat)
    }
    materialCache(file)
  }

}