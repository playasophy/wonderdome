package org.playasophy.wonderdome;


public interface PixelMatrix {

    int getNumRows();

    int getNumColumns();

    void setPixel(int row, int column, int color);

}
