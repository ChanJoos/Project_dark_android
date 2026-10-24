package com.projectdark.mobile;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.io.*;

/** Non-launcher, explicit capture-only component. Normal MainActivity is untouched. */
public final class CaptureEncounterActivity extends Activity {
  private GameView game;private boolean started;
  @Override public void onCreate(Bundle saved){super.onCreate(saved);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);hide();
    try{
      F5mSaveStore.install(this);game=new GameView(this,true);game.setSkillTestMode(true);
      String report=CaptureMineEncounter.stage(game).toString(2);
      try(FileOutputStream out=openFileOutput("capture-encounter.json",MODE_PRIVATE)){out.write(report.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
      FrameLayout frame=new FrameLayout(this);frame.addView(game,new FrameLayout.LayoutParams(-1,-1));
      TextView label=new TextView(this);label.setText(CaptureMineEncounter.LABEL);label.setTextColor(Color.YELLOW);label.setBackgroundColor(0xCC151515);label.setTextSize(12);label.setPadding(8,3,8,3);
      FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);frame.addView(label,lp);
      Button start=new Button(this);start.setText("15마리 테스트 시작");FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(300,64,Gravity.CENTER);frame.addView(start,bp);
      // All15 are staged and visible while simulation is paused; this is not a combat freeze.
      start.setOnClickListener(v->{frame.removeView(start);started=true;game.resume();});setContentView(frame);
    }catch(Exception e){TextView error=new TextView(this);error.setTextColor(Color.WHITE);error.setBackgroundColor(Color.BLACK);error.setText("CAPTURE SETUP FAILED\n"+e);setContentView(error);game=null;}
  }
  private void hide(){getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
  @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)hide();}
  @Override protected void onResume(){super.onResume();hide();if(started&&game!=null)game.resume();}
  @Override protected void onPause(){if(started&&game!=null)game.pause();super.onPause();}
}
