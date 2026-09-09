package com.projectdark.mobile;

import android.content.*;
import android.graphics.*;
import android.os.SystemClock;
import android.view.*;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public final class GameView extends View {
  static final int N=24; final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final Paint px=new Paint();
  final Random rng=new Random(71); final ArrayList<Mob> mobs=new ArrayList<>();
  float scale=1,ox,oy; int x=8,y=10,dir=3,target=-1; long last,moveReady,attackReady,autoSuspend; boolean running,auto;
  Bitmap reference; final String referenceUrl="https://storage.nexon.com/Data01/GnxFile/004/100/000/00/07/18016979784834580.bmp";
  final Runnable tick=new Runnable(){public void run(){if(!running)return;long now=SystemClock.uptimeMillis();update(Math.min(100,now-last));last=now;invalidate();postDelayed(this,33);}};
  static final class Mob {int id,x,y,hp=100,max=100,dir=0;long ready;Mob(int i,int a,int b){id=i;x=a;y=b;}}
  public GameView(Context c){super(c);px.setFilterBitmap(false);mobs.add(new Mob(1,12,10));mobs.add(new Mob(2,15,13));mobs.add(new Mob(3,9,15));loadReference();}
  void loadReference(){new Thread(()->{try(InputStream in=new URL(referenceUrl).openStream()){reference=BitmapFactory.decodeStream(in);postInvalidate();}catch(Exception ignored){}}).start();}
  public void resume(){if(running)return;running=true;last=SystemClock.uptimeMillis();post(tick);} public void pause(){running=false;removeCallbacks(tick);}
  boolean floor(int a,int b){return a>=2&&b>=2&&a<=21&&b<=21&&!((a>=14&&a<=16&&b>=5&&b<=7)||(a>=4&&a<=6&&b>=15&&b<=17));}
  boolean occupied(int a,int b){for(Mob m:mobs)if(m.hp>0&&m.x==a&&m.y==b)return true;return false;}
  int dist(int ax,int ay,int bx,int by){return Math.abs(ax-bx)+Math.abs(ay-by);}
  boolean move(int dx,int dy){long t=SystemClock.uptimeMillis();if(Math.abs(dx)+Math.abs(dy)!=1||t<moveReady||!floor(x+dx,y+dy)||occupied(x+dx,y+dy))return false;x+=dx;y+=dy;dir=dx<0?0:dy<0?1:dy>0?2:3;moveReady=t+165;return true;}
  void update(long dt){long t=SystemClock.uptimeMillis();if(auto&&t>=autoSuspend){Mob n=nearest();if(n!=null){target=n.id;if(dist(x,y,n.x,n.y)>1)stepToward(n.x,n.y);else attack();}}
    for(Mob m:mobs){if(m.hp<=0)continue;int d=dist(m.x,m.y,x,y);if(d>1&&d<=6&&t>=m.ready){int dx=Integer.signum(x-m.x),dy=Integer.signum(y-m.y);int nx=m.x+(dx!=0?dx:0),ny=m.y+(dx!=0?0:dy);if(floor(nx,ny)&&!occupied(nx,ny)&&!(nx==x&&ny==y)){m.x=nx;m.y=ny;m.dir=dx<0?0:dy<0?1:dy>0?2:3;}m.ready=t+420;}}
  }
  Mob nearest(){Mob best=null;int bd=999;for(Mob m:mobs)if(m.hp>0){int d=dist(x,y,m.x,m.y);if(d<bd){bd=d;best=m;}}return best;}
  void attack(){long t=SystemClock.uptimeMillis();Mob m=null;for(Mob q:mobs)if(q.id==target)m=q;if(m==null||m.hp<=0||dist(x,y,m.x,m.y)>1||t<attackReady)return;m.hp-=25;attackReady=t+600;if(m.hp<=0){m.hp=0;target=-1;}}
  boolean stepToward(int tx,int ty){int[] prev=new int[N*N];Arrays.fill(prev,-1);int st=x+y*N,en=tx+ty*N;ArrayDeque<Integer> q=new ArrayDeque<>();q.add(st);prev[st]=st;int[] dx={1,-1,0,0},dy={0,0,1,-1};while(!q.isEmpty()&&prev[en]<0){int k=q.remove(),a=k%N,b=k/N;for(int i=0;i<4;i++){int nx=a+dx[i],ny=b+dy[i],nk=nx+ny*N;if(!floor(nx,ny)||(occupied(nx,ny)&&nk!=en)||prev[nk]>=0)continue;prev[nk]=k;q.add(nk);}}if(prev[en]<0||en==st)return false;int k=en;while(prev[k]!=st)k=prev[k];return move(k%N-x,k/N-y);}
  float sx(int a,int b){return 480+(a-b)*26;} float sy(int a,int b){return 105+(a+b)*13;}
  @Override protected void onSizeChanged(int w,int h,int ow,int oh){scale=Math.min(w/960f,h/540f);ox=(w-960*scale)/2;oy=(h-540*scale)/2;}
  void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);} void text(Canvas c,String s,float a,float b,float z,int col){color(col);p.setTextSize(z);c.drawText(s,a,b,p);}
  @Override protected void onDraw(Canvas c){c.drawColor(Color.BLACK);c.save();c.translate(ox,oy);c.scale(scale,scale);color(0xff0b0d0c);c.drawRect(0,0,960,540,p);
    c.save();c.clipRect(0,48,960,405);for(int sum=0;sum<48;sum++)for(int a=0;a<N;a++){int b=sum-a;if(b<0||b>=N)continue;float X=sx(a,b),Y=sy(a,b);Path path=new Path();path.moveTo(X,Y-13);path.lineTo(X+26,Y);path.lineTo(X,Y+13);path.lineTo(X-26,Y);path.close();if(floor(a,b)){if(reference!=null){int sw=reference.getWidth(),sh=reference.getHeight();Rect src=new Rect(Math.floorMod(a*37,Math.max(1,sw-52)),Math.floorMod(b*29,Math.max(1,sh-26)),Math.min(sw,Math.floorMod(a*37,Math.max(1,sw-52))+52),Math.min(sh,Math.floorMod(b*29,Math.max(1,sh-26))+26));c.save();c.clipPath(path);c.drawBitmap(reference,src,new RectF(X-26,Y-13,X+26,Y+13),px);c.restore();}else{color(0xff394139);c.drawPath(path,p);}color(0x33202020);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);}else if(a>1&&b>1&&a<22&&b<22){color(0xff171a18);c.drawPath(path,p);}}
    ArrayList<int[]> es=new ArrayList<>();es.add(new int[]{x,y,-1});for(Mob m:mobs)if(m.hp>0)es.add(new int[]{m.x,m.y,m.id});es.sort(Comparator.comparingInt(e->e[0]+e[1]));for(int[] e:es){float X=sx(e[0],e[1]),Y=sy(e[0],e[1]);color(0x66000000);c.drawOval(X-13,Y-4,X+13,Y+5,p);if(e[2]<0){drawActor(c,X,Y,0xffd6c29a,dir);text(c,"모험가",X-22,Y-46,11,0xfff4e9cb);}else{Mob m=null;for(Mob z:mobs)if(z.id==e[2])m=z;if(m!=null){drawActor(c,X,Y,m.id==target?0xffc96e52:0xff8fa68a,m.dir);text(c,"원작 몬스터 매핑 대기",X-45,Y-42,9,0xffd0c8ae);if(m.hp<m.max){color(0xff6d1e1e);c.drawRect(X-20,Y-37,X+20,Y-33,p);color(0xffb8463f);c.drawRect(X-20,Y-37,X-20+40*m.hp/(float)m.max,Y-33,p);}}}c.restore();
    color(0xff151714);c.drawRect(0,0,960,48,p);text(c,"PROJECT DARK · v0.45",18,30,18,0xffd6bd7d);text(c,"원작 화면 샘플 기반 타일 렌더 · 4방향 대각 이동 · 몬스터 추적",250,29,12,0xffc8c2ad);
    color(0xff101210);c.drawRect(0,406,960,134,p);button(c,"↖",18,420,58,48);button(c,"↗",82,420,58,48);button(c,"↙",18,476,58,48);button(c,"↘",82,476,58,48);button(c,auto?"AUTO ON":"AUTO OFF",720,430,170,58);text(c,"밀레스 이동 런타임",170,447,16,0xfff1e5c2);text(c,"탭 이동 · 몬스터 탭 타겟 · ATTACK",170,474,12,0xffaaa58f);button(c,"ATTACK",520,430,170,58);c.restore();}
  void drawActor(Canvas c,float X,float Y,int col,int d){color(col);c.drawOval(X-9,Y-32,X+9,Y-14,p);color(0xffd9b596);c.drawCircle(X,Y-38,7,p);color(0xff40352d);float dx=d==0?-7:d==3?7:0,dy=d==1?-4:d==2?4:0;c.drawCircle(X+dx,Y-38+dy,2,p);}
  void button(Canvas c,String s,float a,float b,float w,float h){color(0xff252822);c.drawRect(a,b,a+w,b+h,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xff9b8150);c.drawRect(a,b,a+w,b+h,p);p.setStyle(Paint.Style.FILL);text(c,s,a+10,b+h/2+6,16,0xffeee3c3);}
  @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float a=(e.getX()-ox)/scale,b=(e.getY()-oy)/scale;autoSuspend=SystemClock.uptimeMillis()+1200;if(b>=406){if(a>=18&&a<=76&&b>=420&&b<=468)move(-1,0);else if(a>=82&&a<=140&&b>=420&&b<=468)move(0,-1);else if(a>=18&&a<=76&&b>=476&&b<=524)move(0,1);else if(a>=82&&a<=140&&b>=476&&b<=524)move(1,0);else if(a>=720&&a<=890){auto=!auto;}else if(a>=520&&a<=690)attack();return true;}for(Mob m:mobs)if(m.hp>0&&Math.abs(sx(m.x,m.y)-a)<25&&Math.abs(sy(m.x,m.y)-b)<30){target=m.id;return true;}int sum=Math.round((b-105)/13f),dif=Math.round((a-480)/26f);int tx=Math.round((sum+dif)/2f),ty=Math.round((sum-dif)/2f);if(floor(tx,ty))stepToward(tx,ty);return true;}
}
