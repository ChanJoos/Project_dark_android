package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Compact authored 64x32 walkable shop floor. Screen/world coordinates are intentionally identical. */
public final class ReagentShopInteriorDef {
  public static final String MAP_ID="milles_interior_potion_shop";
  public static final String EXIT_PORTAL_ID="reagent_shop_exit";
  public static final float MIN_X=160f,MAX_X=800f,MIN_Y=132f,MAX_Y=492f;
  public static final float SPAWN_X=480f,SPAWN_Y=432f;
  public static final float MERLIN_X=480f,MERLIN_Y=208f;
  public static final float EXIT_X=480f,EXIT_Y=464f;
  public static final float COUNTER_Y=248f;
  private ReagentShopInteriorDef(){}
  public static List<WorldMoveTargetController.TileCenter> navigationTiles(){
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    int row=0;
    for(float y=272f;y<=464f;y+=16f,row++){
      float off=(row&1)==0?0f:32f;
      for(float x=224f+off;x<=736f;x+=64f)out.add(new WorldMoveTargetController.TileCenter(x,y));
    }
    return Collections.unmodifiableList(out);
  }
  public static boolean inside(float x,float y){return x>=MIN_X&&x<=MAX_X&&y>=272f&&y<=MAX_Y;}
  public static boolean exitContains(float x,float y){return Math.abs(x-EXIT_X)<.01f&&Math.abs(y-EXIT_Y)<.01f;}
}