package com.projectdark.mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * PROJECT DARK v0.50
 * Golden-reference shell: original game fills the screen; mobile controls are overlays.
 * No v0.45 screenshot-tiling renderer is reused here.
 */
public final class GameView extends View {
    private static final int MAP = 40;
    private static final float DESIGN_W = 960f;
    private static final float DESIGN_H = 540f;

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean[][] blocked = new boolean[MAP][MAP];

    private float scale = 1f, ox, oy;
    private float playerX = 19f, playerY = 18f;
    private int facing = 3;
    private boolean running;
    private long lastFrame;

    // Mobile overlay state
    private boolean joystickActive;
    private float joyCx = 92f, joyCy = 448f, joyRadius = 54f;
    private float joyX = 92f, joyY = 448f;
    private int joyDx, joyDy;
    private long nextStep;
    private boolean auto;
    private long autoSuspendUntil;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            long now = SystemClock.uptimeMillis();
            update(now);
            lastFrame = now;
            invalidate();
            postDelayed(this, 16);
        }
    };

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        setKeepScreenOn(true);
        initCollisionPrototype();
    }

    private void initCollisionPrototype() {
        // Temporary collision topology only. It is intentionally NOT a fake Milles map.
        for (int x = 0; x < MAP; x++) {
            blocked[x][0] = true;
            blocked[x][MAP - 1] = true;
        }
        for (int y = 0; y < MAP; y++) {
            blocked[0][y] = true;
            blocked[MAP - 1][y] = true;
        }
        // two test obstacles used only to validate collision before actual Milles tracing
        for (int x = 14; x <= 17; x++) for (int y = 12; y <= 14; y++) blocked[x][y] = true;
        for (int x = 23; x <= 26; x++) for (int y = 21; y <= 24; y++) blocked[x][y] = true;
    }

    public void resume() {
        if (running) return;
        running = true;
        lastFrame = SystemClock.uptimeMillis();
        post(tick);
    }

    public void pause() {
        running = false;
        removeCallbacks(tick);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        scale = Math.min(w / DESIGN_W, h / DESIGN_H);
        ox = (w - DESIGN_W * scale) * 0.5f;
        oy = (h - DESIGN_H * scale) * 0.5f;
    }

    private void update(long now) {
        if (joystickActive && now >= nextStep && (joyDx != 0 || joyDy != 0)) {
            tryMove(joyDx, joyDy);
            nextStep = now + 145;
        }

        // AUTO is deliberately not allowed to drive gameplay until the manual loop is verified.
        // Toggling the button only exposes state in v0.50 shell.
        if (auto && now >= autoSuspendUntil) {
            // Reserved for v0.52 combat/loot controller.
        }
    }

    private boolean walkable(int x, int y) {
        return x >= 0 && y >= 0 && x < MAP && y < MAP && !blocked[x][y];
    }

    private boolean tryMove(int dx, int dy) {
        if (Math.abs(dx) + Math.abs(dy) != 1) return false;
        int nx = Math.round(playerX) + dx;
        int ny = Math.round(playerY) + dy;
        if (!walkable(nx, ny)) return false;
        playerX = nx;
        playerY = ny;
        facing = dx < 0 ? 0 : dy < 0 ? 1 : dy > 0 ? 2 : 3;
        return true;
    }

    // Logical 4-neighbor grid -> original screen-diagonal movement.
    private float worldX(float gx, float gy) { return 480f + (gx - gy) * 25f; }
    private float worldY(float gx, float gy) { return 230f + (gx + gy) * 12.5f; }

    private void fill(int color) { p.setStyle(Paint.Style.FILL); p.setColor(color); }
    private void stroke(int color, float width) { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(width); p.setColor(color); }
    private void text(Canvas c, String s, float x, float y, float size, int color) {
        fill(color); p.setTextSize(size); c.drawText(s, x, y, p);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        canvas.save();
        canvas.translate(ox, oy);
        canvas.scale(scale, scale);

        drawWorld(canvas);
        drawPlayer(canvas);
        drawOriginalStyleHud(canvas);
        drawVirtualJoystick(canvas);
        drawQuickActions(canvas);

        canvas.restore();
    }

    private void drawWorld(Canvas c) {
        // Full-screen world surface. No title bar, no lower control panel, no screenshot mosaic.
        fill(0xff10110f);
        c.drawRect(0, 0, DESIGN_W, DESIGN_H, p);

        // Development-only projection/collision field. Actual Milles visual tracing replaces this layer.
        for (int sum = 0; sum < MAP * 2; sum++) {
            for (int x = 1; x < MAP - 1; x++) {
                int y = sum - x;
                if (y < 1 || y >= MAP - 1) continue;
                float sx = worldX(x, y), sy = worldY(x, y);
                if (sx < -40 || sx > 1000 || sy < -30 || sy > 570) continue;
                Path d = new Path();
                d.moveTo(sx, sy - 12.5f); d.lineTo(sx + 25, sy);
                d.lineTo(sx, sy + 12.5f); d.lineTo(sx - 25, sy); d.close();
                if (blocked[x][y]) fill(0xff1d211c); else fill(((x + y) & 1) == 0 ? 0xff272b24 : 0xff242820);
                c.drawPath(d, p);
                stroke(0x181e231d, 1f); c.drawPath(d, p);
            }
        }

        text(c, "Milles visual trace pending - gameplay shell test", 340, 510, 10, 0x66ffffff);
    }

    private void drawPlayer(Canvas c) {
        float sx = worldX(playerX, playerY), sy = worldY(playerX, playerY);
        // Clear development placeholder; never presented as original sprite.
        fill(0x55000000); c.drawOval(new RectF(sx - 13, sy - 5, sx + 13, sy + 5), p);
        fill(0xffd8c8a5); c.drawCircle(sx, sy - 28, 7, p);
        fill(0xffe9e6dd); c.drawRoundRect(new RectF(sx - 8, sy - 22, sx + 8, sy - 5), 3, 3, p);
        fill(0xff4b4135);
        float fx = facing == 0 ? -6 : facing == 3 ? 6 : 0;
        float fy = facing == 1 ? -3 : facing == 2 ? 3 : 0;
        c.drawCircle(sx + fx, sy - 28 + fy, 2, p);
        text(c, "PLAYER DEV", sx - 26, sy - 39, 9, 0xffe6d3a4);
    }

    private void drawOriginalStyleHud(Canvas c) {
        // Party list, top-left. Compact overlay instead of a huge box.
        panel(c, 12, 14, 138, 80, 0x99100f0d);
        text(c, "GROUP", 20, 29, 11, 0xffd8c394);
        partyRow(c, 22, 48, "모험가 Lv.1", 1.0f, 1.0f);
        partyRow(c, 22, 69, "동료", .82f, .65f);

        // Quest tracker
        panel(c, 160, 14, 150, 54, 0x88100f0d);
        text(c, "QUEST", 169, 29, 11, 0xffd8c394);
        text(c, "밀레스의 첫걸음 0/4", 169, 49, 11, 0xffe3dfd4);

        // Target bar - hidden content but fixed canonical position.
        panel(c, 340, 14, 286, 36, 0x77100f0d);
        text(c, "TARGET", 350, 29, 9, 0xffa89b7d);
        fill(0xff321816); c.drawRect(395, 22, 610, 30, p);

        // Minimap shell and coordinates
        panel(c, 774, 12, 172, 118, 0xaa0d0d0c);
        text(c, "MILLES", 785, 29, 10, 0xffd8c394);
        fill(0xff171a17); c.drawRect(786, 36, 934, 104, p);
        stroke(0xff62543c, 1); c.drawRect(786, 36, 934, 104, p);
        text(c, "X:" + Math.round(playerX) + "  Y:" + Math.round(playerY), 786, 121, 9, 0xffc4bda9);

        // Right vertical original-style utility rail
        String[] rail = {"≡", "I", "Q", "G", "S", "✦"};
        for (int i = 0; i < rail.length; i++) iconButton(c, rail[i], 908, 144 + i * 42, 36, 36);

        // Chat area: translucent and anchored to lower-left.
        panel(c, 15, 336, 325, 96, 0x66000000);
        text(c, "[일반] 밀레스에 도착했습니다.", 26, 360, 10, 0xffded7c3);
        text(c, "[안내] 원작 HUD 구조 + 모바일 입력 Overlay", 26, 379, 10, 0xffbdb6a4);
        text(c, "[안내] v0.50 이동/충돌 검증", 26, 398, 10, 0xffaaa28f);

        // Bottom-center status bars
        panel(c, 355, 465, 258, 62, 0xaa0c0c0b);
        text(c, "LV 1", 365, 482, 11, 0xffded2b0);
        bar(c, 407, 473, 194, 10, 1f, 0xffa22d2b);
        bar(c, 407, 488, 194, 10, 1f, 0xff3268a8);
        bar(c, 407, 503, 194, 10, 0.02f, 0xff4f9a52);
        text(c, "HP", 380, 481, 8, 0xffddd5c2);
        text(c, "MP", 380, 496, 8, 0xffddd5c2);
        text(c, "EXP", 378, 511, 8, 0xffddd5c2);

        // Unified slot grid, original identity preserved; touch target sizing adapted.
        float startX = 624, startY = 412, cell = 38;
        for (int r = 0; r < 3; r++) {
            for (int col = 0; col < 7; col++) {
                float x = startX + col * cell, y = startY + r * cell;
                fill(0xaa6e5636); c.drawRect(x, y, x + 34, y + 34, p);
                stroke(0xffa68b5b, 1); c.drawRect(x, y, x + 34, y + 34, p);
                if (r == 0) text(c, String.valueOf(col + 1), x + 4, y + 10, 7, 0xffd8c8a0);
            }
        }
    }

    private void partyRow(Canvas c, float x, float y, String name, float hp, float mp) {
        text(c, name, x, y, 9, 0xffe3dfd4);
        bar(c, x, y + 4, 112, 5, hp, 0xffb82f39);
        bar(c, x, y + 10, 112, 4, mp, 0xff3f73b8);
    }

    private void bar(Canvas c, float x, float y, float w, float h, float value, int color) {
        fill(0xaa181818); c.drawRect(x, y, x + w, y + h, p);
        fill(color); c.drawRect(x, y, x + w * Math.max(0, Math.min(1, value)), y + h, p);
    }

    private void panel(Canvas c, float x, float y, float w, float h, int bg) {
        fill(bg); c.drawRect(x, y, x + w, y + h, p);
        stroke(0xff746044, 1.2f); c.drawRect(x, y, x + w, y + h, p);
    }

    private void iconButton(Canvas c, String label, float x, float y, float w, float h) {
        fill(0x99131312); c.drawRect(x, y, x + w, y + h, p);
        stroke(0xff6f5e42, 1); c.drawRect(x, y, x + w, y + h, p);
        text(c, label, x + 12, y + 23, 12, 0xffe0d5b5);
    }

    private void drawVirtualJoystick(Canvas c) {
        // Standard mobile circular virtual joystick overlay.
        fill(0x45111111); c.drawCircle(joyCx, joyCy, joyRadius, p);
        stroke(0x887b735f, 2); c.drawCircle(joyCx, joyCy, joyRadius, p);
        fill(0x88736a58); c.drawCircle(joyX, joyY, 23, p);
        stroke(0xb0c4b58e, 2); c.drawCircle(joyX, joyY, 23, p);
    }

    private void drawQuickActions(Canvas c) {
        // Small overlays only; they do not replace the unified slots.
        roundButton(c, "ATK", 855, 446, 36);
        roundButton(c, auto ? "AUTO" : "A", 900, 489, 31);
        // Element quick swap indicator: compact, not six permanent big buttons.
        panel(c, 751, 492, 88, 31, 0x9910100f);
        text(c, "속성  水", 765, 512, 10, 0xffe3d3a5);
    }

    private void roundButton(Canvas c, String label, float cx, float cy, float r) {
        fill(0x991a1815); c.drawCircle(cx, cy, r, p);
        stroke(0xff8b7248, 1.5f); c.drawCircle(cx, cy, r, p);
        text(c, label, cx - r * .45f, cy + 4, 10, 0xffeadfbd);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x = (e.getX() - ox) / scale;
        float y = (e.getY() - oy) / scale;
        int action = e.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (distance(x, y, joyCx, joyCy) <= joyRadius * 1.35f) {
                joystickActive = true;
                autoSuspendUntil = SystemClock.uptimeMillis() + 1200;
                updateJoystick(x, y);
                nextStep = 0;
                return true;
            }
            if (distance(x, y, 900, 489) <= 38) {
                auto = !auto;
                invalidate();
                return true;
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE && joystickActive) {
            autoSuspendUntil = SystemClock.uptimeMillis() + 1200;
            updateJoystick(x, y);
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            joystickActive = false;
            joyX = joyCx; joyY = joyCy;
            joyDx = joyDy = 0;
            invalidate();
            return true;
        }
        return true;
    }

    private void updateJoystick(float x, float y) {
        float dx = x - joyCx, dy = y - joyCy;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len > joyRadius && len > 0) {
            dx = dx / len * joyRadius;
            dy = dy / len * joyRadius;
        }
        joyX = joyCx + dx;
        joyY = joyCy + dy;

        // Screen joystick direction -> logical 4-neighbor direction.
        // screen up-left = logical -X, up-right = logical -Y,
        // screen down-left = logical +Y, down-right = logical +X.
        if (len < 12) { joyDx = joyDy = 0; return; }
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx < 0) { joyDx = -1; joyDy = 0; }
            else { joyDx = 1; joyDy = 0; }
        } else {
            if (dy < 0) { joyDx = 0; joyDy = -1; }
            else { joyDx = 0; joyDy = 1; }
        }
        invalidate();
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
}
