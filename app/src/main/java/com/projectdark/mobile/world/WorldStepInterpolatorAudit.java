package com.projectdark.mobile.world;

/** Android-free regression audit for smooth 0.60-second tile presentation. */
public final class WorldStepInterpolatorAudit {
  private WorldStepInterpolatorAudit(){}
  public static boolean verify(){
    WorldStepInterpolator p=new WorldStepInterpolator(WorldMoveTargetController.TILE_STEP_SECONDS,320f,240f);
    p.begin(352f,256f);
    if(!p.active()||!close(p.x(),320f)||!close(p.y(),240f))return false;
    p.advance(.30f);
    if(!p.active()||!close(p.progress(),.5f)||!close(p.x(),336f)||!close(p.y(),248f))return false;
    p.advance(.30f);
    if(p.active()||!close(p.x(),352f)||!close(p.y(),256f))return false;
    for(int i=0;i<10;i++){p.begin(p.x()-32f,p.y()-16f);p.advance(.60f);}
    for(int i=0;i<10;i++){p.begin(p.x()+32f,p.y()+16f);p.advance(.60f);}
    return !p.active()&&close(p.x(),352f)&&close(p.y(),256f);
  }
  private static boolean close(float a,float b){return Math.abs(a-b)<.001f;}
  public static void main(String[] args){if(!verify())throw new AssertionError("WorldStepInterpolatorAudit failed");System.out.println("WorldStepInterpolatorAudit PASS");}
}
