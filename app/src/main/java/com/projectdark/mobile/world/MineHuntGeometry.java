package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Matching original tunnel 2102. Preserve original cells rather than stretching its shape. */
public final class MineHuntGeometry {
 public static final String ID="MAP_MINE_HUNT_3";
 public static boolean contains(String id){return ID.equals(id);}
 public static float x(int u,int v){return 3400+(u-v)*32;}
 public static float y(int u,int v){return 160+(u+v)*16;}
 public static WorldMoveTargetController.TileCenter at(int u,int v){return new WorldMoveTargetController.TileCenter(x(u,v),y(u,v));}
 public static HorrorCastleOriginalData.Grid grid(){return MineHuntData.grid();}
 public static WorldMoveTargetController.TileCenter entry(){return at(87,37);}
 public static WorldMoveTargetController.TileCenter exit(){return at(86,37);}
 public static boolean inside(float x,float y){int u=Math.round((y-160)/32+(x-3400)/64),v=Math.round((y-160)/32-(x-3400)/64);return grid().walkable(u,v);}
 private static List<WorldMoveTargetController.TileCenter> nav,ground;
 private static List<RectF> obstacles;
 public static List<WorldMoveTargetController.TileCenter> tiles(boolean navigation){if(navigation&&nav!=null)return nav;if(!navigation&&ground!=null)return ground;List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();for(int v=0;v<grid().height;v++)for(int u=0;u<grid().width;u++)if(!navigation||grid().walkable(u,v))out.add(at(u,v));if(navigation)nav=Collections.unmodifiableList(out);else ground=Collections.unmodifiableList(out);return navigation?nav:ground;}
 public static List<RectF> obstacles(){if(obstacles!=null)return obstacles;List<RectF> out=new ArrayList<>();for(int v=0;v<grid().height;v++)for(int u=0;u<grid().width;u++)if(!grid().walkable(u,v))out.add(new RectF(x(u,v)-13,y(u,v)-6,x(u,v)+13,y(u,v)+6));return obstacles=Collections.unmodifiableList(out);}
 private MineHuntGeometry(){}
}
