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
  static void icon(Canvas c,String kind,float x,float y,float r,boolean active){
    c.save();c.translate(x,y);c.scale(r/11f,r/11f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setColor(active?GOLD:WHITE);
    switch(kind){
      case "bag":c.drawRoundRect(-8,-6,8,9,2,2,p);c.drawArc(-5,-12,5,-2,180,180,false,p);c.drawLine(-8,-1,8,-1,p);c.drawRect(-2,-2,2,2,p);break;
      case "stats":c.drawCircle(0,0,9,p);for(int i=0;i<6;i++){double a=i*Math.PI/3;c.drawLine((float)Math.cos(a)*4,(float)Math.sin(a)*4,(float)Math.cos(a)*7,(float)Math.sin(a)*7,p);}c.drawLine(0,0,5,-5,p);break;
      case "equipment":case "attack":path.reset();path.moveTo(-7,8);path.lineTo(4,-9);path.lineTo(9,-11);path.lineTo(9,-6);path.lineTo(-4,10);c.drawPath(path,p);c.drawLine(-8,2,1,9,p);c.drawLine(-9,11,-6,7,p);if(kind.equals("equipment")){c.drawCircle(-7,-7,3,p);c.drawLine(-7,-4,4,10,p);}break;
      case "skill":path.reset();path.moveTo(-10,-7);path.lineTo(-2,-9);path.lineTo(0,-6);path.lineTo(3,-9);path.lineTo(10,-7);path.lineTo(10,9);path.lineTo(3,7);path.lineTo(0,10);path.lineTo(-2,7);path.lineTo(-10,9);path.close();c.drawPath(path,p);c.drawLine(0,-6,0,10,p);c.drawLine(-7,-2,-3,-3,p);c.drawLine(3,-3,7,-2,p);break;
      case "quest":c.drawRoundRect(-7,-10,7,10,1,1,p);c.drawLine(-3,-5,4,-5,p);c.drawLine(-3,0,4,0,p);c.drawLine(-3,5,2,5,p);c.drawLine(-9,-6,-9,7,p);break;
      default:p.setColor(0x887d806f);c.drawLine(-4,0,4,0,p);c.drawLine(0,-4,0,4,p);
    }p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);c.restore();
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
    for(RuntimeState.Monster m:state.monsters())if(m.alive)marker(c,m.x,m.y,minX,minY,maxX,maxY,0xffd98a68,1.3f);
    float x=map(px,minX,maxX,22,114),y=map(py,minY,maxY,27,119);p.setColor(GOLD);path.reset();path.moveTo(x,y-4);path.lineTo(x-3,y+3);path.lineTo(x,y+1.5f);path.lineTo(x+3,y+3);path.close();c.drawPath(path,p);p.setStyle(Paint.Style.STROKE);p.setColor(0xccf8f1ca);p.setStrokeWidth(.7f);c.drawCircle(x,y,6,p);p.setStyle(Paint.Style.FILL);c.restore();
    UiTheme.center(c,"N",cx,31,7,GOLD,true);
  }
  private static float map(float value,float lo,float hi,float start,float end){return start+Math.max(0,Math.min(1,(value-lo)/Math.max(1,hi-lo)))*(end-start);}
  private static void marker(Canvas c,float x,float y,float minX,float minY,float maxX,float maxY,int color,float size){p.setColor(color);c.drawCircle(map(x,minX,maxX,22,114),map(y,minY,maxY,27,119),size,p);}
}
