package com.stovokor.state

import scala.util.Random

object CaptainMoodState {

  var currentMood: CaptainMood = CaptainMood.Energized()
  var currentPhrase = ""

  var nextMood: CaptainMood = CaptainMood.Energized()
  var nextPhrase = ""

  var lastSet = 0L
  var lastUpdate = -1001L

  def set(m: CaptainMood) {
    val now = System.currentTimeMillis
    // don't change the mood too fast
    if (now - lastSet > 1000) {
      nextMood = m
      nextPhrase = Random.shuffle(m.phrases).head
      lastSet = now
    }
  }

  def mood = {
    checkIfUpdateNeeded
    currentMood
  }
  def phrase = {
    checkIfUpdateNeeded
    currentPhrase
  }

  def checkIfUpdateNeeded {
    if (lastUpdate != lastSet && System.currentTimeMillis - lastUpdate > 1000) {
      currentMood = nextMood
      currentPhrase = nextPhrase
      lastUpdate = System.currentTimeMillis
    }
  }

}

abstract class CaptainMood(val label: String, val phrases: List[String]) {

}
object CaptainMood {

  def all = List(
    Happy(), Dissapointed(), Confused(), Sad(), Surprised(), Mad(), Energized())

  case class Happy() extends CaptainMood("win",
    List("Yipi!",
      "Best Captain ever!",
      "Wesley, I'm gonna screw your mother tonight",
      "Oh Beverly! I'm gonna show you the Picard Maneuver",
      "I love the sound of exploding ships... in space"))

  case class Dissapointed() extends CaptainMood("facepalm",
    List("Wesley, you finally managed to kill us all",
      "I give up, this crew is retarded",
      "What do they teach in the Accademy these days?"))

  case class Confused() extends CaptainMood("cantsee",
    List("Didn't I tell you to shoot that Warbird?",
      "Where did they go?",
      "Who let the shrink into the bridge?",
      "Not a battle cruiser you say?",
      "Is that a phaser rifle in your pocket, or are you glad to see me?"))

  case class Sad() extends CaptainMood("sad",
    List("I miss assimilating things",
      "I don't like being shot",
      "Shall we die together?",
      "That's enough Data"))

  case class Surprised() extends CaptainMood("surprise",
    List("Tea. Earl Grey. Hot.",
      "Well done Mr. Worf",
      "I find that proper",
      "Thank you Mr. Data",
      "I don't always fart. But when I do is right before being transported"))

  case class Mad() extends CaptainMood("why",
    List("Why don't you kill more romu's?",
      "Why don't you move your ass?",
      "What the hell is wrong with you?",
      "Wesley, shut up!",
      "Didn't I just say evasive maneuvers?",
      "Oh come on!"))

  case class Energized() extends CaptainMood("armed",
    List("Fire at Will",
      "Make it so",
      "Make the Federation Great Again!",
      "C'mon bitches",
      "I say kill'em. Now!"))
}
