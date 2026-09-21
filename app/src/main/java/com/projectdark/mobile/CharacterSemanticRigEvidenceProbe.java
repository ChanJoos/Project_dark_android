package com.projectdark.mobile;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/** Executable resource probe for the semantic attachment path used by CharacterRenderer. */
public final class CharacterSemanticRigEvidenceProbe {
  private static final int[] BODY_W={17,18,19,21},BODY_H={51,52,51,49};
  private static final int[] ROBE_W={14,19,15,20},ROBE_H={33,22,30,21};
  private static final float MAX_ACTION_SEMANTIC_ERROR=2f;

  public static final class Result {
    public final boolean resourcesReady,northFrozen,swseSemantic,weaponHandDerived,placeholderTruth,actionGlobalAlignment;
    public final float maxSwSeRobeShift,maxWalkHandStep,maxActionPelvisXError,maxActionPelvisYError,maxActionFootYError;
    Result(boolean ready,boolean north,boolean robe,boolean weapon,boolean placeholder,boolean actionAligned,float robeShift,float handStep,float pelvisX,float pelvisY,float footY){
      resourcesReady=ready;northFrozen=north;swseSemantic=robe;weaponHandDerived=weapon;placeholderTruth=placeholder;actionGlobalAlignment=actionAligned;
      maxSwSeRobeShift=robeShift;maxWalkHandStep=handStep;maxActionPelvisXError=pelvisX;maxActionPelvisYError=pelvisY;maxActionFootYError=footY;
    }
    public boolean passes(){return resourcesReady&&northFrozen&&swseSemantic&&weaponHandDerived&&placeholderTruth&&actionGlobalAlignment;}
    public String summary(){return "resources="+resourcesReady+",northFrozen="+northFrozen+",swseSemantic="+swseSemantic+",weaponHandDerived="+weaponHandDerived+",placeholder="+placeholderTruth+",actionGlobalAlignment="+actionGlobalAlignment+",maxRobeShift="+maxSwSeRobeShift+",maxHandStep="+maxWalkHandStep+",actionErr(pX/pY/fY)="+maxActionPelvisXError+"/"+maxActionPelvisYError+"/"+maxActionFootYError;}
  }

  private CharacterSemanticRigEvidenceProbe(){}

  public static Result collect(Resources r){
    Bitmap body=load(r,CharacterRenderer.IDLE_WALK_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap robe=load(r,CharacterRenderer.STARTER_SHIRT_RESOURCE,CharacterRenderer.SOURCE_IDLE_WALK_WIDTH,CharacterRenderer.SOURCE_ATLAS_HEIGHT);
    Bitmap weapon=load(r,CharacterRenderer.MOKDO_RESOURCE,16,8);
    boolean ready=body!=null&&robe!=null&&weapon!=null;
    boolean north=!CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.NW)&&!CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.NE);
    boolean semantic=CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.SW)&&CharacterRenderer.robeUsesRuntimeXYRegistration(CharacterRenderer.Direction.SE);
    boolean weaponDerived=ready;float maxRobe=0f,maxHand=0f;
    float maxPelvisX=0f,maxPelvisY=0f,maxFootY=0f;boolean actionAligned=true;

    if(ready){
      for(CharacterRenderer.Direction d:new CharacterRenderer.Direction[]{CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE}){
        int row=CharacterRenderer.atlasRow(d);CharacterSemanticRig.Point previous=null;
        for(int col=0;col<CharacterRenderer.IDLE_WALK_COLUMNS;col++){
          Rect cell=cell(row,col);
          CharacterSemanticRig.Anchors ba=CharacterSemanticRig.derive(body,cell,d);
          CharacterSemanticRig.Anchors ra=CharacterSemanticRig.derive(robe,cell,d);
          CharacterSemanticRig.Translation tr=CharacterSemanticRig.garmentTranslation(ba,ra);
          if(!finite(ba.dominantHand.x)||!finite(ba.dominantHand.y)||!finite(tr.x)||!finite(tr.y)){semantic=false;weaponDerived=false;continue;}
          maxRobe=Math.max(maxRobe,(float)Math.sqrt(tr.x*tr.x+tr.y*tr.y));
          if(previous!=null){float dx=ba.dominantHand.x-previous.x,dy=ba.dominantHand.y-previous.y;maxHand=Math.max(maxHand,(float)Math.sqrt(dx*dx+dy*dy));}
          previous=ba.dominantHand;
        }
      }
      for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
        int source=CharacterRenderer.actionSourceIndex(d);
        Bitmap ab=load(r,"player_body_mm001_action02_"+source,BODY_W[source],BODY_H[source]);
        Bitmap ar=load(r,"player_shirt_mu0000001_action02_"+source,ROBE_W[source],ROBE_H[source]);
        ready&=ab!=null&&ar!=null;
        if(ab!=null&&ar!=null){
          CharacterSemanticRig.Anchors ba=CharacterSemanticRig.derive(ab,new Rect(0,0,BODY_W[source],BODY_H[source]),d);
          CharacterSemanticRig.Anchors ra=CharacterSemanticRig.derive(ar,new Rect(0,0,ROBE_W[source],ROBE_H[source]),d);
          CharacterSemanticRig.Translation tr=CharacterSemanticRig.garmentTranslation(ba,ra);
          float pelvisX=CharacterSemanticRig.postTranslationPelvisXError(ba,ra,tr);
          float pelvisY=CharacterSemanticRig.postTranslationPelvisYError(ba,ra,tr);
          float footY=CharacterSemanticRig.postTranslationFootYError(ba,ra,tr);
          maxPelvisX=Math.max(maxPelvisX,pelvisX);maxPelvisY=Math.max(maxPelvisY,pelvisY);maxFootY=Math.max(maxFootY,footY);
          boolean finiteAction=finite(tr.x)&&finite(tr.y)&&finite(pelvisX)&&finite(pelvisY)&&finite(footY);
          actionAligned&=finiteAction&&pelvisX<=MAX_ACTION_SEMANTIC_ERROR&&pelvisY<=MAX_ACTION_SEMANTIC_ERROR&&footY<=MAX_ACTION_SEMANTIC_ERROR;
          semantic&=finiteAction;weaponDerived&=finite(ba.dominantHand.x)&&finite(ba.dominantHand.y);
        }else actionAligned=false;
      }
    }else actionAligned=false;
    boolean placeholder=CharacterRenderer.attackUsesSinglePosePlaceholder()&&!CharacterRenderer.attackTemporalSourceResolved()&&!CharacterRenderer.attackUsesDestructiveCrop();
    return new Result(ready,north,semantic,weaponDerived,placeholder,actionAligned,maxRobe,maxHand,maxPelvisX,maxPelvisY,maxFootY);
  }

  private static Rect cell(int row,int col){int left=col*CharacterRenderer.SOURCE_FRAME_WIDTH,top=row*CharacterRenderer.SOURCE_FRAME_HEIGHT;return new Rect(left,top,left+CharacterRenderer.SOURCE_FRAME_WIDTH,top+CharacterRenderer.SOURCE_FRAME_HEIGHT);}
  private static boolean finite(float v){return !Float.isNaN(v)&&!Float.isInfinite(v);}
  private static Bitmap load(Resources r,String name,int w,int h){if(r==null)return null;try{int id=r.getIdentifier(name,"drawable","com.projectdark.mobile");if(id==0)return null;BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap b=BitmapFactory.decodeResource(r,id,o);return b!=null&&b.getWidth()==w&&b.getHeight()==h?b:null;}catch(Throwable ignored){return null;}}
}
