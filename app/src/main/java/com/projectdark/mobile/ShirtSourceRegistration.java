package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.Rect;

/** Exact mu0000001 packed rectangles and registration relative to matching mm001 BODY frames.
 * Source: recovered lod-dressup data/type/{body/mm001,armor/mu0000001}.json.
 * py describes a bottom offset: source top is py*height-height, not py*height.
 * Runtime maps these BODY-local coordinates through the accepted BODY presentation transform.
 */
final class ShirtSourceRegistration {
  static final int[] BODY_WIDTH={18,21,21,27,17,25,24,27,18,26};
  static final int[] BODY_HEIGHT={51,51,51,52,50,49,48,54,51,51};
  private static final int[] IDLE_SX={10,21,33,45,234,245,57,70,83,96};
  private static final int[] IDLE_W={10,11,11,11,10,13,12,12,12,14};
  private static final int[] IDLE_H={17,17,17,17,16,16,17,17,17,17};
  private static final int[] BODY_X={9,8,9,12,9,12,11,12,9,12};
  private static final int[] BODY_Y={8,9,8,6,9,9,12,5,9,8};
  private static final int[] SHIRT_X={6,4,6,6,6,7,5,5,4,6};
  private static final int[] SHIRT_Y={31,31,31,30,32,31,31,30,31,30};
  private static final int[] ACTION_SX={0,259,273,284},ACTION_W={9,13,10,13},ACTION_H={18,16,16,16};
  private static final int[] ACTION_DX={3,3,2,4},ACTION_DY={11,13,12,9};
  private ShirtSourceRegistration(){}
  static int idleFrame(CharacterRenderer.Direction direction,int column){
    int[] frames=(direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE)?new int[]{0,2,3,4,5}:new int[]{1,6,7,8,9};
    return frames[Math.max(0,Math.min(4,column))];
  }
  static Rect idleRect(int frame){return new Rect(IDLE_SX[frame],0,IDLE_SX[frame]+IDLE_W[frame],IDLE_H[frame]);}
  static float idleX(int frame){return BODY_X[frame]-SHIRT_X[frame];}
  static float idleY(int frame){return BODY_HEIGHT[frame]+BODY_Y[frame]-IDLE_H[frame]-SHIRT_Y[frame];}
  static float actionX(int frame){return ACTION_DX[frame];}
  static float actionY(int frame){return ACTION_DY[frame];}
  static Bitmap actionFrame(Bitmap source,int frame){
    if(source==null||source.getWidth()!=298||source.getHeight()!=19)return null;
    return Bitmap.createBitmap(source,ACTION_SX[frame],0,ACTION_W[frame],ACTION_H[frame]);
  }
}
