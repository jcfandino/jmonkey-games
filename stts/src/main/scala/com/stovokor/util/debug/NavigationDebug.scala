package com.stovokor.util.debug

import scala.collection.JavaConversions._
import com.jme3.scene.Node
import com.jme3.asset.AssetManager
import scala.collection.immutable.Map
import com.jme3.ai.navmesh.Path
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.scene.shape.Curve
import com.jme3.math.Spline
import com.jme3.math.Spline.SplineType
import com.jme3.scene.debug.Arrow
import com.jme3.math.Vector3f

class NavigationDebug(val root: Node, val assetManager: AssetManager) {

  val pmat = assetManager.loadMaterial("Common/Materials/RedColor.j3m")
  val lmat = {
    val m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    m.getAdditionalRenderState().setWireframe(true)
    m.setColor("Color", ColorRGBA.Cyan)
    m
  }

  var paths: Map[Any, Node] = Map()

  def drawPath(key: Any, path: Path) {
    clear(key)
    val node = new Node("debugPath")
    paths = paths.updated(key, node)
    for (w <- path.getWaypoints()) {
      val cp = w.getPosition().clone()
      val geo = new Geometry("box", new Box(cp, 0.3f, 0.3f, 0.3f));
      geo.setMaterial(pmat);
      node.attachChild(geo);
    }

    for ((p1, p2) <- path.getWaypoints() zip path.getWaypoints().tail) {
      val v1 = p1.getPosition
      val v2 = p2.getPosition
      val arrow = new Arrow(v2 subtract v1)
      arrow.setLineWidth(3)

      val geo = new Geometry("arrow", arrow)
      geo.setMaterial(lmat)
      geo.setLocalTranslation(v1)
      node.attachChild(geo)
    }
    root.attachChild(node)
    node.setLocalTranslation(0, 0.1f, 0)
  }

  def clear(key: Any) {
    if (paths.contains(key)) {
      root.detachChild(paths(key))
      paths = paths - key
    }
  }
}