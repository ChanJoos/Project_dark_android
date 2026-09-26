package com.projectdark.mobile.world;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** [ADAPTED/B] First playable hunting field shell for canonical MAP_POTE_01 identity. */
public final class PoteFieldDef {
  public static final String MAP_ID="MAP_POTE_01";
  public static final float MIN_X=96f,MAX_X=864f,MIN_Y=64f,MAX_Y=512f;
  public static final float ENTRY_X=448f,ENTRY_Y=416f;
  public static final float EXIT_X=448f,EXIT_Y=480f;
  public static final float EXIT_RADIUS=36f;
  private PoteFieldDef(){}

  public static List<WorldMoveTargetController.TileCenter> navigationTiles(){
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    for(float y=96f;y<=480f;y+=32f)for(float x=128f;x<=832f;x+=64f){
      float px=x+((((int)((y-96f)/32f))&1)==0?0f:32f);
      if(px<=832f&&!blocked(px,y))out.add(new WorldMoveTargetController.TileCenter(px,y));
    }
    return Collections.unmodifiableList(out);
  }
  public static List<RectF> obstacles(){
    List<RectF> out=new ArrayList<>();
    out.add(new RectF(160f,128f,260f,210f));
    out.add(new RectF(700f,120f,800f,205f));
    out.add(new RectF(120f,330f,230f,410f));
    out.add(new RectF(730f,330f,840f,410f));
    return Collections.unmodifiableList(out);
  }
  public static boolean atExit(float x,float y){float dx=x-EXIT_X,dy=y-EXIT_Y;return dx*dx+dy*dy<=EXIT_RADIUS*EXIT_RADIUS;}
  private static boolean blocked(float x,float y){for(RectF r:obstacles())if(x>=r.left&&x<=r.right&&y>=r.top&&y<=r.bottom)return true;return false;}
}
