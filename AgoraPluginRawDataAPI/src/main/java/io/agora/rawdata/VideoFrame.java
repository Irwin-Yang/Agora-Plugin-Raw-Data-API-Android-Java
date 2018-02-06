package io.agora.rawdata;

/**
 * Created by yt on 2018/1/30/030.
 */

public class VideoFrame {
    public int frameType;
    public int width;  //width of video frame
    public int height;  //height of video frame
    public int yStride;  //stride of Y data buffer
    public int uStride;  //stride of U data buffer
    public int vStride;  //stride of V data buffer
    public byte[] yBuffer;  //Y data buffer
    public byte[] uBuffer;  //U data buffer
    public byte[] vBuffer;  //V data buffer
    public int rotation; // rotation of this frame (0, 90, 180, 270)
    public long renderTimeMs;
}