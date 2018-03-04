package com.stovokor.gen.level

import com.jme3.post.FilterPostProcessor
import com.jme3.post.filters.FogFilter
import com.stovokor.util.math.Random
import com.jme3.math.ColorRGBA
import com.jme3.asset.AssetManager

class FogGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager) extends Generator[FilterPostProcessor](seed, number) {

  def generate(ctx: GeneratorContext) = {
    val rnd = Random(seed, number)
    val fpp = new FilterPostProcessor(assetManager)
    val fog = new FogFilter()
    fog.setFogColor(new ColorRGBA(rnd.nextFloat, rnd.nextFloat, rnd.nextFloat, 1.0f))
    fog.setFogDistance(155)
    fog.setFogDensity(2.0f)
    fpp.addFilter(fog)
    fpp
  }
}