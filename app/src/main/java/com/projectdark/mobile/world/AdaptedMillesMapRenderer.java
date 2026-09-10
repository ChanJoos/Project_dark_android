package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * [ADAPTED]/[B] evidence-safe renderer for the current Milles prototype map.
 *
 * This is intentionally a simple geometric presentation, not fabricated original Nexon artwork.
 * It exists so the authored TILE/OBJECT map is visible on-device immediately. PENDING_CROP slots can
 * replace these fills one-by-one without changing the map/runtime contract.
 */
public final class AdaptedMillesMapRenderer {
  private final Paint fill=new Paint();
  private final Paint edge=new Paint();
  private final Path path=new Path();

  public AdaptedMillesMapRenderer(){
    fill.setStyle(Paint.Style.FILL);fill.setAntiAlias(false);
    edge.setStyle(Paint.Style.STROKE);edge.setStrokeWidth(1f);edge.setAntiAlias(false);edge.setColor(0x55312B24);
  }

  /** Draw TILE, decoration, then static structure silhouettes. Dynamic entities remain separately owned. */
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
      canvas.drawPath(path,fill);
      canvas.drawPath(path,edge);
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
          canvas.drawLine(l,foot.y-18,l,foot.y+2,edge);canvas.drawLine(r,foot.y-18,r,foot.y+2,edge);
          edge.setStrokeWidth(1f);break;
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
      }
      edge.setColor(0x55312B24);
    }
  }

  public void drawObjects(Canvas canvas,WorldRuntimeAdapter world){
    for(AdaptedMillesObjectLayer.ObjectInstance object:world.map().renderObjects()){
      WorldCameraTransform.Point tl=world.worldToScreen(object.collisionLeft,object.collisionTop);
      WorldCameraTransform.Point br=world.worldToScreen(object.collisionRight,object.collisionBottom);
      if(br.x<0f||tl.x>canvas.getWidth()||br.y<0f||tl.y>canvas.getHeight())continue;
      fill.setColor(objectColor(object.kind));
      canvas.drawRect(tl.x,tl.y,br.x,br.y,fill);
      canvas.drawRect(tl.x,tl.y,br.x,br.y,edge);
      // Diagonal mark keeps geometric fallback visibly distinct from evidence-backed final art.
      canvas.drawLine(tl.x,tl.y,br.x,br.y,edge);
      canvas.drawLine(br.x,tl.y,tl.x,br.y,edge);
    }
  }

  private void diamond(float cx,float cy,float halfWidth,float halfHeight){
    path.reset();
    path.moveTo(cx,cy-halfHeight);
    path.lineTo(cx+halfWidth,cy);
    path.lineTo(cx,cy+halfHeight);
    path.lineTo(cx-halfWidth,cy);
    path.close();
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
    switch(kind){
      case ROAD:return 0xff756653+delta;
      case PLAZA:return 0xff77756d+delta;
      case GATE:return 0xff665a4c+delta;
      default:return 0xff586451+delta;
    }
  }

  private static int objectColor(AdaptedMillesMapLayer.StructureKind kind){
    switch(kind){
      case HALL:return 0xff574943;
      case SHOP:return 0xff655044;
      case WALL:return 0xff494846;
      case LANDMARK:return 0xff6b675c;
      default:return 0xff5b4d45;
    }
  }
}
