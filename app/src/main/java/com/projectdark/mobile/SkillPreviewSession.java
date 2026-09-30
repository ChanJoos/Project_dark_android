package com.projectdark.mobile;

/** Test-only presentation clock. Never changes damage, healing, statuses, MP or save progression. */
final class SkillPreviewSession {
  private String id,target;private float age,contact;private long sequence=-1;private boolean sent;
  void start(String id,String target,boolean magic,SkillVfxRenderer fx,SkillVfxRenderer.Anchors anchors){clear();this.id=id;this.target=target;contact=magic?.24f:.14f;sequence--;fx.preview(sequence,id,true,RuntimeCombatSession.PLAYER_ID,anchors);}
  void tick(float dt,SkillVfxRenderer fx,SkillVfxRenderer.Anchors anchors){if(id==null)return;age+=Math.max(0,dt);if(!sent&&age>=contact){sent=true;fx.preview(sequence,id,false,target,anchors);}if(age>.8f)clear();}
  void clear(){id=target=null;age=0;sent=false;}
}
