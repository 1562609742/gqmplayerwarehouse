package com.gq.mediaplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayActivity extends Activity implements
        SeekBar.OnSeekBarChangeListener, MyPlayer.OnTimedMPTextListener,
        View.OnClickListener, MyPlayer.OnVideoChangedListener, MyPlayer.IMInfoListener {

    private SurfaceView mPlayerView;
    private MyPlayer player;
    private TSeekBar seekBar;
    private RelativeLayout updateProgressBar;
    private ImageButton btn_play_pause;
    private TextView movieName, tv_time_total, tv_time_current, subtitle_text, text_stop;
    private RelativeLayout playerControlTitleLayout, playerControlBottomLayout;
    private ImageView paly_btn, audio_track, subtitle_track, second_ad_show, load_img;
    private RelativeLayout second_ad_show_layout;
    private AlertDialog dialog = null;

    private static final int HIDE = 1, SHOW = 2, FINISH = 3, ENABLE_SEEK = 4, ENABLE_PLAY = 5, ADSHOWTIME = 6, ADHIDE = 7;
    private int timeout = 8000;
    private int videoDuration;
    private List<TrackBean> audioList = null;
    private List<TrackBean> subtitleList = null;
    private int[] ind = null;

    private String[] name = null;
    private Timer timer;
    private TimerTask timerTask;
    private String videoUri = "/sdcard/test.mkv";
    private String subtitleSrtOrAss = "/sdcard/test.srt";

    private Handler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_myplayer);
        initView();
        initData();
        initPlayer();
        getIntentData();
    }

    private void getIntentData() {
        String uri = getIntent().getStringExtra("videoUri");
        if (!TextUtils.isEmpty(uri)) {
            videoUri = uri;
        }
        String subtitleUri = getIntent().getStringExtra("subUri");
        if (!TextUtils.isEmpty(subtitleUri)) {
            subtitleSrtOrAss = subtitleUri;
        }
        if (!TextUtils.isEmpty(videoUri)) {
            starPlayUrl(videoUri, subtitleSrtOrAss);
        }
    }

    private void initView() {
        playerControlBottomLayout = findViewById(R.id.player_control_bottom_layout);
        paly_btn = findViewById(R.id.control_btn);
        seekBar = findViewById(R.id.seekBar);
        updateProgressBar = findViewById(R.id.update_progress);
        audio_track = findViewById(R.id.audio_track_sel);
        subtitle_track = findViewById(R.id.subtitle_track_sel);
        playerControlTitleLayout = findViewById(R.id.player_control_title_layout);
        movieName = findViewById(R.id.movie_name);
        mPlayerView = findViewById(R.id.mPlayerView);
        btn_play_pause = findViewById(R.id.btn_play_pause);
        tv_time_total = findViewById(R.id.tv_time_total);
        tv_time_current = findViewById(R.id.tv_time_current);
        subtitle_text = findViewById(R.id.subtitle_text);
        load_img = findViewById(R.id.load_img);
        second_ad_show_layout = findViewById(R.id.second_ad_show_layout);
        second_ad_show = findViewById(R.id.second_ad_show);
        text_stop = findViewById(R.id.text_stop);
        seekBar.setOnSeekBarChangeListener(this);
        audio_track.setOnClickListener(this);
        subtitle_track.setOnClickListener(this);
        paly_btn.setOnClickListener(this);
    }

    private void initData() {
        Utils.init(VideoPlayActivity.this);
        Animation translateAnimation = AnimationUtils.loadAnimation(this, R.anim.load_anim);
        load_img.setAnimation(translateAnimation);
        load_img.startAnimation(translateAnimation);
    }

    private void initPlayer() {
        player = new MyPlayer();
        player.setOnVideoChangedListener(this);
        player.setOnTimedMPTextListener(this);
        player.setMediaInfoListener(this);
        playerOnPrepared();
        playerSurfaceDestroyed();
        playerOnBufferUpdateListen();
        seedCompletedListen();
    }

    private void playerSurfaceDestroyed() {
        player.setSurfaceDestroyed(new MyPlayer.IMPlayerSurfaceDestroyed() {
            @Override
            public void onSurfaceDestroyed() {
                mHandler.removeCallbacksAndMessages(null);
            }
        });
    }

    private void seedCompletedListen() {
        player.setSeekCompleted(new MyPlayer.ISeekCompleted() {
            @Override
            public void onSeekCompleted() {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
            }
        });
    }

    private void playerSetListen() {
        player.setPlayListener(new MyPlayListener() {
            @Override
            public void onStart(MyPlayer player) {
            }

            @Override
            public void onPause(MyPlayer player) {
            }

            @Override
            public void onResume(MyPlayer player) {
            }

            @Override
            public void onComplete(MyPlayer player) {
                mHandler.removeCallbacksAndMessages(null);
                exit();
            }
        });
    }

    private void hideSeekbar() {
        if (player != null) {
            if (player.isPlaying()) {
                playerControlTitleLayout.setVisibility(View.GONE);
                playerControlBottomLayout.setVisibility(View.GONE);
                btn_play_pause.setVisibility(View.GONE);
            }
        }
    }

    private void showSeekbar() {
        playerControlTitleLayout.setVisibility(View.VISIBLE);
        playerControlBottomLayout.setVisibility(View.VISIBLE);
        if (player != null) {
            if (!player.isPlaying()) {
                btn_play_pause.setVisibility(View.VISIBLE);
            }
        }
    }

    private void starPlayUrl(String mUrl, String subtitleStr) {
        if (!TextUtils.isEmpty(mUrl)) {
            if (mUrl.length() > 0) {
                try {
                    player.setSource(mUrl, subtitleStr);
                    player.setDisplay(mPlayerView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isTimerTask = false;

    private void playerOnPrepared() {

        player.setOnPrepared(new MyPlayer.IMPlayerPrepared() {
            @Override
            public void onMPlayerPrepare() {
                MediaPlayer.TrackInfo[] mtInfo = player.getTrackInfo();
                encapsulatedOrbit(mtInfo);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(ENABLE_SEEK), timeout);//播放器准备好后开始计时5秒内不允许快进
                playerSetListen();
                videoDuration = player.getDuration();
                movieName.setText("电影名称");
                seekBar.setMax(videoDuration);
                seekBar.setProgress(player.getCurrentPosition());
                String time = StringUtils.generateTime(videoDuration);
                tv_time_current.setText("00:00");
                tv_time_total.setText(" / " + time);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (isTimerTask) {
                            if (timer != null)
                                timer.cancel();
                        }
                        if (player != null) {
                            final int currentPosition = player.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                            VideoPlayActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String time = StringUtils.generateTime(currentPosition);
                                    tv_time_current.setText(time);
                                    seekBar.setText(time);
                                }
                            });
                        }
                    }
                };
                timer.schedule(timerTask, 0, 100);
            }
        });
    }

    private void playerOnBufferUpdateListen() {
        player.setImPlayerBufferUpdate(new MyPlayer.IMPlayerBufferUpdate() {
            @Override
            public void onBufferUpdate(final int percent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (videoDuration > 0) {
                            seekBar.setSecondaryProgress((int) ((percent / 100.0) * videoDuration));
                        }
                    }
                });
            }
        });
    }

    /**
     * 封装track信息
     */
    private void encapsulatedOrbit(MediaPlayer.TrackInfo[] mtInfo) {
        audioList = new ArrayList<>();
        subtitleList = new ArrayList<>();
        if (mtInfo != null && mtInfo.length > 0) {
            /**添加默认选项*/
            TrackBean disabletb = new TrackBean();
            disabletb.setIndex(-1);
            disabletb.setSubtitleName("Disable");
            subtitleList.add(disabletb);
            for (int i = 0; i < mtInfo.length; i++) {
                MediaPlayer.TrackInfo info = mtInfo[i];
                if (null == info) continue;
                if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                    TrackBean tb = new TrackBean();
                    tb.setIndex(i);
                    tb.setAudioName(info.getLanguage());
                    audioList.add(tb);
                } else if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
                    TrackBean tb = new TrackBean();
                    tb.setIndex(i);
                    tb.setSubtitleName(info.getLanguage());
                    subtitleList.add(tb);
                } else if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                    TrackBean tb = new TrackBean();
                    tb.setIndex(i);
                    tb.setSubtitleName(info.getLanguage());
                    subtitleList.add(tb);
                }
            }
            if (!(audioList.size() > 0)) {
                audio_track.setFocusable(false);
                audio_track.setImageResource(R.mipmap.vioce_hui);
            } else {
                audio_track.setFocusable(true);
                player.setSelectTrack(audioList.get(0).getIndex());
                audio_track.setImageResource(R.drawable.audio_control_bg);
                audioSelIndex = 0;
            }

            if (!(subtitleList.size() > 1)) {
                subtitle_track.setFocusable(false);
                subtitle_track.setImageResource(R.mipmap.zimu_hui);
            } else {
                subtitle_track.setFocusable(true);
                player.setSelectTrack(subtitleList.get(1).getIndex());
                subtitle_track.setImageResource(R.drawable.subtitle_control_bg);
                subtitleSelIndex = 1;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
            player = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (playIndexTimer != null) {
            playIndexTimer.cancel();
            playIndexTimer = null;
        }
        load_img.clearAnimation();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.seekto(seekBar.getProgress());
        String time = StringUtils.generateTime(player.getCurrentPosition());
        tv_time_current.setText(time);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
    }

    private long mExitTime;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        mHandler.sendEmptyMessage(SHOW);
                        View view = getCurrentFocus();
                        if (updateProgressBar != null && updateProgressBar.getVisibility() == View.VISIBLE) {
                            updateProgressBar.setVisibility(View.GONE);
                        }
                        if (view == null || view.getId() == R.id.seekBar || view.getId() == R.id.btn_play_pause) {
                            if (player.isPlaying()) {
                                player.onPause();
                                paly_btn.setImageResource(R.drawable.player_control_pause_sel);
                                btn_play_pause.setImageResource(R.mipmap.mediacontroller_play);
                                btn_play_pause.setVisibility(View.VISIBLE);
                            } else {
                                try {
                                    player.onResume();
                                    paly_btn.setImageResource(R.drawable.player_control_playing_sel);
                                    btn_play_pause.setImageResource(R.mipmap.mediacontroller_pause);
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.sendEmptyMessage(HIDE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        mHandler.sendEmptyMessage(SHOW);
                        if (playerControlBottomLayout.getVisibility() == View.VISIBLE) {
                            View focusViewRight = getCurrentFocus();
                            if (focusViewRight != null)
                                if (focusViewRight.getId() == R.id.seekBar) {
                                    if (player != null) {
                                        player.seekto(player.getCurrentPosition() + 5000);
                                    }
                                    return true;
                                }
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        mHandler.sendEmptyMessage(SHOW);
                        if (playerControlBottomLayout.getVisibility() == View.VISIBLE) {
                            View focusViewLeft = getCurrentFocus();
                            if (focusViewLeft != null)
                                if (focusViewLeft.getId() == R.id.seekBar) {
                                    if (player != null) {
                                        player.seekto(player.getCurrentPosition() - 5000);
                                    }
                                    return true;
                                }
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        mHandler.sendEmptyMessage(SHOW);
                        if (playerControlBottomLayout.getVisibility() == View.VISIBLE) {
                            View focusViewDown = getCurrentFocus();
                            if (focusViewDown != null)
                                if (focusViewDown.getId() == R.id.seekBar)
                                    paly_btn.requestFocus();
                        }
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if ((System.currentTimeMillis() - mExitTime) > 2000) {
                            Toast.makeText(this, "再按一次退出播放", Toast.LENGTH_SHORT).show();
                            mExitTime = System.currentTimeMillis();
                            return true;
                        } else {
                            exit();
                        }
                        return true;
                    case KeyEvent.KEYCODE_HOME:
                        return true;
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 外挂字幕展示
     **/
    @Override
    public void setOnTimeMpTextListener(MediaPlayer mediaPlayer, TimedText timedText) {
        if (timedText != null) {
            subtitle_text.setText(timedText.getText());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.control_btn:
                if (updateProgressBar != null && updateProgressBar.getVisibility() == View.VISIBLE) {
                    updateProgressBar.setVisibility(View.GONE);
                }
                if (player.isPlaying()) {
                    player.onPause();
                    paly_btn.setImageResource(R.drawable.player_control_pause_sel);
                    btn_play_pause.setImageResource(R.mipmap.mediacontroller_play);
                    btn_play_pause.setVisibility(View.VISIBLE);
                } else {
                    try {
                        player.onResume();
                        paly_btn.setImageResource(R.drawable.player_control_playing_sel);
                        btn_play_pause.setImageResource(R.mipmap.mediacontroller_pause);
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.sendEmptyMessage(HIDE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.audio_track_sel:
                if (dialog != null && dialog.isShowing())
                    dismissDialog();
                else {
                    ind = null;
                    name = null;
                    if (audioList != null) {
                        ind = new int[audioList.size()];
                        name = new String[audioList.size()];
                        for (int i = 0; i < audioList.size(); i++) {
                            ind[i] = audioList.get(i).getIndex();
                            name[i] = audioList.get(i).getAudioName();
                        }
                        showDialog("音轨选择", name, audioSelIndex, "audioType");
                    }
                }
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
                break;
            case R.id.subtitle_track_sel:
                if (dialog != null && dialog.isShowing())
                    dismissDialog();
                else {
                    ind = null;
                    name = null;
                    if (subtitleList != null) {
                        ind = new int[subtitleList.size()];
                        name = new String[subtitleList.size()];
                        for (int i = 0; i < subtitleList.size(); i++) {
                            ind[i] = subtitleList.get(i).getIndex();
                            name[i] = subtitleList.get(i).getSubtitleName();
                        }
                        showDialog("字幕选择", name, subtitleSelIndex, "subtitleType");
                    }
                }
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
                break;
        }
    }

    private int audioSelIndex = -1;
    private int subtitleSelIndex = -1;

    private void showDialog(String title, String[] arrayStr, final int currentPos, final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(arrayStr, currentPos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    if (i == 0) {
                        player.desselectTrack(ind[currentPos]);
                    } else {
                        player.setSelectTrack(ind[i]);
                    }
                    if ("subtitleType".equals(type)) {
                        subtitleSelIndex = i;
                    }
                    if ("audioType".equals(type)) {
                        audioSelIndex = i;
                    }
                    dismissDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    private void dismissDialog() {
        dialog.dismiss();
    }

    @Override
    public void OnVideoChanged(boolean flag) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        subtitle_text.setLayoutParams(params);
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        if (flag) {
            params.setMargins(0, 0, 0, 10);
        } else {
            params.setMargins(0, 0, 0, 100);
        }
    }

    @Override
    public boolean onMediaInfo(MediaPlayer mediaPlayer, int arg1, int arg2) {
        switch (arg1) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                //开始缓存，暂停播放
                updateProgressBar.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                //缓存完成，继续播放
                if (player.isPlaying())
                    updateProgressBar.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    private class MyHandler extends Handler {
        private WeakReference<VideoPlayActivity> reference;

        public MyHandler(VideoPlayActivity reference) {
            this.reference = new WeakReference<VideoPlayActivity>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HIDE:
                    hideSeekbar();
                    break;
                case SHOW:
                    showSeekbar();
                    break;
                case FINISH:
                    exit();
                    break;
                case ENABLE_SEEK:
                    break;
                case ENABLE_PLAY:
                    try {
                        player.onResume();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ADSHOWTIME:
                    int ob = (int) msg.obj;
                    int time = ob / 1000;
                    text_stop.setText(time + "");
                    break;
            }
        }
    }

    protected void exit() {
        isTimerTask = true;
        mHandler.removeCallbacksAndMessages(null);
        isTimerTask = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (player != null) {
            player.onDestroy();
            player = null;
        }
        finish();
        overridePendingTransition(0, 0);
    }

    private Timer playIndexTimer;

//    @Override
//    public void onAttachedToWindow() {
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//        super.onAttachedToWindow();
//    }

}