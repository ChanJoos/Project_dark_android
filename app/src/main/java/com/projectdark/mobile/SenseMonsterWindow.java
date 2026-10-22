package com.projectdark.mobile;
import android.graphics.*;
/** Five fields and confirmation control observed in retained source sense-44.5/60.5.png. */
final class SenseMonsterWindow {
 private String[] lines;private float remaining;private String map;private boolean automatic;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final AdaptedCampaignRewardCatalog rewards=new AdaptedCampaignRewardCatalog();
 final RectF panel=new RectF(250,120,710,392),confirm=new RectF(414,326,546,370);
 boolean visible(){return lines!=null;}
 void show(RuntimeState state,RuntimeState.Monster m,boolean auto){
  AbelCombatProfile profile=AbelCombatProfile.forId(m.id);AdaptedCampaignRewardCatalog.Reward reward=rewards.find(m.campaignRewardProfileId==null?m.id:m.campaignRewardProfileId);
  String attack=profile==null?"?":element(profile.element),defense=attack;
  String species=PoteForestMonsterShowcase.species(m.id);
  if("LYK_ELECT_KOATL".equals(species)){attack="땅";defense="물";}else if("LYK_TYRANT".equals(species)){attack="물";defense="번개";}
  lines=new String[]{"Name: "+m.name+"   EXP: "+(reward!=null?reward.exp:profile!=null?profile.exp:"?"),"HP: "+m.hp,"Lev: "+(m.level>0?m.level:profile!=null?profile.level:"?"),"ATTACK NATURE: "+attack,"DEFENSE NATURE: "+defense};
  automatic=auto;remaining=auto?.3f:Float.POSITIVE_INFINITY;map=state.currentMapId();
 }
 static String element(String e){switch(e){case "FIRE":return "불";case "WATER":return "물";case "EARTH":return "땅";case "WIND":return "바람";case "LIGHTNING":return "번개";case "DARK":return "암흑";case "LIGHT":return "빛";case "NONE":return "무";default:return e;}}
 void tick(float dt,RuntimeState s){if(!visible())return;remaining-=Math.max(0,dt);if(remaining<=0||!s.player().alive||!map.equals(s.currentMapId()))close();}
 void close(){lines=null;}
 boolean touch(float x,float y){if(!visible()||automatic)return false;if(confirm.contains(x,y))close();return true;}
 void draw(Canvas c,float offset){if(!visible())return;c.save();c.translate(offset,0);p.setStyle(Paint.Style.FILL);p.setColor(0xe6201c18);c.drawRect(panel,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xff655e51);c.drawRect(panel,p);p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(17);p.setColor(0xfff0ece3);for(int i=0;i<lines.length;i++)c.drawText(lines[i],272,152+i*29,p);p.setColor(0xff39332d);c.drawRect(confirm,p);p.setStyle(Paint.Style.STROKE);p.setColor(0xff655e51);c.drawRect(confirm,p);p.setStyle(Paint.Style.FILL);p.setColor(0xffe3ddd3);c.drawText("확인",480-p.measureText("확인")/2,354,p);c.restore();}
}
