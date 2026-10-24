package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Original-art overview. Entrance selection uses the existing live world pathfinder. */
final class WorldMapWindow {
 interface Baker {void draw(Canvas canvas);}
 static final class Pin {
  final String label,destination;final float x,y,targetX,targetY;final List<Pin> members;
  Pin(String label,float x,float y){this(label,label,x,y,x,y);}
  Pin(String label,float x,float y,float tx,float ty){this(label,label,x,y,tx,ty);}
  Pin(String label,String destination,float x,float y,float tx,float ty){this(label,destination,x,y,tx,ty,Collections.emptyList());}
  Pin(String label,String destination,float x,float y,float tx,float ty,List<Pin> members){this.label=label;this.destination=destination;this.x=x;this.y=y;targetX=tx;targetY=ty;this.members=Collections.unmodifiableList(new ArrayList<>(members));}
 }

 boolean open;private String map,title;private Bitmap image;private float minX,minY,q,padX,padY;private int builds;
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final RectF area=new RectF();private final java.util.List<Pin> pins=new ArrayList<>();
 static boolean minimapHit(float x,float y){return Math.hypot(x-68,y-73)<=51;}
 static String gateLabel(DeathVillageData.Gate g){if(DeathVillageGeometry.FIELD.equals(g.to))return "죽음의마을";if(DeathVillageGeometry.LOBBY.equals(g.to))return "대기실";if(DeathVillageGeometry.house(g.to))return "건물 "+g.to.substring(g.to.length()-1);return "지하 "+g.to.substring(g.to.length()-1);}
 private static final Map<String,List<Pin>> ENTRANCES=new HashMap<>();
 static List<Pin> entrances(String id){return ENTRANCES.computeIfAbsent(id,key->Collections.unmodifiableList(groupEntrances(rawEntrances(key))));}
 /** Raw display anchors keep destination identity and the existing activation coordinate separate. */
 static List<Pin> rawEntrances(String id){
  id=RucesionTownGeometry.canonical(AbelTownGeometry.canonical(AbelCoastGeometry.canonical(id)));
  List<Pin> out=new ArrayList<>();TownInteriorDef room=TownInteriorDef.forMap(id);
  if(room!=null){
   if(room.isTemple())for(TempleRoutes.Gate g:TempleRoutes.forRoom(room))out.add(new Pin(g.label,g.exit?interiorExitMap(room):g.target,g.x,g.y,g.x,g.y));
   else out.add(new Pin("출구",interiorExitMap(room),room.exitX(),room.exitY(),room.exitX(),room.exitY()));
  }else if(HorrorCastleGeometry.contains(id)){
   for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(id))out.add(new Pin(d.label,"MILLES".equals(d.destination)?WorldDef.ID:d.destination,d.markerX,d.markerY,d.x,d.y));
  }else if(MineHuntGeometry.contains(id)){
   WorldMoveTargetController.TileCenter exit=MineHuntGeometry.exit();out.add(new Pin("마인 마을",MineTownGeometry.ID,exit.x,exit.y,exit.x,exit.y));
  }else if(DeathVillageGeometry.contains(id)){
   for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))out.add(new Pin(gateLabel(g),g.to,DeathVillageGeometry.portalX(g),DeathVillageGeometry.portalY(g),DeathVillageGeometry.x(g.u,g.v),DeathVillageGeometry.y(g.u,g.v)));
  }else if(WorldDef.ID.equals(id))out.addAll(rawMillesEntrances(new WorldDef().portalSpawns()));
  else{
   if(AbelTownGeometry.contains(id))for(AbelTownGeometry.Building b:AbelTownGeometry.BUILDINGS)out.add(townPin(b.interior,b.doorX(),b.doorY()));
   else if(RucesionTownGeometry.contains(id))for(RucesionTownGeometry.Building b:RucesionTownGeometry.BUILDINGS)out.add(townPin(b.interior,b.doorX(),b.doorY()));
   else if(MineTownGeometry.contains(id))for(MineTownGeometry.Building b:MineTownGeometry.BUILDINGS)out.add(townPin(b.interior,b.doorX(),b.doorY()));
   if(MineTownGeometry.contains(id))out.add(new Pin("카스마늄 광산",MineHuntGeometry.ID,MineTownGeometry.x(14,23),MineTownGeometry.y(14,23),MineTownGeometry.x(15,23),MineTownGeometry.y(15,23)));
   if((CampaignWorld.contains(id)&&!TownTravel.isTown(id))||CampaignWorld.PIET.equals(id)){
    WorldMoveTargetController.TileCenter back=CampaignWorld.backward(id);String previous=CampaignWorld.previous(id),destination=previous==null?WorldDef.ID:previous;
    out.add(new Pin(previous==null?"밀레스 마을":CampaignWorld.title(previous),destination,back.x,back.y,back.x,back.y));
    String next=CampaignWorld.next(id);if(next!=null){WorldMoveTargetController.TileCenter forward=CampaignWorld.forward(id);out.add(new Pin(CampaignWorld.title(next),next,forward.x,forward.y,forward.x,forward.y));}
   }
  }
  return out;
 }
 private static Pin townPin(String destination,float x,float y){return new Pin(TownServiceLabels.name(TownInteriorDef.forMap(destination)),destination,x,y,x,y);}
 static String interiorExitMap(TownInteriorDef room){
  if(room.isTemple())return room.mapId.endsWith("_lobby")?WorldDef.ID:room.mapId.endsWith("_hub")?"milles_interior_temple_lobby":"milles_interior_temple_hub";
  if(room.mapId.startsWith("abel_"))return AbelTownGeometry.ID;if(room.mapId.startsWith("rucesion_"))return RucesionTownGeometry.ID;if(room.mapId.startsWith("mine_"))return MineTownGeometry.ID;return WorldDef.ID;
 }
 private static List<Pin> rawMillesEntrances(List<WorldDef.PortalSpawn> portals){List<Pin> out=new ArrayList<>();for(WorldDef.PortalSpawn portal:portals){TownInteriorDef room=TownInteriorDef.forMap(portal.targetMapId);out.add(new Pin(room==null?"포테의 숲":TownServiceLabels.name(room),portal.targetMapId,portal.x,portal.y,portal.x,portal.y));}return out;}
 static List<Pin> millesEntrances(List<WorldDef.PortalSpawn> portals){return groupEntrances(rawMillesEntrances(portals));}
 /** Shared by every map: one name per same-destination connected entrance, real target retained. */
 static List<Pin> groupEntrances(List<Pin> raw){
  List<Pin> out=new ArrayList<>();boolean[] used=new boolean[raw.size()];
  for(int i=0;i<raw.size();i++){
   if(used[i])continue;Pin first=raw.get(i);List<Pin> cluster=new ArrayList<>();cluster.add(first);used[i]=true;
   for(int cursor=0;cursor<cluster.size();cursor++){
    Pin member=cluster.get(cursor);
    for(int j=0;j<raw.size();j++){Pin other=raw.get(j);if(!used[j]&&first.destination.equals(other.destination)&&Math.hypot(member.targetX-other.targetX,member.targetY-other.targetY)<=72f){used[j]=true;cluster.add(other);}}
   }
   float x=0,y=0;for(Pin pin:cluster){x+=pin.x;y+=pin.y;}x/=cluster.size();y/=cluster.size();
   Pin target=first;double best=Double.MAX_VALUE;for(Pin pin:cluster){double d=Math.hypot(pin.targetX-x,pin.targetY-y);if(d<best){best=d;target=pin;}}
   out.add(new Pin(first.label,first.destination,x,y,target.targetX,target.targetY,cluster));
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
