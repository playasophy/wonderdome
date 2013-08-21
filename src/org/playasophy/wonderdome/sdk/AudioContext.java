package org.playasophy.wonderdome.sdk;


import ddf.minim.*;
import ddf.minim.analysis.*;

import processing.core.PApplet;


public class AudioContext {


    ///// PROPERTIES /////

    public final AudioInput input;
    public final FFT fft;
    public final BeatDetect beats;



    ///// INITIALIZATION /////

    public AudioContext(final AudioInput input) {

        if ( input == null ) throw new IllegalArgumentException("Cannot construct audio context without audio input");

        this.input = input;

        fft = new FFT(input.bufferSize(), input.sampleRate());

        beats = new BeatDetect();
        beats.setSensitivity(200);

    }



    ///// PUBLIC METHODS /////

    /**
     * Updates the audio processing algorithms with the latest audio data.
     */
    public void update() {

        // Perform a Fourier Transform on the audio buffer.
        fft.forward(input.mix);

        // Run beat detection algorithm.
        beats.detect(input.mix);

    }

}
