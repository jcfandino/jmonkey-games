package com.stovokor

import scala.util.Random

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.Node
import com.jme3.scene.VertexBuffer.Type
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.BufferUtils
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.gen.level.QuadFactory
import com.stovokor.util.debug.DebugArrow

object TextureDemo extends SimpleApplication with ActionListener {

  def main(args: Array[String]): Unit = {
    setShowSettings(false)
    TextureDemo.start
  }

  val walkDirection = new Vector3f()
  var gameLevel: Node = null

  var left = false
  var right = false
  var up = false
  var down = false;

  def simpleInitApp = {
    implicit val assetManager = this.assetManager
    flyCam.setMoveSpeed(100)
    setupKeys()

    cam.setFrustumFar(2000)

    // lights
    val dl = new DirectionalLight
    dl.setColor(ColorRGBA.White.clone().multLocal(2))
    dl.setDirection(new Vector3f(-1, -1, -1).normalize())
    rootNode.addLight(dl)

    val am = new AmbientLight
    am.setColor(ColorRGBA.White.mult(2))
    rootNode.addLight(am)

    val floorBox = new Box(100, 0.25f, 100);
    val floorGeometry = new Geometry("Floor", floorBox);
    floorGeometry.setMaterial(material);
    floorGeometry.setLocalTranslation(0, -100.25f, 0);

    val node = new Node
    //    node.attachChild(floorGeometry)

    var x = 0
    for (i <- 0 to 10) {
      val w = 10 + i
      val y = 4 * i
      attachQuad(material, node, x, -50, y, w, w, Vector3f.UNIT_Y.negate)
      x += w
    }

    for (i <- 0 to 20) {
      val r = new Random
      val x = r.nextInt(20)
      val y = r.nextInt(20)
      val w = 5 + r.nextInt(10)
      val h = 5 + r.nextInt(10)
      attachQuad(material, node, x, -100, y, w, h, Vector3f.UNIT_Y)
    }
    for (i <- 0 to 20) {
      val r = new Random
      val x = r.nextInt(20)
      val y = r.nextInt(20)
      val w = 5 + r.nextInt(10)
      val h = 5 + r.nextInt(10)
      attachQuad(material, node, 100, y, x, w, h, Vector3f.UNIT_X)
    }
    rootNode.attachChild(node);

  }
  lazy val material = {
    val material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", assetManager.loadTexture("Debug1.png"));
    material.getTextureParam("ColorMap").getTextureValue().setWrap(WrapMode.Repeat)
    material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    material

    val mode = WrapMode.Repeat
    val bricks = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall2.j3m");
    bricks.getTextureParam("DiffuseMap").getTextureValue().setWrap(mode);
    bricks.getTextureParam("NormalMap").getTextureValue().setWrap(mode);
    bricks.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off); //TODO enable back
    bricks
  }

  lazy val quadFactory = {
    implicit val assetManager = this.assetManager
    new QuadFactory()
  }

  def attachQuad(material: Material, node: Node, x1: Float, y1: Float, z1: Float, a: Float, b: Float, normal: Vector3f) {
    quadFactory.attachQuad(material, node, x1, y1, z1, a, b, normal)
  }
  def attachQuadOld(material: Material, node: Node, x1: Float, y1: Float, z1: Float, a: Float, b: Float, normal: Vector3f) {
    val (n1, n2) = getComplementaryNormals(normal)
    val o = new Vector3f(x1, y1, z1)
    val va = o.add(n1.mult(a))
    val vb = o
    val vc = o.add(n1.mult(a).add(n2.mult(b)))
    val vd = o.add(n2.mult(b))

    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      va, vb, vc, vd))

    val (tx, ty) = (10f, 10f)
    val (uo, vo) = (n1.mult(o).dot(Vector3f.UNIT_XYZ), n2.mult(o).dot(Vector3f.UNIT_XYZ))
    val (ox, oy) = (((uo % tx) / tx), (vo % ty) / ty)
    val (ex, ey) = (ox + a / tx, oy + b / ty)
    println(s"Origin: $o N: $normal  UV: ($uo,$vo) - Dimension ($a, $b)")
    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(ex, oy),
      new Vector2f(ox, oy),
      new Vector2f(ex, ey),
      new Vector2f(ox, ey)))
    println(s"Texture O($ox,$oy) E($ex,$ey)")
    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    val invNormal = normal.negate
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(invNormal, invNormal, invNormal, invNormal))
    m.updateBound()
    TangentBinormalGenerator.generate(m)
    val geom = new Geometry("Wall", m)
    geom.setMaterial(material)
    geom.addControl(new RigidBodyControl(0))

    node.attachChild(geom)
    // Debug
    new DebugArrow(assetManager, (vb add vc) mult 0.5f, normal).draw(node)
  }
  def getComplementaryNormals(n: Vector3f) = {
    (n.x, n.y, n.z) match {
      case (1, 0, 0) => (Vector3f.UNIT_Z, Vector3f.UNIT_Y)
      case (0, 1, 0) => (Vector3f.UNIT_X, Vector3f.UNIT_Z)
      case (0, 0, 1) => (Vector3f.UNIT_X, Vector3f.UNIT_Y)
      case (-1, 0, 0) => (Vector3f.UNIT_Z.negate, Vector3f.UNIT_Y.negate)
      case (0, -1, 0) => (Vector3f.UNIT_X.negate, Vector3f.UNIT_Z.negate)
      case (0, 0, -1) => (Vector3f.UNIT_X.negate, Vector3f.UNIT_Y.negate)
    }
  }

  def setupKeys() = {
    inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(this, "Lefts");
    inputManager.addListener(this, "Rights");
    inputManager.addListener(this, "Ups");
    inputManager.addListener(this, "Downs");
    inputManager.addListener(this, "Space");
  }

  //def getPhysicsSpace() = bulletAppState.getPhysicsSpace

  def onAction(binding: String, value: Boolean, tpf: Float) = {
    if (binding.equals("Lefts")) {
      if (value)
        left = true;
      else
        left = false;
    } else if (binding.equals("Rights")) {
      if (value)
        right = true;
      else
        right = false;
    } else if (binding.equals("Ups")) {
      if (value)
        up = true;
      else
        up = false;
    } else if (binding.equals("Downs")) {
      if (value)
        down = true;
      else
        down = false;
    } else if (binding.equals("Space")) {
      //player.jump();
      //println(player.getPhysicsLocation())
    }
  }

  override def simpleUpdate(tpf: Float) = {
    val camDir = cam.getDirection().clone().multLocal(0.6f);
    val camLeft = cam.getLeft().clone().multLocal(0.4f);
    walkDirection.set(0, 0, 0);
    if (left)
      walkDirection.addLocal(camLeft);
    if (right)
      walkDirection.addLocal(camLeft.negate());
    if (up)
      walkDirection.addLocal(camDir);
    if (down)
      walkDirection.addLocal(camDir.negate());
    //player.setWalkDirection(walkDirection);
    //cam.setLocation(player.getPhysicsLocation());
  }
}