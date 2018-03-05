package io.agora.rtc.plugin.rawdata;

/**
 * Created by yt on 2018/3/5/005.
 */

public interface MediaDataAudioObserver {

    void onRecordAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength);

    void onPlaybackAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength);

    void onPlaybackAudioFrameBeforeMixing(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength);

    void onMixedAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength);
}
