package com.hacknife.demo.CustomMediaPlayer;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;

import com.hacknife.iplayer.MediaInterface;
import com.hacknife.iplayer.MediaManager;
import com.hacknife.iplayer.VideoManager;

/**
 * Created by Nathen on 2017/11/23.
 */
public class CustomMediaPlayerAssertFolder extends MediaInterface implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    public MediaPlayer mediaPlayer;

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void prepare() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(CustomMediaPlayerAssertFolder.this);
            mediaPlayer.setOnCompletionListener(CustomMediaPlayerAssertFolder.this);
            mediaPlayer.setOnBufferingUpdateListener(CustomMediaPlayerAssertFolder.this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnSeekCompleteListener(CustomMediaPlayerAssertFolder.this);
            mediaPlayer.setOnErrorListener(CustomMediaPlayerAssertFolder.this);
            mediaPlayer.setOnInfoListener(CustomMediaPlayerAssertFolder.this);
            mediaPlayer.setOnVideoSizeChangedListener(CustomMediaPlayerAssertFolder.this);

            AssetFileDescriptor assetFileDescriptor = (AssetFileDescriptor) dataSource.getCurrentUrl();
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        mediaPlayer.seekTo((int) time);
    }

    @Override
    public void release() {
        if (mediaPlayer != null)
            mediaPlayer.release();
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        if (dataSource.getCurrentUrl().toString().toLowerCase().contains("mp3")) {
            MediaManager.instance().mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (VideoManager.getCurrentVideo() != null) {
                        VideoManager.getCurrentVideo().onPrepared();
                    }
                }
            });
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        MediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoManager.getCurrentVideo() != null) {
                    VideoManager.getCurrentVideo().onAutoCompletion();
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        MediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoManager.getCurrentVideo() != null) {
                    VideoManager.getCurrentVideo().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        MediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoManager.getCurrentVideo() != null) {
                    VideoManager.getCurrentVideo().onSeekComplete();
                }
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, final int what, final int extra) {
        MediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoManager.getCurrentVideo() != null) {
                    VideoManager.getCurrentVideo().onError(what, extra);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        MediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoManager.getCurrentVideo() != null) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        VideoManager.getCurrentVideo().onPrepared();
                    } else {
                        VideoManager.getCurrentVideo().onInfo(what, extra);
                    }
                }
            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        MediaManager.instance().currentVideoWidth = width;
        MediaManager.instance().currentVideoHeight = height;
        MediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (VideoManager.getCurrentVideo() != null) {
                    VideoManager.getCurrentVideo().onVideoSizeChanged();
                }
            }
        });
    }
}
