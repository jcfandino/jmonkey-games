package com.stovokor

import com.stovokor.domain.NullWeaponControl
import com.stovokor.gen.EnemyClassGenerator
import com.jme3.animation.AnimControl
import com.stovokor.domain.enemy.TacticBehaviors
import com.stovokor.ai.NavigationFactory
import com.jme3.light.DirectionalLight
import com.jme3.input.controls.KeyTrigger
import com.jme3.material.Material
import com.stovokor.domain.Pistol
import com.stovokor.domain.enemy.StrafeAround
import com.jme3.bullet.BulletAppState
import com.jme3.scene.debug.SkeletonDebugger
import com.stovokor.gen.WeaponGenerator
import com.stovokor.domain.CharacterBuilder
import com.stovokor.domain.enemy.EnemyTactic
import com.jme3.light.AmbientLight
import com.stovokor.domain.EnemyWeaponControl
import com.stovokor.gen.level.GeneratorContext
import com.stovokor.state.PlayerMovementControl
import com.stovokor.domain.TechSpecs
import com.jme3.input.KeyInput
import com.stovokor.domain.enemy.DirectApproach
import com.stovokor.domain.Weapon
import com.stovokor.domain.enemy.TacticParameters
import com.stovokor.domain.enemy.StandStill
import com.jme3.app.SimpleApplication
import com.jme3.math.Vector3f
import com.jme3.scene.shape.Box
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.input.controls.ActionListener
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.ColorRGBA
import com.jme3.animation.LoopMode
import com.stovokor.util.math.Random
import java.util.concurrent.Executors
import com.stovokor.util.math.Dist
import com.jme3.scene.shape.Line
import com.jme3.scene.debug.Arrow
import java.util.concurrent.Callable

/**
 * @author xuan
 */
object MultithreadingPhysicsTest extends SimpleApplication with ActionListener {

  def main(args: Array[String]): Unit = {
    MultithreadingPhysicsTest.start
  }

  val bulletAppState = new BulletAppState
  val walkDirection = new Vector3f()
  //val player = new PhysicsCharacter(new SphereCollisionShape(5), .1f)
  var gameLevel: Node = null

  var left = false
  var right = false
  var up = false
  var down = false;

  val threads = 10
  val executor = Executors.newFixedThreadPool(threads)
  val rnd = Random()

  def simpleInitApp = {
    //        bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState)
    flyCam.setMoveSpeed(100)
    setupKeys()

    cam.setFrustumFar(2000)

    implicit val assetManager = this.assetManager
    // add floor
    val material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

    val floorBox = new Box(280, 0.25f, 280);
    val floorGeometry = new Geometry("Floor", floorBox);
    floorGeometry.setMaterial(material);
    floorGeometry.setLocalTranslation(-140, -0.25f, -140);
    floorGeometry.addControl(new RigidBodyControl(0));
    val level = new Node("level")
    level.attachChild(floorGeometry)
    rootNode.attachChild(level);
    getPhysicsSpace().add(floorGeometry);

    for (i <- 1 to 20) {
      val box = new Box(5, 5, 5)
      val geom = new Geometry("box", box)
      val pos = new Vector3f(Dist.normalFloat(rnd, -135, 135), 5, Dist.normalFloat(rnd, -135, 135))
      geom.setLocalTranslation(pos)
      geom.setMaterial(material)
      geom.addControl(new RigidBodyControl(0))
      rootNode.attachChild(geom)
      getPhysicsSpace.add(geom)
      println(s"box at $pos")
    }
    // lights
    val dl = new DirectionalLight
    dl.setColor(ColorRGBA.White.clone().multLocal(2))

    dl.setDirection(new Vector3f(-1, -1, -1).normalize())
    rootNode.addLight(dl)

    val am = new AmbientLight
    am.setColor(ColorRGBA.White.mult(2))
    rootNode.addLight(am)
    //    rootNode.attachChild(robot)

    val rayOrigin = new Vector3f(0, 100, 0)
    val lmat = {
      val m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
      m.getAdditionalRenderState().setWireframe(true)
      m.setColor("Color", ColorRGBA.Cyan)
      m
    }

    for (i <- 1 to threads) {
      executor.submit(new Runnable {
        def run {
          while (true) {
            val rayEnd = new Vector3f(Dist.normalFloat(rnd, -140, 140), 1, Dist.normalFloat(rnd, -140, 140))
            val rayTest = getPhysicsSpace().rayTest(rayOrigin, rayEnd)
            if (!rayTest.isEmpty()) {
              println("hit detected")
              val line = new Arrow(rayEnd subtract rayOrigin)
              line.setLineWidth(3)
              val geom = new Geometry("arrow", line)
              geom.setLocalTranslation(rayOrigin)
              geom.setMaterial(lmat)
              enqueue(new Callable[Unit] {
                def call = {
                  rootNode.attachChild(geom)
                }
              })
            }
            Thread.sleep(10)
          }
        }
      })
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

  def getPhysicsSpace() = bulletAppState.getPhysicsSpace

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