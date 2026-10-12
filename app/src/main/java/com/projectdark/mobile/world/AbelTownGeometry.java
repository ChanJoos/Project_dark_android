package com.projectdark.mobile.world;

import android.graphics.RectF;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/* loaded from: classes3.dex */
public final class AbelTownGeometry {
    public static final List<Building> BUILDINGS = Collections.unmodifiableList(Arrays.asList(new Building("abel_church", "church", "아벨 교회", "abel_interior_church", 7, 6, 5, 5, 244.0f), new Building("abel_inn", "inn", "아벨 여관", "abel_interior_inn", 17, 5, 5, 4, 226.0f), new Building("abel_bank", "bank", "아벨 은행", "abel_interior_bank", 31, 7, 5, 5, 244.0f), new Building("abel_reagent", "house", "아벨 시약상점", "abel_interior_reagent", 6, 18, 4, 4, 200.0f), new Building("abel_equipment", "forge", "아벨 무기상점", "abel_interior_equipment", 6, 30, 4, 4, 210.0f), new Building("abel_home_1", "house", "주택", null, 29, 15, 4, 4, 180.0f), new Building("abel_home_2", "inn", "주택", null, 35, 24, 4, 4, 200.0f), new Building("abel_home_3", "house", "주택", null, 33, 34, 4, 4, 180.0f), new Building("abel_home_4", "house", "주택", null, 16, 35, 4, 4, 180.0f)));
    public static final String ID = "MAP_ABEL_TOWN";
    public static final List<Prop> PROPS;
    public static final int SIZE = 44;

    public static float x(int u, int v) {
        return ((u - v) * 32) + 1536;
    }

    public static float y(int u, int v) {
        return ((u + v) * 16) + 96;
    }

    public static final class Building {
        public final String asset;
        public final int du;
        public final int dv;
        public final String id;
        public final String interior;
        public final String title;
        public final int u;
        public final int v;
        public final float width;

        Building(String id, String asset, String title, String interior, int u, int v, int du, int dv, float width) {
            this.id = id;
            this.asset = asset;
            this.title = title;
            this.interior = interior;
            this.u = u;
            this.v = v;
            this.du = du;
            this.dv = dv;
            this.width = width;
        }

        public float x() {
            return AbelTownGeometry.x((this.u + this.du) - 1, (this.v + this.dv) - 1);
        }

        public float y() {
            return AbelTownGeometry.y((this.u + this.du) - 1, (this.v + this.dv) - 1);
        }

        public float doorX() {
            return AbelTownGeometry.x((this.u + this.du) - 3, (this.v + this.dv) - 1);
        }

        public float doorY() {
            return AbelTownGeometry.y((this.u + this.du) - 3, (this.v + this.dv) - 1);
        }

        boolean occupies(int a, int b) {
            return a >= this.u && a < this.u + this.du && b >= this.v && b < this.v + this.dv;
        }
    }

    static {
        List<Prop> p = new ArrayList<>();
        p.add(new Prop("fountain", 23, 22, 80.0f, false));
        int k = 20;
        while (k <= 24) {
            p.add(new Prop("bench_sw", k, 17, 56.0f, false));
            p.add(new Prop("bench_ne", k, 26, 56.0f, false));
            int k2 = k;
            p.add(new Prop("bench_se", 18, k2, 56.0f, false));
            p.add(new Prop("bench_nw", 27, k2, 56.0f, false));
            k = k2 + 4;
        }
        int[][] iArr = {new int[]{16, 14}, new int[]{18, 26}, new int[]{28, 17}, new int[]{28, 27}, new int[]{13, 14}, new int[]{16, 33}, new int[]{29, 31}, new int[]{37, 19}, new int[]{11, 28}, new int[]{13, 37}, new int[]{33, 18}, new int[]{24, 13}};
        for (int i = 0; i < 12; i++) {
            int[] t = iArr[i];
            p.add(new Prop("lamp", t[0], t[1], 26.0f, false));
        }
        for (int k3 = 20; k3 <= 26; k3 += 2) {
            p.add(new Prop("fence", k3, 30, 58.0f, false));
            p.add(new Prop("fence", k3, 36, 58.0f, true));
        }
        int[][] iArr2 = {new int[]{21, 31}, new int[]{25, 31}, new int[]{21, 34}, new int[]{25, 34}};
        for (int i2 = 0; i2 < 4; i2++) {
            int[] t2 = iArr2[i2];
            p.add(new Prop("tree", t2[0], t2[1], 72.0f, false));
        }
        for (int k4 = 6; k4 <= 33; k4 += 3) {
            p.add(new Prop("fence", k4, 40, 58.0f, true));
            p.add(new Prop("fence", k4, 3, 58.0f, false));
        }
        for (int k5 = 6; k5 <= 36; k5 += 3) {
            p.add(new Prop("fence", 3, k5, 58.0f, true));
        }
        for (int k6 = 7; k6 <= 31; k6 += 6) {
            p.add(new Prop("tree", k6, 41, 72.0f, false));
            p.add(new Prop("tree", k6, 2, 72.0f, false));
        }
        for (int k7 = 8; k7 <= 32; k7 += 6) {
            p.add(new Prop("tree", 2, k7, 72.0f, false));
        }
        p.add(new Prop("notice", 18, 29, 84.0f, false));
        p.add(new Prop("notice", 16, 14, 84.0f, true));
        p.sort(Comparator.comparingDouble(a->a.y));
        PROPS = Collections.unmodifiableList(p);
    }

    public static Building forInterior(String id) {
        for (Building b : BUILDINGS) {
            if (Objects.equals(id, b.interior)) {
                return b;
            }
        }
        return null;
    }

    public static Building doorAt(float px, float py) {
        for (Building b : BUILDINGS) {
            if (b.interior != null && Math.hypot(px - b.doorX(), py - b.doorY()) < 12.0d) {
                return b;
            }
        }
        return null;
    }

    public static boolean pool(int u, int v) {
        return u >= 19 && u <= 26 && v >= 18 && v <= 25;
    }

    public static boolean garden(int u, int v) {
        return (u >= 20 && u <= 26 && v >= 30 && v <= 35) || (v >= 40 && v <= 41 && u >= 5 && u <= 34) || ((u >= 2 && u <= 3 && v >= 5 && v <= 37) || (v >= 2 && v <= 3 && u >= 5 && u <= 34));
    }

    public static boolean channel(int u, int v) {
        return (u >= 13 && u <= 15 && v >= 5 && v <= 12) || (u >= 5 && u <= 12 && v == 25) || ((u >= 30 && u <= 39 && v == 31) || (u == 39 && v >= 6 && v <= 18));
    }

    public static boolean road(int u, int v) {
        return (u >= 16 && u <= 18) || (v >= 15 && v <= 17) || (v >= 27 && v <= 29);
    }

    public static boolean benchCell(int u, int v) {
        return ((u == 18 || u == 27) && (v == 20 || v == 24)) || ((v == 17 || v == 26) && (u == 20 || u == 24));
    }

    public static boolean waterCell(int u, int v) {
        return pool(u, v) || channel(u, v);
    }

    public static boolean blocked(int u, int v) {
        if (u < 1 || v < 1 || u >= 44 || v >= 44 || pool(u, v) || channel(u, v) || garden(u, v) || benchCell(u, v)) {
            return true;
        }
        for (Building b : BUILDINGS) {
            if (b.occupies(u, v) && (b.interior == null || u != (b.u + b.du) - 3 || v != (b.v + b.dv) - 1)) {
                return true;
            }
        }
        return false;
    }

    public static final class Prop {
        public final String asset;
        public final boolean mirror;
        public final int u;
        public final int v;
        public final float width;
        public final float x;
        public final float y;

        Prop(String asset, int u, int v, float width, boolean mirror) {
            this.asset = asset;
            this.u = u;
            this.v = v;
            this.x = AbelTownGeometry.x(u, v);
            this.y = AbelTownGeometry.y(u, v);
            this.width = width;
            this.mirror = mirror;
        }
    }

    public static List<RectF> obstacles() {
        List<RectF> out = new ArrayList<>();
        for (int u = 0; u <= 44; u++) {
            for (int v = 0; v <= 44; v++) {
                if (blocked(u, v)) {
                    out.add(new RectF(x(u, v) - 3.0f, y(u, v) - 3.0f, x(u, v) + 3.0f, y(u, v) + 3.0f));
                }
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static List<WorldMoveTargetController.TileCenter> navigation() {
        List<WorldMoveTargetController.TileCenter> out = new ArrayList<>();
        for (int u = 1; u < 44; u++) {
            for (int v = 1; v < 44; v++) {
                if (!blocked(u, v)) {
                    out.add(new WorldMoveTargetController.TileCenter(x(u, v), y(u, v)));
                }
            }
        }
        return Collections.unmodifiableList(out);
    }

    private AbelTownGeometry() {
    }
}
