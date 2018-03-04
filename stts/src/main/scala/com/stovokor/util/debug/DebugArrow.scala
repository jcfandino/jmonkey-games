package com.stovokor.util.debug

import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Spatial
import com.jme3.scene.debug.Arrow
import com.jme3.math.Quaternion
import com.jme3.asset.AssetManager
import com.jme3.scene.Node

class DebugArrow(val assetManager: AssetManager, val origin: Vector3f, val direction: Vector3f) {

  def draw(node:Node) = {
    val mark = initMark
    mark.setLocalTranslation(origin)
    val q = new Quaternion();
    q.lookAt(direction, Vector3f.UNIT_Y);
    mark.setLocalRotation(q);
    node.attachChild(mark)
  }

  def initMark: Spatial = {
    val arrow = new Arrow(Vector3f.UNIT_Z.mult(2f));
    arrow.setLineWidth(3);

    //Sphere sphere = new Sphere(30, 30, 0.2f);
    val mark = new Geometry("arrow", arrow);
    //mark = new Geometry("BOOM!", sphere);
    val mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mark_mat.setColor("Color", ColorRGBA.Red);
    mark.setMaterial(mark_mat);
    mark
  }
}