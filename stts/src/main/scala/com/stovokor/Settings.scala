package com.stovokor

import com.typesafe.config.ConfigFactory

object Settings {
  val main = ConfigFactory.load("settings.conf")

  object Debug {
    val config = main.getConfig("debug")
    val physics = config.getBoolean("physics")
    val navMesh = config.getBoolean("navMesh")
    val pathFinding = config.getBoolean("pathFinding")
    val disableEnemies = config.getBoolean("disableEnemies")
    val debugShots = config.getBoolean("debugShots")

    // Level gen
    val quadNormals = config.getBoolean("quadNormals")
    val disableLightmap = config.getBoolean("disableLightmap")
    val debugTexture = config.getBoolean("debugTexture")
    val disableCache = config.getBoolean("disableCache")
    val debugOcclussion = config.getBoolean("debugOcclussion")
  }

  object Lightmap {
    val config = main.getConfig("lightmap")
    val size = config.getInt("size")
  }

  object Graphics {
    val config = main.getConfig("graphics")
    val hardwareSkinning = config.getBoolean("hardwareSkinning")
    val bloom = config.getBoolean("bloom")
    val ssao = config.getBoolean("ssao")
  }

  object Physics {
    val config = main.getConfig("physics")
    val maxSubSteps = config.getInt("maxSubSteps")
    val parallel = config.getBoolean("parallel")
  }
}