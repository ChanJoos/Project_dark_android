package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import com.projectdark.mobile.ui.TouchOwnership;
import com.projectdark.mobile.ui.GameUiController;
import com.projectdark.mobile.ui.PrototypeWorldRenderer;
import java.util.List;

/** PROJECT DARK v0.61 - integrated world/action/NPC/combat runtime prototype. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private enum Action { IDLE,WALK,CAST,SWING,THRUST,THROW,PUNCH,SKILL,KICK }

  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),pixel=new Paint();
  private final RuntimeState state=new RuntimeState();
  private final CombatController combat=new CombatController();
  private final InteractionController interaction=new InteractionController();
  private final TouchOwnership touchOwnership=new TouchOwnership();
  private final GameUiController ui=new GameUiController();
  private final PrototypeWorldRenderer worldRenderer=new PrototypeWorldRenderer();
  private float scale=1,ox,oy,vx,vy;
  private final float jx=92,jy=444,jr=60;
  private float knobX=jx,knobY=jy;
  private boolean joy,running;
  private long last,lastLedgerSequence=0;
  private int dir=0;
  private float walkClock=0,actionClock=0;
  private Action action=Action.IDLE;
  private String feedback="";
  private float feedbackClock=0;

  private final Runnable loop=new Runnable(){@Override public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;update(dt);invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);pixel.setFilterBitmap(false);setKeepScreenOn(true);}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);}
  public void pause(){running=false;removeCallbacks(loop);cancelInput();interaction.cancel();combat.cancelApproach();}

  private void update(float dt){
    feedbackClock=Math.max(0,feedbackClock-dt);if(ui.modal()||interaction.dialogOpen())return;combat.tick(dt);state.tick(dt);consumeLedger();updateMonsters(dt);
    if(!state.player().alive){action=Action.IDLE;interaction.cancel();combat.cancelApproach();joy=false;vx=vy=0;knobX=jx;knobY=jy;return;}
    if(isActing()){actionClock+=dt;if(actionClock>=duration(action)){actionClock=0;action=(joy&&(vx!=0||vy!=0))?Action.WALK:Action.IDLE;}return;}
    if(joy&&(vx!=0||vy!=0)){action=Action.WALK;state.tryMove(vx*145*dt,vy*145*dt);walkClock+=dt;interaction.cancelApproach();combat.cancelApproach();return;}
    if(interaction.approaching()){updateNpcInteraction(dt);return;}
    if(combat.approaching()){updateCombatApproach(dt);return;}
    action=Action.IDLE;
  }

  private void consumeLedger(){
    List<CombatLedger.Event> events=state.ledger().snapshot();
    for(CombatLedger.Event e:events){
      if(e.sequence<=lastLedgerSequence)continue;
      lastLedgerSequence=e.sequence;
      switch(e.type){
        case MONSTER_DEFEATED:showFeedback("몬스터 격파 [B]");break;
        case PLAYER_HIT:showFeedback("-"+e.amount+" HP [B]");break;
        case PLAYER_DEFEATED:showFeedback("행동 불능 [B]");break;
        case PLAYER_REVIVED:showFeedback("부활 [B]");break;
        default:break;
      }
    }
  }

  private void updateNpcInteraction(float dt){
    InteractionController.TickResult result=interaction.tick(state,dt);
    vx=interaction.moveX();vy=interaction.moveY();
    if(vx!=0||vy!=0)setFacingDirection(vx,vy);
    if(result==InteractionController.TickResult.WALKING){action=Action.WALK;walkClock+=dt;return;}
    action=Action.IDLE;
    String message=interaction.consumeFeedback();
    if(message!=null)showFeedback(message);
  }

  private void updateCombatApproach(float dt){
    RuntimeState.Monster approach=combat.approachTarget();
    if(approach==null||!approach.alive){combat.cancelApproach();return;}
    float d=state.distanceTo(approach),range=combat.intentRange();
    if(d<=range){
      CombatController.Intent ready=combat.consumeReadyIntent();action=Action.IDLE;
      if(ready==CombatController.Intent.ATTACK)attack();
      else if(ready==CombatController.Intent.CAST)cast();
      else if(ready==CombatController.Intent.SKILL)skill();
      else if(ready==CombatController.Intent.KICK)kick();
      return;
    }
    float dx=approach.x-state.player().x,dy=approach.y-state.player().y;setAutoDirection(dx,dy);action=Action.WALK;
    boolean moved=state.tryMove(vx*105*dt,vy*105*dt);walkClock+=dt;
    if(!moved){combat.cancelApproach();action=Action.IDLE;showFeedback("사거리 접근 실패 [B]");}
  }
  private void beginCombatApproach(CombatController.Intent intent){if(combat.beginApproach(intent))showFeedback("타깃 접근 [ADAPTED]");}

  private void updateMonsters(float dt){
    if(!state.player().alive)return;
    for(RuntimeState.Monster m:state.monsters()){
      if(!m.alive)continue;
      float dx=state.player().x-m.x,dy=state.player().y-m.y,d=(float)Math.sqrt(dx*dx+dy*dy);
      if(m.attackPrimed){if(d>48f){state.cancelMonsterAttack(m);continue;}if(state.monsterAttackReady(m)){state.resolveMonsterAttack(m,4,1.2f);}continue;}
      if(d<180f&&d>42f){float step=28f*dt;state.tryMoveMonster(m,Math.signum(dx)*step,Math.signum(dy)*step);}
      else if(d<=42f&&m.attackCooldown<=0f){state.beginMonsterAttack(m);}
    }
  }

  private void setFacingDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;if(sx<0&&sy>0)dir=0;else if(sx>0&&sy>0)dir=1;else if(sx<0)dir=2;else dir=3;}
  private void setAutoDirection(float dx,float dy){float sx=dx>=0?1f:-1f,sy=dy>=0?1f:-1f;vx=.707f*sx;vy=.707f*sy;setFacingDirection(dx,dy);}
  private void faceTarget(){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)setFacingDirection(t.x-state.player().x,t.y-state.player().y);}
  private boolean isActing(){return action!=Action.IDLE&&action!=Action.WALK;}
  private float duration(Action a){switch(a){case CAST:return .65f;case SWING:return .45f;case THRUST:return .38f;case THROW:return .52f;case PUNCH:return .32f;case SKILL:return .50f;case KICK:return .45f;default:return 0;}}
  private void trigger(Action a){if(isActing()||!state.player().alive)return;interaction.cancel();combat.cancelApproach();action=a;actionClock=0;}
  private void showFeedback(String s){feedback=s;feedbackClock=.8f;}
  private boolean consume(SkillDef def){if(state.player().mp<def.mpCost){showFeedback("MP 부족");return false;}state.player().mp-=def.mpCost;return true;}
  private boolean requireTarget(){if(combat.hasUsableTarget())return true;showFeedback("타깃 없음");return false;}
  private boolean inRange(float range){return combat.inRange(state,range);}
  private void applyToTarget(SkillDef def){RuntimeState.Monster t=combat.target();if(t!=null&&t.alive&&inRange(def.range))state.damage(t,def.damage);}

  /** [B] Offensive prototype cast requires a live target; no MP/cooldown is consumed before target/range validation. */
  private void cast(){
    if(!state.player().alive||isActing()||!combat.castReady()||!requireTarget())return;
    if(!inRange(SkillDef.CAST_PROTO.range)){beginCombatApproach(CombatController.Intent.CAST);return;}
    if(!consume(SkillDef.CAST_PROTO))return;faceTarget();combat.commitCast();trigger(Action.CAST);applyToTarget(SkillDef.CAST_PROTO);
  }
  private void skill(){
    if(!state.player().alive||isActing()||!combat.skillReady()||!requireTarget())return;
    if(!inRange(SkillDef.SKILL_PROTO.range)){beginCombatApproach(CombatController.Intent.SKILL);return;}
    if(!consume(SkillDef.SKILL_PROTO))return;faceTarget();combat.commitSkill();trigger(Action.SKILL);applyToTarget(SkillDef.SKILL_PROTO);
  }
  private void kick(){
    if(!state.player().alive||isActing()||!combat.kickReady()||!requireTarget())return;
    if(!inRange(SkillDef.KICK_PROTO.range)){beginCombatApproach(CombatController.Intent.KICK);return;}
    faceTarget();combat.commitKick();trigger(Action.KICK);applyToTarget(SkillDef.KICK_PROTO);
  }
  private Action actionFor(AttackDef.Kind k){switch(k){case THRUST:return Action.THRUST;case THROW:return Action.THROW;case PUNCH:return Action.PUNCH;default:return Action.SWING;}}
  private void attack(){
    if(isActing()||!combat.attackReady()||!state.player().alive||!requireTarget())return;
    AttackDef def=combat.attackDef();if(!inRange(def.range)){beginCombatApproach(CombatController.Intent.ATTACK);return;}
    faceTarget();combat.commitAttack();trigger(actionFor(def.kind));RuntimeState.Monster t=combat.target();if(t!=null&&t.alive)state.damage(t,def.damage);
  }

  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);worldRenderer.draw(c,state);drawDrops(c);drawNpcs(c);drawMonsters(c);drawCharacter(c);drawActionFx(c);drawHud(c);drawDialogue(c);drawInventory(c);drawDeath(c);c.restore();}

  private void drawNpcs(Canvas c){for(RuntimeState.Npc n:state.npcs()){p.setColor(0x66000000);c.drawOval(new RectF(n.x-11,n.y-3,n.x+11,n.y+4),p);p.setColor(0xffcab58b);c.drawCircle(n.x,n.y-39,7,p);p.setColor(0xff6c5946);c.drawRect(n.x-7,n.y-31,n.x+7,n.y-11,p);p.setColor(0xffb58f58);c.drawRect(n.x-5,n.y-12,n.x-1,n.y-3,p);c.drawRect(n.x+1,n.y-12,n.x+5,n.y-3,p);p.setTextSize(9);p.setColor(0xfff3e1ae);float tw=p.measureText(n.name);c.drawText(n.name,n.x-tw/2,n.y-52,p);if(interaction.approachNpc()==n){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xfff0d17a);c.drawCircle(n.x,n.y-24,19,p);p.setStyle(Paint.Style.FILL);}}}

  private void drawMonsters(Canvas c){RuntimeState.Monster selected=combat.target();for(RuntimeState.Monster m:state.monsters()){if(!m.alive)continue;p.setColor(0x66000000);c.drawOval(new RectF(m.x-15,m.y-4,m.x+15,m.y+5),p);p.setColor(m.hitFlash>0?0xffd9e8d7:0xff6a7c69);c.drawOval(new RectF(m.x-13,m.y-31,m.x+13,m.y-5),p);p.setColor(0xffd8c27b);c.drawCircle(m.x-5,m.y-20,2,p);c.drawCircle(m.x+5,m.y-20,2,p);if(m.attackPrimed){float q=1f-Math.min(1f,m.attackWindup/.24f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2+2*q);p.setColor(0xaaff7755);c.drawCircle(m.x,m.y-19,18+8*q,p);p.setStyle(Paint.Style.FILL);}bar(c,m.x-18,m.y-42,m.x+18,m.y-37,0xffd63442,m.hp/(float)m.maxHp);if(m.damagePopupClock>0){p.setTextSize(12);p.setColor(0xffffdc72);String d="-"+m.lastDamage;float tw=p.measureText(d);c.drawText(d,m.x-tw/2,m.y-50-(.65f-m.damagePopupClock)*20,p);}if(selected==m){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xffffd86b);c.drawOval(new RectF(m.x-21,m.y-9,m.x+21,m.y+9),p);p.setStyle(Paint.Style.FILL);}}}

  private void drawCharacter(Canvas c){float px=state.player().x,py=state.player().y,S=2f,x=px,y=py;int f=action==Action.WALK?((int)(walkClock*8f)%4):0;float bob=(f==1||f==3)?-1:0;p.setColor(state.player().hitFlash>0?0x99ff7766:0x66000000);c.drawOval(new RectF(x-13,y-4,x+13,y+4),p);c.save();c.translate(x,y-56+bob*S);c.scale(S,S);c.translate(-8,0);int skin=state.player().hitFlash>0?0xffffe0d0:0xffffc68f,hair=0xff3b251b,outline=0xff171313,shirt=state.player().hitFlash>0?0xffffd0ca:0xffeee5d3,blue=0xff3c6382,pants=0xff393a3a,shoe=0xff6a4526;int step=(f==1?1:f==3?-1:0);boolean left=(dir==0||dir==2),back=dir>=2;float phase=isActing()?Math.min(1f,actionClock/duration(action)):0;int armLift=action==Action.CAST?(phase<.25f?-5:-8):0;int kick=action==Action.KICK&&phase>.22f&&phase<.78f?5:0;rect(c,outline,4+step-kick,19,7+step,26);rect(c,outline,10-step,19,13-step+kick,26);rect(c,pants,5+step-kick,19,7+step,24);rect(c,pants,10-step,19,12-step+kick,24);rect(c,shoe,4+step-kick,24,7+step,27);rect(c,shoe,10-step,24,13-step+kick,27);rect(c,outline,3,10,14,21);rect(c,shirt,4,11,13,19);rect(c,blue,4,16,13,20);rect(c,0xffc9b070,7,11,9,20);int as=(f==1?1:f==3?-1:0);rect(c,outline,1,11+as+armLift,4,19+as);rect(c,skin,2,12+as+armLift,3,18+as);rect(c,outline,13,11-as+armLift,16,19-as);rect(c,skin,14,12-as+armLift,15,18-as);rect(c,outline,3,2,14,12);rect(c,skin,4,3,13,11);rect(c,hair,3,1,14,6);rect(c,hair,2,3,5,9);rect(c,hair,12,3,15,8);rect(c,hair,5,0,12,3);if(!back){int eye=0xff252020;if(left){rect(c,eye,5,7,6,8);rect(c,eye,9,7,10,8);}else{rect(c,eye,7,7,8,8);rect(c,eye,11,7,12,8);}}rect(c,0xff76503b,left?3:11,3,left?4:12,6);if(action==Action.SWING||action==Action.THRUST){p.setColor(0xffd7d2c5);p.setStrokeWidth(2);float ex=left?-7:23,ey=action==Action.THRUST?14:6;c.drawLine(left?2:15,14,ex,ey,p);}c.restore();}

  private void drawActionFx(Canvas c){if(!isActing())return;float px=state.player().x,py=state.player().y,q=Math.min(1f,actionClock/duration(action));if(action==Action.CAST){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(0xaa78b9ff);c.drawCircle(px,py-70,10+18*q,p);c.drawCircle(px,py-70,24-8*q,p);p.setStyle(Paint.Style.FILL);}else if(action==Action.THROW){p.setColor(0xffffd76b);float sx=(dir==0||dir==2)?-1:1,sy=(dir<2)?1:-1;c.drawCircle(px+sx*55*q,py-30+sy*35*q,4,p);}else if(action==Action.PUNCH||action==Action.KICK||action==Action.SKILL){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(action==Action.SKILL?5:3);p.setColor(action==Action.SKILL?0xaa7fffa8:0xaaffefb0);float sx=(dir==0||dir==2)?-1:1;c.drawArc(new RectF(px+sx*18-18,py-50,px+sx*18+18,py-14),20,130,false,p);p.setStyle(Paint.Style.FILL);}}

  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}private void panel(Canvas c,float l,float t,float r,float b){p.setColor(0x9a0b0b0b);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xaaad9364);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.FILL);}private void text(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setColor(0xffeee4cf);c.drawText(s,x,y,p);}private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){ratio=Math.max(0,Math.min(1,ratio));p.setColor(0xb51a1714);c.drawRoundRect(new RectF(l,t,r,b),5,5,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),5,5,p);}

  private void drawHud(Canvas c){RuntimeState.Monster selected=combat.target();RuntimeMetrics metrics=state.metrics();panel(c,12,12,165,116);text(c,"GROUP",24,31,12);text(c,"로컬 플레이 · 그룹 없음",24,53,10);panel(c,176,12,320,70);text(c,"퀘스트",190,32,13);text(c,"진행 중인 퀘스트 없음",190,52,10);panel(c,372,12,604,55);if(selected!=null){text(c,selected.name,405,29,11);bar(c,392,36,584,44,0xffe21d2e,selected.hp/(float)selected.maxHp);}else{text(c,"타깃 없음",456,31,11);}panel(c,714,12,902,124);text(c,"밀레스",728,31,11);p.setColor(0xaa090909);c.drawRect(728,38,887,104,p);text(c,"X:"+(int)state.player().x+" Y:"+(int)state.player().y,800,117,9);String[] menu={"≡","▣","Q","G","W","⚙"};for(int i=0;i<6;i++){panel(c,912,20+i*50,948,58+i*50);text(c,menu[i],925,45+i*50,15);}panel(c,12,304,272,382);text(c,"[일반] 밀레스",23,326,9);text(c,"타깃/접근/대화/전투 프로토타입",23,342,9);text(c,"격파 "+metrics.monsterDefeats()+" · 피격 "+metrics.playerHits()+" · DMG "+metrics.damageDealt(),23,358,9);if(feedbackClock>0)text(c,feedback,23,374,10);p.setColor(0x33101010);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0x99d8c49b);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.FILL);p.setColor(0x997e7057);c.drawCircle(knobX,knobY,24,p);panel(c,337,462,608,528);text(c,state.rpg().normalLevel()==null?"Lv --":"Lv "+state.rpg().normalLevel(),351,486,12);bar(c,400,474,594,485,0xffe62c42,state.player().hp/(float)state.player().maxHp);bar(c,400,490,594,501,0xff2388df,state.player().mp/(float)state.player().maxMp);bar(c,400,507,594,518,0xff48b84d,0f);String[] slots={"줍기","MAG","P","SK","K"};for(int i=0;i<5;i++)round(c,650+i*48,466,20,slots[i]);cooldown(c,698,466,20,combat.castCooldown(),SkillDef.CAST_PROTO.cooldown);cooldown(c,794,466,20,combat.skillCooldown(),SkillDef.SKILL_PROTO.cooldown);cooldown(c,842,466,20,combat.kickCooldown(),SkillDef.KICK_PROTO.cooldown);round(c,895,478,34,"ATK");cooldown(c,895,478,34,combat.attackCooldown(),combat.attackDef().cooldown);text(c,"AUTO 준비 중",870,518,10);text(c,combat.attackDef().label,840,530,8);}
  private void cooldown(Canvas c,float x,float y,float r,float remaining,float total){if(remaining<=0||total<=0)return;float ratio=Math.min(1f,remaining/total);p.setColor(0x99000000);c.drawArc(new RectF(x-r,y-r,x+r,y+r),-90,360*ratio,true,p);p.setTextSize(8);p.setColor(Color.WHITE);String s=String.format(java.util.Locale.US,"%.1f",remaining);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}
  private void drawDialogue(Canvas c){RuntimeState.Npc dialogNpc=interaction.dialogNpc();if(dialogNpc==null)return;panel(c,250,330,710,440);text(c,dialogNpc.name,270,355,13);drawWrappedText(c,dialogNpc.dialogue,270,380,420,12,18);text(c,"화면을 터치하면 닫기",555,425,9);}private void drawDeath(Canvas c){if(state.player().alive)return;p.setColor(0xaa000000);c.drawRect(0,0,W,H,p);panel(c,330,205,630,335);text(c,"행동 불능 [B]",421,240,18);text(c,"프로토타입 부활: 화면 중앙 터치",375,276,12);text(c,"원작 사망/패널티 규칙을 의미하지 않음",368,302,10);}private void drawWrappedText(Canvas c,String s,float x,float y,float maxWidth,float size,float lineHeight){p.setTextSize(size);p.setColor(0xffeee4cf);String[] words=s.split(" ");String line="";float yy=y;for(String word:words){String test=line.length()==0?word:line+" "+word;if(p.measureText(test)>maxWidth&&line.length()>0){c.drawText(line,x,yy,p);yy+=lineHeight;line=word;}else line=test;}if(line.length()>0)c.drawText(line,x,yy,p);}private void round(Canvas c,float x,float y,float r,String s){p.setColor(0xaa15120e);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xffc9a86c);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);p.setTextSize(s.length()>3?8:10);p.setColor(0xffffefd0);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}

  private void cancelInput(){touchOwnership.cancel();joy=false;vx=vy=0;knobX=jx;knobY=jy;}
  public boolean handleBack(){cancelInput();combat.cancelApproach();if(ui.back())return true;if(interaction.dialogOpen()){interaction.dismissDialog();return true;}interaction.cancel();return false;}
  @Override public boolean onTouchEvent(MotionEvent e){
    int masked=e.getActionMasked(),index=e.getActionIndex();
    if(masked==MotionEvent.ACTION_CANCEL){cancelInput();interaction.cancelApproach();combat.cancelApproach();return true;}
    if(masked==MotionEvent.ACTION_UP||masked==MotionEvent.ACTION_POINTER_UP){
      if(touchOwnership.release(e.getPointerId(index)))cancelInput();
      if(masked==MotionEvent.ACTION_UP)cancelInput();
      return true;
    }
    if(masked==MotionEvent.ACTION_MOVE){
      int owner=e.findPointerIndex(touchOwnership.movementPointer());
      if(owner>=0&&joy&&!ui.modal()&&!interaction.dialogOpen())stick((e.getX(owner)-ox)/scale,(e.getY(owner)-oy)/scale);
      return true;
    }
    if(masked!=MotionEvent.ACTION_DOWN&&masked!=MotionEvent.ACTION_POINTER_DOWN)return true;
    if(masked==MotionEvent.ACTION_DOWN)cancelInput();
    float x=(e.getX(index)-ox)/scale,y=(e.getY(index)-oy)/scale;
    if(x<0||x>W||y<0||y>H)return true;
    if(!state.player().alive){cancelInput();if(dist(x,y,480,270)<=180){state.revivePlayer();combat.clearTarget();interaction.cancel();ui.back();action=Action.IDLE;}return true;}
    if(ui.modal()){
      cancelInput();
      if(x>=680&&x<=720&&y>=135&&y<=175)ui.back();
      else if(x>=250&&x<=710&&y>=198&&y<408)showFeedback(ui.equipRow(state,(int)((y-198)/30)));
      return true;
    }
    if(interaction.dialogOpen()){cancelInput();interaction.dismissDialog();return true;}
    // Controls own touch-down before any world hit test.
    if(x>=912&&y>=70&&y<=108){cancelInput();interaction.cancel();combat.cancelApproach();ui.openInventory();return true;}
    if(dist(x,y,jx,jy)<=jr*1.2f){
      if(touchOwnership.acquireMovement(e.getPointerId(index))){joy=true;interaction.cancelApproach();combat.cancelApproach();stick(x,y);}return true;
    }
    if(dist(x,y,650,466)<=23){showFeedback(ui.pickup(state));return true;}
    if(dist(x,y,698,466)<=23){cast();return true;}
    if(dist(x,y,746,466)<=23){combat.cycleAttackMode();showFeedback(combat.attackDef().label+" [B]");return true;}
    if(dist(x,y,794,466)<=23){skill();return true;}
    if(dist(x,y,842,466)<=23){kick();return true;}
    if(dist(x,y,895,478)<=36){attack();return true;}
    if(GameUiController.hudBlocksWorld(x,y))return true;
    RuntimeState.Npc npc=state.hitNpc(x,y+24,28f);
    if(npc!=null){cancelInput();combat.cancelApproach();interaction.request(state,npc);return true;}
    RuntimeState.Monster monster=state.hitMonster(x,y+18,28f);
    if(monster!=null){combat.selectTarget(monster);interaction.cancelApproach();return true;}
    return true;
  }
  private void drawDrops(Canvas c){
    for(RpgProgressionState.WorldDrop d:state.rpg().worldDrops()){
      p.setColor(0xffd8bc70);c.drawRect(d.x-5,d.y-5,d.x+5,d.y+5,p);
      RpgProgressionState.ItemDefinition item=state.rpg().itemDefinitions().get(d.itemId);
      text(c,item==null?d.itemId:item.name,d.x-25,d.y-10,10);
    }
  }
  private void drawInventory(Canvas c){
    if(!ui.modal())return;
    p.setColor(0x99000000);c.drawRect(0,0,W,H,p);panel(c,240,135,720,438);
    text(c,"가방",260,168,18);text(c,"닫기",682,161,11);
    int row=0;
    for(java.util.Map.Entry<String,Integer> e:state.rpg().inventory().entrySet()){
      if(row>=7)break;
      RpgProgressionState.ItemDefinition item=state.rpg().itemDefinitions().get(e.getKey());
      boolean equipped=state.rpg().equipment().containsValue(e.getKey());
      text(c,(item==null?e.getKey():item.name)+" ×"+e.getValue()+(equipped?" · 장착 중":""),263,220+row*30,13);row++;
    }
    if(row==0)text(c,"아직 아이템이 없습니다",263,220,14);
    text(c,"아이템 터치: 장착 시도 / 바닥 전리품: 가까이에서 줍기",260,421,11);
    if(feedbackClock>0)text(c,feedback,263,396,11);
  }
  private void stick(float x,float y){if(isActing()||!state.player().alive)return;float dx=x-jx,dy=y-jy,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>jr){dx=dx/len*jr;dy=dy/len*jr;len=jr;}knobX=jx+dx;knobY=jy+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;dir=1;}else{vx=-.707f;vy=-.707f;dir=2;}}else{if(ny>0){vx=-.707f;vy=.707f;dir=0;}else{vx=.707f;vy=-.707f;dir=3;}}}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
