package io.agora.rtc.plugin.rawdatademo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.plugin.rawdata.MediaDataAudioObserver;
import io.agora.rtc.plugin.rawdata.MediaDataObserverPlugin;
import io.agora.rtc.plugin.rawdata.MediaDataVideoObserver;
import io.agora.rtc.plugin.rawdata.MediaPreProcessing;
import io.agora.rtc.video.VideoCanvas;

public class VideoChatViewActivity extends AppCompatActivity implements MediaDataAudioObserver, MediaDataVideoObserver {

    private static final String LOG_TAG = VideoChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 3;

    private MediaDataObserverPlugin mediaDataObserverPlugin;
    private int count = 0;

    private RtcEngine mRtcEngine;// Tutorial Step 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
                && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
            initAgoraEngineAndJoinChannel();
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        setupLocalVideo();           // Tutorial Step 3
        joinChannel();               // Tutorial Step 4
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
        if (mediaDataObserverPlugin != null) {
            mediaDataObserverPlugin.removeAudioObserver(this);
            mediaDataObserverPlugin.removeVideoObserver(this);
        }

    }



    public void onLocalCaptureClicked(View view) {
        if (mediaDataObserverPlugin != null) {
            mediaDataObserverPlugin.saveCaptureVideoShot("/sdcard/test/capture" + count + ".jpg");
            Toast.makeText(this, "Picture saved success /sdcard/test/capture" + count +".jpg", Toast.LENGTH_SHORT).show();
            count++;
        }
    }

    // Tutorial Step 9
    public void onLocalAudioMuteClicked(View view) {


        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 5
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    // Tutorial Step 6
    public void onEncCallClicked(View view) {
        finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mediaDataObserverPlugin = MediaDataObserverPlugin.the();
        MediaPreProcessing.setCallback(mediaDataObserverPlugin);
        MediaPreProcessing.setVideoCaptureByteBUffer(mediaDataObserverPlugin.byteBufferCapture);
        MediaPreProcessing.setVideoRenderByteBUffer(mediaDataObserverPlugin.byteBufferRender);
        MediaPreProcessing.setAudioRecordByteBUffer(mediaDataObserverPlugin.byteBufferAudioRecord);
        MediaPreProcessing.setAudioPlayByteBUffer(mediaDataObserverPlugin.byteBufferAudioPlay);
        MediaPreProcessing.setBeforeAudioMixByteBUffer(mediaDataObserverPlugin.byteBufferBeforeAudioMix);
        MediaPreProcessing.setAudioMixByteBUffer(mediaDataObserverPlugin.byteBufferAudioMix);
        mediaDataObserverPlugin.addVideoObserver(this);
        mediaDataObserverPlugin.addAudioObserver(this);

    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }

    // Tutorial Step 3
    private void setupLocalVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    // Tutorial Step 4
    private void joinChannel() {
        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }


    // Tutorial Step 7
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }


    @Override
    public void onCaptureVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, int rotation, long renderTimeMs) {
        Log.i(LOG_TAG, "onCaptureVideoFrame width :" + width + " height:" + height);
    }

    @Override
    public void onRenderVideoFrame(int frameType, int width, int height, int bufferLength, int yStride, int uStride, int vStride, int rotation, long renderTimeMs) {
        Log.i(LOG_TAG, "onRenderVideoFrame width :" + width + " height:" + height);
    }

    @Override
    public void onRecordAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength) {
        Log.i(LOG_TAG, "onRecordAudioFrame samples :" + samples + " bytesPerSample:" + bytesPerSample + " channels:" + channels + " samplesPerSec:" + samplesPerSec);

    }

    @Override
    public void onPlaybackAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength) {
        Log.i(LOG_TAG, "onPlaybackAudioFrame samples :" + samples + " bytesPerSample:" + bytesPerSample + " channels:" + channels + " samplesPerSec:" + samplesPerSec +" bufferLength: " + bufferLength);

    }

    @Override
    public void onPlaybackAudioFrameBeforeMixing(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength) {

    }

    @Override
    public void onMixedAudioFrame(int videoType, int samples, int bytesPerSample, int channels, int samplesPerSec, long renderTimeMs, int bufferLength) {

    }


}
