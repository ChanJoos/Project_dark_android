package com.projectdark.mobile;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Read-only journal projection. Acceptance, defeat and rewards stay in their domain owners. */
final class QuestJournalModel {
  static final String FOREST="Q_ADAPTED_FOREST_GUIDE";
  static final String JOB_CHOICE="Q_ADAPTED_FIRST_JOB";
  enum Status { AVAILABLE, ACTIVE, REPORT, LOCKED, COMPLETE }
  static final class Row {
    final String id,title,area,npc,story,objective,reward,condition,next;
    final Status status;
    final int count,goal;
    Row(String id,String title,String area,String npc,String story,String objective,String reward,String condition,String next,Status status,int count,int goal){
      this.id=id;this.title=title;this.area=area;this.npc=npc;this.story=story;this.objective=objective;this.reward=reward;this.condition=condition;this.next=next;this.status=status;this.count=count;this.goal=goal;
    }
    String label(){switch(status){case AVAILABLE:return "수락 가능";case ACTIVE:return "진행 중";case REPORT:return "보고 가능";case COMPLETE:return "완료";default:return "잠김";}}
    boolean navigable(){return status==Status.AVAILABLE||status==Status.ACTIVE||status==Status.REPORT;}
    String action(){return status==Status.AVAILABLE?"수락하러 이동":status==Status.REPORT?"보고하러 이동":status==Status.ACTIVE?"목표로 이동":status==Status.COMPLETE?"완료한 퀘스트":"아직 받을 수 없습니다";}
  }
  private final Context context;
  private final List<Row> future=new ArrayList<>();
  QuestJournalModel(Context context){this.context=context;try(java.io.InputStream in=context.getAssets().open("quests/journal.json")){
    JSONArray a=new JSONArray(new String(read(in),StandardCharsets.UTF_8));
    for(int i=0;i<a.length();i++){JSONObject q=a.getJSONObject(i);future.add(new Row(q.getString("id"),q.getString("title"),q.getString("area"),q.getString("npc"),q.getString("story"),q.getString("objective"),q.getString("reward"),q.getString("condition"),"해당 지역이 열리면 다음 모험을 시작할 수 있습니다.",Status.LOCKED,0,0));}
  }catch(Exception e){throw new IllegalStateException("Quest journal catalog unavailable",e);}}
  private static byte[] read(java.io.InputStream in)throws java.io.IOException{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];for(int n;(n=in.read(bytes))!=-1;)out.write(bytes,0,n);return out.toByteArray();}
  void visitForest(F5mAdaptedPrologueQuest q,GrowthQuest2 g){if(q.state()==F5mAdaptedPrologueQuest.State.COMPLETED&&g.state()==GrowthQuest2.State.COMPLETED&&F5mSaveStore.writable())context.getSharedPreferences("project_dark_journal_v1",0).edit().putBoolean("forest_visited",true).commit();}
  List<Row> rows(F5mAdaptedPrologueQuest q,GrowthQuest2 g,boolean inForest){return rows(q,g,inForest,null);}
  List<Row> rows(F5mAdaptedPrologueQuest q,GrowthQuest2 g,boolean inForest,RpgProgressionState rpg){
    List<Row> out=new ArrayList<>();Status s=Status.valueOf(q.state()==F5mAdaptedPrologueQuest.State.RETURN_READY?"REPORT":q.state()==F5mAdaptedPrologueQuest.State.COMPLETED?"COMPLETE":q.state().name());
    String goal=s==Status.AVAILABLE?"James에게 여관 의뢰 받기":s==Status.ACTIVE?"여관 생쥐 처치":s==Status.REPORT?"여관의 Benjamin에게 보고":"여관의 소란을 해결했습니다";
    out.add(new Row(q.questId(),"여관의 소란","밀레스 · 여관","James → Benjamin","여관의 손님들이 생쥐 때문에 곤란해하고 있습니다. James의 의뢰를 받아 여관 안을 살펴보세요.",goal,"EXP 7,500  ·  Gold 100  ·  훈련 증표 1","처음부터 받을 수 있습니다","완료 후: James의 성장 훈련",s,q.currentCount(),q.requiredCount()));
    Status gs=Status.valueOf(g.state()==GrowthQuest2.State.RETURN_READY?"REPORT":g.state()==GrowthQuest2.State.COMPLETED?"COMPLETE":g.state().name());
    String ggoal=gs==Status.LOCKED?"여관의 소란 완료":gs==Status.AVAILABLE?"James에게 성장 훈련 받기":gs==Status.ACTIVE?"밀레스 훈련 몬스터 처치":gs==Status.REPORT?"James에게 훈련 결과 보고":"성장 훈련을 마쳤습니다";
    out.add(new Row(GrowthQuest2.QUEST_ID,"성장 훈련","밀레스 · 마을","James","여관의 문제를 해결했다면 마을 밖으로 나갈 준비를 하세요. James가 전투 훈련을 도와줍니다.",ggoal,"EXP 15,000  ·  Gold 250","선행: 여관의 소란 완료","완료 후: William의 포테 숲길 안내",gs,g.currentCount(),g.requiredCount()));
    boolean visited=inForest||context.getSharedPreferences("project_dark_journal_v1",0).getBoolean("forest_visited",false);
    Status fs=gs!=Status.COMPLETE?Status.LOCKED:visited?Status.COMPLETE:Status.AVAILABLE;
    out.add(new Row(FOREST,"포테의 숲길","밀레스 → 포테의 숲","William","훈련을 마쳤습니다. 숲길 경비 William에게 말을 걸면 포테의 숲으로 안내받을 수 있습니다.",fs==Status.LOCKED?"성장 훈련 완료":fs==Status.COMPLETE?"포테의 숲에 도착했습니다":"William와 대화하고 숲으로 이동","포테의 숲 탐험","선행: 성장 훈련 완료","다음 모험: 팜팻의 변화 · 의뢰 지역 개방 필요",fs,visited?1:0,1));
    if(rpg!=null){boolean ready=gs==Status.COMPLETE&&rpg.normalLevel()!=null&&rpg.normalLevel()>=3;Status js=!"COMMONER".equals(rpg.currentJobCode())?Status.COMPLETE:ready?Status.AVAILABLE:Status.LOCKED;out.add(new Row(JOB_CHOICE,"기본 직업 선택","밀레스 · 광장","Michael","성장 훈련과 Lv3을 달성했습니다. Michael과 대화해 전사·도적·마법사·성직자·무도가 중 하나를 고르세요.",js==Status.COMPLETE?"기본 직업을 선택했습니다":ready?"Michael과 대화해 직업과 입문 장비 선택":"성장 훈련 완료 · Lv3 도달","직업별 입문 장비 1세트","선행: 성장 훈련 완료 · Lv3","직업 선택 후 포테의 숲으로 이동",js,js==Status.COMPLETE?1:0,1));}
    if(rpg!=null)for(CampaignProgress.Def d:CampaignProgress.definitions()){
      if(d.kind.equals("EXTERNAL"))continue;CampaignProgress.Status cs=rpg.campaign().status(d,rpg);
      String objective=rpg.campaign().objective(d);
      String reason="Lv"+d.level+" · 선행 의뢰 완료"+(d.job==null?"":" · "+CampaignProgress.jobName(d.job));
      if(cs==CampaignProgress.Status.AVAILABLE||cs==CampaignProgress.Status.REPORT)objective=NpcIdentity.forId(d.npc).name+"에게 "+(cs==CampaignProgress.Status.AVAILABLE?"의뢰 수락":"결과 보고");
      if(cs==CampaignProgress.Status.LOCKED)objective=rpg.normalLevel()<d.level?"Lv"+d.level+"까지 사냥 후 의뢰 받기":"선행 의뢰 또는 직업 조건 확인";
      out.add(new Row("CAMPAIGN_"+d.id,d.title,com.projectdark.mobile.world.CampaignWorld.contains(d.map)?com.projectdark.mobile.world.CampaignWorld.title(d.map):"밀레스",NpcIdentity.forId(d.npc).name,"밀레스에서 시작한 모험이 포테의 숲과 피에트 조사 거점으로 이어집니다.",objective,"EXP "+d.exp+" · Gold "+d.gold+(d.id.startsWith("J02_")||d.id.startsWith("J03_")?" · 직업 장비":""),reason,"완료 후 다음 의뢰가 열립니다",Status.valueOf(cs.name()),rpg.campaign().count(d,rpg),d.goal));
    }
    if(rpg!=null){CampaignProgress.Def next=rpg.campaign().next(rpg);if(next!=null&&!next.kind.equals("EXTERNAL")&&rpg.normalLevel()<next.level)out.add(new Row("CAMPAIGN_HUNT_"+next.id,"다음 의뢰를 위한 성장","현재 사냥 구역","", "사냥 보상으로 레벨을 올린 뒤 다음 의뢰를 받으세요.","Lv"+next.level+"까지 성장","몬스터 EXP · Gold","","목표를 누르면 현재 레벨에 맞는 사냥터로 안내합니다.",Status.ACTIVE,rpg.normalLevel(),next.level));}
    out.addAll(future);return out;
  }
  Row current(F5mAdaptedPrologueQuest q,GrowthQuest2 g,boolean forest){return current(q,g,forest,null);}
  Row current(F5mAdaptedPrologueQuest q,GrowthQuest2 g,boolean forest,RpgProgressionState rpg){List<Row> all=rows(q,g,forest,rpg);for(Row r:all)if(JOB_CHOICE.equals(r.id)&&r.navigable())return r;for(Row r:all)if(r.id.startsWith("CAMPAIGN_")&&r.navigable())return r;for(Row r:all)if(r.navigable())return r;return null;}
  int available(List<Row> rows){int n=0;for(Row r:rows)if(r.status==Status.AVAILABLE)n++;return n;}
  int attention(List<Row> rows){int n=0;for(Row r:rows)if(r.status==Status.AVAILABLE||r.status==Status.REPORT)n++;return n;}
}
