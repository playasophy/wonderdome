package org.playasophy.wonderdome;

import java.util.ArrayList;
import java.util.List;

import processing.core.*;


public class SimulatedPixelMatrix implements PixelMatrix {

    ///// CONSTANTS /////

    private final int numStrips = 6;
    private final int pixelsPerStrip = 240;



    ///// PROPERTIES /////

    private List<SimulatedStrip> simulatedStrips;



    ///// INITIALIZATION /////

    public SimulatedPixelMatrix(PApplet parent) {

        // Set the window size to a reasonably large display.
        parent.size(200, 980);

        simulatedStrips = new ArrayList<SimulatedStrip>(numStrips);
        int stripWidth = parent.width / numStrips;
        for ( int i = 0; i < numStrips; i++ ) {
            SimulatedStrip strip = new SimulatedStrip(parent, (i * stripWidth) + (stripWidth / 2), 0, pixelsPerStrip);
            simulatedStrips.add(strip);
        }
    }



    ///// PUBLIC METHODS /////

    public void draw() {
        for ( int s = 0; s < simulatedStrips.size(); s++ ) {
            simulatedStrips.get(s).draw();
        }
    }



    ///// PixelMatrix METHODS /////

    @Override
    public int getNumRows() {
        return numStrips;
    }

    @Override
    public int getNumColumns() {
        return pixelsPerStrip;
    }

    @Override
    public void setPixel(int row, int column, int color) {
        simulatedStrips.get(row).setPixel(color, column);
    }

}
