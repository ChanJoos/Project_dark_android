package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * [ADAPTED]/[B] evidence-safe renderer for the current Milles prototype map.
 * Geometry is authored in the live 64x32 isometric plane; unverified original art remains PENDING_CROP.
 */
public final class AdaptedMillesMapRenderer {
  private final Paint fill=new Paint();
  private final Paint edge=new Paint();
  private final Path path=new Path();

  public AdaptedMillesMapRenderer(){
    fill.setStyle(Paint.Style.FILL);fill.setAntiAlias(false);
    edge.setStyle(Paint.Style.STROKE);edge.setStrokeWidth(1f);edge.setAntiAlias(false);edge.setColor(0x55312B24);
  }

  /** Draw TILE, 2.5D decoration, then collision-aligned static structures. */
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
      drawTileDetail(canvas,c.x,c.y,tile);
      drawTransitionEdges(canvas,c.x,c.y,tile.transitionMask);
    }
  }

  /**
   * Props are grounded on world coordinates and projected on the same 2:1 axes as tiles/buildings.
   * No TREE/FENCE/BENCH/SIGN is rendered as a screen-aligned flat icon anymore.
   */
  public void drawDecorations(Canvas canvas,WorldRuntimeAdapter world){
    for(AdaptedMillesDecorationLayer.Decoration d:world.map().decorations()){
      WorldCameraTransform.Point foot=world.worldToScreen(d.footX,d.footY);
      if(foot.x+d.width<0||foot.x-d.width>canvas.getWidth()||foot.y+d.height*.25f<0||foot.y-d.height>canvas.getHeight())continue;
      switch(d.kind){
        case TREE:drawIsoTree(canvas,foot,d);break;
        case FENCE:drawIsoFence(canvas,world,d);break;
        case SIGN:drawIsoSign(canvas,world,d);break;
        case WELL:drawIsoWell(canvas,foot,d);break;
        case BENCH:drawIsoBench(canvas,world,d);break;
        case LAMP:drawIsoLamp(canvas,foot,d);break;
        case BUSH:drawIsoBush(canvas,foot,d);break;
        case GATEPOST:drawIsoGatepost(canvas,foot,d);break;
      }
      edge.setStrokeWidth(1f);edge.setColor(0x55312B24);
    }
  }

  private void drawIsoTree(Canvas canvas,WorldCameraTransform.Point foot,AdaptedMillesDecorationLayer.Decoration d){
    float fw=Math.max(10f,d.width*.18f),fd=fw*.48f;
    // Soft projected ground shadow/footprint.
    fill.setColor(0x40161713);diamond(foot.x+5f,foot.y+4f,d.width*.34f,d.width*.13f);canvas.drawPath(path,fill);

    float trunkH=d.height*.46f,topY=foot.y-trunkH;
    // Faceted trunk: two visible vertical faces share one projected diamond foot.
    fill.setColor(0xff6a4b31);quad(canvas,foot.x-fw,foot.y-fd,foot.x,foot.y,foot.x,topY,foot.x-fw*.55f,topY-fd*.45f,fill);
    fill.setColor(0xff493424);quad(canvas,foot.x,foot.y,foot.x+fw,foot.y-fd,foot.x+fw*.55f,topY-fd*.45f,foot.x,topY,fill);
    edge.setColor(0xff34251b);canvas.drawLine(foot.x,foot.y,foot.x,topY,edge);

    // Layered faceted canopy, deliberately irregular like a classic pre-rendered RPG tree sprite.
    float crownY=foot.y-d.height*.67f,cw=d.width*.50f,ch=d.height*.22f;
    fill.setColor(0xff253c2c);isoCrown(canvas,foot.x+4f,crownY+8f,cw,ch);
    fill.setColor(0xff355239);isoCrown(canvas,foot.x-cw*.22f,crownY-4f,cw*.82f,ch*.9f);
    fill.setColor(0xff426246);isoCrown(canvas,foot.x+cw*.22f,crownY-8f,cw*.78f,ch*.82f);
    fill.setColor(0xff547351);isoCrown(canvas,foot.x,crownY-ch*.68f,cw*.62f,ch*.72f);
    edge.setColor(0xff263b2c);edge.setStrokeWidth(2f);
    canvas.drawLine(foot.x-cw*.45f,crownY+ch*.2f,foot.x,crownY+ch*.72f,edge);
    canvas.drawLine(foot.x+cw*.4f,crownY,foot.x,crownY+ch*.72f,edge);
  }

  private void isoCrown(Canvas canvas,float cx,float cy,float hw,float hh){
    path.reset();path.moveTo(cx,cy-hh);path.lineTo(cx+hw*.82f,cy-hh*.18f);path.lineTo(cx+hw*.58f,cy+hh*.7f);
    path.lineTo(cx,cy+hh);path.lineTo(cx-hw*.7f,cy+hh*.55f);path.lineTo(cx-hw*.9f,cy-hh*.12f);path.close();
    canvas.drawPath(path,fill);canvas.drawPath(path,edge);
  }

  private void drawIsoFence(Canvas canvas,WorldRuntimeAdapter world,AdaptedMillesDecorationLayer.Decoration d){
    WorldCameraTransform.Point[] ends=axisEnds(world,d,d.width*.5f);
    WorldCameraTransform.Point a=ends[0],b=ends[1];
    float postH=Math.max(20f,d.height),rail1=postH*.38f,rail2=postH*.68f;
    fill.setColor(0x35191512);projectedStrip(canvas,a.x+4f,a.y+4f,b.x+4f,b.y+4f,7f,fill);

    // Rails are sloped with the isometric world axis, not screen-horizontal.
    edge.setColor(0xff5a412b);edge.setStrokeWidth(5f);
    canvas.drawLine(a.x,a.y-rail1,b.x,b.y-rail1,edge);
    canvas.drawLine(a.x,a.y-rail2,b.x,b.y-rail2,edge);
    edge.setColor(0xff7a5938);edge.setStrokeWidth(2f);
    canvas.drawLine(a.x,a.y-rail2-2f,b.x,b.y-rail2-2f,edge);

    int posts=Math.max(2,(int)(d.width/48f)+1);
    for(int i=0;i<posts;i++){
      float t=posts==1?0f:(float)i/(posts-1),x=lerp(a.x,b.x,t),y=lerp(a.y,b.y,t);
      drawWoodPost(canvas,x,y,postH,6f);
    }
  }

  private void drawWoodPost(Canvas canvas,float x,float y,float h,float half){
    fill.setColor(0xff60462f);quad(canvas,x-half,y-3f,x,y,x,y-h,x-half*.65f,y-h-3f,fill);
    fill.setColor(0xff422f22);quad(canvas,x,y,x+half,y-3f,x+half*.65f,y-h-3f,x,y-h,fill);
    fill.setColor(0xff7d5d3c);path.reset();path.moveTo(x-half*.65f,y-h-3f);path.lineTo(x,y-h-7f);path.lineTo(x+half*.65f,y-h-3f);path.lineTo(x,y-h);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);
  }

  private void drawIsoSign(Canvas canvas,WorldRuntimeAdapter world,AdaptedMillesDecorationLayer.Decoration d){
    WorldCameraTransform.Point foot=world.worldToScreen(d.footX,d.footY);
    float h=d.height,postTop=foot.y-h*.78f;
    drawWoodPost(canvas,foot.x,foot.y,h*.74f,4f);
    WorldCameraTransform.Point[] ends=axisEnds(world,d,d.width*.48f);
    float ax=ends[0].x,ay=postTop+(ends[0].y-foot.y)*.22f,bx=ends[1].x,by=postTop+(ends[1].y-foot.y)*.22f;
    fill.setColor(0xff775638);projectedStrip(canvas,ax,ay,bx,by,18f,fill);
    edge.setColor(0xff382a20);edge.setStrokeWidth(2f);canvas.drawLine(ax,ay,bx,by,edge);canvas.drawLine(ax,ay+18f,bx,by+18f,edge);
    // Small hanging tab suggests shop/wayfinding signage without claiming original pixels.
    fill.setColor(0xffb08a55);float mx=(ax+bx)*.5f,my=(ay+by)*.5f;diamond(mx,my+9f,4f,2f);canvas.drawPath(path,fill);
  }

  private void drawIsoWell(Canvas canvas,WorldCameraTransform.Point foot,AdaptedMillesDecorationLayer.Decoration d){
    float hw=d.width*.5f,hh=d.width*.22f,h=Math.max(16f,d.height*.48f);
    // Raised stone ring as a low isometric prism.
    fill.setColor(0x35181715);diamond(foot.x+4f,foot.y+4f,hw,hh);canvas.drawPath(path,fill);
    fill.setColor(0xff5b574f);quad(canvas,foot.x-hw,foot.y,foot.x,foot.y+hh,foot.x,foot.y+hh-h,foot.x-hw,foot.y-h,fill);
    fill.setColor(0xff777168);quad(canvas,foot.x,foot.y+hh,foot.x+hw,foot.y,foot.x+hw,foot.y-h,foot.x,foot.y+hh-h,fill);
    fill.setColor(0xff8a8478);diamond(foot.x,foot.y-h,hw,hh);canvas.drawPath(path,fill);canvas.drawPath(path,edge);
    fill.setColor(0xff272b2b);diamond(foot.x,foot.y-h+2f,hw*.62f,hh*.58f);canvas.drawPath(path,fill);
  }

  private void drawIsoBench(Canvas canvas,WorldRuntimeAdapter world,AdaptedMillesDecorationLayer.Decoration d){
    WorldCameraTransform.Point[] ends=axisEnds(world,d,d.width*.44f);
    WorldCameraTransform.Point a=ends[0],b=ends[1];
    float seatH=Math.max(10f,d.height*.45f);
    fill.setColor(0x30171310);projectedStrip(canvas,a.x+3f,a.y+4f,b.x+3f,b.y+4f,10f,fill);
    // Seat slab as a projected parallelogram with visible thickness.
    fill.setColor(0xff765137);projectedStrip(canvas,a.x,a.y-seatH,b.x,b.y-seatH,12f,fill);
    edge.setColor(0xff402d21);canvas.drawLine(a.x,a.y-seatH,b.x,b.y-seatH,edge);
    drawWoodPost(canvas,lerp(a.x,b.x,.18f),lerp(a.y,b.y,.18f),seatH,3f);
    drawWoodPost(canvas,lerp(a.x,b.x,.82f),lerp(a.y,b.y,.82f),seatH,3f);
    edge.setStrokeWidth(4f);canvas.drawLine(a.x,a.y-seatH-12f,b.x,b.y-seatH-12f,edge);
  }

  private void drawIsoLamp(Canvas canvas,WorldCameraTransform.Point foot,AdaptedMillesDecorationLayer.Decoration d){
    float h=d.height;
    fill.setColor(0x35181715);diamond(foot.x+3f,foot.y+3f,10f,4f);canvas.drawPath(path,fill);
    fill.setColor(0xff45423d);diamond(foot.x,foot.y,8f,4f);canvas.drawPath(path,fill);canvas.drawPath(path,edge);
    fill.setColor(0xff363633);quad(canvas,foot.x-3f,foot.y-2f,foot.x+3f,foot.y-2f,foot.x+2f,foot.y-h*.76f,foot.x-2f,foot.y-h*.76f,fill);
    float ly=foot.y-h*.82f;
    fill.setColor(0xffd6aa58);diamond(foot.x,ly,9f,7f);canvas.drawPath(path,fill);canvas.drawPath(path,edge);
    fill.setColor(0xffffdc83);diamond(foot.x,ly-2f,5f,4f);canvas.drawPath(path,fill);
    fill.setColor(0xff3a332b);path.reset();path.moveTo(foot.x-10f,ly-7f);path.lineTo(foot.x,ly-14f);path.lineTo(foot.x+10f,ly-7f);path.lineTo(foot.x,ly-4f);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);
  }

  private void drawIsoBush(Canvas canvas,WorldCameraTransform.Point foot,AdaptedMillesDecorationLayer.Decoration d){
    fill.setColor(0x30151613);diamond(foot.x+3f,foot.y+3f,d.width*.42f,d.width*.16f);canvas.drawPath(path,fill);
    float h=d.height*.58f;
    fill.setColor(0xff314832);isoCrown(canvas,foot.x-d.width*.18f,foot.y-h*.55f,d.width*.28f,h*.55f);
    fill.setColor(0xff405c3f);isoCrown(canvas,foot.x+d.width*.18f,foot.y-h*.5f,d.width*.30f,h*.58f);
    fill.setColor(0xff56704e);isoCrown(canvas,foot.x,foot.y-h*.8f,d.width*.26f,h*.5f);
  }

  private void drawIsoGatepost(Canvas canvas,WorldCameraTransform.Point foot,AdaptedMillesDecorationLayer.Decoration d){
    float hw=d.width*.28f,hh=hw*.48f,h=d.height*.78f;
    fill.setColor(0x35161513);diamond(foot.x+4f,foot.y+4f,hw*1.4f,hh*1.4f);canvas.drawPath(path,fill);
    fill.setColor(0xff5b554b);quad(canvas,foot.x-hw,foot.y-hh,foot.x,foot.y,foot.x,foot.y-h,foot.x-hw,foot.y-h-hh,fill);
    fill.setColor(0xff403d38);quad(canvas,foot.x,foot.y,foot.x+hw,foot.y-hh,foot.x+hw,foot.y-h-hh,foot.x,foot.y-h,fill);
    fill.setColor(0xff777064);diamond(foot.x,foot.y-h-hh,hw*1.35f,hh*1.35f);canvas.drawPath(path,fill);canvas.drawPath(path,edge);
    fill.setColor(0xff4d4238);path.reset();path.moveTo(foot.x-hw*1.15f,foot.y-h-hh);path.lineTo(foot.x,foot.y-h-hh-12f);path.lineTo(foot.x+hw*1.15f,foot.y-h-hh);path.lineTo(foot.x,foot.y-h-hh+5f);path.close();canvas.drawPath(path,fill);canvas.drawPath(path,edge);
  }

  private WorldCameraTransform.Point[] axisEnds(WorldRuntimeAdapter world,AdaptedMillesDecorationLayer.Decoration d,float halfSpan){
    float dx=halfSpan,dy=halfSpan*.5f;
    if(d.axis==AdaptedMillesDecorationLayer.Axis.NE_SW)dy=-dy;
    if(d.axis==AdaptedMillesDecorationLayer.Axis.NONE)dy=0f;
    return new WorldCameraTransform.Point[]{world.worldToScreen(d.footX-dx,d.footY-dy),world.worldToScreen(d.footX+dx,d.footY+dy)};
  }

  private void projectedStrip(Canvas canvas,float ax,float ay,float bx,float by,float thickness,Paint paint){
    float len=(float)Math.sqrt((bx-ax)*(bx-ax)+(by-ay)*(by-ay));
    if(len<.001f)return;
    float nx=-(by-ay)/len*thickness*.5f,ny=(bx-ax)/len*thickness*.5f;
    quad(canvas,ax+nx,ay+ny,bx+nx,by+ny,bx-nx,by-ny,ax-nx,ay-ny,paint);
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
      AdaptedMillesIsoBuildingLayer.Building iso=AdaptedMillesIsoBuildingLayer.byStructureId(object.id);
      if(iso!=null)drawIsometricBuilding(canvas,world,iso);
      else drawStructure(canvas,object,visual,tl.x,tl.y,br.x,br.y);
    }
  }

  private void drawIsometricBuilding(Canvas canvas,WorldRuntimeAdapter world,AdaptedMillesIsoBuildingLayer.Building b){
    WorldCameraTransform.Point n=world.worldToScreen(b.centerX,b.centerY-b.halfDepth);
    WorldCameraTransform.Point e=world.worldToScreen(b.centerX+b.halfWidth,b.centerY);
    WorldCameraTransform.Point s=world.worldToScreen(b.centerX,b.centerY+b.halfDepth);
    WorldCameraTransform.Point w=world.worldToScreen(b.centerX-b.halfWidth,b.centerY);

    fill.setColor(0x4d171a18);quad(canvas,n.x+8f,n.y+6f,e.x+10f,e.y+7f,s.x+10f,s.y+7f,w.x+8f,w.y+6f,fill);
    WorldCameraTransform.Point approach=world.worldToScreen(b.approachX,b.approachY);
    fill.setColor(0xff81745f);diamond(approach.x-8f,approach.y-8f,22f,9f);canvas.drawPath(path,fill);canvas.drawPath(path,edge);
    diamond(approach.x-20f,approach.y-18f,18f,8f);canvas.drawPath(path,fill);canvas.drawPath(path,edge);

    float h=b.wallHeight;
    fill.setColor(0xff8a715b);quad(canvas,w.x,w.y-h,s.x,s.y-h,s.x,s.y,w.x,w.y,fill);
    fill.setColor(0xff665447);quad(canvas,s.x,s.y-h,e.x,e.y-h,e.x,e.y,s.x,s.y,fill);
    edge.setColor(0xff42352c);edge.setStrokeWidth(3f);
    canvas.drawLine(w.x,w.y-h,w.x,w.y,edge);canvas.drawLine(s.x,s.y-h,s.x,s.y,edge);canvas.drawLine(e.x,e.y-h,e.x,e.y,edge);
    canvas.drawLine(w.x,w.y-h,s.x,s.y-h,edge);canvas.drawLine(s.x,s.y-h,e.x,e.y-h,edge);

    float db1x=lerp(s.x,e.x,.24f),db1y=lerp(s.y,e.y,.24f),db2x=lerp(s.x,e.x,.62f),db2y=lerp(s.y,e.y,.62f);
    fill.setColor(0xff35271f);quad(canvas,db1x,db1y-34f,db2x,db2y-34f,db2x,db2y,db1x,db1y,fill);
    edge.setColor(0xff211914);canvas.drawLine(db1x,db1y-34f,db2x,db2y-34f,edge);canvas.drawLine(db2x,db2y-34f,db2x,db2y,edge);
    fill.setColor(0xffc5a05d);canvas.drawCircle(lerp(db1x,db2x,.76f),lerp(db1y,db2y,.76f)-15f,2.2f,fill);

    float wb1x=lerp(w.x,s.x,.22f),wb1y=lerp(w.y,s.y,.22f),wb2x=lerp(w.x,s.x,.48f),wb2y=lerp(w.y,s.y,.48f);
    fill.setColor(0xff9dc0bd);quad(canvas,wb1x,wb1y-39f,wb2x,wb2y-39f,wb2x,wb2y-22f,wb1x,wb1y-22f,fill);

    float o=b.roofOverhang;
    float tnX=n.x,tnY=n.y-h-o*.5f,teX=e.x+o,teY=e.y-h,tsX=s.x,tsY=s.y-h+o*.5f,twX=w.x-o,twY=w.y-h;
    float peakX=(n.x+s.x)*.5f,peakY=(n.y+s.y)*.5f-h-b.roofRise;
    fill.setColor(0xff4b3c42);triangle(canvas,tnX,tnY,teX,teY,peakX,peakY,fill);
    fill.setColor(0xff58444a);triangle(canvas,twX,twY,tnX,tnY,peakX,peakY,fill);
    fill.setColor(0xff6b4d4a);triangle(canvas,twX,twY,tsX,tsY,peakX,peakY,fill);
    fill.setColor(0xff593f40);triangle(canvas,tsX,tsY,teX,teY,peakX,peakY,fill);
    edge.setColor(0xff34292b);edge.setStrokeWidth(2f);canvas.drawLine(twX,twY,peakX,peakY,edge);canvas.drawLine(peakX,peakY,teX,teY,edge);canvas.drawLine(twX,twY,tsX,tsY,edge);canvas.drawLine(tsX,tsY,teX,teY,edge);
    edge.setStrokeWidth(1f);edge.setColor(0x55312B24);
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

  private void quad(Canvas canvas,float ax,float ay,float bx,float by,float cx,float cy,float dx,float dy,Paint paint){
    path.reset();path.moveTo(ax,ay);path.lineTo(bx,by);path.lineTo(cx,cy);path.lineTo(dx,dy);path.close();canvas.drawPath(path,paint);canvas.drawPath(path,edge);
  }
  private void triangle(Canvas canvas,float ax,float ay,float bx,float by,float cx,float cy,Paint paint){
    path.reset();path.moveTo(ax,ay);path.lineTo(bx,by);path.lineTo(cx,cy);path.close();canvas.drawPath(path,paint);canvas.drawPath(path,edge);
  }
  private static float lerp(float a,float b,float t){return a+(b-a)*t;}

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

  private void drawTileDetail(Canvas canvas,float cx,float cy,AdaptedMillesIsometricTileLayer.Tile tile){
    float hw=AdaptedMillesIsometricTileLayer.HALF_WIDTH,hh=AdaptedMillesIsometricTileLayer.HALF_HEIGHT;
    switch(tile.kind){
      case GROUND:
        edge.setColor(tile.variant%2==0?0x5541533e:0x554b5b46);edge.setStrokeWidth(1f);
        float gx=cx-11f+tile.variant*4f,gy=cy+2f-(tile.variant&1)*5f;
        canvas.drawLine(gx,gy,gx+3f,gy-5f,edge);canvas.drawLine(gx+3f,gy-5f,gx+6f,gy,edge);break;
      case ROAD:
        edge.setColor(0x6655483c);edge.setStrokeWidth(1f);
        canvas.drawLine(cx-hw*.52f,cy,cx,cy+hh*.52f,edge);canvas.drawLine(cx,cy-hh*.52f,cx+hw*.52f,cy,edge);
        if((tile.row+tile.column)%3==0)canvas.drawCircle(cx,cy,1.5f,edge);break;
      case PLAZA:
        edge.setColor(0x77605e59);edge.setStrokeWidth(1f);diamond(cx,cy,hw*.54f,hh*.54f);canvas.drawPath(path,edge);canvas.drawLine(cx-hw*.34f,cy,cx+hw*.34f,cy,edge);break;
      case GATE:
        edge.setColor(0x88715f4b);edge.setStrokeWidth(2f);canvas.drawLine(cx-hw*.55f,cy-hh*.18f,cx+hw*.55f,cy-hh*.18f,edge);canvas.drawLine(cx-hw*.55f,cy+hh*.18f,cx+hw*.55f,cy+hh*.18f,edge);break;
    }
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
