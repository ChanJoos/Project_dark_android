package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Original-art overview. Entrance selection uses the existing live world pathfinder. */
final class WorldMapWindow {
 interface Baker {void draw(Canvas canvas);}
 static final class Pin {final String label;final float x,y,targetX,targetY;Pin(String label,float x,float y){this(label,x,y,x,y);}Pin(String label,float x,float y,float tx,float ty){this.label=label;this.x=x;this.y=y;targetX=tx;targetY=ty;}}
 boolean open;private String map,title;private Bitmap image;private float minX,minY,q,padX,padY;private int builds;
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final RectF area=new RectF();private final java.util.List<Pin> pins=new ArrayList<>();
 static boolean minimapHit(float x,float y){return Math.hypot(x-68,y-73)<=51;}
 static String gateLabel(DeathVillageData.Gate g){if(DeathVillageGeometry.FIELD.equals(g.to))return "죽음의마을";if(DeathVillageGeometry.LOBBY.equals(g.to))return "대기실";if(DeathVillageGeometry.house(g.to))return "건물 "+g.to.substring(g.to.length()-1);return "지하 "+g.to.substring(g.to.length()-1);}
 static java.util.List<Pin> entrances(String id){
  java.util.List<Pin> out=new ArrayList<>();
  if(DeathVillageGeometry.contains(id)){
   Set<String> seen=new HashSet<>();
   for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id)){
    if(!seen.add(g.to))continue;float x=0,y=0;int n=0;
    for(DeathVillageData.Gate other:DeathVillageGeometry.gates(id))if(g.to.equals(other.to)){x+=DeathVillageGeometry.portalX(other);y+=DeathVillageGeometry.portalY(other);n++;}
    float cx=x/n,cy=y/n;DeathVillageData.Gate target=g;float best=Float.MAX_VALUE;
    for(DeathVillageData.Gate other:DeathVillageGeometry.gates(id))if(g.to.equals(other.to)){float d=(float)Math.hypot(DeathVillageGeometry.x(other.u,other.v)-cx,DeathVillageGeometry.y(other.u,other.v)-cy);if(d<best){best=d;target=other;}}
    out.add(new Pin(gateLabel(g),cx,cy,DeathVillageGeometry.x(target.u,target.v),DeathVillageGeometry.y(target.u,target.v)));
   }
  }else if(WorldDef.ID.equals(id)){
   for(WorldDef.PortalSpawn portal:new WorldDef().portalSpawns()){
    TownInteriorDef interior=TownInteriorDef.forDoor(portal.portalId);
    String label=interior==null?"포테의 숲":TownServiceLabels.name(interior);
    out.add(new Pin(label,portal.x,portal.y));
   }
  }else{
   for(TownServiceLabels.Sign sign:TownServiceLabels.signs(id))out.add(new Pin(sign.name,sign.x,sign.y));
   if(MineTownGeometry.contains(id))out.add(new Pin("카스마늄 광산",MineTownGeometry.x(14,23),MineTownGeometry.y(14,23),MineTownGeometry.x(15,23),MineTownGeometry.y(15,23)));
   if(CampaignWorld.PIET.equals(id)){WorldMoveTargetController.TileCenter back=CampaignWorld.backward(id);out.add(new Pin(CampaignWorld.title(CampaignWorld.previous(id)),back.x,back.y));}
  }
  return out;
 }

 void show(String id,String title,float minX,float minY,float maxX,float maxY,Baker baker){if(!id.equals(map)){if(image!=null)image.recycle();image=Bitmap.createBitmap(1024,512,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(0xff151711);this.minX=minX;this.minY=minY;q=Math.min(1024/(maxX-minX),512/(maxY-minY));padX=(1024-(maxX-minX)*q)/2;padY=(512-(maxY-minY)*q)/2;canvas.save();canvas.translate(padX,padY);canvas.scale(q,q);try{baker.draw(canvas);}finally{canvas.restore();}image.prepareToDraw();map=id;this.title=title;pins.clear();pins.addAll(entrances(id));builds++;}open=true;}
 void close(){open=false;}int builds(){return builds;}long bytes(){return image==null?0:image.getAllocationByteCount();}java.util.List<Pin> pins(){return Collections.unmodifiableList(pins);}
 private float px(float x){return area.left+(padX+(x-minX)*q)*area.width()/1024;}private float py(float y){return area.top+(padY+(y-minY)*q)*area.height()/512;}
 void draw(Canvas canvas,float playerX,float playerY,float offset){paint.setColor(0xe6111614);canvas.drawRect(0,0,960+offset*2,540,paint);area.set(80+offset,72,880+offset,472);paint.setColor(Color.WHITE);paint.setFilterBitmap(true);canvas.drawBitmap(image,null,area,paint);paint.setFilterBitmap(false);UiTheme.center(canvas,title,480+offset,42,17,UiTheme.TEXT,true);UiTheme.close(canvas,907+offset,36);
  for(Pin pin:pins){float x=px(pin.x),y=py(pin.y);paint.setColor(0xffe9ddbc);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f);canvas.drawCircle(x,y,6,paint);canvas.drawLine(x-2,y,x+2,y,paint);canvas.drawLine(x,y-2,x,y+2,paint);paint.setStyle(Paint.Style.FILL);paint.setTypeface(UiTheme.font(true));paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(10);paint.setColor(0xff131713);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);canvas.drawText(pin.label,x,y-11,paint);paint.setStyle(Paint.Style.FILL);paint.setColor(0xffffe4aa);canvas.drawText(pin.label,x,y-11,paint);}
  float x=px(playerX),y=py(playerY);paint.setColor(0xffffdf72);Path arrow=new Path();arrow.moveTo(x,y-8);arrow.lineTo(x-5,y+5);arrow.lineTo(x,y+2);arrow.lineTo(x+5,y+5);arrow.close();canvas.drawPath(arrow,paint);paint.setTextAlign(Paint.Align.LEFT);UiTheme.center(canvas,"현재 위치 ▲     입구 ⊕ 터치하여 이동",480+offset,507,11,UiTheme.GOLD,true);}
 float screenX(Pin pin,float offset){return 80+offset+(padX+(pin.x-minX)*q)*800/1024;}
 float screenY(Pin pin){return 72+(padY+(pin.y-minY)*q)*400/512;}
 Pin touch(float x,float y,float offset){if(Math.hypot(x-(907+offset),y-36)<24||minimapHit(x,y)){close();return null;}Pin best=null;float distance=24;for(Pin pin:pins){float d=(float)Math.hypot(x-screenX(pin,offset),y-screenY(pin));if(d<distance){distance=d;best=pin;}}if(best!=null)close();return best;}
}
