package com.projectdark.mobile;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PROJECT DARK prototype runtime state.
 *
 * Evidence policy:
 * - [B] Coordinates, collision geometry and NPC placement here are prototype reconstruction values.
 * - [ADAPTED] Screen-space collision/interaction exists to validate mobile controls before a verified tile map is available.
 * - No entry in this file should be interpreted as an original LOD server/map value.
 */
public final class RuntimeState {
  public static final float WORLD_MIN_X = 180f;
  public static final float WORLD_MAX_X = 760f;
  public static final float WORLD_MIN_Y = 120f;
  public static final float WORLD_MAX_Y = 455f;
  public static final float PLAYER_RADIUS = 9f;

  public static final class Player {
    public float x = 480f;
    public float y = 300f;
    public int hp = 100;
    public int maxHp = 100;
    public int mp = 90;
    public int maxMp = 100;
  }

  public static final class Npc {
    public final String id;
    public final String name;
    public final float x;
    public final float y;
    public final String dialogue;

    Npc(String id, String name, float x, float y, String dialogue) {
      this.id = id;
      this.name = name;
      this.x = x;
      this.y = y;
      this.dialogue = dialogue;
    }
  }

  private final Player player = new Player();
  private final List<RectF> obstacles = new ArrayList<>();
  private final List<Npc> npcs = new ArrayList<>();

  public RuntimeState() {
    // [B] Screen-space blockers chosen to exercise collision against visible rocky structures.
    obstacles.add(new RectF(272f, 170f, 342f, 238f));
    obstacles.add(new RectF(600f, 150f, 684f, 218f));
    obstacles.add(new RectF(312f, 350f, 374f, 430f));
    obstacles.add(new RectF(620f, 332f, 700f, 420f));

    // [B]/PENDING_CROP: placeholder NPC geometry only; not claimed to be an identified original NPC sprite.
    npcs.add(new Npc(
        "milles_guide_proto",
        "밀레스 안내인 [B]",
        555f,
        248f,
        "밀레스에 온 것을 환영합니다. 이 대화는 모바일 접근/대화 루프 검증용 프로토타입입니다."
    ));
  }

  public Player player() {
    return player;
  }

  public List<RectF> obstacles() {
    return Collections.unmodifiableList(obstacles);
  }

  public List<Npc> npcs() {
    return Collections.unmodifiableList(npcs);
  }

  public boolean tryMove(float dx, float dy) {
    float nx = clamp(player.x + dx, WORLD_MIN_X, WORLD_MAX_X);
    float ny = clamp(player.y + dy, WORLD_MIN_Y, WORLD_MAX_Y);

    // Axis-separated resolution makes sliding along a blocker possible instead of hard-stopping both axes.
    if (!blocked(nx, player.y)) {
      player.x = nx;
    }
    if (!blocked(player.x, ny)) {
      player.y = ny;
    }
    return player.x == nx && player.y == ny;
  }

  public boolean blocked(float x, float y) {
    for (RectF r : obstacles) {
      if (x + PLAYER_RADIUS > r.left && x - PLAYER_RADIUS < r.right
          && y + PLAYER_RADIUS > r.top && y - PLAYER_RADIUS < r.bottom) {
        return true;
      }
    }
    return false;
  }

  public Npc hitNpc(float x, float y, float radius) {
    for (Npc npc : npcs) {
      float dx = x - npc.x;
      float dy = y - npc.y;
      if (dx * dx + dy * dy <= radius * radius) {
        return npc;
      }
    }
    return null;
  }

  public float distanceTo(Npc npc) {
    float dx = player.x - npc.x;
    float dy = player.y - npc.y;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }
}
