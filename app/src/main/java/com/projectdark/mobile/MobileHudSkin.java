package com.projectdark.mobile;
import android.graphics.*;
/** Crisp native vector HUD art. No third-party skill images or UI bitmaps are shipped. */
final class MobileHudSkin {
  private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Path path=new Path();
  private static Bitmap mapTerrain;private static String terrainKey;
  private static final int WHITE=0xfff1eee4,GOLD=0xffdfc88c;
  static void fade(Canvas c,RectF r){p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(r.left,r.top,r.right,r.top,0x03080b10,0x77080b10,Shader.TileMode.CLAMP));c.drawRect(r,p);p.setShader(null);}
  static void ring(Canvas c,float x,float y,float r,boolean active){
    p.setStyle(Paint.Style.FILL);p.setColor(active?0x775f573a:0x6413191b);c.drawCircle(x,y,r,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?1.8f:1f);p.setColor(active?GOLD:0x998e876d);c.drawCircle(x,y,r-.7f,p);
    p.setStrokeWidth(.6f);p.setColor(0x554d4e42);c.drawCircle(x,y,r-2.8f,p);
    p.setColor(active?0xfff4e6b1:0xaaddd0a6);c.drawArc(x-r+1,y-r+1,x+r-1,y+r-1,210,120,false,p);p.setStyle(Paint.Style.FILL);
  }
  static void crown(Canvas c,float x,float y){
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xbbb7b4a2);
    path.reset();path.moveTo(x-47,y+4);path.lineTo(x-27,y-2);path.lineTo(x-34,y-17);path.lineTo(x-14,y-8);path.lineTo(x-10,y-26);path.lineTo(x,y-16);path.lineTo(x+10,y-26);path.lineTo(x+14,y-8);path.lineTo(x+34,y-17);path.lineTo(x+27,y-2);path.lineTo(x+47,y+4);c.drawPath(path,p);
    p.setColor(GOLD);c.drawLine(x-55,y+8,x+55,y+8,p);c.drawCircle(x,y-22,2,p);p.setStyle(Paint.Style.FILL);
  }
  // Filled silver reliefs: silhouette first, dark engraving second, specular edge last.
  // At mobile scale these remain readable without a thick generic line-icon outline.
  static void utility(Canvas c,String kind,float x,float y,boolean active){
    p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(x,y,20,new int[]{0x88101416,0x42101416,0x00101416},new float[]{0,.62f,1},Shader.TileMode.CLAMP));c.drawCircle(x,y,21,p);p.setShader(null);
    icon(c,kind,x,y,12,active);
    if(active){p.setColor(GOLD);c.drawRoundRect(x-12,y+17,x+12,y+18,1,1,p);}
  }
  static void icon(Canvas c,String kind,float x,float y,float r,boolean active){
    c.save();c.translate(x,y);c.scale(r/11f,r/11f);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
    switch(kind){
      case "bag":
        polygon(c,active,-8,-6,8,-6,10,8,7,11,-7,11,-10,8);outline(c,active,-8,-6,8,-6,10,8,7,11,-7,11,-10,8);
        stroke(c,0xff282d31,1.1f,-8,-3,0,2,8,-3);stroke(c,WHITE,1.4f,-5,-6,-5,-10,5,-10,5,-6);
        p.setColor(GOLD);c.drawRoundRect(-2,-1,2,3,.6f,.6f,p);stroke(c,0xff747c80,.8f,-6,3,-6,8);stroke(c,0xff747c80,.8f,6,3,6,8);break;
      case "stats":
        for(int i=0;i<8;i++){c.save();c.rotate(i*45);polygon(c,active,-1.8f,-8,0,-12,1.8f,-8,0,-6);c.restore();}
        polygon(c,active,0,-7,6,-3,6,4,0,9,-6,4,-6,-3);outline(c,active,0,-7,6,-3,6,4,0,9,-6,4,-6,-3);
        polygon(c,false,0,-4,3,0,0,5,-3,0);stroke(c,0xff4b5356,1,-4,0,4,0);break;
      case "equipment":
        polygon(c,active,-4,-10,-9,-6,-11,0,-7,2,-5,-1,-5,9,0,11,5,9,5,-1,7,2,11,0,9,-6,4,-10,2,-7,-2,-7);
        outline(c,active,-4,-10,-9,-6,-11,0,-7,2,-5,-1,-5,9,0,11,5,9,5,-1,7,2,11,0,9,-6,4,-10,2,-7,-2,-7);
        stroke(c,0xff626c70,1,-4,-4,0,-1,4,-4);stroke(c,0xff626c70,1,0,-1,0,8);stroke(c,GOLD,.8f,-4,6,0,7,4,6);break;
      case "attack":
        polygon(c,active,-6,5,5,-11,11,-13,9,-7,-2,9);outline(c,active,-6,5,5,-11,11,-13,9,-7,-2,9);
        stroke(c,0xff6d7b82,.8f,-4,6,9,-11);polygon(c,true,-9,2,-7,0,3,8,2,11);
        polygon(c,false,-8,6,-5,9,-9,14,-12,12);stroke(c,GOLD,.8f,-10,9,-7,11);p.setColor(GOLD);c.drawCircle(-11,13,1.8f,p);break;
      case "skill":
        polygon(c,active,-10,-8,-3,-10,0,-7,3,-10,10,-8,10,8,3,6,0,9,-3,6,-10,8);
        outline(c,active,-10,-8,-3,-10,0,-7,3,-10,10,-8,10,8,3,6,0,9,-3,6,-10,8);
        stroke(c,0xff414c51,1,0,-6,0,8);stroke(c,0xff68747b,.7f,-7,-4,-3,-5);stroke(c,0xff68747b,.7f,-7,-1,-3,-2);stroke(c,0xff68747b,.7f,-7,2,-3,1);
        polygon(c,true,5,-5,6,-1,9,0,6,1,5,4,4,1,1,0,4,-1);break;
      case "quest":
        polygon(c,active,-6,-10,8,-10,8,7,5,10,-8,10,-8,6,-6,6);outline(c,active,-6,-10,8,-10,8,7,5,10,-8,10,-8,6,-6,6);
        stroke(c,0xff58666e,.8f,-3,-5,5,-5);stroke(c,0xff58666e,.8f,-3,-1,5,-1);stroke(c,0xff58666e,.8f,-3,3,2,3);stroke(c,0xff86949a,.9f,5,7,5,10);
        p.setColor(GOLD);c.drawCircle(-6,-8,2.5f,p);break;
      default:stroke(c,0x887d806f,1,-4,0,4,0);stroke(c,0x887d806f,1,0,-4,0,4);
    }
    p.setShader(null);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);c.restore();
  }
  private static void shape(float... points){path.reset();path.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);path.close();}
  private static void polygon(Canvas c,boolean gold,float... points){shape(points);p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(-8,-12,7,12,new int[]{gold?0xfffcf0c6:0xffffffff,gold?0xffd0ab61:0xffc0cbd0,gold?0xfff0dbaa:0xfff8f9f1},new float[]{0,.6f,1},Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);}
  private static void outline(Canvas c,boolean gold,float... points){shape(points);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.65f);p.setColor(gold?0xff826637:0xff3e4b50);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);}
  private static void stroke(Canvas c,int color,float width,float... points){path.reset();path.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(color);p.setStrokeWidth(width);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);}
  static void combat(Canvas c,float x,float y,float r,boolean auto,boolean active){
    p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(x-r*.3f,y-r*.4f,r*1.6f,new int[]{active?0xc7675b35:0xd0354244,0xe00d161b},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setShader(new LinearGradient(x-r,y-r,x+r,y+r,new int[]{0xffeee7cc,0xff667372,0xffc8ba91},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r-1,p);p.setShader(null);p.setStrokeWidth(.65f);p.setColor(active?0xffe1c783:0xff718381);c.drawCircle(x,y,r-3.5f,p);p.setStyle(Paint.Style.FILL);
    if(auto){
      // Two stacked words match the reference's compact AUTO mark. Orbit arrows
      // stay on the perimeter and do not cross the letter shapes.
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.8f);p.setColor(active?GOLD:0xffacbab8);c.drawArc(x-r+4,y-r+4,x+r-4,y+r-4,205,65,false,p);c.drawArc(x-r+4,y-r+4,x+r-4,y+r-4,25,65,false,p);p.setStyle(Paint.Style.FILL);
      UiTheme.center(c,"AU",x,y-.5f,8,active?GOLD:WHITE,true);UiTheme.center(c,"TO",x,y+7.5f,8,active?GOLD:WHITE,true);

    }else{
      icon(c,"attack",x,y-1,r*.58f,active);
      for(int i=0;i<4;i++){double a=(i*90+45)*Math.PI/180;p.setColor(0xffd9cda5);c.drawCircle(x+(float)Math.cos(a)*(r-1),y+(float)Math.sin(a)*(r-1),1.3f,p);}
    }
    p.setStyle(Paint.Style.FILL);
  }
  static void questSeal(Canvas c,float x,float y,QuestJournalModel.Status status){
    p.setColor(status==QuestJournalModel.Status.LOCKED?0x77465743:0xaa8e7137);c.drawRoundRect(x-8,y-9,x+8,y+9,2,2,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.7f);p.setColor(status==QuestJournalModel.Status.LOCKED?0xff80927c:GOLD);c.drawRoundRect(x-8,y-9,x+8,y+9,2,2,p);p.setStyle(Paint.Style.FILL);
    String glyph=status==QuestJournalModel.Status.REPORT?"✓":status==QuestJournalModel.Status.ACTIVE?"›":status==QuestJournalModel.Status.LOCKED?"·":"!";UiTheme.center(c,glyph,x,y+4,12,WHITE,true);
  }
  static void potion(Canvas c,float x,float y,boolean hp){
    p.setStyle(Paint.Style.FILL);p.setColor(0xff242a2d);c.drawRoundRect(x-7,y-5,x+7,y+9,4,4,p);
    p.setShader(new LinearGradient(x-5,y,x+5,y+7,hp?0xffe99d9a:0xffa5dbef,hp?0xff983b42:0xff386bba,Shader.TileMode.CLAMP));c.drawRoundRect(x-5,y,x+5,y+7,2,2,p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xffc6c7bb);c.drawRoundRect(x-7,y-5,x+7,y+9,4,4,p);c.drawRect(x-3,y-10,x+3,y-5,p);c.drawLine(x-3,y-1,x-3,y+4,p);
    p.setStyle(Paint.Style.FILL);p.setColor(GOLD);c.drawRoundRect(x-4,y-12,x+4,y-9,1,1,p);
  }
  static void minimap(Canvas c,RuntimeState state,float px,float py,float minX,float minY,float maxX,float maxY){
    final float cx=68,cy=73,r=49;ring(c,cx,cy,r+2,false);c.save();path.reset();path.addCircle(cx,cy,r-2,Path.Direction.CW);c.clipPath(path);
    String key=state.currentMapId()+":"+minX+":"+maxX+":"+minY+":"+maxY;
    if(mapTerrain==null||!key.equals(terrainKey)){
      if(mapTerrain!=null)mapTerrain.recycle();mapTerrain=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);Canvas terrain=new Canvas(mapTerrain);terrain.drawColor(0xff625943);
      p.setStyle(Paint.Style.FILL);p.setColor(0xff3f4135);
      // Project the authored static collision footprints once per map, never live actors or paths.
      for(RectF o:state.obstacles())terrain.drawRect(map(o.left,minX,maxX,0,96),map(o.top,minY,maxY,0,96),map(o.right,minX,maxX,0,96),map(o.bottom,minY,maxY,0,96),p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.6f);p.setColor(0x339c8c65);for(int i=1;i<5;i++){float v=i*19.2f;terrain.drawLine(v,0,v,96,p);terrain.drawLine(0,v,96,v,p);}p.setStyle(Paint.Style.FILL);terrainKey=key;
    }
    p.setColor(0xffffffff);c.drawBitmap(mapTerrain,null,new RectF(20,25,116,121),p);
    for(RuntimeState.Npc n:state.npcs())marker(c,n.x,n.y,minX,minY,maxX,maxY,0xffc3dfad,1.8f);
    for(RuntimeState.Monster m:state.monsters())if(m.alive&&Math.abs(m.x-px)<600&&Math.abs(m.y-py)<360)marker(c,m.x,m.y,minX,minY,maxX,maxY,0xffd98a68,1.3f);
    float x=map(px,minX,maxX,22,114),y=map(py,minY,maxY,27,119);p.setColor(GOLD);path.reset();path.moveTo(x,y-4);path.lineTo(x-3,y+3);path.lineTo(x,y+1.5f);path.lineTo(x+3,y+3);path.close();c.drawPath(path,p);p.setStyle(Paint.Style.STROKE);p.setColor(0xccf8f1ca);p.setStrokeWidth(.7f);c.drawCircle(x,y,6,p);p.setStyle(Paint.Style.FILL);c.restore();
    UiTheme.center(c,"N",cx,31,7,GOLD,true);
  }
  private static float map(float value,float lo,float hi,float start,float end){return start+Math.max(0,Math.min(1,(value-lo)/Math.max(1,hi-lo)))*(end-start);}
  private static void marker(Canvas c,float x,float y,float minX,float minY,float maxX,float maxY,int color,float size){p.setColor(color);c.drawCircle(map(x,minX,maxX,22,114),map(y,minY,maxY,27,119),size,p);}
}
