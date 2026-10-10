package com.projectdark.mobile;
import android.graphics.RectF;
/** Logical 960x540 HUD anchors: left stays left, right follows the viewport, resources stay centered. */
final class MobileHudLayout {
  static RectF quest(float rightOffset){return new RectF(720+rightOffset,76,944+rightOffset,247);}
  static RectF potion(int index,float rightOffset){float x=438+rightOffset*.5f+index*48;return new RectF(x,378,x+36,418);}
  static RectF status(float rightOffset){return new RectF(355+rightOffset*.5f,424,605+rightOffset*.5f,536);}
  static RectF questRow(int index,float rightOffset){return new RectF(720+rightOffset,76+index*57,944+rightOffset,133+index*57);}
  static RectF chat(boolean expanded){return new RectF(14,expanded?346:493,266,536);}
}
