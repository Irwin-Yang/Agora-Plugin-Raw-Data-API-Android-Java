#include <jni.h>
#include <android/log.h>
#include <cstring>
#include "include/IAgoraMediaEngine.h"

#include "include/IAgoraRtcEngine.h"
#include <string.h>
#include "media_preprocessing_plugin_jni.h"
#include <unistd.h>
#include <malloc.h>
#include <pthread.h>

#define min(X, Y) ((X) < (Y) ? (X) : (Y))
#define max(X, Y) ((X) > (Y) ? (X) : (Y))

jobject gCallBack = nullptr;
jclass gCallbackClass = nullptr;
jmethodID recordAudioMethodId = nullptr;
jmethodID playbackAudioMethodId = nullptr;
jmethodID playBeforeMixAudioMethodId = nullptr;
jmethodID mixAudioMethodId = nullptr;
jmethodID captureVideoMethodId = nullptr;
jmethodID renderVideoMethodId = nullptr;

static JavaVM *gJVM = nullptr;

class AgoraVideoFrameObserver : public agora::media::IVideoFrameObserver {

public:
    AgoraVideoFrameObserver() {

    }

    ~AgoraVideoFrameObserver() {
    }

    void getVideoFrame(VideoFrame &videoFrame, _jmethodID *jmethodID) {
        int width = videoFrame.width;
        int height = videoFrame.height;
        int widthAndheight = width * height;
        int length = widthAndheight * 3 / 2;

        JNIEnv *env = nullptr;

        jint ret = gJVM->AttachCurrentThread(&env, nullptr);

        char *yuv = (char *) malloc(length);
        if (yuv != nullptr) {
            memcpy(yuv, videoFrame.yBuffer, widthAndheight);
            memcpy(yuv + widthAndheight, videoFrame.uBuffer, widthAndheight / 4);
            memcpy(yuv + widthAndheight * 5 / 4, videoFrame.vBuffer, widthAndheight / 4);

            jbyteArray array = env->NewByteArray(length);
            env->SetByteArrayRegion(array, 0, length, reinterpret_cast<jbyte *>(yuv));

            env->CallVoidMethod(gCallBack, jmethodID, videoFrame.type, width, height, length,
                                videoFrame.yStride, videoFrame.uStride,
                                videoFrame.vStride, array, videoFrame.rotation,
                                videoFrame.renderTimeMs);
            env->ReleaseByteArrayElements(array, env->GetByteArrayElements(array, 0), 0);
            env->DeleteLocalRef(array);
        }

        gJVM->DetachCurrentThread();

        free(yuv);
    }

public:
    virtual bool onCaptureVideoFrame(VideoFrame &videoFrame) override {
        getVideoFrame(videoFrame, captureVideoMethodId);
        return true;
    }

    virtual bool onRenderVideoFrame(unsigned int uid, VideoFrame &videoFrame) override {
        getVideoFrame(videoFrame, renderVideoMethodId);
        return true;
    }


};


class AgoraAudioFrameObserver : public agora::media::IAudioFrameObserver {

public:
    AgoraAudioFrameObserver() {
        gCallBack = nullptr;
    }

    ~AgoraAudioFrameObserver() {
    }

    void getAudioFrame(AudioFrame &audioFrame, _jmethodID *jmethodID) {
        JNIEnv *env = nullptr;
        jint ret = gJVM->AttachCurrentThread(&env, nullptr);
        if (env == nullptr) {
            return;
        }

        int len = audioFrame.samples * audioFrame.bytesPerSample;
        jbyteArray array = env->NewByteArray(len);
        env->SetByteArrayRegion(array, 0, len, (jbyte *) audioFrame.buffer);
        env->CallVoidMethod(gCallBack, jmethodID, audioFrame.type, audioFrame.samples,
                            audioFrame.bytesPerSample,
                            audioFrame.channels, audioFrame.samplesPerSec,
                            array, audioFrame.renderTimeMs);

        env->ReleaseByteArrayElements(array, env->GetByteArrayElements(array, 0), 0);
        env->DeleteLocalRef(array);
//        gJVM->DetachCurrentThread(); //jira : AE-1861
    }

public:
    virtual bool onRecordAudioFrame(AudioFrame &audioFrame) override {
        getAudioFrame(audioFrame, recordAudioMethodId);
        return true;
    }

    virtual bool onPlaybackAudioFrame(AudioFrame &audioFrame) override {
        getAudioFrame(audioFrame, playbackAudioMethodId);
        return true;
    }

    virtual bool
    onPlaybackAudioFrameBeforeMixing(unsigned int uid, AudioFrame &audioFrame) override {
        getAudioFrame(audioFrame, playBeforeMixAudioMethodId);
        return true;
    }

    virtual bool onMixedAudioFrame(AudioFrame &audioFrame) override {
        getAudioFrame(audioFrame, mixAudioMethodId);
        return true;
    }
};


static AgoraAudioFrameObserver s_audioFrameObserver;

static AgoraVideoFrameObserver s_videoFrameObserver;
static agora::rtc::IRtcEngine *rtcEngine = nullptr;

#ifdef __cplusplus
extern "C" {
#endif

int __attribute__((visibility("default")))
loadAgoraRtcEnginePlugin(agora::rtc::IRtcEngine *engine) {
    __android_log_print(ANDROID_LOG_ERROR, "plugin", "plugin loadAgoraRtcEnginePlugin");
    rtcEngine = engine;
    return 0;
}

void __attribute__((visibility("default")))
unloadAgoraRtcEnginePlugin(agora::rtc::IRtcEngine *engine) {
    __android_log_print(ANDROID_LOG_ERROR, "plugin", "plugin unloadAgoraRtcEnginePlugin");
    rtcEngine = nullptr;
}


JNIEXPORT void JNICALL
Java_io_agora_rawdata_MediaPreProcessing_setCallback(JNIEnv *env, jobject obj,
                                                        jobject callback) {
    if (!rtcEngine) return;

    env->GetJavaVM(&gJVM);

    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtcEngine, agora::INTERFACE_ID_TYPE::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {

        mediaEngine->registerVideoFrameObserver(&s_videoFrameObserver);
        mediaEngine->registerAudioFrameObserver(&s_audioFrameObserver);

    }

    if (gCallBack == nullptr) {
        gCallBack = env->NewGlobalRef(callback);
        gCallbackClass = env->GetObjectClass(gCallBack);
        recordAudioMethodId = env->GetMethodID(gCallbackClass, "onRecordAudioFrame", "(IIIII[BJ)V");
        playbackAudioMethodId = env->GetMethodID(gCallbackClass, "onPlaybackAudioFrame", "(IIIII[BJ)V");
        playBeforeMixAudioMethodId = env->GetMethodID(gCallbackClass,
                                                      "onPlaybackAudioFrameBeforeMixing", "(IIIII[BJ)V");
        mixAudioMethodId = env->GetMethodID(gCallbackClass, "onMixedAudioFrame", "(IIIII[BJ)V");

        captureVideoMethodId = env->GetMethodID(gCallbackClass, "onCaptureVideoFrame", "(IIIIIII[BIJ)V");
        renderVideoMethodId = env->GetMethodID(gCallbackClass, "onRenderVideoFrame", "(IIIIIII[BIJ)V");
        __android_log_print(ANDROID_LOG_ERROR, "setCallback", "Callback is set successfully");
    }
}

#ifdef __cplusplus
}
#endif
