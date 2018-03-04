package com.stovokor

import com.jme3.animation.AnimControl
import com.jme3.animation.LoopMode
import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.control.RigidBodyControl
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
import com.jme3.scene.debug.SkeletonDebugger
import com.jme3.scene.shape.Box
import com.stovokor.ai.NavigationFactory
import com.stovokor.domain.CharacterBuilder
import com.stovokor.domain.Pistol
import com.stovokor.domain.TechSpecs
import com.stovokor.domain.Weapon
import com.stovokor.domain.WeaponControl
import com.stovokor.domain.enemy.DirectApproach
import com.stovokor.domain.enemy.EnemyTactic
import com.stovokor.domain.enemy.StandStill
import com.stovokor.domain.enemy.StrafeAround
import com.stovokor.domain.enemy.TacticBehaviors
import com.stovokor.domain.enemy.TacticParameters
import com.stovokor.gen.EnemyClassGenerator
import com.stovokor.gen.WeaponGenerator
import com.stovokor.gen.level.GeneratorContext
import com.stovokor.state.PlayerMovementControl
import com.stovokor.util.math.Random
import com.stovokor.domain.EnemyWeaponControl
import com.stovokor.domain.NullWeaponControl

object EnemyDemo extends SimpleApplication with ActionListener {

  def main(args: Array[String]): Unit = {
    EnemyDemo.start
  }

  val bulletAppState = new BulletAppState
  val walkDirection = new Vector3f()
  //val player = new PhysicsCharacter(new SphereCollisionShape(5), .1f)
  var gameLevel: Node = null

  var left = false
  var right = false
  var up = false
  var down = false;

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

    val floorBox = new Box(140, 0.25f, 140);
    val floorGeometry = new Geometry("Floor", floorBox);
    floorGeometry.setMaterial(material);
    floorGeometry.setLocalTranslation(0, -0.25f, 0);
    floorGeometry.addControl(new RigidBodyControl(0));
    val level = new Node("level")
    level.attachChild(floorGeometry)
    rootNode.attachChild(level);
    getPhysicsSpace().add(floorGeometry);

    // navmesh

    val nav = new NavigationFactory().create(level, Set())
    val navGeom = new Geometry("NavMesh");
    navGeom.setMesh(nav.mesh);
    val red = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    red.setColor("Color", ColorRGBA.Green);
    red.getAdditionalRenderState().setWireframe(true);
    navGeom.setMaterial(red);

    rootNode.attachChild(navGeom);
    // weapon
    val playerGunNode = new Node("player-weapon")
    val gunModel = new Geometry("playergun", new Box(0.2f, 0.2f, 0.2f))
    gunModel.setMaterial(material)
    playerGunNode.attachChild(gunModel)
    playerGunNode.setLocalTranslation(new Vector3f(-2, 0, 5))

    val weaponSpec = new TechSpecs(
      500, // milliseconds between shots
      t => if (t > 2000) 5 else t * 0.0025F, // precision: f(triggerTime)
      100, // max damage distance
      20, // damage per shot
      d => if (d > 100) 15 else d * 0.15F, // f(distance)
      1, // for shotguns
      1, // when multicannon or energy based)
      false)

    val wgen = new WeaponGenerator()
    val snds = wgen.generateSounds(Random(1, 1), Pistol, weaponSpec)

    val weapon = new Weapon("A1", playerGunNode, new NullWeaponControl, Pistol, weaponSpec, snds)
    val weaponControl = new EnemyWeaponControl(weapon, () => (cam.getLocation, cam.getDirection))
    //    EventHub.subscribe(weaponControl, PlayerShoot(true))
    //    EventHub.subscribe(weaponControl, PlayerShoot(false))
    //    playerGunNode.addControl(weaponControl)
    //    rootNode.attachChild(gameLevel)

    //    getPhysicsSpace().addAll(gameLevel)
    val behaviors = new TacticBehaviors(
      new StandStill, new DirectApproach, new StrafeAround)
    val parameters = new TacticParameters(20)
    val tactic = new EnemyTactic(behaviors, parameters)

    // player
    val player = CharacterBuilder()
      .on(new Vector3f(60, 10, -60))
      .collision(2, 6)
      .mass(1000F)
      .weapon(weapon)
      .withMovementControl(PlayerMovementControl(cam, getInputManager))
      .asPlayer

    getPhysicsSpace().add(player.physics)
    rootNode.attachChild(player.spatial)

    //for(g <- enemy.ghosts) getPhysicsSpace().add(g)

    val ctx = new GeneratorContext
    val ec = new EnemyClassGenerator(1, 1).generate(ctx).head
    val enemy = ec.create(new Vector3f(-40, 5, 20),0, nav)

    val robot = assetManager.loadModel("Models/Character/robot-base/robot-base.j3o")
    val anim = robot
      .asInstanceOf[Node].getChild("skeleton")
      .getControl(classOf[AnimControl])
    anim.createChannel()

    val channel = anim.getChannel(0)

    channel.setAnim("Run")
    channel.setLoopMode(LoopMode.Loop)

    val skeletonDebug = new SkeletonDebugger("skeleton", anim.getSkeleton())
    val smat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    smat.setColor("Color", ColorRGBA.Green)
    smat.getAdditionalRenderState().setDepthTest(false)
    skeletonDebug.setMaterial(smat)

    //    rootNode.attachChild(skeletonDebug);

    //     test enemy
    val eweapon = new WeaponGenerator().generate(1, 1, 1)
    val enemy2 = CharacterBuilder()
      .on(new Vector3f(-70, 5, 20))
      .collision(3, 10)
      .spatialModel(robot)
      .mass(1F)
      .weapon(eweapon)
      .navigation(nav)
      .tactic(tactic)
      .asEnemy

    for (e <- Set(enemy, enemy2)) {
      rootNode.attachChild(e.spatial)
      getPhysicsSpace().add(e.spatial)
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