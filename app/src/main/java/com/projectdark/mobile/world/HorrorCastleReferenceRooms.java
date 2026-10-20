package com.projectdark.mobile.world;
import java.util.*;
/** V207 screenshot-guided rooms. Native tile pixels; inferred positions are ADAPTED, not recovered maps. */
public final class HorrorCastleReferenceRooms {
 public static final class Piece {
  public final int sprite,u,v,side,anchorU,anchorV,sourceU,sourceV; public final boolean mirror;
  Piece(int sprite,int u,int v,int side,int au,int av,int su,int sv,boolean mirror){this.sprite=sprite;this.u=u;this.v=v;this.side=side;anchorU=au;anchorV=av;sourceU=su;sourceV=sv;this.mirror=mirror;}
 }
 private static final Map<String,HorrorCastleOriginalData.Grid> GRIDS=new HashMap<>();
 private static final Map<String,List<Piece>> PIECES=new HashMap<>();
 private static void block(List<Piece> out,int source,int l,int t,int r,int b,int au,int av,boolean mirror){
  HorrorCastleOriginalData.Grid g=HorrorCastleOriginalData.get(String.format(Locale.ROOT,"MAP_HORROR_ROOM_%02d",source));
  for(int v=t;v<=b;v++)for(int u=l;u<=r;u++)for(int side=1;side<=2;side++){int f=g.value(u,v,side);if(f>10012)out.add(new Piece(f,u,v,side,au,av,l,t,mirror));}
 }
 private static void pair(List<Piece> out,int left,int right,int u,int v){out.add(new Piece(left,0,0,1,u,v,0,0,false));out.add(new Piece(right,0,0,2,u,v,0,0,false));}
 public static synchronized List<Piece> pieces(String id){
  List<Piece> p=PIECES.get(id);if(p!=null)return p;int n=HorrorCastleGeometry.room(id);p=new ArrayList<>();
  if(n==12){
   // Two fireplaces and a stone bust on the right wall; writing desk and armchair at the curtains.
   block(p,11,1,1,1,4,3,1,true);block(p,11,1,11,1,14,10,1,true);
   pair(p,11001,11002,15,1);block(p,7,5,10,5,12,2,8,false);pair(p,11005,11006,1,7);pair(p,11019,11020,9,6);
  }else if(n==13){
   // Gold chair at the left window, low wall shelf by the right-hand door.
   pair(p,11315,11316,1,5);block(p,2,13,1,14,1,3,1,false);pair(p,11019,11020,11,10);
  }else if(n==14){
   // Corner armchair and writing stand, paired stone figures in the foreground.
   pair(p,11003,11004,1,2);pair(p,11017,11018,2,3);pair(p,10999,11000,3,11);pair(p,11001,11002,12,6);
  }else if(n==15){
   // The capture clips the bed's far side: native upholstered pieces and busts reconstruct that motif.
   block(p,11,1,5,1,6,1,3,false);pair(p,11321,11322,1,2);pair(p,11323,11324,1,5);
   // Source painting panels, same wall/door orientation as the reference.
   pair(p,11223,11224,11,1);
  }
  p=Collections.unmodifiableList(p);PIECES.put(id,p);return p;
 }
 public static synchronized HorrorCastleOriginalData.Grid grid(String id){int n=HorrorCastleGeometry.room(id);if(n<12||n>15)return null;HorrorCastleOriginalData.Grid result=GRIDS.get(id);if(result!=null)return result;
  HorrorCastleOriginalData.Grid base=HorrorCastleOriginalData.get("MAP_HORROR_ROOM_03");int w=base.width,h=base.height;byte[] cells=new byte[w*h*6],walk=new byte[(w*h+7)/8];
  for(int v=0;v<h;v++)for(int u=0;u<w;u++){
   for(int layer=0;layer<3;layer++){int value=base.value(u,v,layer);if(u>0&&v>0){if(layer>0)value=0;else value=11051+Math.floorMod(u*17+v*31,6);}int i=((v*w+u)*3+layer)*2;cells[i]=(byte)value;cells[i+1]=(byte)(value>>8);}
   if((u>0&&v>0)||base.walkable(u,v)){int i=v*w+u;walk[i/8]|=1<<(i%8);}
  }
  for(Piece p:pieces(id)){int du=p.u-p.sourceU,dv=p.v-p.sourceV;int u=p.anchorU+(p.mirror?dv:du),v=p.anchorV+(p.mirror?du:dv);if(u>=0&&v>=0&&u<w&&v<h){int i=v*w+u;walk[i/8]&=~(1<<(i%8));}}
  result=new HorrorCastleOriginalData.Grid(0,w,h,Base64.getEncoder().encodeToString(cells),Base64.getEncoder().encodeToString(walk));GRIDS.put(id,result);return result;
 }
 private HorrorCastleReferenceRooms(){}
}
