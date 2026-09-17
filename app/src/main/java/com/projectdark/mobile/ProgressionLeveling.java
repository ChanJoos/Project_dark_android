package com.projectdark.mobile;
import java.lang.reflect.Field;
/** Runtime migration boundary until progression state exposes a durable DTO. Uses the master Level_EXP_Curve only. */
public final class ProgressionLeveling{
 private static final Field LEVEL,EXP;
 static{try{LEVEL=RpgProgressionState.class.getDeclaredField("normalLevel");EXP=RpgProgressionState.class.getDeclaredField("normalExp");LEVEL.setAccessible(true);EXP.setAccessible(true);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 private ProgressionLeveling(){}
 public static int normalize(RpgProgressionState r){try{int before=r.normalLevel()==null?1:r.normalLevel();int lv=before;long exp=r.normalExp()==null?0:r.normalExp();while(lv<99){Long req=LevelExpCurve.requiredForNext(lv);if(req==null||exp<req)break;exp-=req;lv++;}LEVEL.set(r,lv);EXP.set(r,exp);return lv-before;}catch(Exception e){throw new IllegalStateException("progression normalize",e);}}
 public static void restore(RpgProgressionState r,int level,long exp){try{LEVEL.set(r,Math.max(1,Math.min(99,level)));EXP.set(r,Math.max(0L,exp));normalize(r);}catch(Exception e){throw new IllegalStateException("progression restore",e);}}
}