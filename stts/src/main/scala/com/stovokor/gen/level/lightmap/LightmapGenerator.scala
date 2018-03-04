package com.stovokor.gen.level.lightmap

import com.stovokor.gen.level.Generator
import com.stovokor.gen.level.GeneratorContext
import com.jme3.texture.Texture.WrapMode
import com.stovokor.Settings
import com.jme3.asset.AssetManager
import com.stovokor.util.jme.JmeExtensions._
import com.jme3.scene.Geometry

class LightmapGenerator(seed: Long, number: Int)(implicit assetManager: AssetManager) extends Generator[Unit](seed, number) {

  def generate(ctx: GeneratorContext) {
    if (!Settings.Debug.disableLightmap) {
      // Calculate lightmap UV
      ctx.uvBuilder.applyUVs

      // Generate geometry
      val geometryBuilder = new LightmapGeometryBuilder
      geometryBuilder.add(ctx.levelNode)
      (ctx.columns ++ ctx.doors).map(_.spatial).foreach(geometryBuilder.add)
      //      (ctx.columns).map(_.spatial).foreach(geometryBuilder.add)
      val bakeGeometry = geometryBuilder.build

      // Bake
      val baker = new LightmapBaker(seed, number)
      if (Settings.Debug.disableCache || !baker.cached)
        baker.bake(bakeGeometry, ctx.lights)

      if (!Settings.Debug.debugTexture)
        ctx.ambiences.get.reloadLightmap(baker.fileName)
    }
  }
}
