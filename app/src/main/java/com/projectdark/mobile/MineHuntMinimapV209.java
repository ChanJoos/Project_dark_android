package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
/** Static original-cell footprint; never the generic rectangular field texture. */
final class MineHuntMinimapV209 {
 static void drawTerrain(Canvas c,float minX,float minY,float maxX,float maxY){
  c.drawColor(0xff171411);Paint p=new Paint();p.setAntiAlias(false);HorrorCastleOriginalData.Grid g=MineHuntGeometry.grid();Path diamond=new Path();
  for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++){if(g.value(u,v,0)<=0)continue;float x=(MineHuntGeometry.x(u,v)-minX)*96/(maxX-minX),y=(MineHuntGeometry.y(u,v)-minY)*96/(maxY-minY),dx=32*96/(maxX-minX),dy=16*96/(maxY-minY);diamond.reset();diamond.moveTo(x,y-dy);diamond.lineTo(x+dx,y);diamond.lineTo(x,y+dy);diamond.lineTo(x-dx,y);diamond.close();p.setColor(g.walkable(u,v)?0xff887556:0xff41392c);c.drawPath(diamond,p);}
 }
 private MineHuntMinimapV209(){}
}
