package com.projectdark.mobile.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.projectdark.mobile.RuntimeState;

/** Visible geometry QA only, not a reconstruction of the original Milles map or final art. */
public final class PrototypeWorldRenderer {
  private final Paint paint = new Paint();
  public void draw(Canvas canvas, RuntimeState state) {
    canvas.drawColor(0xff171b20);
    paint.setColor(0xff29313a);
    paint.setStrokeWidth(1);
    for (int x = -540; x <= 1500; x += 48) {
      canvas.drawLine(x, 0, x + 540, 540, paint);
      canvas.drawLine(x, 0, x - 540, 540, paint);
    }
    for (RectF blocker : state.obstacles()) {
      paint.setColor(0xff40464b);
      canvas.drawRect(blocker, paint);
      paint.setStyle(Paint.Style.STROKE);
      paint.setColor(0xff89919b);
      canvas.drawRect(blocker, paint);
      paint.setStyle(Paint.Style.FILL);
    }
    paint.setColor(0xffb8c1cc);
    paint.setTextSize(12);
    canvas.drawText("지형·전투 검증 화면 · 원작 맵/스프라이트 미적용", 320, 90, paint);
  }
}
