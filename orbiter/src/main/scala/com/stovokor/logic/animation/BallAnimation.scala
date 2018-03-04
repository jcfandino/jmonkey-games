package com.stovokor.logic.animation

import scala.collection.JavaConversions.asScalaBuffer

import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.stovokor.GameContext
import com.stovokor.domain.ColorBall

abstract class BallDieAnimation(ball: ColorBall, node: Node) extends Animation {

  override def kill = {
    node.detachChild(ball.getSpatial())
    KilledAnimation
  }
}

class BallExplodeAnimation(ball: ColorBall, node: Node) extends BallDieAnimation(ball, node) {

  def updated(tpf: Float) = {
    if (ball.getSpatial().getLocalScale().dot(Vector3f.UNIT_XYZ) < 12f) {
      val node = ball.getSpatial().asInstanceOf[Node];
      node.getChildren().foreach(s => s.scale(1.2F))
      this
    } else {
      kill
    }
  }

}

class BallImplodeAnimation(ball: ColorBall, node: Node, context: GameContext) extends BallDieAnimation(ball, node) {

  def updated(tpf: Float) = {
    if (ball.getSpatial().getLocalScale().dot(Vector3f.UNIT_XYZ) > 0.1) {
      val node = ball.getSpatial().asInstanceOf[Node];
      val shipPos = context.getShip().getSpatial().getLocalTranslation();
      node.move(shipPos.subtract(node.getLocalTranslation()).mult(0.2f));
      node.getChildren().foreach(s => s.scale(0.9f))
      this
    } else {
      kill;
    }
  }
}