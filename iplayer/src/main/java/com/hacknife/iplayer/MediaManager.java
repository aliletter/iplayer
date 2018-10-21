package com.hacknife.iplayer;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

/**
 * 这个类用来和jzvd互相调用，当jzvd需要调用Media的时候调用这个类，当MediaPlayer有回调的时候，通过这个类回调JZVD
 * Created by Nathen on 2017/11/18.
 */
public class MediaManager implements TextureView.SurfaceTextureListener {

    public static final String TAG = "JZVD";
    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_RELEASE = 2;

    public static PlayerTextureView textureView;
    public static SurfaceTexture savedSurfaceTexture;
    public static Surface surface;
    public static MediaManager jzMediaManager;
    public int positionInList = -1;
    public MediaInterface jzMediaInterface;
    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;

    public HandlerThread mMediaHandlerThread;
    public MediaHandler mMediaHandler;
    public Handler mainThreadHandler;

    public MediaManager() {
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
        mainThreadHandler = new Handler();
        if (jzMediaInterface == null)
            jzMediaInterface = new MediaSystem();
    }

    public static MediaManager instance() {
        if (jzMediaManager == null) {
            jzMediaManager = new MediaManager();
        }
        return jzMediaManager;
    }

    //这几个方法是不是多余了，为了不让其他地方动MediaInterface的方法
    public static void setDataSource(DataSource dataSource) {
        instance().jzMediaInterface.dataSource = dataSource;
    }

    public static DataSource getDataSource() {
        return instance().jzMediaInterface.dataSource;
    }


    //    //正在播放的url或者uri
    public static Object getCurrentUrl() {
        return instance().jzMediaInterface.dataSource == null ? null : instance().jzMediaInterface.dataSource.getCurrentUrl();
    }
//
//    public static void setCurrentDataSource(DataSource dataSource) {
//        instance().jzMediaInterface.dataSource = dataSource;
//    }

    public static long getCurrentPosition() {
        return instance().jzMediaInterface.getCurrentPosition();
    }

    public static long getDuration() {
        return instance().jzMediaInterface.getDuration();
    }

    public static void seekTo(long time) {
        instance().jzMediaInterface.seekTo(time);
    }

    public static void pause() {
        instance().jzMediaInterface.pause();
    }

    public static void start() {
        instance().jzMediaInterface.start();
    }

    public static boolean isPlaying() {
        return instance().jzMediaInterface.isPlaying();
    }

    public void releaseMediaPlayer() {
        mMediaHandler.removeCallbacksAndMessages(null);
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (VideoManager.getCurrentVideo() == null) return;
        Log.i(TAG, "onSurfaceTextureAvailable [" + VideoManager.getCurrentVideo().hashCode() + "] ");
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    public class MediaHandler extends Handler {
        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    currentVideoWidth = 0;
                    currentVideoHeight = 0;
                    jzMediaInterface.prepare();

                    if (savedSurfaceTexture != null) {
                        if (surface != null) {
                            surface.release();
                        }
                        surface = new Surface(savedSurfaceTexture);
                        jzMediaInterface.setSurface(surface);
                    }
                    break;
                case HANDLER_RELEASE:
                    jzMediaInterface.release();
                    break;
            }
        }
    }
}
