package com.stovokor.bomber.factory

import com.jme3.asset.AssetManager
import com.jme3.input.InputManager
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.stovokor.bomber.control.PlaneControl
import com.jme3.scene.shape.Quad
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.jme3.math.Vector3f

object PlayerFactory {

  val planeLength = 250f / 100f
  val planeTall = 87f / 100f

  def create(assetManager: AssetManager, inputManager: InputManager) = {
    val quad = new Quad(planeLength, planeTall)
    val plane = new Geometry("plane", quad)
    plane.setMaterial(MaterialFactory.create(assetManager, "Textures/planes/plane4.png"))
    plane.setLocalTranslation(-planeLength / 2f - 0.2f, -planeTall / 3f, 0)
    plane.setQueueBucket(Bucket.Transparent);

    val planeNode = new Node("plane-node")
    planeNode.attachChild(plane)
    planeNode.move(2, 6, .25f)

    val shape = new CapsuleCollisionShape(.3f * planeTall / 2f, .65f * planeLength, 0)
    val body = new RigidBodyControl(shape, 1)
    body.setKinematic(true)
    planeNode.addControl(body)

    val planeCtrl = PlaneControl(inputManager)
    planeNode.addControl(planeCtrl)
    planeNode
  }

}