package io.agora.rawdata;

/**
 * Created by yt on 2018/1/30/030.
 */

public class AudioFrame {
    int videoType;
    int samples;  //number of samples in this frame
    int bytesPerSample;  //number of bytes per sample: 2 for PCM16
    int channels;  //number of channels (data are interleaved if stereo)
    int samplesPerSec;  //sampling rate
    byte[] buffer;  //data buffer
    long renderTimeMs;

}
