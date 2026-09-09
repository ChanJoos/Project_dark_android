package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import java.io.InputStream;
import java.net.URL;

/** PROJECT DARK v0.53 - independent layered pixel character runtime. */
public final class GameView extends View {
  private static final float W=960f,H=540f;
  private static final String WORLD_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), pixel=new Paint();
  private Bitmap world;
  private float scale=1,ox,oy,px=480,py=300,vx,vy;
  private final float jx=92,jy=444,jr=60; private float knobX=jx,knobY=jy;
  private boolean joy,running; private long last; private int dir=0; private float walkClock=0;
  private final Runnable loop=()->{if(!running)return;long n=SystemClock.uptimeMillis();float dt=Math.min(.05f,(n-last)/1000f);last=n;if(joy&&(vx!=0||vy!=0)){px+=vx*145*dt;py+=vy*145*dt;walkClock+=dt;px=Math.max(180,Math.min(760,px));py=Math.max(120,Math.min(455,py));}invalidate();postDelayed(this,16);};

  public GameView(Context c){super(c);pixel.setFilterBitmap(false);setKeepScreenOn(true);loadWorld();}
  private void loadWorld(){new Thread(()->{try(InputStream in=new URL(WORLD_URL).openStream()){world=BitmapFactory.decodeStream(in);}catch(Exception ignored){}postInvalidate();}).start();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(loop);} public void pause(){running=false;removeCallbacks(loop);}
  protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/W,h/H);ox=(w-W*scale)/2;oy=(h-H*scale)/2;}
  protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);drawWorld(c);drawCharacter(c);drawHud(c);c.restore();}
  private void drawWorld(Canvas c){if(world==null){p.setColor(0xff090909);c.drawRect(0,0,W,H,p);return;}int sw=world.getWidth(),sh=world.getHeight();float da=W/H,sa=sw/(float)sh;Rect s;if(sa>da){int uw=Math.round(sh*da),l=(sw-uw)/2;s=new Rect(l,0,l+uw,sh);}else{int uh=Math.round(sw/da),t=(sh-uh)/2;s=new Rect(0,t,sw,t+uh);}c.drawBitmap(world,s,new RectF(0,0,W,H),pixel);p.setColor(0x26000000);c.drawRect(0,0,W,H,p);}

  // 16x28 logical sprite, rendered at 2x. Direction: 0 SW,1 SE,2 NW,3 NE.
  private void drawCharacter(Canvas c){
    float S=2.0f, x=px, y=py; int f=(vx==0&&vy==0)?0:((int)(walkClock*8f)%4); float bob=(f==1||f==3)?-1:0;
    p.setColor(0x66000000);c.drawOval(new RectF(x-13,y-4,x+13,y+4),p);
    c.save();c.translate(x,y-56+bob*S);c.scale(S,S);c.translate(-8,0);
    // rear arm / leg layers
    int skin=0xffffc68f,hair=0xff3b251b,outline=0xff171313,shirt=0xffeee5d3,blue=0xff3c6382,pants=0xff393a3a,shoe=0xff6a4526;
    int step=(f==1?1:f==3?-1:0); boolean leftFacing=(dir==0||dir==2); boolean back=(dir>=2);
    p.setStyle(Paint.Style.FILL);
    // legs
    rect(c,outline,4+step,19,7+step,26);rect(c,outline,10-step,19,13-step,26);
    rect(c,pants,5+step,19,7+step,24);rect(c,pants,10-step,19,12-step,24);
    rect(c,shoe,4+step,24,7+step,27);rect(c,shoe,10-step,24,13-step,27);
    // torso outline and tunic
    rect(c,outline,3,10,14,21);rect(c,shirt,4,11,13,19);rect(c,blue,4,16,13,20);rect(c,0xffc9b070,7,11,9,20);
    // arms swing
    int as=(f==1?1:f==3?-1:0);rect(c,outline,1,11+as,4,19+as);rect(c,skin,2,12+as,3,18+as);rect(c,outline,13,11-as,16,19-as);rect(c,skin,14,12-as,15,18-as);
    // head
    rect(c,outline,3,2,14,12);rect(c,skin,4,3,13,11);
    // hair silhouette differs front/back and direction
    rect(c,hair,3,1,14,6);rect(c,hair,2,3,5,9);rect(c,hair,12,3,15,8);rect(c,hair,5,0,12,3);
    if(!back){int eye=0xff252020;if(leftFacing){rect(c,eye,5,7,6,8);rect(c,eye,9,7,10,8);}else{rect(c,eye,7,7,8,8);rect(c,eye,11,7,12,8);}}
    // directional highlight to make facing readable
    rect(c,0xff76503b,leftFacing?3:11,3,leftFacing?4:12,6);
    c.restore();
  }
  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}

  private void panel(Canvas c,float l,float t,float r,float b){p.setColor(0x9a0b0b0b);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.3f);p.setColor(0xaaad9364);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String s,float x,float y,float size){p.setTextSize(size);p.setColor(0xffeee4cf);c.drawText(s,x,y,p);}
  private void bar(Canvas c,float l,float t,float r,float b,int color,float ratio){p.setColor(0xb51a1714);c.drawRoundRect(new RectF(l,t,r,b),5,5,p);p.setColor(color);c.drawRoundRect(new RectF(l,t,l+(r-l)*ratio,b),5,5,p);}
  private void drawHud(Canvas c){panel(c,12,12,165,116);text(c,"GROUP",24,31,12);String[] n={"조춘찬","수수료좀챙","이루","별빛영혼"};for(int i=0;i<4;i++){text(c,n[i],27,50+i*15,10);bar(c,91,42+i*15,154,48+i*15,0xffdf3342,1f);}panel(c,176,12,320,70);text(c,"퀘스트",190,32,13);text(c,"밀레스의 첫걸음 0/4",190,52,10);panel(c,372,12,604,55);text(c,"[유리드]광산3층 전염동",421,29,11);bar(c,392,36,584,44,0xffe21d2e,1f);panel(c,714,12,902,124);text(c,"밀레스",728,31,11);p.setColor(0xaa090909);c.drawRect(728,38,887,104,p);text(c,"X:60  Y:46",815,117,9);String[] menu={"≡","▣","Q","G","W","⚙"};for(int i=0;i<6;i++){panel(c,912,20+i*50,948,58+i*50);text(c,menu[i],925,45+i*50,15);}panel(c,12,294,272,382);text(c,"[일반] 우주탐험: 시간 좀 같이 넣어야함",23,316,9);text(c,"[일반] 별빛영혼: ㅋㅋ",23,332,9);text(c,"[일반] 가실래",23,348,9);text(c,"일반   파티   길드   귓속말   시스템",23,373,9);p.setColor(0x33101010);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0x99d8c49b);c.drawCircle(jx,jy,jr,p);p.setStyle(Paint.Style.FILL);p.setColor(0x997e7057);c.drawCircle(knobX,knobY,24,p);panel(c,337,462,608,528);text(c,"Lv 1",351,486,12);bar(c,400,474,594,485,0xffe62c42,1f);bar(c,400,490,594,501,0xff2388df,.9f);bar(c,400,507,594,518,0xff48b84d,.35f);String[] slots={"HP","MP","P","S","↻"};for(int i=0;i<5;i++)round(c,650+i*48,466,20,slots[i]);round(c,895,478,34,"ATK");round(c,920,516,26,"AUTO");}
  private void round(Canvas c,float x,float y,float r,String s){p.setColor(0xaa15120e);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xffc9a86c);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);p.setTextSize(s.length()>3?8:10);p.setColor(0xffffefd0);float tw=p.measureText(s);c.drawText(s,x-tw/2,y+3,p);}
  public boolean onTouchEvent(MotionEvent e){float x=(e.getX()-ox)/scale,y=(e.getY()-oy)/scale;switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:if(dist(x,y,jx,jy)<=jr*1.5f){joy=true;stick(x,y);}return true;case MotionEvent.ACTION_MOVE:if(joy)stick(x,y);return true;case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:joy=false;knobX=jx;knobY=jy;vx=vy=0;return true;}return true;}
  private void stick(float x,float y){float dx=x-jx,dy=y-jy,len=(float)Math.sqrt(dx*dx+dy*dy);if(len>jr){dx=dx/len*jr;dy=dy/len*jr;len=jr;}knobX=jx+dx;knobY=jy+dy;if(len<8){vx=vy=0;return;}float nx=dx/len,ny=dy/len;if(Math.abs(nx)>Math.abs(ny)){if(nx>0){vx=.707f;vy=.707f;dir=1;}else{vx=-.707f;vy=-.707f;dir=2;}}else{if(ny>0){vx=-.707f;vy=.707f;dir=0;}else{vx=.707f;vy=-.707f;dir=3;}}}
  private float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
}
