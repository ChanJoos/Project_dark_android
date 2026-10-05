package com.projectdark.mobile;
import com.projectdark.mobile.world.TownInteriorDef;import java.util.*;
/** Authoritative catalogs and atomic, durable trading. Prices are project balance except supplied recall1000. */
public final class TownCommerce {
 public enum Operation { BUY,SELL,DEPOSIT_GOLD,WITHDRAW_GOLD,DEPOSIT_ITEM,WITHDRAW_ITEM,HEAL }
 public enum Result { OK,INVALID,INSUFFICIENT_GOLD,NOT_OWNED,EQUIPPED,FULL,SAVE_FAILED }
 public static final class Offer {public final String id,name,category,description;public final long price;public Offer(String id,String n,String cat,long p,String desc){this.id=id;name=n;category=cat;price=p;description=desc;}}
 private static final List<Offer> REAGENTS=Collections.unmodifiableList(Arrays.asList(
  new Offer("IT_B_MP_POTION","마력 물약","시약류",25,"마력 100 회복 · 한 병씩 사용"),
  new Offer("IT_REAGENT_KOMADIUM","코마디움","시약류",100,"기술 수련에 사용하는 기본 시약"),
  new Offer("IT_REAGENT_DIBENOMUM","디베노뭄","시약류",100,"기술 수련에 사용하는 해독 계열 시약"),
  new Offer("IT_REAGENT_CURANUM","쿠라눔","시약류",50,"성직자 기술 수련에 사용하는 시약"),
  new Offer("IT_REAGENT_CURUM","쿠룸","시약류",100,"생명력 100 회복 · 한 병씩 사용"),
  new Offer("IT_REAGENT_EXCURANUM","엑스쿠라눔","시약류",50000,"생명력 10,000 회복 · 되팔기 불가"),
  new Offer("IT_REAGENT_HOLYWATER","성수","시약류",300,"성직자 수련과 의식용 성수"),
  new Offer("IT_RECALL_MILLES","밀레스리콜","리콜류",1000,"밀레스마을 귀환 스크롤")));
 private static final List<Offer> EQUIPMENT=campaignEquipment(Arrays.asList(
  new Offer("IT_TEST_WEAPON_MW002","에페","무기",300,"세검 · DAM 4 / HIT 1"),
  new Offer("IT_TEST_WEAPON_MW003","커틀라스","무기",500,"곡도 · DAM 5"),
  new Offer(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,"목도","무기",100,"모험을 시작하는 기본 목검"),
  new Offer("IT_SHOP_WEAPON_MW004","세이버","무기",900,"장검 · DAM 7 / HIT 2"),
  new Offer("IT_SHOP_WEAPON_MW005","그라디우스","무기",1400,"중검 · DAM 9 / HIT 1"),
  new Offer("IT_TEST_ARMOR_MU0000002","레더튜닉","방어구",200,"가죽 상의 · AC -2"),
  new Offer("IT_TEST_ARMOR_MU0000003","도복","방어구",300,"무도가 의복 · AC -2 / DEX 1"),
  new Offer("IT_SHOP_ARMOR_JIPON","지폰","방어구",500,"레벨 11 · 전사 의복 / AC -3"),
  new Offer("IT_TEST_SHIELD_MS002","방패","방어구",200,"기본 방패 · AC -3"),
  new Offer("IT_TEST_SHIELD_MS003","강화 방패","방어구",450,"보강 방패 · AC -4"),
  new Offer("IT_SHOES","신발","방어구",100,"기본 신발 · 모험가용"),
  new Offer("IT_TEST_SHOES_ML229","가죽 신발","방어구",150,"가벼운 신발 · DEX 2"),
  new Offer(RpgProgressionState.STARTER_HAT_ITEM_ID,"밀레스털모자","방어구",150,"밀레스의 기본 털모자"),
  new Offer("IT_GLOVE_LEATHER","가죽장갑","장신구",200,"레벨 11 · 공통 장갑"),
  new Offer("IT_LEGGING_LEATHER","가죽각반","장신구",200,"레벨 11 · 공통 각반"),
  new Offer("IT_EARRING_DOUBLE_SILVER","쌍은귀걸이","장신구",300,"레벨 11 · 전사 / 도적 / 무도가"),
  new Offer("IT_RING_REDJADE","홍옥반지","장신구",350,"레벨 11 · 공통 반지"),
  new Offer("IT_RING_GORU","고루반지","장신구",350,"레벨 11 · 마법사 / 성직자")));
 private static List<Offer> campaignEquipment(List<Offer> starter){List<Offer> out=new ArrayList<>(starter);for(String job:CampaignProgress.JOBS)for(int tier:new int[]{11,26}){out.add(new Offer("IT_B_CAMPAIGN_"+job+"_"+tier,CampaignProgress.jobName(job)+(tier==11?" 견습 장비":" 숙련 장비"),"방어구",tier==11?600:1800,"직업 의상 · 레벨 "+tier));out.add(new Offer("IT_B_CAMPAIGN_TOOL_"+job+"_"+tier,CampaignProgress.jobName(job)+(tier==11?" 수련 도구":" 숙련 도구"),job.equals("MARTIAL_ARTIST")?"장신구":"무기",tier==11?400:1200,"직업 전투 도구 · 레벨 "+tier));}return Collections.unmodifiableList(out);}
 public static List<Offer> offers(TownInteriorDef.Kind k){return k==TownInteriorDef.Kind.REAGENT?REAGENTS:k==TownInteriorDef.Kind.EQUIPMENT?EQUIPMENT:Collections.emptyList();}
 public static Offer find(TownInteriorDef.Kind k,String id){for(Offer o:offers(k))if(o.id.equals(id))return o;return null;}
 public static long sellPrice(String id){for(TownInteriorDef.Kind k:TownInteriorDef.Kind.values()){Offer o=find(k,id);if(o!=null)return "IT_REAGENT_EXCURANUM".equals(id)?0:o.price/2;}return -1;}
 public interface Durability {boolean save();}
 public static Result transact(RuntimeState state,TownInteriorDef.Kind kind,Operation op,String id,int quantity,Durability durability){
  if(state==null||kind==null||op==null||durability==null||quantity<=0||quantity>999999)return Result.INVALID;
  RpgProgressionState r=state.rpg();Map<String,Integer> inv=new LinkedHashMap<>(r.inventory()),bank=new LinkedHashMap<>(r.bankInventory());Map<String,String> eq=new LinkedHashMap<>(r.equipment());Map<String,Integer> originalInv=new LinkedHashMap<>(inv),originalBank=new LinkedHashMap<>(bank);long gold=r.gold(),balance=r.bankGold();int hp=state.player().hp,mp=state.player().mp;org.json.JSONObject campaign=r.campaign().snapshot();
  if(op==Operation.BUY){Offer o=find(kind,id);if(o==null)return Result.INVALID;long total=o.price*quantity;if(gold<total)return Result.INSUFFICIENT_GOLD;if(r.autoLootResolvedItem(id,quantity)!=RpgProgressionState.AutoLootResult.LOOTED)return Result.FULL;r.restoreGold(gold-total);r.campaign().bought();}
  else if(op==Operation.SELL){if(kind!=TownInteriorDef.Kind.REAGENT&&kind!=TownInteriorDef.Kind.EQUIPMENT)return Result.INVALID;long unit=sellPrice(id);if(unit<=0)return Result.INVALID;if(eq.containsValue(id))return Result.EQUIPPED;if(inv.getOrDefault(id,0)<quantity)return Result.NOT_OWNED;if(gold>Long.MAX_VALUE-unit*quantity)return Result.FULL;remove(inv,id,quantity);r.restoreOwnedItems(inv,eq);r.restoreGold(gold+unit*quantity);r.campaign().sold();}
  else if(op==Operation.DEPOSIT_GOLD||op==Operation.WITHDRAW_GOLD){if(kind!=TownInteriorDef.Kind.BANK)return Result.INVALID;boolean deposit=op==Operation.DEPOSIT_GOLD;if((deposit?gold:balance)<quantity)return Result.INSUFFICIENT_GOLD;if((deposit?balance:gold)>Long.MAX_VALUE-quantity)return Result.FULL;r.restoreGold(gold+(deposit?-quantity:quantity));r.restoreBank(balance+(deposit?quantity:-quantity),bank);}
  else if(op==Operation.DEPOSIT_ITEM||op==Operation.WITHDRAW_ITEM){if(kind!=TownInteriorDef.Kind.BANK||!r.itemDefinitions().containsKey(id))return Result.INVALID;boolean deposit=op==Operation.DEPOSIT_ITEM;if(deposit&&eq.containsValue(id))return Result.EQUIPPED;Map<String,Integer> from=deposit?inv:bank,to=deposit?bank:inv;if(from.getOrDefault(id,0)<quantity)return Result.NOT_OWNED;if(to.getOrDefault(id,0)>999999-quantity)return Result.FULL;remove(from,id,quantity);to.put(id,to.getOrDefault(id,0)+quantity);r.restoreOwnedItems(inv,eq);r.restoreBank(balance,bank);}
  else if(op==Operation.HEAL){if(kind!=TownInteriorDef.Kind.CHURCH)return Result.INVALID;if(!state.player().alive)return Result.INVALID;if(gold<50)return Result.INSUFFICIENT_GOLD;if(hp==state.player().maxHp&&mp==state.player().maxMp)return Result.INVALID;r.restoreGold(gold-50);state.player().hp=state.player().maxHp;state.player().mp=state.player().maxMp;}
  boolean saved;try{saved=durability.save();}catch(RuntimeException e){saved=false;}
  if(!saved){r.restoreGold(gold);r.restoreOwnedItems(originalInv,eq);r.restoreBank(balance,originalBank);r.campaign().restore(campaign);state.player().hp=hp;state.player().mp=mp;return Result.SAVE_FAILED;}
  return Result.OK;
 }
 private static void remove(Map<String,Integer> m,String id,int q){int n=m.get(id)-q;if(n==0)m.remove(id);else m.put(id,n);}
}
