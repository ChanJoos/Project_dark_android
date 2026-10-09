package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

/** Mobile journal: scrollable list, persistent status filters, objective and contextual travel. */
final class QuestJournalWindow {
  enum Filter { ALL, AVAILABLE, ACTIVE, COMPLETE }
  enum Result { NONE, CLOSE, NAVIGATE }
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  Filter filter=Filter.ALL;
  String selected;
  int offset;
  boolean help,detailGesture;float detailScroll;
  private float downY,lastY;
  private boolean dragging,listGesture;
  private static final int VISIBLE=5;
  List<QuestJournalModel.Row> visible(List<QuestJournalModel.Row> rows){List<QuestJournalModel.Row> out=new ArrayList<>();for(QuestJournalModel.Row r:rows)if(filter==Filter.ALL||filter==Filter.AVAILABLE&&r.status==QuestJournalModel.Status.AVAILABLE||filter==Filter.ACTIVE&&(r.status==QuestJournalModel.Status.ACTIVE||r.status==QuestJournalModel.Status.REPORT)||filter==Filter.COMPLETE&&r.status==QuestJournalModel.Status.COMPLETE)out.add(r);return out;}
  void open(List<QuestJournalModel.Row> rows,QuestJournalModel.Row current){filter=Filter.ALL;selected=current==null?(rows.isEmpty()?null:rows.get(0).id):current.id;offset=0;for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(selected))offset=Math.max(0,i-1);help=false;detailScroll=0;dragging=false;listGesture=false;detailGesture=false;}
  QuestJournalModel.Row selected(List<QuestJournalModel.Row> rows){List<QuestJournalModel.Row> shown=visible(rows);for(QuestJournalModel.Row r:shown)if(r.id.equals(selected))return r;return shown.isEmpty()?null:shown.get(0);}
  private int color(QuestJournalModel.Row r){switch(r.status){case AVAILABLE:return UiTheme.ACCENT;case REPORT:return UiTheme.GOLD;case ACTIVE:return UiTheme.GOOD;default:return UiTheme.MUTED;}}
  void draw(Canvas c,List<QuestJournalModel.Row> rows,float shift){c.save();c.translate(shift,0);UiTheme.scrim(c);box(c,110,52,850,488,0xff211a14,0xffa78554);UiTheme.line(c,132,99,828,99,UiTheme.LINE);text(c,"퀘스트",134,83,22,UiTheme.TEXT,true);text(c,"목표와 보상을 확인하고 모험을 이어가세요",258,81,11,UiTheme.MUTED,false);UiTheme.close(c,818,78);
    String[] tabs={"전체","수락 가능","진행 중","완료"};for(int i=0;i<4;i++){float x=132+i*174;UiTheme.tab(c,new RectF(x,108,x+164,142),tabs[i]+"  "+count(rows,Filter.values()[i]),filter.ordinal()==i);}
    List<QuestJournalModel.Row> shown=visible(rows);offset=Math.max(0,Math.min(offset,Math.max(0,shown.size()-VISIBLE)));QuestJournalModel.Row chosen=selected(rows);
    for(int i=offset;i<Math.min(shown.size(),offset+VISIBLE);i++){QuestJournalModel.Row r=shown.get(i);float y=154+(i-offset)*54;boolean on=chosen!=null&&chosen.id.equals(r.id);box(c,132,y,400,y+48,on?0xff4a3826:0xff2b221a,on?0xffd5ae68:0xff55432e);UiTheme.surface(c,new RectF(141,y+10,145,y+38),color(r),0,2);fit(c,r.title,158,y+20,229,13,0xfff0e2c6,true);text(c,r.label(),158,y+38,10,color(r),true);fit(c,r.area,235,y+37,152,9,0xffbcae98,false);}
    if(shown.isEmpty()){text(c,"표시할 퀘스트가 없습니다",151,198,12,0xffc4b59d,false);text(c,"전체 탭에서 다음 모험을 확인하세요",151,222,10,0xffa99b84,false);}
    UiTheme.button(c,new RectF(132,432,211,466),"이전",offset>0,false);UiTheme.button(c,new RectF(321,432,400,466),"다음",offset+VISIBLE<shown.size(),false);text(c,shown.isEmpty()?"0 / 0":(offset+1)+"–"+Math.min(offset+VISIBLE,shown.size())+" / "+shown.size(),232,453,10,0xffac9a7d,false);
    box(c,420,154,828,410,0xff191611,0xff625039);
    if(chosen!=null){QuestJournalModel.Row r=chosen;text(c,r.label(),440,178,12,color(r),true);fit(c,r.title,440,203,369,19,UiTheme.TEXT,true);fit(c,r.area+"   ·   "+r.npc,440,226,369,11,UiTheme.MUTED,false);
      c.save();c.clipRect(430,234,820,406);
      if(help){text(c,"방법 안내 · 위아래로 스크롤",440,251,12,UiTheme.GOLD,true);c.save();c.clipRect(434,260,817,405);wrap(c,r.guide,440,279-detailScroll,365,13,19,40,UiTheme.TEXT);c.restore();}
      else {wrap(c,r.story,440,250,365,12,18,2,UiTheme.TEXT);text(c,"목표",440,297,11,UiTheme.MUTED,true);wrap(c,r.objective+(r.goal>0&&r.status==QuestJournalModel.Status.ACTIVE?"  "+r.count+" / "+r.goal:""),440,318,365,14,19,2,color(r));
        if(r.goal>0&&r.status==QuestJournalModel.Status.ACTIVE){UiTheme.surface(c,new RectF(440,346,805,350),UiTheme.RAISED,0,2);float ratio=Math.min(1,Math.max(0,r.count/(float)r.goal));if(ratio>0)UiTheme.surface(c,new RectF(440,346,440+365*ratio,350),UiTheme.ACCENT,0,2);}
        if(r.status==QuestJournalModel.Status.LOCKED)wrap(c,r.condition,440,362,365,12,17,2,UiTheme.MUTED);else{wrap(c,"보상  "+r.reward,440,362,365,11,16,2,UiTheme.GOLD);fit(c,r.next,440,402,365,10,UiTheme.MUTED,false);}}
      c.restore();UiTheme.button(c,new RectF(420,421,538,466),help?"목표 보기":"방법 보기",true,help);
      UiTheme.button(c,new RectF(550,421,810,466),r.action(),r.navigable(),r.navigable());
    }
    c.restore();}
  private int count(List<QuestJournalModel.Row> rows,Filter f){Filter old=filter;filter=f;int n=visible(rows).size();filter=old;return n;}
  Result motion(int action,float x,float y,List<QuestJournalModel.Row> rows,float shift){x-=shift;List<QuestJournalModel.Row> shown=visible(rows);
    if(action==MotionEvent.ACTION_DOWN){downY=lastY=y;dragging=false;detailGesture=help&&x>=420&&x<=828&&y>=260&&y<=405;listGesture=x>=132&&x<=400&&y>=154&&y<=420;
      if(x>=794&&x<=838&&y>=58&&y<=98)return Result.CLOSE;
      if(y>=108&&y<=142&&x>=132&&x<828){int i=(int)((x-132)/174);if(i<4){filter=Filter.values()[i];offset=0;selected=null;}return Result.NONE;}
      if(y>=432&&y<=466&&x>=132&&x<=400){if(x<=211)offset=Math.max(0,offset-VISIBLE);if(x>=321)offset=Math.min(Math.max(0,shown.size()-VISIBLE),offset+VISIBLE);return Result.NONE;}
      if(x>=420&&x<=538&&y>=421&&y<=466){help=!help;detailScroll=0;return Result.NONE;}
      QuestJournalModel.Row chosen=selected(rows);if(x>=550&&x<=810&&y>=421&&y<=466&&chosen!=null&&chosen.navigable())return Result.NAVIGATE;
    }else if(action==MotionEvent.ACTION_MOVE&&detailGesture){QuestJournalModel.Row r=selected(rows);if(r!=null){p.setTextSize(13);int lines=1;String line="";for(char ch:r.guide.toCharArray()){if(ch=='\n'||p.measureText(line+ch)>365){lines++;line="";if(ch=='\n')continue;}line+=ch;}detailScroll=Math.max(0,Math.min(Math.max(0,lines*19-126),detailScroll+lastY-y));lastY=y;}}else if(action==MotionEvent.ACTION_MOVE&&listGesture){if(Math.abs(y-downY)>12)dragging=true;if(dragging&&Math.abs(y-lastY)>=28){offset=Math.max(0,Math.min(Math.max(0,shown.size()-VISIBLE),offset+(y<lastY?1:-1)));lastY=y;}}
    else if(action==MotionEvent.ACTION_UP){if(listGesture&&!dragging&&Math.abs(y-downY)<12){int i=offset+(int)((downY-154)/54);if(i>=0&&i<shown.size()&&downY>=154){selected=shown.get(i).id;help=false;detailScroll=0;}}listGesture=false;detailGesture=false;}
    else if(action==MotionEvent.ACTION_CANCEL){dragging=false;listGesture=false;detailGesture=false;}
    return Result.NONE;
  }
  private void box(Canvas c,float l,float t,float r,float b,int fill,int border){boolean selected=fill==0xff705232||fill==0xff4a3826||fill==0xff735332;UiTheme.surface(c,new RectF(l,t,r,b),selected?UiTheme.RAISED:fill==0xff211a14?UiTheme.BG:UiTheme.SURFACE,selected?0xff597882:UiTheme.LINE,8);}
  private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setTypeface(UiTheme.font(bold));p.setTextSize(size);p.setColor(UiTheme.ink(color));c.drawText(s,x,y,p);}
  private void fit(Canvas c,String s,float x,float y,float width,float size,int color,boolean bold){p.setTextSize(size);p.setTypeface(UiTheme.font(bold));String value=s;while(value.length()>0&&p.measureText(value)>width)value=value.substring(0,value.length()-1);if(value.length()<s.length()){while(value.length()>0&&p.measureText(value+"…")>width)value=value.substring(0,value.length()-1);value+="…";}text(c,value,x,y,size,color,bold);}
  private void wrap(Canvas c,String s,float x,float y,float width,float size,float lineHeight,int limit,int color){p.setTextSize(size);p.setTypeface(UiTheme.font(false));String line="";int lines=0;for(int i=0;i<s.length();i++){char ch=s.charAt(i);if(ch=='\n'||p.measureText(line+ch)>width){text(c,line,x,y+lines*lineHeight,size,color,false);line="";if(++lines>=limit)return;if(ch=='\n')continue;}line+=ch;}if(!line.isEmpty())text(c,line,x,y+lines*lineHeight,size,color,false);}
}
