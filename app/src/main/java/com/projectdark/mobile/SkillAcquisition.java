package com.projectdark.mobile;

import java.util.*;
import org.json.*;

/** Project learning rules: stats + Gold/materials only. One explicit book action, one payment/save. */
public final class SkillAcquisition {
  public static final String[] STATS={"STR","INT","WIS","CON","DEX"};
  public interface Durability {boolean checkpoint();}
  public static final class Material {
    public final String id,name;public final int required,owned;
    Material(String id,String name,int required,int owned){this.id=id;this.name=name;this.required=required;this.owned=owned;}
  }
  public static final class Quote {
    public final int[] required,current;public final long gold,ownedGold;
    public final boolean learned,canLearn;public final List<Material> materials;public final List<String> blockers;
    Quote(int[] req,int[] cur,long gold,long owned,boolean learned,List<Material> materials,List<String> blockers){
      required=req;current=cur;this.gold=gold;ownedGold=owned;this.learned=learned;
      this.materials=Collections.unmodifiableList(materials);this.blockers=Collections.unmodifiableList(blockers);canLearn=!learned&&blockers.isEmpty();
    }
  }
  private final SkillBook book;
  public SkillAcquisition(SkillBook book){this.book=book;}
  public Quote quote(SkillBook.Entry e,RpgProgressionState r){
    FinalStats f=r.finalStats();int[] current={f.str,f.intel,f.wis,f.con,f.dex},required=new int[5];
    List<String> blocked=new ArrayList<>();List<Material> materials=new ArrayList<>();JSONObject policy=e==null?null:book.learningPolicy(e.id);long price=-1;
    if(policy==null){blocked.add("습득 정보를 불러오지 못했습니다");}
    else{
      JSONObject stats=policy.optJSONObject("stats");price=policy.optLong("gold",-1);
      if(stats==null||price<0)blocked.add("습득 정보 오류");
      else for(int i=0;i<5;i++){required[i]=stats.optInt(STATS[i],-1);if(required[i]<0)blocked.add("스탯 정보 오류");else if(current[i]<required[i])blocked.add(STATS[i]+" "+required[i]+" 필요");}
      if(r.gold()<price)blocked.add("골드 "+(price-r.gold())+" 부족");
      JSONObject items=policy.optJSONObject("items");if(items==null)blocked.add("재료 정보 오류");
      else{Iterator<String> it=items.keys();while(it.hasNext()){String id=it.next();int count=items.optInt(id,-1);RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);int owned=r.inventory().getOrDefault(id,0);
        if(d==null||d.equippable()||count<=0){blocked.add("재료 정보 오류");continue;}
        materials.add(new Material(id,d.name,count,owned));if(owned<count)blocked.add(d.name+" "+(count-owned)+"개 부족");
      }}
    }
    return new Quote(required,current,price,r.gold(),e!=null&&book.learned(e.id),materials,blocked);
  }
  public List<String> blockers(SkillBook.Entry e,RpgProgressionState r){return quote(e,r).blockers;}
  public boolean learn(String id,RpgProgressionState r){return learn(id,r,()->true);}
  public boolean learn(String id,RpgProgressionState r,Durability durability){
    SkillBook.Entry e=book.get(id);Quote q=quote(e,r);if(!q.canLearn||durability==null)return false;
    Map<String,Integer> costs=new LinkedHashMap<>();for(Material m:q.materials)costs.put(m.id,m.required);
    JSONObject beforeBook=book.snapshot();long beforeGold=r.gold();Map<String,Integer> beforeItems=new LinkedHashMap<>(r.inventory());Map<String,String> beforeEquipment=new LinkedHashMap<>(r.equipment());
    if(!r.paySkillLearningCost(q.gold,costs))return false;
    boolean committed=false;
    try{committed=book.learn(id,0)&&durability.checkpoint();return committed;}
    finally{if(!committed){r.restoreGold(beforeGold);if(!r.restoreOwnedItems(beforeItems,beforeEquipment)||!book.restore(beforeBook))throw new IllegalStateException("learning rollback failed");}}
  }
  public String description(SkillBook.Entry e,RpgProgressionState r){Quote q=quote(e,r);return q.learned?"습득 완료":q.canLearn?"스탯과 습득 비용을 충족했습니다":String.join(" · ",q.blockers);}
}
