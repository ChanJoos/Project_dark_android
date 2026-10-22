package com.projectdark.mobile;
import android.graphics.*;
import android.content.Context;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
/** Five fields and confirmation control observed in retained source sense-44.5/60.5.png. */
final class SenseMonsterWindow {
 private String[] lines;private float remaining;private String map;private boolean automatic;
 private final Bitmap frame;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final AdaptedCampaignRewardCatalog rewards=new AdaptedCampaignRewardCatalog();
 final RectF panel=new RectF(165,105,795,423),confirm=new RectF(165+626*630f/838,105+327*318f/423,165+814*630f/838,105+396*318f/423);
 SenseMonsterWindow(Context c){try{JSONObject j=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/rogue-v232/sense-frame.json"),StandardCharsets.UTF_8));frame=PresentationAssetBytes.bitmap(c,j.getString("path"),j.getString("sha256"));if(frame==null)throw new IllegalStateException("Sense source frame");}catch(Exception e){throw new IllegalStateException("Sense source frame",e);}}
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
 void draw(Canvas c,float offset){if(!visible())return;c.save();c.translate(panel.left+offset,panel.top);c.scale(panel.width()/838f,panel.height()/423f);p.setFilterBitmap(false);p.setAlpha(255);p.setStyle(Paint.Style.FILL);c.drawBitmap(frame,0,0,p);p.setTypeface(Typeface.create("monospace",Typeface.NORMAL));p.setColor(0xffded2ae);p.setTextSize(25);for(int i=0;i<lines.length;i++){p.setTextSize(25);if(i==0&&p.measureText(lines[i])>650)p.setTextSize(25*650/p.measureText(lines[i]));c.drawText(lines[i],70,125+i*26,p);}c.restore();}
}
