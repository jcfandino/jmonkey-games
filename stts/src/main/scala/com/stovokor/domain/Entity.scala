package com.stovokor.domain

import com.jme3.scene.Spatial
import com.jme3.scene.control.Control
import com.stovokor.util.jme.LogicEventListener
import scala.reflect.ClassTag
import scala.reflect._

class Entity[C <: Control](
  val spatial: Spatial,
  val control: C,
  val physicsObject: Option[Spatial] = None,
  val subscribeToEvents: (LogicEventListener => Unit) = (_ => {})) {

}