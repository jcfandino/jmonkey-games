package com.stovokor.state

import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.iterableAsScalaIterable
import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.audio.Environment
import com.jme3.bullet.BulletAppState
import com.jme3.input.InputManager
import com.jme3.input.controls.ActionListener
import com.jme3.light.Light
import com.jme3.math.Vector3f
import com.jme3.scene.Spatial
import com.stovokor.domain.CharacterBuilder
import com.stovokor.domain.DoorControl
import com.stovokor.domain.GameStatus
import com.stovokor.domain.NodeId
import com.stovokor.domain.PlayerCharacter
import com.stovokor.domain.PlayerStatus
import com.stovokor.domain.SwitchControl
import com.stovokor.domain.SwitchId
import com.stovokor.domain.Weapon
import com.stovokor.domain.WeaponControl
import com.stovokor.gen.LevelGenerator
import com.stovokor.gen.WeaponGenerator
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.HitChecker
import com.stovokor.util.jme.LevelChange
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.PickUpAccessCard
import com.stovokor.util.jme.PlayerDied
import com.stovokor.util.jme.PlayerInteracts
import com.stovokor.util.jme.SwitchPushed
import com.stovokor.util.jme.JmeExtensions.SpatialExtensions
import jme3tools.optimize.GeometryBatchFactory
import com.stovokor.Settings.Debug
import com.jme3.light.PointLight
import com.jme3.post.ssao.SSAOFilter
import com.jme3.post.FilterPostProcessor
import com.jme3.scene.control.LightControl
import com.jme3.light.SpotLight
import com.jme3.math.FastMath
import com.jme3.math.ColorRGBA
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Sphere
import com.jme3.material.Material
import com.stovokor.domain.item.ArmorControl
import com.stovokor.domain.RoomControl
import com.stovokor.gen.level.BSPNode
import com.jme3.scene.Node
import com.stovokor.gen.level.NonLeafBSPNode
import com.jme3.math.Plane.Side
import com.stovokor.gen.level.NonLeafBSPNode
import com.stovokor.gen.level.LeafBSPNode
import com.stovokor.util.jme.DoorStateChange
import com.stovokor.util.jme.EnemyDied
import com.jme3.input.KeyInput
import com.jme3.input.controls.KeyTrigger
import com.stovokor.util.jme.JmeExtensions._
import com.stovokor.util.jme.ItemDropped
import com.stovokor.util.jme.ItemDropped
import com.stovokor.util.jme.LoudSound
import com.jme3.post.filters.BloomFilter
import com.stovokor.Settings
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.util.jme.PlayerShoot
import com.jme3.post.SceneProcessor

class InGameState(val status: GameStatus) extends AbstractAppState with ActionListener with LogicEventListener {

  var app: SimpleApplication = null
  var stateManager: AppStateManager = null
  //  var levelLights = List[Light]()
  var player: PlayerCharacter = null
  def space = stateManager.getState(classOf[BulletAppState]).getPhysicsSpace
  def flyCam = app.getFlyByCamera
  def cam = app.getCamera
  def rootNode = app.getRootNode
  implicit def assetManager = app.getAssetManager

  var sceneManager: SceneManager = null

  override def initialize(appStateManager: AppStateManager, simpleApp: Application) {
    super.initialize(appStateManager, simpleApp)

    app = simpleApp.asInstanceOf[SimpleApplication]
    stateManager = appStateManager

    flyCam.setZoomSpeed(0f)
    setupKeys(app.getInputManager())

    //    app.getInputManager().removeListener(flyCam)

    cam.setFrustumFar(400)
    /////

    //Debug
    stateManager.getState(classOf[BulletAppState]).setDebugEnabled(Debug.physics)

    if (status.newGame) {
      val initialWeapon = new WeaponGenerator().generateInitialWeapon()
      var playerStatus = new PlayerStatus(100, List[Weapon](initialWeapon), 100)
      status.playerStatus = playerStatus
    }

    val level = new LevelGenerator().generate(status.seed, status.levelNumber)
    val levelStatus = status.getLevelStatus(level)

    val currentWeapon = status.playerStatus.weapons.head
    val currentHealth = status.playerStatus.health
    val currentArmor = status.playerStatus.armor

    player = CharacterBuilder()
      .on(level.startPoint)
      .collision(2, 6)
      .mass(200F)
      .weapon(currentWeapon)
      .health(currentHealth)
      .armor(currentArmor)
      .playerStatus(status.playerStatus)
      .withMovementControl(PlayerMovementControl(cam, app.getInputManager))
      .asPlayer

    space.setGravity(Vector3f.UNIT_Y mult -30f)
    if (status.levelNumber == 1) {
      //      addInitialWeapons(player)
    }

    sceneManager = new SceneManager(rootNode, level.bsp)
    sceneManager.setPhysics(space)
    sceneManager.setLevel(levelStatus.level.model)
    stateManager.attach(sceneManager)
    stateManager.attach(ParticleEmitterAppState)
    //    rootNode.attachChild(level.model)
    //    space.add(level.model)

    if (Debug.navMesh) {
      val navGeom = level.nav.debugMesh(assetManager)
      rootNode.attachChild(navGeom)
    }

    // Test lights
    val lamps = level.lights.filter(l => l.isInstanceOf[PointLight])
    //    val lampMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    //    lampMat.setColor("Color", ColorRGBA.White);

    for (l <- lamps) {
      //      val g = new Geometry("lamp", new Sphere(6, 6, 1f))
      //      g.setMaterial(lampMat)
      //      g.setLocalTranslation(l.asInstanceOf[PointLight].getPosition())
      val asset = "Models/roof-light-01/roof-light-01.j3o"
      val model = assetManager.loadModel(asset)
      model.setMaterial(lightMaterial)
      TangentBinormalGenerator.generate(model)
      model.setLocalTranslation(l.asInstanceOf[PointLight].getPosition())
      model.move(0, 3, 0)
      model.rotate(FastMath.PI, 0, 0)
      sceneManager.attach(model)
      println(s"Setting up light in ${model.getLocalTranslation}")
    }

    val lights = level.lights.filter(l => !l.isInstanceOf[PointLight])
    for (l <- lights) {
      rootNode.addLight(l)
      //      sceneManager.attach(l)
      //      levelLights = l :: levelLights
    }
    //        app.getViewPort().addProcessor(level.fog)
    val fpp = new FilterPostProcessor(assetManager);

    if (Settings.Graphics.ssao) {
      val ssaoFilter = new SSAOFilter(8, 1.2f, 0.2f, 0.1f);
      fpp.addFilter(ssaoFilter);
    }
    if (Settings.Graphics.bloom) {
      val bloom = new BloomFilter(BloomFilter.GlowMode.Objects)
      fpp.addFilter(bloom)
    }

    app.getViewPort.addProcessor(fpp);

    for (e <- levelStatus.level.entities) {
      e.subscribeToEvents(this)
      sceneManager.attach(e)
    }
    // TODO Ugly fix for batching columns
    for (c <- levelStatus.level.columns) {
      sceneManager.attach(c)
      sceneManager.attachAndBatch(c.spatial)
    }

    rootNode.attachChild(player.spatial)
    space.add(player.physics)
    player.ghosts.foreach(space.add)
    player.physics.setGravity(Vector3f.UNIT_Y mult -100)

    //    sceneManager.optimizeScene
    sceneManager.updatePlayerLocation(player.spatial.getWorldTranslation)
    sceneManager.recalculateOcclusion

    space.addCollisionListener(new PickupItemDetector)
    space.addCollisionListener(new PropCollisionDetector)

    EventHub.subscribe(this, PlayerInteracts())
    EventHub.subscribe(this, PlayerDied())
    EventHub.subscribeByType(this, classOf[PickUpAccessCard])
    EventHub.subscribeByType(this, classOf[LevelChange])

  }

  // TODO Refactor light creation
  lazy val lightMaterial = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/roof-light-01/roof-light-01-diffuse.png"))
    material.setFloat("Shininess", 120)
    material.setTexture("GlowMap", assetManager.loadTexture("Models/roof-light-01/roof-light-01-glow.png"));
    material
  }

  def updatePlayerStatus {
    status.playerStatus.health = player.control.getHealth
    status.playerStatus.weapons = player.control.weaponStash.weapons
  }

  def unsubscribeEvents {
    EventHub.removeFromAll(this)
  }

  def onEvent(event: GameEvent) = {
    if (isEnabled) {
      event match {
        case SwitchPushed(SwitchId.levelExitA) => {
          status.advanceExitA
        }
        case SwitchPushed(SwitchId.levelExitB) => {
          status.advanceExitB
        }
        case SwitchPushed(SwitchId.levelBack) => {
          status.turnBackLevel
        }
        case PlayerInteracts() => {
          checkPlayerInteraction
        }
        case PickUpAccessCard(key) => {
          println("Added access card " + key)
          status.addAccessCard(key)
        }
        case PlayerDied() => {
          status.restartLevel
        }
        case LevelChange(old, next) => {
          unsubscribeEvents
          updatePlayerStatus
          advanceLevel
        }
        case _ =>
      }
    }
  }

  def advanceLevel() {
    setEnabled(false)
    stateManager.detach(this)
    stateManager.detach(sceneManager)
    stateManager.detach(stateManager.getState(classOf[BulletAppState]))
    rootNode.detachAllChildren

    var ps = app.getViewPort.getProcessors//List. fromIterator(app.getViewPort.getProcessors.iterator)
    for (p <- ps) app.getViewPort.removeProcessor(p)
    val levelLights = for (light <- rootNode.getLocalLightList()) yield light
    for (light <- levelLights) rootNode.removeLight(light)

    stateManager.attach(new BulletAppState)
    stateManager.attach(new InGameState(status))
  }

  def checkPlayerInteraction = {
    def interactWith(node: Spatial) = node.getName match {
      case NodeId.switch => {
        node.getControl(classOf[SwitchControl]).interact
      }
      case NodeId.door => {
        val control = node.getControl(classOf[DoorControl])
        control.key match {
          case Some(k) => {
            if (status.accessCards.contains(k)) {
              println("has card " + k)
              control.interact
            } else println("NEED CARD " + k)
          }
          case None => control.interact
        }
      }
      case _ => {
        println("don't know how to interact with: " + node.getName)
      }
    }
    HitChecker.checkHit(
      stateManager.getState(classOf[BulletAppState]),
      cam.getLocation(),
      cam.getDirection(),
      10,
      interactWith)
  }

  override def cleanup = {
    super.cleanup
    println("State Clean up")
    EventHub.removeFromAll(this)
    EventHub.removeFromAll(sceneManager)
    EventHub.removeEvent(PlayerShoot(true))
    EventHub.removeEvent(PlayerShoot(false))
    stateManager.detach(ParticleEmitterAppState)
    stateManager.detach(sceneManager)
  }

  def setupKeys(inputManager: InputManager) = {
    inputManager.addMapping("F4", new KeyTrigger(KeyInput.KEY_F4))
    inputManager.addListener(this, "F4")
  }

  def onAction(binding: String, value: Boolean, tpf: Float) = binding match {
    case "F4" => printSceneInfo
    case _ =>
  }
  override def update(tpf: Float) = {
    app.getListener().setLocation(cam.getLocation())
    app.getListener().setRotation(cam.getRotation())
    app.getAudioRenderer().setEnvironment(Environment.Dungeon)

    sceneManager.updatePlayerLocation(player.spatial.getWorldTranslation)
  }

  def printSceneInfo {
    println("Printing nodes")
    var map: Map[String, Int] = Map.empty.withDefaultValue(0)
    def collectNodes(s: Spatial) { map = map.updated(s.getName, map(s.getName) + 1) }
    rootNode.breadthFirst(collectNodes)
    map.toList.sortBy(_._2).foreach(p => println(s"${p._2} - ${p._1}"))
  }
}
