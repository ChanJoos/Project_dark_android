package com.projectdark.mobile.world;
import java.util.*;
/** Door thresholds measured in source-image pixels, transformed exactly as the foot renderer. */
public final class MillesDoorAnchors {
 public static final class Door {public final String building,id;public final float x,y,approachX,approachY;
  Door(String building,String id,float footX,float footY,float width,float height,float scale,float pixelX,float pixelY,float ax,float ay){this.building=building;this.id=id;x=footX+(pixelX-width/2)*scale;y=footY+(pixelY-height)*scale;approachX=ax;approachY=ay;}}
 public static final List<Door> ALL=Collections.unmodifiableList(Arrays.asList(
  new Door("potion_shop","potion_shop_door",320,420,347,265,1,98,216,256,368),
  new Door("weapon_shop","weapon_shop_door",760,300,338,266,1,97,216,704,240),
  new Door("general_shop","general_shop_door",1120,360,321,263,1,91,216,1056,320),
  new Door("church","church_door",1540,455,284,305,1.35f,85,281,1472,432)));
 public static Door forId(String id){for(Door d:ALL)if(d.id.equals(id)|| (d.id+"_2").equals(id))return d;return null;}
 public static boolean inDoorApproach(String building,float x,float y,float radius){for(Door d:ALL)if(d.building.equals(building))return x-radius>=d.x-40&&x+radius<=d.x+72&&y-radius>=d.y-28&&y+radius<=d.y+116;return false;}
 private MillesDoorAnchors(){}
}
