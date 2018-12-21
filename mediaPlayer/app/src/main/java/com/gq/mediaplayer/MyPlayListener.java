package com.gq.mediaplayer;

public interface MyPlayListener {
    void onStart(MyPlayer player);
    void onPause(MyPlayer player);
    void onResume(MyPlayer player);
    void onComplete(MyPlayer player);
}
