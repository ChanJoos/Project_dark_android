package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Authored 64x32 navigation grid for the classic Milles reagent-shop interior. */
public final class ReagentShopInteriorDef {
  public static final String MAP_ID="milles_interior_potion_shop";
  public static final String ENTRY_SPAWN_ID="entry_from_milles";
  public static final String EXIT_PORTAL_ID="reagent_shop_exit";
  public static final float MIN_X=64f,MAX_X=896f,MIN_Y=48f,MAX_Y=512f;
  public static final float SPAWN_X=448f,SPAWN_Y=432f;
  public static final float MERLIN_X=480f,MERLIN_Y=176f;
  public static final float EXIT_X=448f,EXIT_Y=464f;
  private ReagentShopInteriorDef(){}

  public static List<WorldMoveTargetController.TileCenter> navigationTiles(){
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    int row=0;
    for(float y=64f;y<=480f;y+=16f,row++){
      float offset=(row&1)==0?0f:32f;
      for(float x=96f+offset;x<=864f;x+=64f){
        if(y<224f)continue; // merchant side behind the straight counter is not traversable.
        out.add(new WorldMoveTargetController.TileCenter(x,y));
      }
    }
    return Collections.unmodifiableList(out);
  }
  public static boolean inside(float x,float y){return x>=MIN_X&&x<=MAX_X&&y>=MIN_Y&&y<=MAX_Y;}
  public static boolean exitContains(float x,float y){float dx=x-EXIT_X,dy=y-EXIT_Y;return dx*dx+dy*dy<=24f*24f;}
}