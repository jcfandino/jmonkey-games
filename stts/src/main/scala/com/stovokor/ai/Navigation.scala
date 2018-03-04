package com.stovokor.ai

import com.jme3.ai.navmesh.NavMesh
import com.jme3.scene.Mesh
import com.jme3.material.Material
import com.jme3.scene.Geometry
import com.jme3.math.ColorRGBA
import com.jme3.asset.AssetManager

class Navigation(val navMesh: NavMesh, val mesh: Mesh) {

  def enableDebug = false
  def debugMesh(assetManager: AssetManager) = {
    val navGeom = new Geometry("NavMesh")
    navGeom.setMesh(mesh)
    val color = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    color.setColor("Color", ColorRGBA.Green)
    color.getAdditionalRenderState().setWireframe(true)
    navGeom.setMaterial(color)
    navGeom
  }

}