package io.agora.rawdata;


public class MediaPreProcessing {
    static {
        System.loadLibrary("apm-plugin-media-rawdata");
    }

    public interface ProgressCallback {

        void onCaptureVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, byte[] buffer, int rotation, long renderTimeMs);

        void onRenderVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, byte[] buffer, int rotation, long renderTimeMs);

        void onRecordAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);

        void onPlaybackAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);

        void onPlaybackAudioFrameBeforeMixing(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);

        void onMixedAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);
    }

    public static native void setCallback(ProgressCallback callback);


}
