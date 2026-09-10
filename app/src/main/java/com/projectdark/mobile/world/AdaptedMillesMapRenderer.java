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

  /** Draw TILE first, then static OBJECT silhouettes. Dynamic entities remain owned by their renderers. */
  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    drawTiles(canvas,world);
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
