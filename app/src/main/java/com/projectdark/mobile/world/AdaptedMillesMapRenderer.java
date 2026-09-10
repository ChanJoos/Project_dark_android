package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * [ADAPTED]/[B] evidence-safe renderer for the current Milles prototype map.
 * Geometric fallback is intentionally distinct from unverified original Nexon art.
 */
public final class AdaptedMillesMapRenderer {
  private final Paint fill=new Paint();
  private final Paint edge=new Paint();
  private final Path path=new Path();

  public AdaptedMillesMapRenderer(){
    fill.setStyle(Paint.Style.FILL);fill.setAntiAlias(false);
    edge.setStyle(Paint.Style.STROKE);edge.setStrokeWidth(1f);edge.setAntiAlias(false);edge.setColor(0x55312B24);
  }

  /** Draw TILE, decoration, then collision-aligned static structures. Dynamic entities remain separately owned. */
  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    drawTiles(canvas,world);
    drawDecorations(canvas,world);
    drawObjects(canvas,world);
  }

  public void drawTiles(Canvas canvas,WorldRuntimeAdapter world){
    float margin=AdaptedMillesIsometricTileLayer.TILE_WIDTH;
    for(AdaptedMillesIsometricTileLayer.Tile tile:world.map().tiles()){
      WorldCameraTransform.Point c=world.worldToScreen(tile.centerX,tile.centerY);
      if(c.x<-margin||c.x>canvas.getWidth()+margin||c.y<-margin||c.y>canvas.getHeight()+margin)continue;
      fill.setColor(tileColor(tile.kind,tile.variant));
      diamond(c.x,c.y,AdaptedMillesIsometricTileLayer.HALF_WIDTH,AdaptedMillesIsometricTileLayer.HALF_HEIGHT);
      canvas.drawPath(path,fill);canvas.drawPath(path,edge);
      drawTransitionEdges(canvas,c.x,c.y,tile.transitionMask);
    }
  }

  public void drawDecorations(Canvas canvas,WorldRuntimeAdapter world){
    for(AdaptedMillesDecorationLayer.Decoration d:world.map().decorations()){
      WorldCameraTransform.Point foot=world.worldToScreen(d.footX,d.footY);
      if(foot.x+d.width<0||foot.x-d.width>canvas.getWidth()||foot.y<0||foot.y-d.height>canvas.getHeight())continue;
      float l=foot.x-d.width*.5f,r=foot.x+d.width*.5f,t=foot.y-d.height,b=foot.y;
      switch(d.kind){
        case TREE:
          fill.setColor(0xff493a2e);canvas.drawRect(foot.x-6,t+d.height*.48f,foot.x+6,b,fill);
          fill.setColor(0xff3f5941);canvas.drawCircle(foot.x,t+d.height*.38f,d.width*.48f,fill);
          edge.setColor(0xff293b2d);canvas.drawCircle(foot.x,t+d.height*.38f,d.width*.48f,edge);break;
        case FENCE:
          edge.setColor(0xff57483a);edge.setStrokeWidth(6f);canvas.drawLine(l,foot.y-8,r,foot.y-8,edge);
          canvas.drawLine(l,foot.y-18,l,foot.y+2,edge);canvas.drawLine(r,foot.y-18,r,foot.y+2,edge);edge.setStrokeWidth(1f);break;
        case SIGN:
          fill.setColor(0xff5b4736);canvas.drawRect(foot.x-4,t+18,foot.x+4,b,fill);
          fill.setColor(0xff786044);canvas.drawRect(l,t,r,t+25,fill);canvas.drawRect(l,t,r,t+25,edge);break;
        case WELL:
          fill.setColor(0xff625c52);canvas.drawCircle(foot.x,foot.y-d.height*.45f,d.width*.5f,fill);
          edge.setColor(0xff34383a);canvas.drawCircle(foot.x,foot.y-d.height*.45f,d.width*.30f,edge);break;
        case BENCH:
          fill.setColor(0xff684f38);canvas.drawRect(l,t+d.height*.35f,r,b-5,fill);
          canvas.drawRect(l+8,b-8,l+13,b+4,fill);canvas.drawRect(r-13,b-8,r-8,b+4,fill);break;
        case LAMP:
          fill.setColor(0xff3e3d39);canvas.drawRect(foot.x-3,t+12,foot.x+3,b,fill);
          fill.setColor(0xffffd88a);canvas.drawCircle(foot.x,t+10,8,fill);break;
        case BUSH:
          fill.setColor(0xff40533d);canvas.drawCircle(foot.x-d.width*.22f,foot.y-d.height*.42f,d.height*.42f,fill);
          canvas.drawCircle(foot.x+d.width*.22f,foot.y-d.height*.40f,d.height*.44f,fill);
          fill.setColor(0xff53654b);canvas.drawCircle(foot.x,foot.y-d.height*.55f,d.height*.38f,fill);break;
        case GATEPOST:
          fill.setColor(0xff5d584f);canvas.drawRect(foot.x-d.width*.34f,t+12f,foot.x+d.width*.34f,b,fill);
          edge.setColor(0xff35322e);canvas.drawRect(foot.x-d.width*.34f,t+12f,foot.x+d.width*.34f,b,edge);
          fill.setColor(0xff726a5d);path.reset();path.moveTo(l,t+14f);path.lineTo(foot.x,t);path.lineTo(r,t+14f);path.lineTo(r,t+24f);path.lineTo(l,t+24f);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);break;
      }
      edge.setColor(0x55312B24);
    }
  }

  /**
   * Draws prototype buildings as layered roof/wall/door silhouettes instead of blocker rectangles.
   * Footprints remain identical to WorldDef collision; only presentation extends upward.
   */
  public void drawObjects(Canvas canvas,WorldRuntimeAdapter world){
    for(AdaptedMillesObjectLayer.ObjectInstance object:world.map().renderObjects()){
      WorldCameraTransform.Point tl=world.worldToScreen(object.collisionLeft,object.collisionTop);
      WorldCameraTransform.Point br=world.worldToScreen(object.collisionRight,object.collisionBottom);
      AdaptedMillesStructureVisualLayer.Visual visual=AdaptedMillesStructureVisualLayer.byStructureId(object.id);
      float wallHeight=visual==null?44f:visual.wallHeight;
      float roofRise=visual==null?20f:visual.roofRise;
      float overhang=visual==null?8f:visual.roofOverhang;
      if(br.x+overhang<0f||tl.x-overhang>canvas.getWidth()||br.y<0f||tl.y-wallHeight-roofRise>canvas.getHeight())continue;
      drawStructure(canvas,object,visual,tl.x,tl.y,br.x,br.y);
    }
  }

  private void drawStructure(Canvas canvas,AdaptedMillesObjectLayer.ObjectInstance object,
      AdaptedMillesStructureVisualLayer.Visual visual,float l,float top,float r,float foot){
    if(object.kind==AdaptedMillesMapLayer.StructureKind.WALL){drawWall(canvas,l,top,r,foot,visual);return;}
    if(object.kind==AdaptedMillesMapLayer.StructureKind.LANDMARK){drawLandmark(canvas,l,top,r,foot,visual);return;}

    float wallH=visual==null?56f:visual.wallHeight;
    float roofRise=visual==null?30f:visual.roofRise;
    float overhang=visual==null?12f:visual.roofOverhang;
    float facadeTop=foot-wallH;
    float mid=(l+r)*.5f;

    fill.setColor(sideColor(object.kind));
    path.reset();path.moveTo(r,facadeTop);path.lineTo(r,foot);path.lineTo(r-24f,foot+12f);path.lineTo(r-24f,facadeTop+12f);path.close();
    canvas.drawPath(path,fill);canvas.drawPath(path,edge);

    fill.setColor(wallColor(object.kind));
    canvas.drawRect(l,facadeTop,r,foot,fill);canvas.drawRect(l,facadeTop,r,foot,edge);

    fill.setColor(roofColor(object.kind));
    path.reset();path.moveTo(l-overhang,facadeTop);path.lineTo(mid,facadeTop-roofRise);path.lineTo(r+overhang,facadeTop);path.lineTo(r-overhang*.3f,facadeTop+18f);path.lineTo(l+overhang*.3f,facadeTop+18f);path.close();
    canvas.drawPath(path,fill);canvas.drawPath(path,edge);

    edge.setColor(0xaa302820);edge.setStrokeWidth(2f);canvas.drawLine(l-overhang,facadeTop,r+overhang,facadeTop,edge);edge.setStrokeWidth(1f);

    AdaptedMillesEntranceLayer.Entrance entrance=AdaptedMillesEntranceLayer.byStructureId(object.id);
    if(entrance!=null&&(visual==null||visual.hasDoor)){
      float doorW=visual==null?entrance.width:Math.min(entrance.width,visual.doorWidth);
      float doorH=visual==null?38f:visual.doorHeight;
      float cx=(l+r)*.5f;
      fill.setColor(0xff33281f);canvas.drawRect(cx-doorW*.5f,foot-doorH,cx+doorW*.5f,foot,fill);
      edge.setColor(0xff211b17);canvas.drawRect(cx-doorW*.5f,foot-doorH,cx+doorW*.5f,foot,edge);
      fill.setColor(0xff81705a);canvas.drawCircle(cx+doorW*.27f,foot-doorH*.45f,2.5f,fill);
      fill.setColor(0xff71685b);path.reset();path.moveTo(cx-doorW*.65f,foot);path.lineTo(cx+doorW*.65f,foot);path.lineTo(cx+doorW*.9f,foot+10f);path.lineTo(cx-doorW*.9f,foot+10f);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);
    }
    edge.setColor(0x55312B24);
  }

  private void drawWall(Canvas canvas,float l,float top,float r,float foot,AdaptedMillesStructureVisualLayer.Visual visual){
    float h=visual==null?42f:visual.wallHeight;
    fill.setColor(0xff55514a);canvas.drawRect(l,foot-h,r,foot,fill);canvas.drawRect(l,foot-h,r,foot,edge);
    fill.setColor(0xff6b665d);path.reset();path.moveTo(l-4f,foot-h);path.lineTo(r+4f,foot-h);path.lineTo(r-4f,foot-h-10f);path.lineTo(l+4f,foot-h-10f);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);
  }

  private void drawLandmark(Canvas canvas,float l,float top,float r,float foot,AdaptedMillesStructureVisualLayer.Visual visual){
    float cx=(l+r)*.5f,w=Math.max(24f,(r-l)*.68f),h=visual==null?52f:visual.wallHeight;
    fill.setColor(0xff666158);path.reset();path.moveTo(cx-w*.5f,foot);path.lineTo(cx-w*.35f,foot-h);path.lineTo(cx,foot-h-22f);path.lineTo(cx+w*.35f,foot-h);path.lineTo(cx+w*.5f,foot);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);
  }

  private void diamond(float cx,float cy,float halfWidth,float halfHeight){
    path.reset();path.moveTo(cx,cy-halfHeight);path.lineTo(cx+halfWidth,cy);path.lineTo(cx,cy+halfHeight);path.lineTo(cx-halfWidth,cy);path.close();
  }

  private void drawTransitionEdges(Canvas canvas,float cx,float cy,int mask){
    if(mask==0)return;
    edge.setColor(0x99504436);edge.setStrokeWidth(2f);
    float hw=AdaptedMillesIsometricTileLayer.HALF_WIDTH,hh=AdaptedMillesIsometricTileLayer.HALF_HEIGHT;
    if((mask&AdaptedMillesIsometricTileLayer.EDGE_NW)!=0)canvas.drawLine(cx-hw,cy,cx,cy-hh,edge);
    if((mask&AdaptedMillesIsometricTileLayer.EDGE_NE)!=0)canvas.drawLine(cx,cy-hh,cx+hw,cy,edge);
    if((mask&AdaptedMillesIsometricTileLayer.EDGE_SE)!=0)canvas.drawLine(cx+hw,cy,cx,cy+hh,edge);
    if((mask&AdaptedMillesIsometricTileLayer.EDGE_SW)!=0)canvas.drawLine(cx,cy+hh,cx-hw,cy,edge);
    edge.setStrokeWidth(1f);edge.setColor(0x55312B24);
  }

  private static int tileColor(AdaptedMillesIsometricTileLayer.TileKind kind,int variant){
    int delta=(variant-1)*0x00030303;
    switch(kind){case ROAD:return 0xff756653+delta;case PLAZA:return 0xff77756d+delta;case GATE:return 0xff665a4c+delta;default:return 0xff586451+delta;}
  }
  private static int wallColor(AdaptedMillesMapLayer.StructureKind kind){switch(kind){case HALL:return 0xff77685d;case SHOP:return 0xff806858;default:return 0xff746257;}}
  private static int sideColor(AdaptedMillesMapLayer.StructureKind kind){switch(kind){case HALL:return 0xff51463f;case SHOP:return 0xff59473d;default:return 0xff4d423c;}}
  private static int roofColor(AdaptedMillesMapLayer.StructureKind kind){switch(kind){case HALL:return 0xff493e44;case SHOP:return 0xff604844;default:return 0xff55434a;}}
}
