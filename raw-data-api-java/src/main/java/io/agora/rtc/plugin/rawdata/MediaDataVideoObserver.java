package io.agora.rtc.plugin.rawdata;

/**
 * Created by yt on 2018/3/5/005.
 */

public interface MediaDataVideoObserver {

    void onCaptureVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, int rotation, long renderTimeMs);

    void onRenderVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, int rotation, long renderTimeMs);
}
