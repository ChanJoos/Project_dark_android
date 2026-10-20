package com.projectdark.mobile;

import android.graphics.*;

/** User-reference reconstruction, not recovered original pixels. Prewarmed shared skull atlas. */
final class CurseSkullFx {
  static final float DURATION=.88f;
  static final int FRAMES=12,CELL_W=128,CELL_H=160;
  private static final int[] COLORS={0xff3977ff,0xffffd329,0xffff42bc,0xffff4938};
  private static Bitmap atlas;
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  private final Rect source=new Rect();private final RectF dest=new RectF();
  CurseSkullFx(){prepare();}
  static int variant(String id){
    switch(id){
      case "SK_마법사_002":case "SK_마법사_047":return 0;
      case "SK_마법사_011":case "SK_성직자_012":case "SK_마법사_048":return 1;
      case "SK_마법사_024":case "SK_마법사_049":return 2;
      case "SK_마법사_053":case "SK_마법사_050":case "SK_마법사_046":return 3;
      default:return -1;
    }
  }
  static long allocationBytes(){return atlas==null?0:atlas.getAllocationByteCount();}
  void draw(Canvas c,String id,float age,float x,float y){
    int v=variant(id);if(v<0||age<0||age>=DURATION||!Float.isFinite(x)||!Float.isFinite(y))return;
    int frame=Math.min(FRAMES-1,(int)(age/DURATION*FRAMES));
    source.set(frame*CELL_W,v*CELL_H,(frame+1)*CELL_W,(v+1)*CELL_H);
    // Same visual center for small/large actors; both channels follow the live recipient.
    dest.set(x-36,y-48,x+36,y+42);c.drawBitmap(atlas,source,dest,paint);
  }
  private static synchronized void prepare(){
    if(atlas!=null)return;
    Bitmap b=Bitmap.createBitmap(CELL_W*FRAMES,CELL_H*COLORS.length,Bitmap.Config.ARGB_8888);
    Canvas c=new Canvas(b);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    for(int v=0;v<COLORS.length;v++)for(int f=0;f<FRAMES;f++){
      c.save();c.translate(f*CELL_W+64,v*CELL_H+78);float phase=f/(float)(FRAMES-1);
      c.translate(0,-5*phase);float scale=.86f+.14f*Math.min(1,phase*5);c.scale(scale,scale);
      skull(c,p,COLORS[v],phase);c.restore();
    }
    atlas=b;
  }
  private static void skull(Canvas c,Paint p,int color,float t){
    int alpha=Math.round(255*Math.min(1,(1-t)*4));
    float pulse=.94f+.06f*(float)Math.sin(t*Math.PI*3);
    p.setShader(new RadialGradient(0,-5,72,new int[]{tint(color,alpha/2),tint(color,alpha/4),0},new float[]{0,.55f,1},Shader.TileMode.CLAMP));
    c.drawOval(-62,-77,62,76,p);p.setShader(null);
    Path bone=new Path();bone.moveTo(0,-60);bone.cubicTo(34,-62,46,-40,43,-16);bone.cubicTo(42,-2,34,8,30,15);bone.lineTo(32,29);bone.lineTo(23,33);bone.lineTo(22,48);bone.cubicTo(15,58,-15,58,-22,48);bone.lineTo(-23,33);bone.lineTo(-32,29);bone.lineTo(-30,15);bone.cubicTo(-34,8,-42,-2,-43,-16);bone.cubicTo(-46,-40,-34,-62,0,-60);bone.close();
    p.setStyle(Paint.Style.STROKE);p.setStrokeJoin(Paint.Join.ROUND);
    for(int width:new int[]{15,8,3}){p.setStrokeWidth(width*pulse);p.setColor(tint(color,alpha/(width==15?5:width==8?3:1)));c.drawPath(bone,p);}
    p.setStyle(Paint.Style.FILL);
    // Luminous bone mass, not a wireframe: warm forehead, cool temple shade and solid jaw.
    p.setShader(new LinearGradient(-40,-48,38,50,new int[]{tint(0xfffff1cf,alpha*4/5),tint(0xfff7e5c9,alpha*3/5),tint(color,alpha/2),tint(0xfffff4dc,alpha*4/5)},new float[]{0,.34f,.66f,1},Shader.TileMode.CLAMP));c.drawPath(bone,p);p.setShader(null);
    Path dome=new Path();dome.moveTo(-34,-21);dome.cubicTo(-43,-49,-16,-59,5,-55);dome.cubicTo(-15,-51,-20,-36,-20,-24);dome.close();p.setColor(tint(0xfffff8e7,alpha/2));c.drawPath(dome,p);
    Path temple=new Path();temple.moveTo(29,-46);temple.cubicTo(44,-32,37,-4,30,8);temple.lineTo(25,-5);temple.cubicTo(34,-22,30,-34,29,-46);p.setColor(tint(color,alpha*2/3));c.drawPath(temple,p);
    Path cheek=new Path();cheek.moveTo(-34,6);cheek.quadTo(-25,15,-13,13);cheek.lineTo(-8,24);cheek.lineTo(-21,21);cheek.lineTo(-29,27);cheek.close();cheek.moveTo(34,6);cheek.quadTo(25,15,13,13);cheek.lineTo(8,24);cheek.lineTo(21,21);cheek.lineTo(29,27);cheek.close();p.setColor(tint(0xfffff6e0,alpha*4/5));c.drawPath(cheek,p);
    // Tapered brow ridges, deep sockets and separate cheekbones give the skull a readable face.
    Path eyes=new Path();eyes.moveTo(-34,-17);eyes.quadTo(-19,-18,-7,-7);eyes.cubicTo(-9,7,-21,11,-29,3);eyes.quadTo(-33,-4,-34,-17);eyes.close();
    eyes.moveTo(34,-17);eyes.quadTo(19,-18,7,-7);eyes.cubicTo(9,7,21,11,29,3);eyes.quadTo(33,-4,34,-17);eyes.close();
    p.setColor(tint(0xff100d2b,alpha*9/10));c.drawPath(eyes,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3.6f);p.setColor(tint(0xfffff6df,alpha));c.drawPath(eyes,p);
    Path nose=new Path();nose.moveTo(0,3);nose.lineTo(-6,17);nose.lineTo(0,14);nose.lineTo(6,17);nose.close();
    p.setStyle(Paint.Style.FILL);p.setColor(tint(0xff171127,alpha));c.drawPath(nose,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(tint(0xfffff5dc,alpha));c.drawPath(nose,p);
    Path ridges=new Path();ridges.moveTo(-35,-19);ridges.lineTo(-13,-23);ridges.lineTo(-4,-12);ridges.moveTo(35,-19);ridges.lineTo(13,-23);ridges.lineTo(4,-12);
    ridges.moveTo(-35,11);ridges.lineTo(-20,16);ridges.lineTo(-14,24);ridges.moveTo(35,11);ridges.lineTo(20,16);ridges.lineTo(14,24);
    ridges.moveTo(-22,29);ridges.quadTo(0,39,22,29);ridges.moveTo(-20,46);ridges.quadTo(0,53,20,46);c.drawPath(ridges,p);
    // Individual teeth, jaw and fine cranial fractures, rather than a circle/icon substitute.
    for(int i=-3;i<=3;i++){float x=i*5.1f;p.setStrokeWidth(i==0?2:1.4f);c.drawLine(x,32+Math.abs(i)*-.7f,x,46-Math.abs(i)*.5f,p);}
    Path crack=new Path();crack.moveTo(7,-56);crack.lineTo(2,-43);crack.lineTo(8,-34);crack.lineTo(1,-25);crack.moveTo(2,-43);crack.lineTo(-8,-41);crack.lineTo(-12,-33);crack.moveTo(-37,-33);crack.lineTo(-28,-28);crack.lineTo(-30,-22);p.setStrokeWidth(1.2f);p.setColor(tint(0xff735c81,alpha*3/4));c.drawPath(crack,p);
    p.setStrokeWidth(1.7f);p.setColor(tint(0xfffff6e8,alpha));c.drawArc(-39,-57,39,27,186,148,false,p);c.drawArc(-23,22,23,56,5,170,false,p);
    for(int i=0;i<7;i++){float a=i*.8976f+t*.5f;float xx=(float)Math.cos(a)*51,yy=(float)Math.sin(a)*65-4;float r=i%2==0?2.8f:1.5f;p.setColor(tint(color,alpha*2/3));p.setStrokeWidth(1);c.drawLine(xx-r*2,yy,xx+r*2,yy,p);c.drawLine(xx,yy-r*2,xx,yy+r*2,p);p.setStyle(Paint.Style.FILL);p.setColor(tint(0xfffff9ed,alpha));c.drawCircle(xx,yy,r*.45f,p);p.setStyle(Paint.Style.STROKE);}
    p.setStyle(Paint.Style.FILL);
  }
  private static int tint(int color,int alpha){return(color&0xffffff)|(Math.max(0,Math.min(255,alpha))<<24);}
}
