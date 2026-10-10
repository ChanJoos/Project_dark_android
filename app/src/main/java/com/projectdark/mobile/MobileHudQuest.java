package com.projectdark.mobile;
import android.graphics.*;
import java.util.*;
/** Compact B tracker with current task priority and collapsed future entries. */
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
   QuestJournalModel.Row row=rows.get(i);RectF r=MobileHudLayout.questRow(i,d);boolean locked=row.status==QuestJournalModel.Status.LOCKED;
   int accent=locked?0xff969e95:i==0?0xffead195:0xffb9c8c5;
   p.setShader(new LinearGradient(r.left,r.top,r.right,r.top,new int[]{0x07101715,0x99101715,0x55101715},new float[]{0,.4f,1},Shader.TileMode.CLAMP));p.setStyle(Paint.Style.FILL);c.drawRect(r.left,r.top,r.right,locked?r.top+22:r.bottom-3,p);p.setShader(null);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.6f);p.setColor(locked?0x66727870:i==0?0xaac3a56c:0x778eaba2);c.drawLine(r.left+12,r.top+2,r.right-4,r.top+2,p);
   float x=r.left+7,y=r.top+12;path.reset();path.moveTo(x,y-7);path.lineTo(x+6,y);path.lineTo(x,y+7);path.lineTo(x-6,y);path.close();c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
   UiTheme.center(c,locked?"·":row.status==QuestJournalModel.Status.REPORT?"✓":"!",x,y+3,9,accent,true);
   String category=locked?"[다음] ":row.id.startsWith("CAMPAIGN_T")?"[학습] ":"[메인] ";
   UiTheme.fit(c,category+row.title,r.left+18,r.top+16,r.width()-42,10.5f,accent,true);UiTheme.right(c,"›",r.right-4,r.top+17,15,accent,true);
   if(locked)continue;
   String progress=row.status==QuestJournalModel.Status.ACTIVE&&row.goal>0?row.count+"/"+row.goal:row.label();
   UiTheme.fit(c,row.objective,r.left+18,r.top+33,r.width()-83,9.5f,UiTheme.TEXT,false);UiTheme.right(c,progress,r.right-8,r.top+33,8.5f,accent,true);
  }
 }
}
