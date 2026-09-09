package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.io.InputStream;
import java.net.URL;

/** PROJECT DARK v0.52 - approved mobile composition runtime. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final String WORLD_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), pixel=new Paint();
  private Bitmap world, player;
  private float scale=1,ox,oy,px=480,py=300,vx,vy;
  private final float jx=92,jy=444,jr=60; private float knobX=jx,knobY=jy;
  private boolean joy,running; private long last;
  private final Runnable loop=new Runnable(){public void run(){if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;if(joy){px+=vx*145*dt;py+=vy*145*dt;px=Math.max(180,Math.min(760,px));py=Math.max(120,Math.min(455,py));}invalidate();postDelayed(this,16);}};

  public GameView(Context c){super(c);pixel.setFilterBitmap(false);setKeepScreenOn(true);loadWorld();}
  private void loadWorld(){new Thread(()->{try(InputStream in=new URL(WORLD_URL).openStream()){Bitmap b=BitmapFactory.decodeStream(in);if(b!=null){world=b;int sw=b.getWidth(),sh=b.getHeight();int cw=Math.max(32,Math.min(70,sw/18)),ch=Math.max(55,Math.min(110,sh/7));int l=Math.max(0,sw/2-cw/2),t=Math.max(0,(int)(sh*.52f)-ch/2);player=Bitmap.createBitmap(b,l,t,Math.min(cw,sw-l),Math.min(ch,sh-t));}}catch(Exception ignored){}postInvalidate();}).start();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);} public void pause(){running=false;removeCallbacks(loop);}
  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);drawWorld(c);drawPlayer(c);drawHud(c);c.restore();}
  private void drawWorld(Canvas c){if(world==null){p.setColor(0xff090909);c.drawRect(0,0,W,H,p);p.setColor(0xffd9c69c);p.setTextSize(16);c.drawText("원작 화면 로딩 중...",405,270,p);return;}int sw=world.getWidth(),sh=world.getHeight();float da=W/H,sa=sw/(float)sh;Rect s;if(sa>da){int uw=Math.round(sh*da),l=(sw-uw)/2;s=new Rect(l,0,l+uw,sh);}else{int uh=Math.round(sw/da),t=(sh-uh)/2;s=new Rect(0,t,sw,t+uh);}c.drawBitmap(world,s,new RectF(0,0,W,H),pixel);p.setColor(0x26000000);c.drawRect(0,0,W,H,p);}
  private void drawPlayer(Canvas c){if(player==null)return;float h=72,w=h*player.getWidth()/(float)player.getHeight();p.setColor(0x77000000);c.drawOval(new RectF(px-17,py-5,px+17,py+5),p);c.drawBitmap(player,null,new RectF(px-w/2,py-h,px+w/2,py),pixel);}
  private void panel(Canvas c,float l,float t,float r,float b){p.setColor(0x9a0b0b0b);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xaaad9364);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setColor(0xffeee4cf);c.drawText(s,x,y,p);}
  private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){p.setColor(0xb51a1714);c.drawRoundRect(new RectF(l,t,r,b),5,5,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),5,5,p);}
  private void drawHud(Canvas c){
    panel(c,12,12,165,116);text(c,"GROUP",24,31,12);String[] n={"조춘찬","수수료좀챙","이루","별빛영혼"};for(int i=0;i<4;i++){text(c,n[i],27,50+i*15,10);bar(c,91,42+i*15,154,48+i*15,0xffdf3342,1f);}
    panel(c,176,12,320,70);text(c,"퀘스트",190,32,13);text(c,"밀레스의 첫걸음 0/4",190,52,10);
    panel(c,372,12,604,55);text(c,"[유리드]광산3층 전염동",421,29,11);bar(c,392,36,584,44,0xffe21d2e,1f);
    panel(c,714,12,902,124);text(c,"밀레스",728,31,11);p.setColor(0xaa090909);c.drawRect(728,38,887,104,p);text(c,"X:60  Y:46",815,117,9);
    String[] menu={"≡","▣","Q","G","W","⚙"};for(int i=0;i<6;i++){panel(c,912,20+i*50,948,58+i*50);text(c,menu[i],925,45+i*50,15);}
    panel(c,12,294,272,382);text(c,"[일반] 우주탐험: 시간 좀 같이 넣어야함",23,316,9);text(c,"[일반] 별빛영혼: ㅋㅋ",23,332,9);text(c,"[일반] 가실래",23,348,9);text(c,"일반   파티   길드   귓속말   시스템",23,373,9);
    p.setColor(0x33101010);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0x99d8c49b);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.FILL);p.setColor(0x997e7057);c.drawCircle(knobX,knobY,24,p);
    panel(c,337,462,608,528);text(c,"Lv 154",351,486,12);bar(c,400,474,594,485,0xffe62c42,1f);bar(c,400,490,594,501,0xff2388df,.9f);bar(c,400,507,594,518,0xff48b84d,.35f);
    String[] slots={"HP","MP","P","S","↻"};for(int i=0;i<5;i++)round(c,650+i*48,466,20,slots[i]);round(c,895,478,34,"ATK");round(c,920,516,26,"AUTO");
  }
  private void round(Canvas c,float x,float y,float r,String s){p.setColor(0xaa15120e);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xffc9a86c);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);p.setTextSize(s.length()>3?8:10);p.setColor(0xffffefd0);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}
  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:if(dist(x,y,jx,jy)<=jr*1.5f){joy=true;stick(x,y);}return true;case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;knobX=jx;knobY=jy;vx=vy=0;return true;}return true;}
  private void stick(float x,float y){float dx=x-jx,dy=y-jy,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>jr){dx=dx/len*jr;dy=dy/len*jr;len=jr;}knobX=jx+dx;knobY=jy+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;}else{vx=-.707f;vy=-.707f;}}else{if(ny>0){vx=-.707f;vy=.707f;}else{vx=.707f;vy=-.707f;}}}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
