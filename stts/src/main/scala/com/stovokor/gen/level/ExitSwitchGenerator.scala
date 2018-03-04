package com.stovokor.gen.level

import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.texture.Texture.WrapMode
import com.stovokor.domain.NodeId
import com.stovokor.domain.Switch
import com.stovokor.domain.SwitchControl
import com.stovokor.domain.SwitchId
import com.stovokor.gen.level.quest.Quest
import com.stovokor.util.jme.JmeExtensions.SpatialExtensions
import com.jme3.math.Quaternion
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.FastMath
import com.jme3.util.TangentBinormalGenerator

class ExitSwitchGenerator(seed: Long, number: Int)(implicit val assetManager: AssetManager) extends Generator[Set[Switch]](seed, number) {

  def generate(ctx: GeneratorContext) = {
    val quest = ctx.quest.get
    val levelNode = ctx.levelNode
    val switchAdvA = generateTestSwitch(quest, SwitchId.levelExitA, levelNode)
    val switchAdvB = generateTestSwitch(quest, SwitchId.levelExitB, levelNode)
    val switchBack = generateTestSwitch(quest, SwitchId.levelBack, levelNode)
    Set(switchAdvA, switchAdvB, switchBack)
  }
  def generateTestSwitch(quest: Quest, switchId: String, levelNode: Spatial) = {
    val ver = quest.verts.find(v => v.exit.isDefined && v.exit.get.switchId == switchId)
    val nodeId = ver.get.room.id + "-exit"
    var switch: Switch = null
    levelNode.depthFirst(s => {
      if (s.getName == nodeId) {
        println(s"Exit goes here $nodeId - ${s.getLocalTranslation}")
        //        switch = createSwitchObject(s)
        switch = createElevatorDoor(s)
      }
    })
    def createElevatorDoor(s: Spatial) = {
      val control = new SwitchControl(switchId)
      val node = new Node(NodeId.switch)
      val asset = "Models/elevator-door/elevator-door.j3o"
      val model = assetManager.loadModel(asset)
      node.attachChild(model)
      val wx: Float = s.getUserData("x")
      val wy: Float = s.getUserData("y")
      val wz: Float = s.getUserData("z")
      val wd1: Float = s.getUserData("d1")
      val wd2: Float = s.getUserData("d1")
      val wnx: Float = s.getUserData("nx")
      val wnz: Float = s.getUserData("nz")
      node.setMaterial(doorMaterial)
      node.setLocalTranslation(wx , wy, wz )
      node.lookAt(new Vector3f(wx,wy,wz).addLocal(wnx,0,wnz), Vector3f.UNIT_Y)
      node.addControl(new RigidBodyControl(0))
      node.addControl(control)
      TangentBinormalGenerator.generate(model)
      new Switch(node, control)
    }
    switch

  }

  lazy val doorMaterial = {
    val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    material.setTexture("DiffuseMap", assetManager.loadTexture("Models/elevator-door/elevator-door-diffuse.png"))
    material.setFloat("Shininess", 50)
    material.setTexture("GlowMap", assetManager.loadTexture("Models/elevator-door/elevator-door-glow.png"));
    material
  }
  lazy val debugMaterial = {
    val material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", assetManager.loadTexture("Debug1.png"));
    material.getTextureParam("ColorMap").getTextureValue().setWrap(WrapMode.Repeat)
    material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off)
    material
  }
}