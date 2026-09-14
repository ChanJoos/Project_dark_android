package com.projectdark.mobile;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runtime/source evidence for the 2026-09-14 Visual attack + robe registration repair. */
public final class CharacterVisualEvidenceProbeV2 {
  private static final int W=CharacterRenderer.SOURCE_FRAME_WIDTH,H=CharacterRenderer.SOURCE_FRAME_HEIGHT;
  private static final int[] BODY_W={17,18,19,21},BODY_H={51,52,51,49},BODY_X={8,2,6,4},BODY_Y={8,8,9,3};
  private static final int[] ROBE_W={14,19,15,20},ROBE_H={33,22,30,21};
  private static final float PIVOT_X=2f,PIVOT_Y=4f;

  public static final class Bounds {
    public final int left,top,right,bottom;
    Bounds(int l,int t,int r,int b){left=l;top=t;right=r;bottom=b;}
    public boolean empty(){return right<left||bottom<top;}
    public int height(){return empty()?0:bottom-top+1;}
    public float centerX(){return empty()?Float.NaN:(left+right)*.5f;}
    public Bounds shifted(float dx,float dy){return empty()?this:new Bounds(Math.round(left+dx),Math.round(top+dy),Math.round(right+dx),Math.round(bottom+dy));}
    @Override public String toString(){return empty()?"empty":"["+left+","+top+".."+right+","+bottom+"]";}
  }

  public static final class WalkRegistration {
    public final CharacterRenderer.Direction direction;public final int column;public final Bounds body,robe,registeredRobe;
    public final float correctionX,correctionY,centerDelta;public final int footDelta;
    WalkRegistration(CharacterRenderer.Direction d,int c,Bounds b,Bounds r,float x,float y){direction=d;column=c;body=b;robe=r;correctionX=x;correctionY=y;registeredRobe=r.shifted(x,y);centerDelta=registeredRobe.centerX()-body.centerX();footDelta=registeredRobe.bottom-body.bottom;}
    @Override public String toString(){return direction+"/c"+column+" body="+body+" robe="+robe+" corr=("+correctionX+","+correctionY+") regCenter="+centerDelta+" regFoot="+footDelta;}
  }

  public static final class AttackGeometry {
    public final CharacterRenderer.Direction direction;public final int sourceIndex;public final Bounds idleBody,rawAction;
    public final int cropTop,cropBottomExclusive,normalizedHeight,offsetX,offsetY,heightError,footError,centerError;public final boolean pass;
    AttackGeometry(CharacterRenderer.Direction d,int source,Bounds idle,Bounds raw){
      direction=d;sourceIndex=source;idleBody=idle;rawAction=raw;
      if(idle.empty()||raw.empty()){cropTop=0;cropBottomExclusive=0;normalizedHeight=0;offsetX=offsetY=0;heightError=footError=centerError=999;pass=false;return;}
      cropTop=Math.max(raw.top,raw.bottom-idle.height()+1);cropBottomExclusive=raw.bottom+1;normalizedHeight=cropBottomExclusive-cropTop;
      int globalBottom=BODY_Y[source]+raw.bottom;float globalCenter=BODY_X[source]+raw.centerX();
      offsetY=idle.bottom-globalBottom;offsetX=Math.round(idle.centerX()-globalCenter);
      heightError=Math.abs(idle.height()-normalizedHeight);footError=Math.abs(idle.bottom-(globalBottom+offsetY));centerError=Math.round(Math.abs(idle.centerX()-(globalCenter+offsetX)));
      pass=CharacterRenderer.alphaGeometryWithinTolerance(idle.height(),normalizedHeight,footError,centerError);
    }
    @Override public String toString(){return direction+" src="+sourceIndex+" idle="+idleBody+" raw="+rawAction+" cropY="+cropTop+".."+(cropBottomExclusive-1)+" normH="+normalizedHeight+" shift=("+offsetX+","+offsetY+") err(h/f/c)="+heightError+"/"+footError+"/"+centerError+" pass="+pass;}
  }

  public static final class AttackLayers {
    public final CharacterRenderer.Direction direction;public final boolean body,robe,weapon,atomic,noWeaponOnlyFallback;
    AttackLayers(CharacterRenderer.Direction d,boolean b,boolean r,boolean w){direction=d;body=b;robe=r;weapon=w;atomic=CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",b,r,w);noWeaponOnlyFallback=!CharacterRenderer.attackFallbackWeaponSwingReachable();}
    @Override public String toString(){return direction+" BODY="+body+" ROBE="+robe+" MW001="+weapon+" atomic="+atomic+" noWeaponOnlyFallback="+noWeaponOnlyFallback;}
  }

  public static final class WeaponAttack {
    public final CharacterRenderer.Direction direction;public final boolean mirror;public final float handX,handY,tipX,tipY,angle,tipDistance;
    WeaponAttack(CharacterRenderer.Direction d,boolean m,float hx,float hy,float tx,float ty,float a,float len){direction=d;mirror=m;handX=hx;handY=hy;tipX=tx;tipY=ty;angle=a;tipDistance=len;}
    @Override public String toString(){return direction+" mirror="+mirror+" hand=("+handX+","+handY+") tip=("+tipX+","+tipY+") angle="+angle+" len="+tipDistance;}
  }

  public static final class Evidence {
    public final boolean resourcesReady,swSeRegistrationPass,attackGeometryPass,attackAtomicPass,weaponTransformPass;
    public final List<WalkRegistration> walk;public final List<AttackGeometry> attack;public final List<AttackLayers> layers;public final List<WeaponAttack> weapons;
    Evidence(boolean rr,boolean wp,boolean ap,boolean lp,boolean mp,List<WalkRegistration>w,List<AttackGeometry>a,List<AttackLayers>l,List<WeaponAttack>m){resourcesReady=rr;swSeRegistrationPass=wp;attackGeometryPass=ap;attackAtomicPass=lp;weaponTransformPass=mp;walk=Collections.unmodifiableList(w);attack=Collections.unmodifiableList(a);layers=Collections.unmodifiableList(l);weapons=Collections.unmodifiableList(m);}
    public boolean passes(){return resourcesReady&&swSeRegistrationPass&&attackGeometryPass&&attackAtomicPass&&weaponTransformPass;}
    public String summary(){return "resources="+resourcesReady+",walk20="+walk.size()+",swSeXY="+swSeRegistrationPass+",attack4="+attackGeometryPass+",atomic4="+attackAtomicPass+",weapon4="+weaponTransformPass;}
  }

  private CharacterVisualEvidenceProbeV2(){}

  public static Evidence collect(Resources r){
    Bitmap body=load(r,CharacterRenderer.IDLE_WALK_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap robe=load(r,CharacterRenderer.LUERS_ROBE_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap weapon=load(r,CharacterRenderer.MOKDO_RESOURCE,16,8);
    Bitmap[] bodyAction=new Bitmap[4],robeAction=new Bitmap[4];boolean ready=body!=null&&robe!=null&&weapon!=null;
    for(int i=0;i<4;i++){bodyAction[i]=load(r,"player_body_mm001_action02_"+i,BODY_W[i],BODY_H[i]);robeAction[i]=load(r,"player_robe_mu0000058_action02_"+i,ROBE_W[i],ROBE_H[i]);ready&=bodyAction[i]!=null&&robeAction[i]!=null;}

    List<WalkRegistration> walk=new ArrayList<>();boolean walkPass=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int row=CharacterRenderer.atlasRow(d);Bounds idleB=cellBounds(body,row,0),idleR=cellBounds(robe,row,0);float baseX=CharacterRenderer.robeFrameRegistrationX(d,0);
      float targetCenter=(idleR.centerX()+baseX)-idleB.centerX();int targetFoot=idleR.bottom-idleB.bottom;
      for(int col=0;col<5;col++){
        Bounds b=cellBounds(body,row,col),rr=cellBounds(robe,row,col);float x,y;
        if(CharacterRenderer.robeUsesRuntimeXYRegistration(d)){x=Math.round(targetCenter-(rr.centerX()-b.centerX()));y=Math.round(targetFoot-(rr.bottom-b.bottom));}
        else{x=CharacterRenderer.robeFrameRegistrationX(d,col);y=0f;}
        WalkRegistration sample=new WalkRegistration(d,col,b,rr,x,y);walk.add(sample);
        if(CharacterRenderer.robeUsesRuntimeXYRegistration(d))walkPass&=!b.empty()&&!rr.empty()&&Math.abs(sample.centerDelta-targetCenter)<=.5f&&Math.abs(sample.footDelta-targetFoot)<=1;
      }
    }

    List<AttackGeometry> attacks=new ArrayList<>();boolean attackPass=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int row=CharacterRenderer.atlasRow(d),src=CharacterRenderer.actionSourceIndex(d);AttackGeometry g=new AttackGeometry(d,src,cellBounds(body,row,0),bounds(bodyAction[src]));attacks.add(g);attackPass&=g.pass;
    }

    List<AttackLayers> layers=new ArrayList<>();boolean layerPass=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int src=CharacterRenderer.actionSourceIndex(d);AttackLayers l=new AttackLayers(d,bodyAction[src]!=null,robeAction[src]!=null,weapon!=null);layers.add(l);layerPass&=l.atomic&&l.noWeaponOnlyFallback;
    }

    Bounds wb=bounds(weapon);float tipXLocal=PIVOT_X,tipYLocal=PIVOT_Y,maxD2=-1f;
    if(weapon!=null&&!wb.empty())for(int y=wb.top;y<=wb.bottom;y++)for(int x=wb.left;x<=wb.right;x++)if(((weapon.getPixel(x,y)>>>24)&0xff)!=0){float dx=x-PIVOT_X,dy=y-PIVOT_Y,d2=dx*dx+dy*dy;if(d2>maxD2){maxD2=d2;tipXLocal=x;tipYLocal=y;}}
    float expectedLen=(float)Math.sqrt(Math.max(0f,maxD2));List<WeaponAttack> weapons=new ArrayList<>();boolean weaponPass=weapon!=null&&!wb.empty()&&CharacterRenderer.weaponTransformUsesSingleMirror();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      float hx=CharacterRenderer.weaponAttackOffsetX(d),hy=-CharacterRenderer.weaponAttackOffsetY(d),lx=tipXLocal-PIVOT_X,ly=tipYLocal-PIVOT_Y;if(CharacterRenderer.weaponMirrorX(d))lx=-lx;
      float angle=CharacterRenderer.weaponAttackAngle(d,.55f),rad=(float)Math.toRadians(angle),rx=(float)(lx*Math.cos(rad)-ly*Math.sin(rad)),ry=(float)(lx*Math.sin(rad)+ly*Math.cos(rad));float len=(float)Math.sqrt(rx*rx+ry*ry);
      WeaponAttack sample=new WeaponAttack(d,CharacterRenderer.weaponMirrorX(d),hx,hy,hx+rx,hy+ry,angle,len);weapons.add(sample);weaponPass&=len>=5f&&Math.abs(len-expectedLen)<.01f;
    }
    return new Evidence(ready,walkPass,attackPass,layerPass,weaponPass,walk,attacks,layers,weapons);
  }

  public static String report(Evidence e){StringBuilder s=new StringBuilder(e.summary()).append('\n');s.append("ROBE REGISTRATION 4x5\n");for(WalkRegistration x:e.walk)s.append(x).append('\n');s.append("ATTACK GEOMETRY 4\n");for(AttackGeometry x:e.attack)s.append(x).append('\n');s.append("ATTACK LAYERS 4\n");for(AttackLayers x:e.layers)s.append(x).append('\n');s.append("MW001 ATTACK TRANSFORM 4\n");for(WeaponAttack x:e.weapons)s.append(x).append('\n');return s.toString();}

  private static Bitmap load(Resources r,String name,int width,int height){if(r==null)return null;try{int id=r.getIdentifier(name,"drawable","com.projectdark.mobile");if(id==0)return null;BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap b=BitmapFactory.decodeResource(r,id,o);return b!=null&&b.getWidth()==width&&b.getHeight()==height?b:null;}catch(Throwable ignored){return null;}}
  private static Bounds bounds(Bitmap b){if(b==null)return new Bounds(0,0,-1,-1);int l=b.getWidth(),t=b.getHeight(),rr=-1,bb=-1;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(((b.getPixel(x,y)>>>24)&0xff)!=0){l=Math.min(l,x);rr=Math.max(rr,x);t=Math.min(t,y);bb=Math.max(bb,y);}return new Bounds(l,t,rr,bb);}
  private static Bounds cellBounds(Bitmap atlas,int row,int col){if(atlas==null)return new Bounds(0,0,-1,-1);int ox=col*W,oy=row*H,l=W,t=H,rr=-1,bb=-1;for(int y=0;y<H;y++)for(int x=0;x<W;x++)if(((atlas.getPixel(ox+x,oy+y)>>>24)&0xff)!=0){l=Math.min(l,x);rr=Math.max(rr,x);t=Math.min(t,y);bb=Math.max(bb,y);}return new Bounds(l,t,rr,bb);}
}
