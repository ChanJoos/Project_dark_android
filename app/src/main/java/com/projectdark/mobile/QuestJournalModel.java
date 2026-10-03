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
  List<Row> rows(F5mAdaptedPrologueQuest q,GrowthQuest2 g,boolean inForest){
    List<Row> out=new ArrayList<>();Status s=Status.valueOf(q.state()==F5mAdaptedPrologueQuest.State.RETURN_READY?"REPORT":q.state()==F5mAdaptedPrologueQuest.State.COMPLETED?"COMPLETE":q.state().name());
    String goal=s==Status.AVAILABLE?"제임스에게 여관 의뢰 받기":s==Status.ACTIVE?"여관 생쥐 처치":s==Status.REPORT?"여관의 메리에게 보고":"여관의 소란을 해결했습니다";
    out.add(new Row(q.questId(),"여관의 소란","밀레스 · 여관","제임스 → 메리","여관의 손님들이 생쥐 때문에 곤란해하고 있습니다. 제임스의 의뢰를 받아 여관 안을 살펴보세요.",goal,"EXP 7,500  ·  Gold 100  ·  훈련 증표 1","처음부터 받을 수 있습니다","완료 후: 제임스의 성장 훈련",s,q.currentCount(),q.requiredCount()));
    Status gs=Status.valueOf(g.state()==GrowthQuest2.State.RETURN_READY?"REPORT":g.state()==GrowthQuest2.State.COMPLETED?"COMPLETE":g.state().name());
    String ggoal=gs==Status.LOCKED?"여관의 소란 완료":gs==Status.AVAILABLE?"제임스에게 성장 훈련 받기":gs==Status.ACTIVE?"밀레스 훈련 몬스터 처치":gs==Status.REPORT?"제임스에게 훈련 결과 보고":"성장 훈련을 마쳤습니다";
    out.add(new Row(GrowthQuest2.QUEST_ID,"성장 훈련","밀레스 · 마을","제임스","여관의 문제를 해결했다면 마을 밖으로 나갈 준비를 하세요. 제임스가 전투 훈련을 도와줍니다.",ggoal,"EXP 15,000  ·  Gold 250","선행: 여관의 소란 완료","완료 후: 한스의 포테 숲길 안내",gs,g.currentCount(),g.requiredCount()));
    boolean visited=inForest||context.getSharedPreferences("project_dark_journal_v1",0).getBoolean("forest_visited",false);
    Status fs=gs!=Status.COMPLETE?Status.LOCKED:visited?Status.COMPLETE:Status.AVAILABLE;
    out.add(new Row(FOREST,"포테의 숲길","밀레스 → 포테의 숲","한스","훈련을 마쳤습니다. 숲길 경비 한스에게 말을 걸면 포테의 숲으로 안내받을 수 있습니다.",fs==Status.LOCKED?"성장 훈련 완료":fs==Status.COMPLETE?"포테의 숲에 도착했습니다":"한스와 대화하고 숲으로 이동","포테의 숲 탐험","선행: 성장 훈련 완료","다음 모험: 팜팻의 변화 · 의뢰 지역 개방 필요",fs,visited?1:0,1));
    out.addAll(future);return out;
  }
  Row current(F5mAdaptedPrologueQuest q,GrowthQuest2 g,boolean forest){for(Row r:rows(q,g,forest))if(r.navigable())return r;return null;}
  int available(List<Row> rows){int n=0;for(Row r:rows)if(r.status==Status.AVAILABLE)n++;return n;}
  int attention(List<Row> rows){int n=0;for(Row r:rows)if(r.status==Status.AVAILABLE||r.status==Status.REPORT)n++;return n;}
}
