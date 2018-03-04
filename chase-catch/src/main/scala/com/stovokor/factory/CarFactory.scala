package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.bullet.control.VehicleControl
import com.jme3.bullet.util.CollisionShapeFactory
import com.jme3.input.InputManager
import com.jme3.math.FastMath
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.stovokor.control.CarControl
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.math.Vector3f
import com.jme3.renderer.queue.RenderQueue.Bucket

object CarFactory {

  val carWidth = 0.5f
  val carLength = 1f
  val carHeight = 0.2f

  def create(assetManager: AssetManager, inputManager: InputManager) = {
    val car = new Geometry("car", new Box(carWidth / 2f, carLength / 2f, carHeight / 2f))
    car.setMaterial(MaterialFactory.create(assetManager, "Textures/Car.png"))
    car.setQueueBucket(Bucket.Transparent);

    var carNode = new Node("car")
    carNode.attachChild(car)
    carNode.move(2, 2, 1)
    
    val carHull = CollisionShapeFactory.createDynamicMeshShape(car);
    var vehicleControl = createVehicleControl(carHull)
    carNode.addControl(vehicleControl)

    var carCtrl = CarControl(inputManager)
    carNode.addControl(carCtrl)
    carNode
  }

  def createVehicleControl(carHull: CollisionShape) = {
    val stiffness = 200.0f //200=f1 car
    val compValue = 0.2f //(lower than damp!)
    val dampValue = 0.3f
    val mass = 1000

    //Create a vehicle control
    val control = new VehicleControl(carHull, mass)

    //Setting default values for wheels
    control.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness))
    control.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness))
    control.setSuspensionStiffness(stiffness)
    control.setMaxSuspensionForce(10000)
    //Create four wheels and add them at their locations
    //note that our fancy car actually goes backwards..
    val wheelDirection = new Vector3f(0, 0, -1)
    val wheelAxle = new Vector3f(-1, 0, 0)
    val wheelRadius = .1f
    val suspensionLength = 0.05f
    val wheelOffset = new Vector3f(carWidth / 2f - 0.01f,
      carLength / 2f - 0.05f,
      -carHeight / 2f + wheelRadius - 0.01f)

    // Front left
    control.addWheel(
      wheelOffset.mult(new Vector3f(-1f, 1f, 1f)),
      wheelDirection,
      wheelAxle,
      suspensionLength,
      wheelRadius,
      true)

    // Front right
    control.addWheel(
      wheelOffset,
      wheelDirection,
      wheelAxle,
      suspensionLength,
      wheelRadius,
      true)

    // Back left
    control.addWheel(
      wheelOffset.mult(new Vector3f(-1f, -1f, 1f)),
      wheelDirection,
      wheelAxle,
      suspensionLength,
      wheelRadius,
      false)

    // Back right
    control.addWheel(
      wheelOffset.mult(new Vector3f(1f, -1f, 1f)),
      wheelDirection,
      wheelAxle,
      suspensionLength,
      wheelRadius,
      false)

    control.setRollInfluence(0, 0.1f)
    control.setRollInfluence(1, 0.1f)
    control.setRollInfluence(2, 0.1f)
    control.setRollInfluence(3, 0.1f)

    control.getWheel(0).setFrictionSlip(1)
    control.getWheel(1).setFrictionSlip(1)
    control.getWheel(2).setFrictionSlip(1.1f)
    control.getWheel(3).setFrictionSlip(1.1f)
    control
  }

}