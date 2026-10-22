package com.projectdark.mobile;
/** UserV232: Sense never rotates or approaches. Only the facing ray's first two monster cells. */
final class RogueSkillPolicy {
 static final String SENSE="SK_도적_002",SHURIKEN="SK_도적_003",AMNESIA="SK_도적_005";
 static boolean shuriken(String id){return SHURIKEN.equals(id);}
 static boolean ray(String id){return shuriken(id)||AMNESIA.equals(id);}
 static boolean facingOnly(String id){return sense(id)||ray(id);}
 static int reach(String action){return ray(action)?4:2;}
 static boolean sense(String id){return SENSE.equals(id);}
 static boolean valid(RuntimeState s,String id,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){
  if(id==null||!visible.test(id))return false;float[] d=MartialSkillPolicy.step(facing);
  for(RuntimeState.Monster m:s.monsters())if(m.alive&&id.equals(m.id))for(int n=1;n<=2;n++)if(MartialSkillPolicy.same(s.player().x+d[0]*n,s.player().y+d[1]*n,m.x,m.y))return true;
  return false;
 }
 static String frontTarget(RuntimeState s,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){return frontTarget(s,SENSE,facing,visible);}
 static boolean validShuriken(RuntimeState s,String target,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){return validRay(s,SHURIKEN,target,facing,visible);}
 static boolean validRay(RuntimeState s,String action,String target,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){return target!=null&&target.equals(frontTarget(s,action,facing,visible));}
 static String frontTarget(RuntimeState s,String action,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){
  float[] d=MartialSkillPolicy.step(facing);for(int n=1;n<=reach(action);n++)for(RuntimeState.Monster m:s.monsters())if(m.alive&&visible.test(m.id)&&MartialSkillPolicy.same(s.player().x+d[0]*n,s.player().y+d[1]*n,m.x,m.y))return m.id;return null;
 }
 private RogueSkillPolicy(){}
}
