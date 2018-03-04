package com.stovokor.gen.level

import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.Reflections
import java.util.regex.Pattern
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.FilterBuilder
import scala.collection.JavaConversions._

sealed case class TextureType(val name: String) {
  override def toString = name
}

//case object Bricks extends TextureType("bricks")
//case object Granite extends TextureType("granite")
//case object Metal extends TextureType("granite")
//case object Stone extends TextureType("stone")
//case object Tiles extends TextureType("tiles")

abstract class TextureApplicability { //(val walls: Boolean, val ceiling: Boolean, val floor: Boolean) {
  //  def this(s: String) = this(s.contains("w"), s.contains("c"), s.contains("f"))
  //  override def toString = s"${walls.→("w")}${ceiling.→("c")}${floor.→("f")}"
  def apply(s: String): Boolean
}

case class DoorApplicability(width: Int, height: Int) extends TextureApplicability {
  def this(s: String) = this(s.split("x")(0).toInt, s.split("x")(1).toInt)
  override def toString = s"${width}x${height}"

  def apply(s: String) = true
  val proportion = width.toFloat / height.toFloat
}

case class SurfaceApplicability(val walls: Boolean, val ceiling: Boolean, val floor: Boolean) extends TextureApplicability {
  def this(s: String) = this(s.contains("w"), s.contains("c"), s.contains("f"))
  override def toString = s"${walls.→("w")}${ceiling.→("c")}${floor.→("f")}"

  def apply(s: String) = s match {
    case "walls" => walls
    case "ceiling" => ceiling
    case "floor" => floor
    case _ => false
  }
}

class TextureDefinition(
    val diffuse: String,
    val normal: Option[String],
    val bump: Option[String],
    val classification: TextureType,
    val applicability: TextureApplicability) {

  override def toString = s"Texture: D: $diffuse N: $normal B: $bump Type: $classification Applic: ($applicability)"
}

class TextureFile(
    val path: String,
    val classification: String,
    val index: Int,
    val applicability: String,
    val modifier: Option[String]) {

  override def toString = s"Path: $path ($classification $index $applicability $modifier)"
}

class TextureCatalog(val surface: Map[TextureType, Iterable[TextureDefinition]],
    val door: Map[TextureType, Iterable[TextureDefinition]]) {

}

object TextureCatalog {

  type DefinitionFunction = Set[TextureFile] => TextureDefinition

  val surfaceRegex = "(\\w+)\\-(\\d{3})\\-([wfc]{1,3})\\-?(\\w+)?\\.(:?jpg|png)"
  lazy val surfacePattern = Pattern.compile(surfaceRegex)

  val doorRegex = "(\\w+)\\-(\\d{3})\\-([0-9x]{3})\\-?(\\w+)?\\.(:?jpg|png)"
  lazy val doorPattern = Pattern.compile(doorRegex)

  lazy val load: TextureCatalog = { //Map[TextureType, Iterable[TextureDefinition]] = {
    val reflections = new Reflections(new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage("textures"))
      .setScanners(new ResourcesScanner())
      .filterInputsBy(new FilterBuilder().includePackage("textures")))

    val doorFiles = //Set("metal-002-2x2.jpg","metal-002-2x2-bump.jpg","metal-002-2x2-normal.png")
      reflections.getResources(doorPattern).toSet
    val surfaceFiles = reflections.getResources(surfacePattern).toSet

    new TextureCatalog(
      groupTextures(surfaceFiles, surfacePattern, asSurfaceDefinition),
      groupTextures(doorFiles, doorPattern, asDoorDefinition))
  }

  def groupTextures(files: Set[String], pattern: Pattern, definitionFunction: DefinitionFunction) = {
    val textureFiles = files.map(f => asTextureFile(pattern, f))
    val grouped = textureFiles.groupBy(f => f.classification + f.index)
    val defs = grouped.map(g => definitionFunction(g._2))
    val map = defs.groupBy(d => d.classification)
    println(s"${defs.size} textures:\n${defs.mkString("\n")}")
    map
  }

  def asSurfaceDefinition(group: Set[TextureFile]): TextureDefinition = {
    println(s"Surface textures $group")
    asTextureDefinition(group)(s => new SurfaceApplicability(s))
  }

  def asDoorDefinition(group: Set[TextureFile]): TextureDefinition = {
    println(s"Door textures $group")
    asTextureDefinition(group)(s => new DoorApplicability(s))
  }

  def asTextureDefinition(group: Set[TextureFile])(asApplicability: String => TextureApplicability): TextureDefinition = {
    println(s"Door textures $group")
    val diffuse = group.find(f => f.modifier.isEmpty).get
    val normal = group.find(f => f.modifier.isDefined && f.modifier.get == "normal").map(f => f.path)
    val bump = group.find(f => f.modifier.isDefined && f.modifier.get == "bump").map(f => f.path)
    val texType = new TextureType(diffuse.classification)
    val texAppl = asApplicability(diffuse.applicability)
    new TextureDefinition(diffuse.path, normal, bump, texType, texAppl)
  }

  def asTextureFile(pattern: Pattern, path: String) = {
    val matcher = pattern.matcher(path.replaceAll("^.*/", ""))
    if (!matcher.matches()) throw new RuntimeException("File doesn't match: " + path)
    val classification = matcher.group(1)
    val index = matcher.group(2).replaceAll("^0+", "").toInt
    val applicability = matcher.group(3)
    val modifier = Option(matcher.group(4))
    new TextureFile(path, classification, index, applicability, modifier)
  }
}
