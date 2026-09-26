package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Asset-driven Pote forest renderer.
 * No synthetic circles/ovals/placeholder trees are permitted here: every visible forest object
 * resolves to a POTE_* production sprite and is placed by the spatial relationship grammar.
 */
public final class PoteFieldRenderer {
  public static final String STATUS="POTE_ASSET_DRIVEN_FOREST_V1";
  private static final float TILE_W=64f,TILE_H=32f;
  private final Paint pixel=new Paint();
  private final AssetManager assets=findAssets();
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();
  private final List<Placement> placements=buildPlacements();

  private static final class Placement {
    final String asset,role; final float x,y,scale; final boolean tile;
    Placement(String a,float x,float y,float s,String role,boolean tile){
      this.asset=a;this.x=x;this.y=y;this.scale=s;this.role=role;this.tile=tile;
    }
  }

  public PoteFieldRenderer(){
    pixel.setAntiAlias(false);pixel.setFilterBitmap(false);pixel.setDither(false);
  }

  public int placementCount(){return placements.size();}

  public void draw(Canvas c,WorldRuntimeAdapter w){
    if(c==null||w==null)return;
    c.drawColor(0xff241d15);
    drawFloor(c,w);
    for(Placement p:placements)drawPlacement(c,w,p);
  }

  private void drawFloor(Canvas c,WorldRuntimeAdapter w){
    int variant=0;
    for(float y=96;y<=864;y+=32){
      int row=(int)((y-96)/32);
      for(float x=96;x<=1376;x+=64){
        float px=x+((row&1)==0?0:32);if(px>1376)continue;
        // Main walk corridors are barer; forest edges use leaf/moss variants.
        boolean corridor=(px<720&&y>690)||(px>420&&px<1020&&y>360&&y<610)||(px>650&&y<330);
        int id=corridor?5+((row+(int)(px/64))%3):1+((variant++)%4);
        drawTile(c,w,String.format("POTE_GD_%02d.png",id),px,y);
      }
    }
  }

  private void drawTile(Canvas c,WorldRuntimeAdapter w,String asset,float x,float y){
    Bitmap b=bitmap(asset);if(b==null)return;WorldCameraTransform.Point q=w.worldToScreen(x,y);
    // Ground sprites are authored as overlapping forest-floor patches (~148x111).
    // Keep that footprint so the floor reads as continuous soil rather than a diamond grid.
    float ww=b.getWidth(),hh=b.getHeight();
    RectF dst=new RectF(Math.round(q.x-ww*.5f),Math.round(q.y-hh*.5f),Math.round(q.x+ww*.5f),Math.round(q.y+hh*.5f));
    c.drawBitmap(b,null,dst,pixel);
  }

  private void drawPlacement(Canvas c,WorldRuntimeAdapter w,Placement p){
    Bitmap b=bitmap(p.asset);if(b==null)return;
    WorldCameraTransform.Point q=w.worldToScreen(p.x,p.y);
    if(p.tile){
      RectF dst=new RectF(q.x-TILE_W*.5f,q.y-TILE_H*.5f,q.x+TILE_W*.5f,q.y+TILE_H*.5f);
      c.drawBitmap(b,null,dst,pixel);return;
    }
    float ww=b.getWidth()*p.scale,hh=b.getHeight()*p.scale;
    RectF dst=new RectF(Math.round(q.x-ww*.5f),Math.round(q.y-hh),Math.round(q.x+ww*.5f),Math.round(q.y));
    if(dst.right>=0&&dst.left<=c.getWidth()&&dst.bottom>=0&&dst.top<=c.getHeight())c.drawBitmap(b,null,dst,pixel);
  }

  private Bitmap bitmap(String name){
    if(cache.containsKey(name))return cache.get(name);
    Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(name)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);
    }catch(Throwable ignored){}
    cache.put(name,b);return b;
  }

  private static List<Placement> buildPlacements(){
    List<Placement> p=new ArrayList<>();
    // STREAM AXIS: water first, then bank rocks, moisture vegetation. It bends through the east side.
    water(p,1110,220,1); water(p,1150,280,2); water(p,1120,345,3);
    water(p,1060,410,4); water(p,1050,480,1); water(p,1090,545,2);
    water(p,1150,610,3); water(p,1180,675,4); water(p,1160,740,5);
    water(p,1210,805,6);
    rock(p,1035,245,1);rock(p,1200,320,2);rock(p,1015,455,3);rock(p,1210,650,4);rock(p,1110,785,5);
    bank(p,1010,275,1);bank(p,1215,370,2);bank(p,1000,520,3);bank(p,1230,705,4);bank(p,1100,825,5);

    // NORTH/WEST FOREST WALL: large canopy -> small tree -> bush -> groundcover.
    tree(p,135,180,1,1.35f);tree(p,245,150,2,1.35f);tree(p,365,145,3,1.35f);
    tree(p,500,130,4,1.35f);tree(p,650,125,5,1.35f);tree(p,820,125,6,1.35f);
    tree(p,980,145,7,1.35f);tree(p,1280,210,2,1.35f);tree(p,1320,350,5,1.35f);
    tree(p,1310,520,3,1.35f);tree(p,1325,690,6,1.35f);tree(p,1300,825,1,1.35f);
    tree(p,110,330,4,1.35f);tree(p,125,500,7,1.35f);tree(p,145,650,2,1.35f);
    bushRing(p,new float[][]{{190,215},{300,205},{430,200},{575,190},{735,185},{900,195},{1240,250},{1240,430},{1240,590},{1240,770}},1);

    // INTERNAL GROVE A: blocks direct sight, but leaves a south-east escape corridor.
    tree(p,300,300,5,1.35f);tree(p,380,330,1,1.35f);small(p,335,385,1);small(p,415,285,2);
    bushRing(p,new float[][]{{265,350},{330,420},{420,390},{455,335}},3);
    ground(p,285,410,1);ground(p,440,405,3);detail(p,365,430,2);

    // INTERNAL GROVE B: old-growth landmark around the twisted trunk.
    tree(p,540,560,6,1.35f);tree(p,625,610,3,1.35f);small(p,500,645,3);
    stump(p,470,590,3);stump(p,680,650,4);
    bushRing(p,new float[][]{{500,520},{590,520},{680,570},{620,690},{520,700}},5);
    ground(p,455,675,4);ground(p,700,610,5);

    // NORTH-EAST GROVE, framing the approach to the stream.
    tree(p,785,245,7,1.35f);tree(p,885,275,2,1.35f);small(p,745,325,2);
    bushRing(p,new float[][]{{735,225},{835,205},{930,250},{950,335},{820,365}},7);
    ground(p,710,340,2);ground(p,940,370,6);rock(p,920,205,2);

    // SOUTH-EAST GROVE: dense wall behind the final encounter pocket.
    tree(p,930,670,4,1.35f);tree(p,1020,720,1,1.35f);small(p,885,760,1);
    bushRing(p,new float[][]{{875,650},{970,610},{1060,650},{1080,760},{950,805}},2);
    stump(p,850,705,1);ground(p,860,805,3);detail(p,1040,815,5);

    // ENCOUNTER POCKETS: deliberately open centres; peripheral detail gives visual enclosure.
    encounterEdge(p,580,385,1);encounterEdge(p,760,505,4);encounterEdge(p,360,735,6);
    // Entrance readability: sparse vegetation, landmark stump and flowers, no canopy over the spawn.
    stump(p,250,790,2);ground(p,285,820,6);detail(p,330,805,4);
    bush(p,165,735,8);bush(p,410,815,7);

    // Forest-floor variation and small landmarks along corridors.
    detail(p,520,330,1);detail(p,690,300,3);detail(p,830,445,2);detail(p,650,760,4);
    ground(p,470,460,2);ground(p,620,445,1);ground(p,845,555,5);ground(p,730,675,6);
    rock(p,545,280,1);rock(p,720,575,5);stump(p,800,585,1);

    p.sort(Comparator.comparingDouble((Placement a)->a.y).thenComparing(a->a.asset));
    return p;
  }

  private static void encounterEdge(List<Placement> p,float x,float y,int seed){
    bush(p,x-95,y-20,1+seed%8);bush(p,x+95,y+10,1+(seed+2)%8);
    ground(p,x-70,y+65,1+seed%6);ground(p,x+75,y+70,1+(seed+3)%6);
    rock(p,x+120,y-45,1+seed%5);
  }
  private static void bushRing(List<Placement> p,float[][] xy,int seed){int i=0;for(float[] q:xy)bush(p,q[0],q[1],1+(seed+i++)%8);}
  private static void tree(List<Placement> p,float x,float y,int n,float s){p.add(new Placement(String.format("POTE_TR_%02d.png",n),x,y,s,"canopy",false));}
  private static void small(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_TS_%02d.png",n),x,y,1.15f,"secondary_canopy",false));}
  private static void bush(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BS_%02d.png",n),x,y,1f,"understory",false));}
  private static void ground(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_GF_%02d.png",n),x,y,1f,"groundcover",false));}
  private static void stump(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_ST_%02d.png",n),x,y,1f,"stump",false));}
  private static void rock(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_RK_%02d.png",n),x,y,1f,"rock",false));}
  private static void water(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_WT_%02d.png",n),x,y,1f,"stream",false));}
  private static void bank(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_BW_%02d.png",n),x,y,1f,"bank",false));}
  private static void detail(List<Placement> p,float x,float y,int n){p.add(new Placement(String.format("POTE_OT_%02d.png",n),x,y,1f,"detail",false));}

  private static AssetManager findAssets(){
    try{Class<?> t=Class.forName("android.app.ActivityThread");Method m=t.getDeclaredMethod("currentApplication");
      Object app=m.invoke(null);return app instanceof Context?((Context)app).getAssets():null;
    }catch(Throwable ignored){return null;}
  }
}
