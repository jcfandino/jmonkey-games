package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.asset.plugins.FileLocator
import com.jme3.material.Material
import com.jme3.texture.Texture.WrapMode
import com.stovokor.Settings.Debug
import com.stovokor.util.math.Dist
import com.stovokor.util.math.Random
import com.jme3.math.ColorRGBA

class Ambiences(val ambiences: List[Ambience]) {
  def apply(i: Int) = ambiences(i)

  def material(getter: Ambience => List[TextureDefinition])(coor: (Int, Float))(implicit assetManager: AssetManager) =
    texture(getter)(coor)

  // Usage: ambiences.wallMaterial.apply((0, 0))
  def wallMaterial(implicit assetManager: AssetManager) = material(a => a.texturesForWalls)_
  def ceilingMaterial(implicit assetManager: AssetManager) = material(a => a.texturesForCeilings)_
  def floorMaterial(implicit assetManager: AssetManager) = material(a => a.texturesForFloors)_

  def doorMaterial(p: Float)(implicit assetManager: AssetManager): Material = {
    //material(a => a.texturesForDoors)_
    val definition = ambiences.flatMap(_.texturesForDoors).sortBy(
      d => (d.applicability.asInstanceOf[DoorApplicability].proportion - p).abs)
      .head
    ambiences.head.load(definition)
  }

  def reloadLightmap(fileName: String)(implicit assetManager: AssetManager) = {
    assetManager.registerLocator("cache", classOf[FileLocator])
    val lightMap = assetManager.loadTexture(fileName)
    for (a <- ambiences; material <- a.materials) {
      println(s"Reloading lightmap in material $material")
      material.setTexture("LightMap", lightMap)
      material.setBoolean("SeparateTexCoord", true)
      material.getTextureParam("LightMap").getTextureValue().setWrap(WrapMode.BorderClamp)
    }
    lightMap
  }

  def texture(getter: Ambience => List[TextureDefinition])(coor: (Int, Float))(implicit assetManager: AssetManager) = coor match {
    case (i, f) => {
      val amb = ambiences(i)
      val ts = getter(amb)
      val ti = (ts.size.toFloat * f).toInt
      amb.load(ts(ti))
    }
  }
}
class Ambience(
    val texturesForWalls: List[TextureDefinition],
    val texturesForCeilings: List[TextureDefinition],
    val texturesForFloors: List[TextureDefinition],
    val texturesForDoors: List[TextureDefinition]) extends LoadsTexture {

  lazy val textures = {
    texturesForWalls ++ texturesForCeilings ++ texturesForFloors
  }

  def materials(implicit assetManager: AssetManager) = {
    textures.map(t => load(t))
  }

}

trait LoadsTexture {

  // TODO improve, use mutable map getOrElseUpdate
  var cache: Map[TextureDefinition, Material] = Map.empty

  def load(texture: TextureDefinition)(implicit assetManager: AssetManager): Material = {
    def getter(td: TextureDefinition) = loadDef(td)
    val m = cache.get(texture)
    if (m.isDefined) m.get else {
      val lm = getter(texture)
      cache = cache.updated(texture, lm)
      lm
    }
  }

  var loaded: Map[TextureDefinition, Int] = Map.empty.withDefault(_ => 0)

  def loadDef(texture: TextureDefinition)(implicit assetManager: AssetManager) = {

    loaded = loaded.updated(texture, loaded(texture) + 1)

    val matDef = "light/Lighting.j3md"
    //    val matDef = "Common/MatDefs/Light/Lighting.j3md"
    val mode = WrapMode.Repeat
    val mat = new Material(assetManager, matDef)
    // Diffuse
    if (Debug.debugTexture) {
      mat.setTexture("DiffuseMap", assetManager.loadTexture("Debug1.png"))
    } else {
      mat.setTexture("DiffuseMap", assetManager.loadTexture(texture.diffuse))
    }
    mat.getTextureParam("DiffuseMap").getTextureValue().setWrap(mode)
    // Lightmap
    if (!Debug.disableLightmap) {
      mat.setTexture("LightMap", assetManager.loadTexture("lightmap-test.png"))
      mat.setBoolean("SeparateTexCoord", true)
    }
    if (texture.normal.isDefined) {
      mat.setTexture("NormalMap", assetManager.loadTexture(texture.normal.get))
      mat.getTextureParam("NormalMap").getTextureValue().setWrap(mode)
      mat.setFloat("ParallaxHeight", 0.1f)
    }
    mat.setColor("Specular", ColorRGBA.Red)
    mat.setFloat("Shininess", 2) // [0,128]
    //    mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    mat
  }
}
class AmbienceGenerator(seed: Long, number: Int) extends Generator[Ambiences](seed, number) {

  val rnd: Random = Random(seed, number)
  val catalog = TextureCatalog.load
  def surfaces = catalog.surface
  //  val ambiencesToGenerate = Dist.normalInt(rnd, 2, 5)

  object Predicates {
    def walls(a: TextureApplicability): Boolean = a.apply("walls")
    def walls(d: TextureDefinition): Boolean = walls(d.applicability)

    def ceiling(a: TextureApplicability): Boolean = a.apply("ceiling")
    def ceiling(d: TextureDefinition): Boolean = ceiling(d.applicability)

    def floor(a: TextureApplicability): Boolean = a.apply("floor")
    def floor(d: TextureDefinition): Boolean = floor(d.applicability)
  }

  override def generate(ctx: GeneratorContext) = {
    val seq = for (i <- 1 to ctx.numAmbiences) yield generate(i)
    new Ambiences(seq.toList)
  }

  def generate(idx: Int): Ambience = {
    val irnd = Random(seed, number, idx)
    val typesToChoose = Dist.normalInt(irnd, 1, 3)
    val shuffledTypes = irnd.shuffle(surfaces.keys.toList)
    val chosenTypes = shuffledTypes.take(typesToChoose)
    val chosenTextures = for (c <- chosenTypes) yield {
      val texturesForThisType = Dist.normalInt(irnd, 1, 3)
      irnd.shuffle(surfaces(c).toList).take(texturesForThisType)
    }
    val textures = chosenTextures.flatten.toList
    val withAll = addAllIfNeeded(textures, chosenTypes.toSeq)

    val wallsOnly = withAll.filter(Predicates.walls).toList
    val ceilingOnly = withAll.filter(Predicates.ceiling).toList
    val floorOnly = withAll.filter(Predicates.floor).toList
    val doorOnly = catalog.door.values.flatten.toList

    new Ambience(wallsOnly, ceilingOnly, floorOnly, doorOnly)
  }

  def addAllIfNeeded(textures: List[TextureDefinition], types: Seq[TextureType]) = {
    val add = addIfNeeded(textures, types)_
    val withAll = add(Predicates.walls) ++ add(Predicates.ceiling) ++ add(Predicates.floor)
    withAll.toSet
  }

  def addIfNeeded(textures: List[TextureDefinition], types: Seq[TextureType])(predicate: TextureApplicability => Boolean): List[TextureDefinition] = {
    def f(d: TextureDefinition) = predicate(d.applicability)
    val has = textures.find(f).isDefined
    val result = if (has) {
      textures
    } else {
      val candidates = surfaces.filterKeys(c => types.contains(c)).values.flatten
      textures ++ candidates.filter(f).take(1)
    }
    result.toList
  }
}