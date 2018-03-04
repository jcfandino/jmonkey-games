package com.stovokor.domain

import com.jme3.bullet.BulletAppState
import com.jme3.bullet.collision.PhysicsRayTestResult
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.collision.CollisionResults
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Ray
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Spatial
import com.jme3.scene.debug.Arrow
import com.stovokor.STTS
import scala.collection.JavaConversions._
import com.stovokor.domain.enemy.EnemyControl
import com.jme3.math.Quaternion
import com.stovokor.util.math.Dist
import com.stovokor.util.math.Random
import com.jme3.effect.ParticleEmitter
import com.jme3.effect.ParticleMesh.Type
import com.stovokor.Settings.Debug
import com.stovokor.state.CollisionGroups
import java.util.ArrayList
import com.stovokor.util.jme.EventHub
import com.stovokor.util.jme.SurfaceHit

sealed abstract class Technology(val s: String) {
  def checkHit(physicsState: BulletAppState, location: Vector3f, direction: Vector3f, spec: TechSpecs)
  def ammoType: Ammo
  override def toString = s
}

abstract trait DirectHitTechnology {
  def checkHit(physicsState: BulletAppState, location: Vector3f, direction: Vector3f, spec: TechSpecs) {
    println(s"Checking Hit. Location: $location Direction: $direction")
    val rayTest = physicsState
      .getPhysicsSpace().rayTest(location, location.add(direction.mult(spec.maxRange)), new ArrayList())
    if (rayTest.size() > 0) {
      validateHit(location, direction, sort(rayTest.toList), spec)
    }
    // Debug
    if (Debug.debugShots) {
      val mark = initMark
      mark.setLocalTranslation(location)
      val q = new Quaternion();
      q.lookAt(direction, Vector3f.UNIT_Y);
      mark.setLocalRotation(q);
      STTS.getRootNode.attachChild(mark)
    }
  }

  def validateHit(location: Vector3f, direction: Vector3f, result: List[PhysicsRayTestResult], spec: TechSpecs): Boolean = {
    if (!result.isEmpty) {
      val node: Spatial = headAsSpatial(result)
      lazy val distance = node.getLocalTranslation.distance(location)
      println("shoot: " + node.getName)
      node.getName match {
        case NodeId.enemy => {
          // Revalidate
          if (isCollisionValid(location, direction, node)) {
            doDamage(distance, direction, node.getControl(classOf[EnemyControl]), spec)
          } else {
            validateHit(location, direction, result.tail, spec)
          }
        }
        case NodeId.player => {
          doDamage(distance, direction, node.getControl(classOf[PlayerControl]), spec)
        }
        case _ => {
          checkHitWithSolidObject(node, location, direction)
        }
      }
      true
    }
    false
  }

  def doDamage(distance: Float, direction: Vector3f, victim: CanBeHit, spec: TechSpecs) = {
    victim.receiveHit((spec.damage - spec.damageLoss(distance)).toInt, direction)
  }

  def checkHitWithSolidObject(node: Spatial, location: Vector3f, direction: Vector3f) = {
    val body = node.getControl(classOf[RigidBodyControl])
    if (body != null) {
      val result = testCollision(location, direction, node)
      if (result.size > 0) {
        val contact = result.getClosestCollision.getContactPoint
        val local = node.worldToLocal(contact.clone, new Vector3f)
        val normal = result.getClosestCollision.getContactNormal
        body.applyImpulse(direction.normalize.mult(5f), local)

        addParticles(contact, normal)
        if (Debug.debugShots) {
          val mark = initMark
          mark.setLocalTranslation(contact)
          val q = new Quaternion();
          q.lookAt(normal, Vector3f.UNIT_Y);
          mark.setLocalRotation(q);
          STTS.getRootNode.attachChild(mark)
        }
      }
    }
  }

  def testCollision(point: Vector3f, direction: Vector3f, node: Spatial) = {
    val ray = new Ray(point, direction)
    val result = new CollisionResults
    node.collideWith(ray, result)
    result
  }

  def isCollisionValid(point: Vector3f, direction: Vector3f, node: Spatial) = {
    testCollision(point, direction, node).size > 0
  }

  def sort(list: List[PhysicsRayTestResult]) = {
    list
      .sortBy(n => n.getHitFraction)
      .filter(p => p.getCollisionObject.getUserObject.isInstanceOf[Spatial])
      .filter(p => (p.getCollisionObject.getCollisionGroup & CollisionGroups.items) == 0)
  }

  def headAsSpatial(list: List[PhysicsRayTestResult]) = {
    list.head.getCollisionObject.getUserObject.asInstanceOf[Spatial]
  }

  // For debug
  def initMark: Spatial = {
    val arrow = new Arrow(Vector3f.UNIT_Z.mult(2f));
    arrow.setLineWidth(3);
    val mark = new Geometry("BOOM!", arrow);
    val mark_mat = new Material(STTS.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mark_mat.setColor("Color", ColorRGBA.Red);
    mark.setMaterial(mark_mat);
    mark
  }

  def addParticles(location: Vector3f, normal: Vector3f) {
    EventHub.trigger(SurfaceHit("spark", location, normal))
  }

}

case object Pistol extends Technology("Pistol") with DirectHitTechnology {
  val ammoType = PistolBullet

}
case object Rifle extends Technology("Rifle") with DirectHitTechnology {
  val ammoType = RifleBullet

}
case object AutomaticRifle extends Technology("Automatic") with DirectHitTechnology {
  val ammoType = RifleBullet

}
case object Shotgun extends Technology("Shotgun") with DirectHitTechnology {
  val ammoType = Shell
  // TODO Shotgun should no be direct hit (ray test).
  // Instead check hit with a cone
  override def checkHit(physicsState: BulletAppState, location: Vector3f, direction: Vector3f, spec: TechSpecs) {
    for (i <- 0 to 10) {
      super.checkHit(physicsState, location, deviatedDir(direction, spec), spec)
    }
  }
  val rnd = Random()
  def deviatedDir(direction: Vector3f, spec: TechSpecs) = {
    val dev = spec.openingAngle
    direction.add(Dist.normalFloat(rnd, -dev, dev),
      Dist.normalFloat(rnd, -dev, dev),
      Dist.normalFloat(rnd, -dev, dev))
  }

}
case object RocketLauncher extends Technology("RPG") {
  val ammoType = Rocket
  def checkHit(physicsState: BulletAppState, location: Vector3f, direction: Vector3f, spec: TechSpecs) {}
}
case object Railgun extends Technology("Railgun") with DirectHitTechnology {
  val ammoType = Energy

}
case object Plasma extends Technology("Plasmagun") {
  val ammoType = Energy
  def checkHit(physicsState: BulletAppState, location: Vector3f, direction: Vector3f, spec: TechSpecs) {}
}

sealed abstract class Ammo(val s: String, val max: Int, val default: Int) {
  override def toString = s
}
case object PistolBullet extends Ammo("pistol-bullet", 200, 200) // TODO 10
case object RifleBullet extends Ammo("rifle-bullet", 400, 400) //TODO - 20
case object Shell extends Ammo("shell", 100, 100) // TODO
case object Energy extends Ammo("energy", 500, 25)
case object Rocket extends Ammo("rocket", 50, 2)

case class TechSpecs(
    val delayTime: Int, // milliseconds between shots
    val precision: Float => Float, // f(triggerTime)
    val maxRange: Float, // max damage distance
    val damage: Float, // damage per shot
    val damageLoss: Float => Float, // f(distance)
    val openingAngle: Float, // for shotguns
    val ammoPerShot: Int, // when multicannon or energy based
    val automatic: Boolean // if needs to pull the trigger for each shot
    ) {

}
