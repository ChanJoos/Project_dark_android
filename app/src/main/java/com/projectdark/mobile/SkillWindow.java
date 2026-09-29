package com.projectdark.mobile;

import android.graphics.*;
import java.util.*;

/** Source-backed skill browser; combat, acquisition and saving stay in their own services. */
final class SkillWindow {
  interface Actions {void use(SkillBook.Entry entry);boolean canLearn(SkillBook.Entry entry);void learn(SkillBook.Entry entry);void save();float cooldown(String id);String requirements(SkillBook.Entry entry);void notice(String text);}
  boolean open,magic,learnedOnly,choosingSlot;
  int page,detailPage,detailOffset;
  String selectedId,job="전체";
  private static final String[] JOBS={"전체","전사","도적","무도가","마법사","성직자"};
  private final SkillBook book;
  private final SkillIconCatalog icons;
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF bounds=new RectF(296,40,936,512);
  SkillWindow(SkillBook book,SkillIconCatalog icons){this.book=book;this.icons=icons;}
  void close(){open=false;choosingSlot=false;}
  private SkillBook.Entry selected(){return book.get(selectedId);}
  private List<SkillBook.Entry> rows(){List<SkillBook.Entry> out=new ArrayList<>();for(SkillBook.Entry e:book.list(magic,learnedOnly))if("전체".equals(job)||job.equals(e.job))out.add(e);return out;}
  private void reset(){page=0;selectedId=null;detailPage=detailOffset=0;choosingSlot=false;}
  void draw(Canvas c,Actions a){
    if(!open)return;
    p.setColor(0x44000000);c.drawRect(0,0,960,540,p);
    panel(c,296,40,936,512,0xff201810,0xff876c41);
    gradient(c,304,48,928,87,0xff5e4728,0xff2c2318);
    rule(c,310,86,922,86,0xffc5a36c);label(c,"기술과 마법",324,74,19,0xffffe6b1,true);
    label(c,"SKILL BOOK",493,71,9,0xffbea681,false);
    button(c,894,51,923,80,"×",true);
    tab(c,312,95,463,124,"기술",!magic);tab(c,470,95,621,124,"마법",magic);
    tab(c,638,95,774,124,"전체 목록",!learnedOnly);tab(c,781,95,918,124,"습득 목록",learnedOnly);
    for(int i=0;i<JOBS.length;i++){float x=312+i*102;tab(c,x,132,x+96,155,JOBS[i],job.equals(JOBS[i]));}
    List<SkillBook.Entry> list=rows();int pages=Math.max(1,(list.size()+15)/16);page=Math.max(0,Math.min(page,pages-1));
    for(int i=0;i<16&&!list.isEmpty();i++){
      float x=312+(i%4)*78,y=164+(i/4)*62;int n=page*16+i;
      gradient(c,x,y,x+72,y+57,0xff382b1c,0xff251e16);outline(c,x,y,x+72,y+57,0xff645034,1);
      if(n>=list.size())continue;SkillBook.Entry e=list.get(n);boolean active=e.id.equals(selectedId);
      if(active){gradient(c,x+1,y+1,x+71,y+56,0xff68532f,0xff3d301e);outline(c,x,y,x+72,y+57,0xffffd588,2);}
      boolean art=icons.draw(c,e.id,new RectF(x+19,y+3,x+53,y+37));
      if(!art){List<String> name=wrap(e.name,64,11);for(int line=0;line<Math.min(2,name.size());line++)center(c,name.get(line),x+36,y+23+line*14,11,0xffead4ae);center(c,e.job,x+36,y+51,8,0xffaa936e);}else fitted(c,e.name,x+3,y+51,66,10,0xfff1dec0);
      if(book.learned(e.id)){p.setColor(0xffb6ca85);c.drawCircle(x+64,y+8,2.5f,p);}
      float cd=e.runtime==null?0:a.cooldown(e.id);if(cd>0){p.setColor(0xb0000000);c.drawRect(x+17,y+2,x+55,y+38,p);center(c,String.format(Locale.ROOT,"%.1f",cd),x+36,y+25,13,Color.WHITE);}
    }
    if(list.isEmpty()){
      panel(c,312,164,621,408,0xff211d15,0xff615038);
      center(c,learnedOnly?"아직 배운 "+(magic?"마법":"기술")+"이 없습니다":"이 직업의 목록이 없습니다",462,258,15,0xffe4d2b1);
      center(c,learnedOnly?"전체 목록에서 효과와 습득 조건을 확인하세요":"다른 직업 또는 기술·마법 탭을 선택하세요",462,284,10,0xffc0aa85);
    }
    panel(c,638,164,918,408,0xff191711,0xff766040);
    SkillBook.Entry e=selected();
    if(e==null){center(c,list.isEmpty()?"습득한 목록이 이곳에 표시됩니다":magic?"마법을 선택하세요":"기술을 선택하세요",778,258,list.isEmpty()?12:16,0xffedd9b4);center(c,"효과 · 쿨타임 · 습득 조건",778,284,11,0xffb7a080);}
    else{
      boolean sourceIcon=icons.draw(c,e.id,new RectF(650,176,699,225));float nameLeft=sourceIcon?711:651;
      fitted(c,e.name,nameLeft,194,903-nameLeft,17,0xffffdf9f);
      fitted(c,e.job+" · "+e.kind+(e.circle.isEmpty()?"":" · "+e.circle+"서클"),nameLeft,214,903-nameLeft,10,0xffcbb58f);
      label(c,book.learned(e.id)?"습득 · 숙련도 "+book.proficiency(e.id)+"%":"미습득",651,240,10,book.learned(e.id)?0xffbcce87:0xffd0b486,false);
      tab(c,648,249,770,274,"효과 · 쿨타임",detailPage==0);tab(c,778,249,908,274,"습득 조건",detailPage==1);
      String body=detailPage==1?a.requirements(e):description(e,a);
      List<String> lines=wrap(body,248,11);detailOffset=Math.max(0,Math.min(detailOffset,Math.max(0,lines.size()-6)));int shown=Math.min(lines.size()-detailOffset,6);
      c.save();c.clipRect(648,279,908,376);for(int i=0;i<shown;i++)label(c,lines.get(detailOffset+i),650,293+i*15,11,0xffe0d0b1,false);c.restore();
      if(lines.size()>6){label(c,(detailOffset+1)+"–"+(detailOffset+shown)+" / "+lines.size(),651,394,9,0xffddbd7f,false);button(c,827,381,861,402,"↑",detailOffset>0);button(c,872,381,906,402,"↓",detailOffset+6<lines.size());}
    }
    button(c,312,418,351,447,"‹",page>0);center(c,(page+1)+" / "+pages+"    "+list.size()+"개",466,438,11,0xffe9d4ad);button(c,581,418,621,447,"›",page+1<pages);
    button(c,638,418,772,447,e!=null&&book.learned(e.id)?"습득 완료":"습득",e!=null&&!book.learned(e.id)&&a.canLearn(e));button(c,780,418,918,447,choosingSlot?"등록 취소":"퀵슬롯 등록",e!=null&&book.learned(e.id));
    label(c,choosingSlot?"등록할 슬롯을 선택하세요 · 같은 기술을 선택하면 해제":"퀵슬롯 · 배운 기술을 등록하고 전투 화면에서 사용하세요",313,463,9,0xffcdb386,false);
    for(int i=0;i<8;i++){float x=312+i*76;gradient(c,x,469,x+70,503,0xff3b2d1e,0xff211b14);outline(c,x,469,x+70,503,choosingSlot?0xffe0bd79:0xff78603f,1);label(c,""+(i+1),x+4,481,8,0xffb39c76,false);SkillBook.Entry s=book.get(book.slot(i));if(s!=null){icons.draw(c,s.id,new RectF(x+24,472,x+46,494));fitted(c,s.name,x+13,501,53,7,0xffedd6ae);}else center(c,"—",x+38,493,12,0xff74644d);}
  }
  boolean touch(float x,float y,Actions a){
    if(!open)return false;
    if(x>=889&&x<=936&&y>=40&&y<=88){close();return true;}
    if(!bounds.contains(x,y))return true;
    if(y>=95&&y<=124){if(x>=312&&x<=621){magic=x>=470;reset();}else if(x>=638&&x<=918){learnedOnly=x>=781;reset();}return true;}
    if(y>=132&&y<=155&&x>=312&&x<924){int i=(int)((x-312)/102);if(i<JOBS.length){job=JOBS[i];reset();}return true;}
    if(x>=312&&x<624&&y>=164&&y<412){int col=(int)((x-312)/78),row=(int)((y-164)/62);if((x-312)%78<=72&&(y-164)%62<=57){List<SkillBook.Entry> list=rows();int i=page*16+row*4+col;if(i<list.size()){selectedId=list.get(i).id;detailPage=detailOffset=0;choosingSlot=false;}}return true;}
    if(x>=648&&x<=910&&y>=249&&y<=274){detailPage=x>=778?1:0;detailOffset=0;return true;}
    if(x>=638&&x<=918&&y>=279&&y<=408){if(y>=381){if(x>=827&&x<=861)detailOffset=Math.max(0,detailOffset-1);else if(x>=872&&x<=906)detailOffset++;}return true;}
    if(y>=418&&y<=447){if(x>=312&&x<=351){page=Math.max(0,page-1);selectedId=null;}else if(x>=581&&x<=621){page=Math.min(page+1,Math.max(0,(rows().size()-1)/16));selectedId=null;}else if(x>=638&&x<=772){if(selected()!=null&&!book.learned(selectedId))a.learn(selected());}else if(x>=780&&x<=918){if(selected()!=null&&book.learned(selectedId))choosingSlot=!choosingSlot;else a.notice("먼저 기술을 습득하세요");}return true;}
    if(y>=469&&y<=503&&x>=312&&x<920){int i=(int)((x-312)/76);if(i<8){if(choosingSlot&&selected()!=null){if(selectedId.equals(book.slot(i)))book.clearSlot(i);else if(!book.assign(i,selectedId))return true;a.save();choosingSlot=false;a.notice(book.slot(i)==null?"퀵슬롯 해제":"퀵슬롯 "+(i+1)+" 등록");}else{SkillBook.Entry e=book.get(book.slot(i));if(e!=null){selectedId=e.id;detailOffset=detailPage=0;}}}return true;}return true;
  }
  void drawSlot(Canvas c,RectF r,int index,Actions a){SkillBook.Entry e=book.get(book.slot(index));if(e==null)return;RectF art=new RectF(r.left+4,r.top+3,r.right-4,r.bottom-11);if(!icons.draw(c,e.id,art))fitted(c,e.name,r.left+3,r.centerY()+3,r.width()-6,8,0xffefd1a1);else fitted(c,e.name,r.left+2,r.bottom-2,r.width()-4,7,0xffefd1a1);float cd=a.cooldown(e.id);if(cd>0){p.setColor(0xb9000000);c.drawRect(r,p);center(c,String.format(Locale.ROOT,"%.1f",cd),r.centerX(),r.centerY()+4,11,Color.WHITE);}}
  private String description(SkillBook.Entry e,Actions a){
    if("SK_공통_001".equals(e.id))return "장착 무기로 기본공격\n소모  MP 0\n공격 주기  장착 무기 기준\n기본공격 버튼과 재사용 시간을 공유합니다";
    if(e.runtime==null)return clean(e.effect)+"\n대상  "+e.target+"\n쿨타임  확인 중\n전투 효과 준비 중";
    SkillDef d=e.runtime;String effect=d.targetPolicy==SkillDef.TargetPolicy.SELF?"자신의 HP를 "+d.damage+" + WIS만큼 회복":(e.magic()?"단일 마법 공격":"단일 근접 공격")+" · 기본 피해 "+d.damage;
    float remaining=a.cooldown(e.id);
    return effect+"\n소모  MP "+d.mpCost+"\n쿨타임  "+String.format(Locale.ROOT,"%.1f초",d.cooldown)+(remaining>0?" · 남은 "+String.format(Locale.ROOT,"%.1f초",remaining):"")+"\n수치는 현재 게임의 시험값입니다";
  }
  private String clean(String text){if(text.contains("미확정")||text.contains("확인 필요"))return "효과 확인 중";return text.split("[;；]")[0].replace("자료상 ","").replace("자료 존재","");}
  private void label(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setStyle(Paint.Style.FILL);p.setTextSize(size);p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setColor(color);c.drawText(s,x,y,p);}
  private void center(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);label(c,s,x-p.measureText(s)/2,y,size,color,false);}
  private void fitted(Canvas c,String s,float x,float y,float width,float size,int color){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);if(p.measureText(s)>width){int n=p.breakText(s,true,width-p.measureText("…"),null);s=s.substring(0,Math.max(0,n))+"…";}label(c,s,x,y,size,color,false);}
  private void gradient(Canvas c,float l,float t,float r,float b,int top,int bottom){p.setShader(new LinearGradient(l,t,l,b,top,bottom,Shader.TileMode.CLAMP));c.drawRect(l,t,r,b,p);p.setShader(null);}
  private void outline(Canvas c,float l,float t,float r,float b,int color,float width){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setColor(color);c.drawRect(l,t,r,b,p);p.setStyle(Paint.Style.FILL);}
  private void rule(Canvas c,float l,float t,float r,float b,int color){p.setColor(color);p.setStrokeWidth(1);c.drawLine(l,t,r,b,p);}
  private void panel(Canvas c,float l,float t,float r,float b,int fill,int edge){p.setColor(0xff0d0c0a);c.drawRect(l-4,t-4,r+4,b+4,p);p.setColor(fill);c.drawRect(l,t,r,b,p);outline(c,l,t,r,b,edge,2);outline(c,l+4,t+4,r-4,b-4,0xff4e3d27,1);for(float x:new float[]{l+5,r-5})for(float y:new float[]{t+5,b-5}){p.setColor(0xffb09461);c.drawCircle(x,y,1.5f,p);}}
  private void tab(Canvas c,float l,float t,float r,float b,String s,boolean active){gradient(c,l,t,r,b,active?0xff79603a:0xff332b1e,active?0xff473821:0xff231e16);outline(c,l,t,r,b,active?0xffd2b17b:0xff645236,1);center(c,s,(l+r)/2,(t+b)/2+4,11,active?0xffffe3ad:0xffc0ad8d);if(active)rule(c,l+8,b-2,r-8,b-2,0xffe9ca87);}
  private void button(Canvas c,float l,float t,float r,float b,String s,boolean active){gradient(c,l,t,r,b,active?0xff735832:0xff302a20,active?0xff42321e:0xff242018);outline(c,l,t,r,b,active?0xffc7a168:0xff63533c,1);center(c,s,(l+r)/2,(t+b)/2+4,12,active?0xffffe4af:0xffaa977a);}
  private List<String> wrap(String s,float width,float size){p.setTypeface(Typeface.DEFAULT);p.setTextSize(size);List<String> out=new ArrayList<>();for(String paragraph:s.split("\n",-1)){String rest=paragraph;do{int n=p.breakText(rest,true,width,null);if(n==0&&!rest.isEmpty())n=1;out.add(rest.substring(0,n));rest=rest.substring(n);}while(!rest.isEmpty());}return out;}
}
