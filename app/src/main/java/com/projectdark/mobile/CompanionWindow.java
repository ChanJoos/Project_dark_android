package com.projectdark.mobile;

import android.graphics.*;

/** Presentation/input only. Domain mutations and durable commands stay outside this window. */
final class CompanionWindow {
  enum Command {
    NONE,
    SELECT_ALLY,
    SELECT_SELF,
    WARRIOR,
    PRIEST,
    ROGUE,
    MAGE,
    MONK,
    DISMISS,
    FOLLOW,
    FOCUS,
    WAIT,
    RECOVER
  }

  boolean open;
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  static RectF hud() {
    return new RectF(18, 193, 166, 245);
  }

  void drawHud(Canvas c, RuntimeState s) {
    RectF r = hud();
    UiTheme.surface(c, r, 0xe5101722, 0x88607485, 5);
    buttonCompact(c, 260, 230, "도적 동료");
    buttonCompact(c, 414, 230, "마법사 동료");
    buttonCompact(c, 568, 230, "무도가 동료");
    CompanionParty.Member m = s.party().active();
    UiTheme.text(
        c,
        m == null
            ? "동료 · 눌러서 관리"
            : m.name + " · Lv" + m.level + (s.party().testing() ? " 테스트" : ""),
        r.left + 7,
        r.top + 13,
        9,
        UiTheme.TEXT,
        true);
    if (m == null) {
      UiTheme.text(
          c,
          s.party().testing() ? "Lv99 테스트 동료 선택" : s.party().unlocked(s) ? "마을에서 동행 선택" : "전직 후 이용",
          r.left + 7,
          r.top + 32,
          8,
          UiTheme.MUTED,
          false);
      return;
    }
    meter(
        c, r.left + 7, r.top + 19, r.width() - 14, 5, m.body.hp / (float) m.body.maxHp, 0xffce5058);
    meter(
        c,
        r.left + 7,
        r.top + 27,
        r.width() - 14,
        4,
        m.body.mp / (float) Math.max(1, m.body.maxMp),
        0xff557ece);
    UiTheme.text(c, m.notice, r.left + 7, r.top + 44, 7.5f, UiTheme.MUTED, false);
  }

  private void meter(Canvas c, float x, float y, float width, float h, float q, int color) {
    paint.setColor(0xff252b34);
    c.drawRect(x, y, x + width, y + h, paint);
    paint.setColor(color);
    c.drawRect(x, y, x + width * Math.max(0, Math.min(1, q)), y + h, paint);
  }

  void draw(Canvas c, RuntimeState s, float offset) {
    if (!open) return;
    c.save();
    c.translate(offset, 0);
    UiTheme.surface(c, new RectF(240, 118, 720, 420), UiTheme.BG, UiTheme.LINE, 10);
    UiTheme.text(c, "동료 관리", 260, 145, 15, UiTheme.TEXT, true);
    UiTheme.text(c, "닫기", 666, 145, 10, UiTheme.MUTED, false);
    UiTheme.text(
        c,
        s.party().testing() ? "임시 테스트 · Lv99 · 직업별 모든 기술 습득" : "동행 변경·회복은 마을에서 가능합니다",
        260,
        168,
        10,
        UiTheme.MUTED,
        false);
    button(c, 260, 185, "전사 동료");
    button(c, 414, 185, "성직자 동료");
    button(c, 568, 185, s.party().testing() ? "동행 해제" : "마을 대기");
    buttonCompact(c, 260, 230, "도적 동료");
    buttonCompact(c, 414, 230, "마법사 동료");
    buttonCompact(c, 568, 230, "무도가 동료");
    CompanionParty.Member m = s.party().active();
    UiTheme.text(
        c,
        m == null
            ? "동행 중인 동료가 없습니다"
            : m.name + " · HP " + m.body.hp + " / " + m.body.maxHp + " · MP " + m.body.mp,
        260,
        379,
        10,
        UiTheme.TEXT,
        false);
    button(c, 260, 262, "동행");
    button(c, 414, 262, "선택한 적 집중");
    button(c, 568, 262, "제자리 대기");
    button(c, 260, 322, "동료 지원 선택");
    button(c, 414, 322, "자신 선택");
    button(c, 568, 322, s.party().testing() ? "테스트 회복" : "마을에서 회복");
    UiTheme.text(c, "회복·보호 기술은 선택한 아군에게 사용합니다", 260, 399, 9, UiTheme.MUTED, false);
    c.restore();
  }

  private void button(Canvas c, float x, float y, String label) {
    UiTheme.surface(c, new RectF(x, y, x + 132, y + 39), 0xff273340, UiTheme.LINE, 4);
    UiTheme.center(c, label, x + 66, y + 24, 10, UiTheme.TEXT, true);
  }

  private void buttonCompact(Canvas c, float x, float y, String label) {
    UiTheme.surface(c, new RectF(x, y, x + 132, y + 27), 0xff273340, UiTheme.LINE, 4);
    UiTheme.center(c, label, x + 66, y + 18, 10, UiTheme.TEXT, true);
  }

  Command touch(float x, float y, float offset) {
    if (!open) {
      if (hud().contains(x, y)) {
        open = true;
        return Command.NONE;
      }
      return null;
    }
    x -= offset;
    if (x >= 650 && y < 164 || x < 240 || x > 720 || y < 118 || y > 420) {
      open = false;
      return Command.NONE;
    }
    int row = y >= 185 && y <= 224 ? 0 : y >= 262 && y <= 301 ? 1 : y >= 322 && y <= 361 ? 2 : -1;
    int col = x >= 260 && x <= 392 ? 0 : x >= 414 && x <= 546 ? 1 : x >= 568 && x <= 700 ? 2 : -1;
    if (y >= 230 && y <= 257 && col >= 0)
      return new Command[] {Command.ROGUE, Command.MAGE, Command.MONK}[col];
    if (row < 0 || col < 0) return Command.NONE;
    Command[][] commands = {
      {Command.WARRIOR, Command.PRIEST, Command.DISMISS},
      {Command.FOLLOW, Command.FOCUS, Command.WAIT},
      {Command.SELECT_ALLY, Command.SELECT_SELF, Command.RECOVER}
    };
    Command command = commands[row][col];
    if (command == Command.SELECT_ALLY || command == Command.SELECT_SELF) open = false;
    return command;
  }
}
