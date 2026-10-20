package com.projectdark.mobile;
import android.graphics.*;
/** User-requested display projection: handle lower-left, long axis upper-right in both item windows.
 * Source assets/receipts and equipped actor animation are never modified. */
final class WeaponInventoryArtworkV209 {
 static double[] moments(Bitmap b){double mass=0,x=0,y=0,xx=0,yy=0,xy=0;for(int j=0;j<b.getHeight();j++)for(int i=0;i<b.getWidth();i++){int a=Color.alpha(b.getPixel(i,j));if(a<16)continue;double w=a/255.;mass+=w;x+=i*w;y+=j*w;xx+=i*i*w;yy+=j*j*w;xy+=i*j*w;}if(mass==0)return new double[]{0,0,0};return new double[]{xx/mass-x*x/mass/mass,yy/mass-y*y/mass/mass,xy/mass-x*y/mass/mass};}
 static float axisDegrees(Bitmap b){double[] q=moments(b);return (float)Math.toDegrees(.5*Math.atan2(2*q[2],q[0]-q[1]));}
 static float elongation(Bitmap b){double[] q=moments(b);double d=Math.hypot(q[0]-q[1],2*q[2]);return (float)((q[0]+q[1]+d)/Math.max(.01,q[0]+q[1]-d));}
 static Bitmap diagonal(Bitmap source){
  Rect bounds=SourceItemIconRegistry.visibleEquipmentBounds(source);if(bounds.isEmpty())return source;
  Bitmap crop=Bitmap.createBitmap(source,bounds.left,bounds.top,bounds.width(),bounds.height());float angle=-45-axisDegrees(crop);while(angle>90)angle-=180;while(angle< -90)angle+=180;
  Matrix turn=new Matrix();turn.postRotate(Math.abs(angle)<3?0:angle);Bitmap rotated=Bitmap.createBitmap(crop,0,0,crop.getWidth(),crop.getHeight(),turn,false);Rect visible=SourceItemIconRegistry.visibleEquipmentBounds(rotated);
  Bitmap icon=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(icon);Paint p=new Paint();p.setFilterBitmap(false);p.setAntiAlias(false);float scale=Math.min(1f,Math.min(28f/visible.width(),28f/visible.height()));float w=visible.width()*scale,h=visible.height()*scale;c.drawBitmap(rotated,visible,new RectF(16-w/2,16-h/2,16+w/2,16+h/2),p);icon.prepareToDraw();return icon;
 }
 private WeaponInventoryArtworkV209(){}
}
