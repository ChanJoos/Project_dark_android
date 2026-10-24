package com.projectdark.mobile;
import com.projectdark.mobile.world.HorrorCastleGeometry;
import org.json.*;
import java.util.*;
/** One instance owns fifteen encounters per repeating lap, including death state across process restarts. */
public final class HorrorCastleRun {
 private long seed=1;private final Map<String,Integer> hp=new LinkedHashMap<>();private int clearedMask;private long serial;
 public long seed(){return seed;}
 public void start(){seed=new Random().nextLong();serial++;clearedMask=0;hp.clear();}
 public void record(String map,List<RuntimeState.Monster> actors){if(HorrorCastleGeometry.room(map)==0)return;for(RuntimeState.Monster m:actors)hp.put(m.id,m.hp);}
 public void apply(List<RuntimeState.Monster> actors){for(RuntimeState.Monster m:actors){Integer saved=hp.get(m.id);if(saved!=null){m.hp=Math.max(0,Math.min(m.maxHp,saved));m.alive=m.hp>0;m.state=m.alive?RuntimeState.Monster.State.IDLE:RuntimeState.Monster.State.DEAD;m.respawnClock=Float.POSITIVE_INFINITY;}}}
 public boolean clear(String map,List<RuntimeState.Monster> actors){int n=HorrorCastleGeometry.room(map);if(n==0||actors.size()!=15)return false;for(RuntimeState.Monster m:actors)if(m.alive)return false;record(map,actors);clearedMask|=1<<(n-1);return true;}
 public String next(String map){int n=HorrorCastleGeometry.room(map);return n==HorrorCastleGeometry.ROOM_COUNT?HorrorCastleGeometry.roomId(1):HorrorCastleGeometry.roomId(n+1);}
 public int clearedMask(){return clearedMask;}
 public JSONObject snapshot(){try{JSONObject states=new JSONObject();for(Map.Entry<String,Integer> e:hp.entrySet())states.put(e.getKey(),e.getValue());return new JSONObject().put("seed",seed).put("serial",serial).put("cleared",clearedMask).put("hp",states);}catch(JSONException e){throw new IllegalStateException(e);}}
 public boolean restore(JSONObject data){try{long s=data.getLong("seed"),ser=data.getLong("serial");int clear=data.getInt("cleared");if(clear<0||clear>((1<<HorrorCastleGeometry.ROOM_COUNT)-1))return false;JSONObject h=data.getJSONObject("hp");Map<String,Integer> staged=new LinkedHashMap<>();if(h.length()>HorrorCastleGeometry.ROOM_COUNT*15)return false;for(Iterator<String> it=h.keys();it.hasNext();){String k=it.next();int v=h.getInt(k);if(!k.startsWith("HORROR_")||v<0||v>140000)return false;staged.put(k,v);}seed=s;serial=ser;clearedMask=clear;hp.clear();hp.putAll(staged);return true;}catch(Exception e){return false;}}
}
