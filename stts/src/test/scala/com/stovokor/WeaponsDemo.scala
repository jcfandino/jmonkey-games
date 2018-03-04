package com.stovokor
import com.jme3.app.SimpleApplication
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Sphere
import com.jme3.material.Material
import com.jme3.light.DirectionalLight
import com.jme3.math.Vector3f
import com.jme3.bullet.BulletAppState
import com.jme3.math.ColorRGBA
import com.jme3.light.AmbientLight
import com.jme3.asset.plugins.HttpZipLocator
import com.jme3.asset.plugins.ZipLocator
import com.jme3.scene.plugins.ogre.OgreMeshKey
import com.jme3.material.MaterialList
import com.jme3.bullet.objects.PhysicsCharacter
import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.scene.Node
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.scene.shape.Box
import com.stovokor.gen.WeaponGenerator
import com.stovokor.domain.CharacterBuilder
import com.stovokor.util.jme.EventHub
import com.stovokor.domain.Weapon
import com.stovokor.domain.TechSpecs
import com.stovokor.domain.WeaponControl
import com.stovokor.domain.Pistol
import com.stovokor.util.jme.PlayerShoot
import com.stovokor.state.PlayerMovementControl
import com.stovokor.util.math.Random
import com.stovokor.domain.PlayerWeaponControl
import com.stovokor.domain.WeaponStashControl
import com.stovokor.domain.PlayerStatus
import com.stovokor.domain.NullWeaponControl

object WeaponsDemo extends SimpleApplication with ActionListener {

  def main(args: Array[String]): Unit = {
    WeaponsDemo.start
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
    implicit val assetManager = this.assetManager
    //        bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState)
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
    material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

    val floorBox = new Box(140, 0.25f, 140);
    val floorGeometry = new Geometry("Floor", floorBox);
    floorGeometry.setMaterial(material);
    floorGeometry.setLocalTranslation(0, -0.25f, 0);
    floorGeometry.addControl(new RigidBodyControl(0));
    rootNode.attachChild(floorGeometry);
    getPhysicsSpace().add(floorGeometry);

    //    player.setJumpSpeed(20)
    //    player.setFallSpeed(30)
    //    player.setGravity(60)
    //
    //    player.setPhysicsLocation(new Vector3f(60, 10, -60))

    // weapon
    val playerGunNode = new Node("player-weapon")
    val gunModel = new Geometry("playergun", new Box(0.2f, 0.2f, 5))
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

    def pointingAt = (cam.getLocation, cam.getDirection)
    val wgen = new WeaponGenerator()
    val snds = wgen.generateSounds(Random(1, 1), Pistol, weaponSpec)
    val status = new PlayerStatus
    val stash = new WeaponStashControl(status, pointingAt _)
    val weapon = new Weapon("A1", playerGunNode, new NullWeaponControl, Pistol, weaponSpec, snds)
    val weaponControl = new PlayerWeaponControl(stash, pointingAt _)
    //    EventHub.subscribe(weaponControl, PlayerShoot(true))
    //    EventHub.subscribe(weaponControl, PlayerShoot(false))
    playerGunNode.addControl(weaponControl)
    //    rootNode.attachChild(gameLevel)

    //    getPhysicsSpace().addAll(gameLevel)

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

    // test enemy
    val eweapon = new WeaponGenerator().generate(1, 1, 1)
    val enemy = CharacterBuilder()
      .on(new Vector3f(-70, 5, 20))
      .collision(3, 10)
      .spatialModel(assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml"))
      .mass(1F)
      .weapon(eweapon)
      .asEnemy

    rootNode.attachChild(enemy.spatial)
    getPhysicsSpace().add(enemy.spatial)
    //for(g <- enemy.ghosts) getPhysicsSpace().add(g)
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