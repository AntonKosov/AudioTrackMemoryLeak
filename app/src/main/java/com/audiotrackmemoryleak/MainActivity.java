package com.audiotrackmemoryleak;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaSync;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    private Surface mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurface = holder.getSurface();
                createMediaSync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurface = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSurface != null)
            createMediaSync();
    }

    private void createMediaSync() {
        final MediaSync mediaSync = new MediaSync();
        mediaSync.setSurface(mSurface);
        final Surface inputSurface = mediaSync.createInputSurface();
        final MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
        final MediaCodec mediaCodec;
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        mediaCodec.configure(videoFormat, inputSurface, null, 0);

        final AudioTrack audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(48000)
                        .setChannelMask(12)
                        .build())
                .build();
        mediaSync.setAudioTrack(audioTrack);

        mediaSync.release();
        mediaCodec.release();
        inputSurface.release();
        audioTrack.release();

        Toast.makeText(this, "AudioTrack has been released", Toast.LENGTH_SHORT).show();
    }
}