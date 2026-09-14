package com.projectdark.mobile;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runtime/source evidence for P3 robe, P4 attack registration and P5 SE mw001 continuity. */
public final class CharacterVisualEvidenceProbeV2 {
  private static final int W=CharacterRenderer.SOURCE_FRAME_WIDTH,H=CharacterRenderer.SOURCE_FRAME_HEIGHT;
  private static final int[] BODY_W={17,18,19,21},BODY_H={51,52,51,49};
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
    public final float semanticPivotX,semanticFootY,presentationScale;public final int offsetX,offsetY,rawVisibleHeight;
    public final boolean destructiveCrop,pass;
    AttackGeometry(CharacterRenderer.Direction d,int source,Bounds idle,Bounds raw){
      direction=d;sourceIndex=source;idleBody=idle;rawAction=raw;
      semanticPivotX=CharacterRenderer.actionSemanticPivotX(source);semanticFootY=CharacterRenderer.actionSemanticFootY(source);
      presentationScale=CharacterRenderer.normalizedActionScale(source);destructiveCrop=CharacterRenderer.attackUsesDestructiveCrop();
      offsetX=Math.round(CharacterRenderer.SOURCE_FOOT_ANCHOR_X-semanticPivotX);offsetY=Math.round(CharacterRenderer.SOURCE_FOOT_ANCHOR_Y-semanticFootY);
      rawVisibleHeight=raw.empty()?0:raw.height();
      pass=!idle.empty()&&!raw.empty()&&!destructiveCrop&&!Float.isNaN(semanticPivotX)&&!Float.isNaN(semanticFootY)&&
          Math.abs(presentationScale-(1.70f/1.50f))<.0001f&&semanticPivotX>=10f&&semanticPivotX<=18f&&semanticFootY>=46f&&semanticFootY<=60f;
    }
    @Override public String toString(){return direction+" src="+sourceIndex+" idle="+idleBody+" raw="+rawAction+" rawH="+rawVisibleHeight+" semanticPivot="+semanticPivotX+" semanticFoot="+semanticFootY+" shift=("+offsetX+","+offsetY+") scale="+presentationScale+" crop="+destructiveCrop+" pass="+pass;}
  }

  public static final class AttackLayers {
    public final CharacterRenderer.Direction direction;public final boolean body,robe,weapon,atomic,noWeaponOnlyFallback;
    AttackLayers(CharacterRenderer.Direction d,boolean b,boolean r,boolean w){direction=d;body=b;robe=r;weapon=w;atomic=CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",b,r,w);noWeaponOnlyFallback=!CharacterRenderer.attackFallbackWeaponSwingReachable();}
    @Override public String toString(){return direction+" BODY="+body+" ROBE="+robe+" MW001="+weapon+" atomic="+atomic+" noWeaponOnlyFallback="+noWeaponOnlyFallback;}
  }

  public static final class WeaponWalk {
    public final int column;public final float x,y,angle;
    WeaponWalk(int c,float x,float y,float angle){column=c;this.x=x;this.y=y;this.angle=angle;}
    @Override public String toString(){return "SE/c"+column+" hand=("+x+","+y+") angle="+angle;}
  }

  public static final class WeaponAttack {
    public final CharacterRenderer.Direction direction;public final boolean bodyMirror,weaponMirror;public final float handX,handY,tipX,tipY,angle,tipDistance;
    WeaponAttack(CharacterRenderer.Direction d,boolean bm,boolean wm,float hx,float hy,float tx,float ty,float a,float len){direction=d;bodyMirror=bm;weaponMirror=wm;handX=hx;handY=hy;tipX=tx;tipY=ty;angle=a;tipDistance=len;}
    @Override public String toString(){return direction+" bodyMirror="+bodyMirror+" weaponMirror="+weaponMirror+" hand=("+handX+","+handY+") tip=("+tipX+","+tipY+") angle="+angle+" len="+tipDistance;}
  }

  public static final class Evidence {
    public final boolean resourcesReady,robeRegistrationPass,attackGeometryPass,attackAtomicPass,seWeaponContinuityPass,attackWeaponTransformPass;
    public final List<WalkRegistration> walk;public final List<AttackGeometry> attack;public final List<AttackLayers> layers;public final List<WeaponWalk> seWalk;public final List<WeaponAttack> weapons;
    Evidence(boolean rr,boolean rp,boolean ap,boolean lp,boolean sp,boolean wp,List<WalkRegistration>w,List<AttackGeometry>a,List<AttackLayers>l,List<WeaponWalk>s,List<WeaponAttack>m){resourcesReady=rr;robeRegistrationPass=rp;attackGeometryPass=ap;attackAtomicPass=lp;seWeaponContinuityPass=sp;attackWeaponTransformPass=wp;walk=Collections.unmodifiableList(w);attack=Collections.unmodifiableList(a);layers=Collections.unmodifiableList(l);seWalk=Collections.unmodifiableList(s);weapons=Collections.unmodifiableList(m);}
    public boolean passes(){return resourcesReady&&robeRegistrationPass&&attackGeometryPass&&attackAtomicPass&&seWeaponContinuityPass&&attackWeaponTransformPass;}
    public String summary(){return "resources="+resourcesReady+",robe20="+robeRegistrationPass+",attack4="+attackGeometryPass+",atomic4="+attackAtomicPass+",seCarry5="+seWeaponContinuityPass+",weapon4="+attackWeaponTransformPass;}
  }

  private CharacterVisualEvidenceProbeV2(){}

  public static Evidence collect(Resources r){
    Bitmap body=load(r,CharacterRenderer.IDLE_WALK_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap robe=load(r,CharacterRenderer.LUERS_ROBE_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap weapon=load(r,CharacterRenderer.MOKDO_RESOURCE,16,8);
    Bitmap[] bodyAction=new Bitmap[4],robeAction=new Bitmap[4];boolean ready=body!=null&&robe!=null&&weapon!=null;
    for(int i=0;i<4;i++){bodyAction[i]=load(r,"player_body_mm001_action02_"+i,BODY_W[i],BODY_H[i]);robeAction[i]=load(r,"player_robe_mu0000058_action02_"+i,ROBE_W[i],ROBE_H[i]);ready&=bodyAction[i]!=null&&robeAction[i]!=null;}

    float[][] expectedNorthX={{-1,-2,-1,0,1},{2,2,2,0,1}};
    List<WalkRegistration> walk=new ArrayList<>();boolean robePass=CharacterRenderer.robeRegistrationContinuityWithin(2f);boolean swVertical=false,seVertical=false;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int row=CharacterRenderer.atlasRow(d);
      for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
        Bounds b=cellBounds(body,row,col),rr=cellBounds(robe,row,col);float x=CharacterRenderer.robeFrameRegistrationX(d,col),y=CharacterRenderer.robeFrameRegistrationY(d,col);
        WalkRegistration sample=new WalkRegistration(d,col,b,rr,x,y);walk.add(sample);robePass&=!b.empty()&&!rr.empty();
        if(d==CharacterRenderer.Direction.NW){robePass&=x==expectedNorthX[0][col]&&y==0f;}
        if(d==CharacterRenderer.Direction.NE){robePass&=x==expectedNorthX[1][col]&&y==0f;}
        if(d==CharacterRenderer.Direction.SW){swVertical|=y!=0f;robePass&=Math.abs(y)<=1f;}
        if(d==CharacterRenderer.Direction.SE){seVertical|=y!=0f;robePass&=Math.abs(y)<=1f;}
      }
    }
    robePass&=swVertical&&seVertical;

    List<AttackGeometry> attacks=new ArrayList<>();boolean attackPass=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int row=CharacterRenderer.atlasRow(d),src=CharacterRenderer.actionSourceIndex(d);AttackGeometry g=new AttackGeometry(d,src,cellBounds(body,row,0),bounds(bodyAction[src]));attacks.add(g);attackPass&=g.pass;
    }

    List<AttackLayers> layers=new ArrayList<>();boolean layerPass=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int src=CharacterRenderer.actionSourceIndex(d);AttackLayers l=new AttackLayers(d,bodyAction[src]!=null,robeAction[src]!=null,weapon!=null);layers.add(l);layerPass&=l.atomic&&l.noWeaponOnlyFallback;
    }

    List<WeaponWalk> seWalk=new ArrayList<>();
    for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++)seWalk.add(new WeaponWalk(col,CharacterRenderer.weaponCarryOffsetX(CharacterRenderer.Direction.SE,col),CharacterRenderer.weaponCarryOffsetY(CharacterRenderer.Direction.SE,col),CharacterRenderer.weaponCarryAngle(CharacterRenderer.Direction.SE,col)));
    boolean sePass=CharacterRenderer.weaponCarryMaxJumpX(CharacterRenderer.Direction.SE)<=1f&&CharacterRenderer.weaponCarryMaxJumpY(CharacterRenderer.Direction.SE)<=1f&&CharacterRenderer.weaponCarryMaxJumpAngle(CharacterRenderer.Direction.SE)<=2f;

    Bounds wb=bounds(weapon);float tipXLocal=PIVOT_X,tipYLocal=PIVOT_Y,maxD2=-1f;
    if(weapon!=null&&!wb.empty())for(int y=wb.top;y<=wb.bottom;y++)for(int x=wb.left;x<=wb.right;x++)if(((weapon.getPixel(x,y)>>>24)&0xff)!=0){float dx=x-PIVOT_X,dy=y-PIVOT_Y,d2=dx*dx+dy*dy;if(d2>maxD2){maxD2=d2;tipXLocal=x;tipYLocal=y;}}
    float expectedLen=(float)Math.sqrt(Math.max(0f,maxD2));List<WeaponAttack> weapons=new ArrayList<>();boolean weaponPass=weapon!=null&&!wb.empty();
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      boolean bodyMirror=CharacterRenderer.bodyMirrorX(d),weaponMirror=CharacterRenderer.weaponMirrorX(d);float hx=CharacterRenderer.weaponAttackOffsetX(d),hy=-CharacterRenderer.weaponAttackOffsetY(d),lx=tipXLocal-PIVOT_X,ly=tipYLocal-PIVOT_Y;if(weaponMirror)lx=-lx;
      float angle=CharacterRenderer.weaponAttackAngle(d,.55f),rad=(float)Math.toRadians(angle),rx=(float)(lx*Math.cos(rad)-ly*Math.sin(rad)),ry=(float)(lx*Math.sin(rad)+ly*Math.cos(rad)),len=(float)Math.sqrt(rx*rx+ry*ry);
      WeaponAttack sample=new WeaponAttack(d,bodyMirror,weaponMirror,hx,hy,hx+rx,hy+ry,angle,len);weapons.add(sample);weaponPass&=len>=5f&&Math.abs(len-expectedLen)<.01f;
    }
    return new Evidence(ready,robePass,attackPass,layerPass,sePass,weaponPass,walk,attacks,layers,seWalk,weapons);
  }

  public static String report(Evidence e){StringBuilder s=new StringBuilder(e.summary()).append('\n');s.append("P3 ROBE REGISTRATION 4x5\n");for(WalkRegistration x:e.walk)s.append(x).append('\n');s.append("P4 ATTACK RAW+SEMANTIC REGISTRATION 4\n");for(AttackGeometry x:e.attack)s.append(x).append('\n');s.append("ATTACK LAYERS 4\n");for(AttackLayers x:e.layers)s.append(x).append('\n');s.append("P5 SE MW001 CARRY 5 maxJump=").append(CharacterRenderer.weaponCarryMaxJumpX(CharacterRenderer.Direction.SE)).append('/').append(CharacterRenderer.weaponCarryMaxJumpY(CharacterRenderer.Direction.SE)).append('/').append(CharacterRenderer.weaponCarryMaxJumpAngle(CharacterRenderer.Direction.SE)).append('\n');for(WeaponWalk x:e.seWalk)s.append(x).append('\n');s.append("MW001 ATTACK TRANSFORM 4 (BODY/WEAPON INDEPENDENT)\n");for(WeaponAttack x:e.weapons)s.append(x).append('\n');return s.toString();}

  private static Bitmap load(Resources r,String name,int width,int height){if(r==null)return null;try{int id=r.getIdentifier(name,"drawable","com.projectdark.mobile");if(id==0)return null;BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap b=BitmapFactory.decodeResource(r,id,o);return b!=null&&b.getWidth()==width&&b.getHeight()==height?b:null;}catch(Throwable ignored){return null;}}
  private static Bounds bounds(Bitmap b){if(b==null)return new Bounds(0,0,-1,-1);int l=b.getWidth(),t=b.getHeight(),rr=-1,bb=-1;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(((b.getPixel(x,y)>>>24)&0xff)!=0){l=Math.min(l,x);rr=Math.max(rr,x);t=Math.min(t,y);bb=Math.max(bb,y);}return new Bounds(l,t,rr,bb);}
  private static Bounds cellBounds(Bitmap atlas,int row,int col){if(atlas==null)return new Bounds(0,0,-1,-1);int ox=col*W,oy=row*H,l=W,t=H,rr=-1,bb=-1;for(int y=0;y<H;y++)for(int x=0;x<W;x++)if(((atlas.getPixel(ox+x,oy+y)>>>24)&0xff)!=0){l=Math.min(l,x);rr=Math.max(rr,x);t=Math.min(t,y);bb=Math.max(bb,y);}return new Bounds(l,t,rr,bb);}
}
