package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.texture.Texture
import com.jme3.material.RenderState.BlendMode

object MaterialFactory {

  def create(assetManager: AssetManager, file: String): Material = {
    val tex = assetManager.loadTexture(file)
    tex.setMagFilter(Texture.MagFilter.Nearest)
    tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps)
    tex.setAnisotropicFilter(0)

    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setTexture("ColorMap", tex)
    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha)
    mat
  }
}