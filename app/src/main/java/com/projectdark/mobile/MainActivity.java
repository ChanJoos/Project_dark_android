package com.projectdark.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;

public final class MainActivity extends Activity {
    private GameView game;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);game=new GameView(this);setContentView(game);}
    @Override protected void onResume(){super.onResume();game.resume();}
    @Override protected void onPause(){game.pause();super.onPause();}
}
