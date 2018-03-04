package com.stovokor.math

import com.jme3.math.Vector3f

trait SimpleBounding extends SimpleCollidable {
	
	def getPosition():Vector3f
	def getRadius():Float

}