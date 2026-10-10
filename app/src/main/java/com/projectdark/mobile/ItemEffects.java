package com.projectdark.mobile;
import java.util.*;
/** Equipment effects and actual-use descriptions share this authority. */
final class ItemEffects {
 private ItemEffects(){}
 static String weaponName(RpgProgressionState r){RpgProgressionState.ItemDefinition d=r.equippedDefinition("무기");return d==null?"":RpgInventoryPresentation.displayName(d.name);}
 static boolean magic(String id){SkillActionContract.Rule r=SkillActionContract.get(id);return r!=null&&"마법".equals(r.kind);}
 static float baseCast(String id,float fallback){
  SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null||!magic(id))return fallback;
  // Exact official 2021-04-22 update; no guessed cast defaults for other skills.
  if(a.name.equals("홀리쇼크"))return 3f;if(a.name.equals("홀리블로우"))return 4f;
  return fallback;
 }
 static float castSeconds(RpgProgressionState r,String id,float base){
  if(!magic(id))return base;SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);
  return castFor(weaponName(r),base,a!=null&&a.heal(),a!=null&&a.status.startsWith("CURSE"));
 }
 static float castFor(String weapon,float base,boolean heal,boolean curse){
  switch(weapon){
   case "매직파나":if(heal)return 0;if(curse)return 1;return Math.max(0,base-1);
   case "매직루나":return Math.max(0,base-1);
   case "매직마르시아":return curse?1:base;
   case "매직새티아":case "매직스태프":return Math.abs(base-2)<.001f?1:base;
   case "매직쥬피티아":return Math.abs(base-4)<.001f?2:base;
   case "매직가이아":return base>=5?4:base;
   case "매직솔라":return 3;
   case "아리펠스탭":return curse?1:Math.max(0,base-1);
   case "홀리머큐리아":return heal?0:base;
   default:return base;
  }
 }
 static int mpCost(RpgProgressionState r,String id,int base){
  SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);
  // Resource-burst formulas consume a pool rather than a fixed cast cost.
  return magic(id)&&a!=null&&!a.resourceBurst()&&weaponName(r).equals("매직파나")?(int)Math.ceil(base*.9):base;
 }
 static String staffDescription(String name){switch(RpgInventoryPresentation.displayName(name)){
  case "매직파나":return "일반 마법 시전 -1초 · 저주 1초 · 회복 마법 즉시 · 고정 MP 소모 10% 절감";
  case "매직루나":return "모든 마법 시전 -1초 (최소 0초)";
  case "매직마르시아":return "저주 마법 시전 1초";
  case "매직새티아":case "매직스태프":return "기본 시전 2초인 마법을 1초로 단축";
  case "매직쥬피티아":return "기본 시전 4초인 마법을 2초로 단축";
  case "매직가이아":return "기본 시전 5초 이상인 마법을 4초로 단축";
  case "매직솔라":return "모든 마법 시전시간 3초";
  case "아리펠스탭":return "일반 마법 시전 -1초 · 저주 1초";
  case "홀리머큐리아":return "회복 마법 시전시간 0초 (즉시 회복)";
  default:return "";}}
 static int healHp(String id){if(RpgProgressionState.REAGENT_CURANUM_ITEM_ID.equals(id))return 1000;if("IT_REAGENT_CURUM".equals(id))return 200;if("IT_REAGENT_EXCURANUM".equals(id))return 10000;if(RpgProgressionState.B5_SMALL_HP_POTION_ITEM_ID.equals(id))return RpgProgressionState.B5_SMALL_HP_POTION_HEAL;return 0;}
 static int healMp(String id){return "IT_B_MP_POTION".equals(id)?100:0;}
 static boolean usable(String id){return healHp(id)>0||healMp(id)>0||RpgProgressionState.REAGENT_DIBENOMUM_ITEM_ID.equals(id);}
 static int regeneration(RpgProgressionState r){int n=0;for(String id:r.equipment().values()){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);if(d!=null)n+=d.statModifiers.getOrDefault("REGEN",0);}return Math.max(0,n);}
 static int additionalRegeneration(RpgProgressionState r){return (int)Math.min(Integer.MAX_VALUE,(long)r.baseMaxHp()*regeneration(r)/1000);}
 static List<String> description(RpgProgressionState.ItemDefinition d){
  List<String> lines=new ArrayList<>();if(d==null)return lines;
  int hp=healHp(d.itemId),mp=healMp(d.itemId);
  if(hp>0)lines.add("사용 시 HP "+hp+" 회복 · 최대 HP 초과 불가");
  if(mp>0)lines.add("사용 시 MP "+mp+" 회복 · 최대 MP 초과 불가");
  if(hp>0||mp>0)lines.add("회복할 자원이 없거나 사망 상태면 소모되지 않습니다.");
  if(RpgProgressionState.REAGENT_DIBENOMUM_ITEM_ID.equals(d.itemId))lines.add("시험 기능: 자신의 중독 해제 · 중독 시만 소모 · 원작 아이템 규칙 미확정");
  if(RpgProgressionState.RECALL_MILLES_ITEM_ID.equals(d.itemId))lines.add("살아 있을 때 밀레스마을 시작 위치로 귀환 · 1개 소모");
  if(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID.equals(d.itemId))lines.add("코마 상태 동료 회복용 · 동료/코마 대상 기능 미구현");
  if("IT_REAGENT_HOLYWATER".equals(d.itemId))lines.add("성수의 원작 사용 효과는 미확정 · 현재 사용 불가");
  String staff=staffDescription(d.name);if(!staff.isEmpty()){lines.add(staff);lines.add("재사용 대기시간·물리 공격에는 적용되지 않습니다.");}
  if(d.statModifiers.getOrDefault("REGEN",0)>0){lines.add("재생력 +"+d.statModifiers.get("REGEN")+" · 25초마다 HP 추가 회복");lines.add("시험식: 기본 최대HP × 재생력 / 1000 · MP 영향 없음");}
  String pending=SourceItemFunctions.unresolved(d.itemId);if(!pending.isEmpty())lines.add(pending);
  if(d.equippable()){
   if(d.evidence==RpgProgressionState.Evidence.PENDING)lines.add("원작 수치·착용 조건 미확정 · 외형 테스트용");
   else if(d.itemId.startsWith("IT_WARDROBE_"))lines.add("확인된 옵션만 적용 · 나머지 조건·특수 효과 확인 중");
   else if(d.evidence==RpgProgressionState.Evidence.ADAPTED||d.evidence==RpgProgressionState.Evidence.B)lines.add("일부 수치는 현재 게임의 시험 설정입니다.");
   else if(d.evidence==RpgProgressionState.Evidence.FAN||d.evidence==RpgProgressionState.Evidence.V||d.evidence==RpgProgressionState.Evidence.O||d.evidence==RpgProgressionState.Evidence.U)lines.add("장착 시 표시된 능력치·속성 적용 · 원작 전체 특수 기능 검증 전");
  }else if(lines.isEmpty())lines.add(d.itemId.contains("TOKEN")||d.itemId.contains("ESSENCE")?"퀘스트·진행용 재료 · 직접 사용/장착 불가":"용도·원작 효과 미확정 · 직접 사용/장착 불가");
  return Collections.unmodifiableList(lines);
 }
 static String actionLabel(RpgProgressionState.ItemDefinition d,RpgProgressionState r,boolean equipped){return usable(d.itemId)||r.isRecall(d.itemId)?"사용":d.equippable()?equipped?"해제":"장착":"사용 불가";}
}
