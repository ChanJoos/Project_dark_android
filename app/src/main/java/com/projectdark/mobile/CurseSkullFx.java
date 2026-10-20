package com.projectdark.mobile;
import android.graphics.*;
/** Original captured curse silhouette/palette. No replacement anatomical skull or invented geometry. */
final class CurseSkullFx {
 static final float DURATION=.88f;
 private final CapturedSkillFx.Sequence[] originals=new CapturedSkillFx.Sequence[4];
 private final PorterDuffXfermode blend=new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
 private final Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG);
 private final Rect source=new Rect();private final RectF destination=new RectF();
 private static long bytes;
 CurseSkullFx(ClassicSkillReference refs){String[] ids={"SK_마법사_002","SK_마법사_011","SK_마법사_024","SK_마법사_053"};long total=0;for(int i=0;i<4;i++){originals[i]=refs.channel(ids[i],false).sequence;total+=originals[i].atlas.getAllocationByteCount();}bytes=total;}
  static int variant(String id){
    switch(id){
      case "SK_마법사_002":case "SK_마법사_047":return 0;
      case "SK_마법사_011":case "SK_성직자_012":case "SK_마법사_048":return 1;
      case "SK_마법사_024":case "SK_마법사_049":return 2;
      case "SK_마법사_053":case "SK_마법사_050":case "SK_마법사_046":return 3;
      default:return -1;
    }
  }
 static long allocationBytes(){return bytes;}
 void draw(Canvas c,String id,float age,float x,float y){int variant=variant(id);if(variant<0||age<0||age>=DURATION||!Float.isFinite(x)||!Float.isFinite(y))return;
  CapturedSkillFx.Sequence s=originals[variant];int frame=s.frame(age/DURATION*s.duration),col=frame%s.columns,row=frame/s.columns;
  source.set(col*s.width,row*s.height,(col+1)*s.width,(row+1)*s.height);
  destination.set(x-s.width/2f,y-s.height/2f,x+s.width/2f,y+s.height/2f);
  paint.setXfermode(blend);c.drawBitmap(s.atlas,source,destination,paint);paint.setXfermode(null);
 }
}
