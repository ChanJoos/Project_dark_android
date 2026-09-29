package com.projectdark.mobile;

import android.graphics.*;
import java.util.List;

/** Modal input projection; never owns world ticking, combat rules or save writes. */
final class SkillWindow {
  interface Actions { void use(SkillBook.Entry entry); void save(); float cooldown(String id); String requirements(SkillBook.Entry entry); void notice(String text); }
  boolean open,magic,learnedOnly,choosingSlot;
  int page,detailPage;
  String selectedId;
  private final SkillBook book;
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  SkillWindow(SkillBook book){this.book=book;}
  void close(){open=false;choosingSlot=false;}
  private SkillBook.Entry selected(){return book.get(selectedId);}
  void draw(Canvas c,Actions a){
    if(!open)return;
    button(c,440,100,555,132,magic?"기술":"기술 ✓",!magic);
    button(c,560,100,675,132,magic?"마법 ✓":"마법",magic);
    button(c,716,100,918,132,learnedOnly?"배운 스킬 · 전체 보기":"전체 · 배운 스킬 보기",true);
    List<SkillBook.Entry> rows=book.list(magic,learnedOnly);int pages=Math.max(1,(rows.size()+15)/16);page=Math.max(0,Math.min(page,pages-1));
    for(int i=0;i<16;i++){
      float x=440+(i%4)*67,y=142+(i/4)*67;box(c,x,y,x+62,y+62,0xff191411,0xff765335);
      int index=page*16+i;if(index>=rows.size())continue;SkillBook.Entry e=rows.get(index);
      if(e.id.equals(selectedId))box(c,x+1,y+1,x+61,y+61,0x00222222,0xffffce70);
      label(c,e.magic()?"魔":"技",x+20,y+28,19,book.learned(e.id)?0xffffd78c:0xff938578);
      fitted(c,e.name,x+4,y+45,54,8,book.learned(e.id)?0xfff3dfbc:0xffafa498);
      label(c,book.learned(e.id)?"습득":"미습득",x+17,y+57,6,0xffad9e84);
      float cd=e.runtime==null?0:a.cooldown(e.id);if(cd>0){p.setColor(0xa9000000);c.drawRect(x,y,x+62,y+62,p);label(c,String.format(java.util.Locale.ROOT,"%.1f",cd),x+18,y+34,14,Color.WHITE);}
    }
    if(rows.isEmpty()){label(c,"배운 스킬이 없습니다",453,266,12,0xffe4cda9);label(c,"상단에서 전체 목록을 볼 수 있습니다",445,292,8,0xffb6a48b);}
    box(c,716,142,918,405,0xff161210,0xff8b6039);
    SkillBook.Entry e=selected();
    if(e==null){label(c,"스킬을 선택하세요",752,260,11,0xffd8c8b0);}else{
      fitted(c,e.name,728,164,178,13,0xffffd78c);
      fitted(c,e.job+" · "+e.stage+" · "+e.kind+(e.circle.isEmpty()?"":" · "+e.circle+"서클"),728,182,178,8,0xffcdb89b);
      String status=book.learned(e.id)?"습득 · 숙련도 "+book.proficiency(e.id)+"%":"미습득";
      label(c,status+(e.runtime==null?" · 구현 예정":" · B 시험 동작"),728,201,8,0xffcdb89b);
      String body=detailPage==0?e.effect+"\n대상: "+e.target+"\n범위: "+e.range+"\n소모: "+e.resource+"\n쿨타임: "+(e.runtime==null?"미확정":e.runtime.cooldown+"초 (B)")+"\n제한: "+e.limit:
        detailPage==1?"습득 조건 · "+e.requirementStatus+"\n직업: "+e.job+"\n단계: "+e.stage+"\n"+a.requirements(e):
        "구 클라이언트 참고 조건\n"+(e.legacy.isEmpty()?"기록 없음":e.legacy)+"\n현재 게임에 자동 적용되지 않습니다.\n효과 근거: "+e.evidence;
      wrapped(c,body,728,222,178,9,13,378);
      label(c,new String[]{"효과","습득 조건","구 자료"}[detailPage]+" · 눌러서 다음 ("+(detailPage+1)+"/3)",728,395,7,0xffe0ad62);
    }
    button(c,440,416,500,448,"‹",true);label(c,(page+1)+" / "+pages+" · "+rows.size()+"개",519,437,9,0xffd8c8b0);button(c,647,416,707,448,"›",true);
    button(c,716,416,810,448,"사용",e!=null&&book.usable(e.id));button(c,816,416,918,448,choosingSlot?"등록 취소":"퀵슬롯 등록",e!=null&&book.usable(e.id));
    if(choosingSlot){label(c,"슬롯 선택 · 같은 스킬을 다시 선택하면 해제",444,463,8,0xffffd78c);for(int i=0;i<8;i++){float x=440+i*60;SkillBook.Entry s=book.get(book.slot(i));button(c,x,469,x+54,500,(i+1)+" "+(s==null?"빈칸":s.name),true);}}
    else label(c,"아이콘 선택 → 설명 확인 → 사용 / 등록",450,481,9,0xffcdb89b);
  }
  boolean touch(float x,float y,Actions a){
    if(!open)return false;
    if(x>=890&&x<=937&&y>=54&&y<=98){close();return true;}
    if(x<428||x>936||y<54||y>512)return true;
    if(choosingSlot&&y>=469&&y<=500&&x>=440&&x<920){int slot=(int)((x-440)/60);if(slot<8&&selected()!=null){if(selectedId.equals(book.slot(slot)))book.clearSlot(slot);else if(!book.assign(slot,selectedId)){a.notice("배운 스킬과 전투 구현 여부를 확인하세요");return true;}a.save();choosingSlot=false;}return true;}
    if(y>=100&&y<=132){if(x>=440&&x<=675){magic=x>=560;page=0;selectedId=null;detailPage=0;choosingSlot=false;}else if(x>=716&&x<=918){learnedOnly=!learnedOnly;page=0;selectedId=null;choosingSlot=false;}return true;}
    if(x>=440&&x<707&&y>=142&&y<405){int col=(int)((x-440)/67),row=(int)((y-142)/67);if((x-440)%67<=62&&(y-142)%67<=62){List<SkillBook.Entry> rows=book.list(magic,learnedOnly);int i=page*16+row*4+col;if(col<4&&row<4&&i<rows.size()){selectedId=rows.get(i).id;detailPage=0;choosingSlot=false;}}return true;}
    if(x>=716&&x<=918&&y>=205&&y<=405){detailPage=(detailPage+1)%3;return true;}
    if(y>=416&&y<=448){if(x>=440&&x<=500)page=Math.max(0,page-1);else if(x>=647&&x<=707)page++;else if(x>=716&&x<=810){SkillBook.Entry e=selected();if(e!=null&&book.usable(e.id))a.use(e);else a.notice("미습득 또는 구현 예정 스킬입니다");}else if(x>=816&&x<=918){if(selected()!=null&&book.usable(selectedId))choosingSlot=!choosingSlot;else a.notice("배운 스킬 중 사용 가능한 스킬을 등록하세요");}return true;}return true;
  }
  void drawSlot(Canvas c,RectF r,int index,Actions a){SkillBook.Entry e=book.get(book.slot(index));if(e==null)return;fitted(c,e.name,r.left+3,r.centerY()+3,r.width()-6,8,0xffffd78c);float cd=a.cooldown(e.id);if(cd>0){p.setColor(0xb9000000);c.drawRect(r,p);label(c,String.format(java.util.Locale.ROOT,"%.1f",cd),r.left+7,r.centerY()+4,10,Color.WHITE);}}
  private void label(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setTextSize(size);p.setColor(color);c.drawText(s,x,y,p);}
  private void fitted(Canvas c,String s,float x,float y,float w,float size,int color){p.setTextSize(size);while(s.length()>1&&p.measureText(s)>w)s=s.substring(0,s.length()-2)+"…";label(c,s,x,y,size,color);}
  private void box(Canvas c,float l,float t,float r,float b,int fill,int edge){p.setStyle(Paint.Style.FILL);p.setColor(fill);c.drawRect(l,t,r,b,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(edge);c.drawRect(l,t,r,b,p);p.setStyle(Paint.Style.FILL);}
  private void button(Canvas c,float l,float t,float r,float b,String s,boolean active){box(c,l,t,r,b,active?0xff624323:0xff30271e,active?0xffd0a066:0xff74624f);fitted(c,s,l+6,(t+b)/2+4,r-l-12,9,active?0xfff4dfbe:0xffa29684);}
  private void wrapped(Canvas c,String s,float x,float y,float width,float size,float line,float bottom){p.setTextSize(size);for(String paragraph:s.split("\n",-1)){String rest=paragraph;do{int n=p.breakText(rest,true,width,null);if(n==0&&rest.length()>0)n=1;if(y>bottom){label(c,"…",x,bottom,size,0xffcdb89b);return;}label(c,rest.substring(0,n),x,y,size,0xffcdb89b);rest=rest.substring(n);y+=line;}while(!rest.isEmpty());}}
}
