package com.projectdark.mobile;
import android.graphics.Bitmap;
/** Immutable source sprites need one bulk CPU read, never a native pixel read per frame. */
public final class SpriteAlphaMask {
 public final int width,height;
 private final byte[] alpha;
 public SpriteAlphaMask(Bitmap bitmap){
  width=bitmap.getWidth();height=bitmap.getHeight();alpha=new byte[width*height];
  int[] pixels=new int[alpha.length];bitmap.getPixels(pixels,0,width,0,0,width,height);
  for(int i=0;i<pixels.length;i++)alpha[i]=(byte)(pixels[i]>>>24);
 }
 public int at(int x,int y){return x<0||y<0||x>=width||y>=height?0:alpha[y*width+x]&255;}
}
