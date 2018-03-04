package com.stovokor.gen.level.lightmap

import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.BatchNode
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.stovokor.util.jme.JmeExtensions._
import scala.collection.JavaConversions._

class LightmapGeometryBuilder {

  val mat = new Material
  val batch = new GeometryAccessBatchNode

  def add(spatial: Spatial) {
    val clone = spatial.clone(false)
    clone.setMaterial(mat)
    batch.attachChild(clone)
  }

  def build = {
    batch.geometry
  }

  class GeometryAccessBatchNode extends BatchNode {
    def geometry = {
      batch()
      val batches = this.batches //this.getClass.getDeclaredField("batches")
      val theBatch = batches.get(0)
      val geom = theBatch.getClass.getDeclaredField("geometry")
      geom.setAccessible(true)
      geom.get(theBatch).asInstanceOf[Geometry]
    }
  }
}