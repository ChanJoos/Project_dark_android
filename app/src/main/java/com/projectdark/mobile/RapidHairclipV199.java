package com.projectdark.mobile;
import java.util.*;
/** Exact source tiers; projecting source back/cape onto two ornament slots is ADAPTED. No inferred Lv85/dyes. */
final class RapidHairclipV199 {
 static int tier(String id){if(id==null)return 0;switch(id){case "USER_ITEM_20261018_R05141":return 146;case "USER_ITEM_20261018_R05145":return 147;case "USER_ITEM_20261018_R05143":case "USER_ITEM_20261018_R01339":return 148;case "USER_ITEM_20261018_R05142":return 149;default:return 0;}}
 static String appearance(String id,String slot){int n=tier(id);return n==0?null:(RpgProgressionState.SECOND_ORNAMENT_SLOT.equals(slot)?"mc":"mg")+n;}
 static boolean source(String id){return id!=null&&id.matches("[mw][gc]14[6-9]");}
 static String gender(String id,String body){return source(id)?("wm001".equals(body)?"w":"m")+id.substring(1):id;}
 static List<String> layers(String ref,String body,CharacterRenderer.Direction direction,boolean behind){
  // User V252: these hairclips must remain above hats in every facing and motion.
  boolean up=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE;
  if(behind||ref==null)return Collections.emptyList();List<String> ids=new ArrayList<>();
  for(String id:ref.split(","))if(source(id))ids.add(gender(id,body));
  ids.sort((a,b)->up?Character.compare(a.charAt(1),b.charAt(1)):Character.compare(b.charAt(1),a.charAt(1)));return ids;
 }
}
