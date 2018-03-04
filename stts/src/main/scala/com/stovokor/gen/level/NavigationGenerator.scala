package com.stovokor.gen.level

import com.stovokor.ai.Navigation
import com.stovokor.ai.NavigationFactory

class NavigationGenerator extends Generator[Navigation](1, 1) {
  def generate(ctx: GeneratorContext) = {
    val spatials = (ctx.props ++ ctx.columns).map(_.spatial)
    new NavigationFactory().create(ctx.levelNode, spatials)
  }
}
