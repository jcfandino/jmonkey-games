package com.stovokor.factory

import com.jme3.asset.AssetManager
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer.Type
import com.jme3.scene.shape.Box
import com.jme3.texture.Texture
import com.jme3.util.BufferUtils
import com.stovokor.control.ExplosionControl
import com.jme3.material.RenderState.BlendMode

object ExplosionFactory {

  val radius = 1f 

  def create(assetManager: AssetManager, pos: Vector3f) = {
    val bam = new Geometry("explosion", createMesh)
    bam.setLocalTranslation(pos)

    val mat = MaterialFactory.create(assetManager, "Textures/Sheets/Explosion.png")
    bam.setMaterial(mat)
    bam.setQueueBucket(Bucket.Transparent);

    bam.addControl(new ExplosionControl(4, 5, 2f))
    bam
  }

  def createMesh = {
    val m = new Mesh
    m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(
      new Vector3f(-radius, -radius, 0f),
      new Vector3f(radius, -radius, 0f),
      new Vector3f(-radius, radius, 0f),
      new Vector3f(radius, radius, 0f)))

    m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(
      new Vector2f(0f, 0f),
      new Vector2f(1f, 0f),
      new Vector2f(0f, 1f),
      new Vector2f(1f, 1f)))

    //    println(s"Texture O($ox,$oy) E($ex,$ey)")
    m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(2, 0, 1, 1, 3, 2))
    val normal = Vector3f.UNIT_Z
    m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(
      normal, normal, normal, normal))
    m.updateBound()
    m
  }
}