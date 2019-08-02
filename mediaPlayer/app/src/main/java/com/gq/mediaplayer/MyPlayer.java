package com.gq.mediaplayer;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by gq.
 */

class MyPlayer implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnErrorListener, SurfaceHolder.Callback, MediaPlayer.OnInfoListener {

    private MediaPlayer player;
    private String source;
    private SurfaceHolder surfaceHolder;
    /**
     * 2019.3.4
     */
//    private SurfaceView subtitleSurface;
    private SurfaceHolder subtitleSurfaceHolder;
    private SurfaceView mSurfaceView;
    private MyPlayListener mPlayListener;
    private IMPlayerPrepared imPlayerPrepared;
    private ISeekCompleted iSeekCompleted;
    private IMPlayerSurfaceDestroyed imPlayerSurfaceDestroyed;
    private IMPlayerBufferUpdate imPlayerBufferUpdate;

    public void setSelectTrack(int index) {
        player.selectTrack(index);
    }

    public void desselectTrack(int index) {
        player.deselectTrack(index);
    }

    public Object getSelectTrack(int mediaType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                int pos = player.getSelectedTrack(mediaType);
                return pos;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private IMInfoListener imInfoListener;

    public void setMediaInfoListener(IMInfoListener imInfoListener) {
        this.imInfoListener = imInfoListener;
    }

    public interface IMInfoListener {
        boolean onMediaInfo(MediaPlayer mediaPlayer, int i, int i1);
    }

    public interface IMPlayerPrepared {
        void onMPlayerPrepare();
    }

    public interface ISeekCompleted {
        void onSeekCompleted();
    }

    public interface IMPlayerSurfaceDestroyed {
        void onSurfaceDestroyed();
    }

    public interface IMPlayerBufferUpdate {
        void onBufferUpdate(int percent);
    }

    public void setImPlayerBufferUpdate(IMPlayerBufferUpdate onPlayerBufferUpdate) {
        this.imPlayerBufferUpdate = onPlayerBufferUpdate;
    }

    public void setOnPrepared(IMPlayerPrepared onPrepare) {
        this.imPlayerPrepared = onPrepare;
    }

    public void setSeekCompleted(ISeekCompleted onSeekCompleted) {
        this.iSeekCompleted = onSeekCompleted;
    }

    public void setSurfaceDestroyed(IMPlayerSurfaceDestroyed onSurfaceDestroyed) {
        this.imPlayerSurfaceDestroyed = onSurfaceDestroyed;
    }

    private void createPlayerIfNeed() {
        if (null == player) {
            player = new MediaPlayer();
            player.setScreenOnWhilePlaying(true);
            player.setOnBufferingUpdateListener(this);
            player.setOnVideoSizeChangedListener(this);
            player.setOnCompletionListener(this);
            player.setOnPreparedListener(this);
            player.setOnSeekCompleteListener(this);
            player.setOnErrorListener(this);
            player.setOnInfoListener(this);
        }
    }

    public void seekto(int msec) {
        if (player != null)
            player.seekTo(msec);
    }

    public int getDuration() {
        int tmp = 0;
        try {
            tmp = player.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public int getCurrentPosition() {
        int pos = -1;
        if (player != null) {
            try {
                pos = player.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pos;
    }

    public void setPlayListener(MyPlayListener listener) {
        this.mPlayListener = listener;
    }

    public boolean isPlaying() {
        boolean flag = false;
        try {
            flag = player != null && player.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public void setDisplay(SurfaceView surfaceView) {
        if (this.surfaceHolder != null) {
            this.surfaceHolder.removeCallback(this);
            this.surfaceHolder = null;
        }
        this.mSurfaceView = surfaceView;
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
    }

    public void setSubtitleSurface(SurfaceView subtitleSurface) {

        if(subtitleSurfaceHolder!=null){
            this.subtitleSurfaceHolder = null;
        }
        subtitleSurface.setZOrderMediaOverlay(true);
        subtitleSurfaceHolder = subtitleSurface.getHolder();
        subtitleSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        subtitleSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                ReflectionMediaplayer.setSubtitleSurface( player, surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

    }

    public void onDestroy() {
        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int arg1, int arg2) {
        if (imInfoListener != null) {
            return imInfoListener.onMediaInfo(mediaPlayer, arg1, arg2);
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (imPlayerBufferUpdate != null)
            imPlayerBufferUpdate.onBufferUpdate(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onDestroy();
        if (mPlayListener != null) {
            mPlayListener.onComplete(this);
        }
    }

    public void onResume() {
        if (player != null) {
            try {
                player.start();
                if (mPlayListener != null) {
                    mPlayListener.onResume(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onPause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            if (mPlayListener != null) {
                mPlayListener.onPause(this);
            }
        }
    }

    private MediaPlayer.TrackInfo[] mtInfo = null;
    private String subtitleStr = null;

    @Override
    public void onPrepared(MediaPlayer mp) {
        mtInfo = player.getTrackInfo();
        player.setOnTimedTextListener(new MediaPlayer.OnTimedTextListener() {
            @Override
            public void onTimedText(MediaPlayer mediaPlayer, TimedText timedText) {
                if (onTimedMPTextListener != null) {
                    onTimedMPTextListener.setOnTimeMpTextListener(mediaPlayer, timedText);
                }
            }
        });

        imPlayerPrepared.onMPlayerPrepare();
        if (surfaceHolder == null) return;
        player.setDisplay(surfaceHolder);
        player.start();
        if (mPlayListener != null) {
            mPlayListener.onStart(this);
        }
        player.seekTo(currentPosition);
    }


    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (onVideoChangedListener != null) {
            boolean flag = false;
            if (height <= 1080 && height > 1000) {
                flag = true;
            }
            if (height >= 800 && height < 900) {
                flag = false;
            }
            onVideoChangedListener.OnVideoChanged(flag);
        }

        if (width > 0 && height > 0) {
            tryResetSurfaceSize(mSurfaceView, width, height);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (iSeekCompleted != null)
            iSeekCompleted.onSeekCompleted();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    private int currentPosition = 0;

    public void setSource(String url, String subtitleStr) {
        this.source = url;
        this.subtitleStr = subtitleStr;
        currentPosition = 0;
        createPlayerIfNeed();
        playVideo();
    }

    private void playVideo() {
        if (isCreated) {
            try {
                player.reset();
                if (TextUtils.isEmpty(source)) {
                    return;
                }
                player.setDataSource(source);
                try {
                    if (!TextUtils.isEmpty(subtitleStr)) {
                        //                        if (subTiTleStart)
                        player.addTimedTextSource(subtitleStr, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surfaceHolder != null) {
            createPlayerIfNeed();
            isCreated = true;
            player.setDisplay(surfaceHolder);
            playVideo();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private boolean isCreated = false;

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null && player.isPlaying()) {
            currentPosition = player.getCurrentPosition();
            player.reset();
            player.release();
            player = null;
            isCreated = false;
            imPlayerSurfaceDestroyed.onSurfaceDestroyed();
        }
    }

    public MediaPlayer.TrackInfo[] getTrackInfo() {
        if (mtInfo != null) {
            return mtInfo;
        } else {
            return null;
        }
    }

    public void setOnTimedMPTextListener(OnTimedMPTextListener onTimedMPTextListener) {
        this.onTimedMPTextListener = onTimedMPTextListener;
    }

    OnTimedMPTextListener onTimedMPTextListener;

    public interface OnTimedMPTextListener {
        void setOnTimeMpTextListener(MediaPlayer mediaPlayer, TimedText timedText);
    }

    OnVideoChangedListener onVideoChangedListener;

    public interface OnVideoChangedListener {
        void OnVideoChanged(boolean flag);
    }

    public void setOnVideoChangedListener(OnVideoChangedListener onVideoChangedListener) {
        this.onVideoChangedListener = onVideoChangedListener;
    }

    private boolean mIsCrop = false;

    //根据设置和视频尺寸，调整视频播放区域的大小
    private void tryResetSurfaceSize(final View view, int videoWidth, int videoHeight) {
        ViewGroup parent = (ViewGroup) view.getParent();
        int width = parent.getWidth();
        int height = parent.getHeight();
        if (width > 0 && height > 0) {
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (mIsCrop) {
                float scaleVideo = videoWidth / (float) videoHeight;
                float scaleSurface = width / (float) height;
                if (scaleVideo < scaleSurface) {
                    params.width = width;
                    params.height = (int) (width / scaleVideo);
                    params.setMargins(0, (height - params.height) / 2, 0, (height - params.height) / 2);
                } else {
                    params.height = height;
                    params.width = (int) (height * scaleVideo);
                    params.setMargins((width - params.width) / 2, 0, (width - params.width) / 2, 0);
                }
            } else {
                float scaleVideo = videoWidth / (float) videoHeight;
                float scaleSurface = width / (float) height;
                if (scaleVideo > scaleSurface) {
                    params.width = width;
                    params.height = (int) (width / scaleVideo);
                    params.setMargins(0, (height - params.height) / 2, 0, (height - params.height) / 2);
                } else {
                    params.height = height;
                    params.width = (int) (height * scaleVideo);
                    params.setMargins((width - params.width) / 2, 0, (width - params.width) / 2, 0);
                }
            }
            view.setLayoutParams(params);
        }
    }
}
