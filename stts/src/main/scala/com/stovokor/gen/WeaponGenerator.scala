package com.stovokor.gen

import scala.collection.immutable.List
import com.jme3.asset.AssetManager
import com.jme3.audio.AudioNode
import com.jme3.material.Material
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Cylinder
import com.jme3.texture.Texture.WrapMode
import com.stovokor.domain.NodeId
import com.stovokor.domain.Pistol
import com.stovokor.domain.TechSpecs
import com.stovokor.domain.Technology
import com.stovokor.domain.Weapon
import com.stovokor.domain.WeaponSounds
import com.stovokor.util.math.Dist
import com.stovokor.util.math.Random
import com.jme3.math.ColorRGBA
import com.jme3.scene.shape.Box
import com.stovokor.domain.AutomaticRifle
import com.stovokor.domain.Shotgun
import com.jme3.effect.ParticleEmitter
import com.stovokor.STTS
import com.jme3.effect.ParticleMesh.Type
import com.jme3.effect.shapes.EmitterSphereShape
import com.jme3.math.Vector3f
import com.stovokor.domain.WeaponControl
import com.stovokor.domain.NullWeaponControl

class WeaponGenerator(implicit val assetManager: AssetManager) {

  // Testing guns generate
  def generate(seed: Long, number: Int, index: Int): Weapon = {
    if (index == 0) coltPistol
    else if (index == 1) rem870shotgun
    else ak47automaticRifle
  }

  def generateReal(seed: Long, number: Int, index: Int): Weapon = {
    val rnd = Random(seed, number, index)
    val technology = chooseTechnology(rnd)
    val spec = generateSpec(rnd, technology)
    val gunNode = generateModel(rnd)
    //    gunNode.setLocalTranslation(new Vector3f(-4, 0, 2.5F))
    val sounds = generateSounds(rnd, technology, spec)
    sounds.attachTo(gunNode)
    val name = s"${technology} ${seed}-${number}-${index}"
    println(s"Generated: $name with: $spec")
    new Weapon(name, gunNode, new NullWeaponControl, technology, spec, sounds)
  }

  val technologies = List(Pistol, AutomaticRifle)

  def chooseTechnology(random: Random): Technology = {
    technologies(random.nextInt(technologies.size))
  }

  def generateSpec(random: Random, tech: Technology): TechSpecs = {
    new TechSpecs(
      Dist.normalInt(random, 50, 1000), // milliseconds between shots
      Dist.lineal(0, 5, 2000), // precision: f(triggerTime)
      Dist.normalInt(random, 20, 1000), // max damage distance
      Dist.normalInt(random, 1, 50), // damage per shot
      Dist.lineal(0, 15, 100), // f(distance)
      1, // for shotguns
      1, // when multicannon or energy based)
      true)
  }

  private def generateModel(rnd: Random) = {
    def cylinder = {
      val sphMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
      sphMat.setTexture("DiffuseMap",
        assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
      sphMat.setTexture("NormalMap",
        assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
      // weapon
      //val cyl = new Geometry("gun", new Cylinder(6, 8, .1F, 4, true))
      val cyl = new Geometry("gun", new Cylinder(
        Dist.normalInt(rnd, 3, 10),
        Dist.normalInt(rnd, 4, 12),
        .1F,
        Dist.normalInt(rnd, 2, 6),
        true))
      cyl.setMaterial(sphMat)
      cyl
    }
    def m4 = {
      val m4 = assetManager.loadModel("Models/Weapons/M4.j3o")
      m4.setMaterial(randomColorMaterial(rnd))
      m4.scale(0.25f * (0.9f + .5f * rnd.nextFloat()))
      m4
    }
    def ak47 = {
      val scene = assetManager.loadModel("Models/Weapons/assault01/assault01.j3o")
      val material = assetManager.loadMaterial("Models/Weapons/assault01/assault01.j3m")
      val ak47 = scene.asInstanceOf[Node].getChild("ak47")
      ak47.setMaterial(material)
      ak47.setLocalScale(0.75f)
      ak47.asInstanceOf[Node]
    }

    val model = ak47

    val gunNode = new Node(NodeId.weapon)
    gunNode.attachChild(model)
    gunNode
  }

  def generateSounds(random: Random, technology: Technology, spec: TechSpecs) = {
    val shoot = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false)
    shoot.setPositional(true)
    shoot.setLooping(false)
    shoot.setVolume(2)
    //    shoot.setMaxDistance(100)
    shoot.setRefDistance(50f)
    shoot.setPitch(0.5f + 1.5f * random.nextFloat())

    new WeaponSounds(shoot)
  }

  def randomColorMaterial(rnd: Random) = {
    def color = 0.4f * rnd.nextFloat()
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    material.setBoolean("UseMaterialColors", true)
    material.setFloat("ParallaxHeight", -2.9f)
    material.setColor("Diffuse", new ColorRGBA(color, color, color, 1f))
    material.setColor("Specular", ColorRGBA.White)
    material.setFloat("Shininess", 128f * rnd.nextFloat()) // [0,128]
    material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    material
  }

  def generateInitialWeapon() = {
//    ak47automaticRifle
        rem870shotgun
  }

  lazy val coltPistol = {
    val scene = assetManager.loadModel("Models/Weapons/pistol01/pistol01.j3o")
    val colt = scene.asInstanceOf[Node].getChild("Colt")
    val material = assetManager.loadMaterial("Models/Weapons/pistol01/pistol01.j3m")
    colt.setMaterial(material)

    val node = new Node(NodeId.weapon)
    node.attachChild(colt)

    val flash = createFlashEmitter
    node.attachChild(flash)
    flash.setLocalTranslation(-3, 0.4f, 0)

    val weaponSpec = new TechSpecs(
      250, // milliseconds between shots
      t => 0F, // precision: f(triggerTime)
      100, // max damage distance
      20, // damage per shot
      d => if (d > 100) 15 else d * 0.15F, // f(distance)
      1, // for shotguns
      1, // when multicannon or energy based)
      false)

    val snds = generateSounds(Random(1, 1), Pistol, weaponSpec)

    new Weapon("colt", node, new NullWeaponControl, Pistol, weaponSpec, snds)
  }

  lazy val ak47automaticRifle = {
    val scene = assetManager.loadModel("Models/Weapons/assault01/assault01.j3o")
    val material = assetManager.loadMaterial("Models/Weapons/assault01/assault01.j3m")
    val ak47 = scene.asInstanceOf[Node].getChild("ak47")
    ak47.setMaterial(material)
    ak47.setLocalScale(0.75f)
    ak47.setLocalTranslation(0, -.5f, 0)

    val node = new Node(NodeId.weapon)
    node.attachChild(ak47)

    val flash = createFlashEmitter
    node.attachChild(flash)
    flash.setLocalTranslation(-5, .4f, 0)

    val weaponSpec = new TechSpecs(
      100, // milliseconds between shots
      t => if (t > 2f) .005f else t * .0025f, // precision: f(triggerTime)
      100, // max damage distance
      20, // damage per shot
      d => if (d > 100) 15 else d * 0.15F, // f(distance)
      1, // for shotguns
      1, // when multicannon or energy based)
      true)

    val snds = generateSounds(Random(1, 2), AutomaticRifle, weaponSpec)

    new Weapon("ak47", node, new NullWeaponControl, AutomaticRifle, weaponSpec, snds)
  }

  lazy val rem870shotgun= {
    val rem = assetManager.loadModel("Models/Weapons/rem870/rem870.j3o")
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/Weapons/rem870/rem870-diffuse.png"))
    material.setTexture("NormalMap", assetManager.loadTexture("Models/Weapons/rem870/rem870-normal.png"))
//    val material = assetManager.loadMaterial("Models/Weapons/rem870/rem870.j3m")
//    material.setBoolean("UseMaterialColors", true)
//    material.setColor("Diffuse", ColorRGBA.Gray)
    rem.setMaterial(material)

    val node = new Node(NodeId.weapon)
    node.attachChild(rem)

    val flash = createFlashEmitter
    node.attachChild(flash)
    flash.setLocalTranslation(-5, 0.4f, 0)

    val weaponSpec = new TechSpecs(
      500, // milliseconds between shots
      t => 0F, // precision: f(triggerTime)
      100, // max damage distance
      16, // damage per shot
      d => if (d > 80f) 16f else d * 0.1f, // f(distance)
      .01f, // for shotguns
      1, // when multicannon or energy based)
      false)

    val snds = generateSounds(Random(1, 1), Pistol, weaponSpec)

    new Weapon("shotgun", node, new NullWeaponControl, Shotgun, weaponSpec, snds)
  }

  def createFlashEmitter = {
    val flash = new ParticleEmitter("flash", Type.Point, 12)
    flash.setSelectRandomImage(true)
    flash.setStartColor(new ColorRGBA(1f, 0.9f, 0.6f, 1f))
    flash.setEndColor(new ColorRGBA(1f, 0.6f, 0.2f, 0f))
    flash.setStartSize(.1f)
    flash.setEndSize(.8f)
    flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f))
    flash.setParticlesPerSec(0)
    flash.setGravity(0, 0, 0)
    flash.setLowLife(.2f)
    flash.setHighLife(.2f)
    flash.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2f, 5f))
    flash.getParticleInfluencer().setVelocityVariation(1)
    flash.setImagesX(2)
    flash.setImagesY(2)
    // TODO - Cache the material when loading the game
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
    mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"))
    mat.setBoolean("PointSprite", true)
    flash.setMaterial(mat)
    flash
  }

}

