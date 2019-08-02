package com.gq.mediaplayer;

import android.util.Log;
import android.view.SurfaceHolder;

import java.lang.reflect.Method;

public class ReflectionMediaplayer {

    private static final String TAG = "ReflectionMediaplayer";

    public static void setSubtitleSurface(final Object player, SurfaceHolder surfaceHolder){
        if((null != player) && (null != player.getClass())){
            try{
                Method func =  player.getClass().getMethod("setSubtitleSurface", SurfaceHolder.class);
                func.setAccessible(true);
                func.invoke(player, surfaceHolder);
                Log.e(TAG, "setSubtitleSurface --> exec ok...");
            } catch (Exception e) {
                Log.e(TAG, "setSubtitleSurface --> Fail ...NoSuchMethodException");
            }
        }
    }

}
