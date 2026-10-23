package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
/** Original retained ASCII font atlas, nearest sampled. Material is prebuilt once. */
final class OriginalDamageDigitsV260 {
 private final Bitmap[] glyphs=new Bitmap[11];private final Paint paint=new Paint();private final RectF dst=new RectF();
 OriginalDamageDigitsV260(Context context){
  try{
   Bitmap font=BitmapFactory.decodeStream(context.getAssets().open("town-v247/font-en.png"));if(font==null)throw new IllegalStateException("Original font");
   Paint p=new Paint();p.setFilterBitmap(false);
   for(int i=0;i<11;i++){
    char ch=i==10?'-':(char)('0'+i);int code=ch-32;Rect source=new Rect((code%32)*8,(code/32)*12,(code%32+1)*8,(code/32+1)*12);
    Bitmap mask=Bitmap.createBitmap(font,source.left,source.top,8,12);Bitmap out=Bitmap.createBitmap(10,14,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(out);
    p.setColorFilter(new PorterDuffColorFilter(0xff302138,PorterDuff.Mode.SRC_IN));
    for(int y=0;y<3;y++)for(int x=0;x<3;x++)if(x!=1||y!=1)c.drawBitmap(mask,x,y,p);
    p.setColorFilter(new PorterDuffColorFilter(0xfff5ffff,PorterDuff.Mode.SRC_IN));c.drawBitmap(mask,1,1,p);
    c.save();c.clipRect(0,8,10,14);p.setColorFilter(new PorterDuffColorFilter(0xffd3d1e9,PorterDuff.Mode.SRC_IN));c.drawBitmap(mask,1,1,p);c.restore();
    glyphs[i]=out;mask.recycle();
   }
   font.recycle();paint.setFilterBitmap(false);paint.setAntiAlias(false);
  }catch(java.io.IOException e){throw new IllegalStateException(e);}
 }
 void draw(Canvas c,String value,float center,float baseline,float alpha){
  float scale=1.25f,advance=8f*scale,left=Math.round(center-value.length()*advance*.5f);
  paint.setAlpha(Math.round(255f*Math.max(0f,Math.min(1f,alpha))));
  for(int i=0;i<value.length();i++){char ch=value.charAt(i);int index=ch=='-'?10:ch-'0';if(index<0||index>=glyphs.length)continue;
   float x=left+i*advance;dst.set(x,Math.round(baseline-12f*scale),x+10f*scale,Math.round(baseline-12f*scale)+14f*scale);c.drawBitmap(glyphs[index],null,dst,paint);
  }paint.setAlpha(255);
 }
}
