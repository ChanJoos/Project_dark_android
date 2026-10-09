package com.projectdark.mobile;

/** Continuous, asset-preserving presentation. Never changes damage, movement or rewards. */
public final class PoteMonsterMotion {
  public static final class Frame {
    public String pose;
    public float lift,lean,stride,lunge,recoil;
    public boolean articulated;
  }
  private PoteMonsterMotion(){}
  private static float clamp(float v){return Math.max(0f,Math.min(1f,v));}
  private static float smooth(float v){v=clamp(v);return v*v*(3f-2f*v);}
  public static void sample(String id,String state,float progress,float clock,float hitRemaining,Frame out){
    out.pose="idle";out.lift=out.lean=out.stride=out.lunge=out.recoil=0f;
    out.articulated=false;
    String species=PoteForestMonsterShowcase.species(id);
    boolean plant=species!=null&&(species.contains("PAMFET")||species.contains("PURPLE")
        ||species.contains("RED")||species.contains("GREEN")||species.contains("SILVER")&&!species.contains("WOLF"));
    if("walk".equals(state)){
      out.pose="walk";out.articulated=true;
      float beat=(float)Math.sin(clock*2f*(float)Math.PI/CharacterRenderer.WALK_CYCLE_SECONDS);
      out.stride=beat*(plant?.65f:2.6f);
      out.lift=Math.abs(beat)*(plant?.6f:.9f);
      out.lean=beat*.55f;
    }else if("attack".equals(state)){
      float p=clamp(progress);
      if(p<.5f){
        // Windup shows the relaxed source. Strike art appears only at actual contact.
        float wind=smooth(p*2f);out.lunge=-1.5f*wind;out.lean=-1.7f*wind;
      }else{
        float recover=smooth((p-.5f)/.5f);out.pose=p<.91f?"attack":"idle";
        out.lunge=3.4f*(1f-recover);out.lean=2.2f*(1f-recover);
      }
      out.articulated=true;
    }else out.lift=(float)Math.sin(clock*4.5f)*.35f;
    if(hitRemaining>0f){
      float age=.14f-Math.min(.14f,hitRemaining);
      out.recoil=2.2f*(float)Math.sin(Math.PI*clamp(age/.14f));
    }
  }
}
