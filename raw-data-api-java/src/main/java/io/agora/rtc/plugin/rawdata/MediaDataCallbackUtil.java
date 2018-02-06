package io.agora.rtc.plugin.rawdata;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by yt on 2018/2/1/001.
 */

public class MediaDataCallbackUtil implements MediaPreProcessing.ProgressCallback {

    private List<MediaDataObserver> observerList = new ArrayList<>();

    private static MediaDataCallbackUtil myAgent = null;
    private boolean beCaptureVideoShot = false;
    private boolean beRenderVideoShot = false;
    private String captureFilePath = null;
    private String renderFilePath = null;

    public static MediaDataCallbackUtil the() {
        if (myAgent == null) {
            synchronized (MediaDataCallbackUtil.class) {
                if (myAgent == null)
                    myAgent = new MediaDataCallbackUtil();
            }

        }
        return myAgent;
    }

    public void addObserverListerner(MediaDataObserver observer) {
        observerList.add(observer);
    }

    public void removeObserverListener(MediaDataObserver observer) {
        observerList.remove(observer);
    }

    public void saveCaptureVideoShot(String filePath) {
        beCaptureVideoShot = true;
        captureFilePath = filePath;
    }

    public void saveRenderVideoShot(String filePath) {
        beRenderVideoShot = true;
        renderFilePath = filePath;
    }


    @Override
    public void onCaptureVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, byte[] buffer, int rotation, long renderTimeMs) {

        for (MediaDataObserver observer : observerList) {
            observer.onCaptureVideoFrame(frameType, width, height, bufferLength, yStride, uStride, vStride, buffer, rotation, renderTimeMs);
        }
        if (beCaptureVideoShot) {
            beCaptureVideoShot = false;
            getVideoShot(width, height, bufferLength, buffer, captureFilePath);

        }
    }

    @Override
    public void onRenderVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, byte[] buffer, int rotation, long renderTimeMs) {
        for (MediaDataObserver observer : observerList) {
            observer.onRenderVideoFrame(frameType, width, height, bufferLength, yStride, uStride, vStride, buffer, rotation, renderTimeMs);
        }
        if (beRenderVideoShot) {
            beRenderVideoShot = false;
            getVideoShot(width, height, bufferLength, buffer, renderFilePath);
        }
    }

    @Override
    public void onRecordAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs) {
        for (MediaDataObserver observer : observerList) {
            observer.onRecordAudioFrame(videoType, samples, bytesPerSample, channels, samplesPerSec, buffer, renderTimeMs);
        }
    }

    @Override
    public void onPlaybackAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs) {
        for (MediaDataObserver observer : observerList) {
            observer.onPlaybackAudioFrame(videoType, samples, bytesPerSample, channels, samplesPerSec, buffer, renderTimeMs);
        }
    }

    @Override
    public void onPlaybackAudioFrameBeforeMixing(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs) {
        for (MediaDataObserver observer : observerList) {
            observer.onPlaybackAudioFrameBeforeMixing(videoType, samples, bytesPerSample, channels, samplesPerSec, buffer, renderTimeMs);
        }
    }

    @Override
    public void onMixedAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs) {
        for (MediaDataObserver observer : observerList) {
            observer.onMixedAudioFrame(videoType, samples, bytesPerSample, channels, samplesPerSec, buffer, renderTimeMs);
        }
    }

    private void getVideoShot(int width, int height, int bufferLength, byte[] buffer, String filePath) {
        File file = new File(filePath);

        byte[] NV21 = new byte[bufferLength];
        swapYV12toYUV420SemiPlanar(buffer, NV21, width, height);

        YuvImage image = new YuvImage(NV21, ImageFormat.NV21, width, height, null);

        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        image.compressToJpeg(
                new Rect(0, 0, image.getWidth(), image.getHeight()),
                70, fos);
    }


    private void swapYV12toYUV420SemiPlanar(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        int startPos = width * height;
        int yv_start_pos_u = startPos;
        int yv_start_pos_v = startPos + startPos / 4;
        for (int i = 0; i < startPos / 4; i++) {
            i420bytes[startPos + 2 * i + 0] = yv12bytes[yv_start_pos_v + i];
            i420bytes[startPos + 2 * i + 1] = yv12bytes[yv_start_pos_u + i];
        }
    }

    public interface MediaDataObserver {
        void onCaptureVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, byte[] buffer, int rotation, long renderTimeMs);

        void onRenderVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, byte[] buffer, int rotation, long renderTimeMs);

        void onRecordAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);

        void onPlaybackAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);

        void onPlaybackAudioFrameBeforeMixing(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);

        void onMixedAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, byte[] buffer, long renderTimeMs);
    }
}
