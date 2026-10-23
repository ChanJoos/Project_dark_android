package com.projectdark.mobile;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.MotionEvent;
import java.util.*;
import org.json.*;

/** Four owned-item shortcuts. Inventory/equipment/consumption remain in existing RPG services. */
final class QuickItemBarV254 {
 static final String PREFS="project_dark_quick_items_v254";
 interface Use {boolean use(String id);}
 interface Icon {void draw(Canvas c,String id,RectF rect);}
 final Context context;final SharedPreferences prefs;
 private final String[] slots=new String[4];
 private int useSlot=-1;
 private int pressed=-1;private long downAt;private float downX,downY;private boolean moved;
 QuickItemBarV254(Context c){
  context=c;prefs=c.getSharedPreferences(PREFS,0);
  try{JSONArray a=new JSONArray(prefs.getString("slots","[null,null,null,null]"));if(a.length()==4)for(int i=0;i<4;i++)if(!a.isNull(i))slots[i]=RpgProgressionState.canonicalItemId(a.getString(i));}catch(Exception ignored){}
 }
 boolean seedFresh(String[] ids,RpgProgressionState rpg){
  if(prefs.contains("slots"))return true;if(ids.length!=4||!F5mSaveStore.writable())return false;
  JSONArray a=new JSONArray();for(String id:ids){if(id!=null&&(!eligible(id,rpg)||rpg.inventory().getOrDefault(id,0)<=0))return false;a.put(id==null?JSONObject.NULL:id);}
  if(!prefs.edit().putString("slots",a.toString()).commit())return false;System.arraycopy(ids,0,slots,0,4);return true;
 }
 String slot(int i){return i>=0&&i<4?slots[i]:null;}
 boolean assign(int i,String id,RpgProgressionState rpg){
  if(i<0||i>=4||!F5mSaveStore.writable())return false;
  if(id!=null){id=RpgProgressionState.canonicalItemId(id);if(!eligible(id,rpg)||rpg.inventory().getOrDefault(id,0)<=0)return false;}
  String[] next=slots.clone();next[i]=id;JSONArray a=new JSONArray();for(String v:next)a.put(v==null?JSONObject.NULL:v);
  if(!prefs.edit().putString("slots",a.toString()).commit())return false;System.arraycopy(next,0,slots,0,4);return true;
 }
 String[] snapshot(){return slots.clone();}
 boolean restore(String[] values){if(Arrays.equals(slots,values))return true;if(!F5mSaveStore.writable())return false;JSONArray a=new JSONArray();for(String v:values)a.put(v==null?JSONObject.NULL:v);if(!prefs.edit().putString("slots",a.toString()).commit())return false;System.arraycopy(values,0,slots,0,4);return true;}
 boolean exchange(String used,String previous,RpgProgressionState r){int index=useSlot;if(index<0||!Objects.equals(slot(index),used)){index=-1;for(int i=0;i<4;i++)if(Objects.equals(slot(i),used)){index=i;break;}}return index<0||assign(index,previous,r);}
 static boolean eligible(String id,RpgProgressionState rpg){RpgProgressionState.ItemDefinition d=rpg.itemDefinitions().get(id);return d!=null&&(d.equippable()||rpg.isConsumable(id)||rpg.isRecall(id));}
 List<String> choices(RpgProgressionState rpg){
  List<String> ids=new ArrayList<>();for(String id:rpg.inventory().keySet())if(rpg.inventory().getOrDefault(id,0)>rpg.equippedCount(id)&&eligible(id,rpg))ids.add(id);
  ids.sort(Comparator.comparingInt((String id)->"목걸이".equals(rpg.itemDefinitions().get(id).equipSlot)?0:1).thenComparing(id->rpg.itemDefinitions().get(id).name).thenComparing(id->id));return ids;
 }
 void choose(int i,RpgProgressionState rpg){
  List<String> ids=choices(rpg);String[] labels=new String[ids.size()+1];labels[0]="등록 해제";
  for(int n=0;n<ids.size();n++){String id=ids.get(n);RpgProgressionState.ItemDefinition d=rpg.itemDefinitions().get(id);labels[n+1]=d.name+" ×"+rpg.inventory().get(id)+(d.equippable()?" · "+d.equipSlot:"");}
  new AlertDialog.Builder(context).setTitle("아이템 슬롯 "+(i+1)).setItems(labels,(dialog,which)->assign(i,which==0?null:ids.get(which-1),rpg)).setNegativeButton("취소",null).show();
 }
 boolean motion(MotionEvent e,float x,float y,float offset,RpgProgressionState rpg,Use use){
  int action=e.getActionMasked();
  if(action==MotionEvent.ACTION_DOWN){pressed=-1;for(int i=0;i<4;i++)if(MobileHudLayout.quickItem(i,offset).contains(x,y)){pressed=i;downAt=e.getEventTime();downX=x;downY=y;moved=false;return true;}return false;}
  if(pressed<0)return false;
  if(action==MotionEvent.ACTION_MOVE){if(Math.hypot(x-downX,y-downY)>12)moved=true;return true;}
  if(action==MotionEvent.ACTION_UP){int i=pressed;pressed=-1;if(!moved&&MobileHudLayout.quickItem(i,offset).contains(x,y)){if(e.getEventTime()-downAt>=500||slot(i)==null)choose(i,rpg);else{useSlot=i;try{use.use(slot(i));}finally{useSlot=-1;}}}return true;}
  if(action==MotionEvent.ACTION_CANCEL)pressed=-1;return true;
 }
 void draw(Canvas c,RpgProgressionState rpg,float offset,Icon icons){
  for(int i=0;i<4;i++){RectF r=MobileHudLayout.quickItem(i,offset);String id=slot(i);boolean active=id!=null&&rpg.equipment().containsValue(id);MobileHudSkin.square(c,r,active);
   if(id!=null&&rpg.itemDefinitions().containsKey(id)){RectF inner=new RectF(r);inner.inset(4,4);c.save();c.clipRect(r.left+3,r.top+3,r.right-3,r.bottom-3);icons.draw(c,id,inner);c.restore();UiTheme.right(c,""+rpg.inventory().getOrDefault(id,0),r.right-3,r.bottom-3,8,UiTheme.TEXT,true);}
   else MobileHudSkin.icon(c,"empty",r.centerX(),r.centerY(),7,false);
   UiTheme.text(c,""+(i+1),r.left+3,r.top+9,7,UiTheme.TEXT,true);
  }
 }
}
