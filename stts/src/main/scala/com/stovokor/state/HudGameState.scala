package com.stovokor.state

import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.iterableAsScalaIterable
import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.asset.AssetManager
import com.jme3.audio.Environment
import com.jme3.bullet.BulletAppState
import com.jme3.font.BitmapText
import com.jme3.input.InputManager
import com.jme3.input.controls.ActionListener
import com.jme3.light.Light
import com.jme3.material.Material
import com.jme3.renderer.Camera
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.shape.Box
import com.stovokor.domain.CharacterBuilder
import com.stovokor.domain.DoorControl
import com.stovokor.domain.GameStatus
import com.stovokor.domain.NodeId
import com.stovokor.domain.Pistol
import com.stovokor.domain.PlayerCharacter
import com.stovokor.domain.SwitchControl
import com.stovokor.domain.SwitchId
import com.stovokor.domain.TechSpecs
import com.stovokor.domain.Weapon
import com.stovokor.domain.WeaponControl
import com.stovokor.gen.LevelGenerator
import com.stovokor.gen.WeaponGenerator
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.HitChecker
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.PickUpAccessCard
import com.stovokor.util.jme.PlayerInteracts
import com.stovokor.util.jme.SwitchPushed
import com.stovokor.util.math.Random
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.math.Vector3f
import com.jme3.math.ColorRGBA
import com.stovokor.util.debug.NavigationDebug
import com.jme3.post.ssao.SSAOFilter
import com.jme3.post.FilterPostProcessor
import com.stovokor.util.jme.LevelChange
import com.stovokor.util.jme.LevelChange
import com.stovokor.domain.PlayerStatus
import com.stovokor.util.jme.SwitchPushed
import com.stovokor.util.jme.PlayerShoot
import com.stovokor.util.jme.PickUpAccessCard
import com.stovokor.util.jme.PlayerDied
import com.stovokor.util.jme.PlayerDied
import com.stovokor.STTSApp
import com.stovokor.util.jme.HealthChange
import com.stovokor.util.jme.AmmoChange
import com.stovokor.util.jme.WeaponDrawn
import com.stovokor.domain.Ammo
import com.jme3.font.BitmapFont
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.screen.Screen
import com.jme3.niftygui.NiftyJmeDisplay
import com.jme3.audio.AudioRenderer
import com.jme3.renderer.ViewPort
import de.lessvoid.nifty.elements.Element
import de.lessvoid.nifty.elements.render.TextRenderer
import com.stovokor.domain.PistolBullet
import com.stovokor.domain.RifleBullet
import com.stovokor.domain.Shell
import com.stovokor.domain.Rocket
import com.stovokor.domain.Energy
import com.stovokor.util.jme.ArmorChange

class HudGameState(val status: GameStatus) extends AbstractAppState {

  var app: STTSApp = null
  def gui = app.getGuiNode
  def settings = app.getSettings
  def smallFont = app.getGuiFont
  lazy val hudFont = app.getAssetManager().loadFont("Interface/Fonts/Orbitron-small.fnt")
  def level = status.levelNumber
  def currentWeapon = status.playerStatus.weapons.head
  def currentHealth = status.playerStatus.health

  val controller: HudController = new HudController(status)

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    super.initialize(appStateManager, simpleApp)
    app = simpleApp.asInstanceOf[STTSApp]

    initNifty
    initCrossHair
    refreshHudLabels
  }

  def initNifty() {
    val niftyDisplay = new NiftyJmeDisplay(app.getAssetManager,
      app.getInputManager,
      app.getAudioRenderer,
      app.getGuiViewPort)

    val nifty = niftyDisplay.getNifty()
    nifty.fromXml("Interface/Nifty/Hud.xml", "hud", controller);

    app.getGuiViewPort.addProcessor(niftyDisplay);
  }

  def initCrossHair() = {
    val crosshairText = new BitmapText(smallFont)
    crosshairText.setText("+")
    crosshairText.setLocalTranslation((settings.getWidth() - crosshairText.getLineWidth()) * 0.5f,
      (settings.getHeight() + crosshairText.getLineHeight()) * 0.5f, 0)
    gui.attachChild(crosshairText)
  }

  def refreshHudLabels {
    val currentWeapon = status.playerStatus.currentWeapon
    for (ammo <- List(PistolBullet, RifleBullet, Shell, Rocket, Energy)) {
      val value = status.playerStatus.getAmmo(ammo)
      controller.printAmmo(ammo, value)
      if (currentWeapon.isDefined) {
        controller.printAmmo(value)
      }
    }
    controller.printHealth(status.playerStatus.health)
    controller.printLevel(level)
  }

  override def update(tpf: Float) = {}
}

class HudController(status: GameStatus) extends ScreenController with LogicEventListener {

  var nifty: Nifty = null
  def playerStatus = status.playerStatus

  var currentAmmo: Option[Ammo] = {
    if (playerStatus.currentWeapon.isDefined)
      Some(playerStatus.currentWeapon.get.tech.ammoType)
    else None
    // TODO this inited before the weapon is added to the player, so is always None
  }

  def bind(nifty: Nifty, screen: Screen) {
    this.nifty = nifty
  }

  def onEndScreen() {
    EventHub.removeFromAll(this)
  }

  def onStartScreen() {
    EventHub.subscribeByType(this, classOf[PickUpAccessCard])
    EventHub.subscribeByType(this, classOf[LevelChange])
    EventHub.subscribeByType(this, classOf[HealthChange])
    EventHub.subscribeByType(this, classOf[ArmorChange])
    EventHub.subscribeByType(this, classOf[AmmoChange])
    EventHub.subscribeByType(this, classOf[WeaponDrawn])
  }

  def onEvent(event: GameEvent) = event match {
    case PickUpAccessCard(key) => {
      printCards(key)
    }
    case LevelChange(old, next) => {
      printLevel(next)
    }
    case HealthChange(health) => {
      printHealth(health)
    }
    case ArmorChange(armor) => {
      printArmor(armor)
    }
    case AmmoChange(ammoType, amount) => {
      println(s"Ammo change $ammoType - $amount")
      printAmmo(ammoType, amount)
      for (ammo <- currentAmmo if ammoType == ammo) {
        printAmmo(amount)
      }
    }
    case WeaponDrawn(weapon) => {
      currentAmmo = Some(weapon.tech.ammoType)
      val ammo = playerStatus.getAmmo(weapon.tech.ammoType)
      printAmmo(ammo)
      printWeapon(weapon)
    }
    case _ =>

  }

  def printHealth(newHealth: Int) {
    print(GuiNodeId.health, newHealth)
  }

  def printArmor(newArmor: Int) {
    print(GuiNodeId.armor, newArmor)
  }
  def printCards(newCard: String) {
    val n = newCard.last - ('0' - 1)
    val c = ('W'.toInt + n - 1).toChar.toString
    print(s"key$n-icon", c)
  }

  def printLevel(level: Int) {
    val ring = (Math.log(level) / Math.log(2)).toInt
    val ringLvl = level - Math.pow(2, ring).toInt + 1
    print(GuiNodeId.level, s"R${ring}L${ringLvl}")
  }
  def printWeapon(weapon: Weapon) {
    print(GuiNodeId.weapon, s"${weapon.tech} ${weapon.id}")
  }
  def printAmmo(ammo: Ammo, value: Int) {
    print(GuiNodeId.ammo(ammo), value)
  }
  def printAmmo(value: Int) {
    print(GuiNodeId.ammo, value)
  }

  def print(field: String, n: Int) {
    print(field, n.toString)
  }

  def print(field: String, text: String) {
    val niftyElement = nifty.getCurrentScreen().findElementByName(field)

    println(s"Printing $field = $text ($niftyElement")
    niftyElement.getRenderer(classOf[TextRenderer]).setText(text)
  }

}

object GuiNodeId {
  val health = "health-label"
  val armor = "armor-label"
  val level = "level-label"
  val weapon = "info-label"
  val crosshair = "crosshair"
  val cards = "cards"
  val ammo = "ammo-label"
  val ammoIcon = "ammo-icon"
  def ammo(a: Ammo) = s"$a-label"
  def key(k: Int) = s"key-$k-icon"

}
