package com.projectdark.mobile;
import android.graphics.RectF;
/** Logical 960x540 HUD anchors: left stays left, right follows the viewport, resources stay centered. */
final class MobileHudLayout {
  static RectF quest(float rightOffset){return new RectF(720+rightOffset,76,944+rightOffset,199);}
  static RectF itemBar(int index,float rightOffset){float x=372+rightOffset*.5f+index*36;return new RectF(x,438,x+36,474);}
  static RectF potion(int index,float rightOffset){return itemBar(index,rightOffset);}
  static RectF quickItem(int index,float rightOffset){return itemBar(index+2,rightOffset);}
  static RectF status(float rightOffset){return new RectF(350+rightOffset*.5f,476,610+rightOffset*.5f,532);}
  static RectF questRow(int index,float rightOffset){return new RectF(720+rightOffset,76+index*41,944+rightOffset,117+index*41);}
  static RectF chat(boolean expanded){return new RectF(14,expanded?346:493,266,536);}
}
