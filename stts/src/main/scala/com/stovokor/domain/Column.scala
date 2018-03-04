package com.stovokor.domain

import com.jme3.scene.Spatial
import com.jme3.bullet.control.RigidBodyControl

class Column(model: Spatial) extends Entity(model, model.getControl(classOf[RigidBodyControl]), Some(model)) {

}