package com.projectdark.mobile;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private GameView game;
    private boolean startupFailed=false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUi();
        try {
            game = new GameView(this);
            setContentView(game);
            Toast.makeText(this, "이동: 빈 맵 터치 · 전투: 몬스터 선택 → ATK / SKILL / MAGIC", Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            startupFailed=true;
            TextView crash=new TextView(this);
            crash.setBackgroundColor(Color.rgb(18,18,18));
            crash.setTextColor(Color.WHITE);
            crash.setTextSize(16f);
            crash.setPadding(32,24,32,24);
            StringBuilder sb=new StringBuilder();
            sb.append("PROJECT DARK STARTUP CRASH\n\n");
            sb.append(t.getClass().getName()).append("\n");
            sb.append(String.valueOf(t.getMessage())).append("\n\n");
            StackTraceElement[] trace=t.getStackTrace();
            for(int i=0;i<trace.length&&i<12;i++) sb.append(trace[i].toString()).append("\n");
            crash.setText(sb.toString());
            setContentView(crash);
        }
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override protected void onResume() {
        super.onResume();
        hideSystemUi();
        if(!startupFailed&&game!=null)game.resume();
    }

    @Override protected void onPause() {
        if(!startupFailed&&game!=null)game.pause();
        super.onPause();
    }
}
