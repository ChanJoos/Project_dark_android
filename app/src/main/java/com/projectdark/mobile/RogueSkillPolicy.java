package com.projectdark.mobile;
/** UserV232: Sense never rotates or approaches. Only the facing ray's first two monster cells. */
final class RogueSkillPolicy {
 static final String SENSE="SK_도적_002";
 static boolean sense(String id){return SENSE.equals(id);}
 static boolean valid(RuntimeState s,String id,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){
  if(id==null||!visible.test(id))return false;float[] d=MartialSkillPolicy.step(facing);
  for(RuntimeState.Monster m:s.monsters())if(m.alive&&id.equals(m.id))for(int n=1;n<=2;n++)if(MartialSkillPolicy.same(s.player().x+d[0]*n,s.player().y+d[1]*n,m.x,m.y))return true;
  return false;
 }
 static String frontTarget(RuntimeState s,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){
  float[] d=MartialSkillPolicy.step(facing);for(int n=1;n<=2;n++)for(RuntimeState.Monster m:s.monsters())if(m.alive&&visible.test(m.id)&&MartialSkillPolicy.same(s.player().x+d[0]*n,s.player().y+d[1]*n,m.x,m.y))return m.id;return null;
 }
 private RogueSkillPolicy(){}
}
