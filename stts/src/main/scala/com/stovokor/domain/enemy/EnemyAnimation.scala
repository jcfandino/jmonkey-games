package com.stovokor.domain.enemy

import com.jme3.animation.AnimControl
import com.jme3.animation.AnimEventListener
import com.jme3.scene.Spatial
import com.jme3.animation.AnimChannel
import com.jme3.scene.Node
import com.jme3.animation.LoopMode
import com.jme3.animation.SkeletonControl

class AnimChannels(charModel: Spatial) {
  val bodyIndex = 0
  val armIndex = 1

  def getModelGeometry = charModel
    .asInstanceOf[Node].getChild("skeleton")

  def getAnimControl = getModelGeometry
    .getControl(classOf[AnimControl])

  def getSkeletonControl = getModelGeometry
    .getControl(classOf[SkeletonControl])

  def getBodyChannel = getAnimControl.getChannel(bodyIndex)
  def getArmChannel = getAnimControl.getChannel(armIndex)

  def setBodyDefaultAnimation(name: String) {
    val l = new AnimEventListener() {
      def onAnimCycleDone(ctrl: AnimControl, chn: AnimChannel, n: String) {
        chn.setAnim(name)
        chn.setLoopMode(LoopMode.Loop)
      }
      def onAnimChange(ctrl: AnimControl, chn: AnimChannel, n: String) {}
    }
    getBodyChannel.getControl().addListener(l)
  }
}
