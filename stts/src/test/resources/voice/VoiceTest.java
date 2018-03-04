package com.stovokor.test.voice;

import java.beans.PropertyVetoException;
import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import org.junit.Test;

// Needs to install this manually:
// http://sourceforge.net/project/showfiles.php?group_id=42080
// And this with maven
//
// <dependency>
// <groupId>javax.speech</groupId>
// <artifactId>jsapi</artifactId>
// <version>1.0</version>
// </dependency>
// </dependencies>
// <repositories>
// <repository>
// <id>speech</id>
// <name>speech</name>
// <url>http://maven.it.su.se/it.su.se/maven2</url>
// </repository>
// <repository>
// <id>java.net-m2</id>
// <name>java.net - Maven 2</name>
// <url>https://repository.jboss.org/nexus/content/repositories/java.net-m2</url>
// </repository>
// </repositories>
public class VoiceTest {

    static class SpeechUtils {
        SynthesizerModeDesc desc;
        Synthesizer synthesizer;
        Voice voice;

        public void init(String voiceName) throws EngineException, AudioException,
                EngineStateError, PropertyVetoException {
            if (desc == null) {
                System.setProperty("freetts.voices",
                        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
                desc = new SynthesizerModeDesc(Locale.US);
                Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
                synthesizer = Central.createSynthesizer(desc);
                synthesizer.allocate();
                synthesizer.resume();
                SynthesizerModeDesc smd = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
                Voice[] voices = smd.getVoices();
                Voice voice = null;
                for (int i = 0; i < voices.length; i++) {
                    System.out.println("Voice: " + voices[i].getName());
                    if (voices[i].getName().equals(voiceName)) {
                        voice = voices[i];
                        break;
                    }
                }
                synthesizer.getSynthesizerProperties().setVoice(voice);
            }
        }

        public void terminate() throws EngineException, EngineStateError {
            synthesizer.deallocate();
        }

        public void doSpeak(String speakText) throws EngineException, AudioException,
                IllegalArgumentException, InterruptedException {
            synthesizer.speakPlainText(speakText, null);
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        }

    }

    @Test
    public void test() throws EngineException, AudioException, EngineStateError,
            PropertyVetoException, IllegalArgumentException, InterruptedException {
        SpeechUtils su = new SpeechUtils();
        // kevin, kevin16, mbrola_us1, mbrola_us2, or mbrola_us3
        su.init("kevin16");
        su.doSpeak("swaying to the symphony");
        Thread.sleep(300);
        su.doSpeak(".... of destruction!");
        su.terminate();
    }
}
