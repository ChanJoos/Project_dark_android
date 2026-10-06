package com.projectdark.mobile;

/** Mobile learning curriculum; descriptions refer to real controls and checked actions. */
final class OnboardingGuide {
 private OnboardingGuide(){}
 static String objective(CampaignProgress.Def d){switch(d.kind){
  case "STAT":return "능력치 포인트 1개 직접 배분";
  case "LEARN":return "직업의 첫 기술을 직접 습득";
  case "SLOT":return "습득한 첫 기술을 퀵슬롯에 등록";
  case "BUY":return d.id.equals("T04")?"신발 1개 구매":d.id.equals("T06")?"소형 회복물약 1개 구매":"마력 물약 1개 구매";
  case "EQUIP":return "구매한 신발 직접 장착";
  case "USE":return d.id.equals("T07")?"물약으로 실제 HP 회복 1회":"물약으로 실제 MP 회복 1회";
  case "QUICK_USE":return "하단 퀵슬롯 물약으로 실제 회복 1회";
  case "SELL":return "남는 마력 물약 1개 판매";default:return "목표 달성";
 }}
 static String chapter(CampaignProgress.Def d){if(d.id.equals("M01")||d.id.equals("M02"))return "1장 · 첫 전투";if(d.id.startsWith("T")||d.id.equals("M03")||d.id.equals("M04")||d.id.equals("M05")||d.id.startsWith("J01"))return "2장 · 모험의 기본기";if(d.level<18)return "3장 · 포테의 숲";if(d.level<30)return "4장 · 피에트 조사";return "5장 · 숲의 결계";}
 static String story(CampaignProgress.Def d){switch(d.kind){
  case "STAT":return "마이클: 장비만으로는 부족하지. 성장 포인트를 네 손으로 배분해 보게.";
  case "LEARN":return "마이클: 기술은 책에서 직접 배운다네. 조건과 비용을 먼저 확인하게.";
  case "SLOT":return "마이클: 배운 기술은 퀵슬롯에 올려야 전투 중 바로 쓸 수 있지.";
  case "EQUIP":return "마이클: 구매와 장착은 다르다네. 신발을 착용하고 장비창을 확인하게.";
  case "BUY":return "마이클: 상점에서는 상품과 수량, 최종 금액을 확인한 뒤 구매하게.";
  case "USE":return "마이클: 물약은 부족한 생명력이나 마력을 되찾을 때 쓰는 것이네.";
  case "SELL":return "마이클: 필요 없는 물품은 팔 수 있지만, 장착한 장비는 먼저 해제해야 하네.";
  case "SKILL":return "스승: 연습은 자네 손으로 해야지. 등록한 기술을 세 번 사용하고 돌아오게.";
  case "KILL":return d.id.equals("M04")?"경비: 들쥐 세 마리를 정리해 주게. 의뢰 목표를 누르면 자동으로 접근해 공격하네.":"경비: 이 구역의 위험한 개체를 정리하고 숲길을 안전하게 만들어 주세요.";
  case "PAIR":return "조사관: 서로 다른 개체를 균형 있게 조사해야 변화의 원인을 알 수 있습니다.";
  case "VISIT":return "안내자: 준비를 마쳤다면 다음 지역으로 갑시다. 길을 따라 이동해 도착을 확인하세요.";
  case "ALTAR":return "정제사: 모아 둔 정수를 세 제단에 하나씩 바쳐 결계를 안정시켜 주세요.";
  default:return "먼저 현재 의뢰를 마치세요. 다음 단계는 완료한 뒤 차례로 열립니다.";
 }}
 static String how(CampaignProgress.Def d,RpgProgressionState r,SkillBook book){String skill=CampaignProgress.beginnerSkill(r.currentJobCode());SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(skill);String name=a==null?"첫 기술":a.name;
  switch(d.kind){
   case "STAT":return "① 안내 버튼으로 능력치창 열기\n② 원하는 능력치 옆 + 누르기\nSTR: 물리 공격 · INT: 마법 공격\nDEX: 명중 · CON/WIS: HP/MP 성장\n"+(r.currentJobCode().equals("MAGE")?"첫 마법 조건: INT 6 · WIS 4. 조건을 먼저 맞추세요.":r.currentJobCode().equals("CLERIC")?"성직자는 INT/WIS, 근접 직업은 STR/CON을 먼저 살펴보세요.":"근접 직업은 STR/CON을 먼저 살펴보세요.");
   case "LEARN":return "① 안내 버튼으로 "+name+" 선택\n② 조건 탭에서 스탯·Gold 확인\n③ 부족한 스탯은 능력치창의 +로 배분\n④ 습득 버튼 누르기 (첫 기술 50 Gold)\n시험 목록은 습득으로 인정되지 않습니다.";
   case "SLOT":return "① 안내 버튼으로 "+name+" 선택\n② 등록 버튼 누르기\n③ 아래 8개 슬롯 중 빈 슬롯 선택\n④ 책을 닫고 하단 기술 아이콘 확인\n이미 실제 슬롯에 등록했다면 완료 가능합니다.";
   case "BUY":return "① 목표 안내로 상점 주인에게 이동\n② 구매 탭에서 "+(d.id.equals("T04")?"신발":d.id.equals("T06")?"소형 회복물약":"마력 물약")+" 선택\n③ 수량 1과 금액 확인 후 구매\n구매 전 보유하던 물품은 구매 횟수로 세지 않습니다.";
   case "EQUIP":return "① 안내 버튼으로 가방 열기\n② 신발을 선택하고 장착 누르기\n③ 장비창의 신발 슬롯 확인\n장비 선택만 하거나 해제한 행동은 세지 않습니다.";
   case "USE":return "① "+(d.id.equals("T07")?"들쥐에게 맞아 HP가 줄어들면":"습득한 기술을 사용해 MP가 줄어들면")+"\n② "+(d.id.equals("T07")?"하단 HP 물약 또는 가방의 소형 회복물약":"하단 MP 물약 또는 가방의 마력 물약")+" 사용\n③ 실제 회복되면 목표 완료\n가득 찬 상태의 사용은 물약과 횟수를 소모하지 않습니다.";
   case "QUICK_USE":return "① "+((r.currentJobCode().equals("MAGE")||r.currentJobCode().equals("CLERIC"))?"기술로 MP를 소모한 뒤 하단 MP 물약":"들쥐에게 맞은 뒤 하단 HP 물약")+" 누르기\n② 실제 회복되면 완료\n근접 직업의 첫 기술은 MP를 쓰지 않습니다. 마력 물약은 이후 기술을 위해 준비합니다.";
   case "SELL":return "① 목표 안내로 시약점에 이동\n② 판매 탭에서 마력 물약 선택\n③ 수량 1과 받을 Gold 확인 후 판매\n은행 보관과 버리기는 판매로 인정하지 않습니다.";
   case "SKILL":String id=r.campaign().practiceSkill(d,r);SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(id);return "① 기술창에서 "+(ability==null?"목표 기술":ability.name)+" 등록 확인\n② 목표 안내로 들판의 적에게 접근\n③ 하단의 등록한 기술을 직접 3회 사용\n"+(ability!=null&&ability.heal()?"회복 기술은 HP가 감소한 상태에서 실제 회복해야 합니다.":"MP·거리·재사용 시간을 확인하세요. 거절된 입력은 세지 않습니다.");
   case "KILL":return "① 의뢰를 수락한 뒤 목표 안내 누르기\n② 목표까지 이동·타기팅·자동 공격\n③ 목표 수를 채우면 보고 가능으로 변경\n④ 안내 버튼으로 의뢰인에게 돌아가 완료";
   default:return "① 선행 의뢰를 마치고 수락\n② 목표 안내를 따라 실제 행동 수행\n③ 보고 가능이 되면 의뢰인에게 완료\n같은 의뢰의 보상은 한 번만 받을 수 있습니다.";
  }
 }
}
