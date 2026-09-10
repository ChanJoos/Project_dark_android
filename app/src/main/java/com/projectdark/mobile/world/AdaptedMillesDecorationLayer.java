package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/** Render-ready [ADAPTED]/[B] non-collision decoration layer for village readability. */
public final class AdaptedMillesDecorationLayer {
  public enum Kind { TREE, FENCE, SIGN, WELL, BENCH, LAMP, BUSH, GATEPOST }
  public static final class Decoration {
    public final String id,assetRef,evidence,status;
    public final Kind kind;
    public final float footX,footY,width,height;
    Decoration(String id,Kind kind,float x,float y,float width,float height){
      this.id=id;this.kind=kind;footX=x;footY=y;this.width=width;this.height=height;
      evidence="ADAPTED/B";status="PROTOTYPE_NON_COLLISION_REPLACE_WITH_VERIFIED_MILLES";
      assetRef="PENDING_CROP/milles/decor/"+kind.name().toLowerCase();
    }
    public long depthKey(){return (Math.round(footY*100f)<<20)|(Math.round(footX*10f)&0xfffffL);}
  }

  private static final List<Decoration> DECORATIONS=build();
  private AdaptedMillesDecorationLayer(){}
  public static List<Decoration> decorations(){return DECORATIONS;}

  private static List<Decoration> build(){
    List<Decoration> result=new ArrayList<>(Arrays.asList(
        d("plaza_well",Kind.WELL,800,575,56,30),
        d("plaza_bench_w",Kind.BENCH,610,660,70,22),d("plaza_bench_e",Kind.BENCH,995,610,70,22),
        d("north_sign",Kind.SIGN,830,385,32,58),d("market_sign",Kind.SIGN,1660,735,32,58),
        d("south_sign",Kind.SIGN,680,1420,32,58),
        d("plaza_lamp_n",Kind.LAMP,650,455,24,76),d("plaza_lamp_s",Kind.LAMP,1000,710,24,76),
        d("market_lamp_n",Kind.LAMP,1780,640,24,76),d("market_lamp_s",Kind.LAMP,2050,900,24,76),
        d("tree_nw_1",Kind.TREE,105,355,72,112),d("tree_nw_2",Kind.TREE,155,380,72,112),
        d("tree_w_1",Kind.TREE,100,700,72,112),d("tree_w_2",Kind.TREE,180,735,72,112),
        d("tree_market_1",Kind.TREE,1580,510,72,112),d("tree_market_2",Kind.TREE,2240,610,72,112),
        d("tree_market_3",Kind.TREE,1640,1110,72,112),d("tree_east_1",Kind.TREE,2260,1040,72,112),
        d("tree_south_1",Kind.TREE,240,1370,72,112),d("tree_south_2",Kind.TREE,380,1410,72,112),
        d("tree_south_3",Kind.TREE,1240,1410,72,112),d("tree_south_4",Kind.TREE,1370,1370,72,112),
        d("fence_market_n",Kind.FENCE,1920,615,230,20),d("fence_market_s",Kind.FENCE,1900,990,250,20),
        d("fence_south_w",Kind.FENCE,475,1430,210,20),d("fence_south_e",Kind.FENCE,1110,1430,210,20),

        // Entrance-adjacent vegetation breaks up flat building fronts without changing collision.
        d("bush_north_hall_w",Kind.BUSH,620,304,44,30),d("bush_north_hall_e",Kind.BUSH,720,304,44,30),
        d("bush_east_shop_w",Kind.BUSH,1280,684,44,30),d("bush_east_shop_e",Kind.BUSH,1410,684,44,30),
        d("bush_market_shop_w",Kind.BUSH,1715,604,44,30),d("bush_market_shop_e",Kind.BUSH,1845,604,44,30),
        d("bush_south_house_w",Kind.BUSH,1095,1344,44,30),d("bush_south_house_e",Kind.BUSH,1235,1344,44,30),
        d("bush_outer_w",Kind.BUSH,1510,1280,52,34),d("bush_outer_e",Kind.BUSH,1600,1288,52,34),

        // Outer gate reads as an actual village threshold; portal destination remains PENDING.
        d("south_gate_post_w",Kind.GATEPOST,700,1558,34,92),d("south_gate_post_e",Kind.GATEPOST,880,1558,34,92)
    ));
    result.sort(Comparator.comparingLong(Decoration::depthKey));
    return Collections.unmodifiableList(result);
  }
  private static Decoration d(String id,Kind kind,float x,float y,float w,float h){return new Decoration(id,kind,x,y,w,h);}
}
