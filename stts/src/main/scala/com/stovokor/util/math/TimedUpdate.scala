package com.stovokor.util.math

trait TimedUpdate {
  var lastCheck = 0L
  def canCheck = {
    val can = System.currentTimeMillis - lastCheck > checkSpan
    if (can) check
    can
  }
  def check { lastCheck = System.currentTimeMillis }
  def checkSpan: Long
}

class TimedChecker(function: () => Boolean, val checkSpan: Long = 100L) extends TimedUpdate {
  var lastResult = false

  def checkValue: Boolean = {
    if (canCheck) {
      lastResult = function()
    }
    lastResult
  }

}
