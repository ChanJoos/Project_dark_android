package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Combat·Monster projection boundary for Master-backed monster definitions.
 * Only positively matched Master rows are registered as canonical. Runtime fixtures remain explicit PENDING.
 */
public final class MonsterDefinitionRegistry {
  private final Map<String,MonsterDefinition> definitions=new LinkedHashMap<>();

  public MonsterDefinitionRegistry(){
    // master/data/Monster_Master.csv — exact verified row; region intentionally remains 포테의숲.
    definitions.put("POTE_SPIRIT",new MonsterDefinition(
        "POTE_SPIRIT","포테의정령","포테의숲",48,29201,308950,
        "세줄금반지/금전더미/강력한연필/포테정령의뿔",
        MonsterDefinition.Evidence.V,MonsterDefinition.Status.CANONICAL));

    // Current Milles runtime combat fixture. It is not a Master monster and receives no canonical reward.
    definitions.put("combat_dummy_01",new MonsterDefinition(
        "combat_dummy_01","훈련용 몬스터 [B]","밀레스 runtime prototype",null,null,null,null,
        MonsterDefinition.Evidence.B,MonsterDefinition.Status.PROTOTYPE_PENDING));
  }

  public MonsterDefinition resolve(String monsterId){
    MonsterDefinition def=definitions.get(monsterId);
    if(def!=null)return def;
    return new MonsterDefinition(monsterId,null,null,null,null,null,null,
        MonsterDefinition.Evidence.PENDING,MonsterDefinition.Status.UNRESOLVED);
  }

  public Map<String,MonsterDefinition> definitions(){return Collections.unmodifiableMap(definitions);}
}
