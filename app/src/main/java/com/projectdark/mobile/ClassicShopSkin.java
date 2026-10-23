package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.nio.charset.Charset;
import java.util.*;

/** Original Setoa shop/dialogue pixels and Legend bitmap font in the original640x480 control space. */
final class ClassicShopSkin {
 static final float SCALE=1.125f,LEFT=120,SHOP_X=191,SHOP_Y=78;
 private final Map<String,Bitmap> images=new HashMap<>();private final Paint p=new Paint();private final Paint text=new Paint();
 private final Map<Character,Integer> glyphs=new HashMap<>();private final Charset korean=Charset.forName("EUC-KR");
 ClassicShopSkin(Context c){for(String name:new String[]{"nd_mback-0","nd_npcbg-0","nd_talk-0","nd_mtab-0","nd_mtab-1","nd_mcp-0","nd_mcn-0","nd_mprev-0","nd_mnext-0","_nbtn-0","_nbtn-3","_nbtn-6","_nbtn-27","dlgback2-0","font-ko","font-en"})try(java.io.InputStream in=c.getAssets().open("town-v247/"+name+".png")){BitmapFactory.Options opt=new BitmapFactory.Options();opt.inScaled=false;images.put(name,BitmapFactory.decodeStream(in,null,opt));}catch(java.io.IOException e){throw new IllegalStateException("Missing original shop asset "+name,e);}p.setFilterBitmap(false);text.setFilterBitmap(false);}
 void begin(Canvas c,float offset){c.save();c.translate(offset+LEFT,0);c.scale(SCALE,SCALE);}
 void end(Canvas c){c.restore();}
 static float x(float x,float offset){return (x-offset-LEFT)/SCALE;}static float y(float y){return y/SCALE;}
 void image(Canvas c,String key,float x,float y){c.drawBitmap(images.get(key),x,y,p);}
 void image(Canvas c,String key,RectF dst){c.drawBitmap(images.get(key),null,dst,p);}
 void rect(Canvas c,float x,float y,float r,float b,int color){p.setColor(color);c.drawRect(x,y,r,b,p);p.setColor(Color.WHITE);}
 private int glyph(char ch){Integer old=glyphs.get(ch);if(old!=null)return old;byte[] b=String.valueOf(ch).getBytes(korean);int i=-1;if(b.length==2){int a=b[0]&255,t=b[1]&255;if(a>=176&&a<=200&&t>=161&&t<=254)i=(a-176)*94+t-161;else if(a==164&&t>=161&&t<=211)i=2350+t-161;}glyphs.put(ch,i);return i;}
 int width(String s){int w=0;for(int i=0;i<s.length();i++)w+=s.charAt(i)<128?6:12;return w;}
 void label(Canvas c,String s,float x,float y,int color){text.setColorFilter(new PorterDuffColorFilter(color,PorterDuff.Mode.SRC_IN));for(int j=0;j<s.length();j++){char ch=s.charAt(j);boolean ascii=ch<128;int i=ascii?ch-33:glyph(ch),w=ascii?8:16;if(ch!=' '&&i>=0){Rect src=new Rect(i%32*w,i/32*12,i%32*w+w,i/32*12+12);c.drawBitmap(images.get(ascii?"font-en":"font-ko"),src,new RectF(x,y,x+w,y+12),text);}x+=ascii?6:12;}text.setColorFilter(null);}
 void right(Canvas c,String s,float x,float y,int color){label(c,s,x-width(s),y,color);}
 void wrap(Canvas c,String s,float x,float y,int width,int lines,int color){StringBuilder line=new StringBuilder();int row=0;for(int i=0;i<s.length()&&row<lines;i++){char ch=s.charAt(i);if(ch=='\n'||width(line.toString()+ch)>width){label(c,line.toString(),x,y+row*14,color);line.setLength(0);row++;if(ch=='\n')continue;}line.append(ch);}if(row<lines)label(c,line.toString(),x,y+row*14,color);}
 void panel(Canvas c,float x,float y,float w,float h){Bitmap b=images.get("nd_talk-0");c.drawBitmap(b,new Rect(0,0,8,8),new RectF(x,y,x+8,y+8),p);c.drawBitmap(b,new Rect(632,0,640,8),new RectF(x+w-8,y,x+w,y+8),p);c.drawBitmap(b,new Rect(0,100,8,108),new RectF(x,y+h-8,x+8,y+h),p);c.drawBitmap(b,new Rect(632,100,640,108),new RectF(x+w-8,y+h-8,x+w,y+h),p);c.drawBitmap(b,new Rect(8,0,632,8),new RectF(x+8,y,x+w-8,y+8),p);c.drawBitmap(b,new Rect(8,100,632,108),new RectF(x+8,y+h-8,x+w-8,y+h),p);c.drawBitmap(b,new Rect(0,8,8,100),new RectF(x,y+8,x+8,y+h-8),p);c.drawBitmap(b,new Rect(632,8,640,100),new RectF(x+w-8,y+8,x+w,y+h-8),p);c.drawBitmap(b,new Rect(8,8,632,100),new RectF(x+8,y+8,x+w-8,y+h-8),p);}
 void localizedFrame(Canvas c){image(c,"nd_mback-0",SHOP_X,SHOP_Y);Bitmap b=images.get("nd_mback-0");for(int y:new int[]{62,86,110,134}){c.drawBitmap(b,new Rect(284,150,317,163),new RectF(SHOP_X+284,SHOP_Y+y,SHOP_X+317,SHOP_Y+y+13),p);}String[] labels={"직업","레벨","무게","기능"};for(int i=0;i<4;i++)label(c,labels[i],SHOP_X+287,SHOP_Y+63+24*i,0xffbcb8a7);c.drawBitmap(b,new Rect(175,225,215,237),new RectF(SHOP_X+216,SHOP_Y+225,SHOP_X+256,SHOP_Y+237),p);}
}
