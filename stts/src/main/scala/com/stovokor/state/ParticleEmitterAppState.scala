package com.stovokor.state

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import com.jme3.app.Application
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.collision.PhysicsRayTestResult
import com.jme3.math.Vector3f
import scala.collection.JavaConversions._
import com.jme3.bullet.PhysicsSpace
import java.util.concurrent.TimeUnit
import java.util.ArrayList
import com.jme3.effect.ParticleEmitter
import com.jme3.math.ColorRGBA
import com.jme3.material.Material
import com.stovokor.STTS
import com.jme3.effect.ParticleMesh.Type
import com.stovokor.util.jme.SurfaceHit
import com.stovokor.util.jme.GameEvent
import com.stovokor.util.jme.LogicEventListener
import com.stovokor.util.jme.EventHub
import com.jme3.math.Quaternion

/**
 * @author xuan
 */
object ParticleEmitterAppState extends AbstractAppState with LogicEventListener {

  val sparks = 20
  var sparkPool = List[ParticleEmitter]()

  var app: SimpleApplication = null
  var stateManager: AppStateManager = null
  def space = stateManager.getState(classOf[BulletAppState]).getPhysicsSpace

  override def initialize(appStateManager: AppStateManager, simpleApp: Application): Unit = {
    super.initialize(appStateManager, simpleApp)
    app = simpleApp.asInstanceOf[SimpleApplication]
    stateManager = appStateManager

    sparkPool = (for (i <- 1 to sparks) yield createSparkEmitter()).toList

    EventHub.subscribeByType(this, classOf[SurfaceHit])
  }

  def createSparkEmitter() = {
    val spark = new ParticleEmitter("Spark", Type.Triangle, 30)
    spark.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1.0f))
    spark.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f))
    spark.setStartSize(.5f)
    spark.setEndSize(.5f)
    spark.setFacingVelocity(true)
    spark.setParticlesPerSec(0)
    spark.setGravity(0, 20, 0)
    spark.setLowLife(0.4f) //1.1
    spark.setHighLife(0.8f) //1.5
    spark.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 20, 0))
    spark.getParticleInfluencer().setVelocityVariation(1)
    spark.setImagesX(1)
    spark.setImagesY(1)
    spark.setMaterial(sparkMaterial)
    app.getRootNode.attachChild(spark)
    spark
  }

  lazy val sparkMaterial = {
    val mat = new Material(STTS.getAssetManager, "Common/MatDefs/Misc/Particle.j3md")
    mat.setTexture("Texture", STTS.getAssetManager.loadTexture("Effects/Explosion/spark.png"))
    mat
  }

  def emitSpark(position: Vector3f, direction: Vector3f) = sparkPool match {
    case spark :: others => {
      spark.setLocalTranslation(position)
      spark.setLocalRotation(new Quaternion().fromAngleNormalAxis(0, direction))
      spark.emitAllParticles()
      sparkPool = others :+ spark
    }
    case _ =>
  }

  def onEvent(event: GameEvent) = {
    if (isEnabled) event match {
      case SurfaceHit("spark", position, direction) => {
        emitSpark(position, direction)
      }
      case _ =>
    }
  }

  override def update(tpf: Float) {
  }

  override def cleanup = {
    EventHub.removeFromAll(this)
  }
}