package com.projectdark.mobile;
import android.graphics.*;
/** Touch replacement for the original 은총 keyword. Every payment has an exact confirmation. */
public final class TempleGrowthWindow {
 private boolean open,confirm;private int tab,count=1;private long quoted;private int quotedBase;private String notice="원하는 은총을 선택하세요.";
 private RuntimeState state;
 public boolean isOpen(){return open;}public void close(){open=false;confirm=false;}
 public void open(RuntimeState s){state=s;open=true;confirm=false;tab=0;count=1;notice="원하는 은총을 선택하세요.";}
 private TempleGrowthPolicy.Resource resource(){return tab==0?TempleGrowthPolicy.Resource.HP:TempleGrowthPolicy.Resource.MP;}
 private int base(){return tab==0?state.rpg().baseMaxHp():state.rpg().baseMaxMp();}
 private String n(long value){return String.format(java.util.Locale.KOREA,"%,d",value);}
 public void draw(Canvas c,float offset){if(!open)return;c.save();c.translate(offset,0);RpgProgressionState r=state.rpg();UiTheme.panel(c,new RectF(185,92,775,468),"세오 · 신의 은총");UiTheme.button(c,new RectF(715,101,762,129),"닫기",true,false);
  for(int i=0;i<3;i++)UiTheme.button(c,new RectF(205+i*183,146,377+i*183,182),new String[]{"체력 구매","마력 구매","1차 승급"}[i],true,tab==i);
  UiTheme.text(c,"보유 EXP  "+n(r.normalExp())+" / "+n(TempleGrowthPolicy.EXP_CAP),210,211,12,UiTheme.TEXT,true);
  UiTheme.text(c,"순수 체력 "+n(r.baseMaxHp())+"    순수 마력 "+n(r.baseMaxMp()),210,238,12,UiTheme.TEXT,false);
  if(tab<2){long cost=TempleGrowthPolicy.quote(r,resource(),count);UiTheme.text(c,(tab==0?"체력 +50":"마력 +25")+" × "+count+"회  ·  총 +"+n((long)TempleGrowthPolicy.unit(resource())*count),210,271,13,UiTheme.GOLD,true);
   UiTheme.text(c,"필요 EXP  "+(cost<0?"구매 한도 초과":n(cost)),210,300,13,UiTheme.TEXT,false);
   UiTheme.text(c,"구매 후 EXP  "+(cost>=0&&cost<=r.normalExp()?n(r.normalExp()-cost):"경험치 부족"),210,327,12,UiTheme.TEXT,false);
   if(!confirm)for(int i=0;i<3;i++)UiTheme.button(c,new RectF(210+i*108,345,310+i*108,378),new String[]{"1회","10회","100회"}[i],true,count==new int[]{1,10,100}[i]);
  }else {String job=r.currentJobCode();UiTheme.text(c,CampaignProgress.jobName(job)+" · "+(r.firstAdvanced()?"1차 승급 완료":"99레벨 이상"),210,271,13,UiTheme.GOLD,true);
   UiTheme.text(c,"필요 순수 체력 "+n(TempleGrowthPolicy.hpRequired(job))+" / 마력 "+n(TempleGrowthPolicy.mpRequired(job)),210,300,12,UiTheme.TEXT,false);
   UiTheme.text(c,"납부 EXP "+n(TempleGrowthPolicy.ADVANCEMENT_EXP)+" · 체마는 유지됩니다",210,327,12,UiTheme.TEXT,false);
   UiTheme.text(c,"기술 숙련·승급길·제물 없이 바로 승급",210,363,12,UiTheme.TEXT,false);
  }
  UiTheme.fit(c,confirm?"표시한 경험치를 납부하고 진행하시겠습니까?":notice,210,405,530,12,UiTheme.GOLD,false);
  UiTheme.button(c,new RectF(462,420,595,453),confirm?"취소":"대화 끝내기",true,false);UiTheme.button(c,new RectF(609,420,749,453),confirm?"확정":tab==2?"승급하기":"구매하기",true,true);c.restore();
 }
 public void touch(float x,float y,float offset){if(!open)return;x-=offset;
  if(hit(x,y,715,101,762,129)){close();return;}
  if(hit(x,y,462,420,595,453)){if(confirm)confirm=false;else close();return;}
  if(!confirm){for(int i=0;i<3;i++){if(hit(x,y,205+i*183,146,377+i*183,182)){tab=i;count=1;notice="원하는 은총을 선택하세요.";return;}if(tab<2&&hit(x,y,210+i*108,345,310+i*108,378)){count=new int[]{1,10,100}[i];return;}}}
  if(!hit(x,y,609,420,749,453))return;
  RpgProgressionState r=state.rpg();if(!state.player().alive){notice="살아 있는 상태에서 이용하세요.";confirm=false;return;}
  if(!confirm){if(tab==2){TempleGrowthPolicy.Result gate=TempleGrowthPolicy.eligibility(r);if(gate!=TempleGrowthPolicy.Result.OK){notice=TempleGrowthPolicy.message(gate);return;}quoted=TempleGrowthPolicy.ADVANCEMENT_EXP;}else {quoted=TempleGrowthPolicy.quote(r,resource(),count);quotedBase=base();if(r.normalLevel()<99||TempleGrowthPolicy.hpRequired(r.currentJobCode())==0||quoted<0||r.normalExp()<quoted){notice="99레벨·기본 직업·보유 경험치를 확인하세요.";return;}}confirm=true;return;}
  if(tab<2&&(quoted!=TempleGrowthPolicy.quote(r,resource(),count)||quotedBase!=base())){confirm=false;notice="체마가 변경되었습니다. 다시 확인하세요.";return;}
  final TempleGrowthPolicy.Result[] result={TempleGrowthPolicy.Result.INVALID};boolean saved=F5mSaveStore.transactActive(r,null,()->{result[0]=tab==2?r.advanceAtTemple():r.buyTempleResource(resource(),count);return result[0]==TempleGrowthPolicy.Result.OK;});
  if(saved){state.applyDerivedGrowth();notice=tab==2?"1차 승급을 축하합니다!":TempleGrowthPolicy.message(result[0]);}else notice=TempleGrowthPolicy.message(result[0]==TempleGrowthPolicy.Result.OK?TempleGrowthPolicy.Result.SAVE_FAILED:result[0]);confirm=false;
 }
 private static boolean hit(float x,float y,float l,float t,float r,float b){return x>=l&&x<=r&&y>=t&&y<=b;}
}
