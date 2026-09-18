package com.projectdark.mobile;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Device/runtime evidence probe for source-backed player presentation.
 *
 * This is deliberately NOT an app-startup fail-fast audit. Director/device QA may invoke it with
 * live Resources and persist the returned measurements as merge evidence.
 */
public final class CharacterVisualEvidenceProbe {
  private static final int FRAME_W=CharacterRenderer.SOURCE_FRAME_WIDTH;
  private static final int FRAME_H=CharacterRenderer.SOURCE_FRAME_HEIGHT;
  private static final int FOOT_X=CharacterRenderer.SOURCE_FOOT_ANCHOR_X;
  private static final int FOOT_Y=CharacterRenderer.SOURCE_FOOT_ANCHOR_Y;
  private static final int[] BODY_W={17,18,19,21};
  private static final int[] BODY_H={51,52,51,49};
  private static final int[] BODY_X={8,2,6,4};
  private static final int[] BODY_Y={8,8,9,3};
  private static final int[] ROBE_W={14,19,15,20};
  private static final int[] ROBE_H={33,22,30,21};
  private static final int[] ROBE_X={8,3,6,6};
  private static final int[] ROBE_Y={20,27,23,22};
  private static final float MOKDO_PIVOT_X=2f;
  private static final float MOKDO_PIVOT_Y=4f;

  public static final class Bounds {
    public final int left,top,right,bottom;
    Bounds(int left,int top,int right,int bottom){this.left=left;this.top=top;this.right=right;this.bottom=bottom;}
    public boolean empty(){return right<left||bottom<top;}
    public int width(){return empty()?0:right-left+1;}
    public int height(){return empty()?0:bottom-top+1;}
    public float centerX(){return empty()?Float.NaN:(left+right)*.5f;}
    @Override public String toString(){return empty()?"empty":"["+left+","+top+".."+right+","+bottom+"]";}
  }

  public static final class WalkSample {
    public final CharacterRenderer.Direction direction;
    public final int column;
    public final Bounds body,robe;
    public final int robeMinusBodyFoot;
    public final float robeMinusBodyCenter;
    WalkSample(CharacterRenderer.Direction direction,int column,Bounds body,Bounds robe){
      this.direction=direction;this.column=column;this.body=body;this.robe=robe;
      this.robeMinusBodyFoot=robe.bottom-body.bottom;
      this.robeMinusBodyCenter=robe.centerX()-body.centerX();
    }
    @Override public String toString(){return direction+"/c"+column+" body="+body+" robe="+robe+" dFoot="+robeMinusBodyFoot+" dCenter="+robeMinusBodyCenter;}
  }

  public static final class AttackSample {
    public final CharacterRenderer.Direction direction;
    public final Bounds idleComposite,attackComposite;
    public final int heightError,footError,centerError;
    public final boolean withinTolerance;
    AttackSample(CharacterRenderer.Direction direction,Bounds idleComposite,Bounds attackComposite){
      this.direction=direction;this.idleComposite=idleComposite;this.attackComposite=attackComposite;
      this.heightError=Math.abs(idleComposite.height()-attackComposite.height());
      this.footError=Math.abs(idleComposite.bottom-attackComposite.bottom);
      this.centerError=Math.round(Math.abs(idleComposite.centerX()-attackComposite.centerX()));
      this.withinTolerance=!idleComposite.empty()&&!attackComposite.empty()&&
          heightError<=CharacterRenderer.MAX_ALPHA_HEIGHT_ERROR_PX&&
          footError<=CharacterRenderer.MAX_ALPHA_FOOT_ERROR_PX&&
          centerError<=CharacterRenderer.MAX_ALPHA_CENTER_ERROR_PX;
    }
    @Override public String toString(){return direction+" idle="+idleComposite+" attack="+attackComposite+" err(h/f/c)="+heightError+"/"+footError+"/"+centerError+" pass="+withinTolerance;}
  }

  public static final class WeaponSample {
    public final CharacterRenderer.Direction direction;
    public final int column;
    public final float handX,handY,tipX,tipY,tipDistance,angle;
    WeaponSample(CharacterRenderer.Direction direction,int column,float handX,float handY,float tipX,float tipY,float tipDistance,float angle){
      this.direction=direction;this.column=column;this.handX=handX;this.handY=handY;this.tipX=tipX;this.tipY=tipY;this.tipDistance=tipDistance;this.angle=angle;
    }
    @Override public String toString(){return direction+"/c"+column+" hand=("+handX+","+handY+") tip=("+tipX+","+tipY+") len="+tipDistance+" angle="+angle;}
  }

  public static final class Evidence {
    public final boolean resourcesReady;
    public final boolean walkCoveragePass;
    public final boolean walkAlignmentPass;
    public final boolean attackCompositePass;
    public final boolean weaponHandTipPass;
    public final List<WalkSample> walkSamples;
    public final List<AttackSample> attackSamples;
    public final List<WeaponSample> weaponSamples;
    Evidence(boolean resourcesReady,boolean walkCoveragePass,boolean walkAlignmentPass,boolean attackCompositePass,boolean weaponHandTipPass,
        List<WalkSample> walkSamples,List<AttackSample> attackSamples,List<WeaponSample> weaponSamples){
      this.resourcesReady=resourcesReady;this.walkCoveragePass=walkCoveragePass;this.walkAlignmentPass=walkAlignmentPass;
      this.attackCompositePass=attackCompositePass;this.weaponHandTipPass=weaponHandTipPass;
      this.walkSamples=Collections.unmodifiableList(walkSamples);this.attackSamples=Collections.unmodifiableList(attackSamples);this.weaponSamples=Collections.unmodifiableList(weaponSamples);
    }
    public boolean passes(){return resourcesReady&&walkCoveragePass&&walkAlignmentPass&&attackCompositePass&&weaponHandTipPass;}
    public String summary(){return "resources="+resourcesReady+",walkCoverage="+walkCoveragePass+",walkAlignment="+walkAlignmentPass+
        ",attackComposite="+attackCompositePass+",weaponHandTip="+weaponHandTipPass+",walkSamples="+walkSamples.size()+",attackSamples="+attackSamples.size()+",weaponSamples="+weaponSamples.size();}
  }

  private CharacterVisualEvidenceProbe(){}

  public static Evidence collect(Resources resources){
    Bitmap bodyAtlas=load(resources,CharacterRenderer.IDLE_WALK_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap robeAtlas=load(resources,CharacterRenderer.STARTER_SHIRT_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap weapon=load(resources,CharacterRenderer.MOKDO_RESOURCE,16,8);
    Bitmap[] bodyAction=new Bitmap[4],robeAction=new Bitmap[4];
    for(int i=0;i<4;i++){
      bodyAction[i]=load(resources,"player_body_mm001_action02_"+i,BODY_W[i],BODY_H[i]);
      robeAction[i]=load(resources,"player_shirt_mu0000001_action02_"+i,ROBE_W[i],ROBE_H[i]);
    }
    boolean resourcesReady=bodyAtlas!=null&&robeAtlas!=null&&weapon!=null;
    for(int i=0;i<4;i++)resourcesReady&=bodyAction[i]!=null&&robeAction[i]!=null;

    List<WalkSample> walk=new ArrayList<>();
    boolean coverage=true,alignment=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int row=CharacterRenderer.atlasRow(d);
      int minFoot=Integer.MAX_VALUE,maxFoot=Integer.MIN_VALUE;
      float minCenter=Float.POSITIVE_INFINITY,maxCenter=Float.NEGATIVE_INFINITY;
      for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
        Bounds bb=cellBounds(bodyAtlas,row,col),rb=cellBounds(robeAtlas,row,col);
        WalkSample sample=new WalkSample(d,col,bb,rb);walk.add(sample);
        coverage&=!bb.empty()&&!rb.empty();
        if(!bb.empty()&&!rb.empty()){
          minFoot=Math.min(minFoot,sample.robeMinusBodyFoot);maxFoot=Math.max(maxFoot,sample.robeMinusBodyFoot);
          minCenter=Math.min(minCenter,sample.robeMinusBodyCenter);maxCenter=Math.max(maxCenter,sample.robeMinusBodyCenter);
        }
      }
      if(minFoot==Integer.MAX_VALUE||maxFoot-minFoot>1)alignment=false;
      if(minCenter==Float.POSITIVE_INFINITY||maxCenter-minCenter>2.0f)alignment=false;
    }

    List<AttackSample> attacks=new ArrayList<>();
    boolean attackPass=true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int row=CharacterRenderer.atlasRow(d),src=CharacterRenderer.actionSourceIndex(d);
      Bounds idleBody=cellBounds(bodyAtlas,row,0),idleRobe=cellBounds(robeAtlas,row,0);
      Bounds idleComposite=union(idleBody,idleRobe);
      Bounds rawBody=bounds(bodyAction[src]),rawRobe=bounds(robeAction[src]);
      if(rawBody.empty()||rawRobe.empty()||idleComposite.empty()){
        AttackSample sample=new AttackSample(d,idleComposite,new Bounds(0,0,-1,-1));attacks.add(sample);attackPass=false;continue;
      }
      int bodyGlobalBottom=BODY_Y[src]+rawBody.bottom;
      float bodyGlobalCenter=BODY_X[src]+rawBody.centerX();
      int dy=idleBody.bottom-bodyGlobalBottom;
      int dx=Math.round(idleBody.centerX()-bodyGlobalCenter);
      Bounds placedBody=translate(rawBody,BODY_X[src]+dx,BODY_Y[src]+dy);
      Bounds placedRobe=translate(rawRobe,ROBE_X[src]+dx,ROBE_Y[src]+dy);
      Bounds attackComposite=clip(union(placedBody,placedRobe),0,0,FRAME_W-1,FRAME_H-1);
      AttackSample sample=new AttackSample(d,idleComposite,attackComposite);attacks.add(sample);attackPass&=sample.withinTolerance;
    }

    List<WeaponSample> weapons=new ArrayList<>();
    boolean weaponPass=weapon!=null&&CharacterRenderer.weaponTransformUsesSingleMirror();
    Bounds wb=bounds(weapon);
    float tipLocalX=MOKDO_PIVOT_X,tipLocalY=MOKDO_PIVOT_Y,maxD2=-1f;
    if(wb.empty())weaponPass=false;
    else{
      for(int y=wb.top;y<=wb.bottom;y++)for(int x=wb.left;x<=wb.right;x++){
        if(((weapon.getPixel(x,y)>>>24)&0xff)==0)continue;
        float dx=x-MOKDO_PIVOT_X,dy=y-MOKDO_PIVOT_Y,d2=dx*dx+dy*dy;
        if(d2>maxD2){maxD2=d2;tipLocalX=x;tipLocalY=y;}
      }
    }
    float expectedLen=(float)Math.sqrt(Math.max(0f,maxD2));
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
      float handX=CharacterRenderer.weaponCarryOffsetX(d,col),handY=CharacterRenderer.weaponCarryOffsetY(d,col);
      float localX=tipLocalX-MOKDO_PIVOT_X,localY=tipLocalY-MOKDO_PIVOT_Y;
      if(CharacterRenderer.weaponMirrorX(d))localX=-localX;
      float angle=CharacterRenderer.weaponCarryAngle(d,col),rad=(float)Math.toRadians(angle);
      float rx=(float)(localX*Math.cos(rad)-localY*Math.sin(rad));
      float ry=(float)(localX*Math.sin(rad)+localY*Math.cos(rad));
      float tipX=handX+rx,tipY=handY+ry,len=(float)Math.sqrt(rx*rx+ry*ry);
      WeaponSample sample=new WeaponSample(d,col,handX,handY,tipX,tipY,len,angle);weapons.add(sample);
      weaponPass&=!Float.isNaN(handX)&&!Float.isNaN(handY)&&!Float.isNaN(tipX)&&!Float.isNaN(tipY)&&Math.abs(len-expectedLen)<.01f&&len>=5f;
    }
    return new Evidence(resourcesReady,coverage,alignment,attackPass,weaponPass,walk,attacks,weapons);
  }

  private static Bitmap load(Resources r,String name,int width,int height){
    if(r==null)return null;
    try{
      int id=r.getIdentifier(name,"drawable","com.projectdark.mobile");if(id==0)return null;
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;
      Bitmap b=BitmapFactory.decodeResource(r,id,o);return b!=null&&b.getWidth()==width&&b.getHeight()==height?b:null;
    }catch(Throwable ignored){return null;}
  }
  private static Bounds bounds(Bitmap b){
    if(b==null)return new Bounds(0,0,-1,-1);int l=b.getWidth(),t=b.getHeight(),r=-1,bt=-1;
    for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(((b.getPixel(x,y)>>>24)&0xff)!=0){l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);bt=Math.max(bt,y);}
    return new Bounds(l,t,r,bt);
  }
  private static Bounds cellBounds(Bitmap atlas,int row,int col){
    if(atlas==null||row<0||col<0)return new Bounds(0,0,-1,-1);int ox=col*FRAME_W,oy=row*FRAME_H,l=FRAME_W,t=FRAME_H,r=-1,b=-1;
    for(int y=0;y<FRAME_H;y++)for(int x=0;x<FRAME_W;x++)if(((atlas.getPixel(ox+x,oy+y)>>>24)&0xff)!=0){l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);b=Math.max(b,y);}
    return new Bounds(l,t,r,b);
  }
  private static Bounds translate(Bounds b,int dx,int dy){return b.empty()?b:new Bounds(b.left+dx,b.top+dy,b.right+dx,b.bottom+dy);}
  private static Bounds union(Bounds a,Bounds b){
    if(a.empty())return b;if(b.empty())return a;
    return new Bounds(Math.min(a.left,b.left),Math.min(a.top,b.top),Math.max(a.right,b.right),Math.max(a.bottom,b.bottom));
  }
  private static Bounds clip(Bounds b,int l,int t,int r,int bt){
    if(b.empty())return b;Bounds out=new Bounds(Math.max(l,b.left),Math.max(t,b.top),Math.min(r,b.right),Math.min(bt,b.bottom));return out.empty()?new Bounds(0,0,-1,-1):out;
  }
}