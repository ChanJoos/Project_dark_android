package com.projectdark.mobile;

/** Live logical tile coordinates, not world/screen pixels; cache until the displayed integer changes. */
final class HudMapLocation {
  private String title,label;private int x=Integer.MIN_VALUE,y=Integer.MIN_VALUE;
  String label(String map,float worldX,float worldY){
    int nx=Math.round(worldX),ny=Math.round(worldY);
    if(label==null||!map.equals(title)||nx!=x||ny!=y){title=map;x=nx;y=ny;label=map+" ("+x+","+y+")";}
    return label;
  }
}
