package com.projectdark.mobile;
/** World/NPC interaction orchestration. [ADAPTED] tap-to-approach mobile convenience. */
public final class InteractionController{
 public enum TickResult{IDLE,WALKING,DIALOG_OPENED,BLOCKED} private static final float INTERACTION_RANGE=56f,APPROACH_SPEED=92f,BLOCK_TIMEOUT=2f,DIAGONAL=.70710677f;
 private RuntimeState.Npc approachNpc,dialogNpc;private float blockedClock,moveX,moveY;private String feedback;
 public RuntimeState.Npc approachNpc(){return approachNpc;}public RuntimeState.Npc dialogNpc(){return dialogNpc;}public boolean approaching(){return approachNpc!=null;}public boolean dialogOpen(){return dialogNpc!=null;}public float moveX(){return moveX;}public float moveY(){return moveY;}
 public TickResult request(RuntimeState state,RuntimeState.Npc npc){if(state==null||npc==null||!state.player().alive)return TickResult.IDLE;feedback=null;blockedClock=0f;if(state.distanceTo(npc)<=INTERACTION_RANGE){dialogNpc=npc;approachNpc=null;moveX=moveY=0f;completeF5mTurnInIfReady(state,npc);if(feedback==null)feedback="대화 시작 · "+dialogNpc.name;return TickResult.DIALOG_OPENED;}dialogNpc=null;approachNpc=npc;updateDirection(state);return TickResult.WALKING;}
 private void completeF5mTurnInIfReady(RuntimeState state,RuntimeState.Npc npc){
  if(!"milles_guide_proto".equals(npc.id))return;
  F5mAdaptedPrologueQuest q=F5mAdaptedPrologueQuest.activeOpening();if(q==null)return;
  F5mSaveStore.restoreRewardsActive(state.rpg());
  if(q.state()==F5mAdaptedPrologueQuest.State.COMPLETED){feedback=F5mSaveStore.rewardClaimedActive()?"퀘스트 완료 · 보상 수령 완료":"퀘스트 완료 · 보상 상태 확인 필요";return;}
  if(q.state()!=F5mAdaptedPrologueQuest.State.RETURN_READY)return;
  RpgProgressionState.AutoLootResult reward=state.rpg().autoLootResolvedItem(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,1);
  if(reward!=RpgProgressionState.AutoLootResult.LOOTED){feedback="보상 지급 실패 · 퀘스트는 완료 처리되지 않음";return;}
  if(F5mSaveStore.commitTurnInActive(q,state.rpg())){feedback="퀘스트 완료 · 훈련 증표 [B] x1";return;}
  feedback="보상 저장 실패 · 재시도 필요";
 }
 public TickResult tick(RuntimeState state,float dt){if(state==null||!state.player().alive){cancel();return TickResult.IDLE;}if(approachNpc==null)return TickResult.IDLE;if(state.distanceTo(approachNpc)<=INTERACTION_RANGE){dialogNpc=approachNpc;approachNpc=null;blockedClock=0f;moveX=moveY=0f;completeF5mTurnInIfReady(state,dialogNpc);if(feedback==null)feedback="대화 시작 · "+dialogNpc.name;return TickResult.DIALOG_OPENED;}updateDirection(state);float dx=moveX*APPROACH_SPEED*dt,dy=moveY*APPROACH_SPEED*dt;if(state.tryMove(dx,dy)){blockedClock=0f;return TickResult.WALKING;}blockedClock+=dt;boolean moved=state.tryMove(dx,-dy);if(!moved)moved=state.tryMove(-dx,dy);if(moved){blockedClock=Math.max(0f,blockedClock-.12f);return TickResult.WALKING;}if(blockedClock>BLOCK_TIMEOUT){RuntimeState.Npc blockedNpc=approachNpc;approachNpc=null;blockedClock=0f;moveX=moveY=0f;feedback=blockedNpc==null?"NPC 접근 실패 [B]":blockedNpc.name+" 접근 실패 · 직접 이동 후 다시 탭 [B]";return TickResult.BLOCKED;}return TickResult.WALKING;}
 public void dismissDialog(){dialogNpc=null;}public void cancelApproach(){approachNpc=null;blockedClock=0f;moveX=moveY=0f;}public void cancel(){cancelApproach();dialogNpc=null;feedback=null;}public String consumeFeedback(){String out=feedback;feedback=null;return out;}
 private void updateDirection(RuntimeState state){float dx=approachNpc.x-state.player().x,dy=approachNpc.y-state.player().y;moveX=dx>=0?DIAGONAL:-DIAGONAL;moveY=dy>=0?DIAGONAL:-DIAGONAL;}
}
