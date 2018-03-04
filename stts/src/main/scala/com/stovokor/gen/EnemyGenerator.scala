package com.stovokor.gen

import com.jme3.animation.SkeletonControl
import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.util.TangentBinormalGenerator
import com.stovokor.ai.Navigation
import com.stovokor.domain.CharacterBuilder
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.domain.Weapon
import com.stovokor.domain.enemy.EnemyCharacter
import com.stovokor.domain.enemy.TacticBehaviors
import com.stovokor.domain.enemy.StrafeAround
import com.stovokor.domain.enemy.EnemyTactic
import com.stovokor.domain.enemy.DirectApproach
import com.stovokor.domain.enemy.StandStill
import com.stovokor.domain.enemy.TacticParameters
import com.stovokor.gen.level.Generator
import com.stovokor.gen.level.GeneratorContext
import com.stovokor.Settings.Debug
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.stovokor.Settings
import com.jme3.scene.BatchNode

class EnemyGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager)
    extends Generator[Set[EnemyCharacter]](seed, number) {

  def generate(ctx: GeneratorContext): Set[EnemyCharacter] = {
    val quest = ctx.quest.get
    val nav = ctx.navigation.get
    val classes = ctx.enemyClasses

    val allEnemies = for (r <- quest.verts; e <- r.enemies) yield {
      val location = r.absoluteItemPosition(e).add(Vector3f.UNIT_Y mult 2)
      println(s"Enemy ${e.index} at $location rotated: ${e.angle}")
      val enemy = classes(e.index).create(location, e.angle, nav)
      enemy
    }
    if (Debug.disableEnemies) Set()
    else allEnemies.toSet
  }
}

class EnemyClassGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager)
    extends Generator[List[EnemyClass]](seed, number) {

  val weaponGenerator = new WeaponGenerator()

  def generate(ctx: GeneratorContext) = {
    //    val quest = ctx.quest.get
    //    val nav = ctx.navigation.get

    //val classes = for 
    val classes = (0 to 2).map(generateClass)
    classes.toList
  }

  def generateClass(index: Int): EnemyClass = {
    val asset = "Models/Character/robot-base/robot-base.j3o"
    val model = assetManager.loadModel(asset)
    //    model.scale(1f, 1.5f,1f)
    val skeletonModel = model
      .asInstanceOf[Node].getChild("Armature")
      .asInstanceOf[Node].getChild("RobotMesh")
      .asInstanceOf[Node].getChild("ShapeIndexedFaceSet.001-entity")
      .asInstanceOf[Node].getChild("ShapeIndexedFaceSet.001-ogremesh")

    skeletonModel.setName("skeleton")
    skeletonModel.rotate(0, FastMath.PI, 0)

    val batch = new BatchNode("enemy")
    //    val batch = new Node("enemy")
    batch.attachChild(skeletonModel)

    val skeletonControl = skeletonModel.getControl(classOf[SkeletonControl])

    skeletonControl.setHardwareSkinningPreferred(Settings.Graphics.hardwareSkinning)
    skeletonModel.setMaterial(materialBase)
    TangentBinormalGenerator.generate(skeletonModel)

    val bodyParts: List[RobotPart] = List(
      new RobotPart("head", 1),
      new RobotPart("chest", 1),
      new RobotPart("stomach", 1),
      new RobotPart("pelvis", "Stomach", 1),
      new RobotLeftLimbPart("upper-arm", "Upper.Arm", 1),
      new RobotLeftLimbPart("lower-arm", "Lower.Arm", 1),
      new RobotLeftLimbPart("upper-leg", "Upper.Leg", 1),
      new RobotLeftLimbPart("lower-leg", "Lower.Leg", 1),
      new RobotLeftLimbPart("foot", "Foot", 1),
      new RobotRightLimbPart("upper-arm", "Upper.Arm", 1),
      new RobotRightLimbPart("lower-arm", "Lower.Arm", 1),
      new RobotRightLimbPart("upper-leg", "Upper.Leg", 1),
      new RobotRightLimbPart("lower-leg", "Lower.Leg", 1),
      new RobotRightLimbPart("foot", "Foot", 1))
    //      new RobotPart("chest", 1))

    val material = generatePartsMaterial
    for (part <- bodyParts) {
      part.attachTo(skeletonControl, assetManager, material)
    }

    // TODO Try to fix BatchNode
    //    batch.batch()

    val tactic = generateTactic
    // enemy1
    val weapon = weaponGenerator.generate(seed, number, index)
    val builder = CharacterBuilder()
      .collision(3, 10)
      .spatialModel(batch)
      .mass(400F)
      .weapon(weapon)
      .tactic(tactic)
      .health(20)
      .speed(12)

    new EnemyClass(builder)
  }

  def generateTactic = {
    val behaviors = new TacticBehaviors(
      new StandStill, new DirectApproach, new StrafeAround)
    val parameters = new TacticParameters(20)
    new EnemyTactic(behaviors, parameters)
  }

  lazy val materialBase = {
    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    mat.setTexture("DiffuseMap", assetManager.loadTexture("Models/Character/robot-base/robot-base-diffuse.png"))
    mat.setTexture("NormalMap", assetManager.loadTexture("Models/Character/robot-base/robot-base-normal.png"))
    mat
  }
  def generatePartsMaterial = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    material.setTexture("DiffuseMap",
      assetManager.loadTexture("Models/Character/robot-parts-1/robot-parts-1-diffuse.png"))
    material.setTexture("NormalMap",
      assetManager.loadTexture("Models/Character/robot-parts-1/robot-parts-1-normal.png"))

    //    material.setBoolean("UseMaterialColors", true)
    //        material.setBoolean("VTangent",true)
    material.setFloat("ParallaxHeight", -2.9f)
    material.setColor("Diffuse", ColorRGBA.randomColor())
    material.setColor("Specular", ColorRGBA.White)
    material.setFloat("Shininess", 128f * Math.random.asInstanceOf[Float]) // [0,128]

    material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    material
  }

  lazy val boxMaterial = {
    val mat = new Material(assetManager,
      "Common/MatDefs/Light/Lighting.j3md")
    mat.setTexture("DiffuseMap",
      assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
    mat.setTexture("NormalMap",
      assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
    mat
  }

}

class RobotPart(val name: String, val bone: String, val idx: Integer, val mirrored: Boolean) {

  def this(name: String, bone: String, idx: Integer) = this(name, bone, idx, false)
  def this(name: String, idx: Integer) = this(name, name(0).toUpper + name.drop(1), idx, false)

  def objectFile = s"Models/Character/robot-parts-$idx/robot-parts-$idx-$name.j3o"

  def attachTo(skeleton: SkeletonControl, assetManager: AssetManager, material: Material) {
    val part = assetManager.loadModel(objectFile)
    //    val part = new Geometry("enemy", new Box(1,3,1))
    if (mirrored) {
      part.setLocalScale(-1, 1, 1)
    }
    part.setMaterial(material)
    TangentBinormalGenerator.generate(part)
    println(s"Attaching part $name to $bone")
    skeleton.getAttachmentsNode(bone).attachChild(part)
  }
}
class RobotLeftLimbPart(name: String, bone: String, idx: Integer)
  extends RobotPart(name, bone + ".L", idx)
class RobotRightLimbPart(name: String, bone: String, idx: Integer)
  extends RobotPart(name, bone + ".R", idx, true)

class EnemyClass(val b: CharacterBuilder) {
  def create(position: Vector3f, angle: Float, nav: Navigation): EnemyCharacter = {
    b.navigation(nav).on(position).angle(angle).asEnemy
  }
}