package com.stovokor.ai;

import com.jme3.ai.navmesh.NavMesh
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.Node
import scala.collection.JavaConversions._
import jme3tools.optimize.GeometryBatchFactory
import com.jme3.scene.Spatial
import com.jme3.scene.SceneGraphVisitor
import com.jme3.scene.shape.Box

class NavigationFactory {

  def create(level: Node, props: Set[Spatial]) = {
    val generator = initGenerator()
    val navMesh = new NavMesh()
    val mesh = new Mesh()

    val baseMesh = generateBaseMesh(level, props)
    val geometries = findGeometries(baseMesh)
    GeometryBatchFactory.mergeGeometries(geometries, mesh)
    val optiMesh = generator.optimize(mesh)
    val validMesh = Option(optiMesh).orElse(Some(mesh)).get

    navMesh.loadFromMesh(validMesh)

    new Navigation(navMesh, validMesh)
  }

  def initGenerator() = {
    val generator = new NavMeshGenerator()
    generator.setMinTraversableHeight(5f)
    generator.setTraversableAreaBorderSize(0.2f)
//    generator.setTraversableAreaBorderSize(1.5f)
    generator
  }

  def findGeometries(node: Node): Seq[Geometry] = {
    val lists = for (spatial <- node.getChildren()) yield if (spatial.isInstanceOf[Geometry]) {
      List(spatial.asInstanceOf[Geometry])
    } else if (spatial.isInstanceOf[Node]) {
      findGeometries(spatial.asInstanceOf[Node]);
    } else {
      List()
    }
    lists.flatten
  }

  def generateBaseMesh(level: Node, props: Set[Spatial]) = {
    val names = Set("Floor", "Wall")
    val baseNode = new Node("navMap")

    level.depthFirstTraversal(new SceneGraphVisitor() {
      def visit(spatial: Spatial) = {
        if (names.contains(spatial.getName())) {
          baseNode.attachChild(spatial.clone)
        }
      }
    })
    //    testNode.attachChild(levelNode)
    for (p <- props) baseNode.attachChild(p)
    for (i <- 0 to 40; j <- 0 to 40) {
      val testGeom = new Geometry("navGeom")
      testGeom.setMesh(new Box(10, 1, 10))
      testGeom.setLocalTranslation(10 * i, -1, 10 * j)
      baseNode.attachChild(testGeom)
    }
    baseNode
  }
}
