package com.stovokor

import com.jme3.app.SimpleApplication
import com.jme3.asset.plugins.FileLocator
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.light.AmbientLight
import com.jme3.light.PointLight
import com.jme3.material.Material
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Sphere
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.gen.level.QuadFactory
import com.stovokor.gen.level.lightmap.DebugLightmapBaker
import com.stovokor.gen.level.lightmap.LightmapUVBuilder
import com.jme3.light.Light

object LightMapTest extends SimpleApplication with ActionListener {

  def main(args: Array[String]): Unit = {
    setShowSettings(false)
    LightMapTest.start
  }

  val walkDirection = new Vector3f()
  var gameLevel: Node = null

  var left = false
  var right = false
  var up = false
  var down = false;

  val uvBuilder = new LightmapUVBuilder
  var baker = new DebugLightmapBaker(1, 1)

  def simpleInitApp = {
    implicit val assetManager = this.assetManager
    flyCam.setMoveSpeed(100)
    setupKeys()

    cam.setFrustumFar(2000)

    // lights
    val pl = new PointLight
    pl.setColor(ColorRGBA.Red)
    pl.setPosition(new Vector3f(-10, 0, -20))
    //    rootNode.addLight(pl)

    val lgeom = new Geometry("light", new Sphere(8, 8, .5f))
    lgeom.setMaterial(lightMat)
    lgeom.setLocalTranslation(pl.getPosition())
    rootNode.attachChild(lgeom)

    val am = new AmbientLight
    am.setColor(ColorRGBA.White)
    rootNode.addLight(am)

    val floorBox = new Box(100, 0.25f, 100)
    val floorGeometry = new Geometry("Floor", floorBox)
    floorGeometry.setMaterial(material)
    floorGeometry.setLocalTranslation(0, 0, -180f)
    floorGeometry.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0f, 0f))
    TangentBinormalGenerator.generate(floorGeometry)

    val node = new Node
    //    node.attachChild(floorGeometry)

    attachQuad(material, node, -15f, -15f, 0f, 10f, 30f, Vector3f.UNIT_Z)
    attachQuad(material, node, -5f, -15f, 0f, 10f, 10f, Vector3f.UNIT_Z)
    attachQuad(material, node, -5f, 5f, 0f, 10f, 10f, Vector3f.UNIT_Z)
    attachQuad(material, node, 5f, -15f, 0f, 10f, 30f, Vector3f.UNIT_Z)

    attachQuad(material, node, 5, -15f, 10f, 30f, 30f, Vector3f.UNIT_Z)

    uvBuilder.applyUVs
    baker.bake(node, Set[Light](pl))

    assetManager.registerLocator("cache", classOf[FileLocator])
    material.setTexture("LightMap", assetManager.loadTexture(baker.fileName));
    material.setBoolean("SeparateTexCoord", true)
    rootNode.attachChild(node);

  }
  lazy val material = {

    val mode = WrapMode.Repeat
    val bricks = assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall2.j3m");
    bricks.getTextureParam("DiffuseMap").getTextureValue().setWrap(mode);
    bricks.getTextureParam("NormalMap").getTextureValue().setWrap(mode);
    bricks.setTexture("LightMap", assetManager.loadTexture("lightmap-test.png"));
    bricks.setBoolean("SeparateTexCoord", true)
    //    bricks.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off); //TODO enable back
    bricks
  }
  lazy val lightMat = {
    val material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", assetManager.loadTexture("Debug1.png"));
    material.getTextureParam("ColorMap").getTextureValue().setWrap(WrapMode.Repeat)
    material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    material
  }
  lazy val quadFactory = {
    implicit val assetManager = this.assetManager
    new QuadFactory()
  }

  def attachQuad(material: Material, node: Node, x1: Float, y1: Float, z1: Float, a: Float, b: Float, normal: Vector3f) {
    val quad = quadFactory.attachQuad(material, node, x1, y1, z1, a, b, normal)
    for (q <- quad) uvBuilder.collectQuad(q, a, b)
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