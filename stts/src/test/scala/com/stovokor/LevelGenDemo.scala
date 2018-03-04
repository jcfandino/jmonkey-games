package com.stovokor

import com.jme3.app.SimpleApplication
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.stovokor.gen.level.AmbienceGenerator
import com.stovokor.gen.level.BSPNode
import com.stovokor.gen.level.DungeonGenerator
import com.stovokor.gen.level.GeneratorContext
import com.stovokor.gen.level.LevelMapGenerator
import com.stovokor.gen.level.Part
import com.stovokor.gen.level.NonLeafBSPNode
import com.jme3.scene.shape.Quad
import com.jme3.math.Quaternion
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.Plane
import com.jme3.math.FastMath

object LevelGenDemo extends SimpleApplication with ActionListener {

  def main(args: Array[String]): Unit = {
    LevelGenDemo.start
  }

  //val bulletAppState = new BulletAppState
  val walkDirection = new Vector3f()
  //val player = new PhysicsCharacter(new SphereCollisionShape(5), .1f)
  var gameLevel: Node = null

  var left = false
  var right = false
  var up = false
  var down = false;

  def simpleInitApp = {
    implicit val assetManager = this.assetManager
    //        bulletAppState = new BulletAppState();
    //stateManager.attach(bulletAppState)
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

    // add floor
    val material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    // material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
    //
    //    val floorBox = new Box(100, 0.25f, 100);
    //    val floorGeometry = new Geometry("Floor", floorBox);
    //    floorGeometry.setMaterial(material);
    //    floorGeometry.setLocalTranslation(0, -100.25f, 0);

    val node = new Node
    //    node.attachChild(floorGeometry)

    rootNode.attachChild(node);

    val seed = System.currentTimeMillis()
    val amgen = new AmbienceGenerator(seed, 1)
    val gen = new DungeonGenerator(seed, 1)
    val ctx = new GeneratorContext
    ctx.width = 400
    ctx.height = 80
    ctx.depth = 400
    ctx.numAmbiences = 4

    val bsp = gen.generate(ctx)

    val ambiences = amgen.generate(ctx)

    ctx.ambiences = Some(ambiences)
    ctx.bsp = Some(bsp)

    val mapGen = new LevelMapGenerator
    val map = mapGen.generate(ctx)
    rootNode.attachChild(map)

    bsp.walk(n => n match {
      case NonLeafBSPNode(_, _, _, _, _, plane) => {
        val g = new Geometry("plane", new Quad(ctx.width, ctx.height))
        val q = new Quaternion()
        q.lookAt(plane.getNormal, Vector3f.ZERO)
        g.setLocalTranslation((plane.getNormal mult plane.getConstant))
        if(plane.getNormal.x !=0) g.move(0, 0, ctx.depth)
        g.setLocalRotation(q)
        val mat = material.clone()
        mat.setColor("Color", ColorRGBA.randomColor().mult(new ColorRGBA(1, 1, 1, .5f)))
        mat.getAdditionalRenderState.setFaceCullMode(FaceCullMode.Off)
        g.setMaterial(mat)
                rootNode.attachChild(g)
      }
      case _ =>
    })


    //bsp.walk(drawDungeon)
    //bsp.walk(calcVolume)
    //bsp.walk(drawRooms)
    //    drawWalls(bsp)
    //    drawCorridors(bsp)

    println(s"Expected volume ${100 * 100 * 100}, result is $volume")

  }

  var volume = 0
  def calcVolume(part: Part) {
    volume += part.width * part.height * part.depth
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