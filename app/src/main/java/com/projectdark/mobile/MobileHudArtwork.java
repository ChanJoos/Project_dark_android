package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
/** Generated adapted UI illustrations, separate from original world and skill art. */
final class MobileHudArtwork {
 private static Bitmap atlas;
 private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 static void install(Context context){
  if(atlas!=null)return;
  try(java.io.InputStream in=context.getAssets().open("mobile-hud-v136/icons.png")){atlas=BitmapFactory.decodeStream(in);}
  catch(java.io.IOException e){throw new IllegalStateException("Missing approved HUD artwork",e);}
 }
 static boolean icon(Canvas c,String kind,float x,float y,float size,boolean active){
  int i;switch(kind){case "bag":i=0;break;case "stats":i=1;break;case "equipment":i=2;break;case "skill":i=3;break;case "quest":i=4;break;case "attack":i=5;break;default:return false;}
  if(atlas==null)return false;int cell=atlas.getWidth()/7;
  p.setAlpha(active?255:240);c.drawBitmap(atlas,new Rect(i*cell,0,(i+1)*cell,atlas.getHeight()),new RectF(x-size/2,y-size/2,x+size/2,y+size/2),p);p.setAlpha(255);return true;
 }
 static boolean potion(Canvas c,float x,float y,boolean hp){
  if(atlas==null)return false;int cell=atlas.getWidth()/7;
  if(!hp)p.setColorFilter(new ColorMatrixColorFilter(new float[]{0,0,1,0,0, 0,1,0,0,0, 1,0,0,0,0, 0,0,0,1,0}));
  c.drawBitmap(atlas,new Rect(6*cell,0,7*cell,atlas.getHeight()),new RectF(x-13,y-13,x+13,y+13),p);p.setColorFilter(null);return true;
 }

}
