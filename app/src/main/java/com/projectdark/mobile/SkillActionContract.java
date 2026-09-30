package com.projectdark.mobile;

import java.util.*;

/** One per-ID tile contract for live combat and presentation tests. No pixel-radius melee fallback. */
public final class SkillActionContract {
  public enum Pattern { SELF, ALLY, GROUP, SINGLE, FRONT, CROSS, AROUND, FRONT_BACK, TARGET_CROSS, SCREEN, PASSIVE, UTILITY, UNRESOLVED }
  public static final class Rule {
    public final String id,name,mode,mechanic,evidence,note,kind;public final Pattern pattern;
    public final int reach,minReach,hits;public final float contact;
    Rule(String[] r){id=r[0];name=r[1];mode=r[2];pattern=Pattern.valueOf(r[3]);reach=Integer.parseInt(r[4]);minReach=Integer.parseInt(r[5]);mechanic=r[6];hits=Integer.parseInt(r[7]);contact=Float.parseFloat(r[8]);evidence=r[9];note=r[10];kind=r[11];}
    public boolean selfAnchored(){return pattern==Pattern.SELF||pattern==Pattern.ALLY||pattern==Pattern.GROUP||pattern==Pattern.CROSS||pattern==Pattern.AROUND||pattern==Pattern.SCREEN;}
    public boolean presentationAllowed(){return !mode.equals("LINKED")&&!mode.equals("UTILITY")&&pattern!=Pattern.PASSIVE&&pattern!=Pattern.UTILITY&&pattern!=Pattern.UNRESOLVED;}
    public boolean damage(){return mechanic.equals("DAMAGE_ADAPTED_BALANCE");}
    public boolean heal(){return mechanic.equals("HEAL_ADAPTED_BALANCE");}
    public boolean needsSelection(){return !selfAnchored();}
  }
  private static final Map<String,Rule> RULES=new LinkedHashMap<>();
  static {for(String[] row:SkillActionData.ROWS){Rule r=new Rule(row);if(RULES.put(r.id,r)!=null)throw new IllegalStateException("duplicate skill rule");}}
  public static Rule get(String id){return RULES.get(id);}
  public static Collection<Rule> all(){return Collections.unmodifiableCollection(RULES.values());}
  private SkillActionContract(){}
  // Original four directions are the axes in this inverse 64x32 isometric grid.
  static float u(float dx,float dy){return (dx/32f+dy/16f)/2f;}
  static float v(float dx,float dy){return (dy/16f-dx/32f)/2f;}
  static boolean integer(float x){return Math.abs(x-Math.round(x))<.015f;}
  static int distance(float ax,float ay,float bx,float by){float u=u(bx-ax,by-ay),v=v(bx-ax,by-ay);if(!integer(u)||!integer(v))return Integer.MAX_VALUE;return Math.max(Math.abs(Math.round(u)),Math.abs(Math.round(v)));}
  public static boolean canStart(Rule r,float ax,float ay,float tx,float ty,boolean visible){
    if(r==null)return true;if(!r.presentationAllowed())return false;
    if(r.selfAnchored())return Math.abs(ax-tx)<.01f&&Math.abs(ay-ty)<.01f;
    if(!visible)return false;
    if(r.pattern==Pattern.FRONT||r.pattern==Pattern.FRONT_BACK)return onAxis(ax,ay,tx,ty,r.reach);
    int d=distance(ax,ay,tx,ty);if(d==Integer.MAX_VALUE)return false;
    return d>=r.minReach&&(r.reach==0||r.pattern==Pattern.TARGET_CROSS||d<=r.reach);
  }
  private static boolean onAxis(float ax,float ay,float tx,float ty,int reach){float u=u(tx-ax,ty-ay),v=v(tx-ax,ty-ay);if(!integer(u)||!integer(v))return false;int a=Math.abs(Math.round(u)),b=Math.abs(Math.round(v));return (a==0&&b>=1&&b<=reach)||(b==0&&a>=1&&a<=reach);}
  /** Membership is evaluated again at contact; no effects or damage are emitted outside the shape. */
  public static boolean includes(Rule r,float ax,float ay,float tx,float ty,float x,float y,boolean visible){
    if(r==null||!r.presentationAllowed()||!visible)return false;
    if(r.pattern==Pattern.SINGLE||r.pattern==Pattern.SELF||r.pattern==Pattern.ALLY||r.pattern==Pattern.GROUP)return Math.abs(x-tx)<.01f&&Math.abs(y-ty)<.01f;
    if(r.pattern==Pattern.SCREEN)return true;
    float cx=r.pattern==Pattern.TARGET_CROSS?tx:ax,cy=r.pattern==Pattern.TARGET_CROSS?ty:ay;
    float u=u(x-cx,y-cy),v=v(x-cx,y-cy);if(!integer(u)||!integer(v))return false;
    int a=Math.round(u),b=Math.round(v),d=Math.max(Math.abs(a),Math.abs(b));
    if(r.pattern==Pattern.TARGET_CROSS)return (a==0||b==0)&&d<=r.reach;
    if(d==0)return false;
    if(r.pattern==Pattern.CROSS)return (a==0||b==0)&&d<=r.reach;
    if(r.pattern==Pattern.AROUND)return d<=r.reach;
    float tu=u(tx-ax,ty-ay),tv=v(tx-ax,ty-ay);
    if(r.pattern==Pattern.FRONT||r.pattern==Pattern.FRONT_BACK){
      if(!onAxis(ax,ay,tx,ty,r.reach))return false;
      boolean axis=Math.abs(tu)>.1f?b==0:a==0;float dot=a*tu+b*tv;
      return axis&&d<=r.reach&&(r.pattern==Pattern.FRONT_BACK||dot>0);
    }
    return false;
  }
}
