package com.projectdark.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.io.InputStream;
import java.net.URL;

/** PROJECT DARK v0.51
 * Visual-first rebuild. v0.45/v0.50 tile-placeholder renderer is not reused.
 * Base layer: verified Nexon Milles screenshot.
 * UI: original screenshot UI remains visible; mobile controls are overlays only.
 * Character: extracted from the original screen and rendered as a movable entity.
 */
public final class GameView extends View {
    private static final float W = 960f, H = 540f;
    private static final String MILLES_URL =
            "https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pixel = new Paint();
    private Bitmap milles;
    private Bitmap playerSprite;
    private float scale = 1f, ox, oy;

    // Screen-space prototype movement. Map-grid tracing comes next.
    private float playerX = 480f, playerY = 300f;
    private boolean running;
    private boolean joystickActive;
    private float joyCx = 92f, joyCy = 445f, joyR = 58f;
    private float joyX = joyCx, joyY = joyCy;
    private float vx, vy;
    private long last;

    private final Runnable loop = new Runnable() {
        @Override public void run() {
            if (!running) return;
            long now = SystemClock.uptimeMillis();
            float dt = Math.min(0.05f, (now - last) / 1000f);
            last = now;
            update(dt);
            invalidate();
            postDelayed(this, 16);
        }
    };

    public GameView(Context context) {
        super(context);
        pixel.setFilterBitmap(false);
        setKeepScreenOn(true);
        loadMilles();
    }

    private void loadMilles() {
        new Thread(() -> {
            try (InputStream in = new URL(MILLES_URL).openStream()) {
                Bitmap b = BitmapFactory.decodeStream(in);
                if (b != null) {
                    milles = b;
                    buildPlayerSprite(b);
                    postInvalidate();
                }
            } catch (Exception ignored) {
                postInvalidate();
            }
        }).start();
    }

    private void buildPlayerSprite(Bitmap src) {
        // Original gameplay screenshots conventionally keep the local player near center.
        // Keep this crop isolated so it can be replaced by verified sprite-sheet frames later.
        int sw = src.getWidth(), sh = src.getHeight();
        int cw = Math.max(28, Math.min(72, sw / 18));
        int ch = Math.max(48, Math.min(110, sh / 7));
        int cx = sw / 2;
        int cy = (int)(sh * 0.52f);
        int left = Math.max(0, Math.min(sw - cw, cx - cw / 2));
        int top = Math.max(0, Math.min(sh - ch, cy - ch / 2));
        playerSprite = Bitmap.createBitmap(src, left, top, cw, ch);
    }

    public void resume() {
        if (running) return;
        running = true;
        last = SystemClock.uptimeMillis();
        post(loop);
    }

    public void pause() {
        running = false;
        removeCallbacks(loop);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        scale = Math.min(w / W, h / H);
        ox = (w - W * scale) * .5f;
        oy = (h - H * scale) * .5f;
    }

    private void update(float dt) {
        if (!joystickActive) return;
        float speed = 150f;
        playerX += vx * speed * dt;
        playerY += vy * speed * dt;
        playerX = Math.max(45, Math.min(W - 45, playerX));
        playerY = Math.max(75, Math.min(H - 45, playerY));
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawColor(Color.BLACK);
        c.save();
        c.translate(ox, oy);
        c.scale(scale, scale);

        drawMilles(c);
        drawMovableCharacter(c);
        drawMobileOverlay(c);

        c.restore();
    }

    private void drawMilles(Canvas c) {
        if (milles == null) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xff080808);
            c.drawRect(0, 0, W, H, paint);
            paint.setTextSize(18);
            paint.setColor(0xffd6c18c);
            c.drawText("밀레스 원본 화면 불러오는 중...", 335, 270, paint);
            return;
        }

        // Center-crop to full 16:9 field. No fake tile repetition.
        int sw = milles.getWidth(), sh = milles.getHeight();
        float dstAspect = W / H;
        float srcAspect = sw / (float)sh;
        Rect src;
        if (srcAspect > dstAspect) {
            int useW = Math.round(sh * dstAspect);
            int left = (sw - useW) / 2;
            src = new Rect(left, 0, left + useW, sh);
        } else {
            int useH = Math.round(sw / dstAspect);
            int top = (sh - useH) / 2;
            src = new Rect(0, top, sw, top + useH);
        }
        c.drawBitmap(milles, src, new RectF(0, 0, W, H), pixel);
    }

    private void drawMovableCharacter(Canvas c) {
        if (playerSprite == null) return;
        float h = 72f;
        float w = h * playerSprite.getWidth() / (float)playerSprite.getHeight();
        RectF dst = new RectF(playerX - w/2, playerY - h, playerX + w/2, playerY);

        paint.setColor(0x66000000);
        paint.setStyle(Paint.Style.FILL);
        c.drawOval(new RectF(playerX - 16, playerY - 6, playerX + 16, playerY + 5), paint);
        c.drawBitmap(playerSprite, null, dst, pixel);
    }

    private void drawMobileOverlay(Canvas c) {
        // Only adaptations required for touch are drawn over the original game screen.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x33101010);
        c.drawCircle(joyCx, joyCy, joyR, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(0x99d7c191);
        c.drawCircle(joyCx, joyCy, joyR, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x88776c55);
        c.drawCircle(joyX, joyY, 23f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xffd7c191);
        c.drawCircle(joyX, joyY, 23f, paint);

        // Compact touch actions; original unified-slot UI remains visible underneath.
        roundButton(c, 858, 460, 34, "ATK");
        roundButton(c, 914, 500, 28, "AUTO");
    }

    private void roundButton(Canvas c, float x, float y, float r, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x6612110f);
        c.drawCircle(x, y, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(0xffa78d59);
        c.drawCircle(x, y, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(label.length() > 3 ? 8 : 11);
        paint.setColor(0xffeee0ba);
        float tw = paint.measureText(label);
        c.drawText(label, x - tw/2, y + 4, paint);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x = (e.getX() - ox) / scale;
        float y = (e.getY() - oy) / scale;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (distance(x, y, joyCx, joyCy) <= joyR * 1.4f) {
                    joystickActive = true;
                    updateJoystick(x, y);
                    return true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystickActive) updateJoystick(x, y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                joystickActive = false;
                joyX = joyCx; joyY = joyCy;
                vx = vy = 0;
                return true;
            default:
                return true;
        }
    }

    private void updateJoystick(float x, float y) {
        float dx = x - joyCx, dy = y - joyCy;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len > joyR) {
            dx = dx / len * joyR;
            dy = dy / len * joyR;
            len = joyR;
        }
        joyX = joyCx + dx;
        joyY = joyCy + dy;
        if (len < 8f) { vx = vy = 0; return; }

        // Original movement appearance is diagonal. Quantize a normal circular stick
        // into the four original screen diagonals.
        float nx = dx / Math.max(1f, len);
        float ny = dy / Math.max(1f, len);
        if (Math.abs(nx) > Math.abs(ny)) {
            if (nx > 0) { vx = .707f; vy = .707f; }
            else { vx = -.707f; vy = -.707f; }
        } else {
            if (ny > 0) { vx = -.707f; vy = .707f; }
            else { vx = .707f; vy = -.707f; }
        }
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
}
