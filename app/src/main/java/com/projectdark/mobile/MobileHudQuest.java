package com.projectdark.mobile;
import android.graphics.*;
import java.util.*;
/** Reference-style tracker projection; all content and actions remain journal-owned. */
final class MobileHudQuest {
  private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  private static final Path path=new Path();
  static List<QuestJournalModel.Row> shown(List<QuestJournalModel.Row> all){
    List<QuestJournalModel.Row> out=new ArrayList<>();
    for(QuestJournalModel.Row row:all)if(row.navigable()&&out.size()<3)out.add(row);
    for(QuestJournalModel.Row row:all)if(row.status==QuestJournalModel.Status.LOCKED&&out.size()<3)out.add(row);
    return out;
  }
  static QuestJournalModel.Row hit(List<QuestJournalModel.Row> all,float x,float y,float d){
    List<QuestJournalModel.Row> rows=shown(all);for(int i=0;i<rows.size();i++)if(MobileHudLayout.questRow(i,d).contains(x,y))return rows.get(i);return null;
  }
  static void draw(Canvas c,List<QuestJournalModel.Row> all,float d){
    List<QuestJournalModel.Row> rows=shown(all);
    for(int i=0;i<rows.size();i++){
      QuestJournalModel.Row row=rows.get(i);RectF r=MobileHudLayout.questRow(i,d);
      boolean locked=row.status==QuestJournalModel.Status.LOCKED;
      int accent=locked?0xff8eab89:0xffdcc58a;
      p.setShader(new LinearGradient(r.left,r.top,r.right,r.top,0x000b0d10,0x850b0d10,Shader.TileMode.CLAMP));c.drawRect(r,p);p.setShader(null);
      // Narrow notched title ribbon, diamond marker and right-aligned medallion.
      path.reset();path.moveTo(r.left+3,r.top+4);path.lineTo(r.right-29,r.top+4);path.lineTo(r.right-25,r.top+13);path.lineTo(r.right-29,r.top+22);path.lineTo(r.left+3,r.top+22);path.lineTo(r.left-2,r.top+13);path.close();
      p.setColor(locked?0x363b5c38:0x515b4c24);c.drawPath(path,p);
      p.setColor(accent);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.65f);c.drawPath(path,p);
      path.reset();path.moveTo(r.left+8,r.top+7);path.lineTo(r.left+13,r.top+13);path.lineTo(r.left+8,r.top+19);path.lineTo(r.left+3,r.top+13);path.close();c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
      String category=locked?"[다음] ":row.id.startsWith("CAMPAIGN_T")?"[학습] ":"[메인] ";
      UiTheme.fit(c,category+row.title,r.left+18,r.top+17,r.width()-52,10.5f,accent,true);
      MobileHudSkin.questSeal(c,r.right-12,r.top+13,row.status);
      UiTheme.fit(c,row.objective,r.left+15,r.top+35,r.width()-29,9.5f,locked?0xffb4b9ad:UiTheme.TEXT,false);
      String progress=row.status==QuestJournalModel.Status.ACTIVE&&row.goal>0?row.count+" / "+row.goal:row.label();
      UiTheme.right(c,progress,r.right-7,r.top+49,8.5f,accent,true);
      UiTheme.text(c,locked?"선행 의뢰 완료 후 개방":row.status==QuestJournalModel.Status.REPORT?"완료 · 보상 수령 ›":row.id.startsWith("CAMPAIGN_T")?"학습 안내 ›":"목표 추적 ›",r.left+15,r.top+49,8,locked?0xff989f94:0xffd6d0bc,false);
      if(row.goal>0&&row.status==QuestJournalModel.Status.ACTIVE){float q=Math.max(0,Math.min(1,row.count/(float)row.goal));p.setColor(0x555b5a48);c.drawRect(r.left+15,r.bottom-3,r.right-7,r.bottom-2,p);p.setColor(accent);c.drawRect(r.left+15,r.bottom-3,r.left+15+(r.width()-22)*q,r.bottom-2,p);}
    }
  }
}
