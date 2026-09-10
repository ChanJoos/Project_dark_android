package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/** Render-ready [ADAPTED]/[B] village props authored on the same 64x32 isometric plane as buildings. */
public final class AdaptedMillesDecorationLayer {
  public enum Kind { TREE, FENCE, SIGN, WELL, BENCH, LAMP, BUSH, GATEPOST }
  public enum Axis { NONE, NW_SE, NE_SW }
  public static final class Decoration {
    public final String id,assetRef,evidence,status;
    public final Kind kind;
    public final Axis axis;
    /** footX/footY are ground contact; width is projected span/canopy width; height is vertical sprite height. */
    public final float footX,footY,width,height;
    Decoration(String id,Kind kind,Axis axis,float x,float y,float width,float height){
      this.id=id;this.kind=kind;this.axis=axis;footX=x;footY=y;this.width=width;this.height=height;
      evidence="ADAPTED/B";status="ISOMETRIC_25D_REPLACE_WITH_VERIFIED_MILLES";
      assetRef="PENDING_CROP/milles/decor/"+kind.name().toLowerCase();
    }
    public long depthKey(){return (Math.round(footY*100f)<<20)|(Math.round(footX*10f)&0xfffffL);}
  }

  private static final List<Decoration> DECORATIONS=build();
  private AdaptedMillesDecorationLayer(){}
  public static List<Decoration> decorations(){return DECORATIONS;}

  private static List<Decoration> build(){
    List<Decoration> result=new ArrayList<>(Arrays.asList(
        d("plaza_well",Kind.WELL,800,575,56,34),
        a("plaza_bench_w",Kind.BENCH,Axis.NW_SE,610,660,70,24),a("plaza_bench_e",Kind.BENCH,Axis.NE_SW,995,610,70,24),
        a("north_sign",Kind.SIGN,Axis.NE_SW,830,385,34,60),a("market_sign",Kind.SIGN,Axis.NW_SE,1660,735,34,60),
        a("south_sign",Kind.SIGN,Axis.NE_SW,680,1420,34,60),
        d("plaza_lamp_n",Kind.LAMP,650,455,24,78),d("plaza_lamp_s",Kind.LAMP,1000,710,24,78),
        d("market_lamp_n",Kind.LAMP,1780,640,24,78),d("market_lamp_s",Kind.LAMP,2050,900,24,78),
        d("tree_nw_1",Kind.TREE,105,355,76,116),d("tree_nw_2",Kind.TREE,155,380,72,108),
        d("tree_w_1",Kind.TREE,100,700,76,116),d("tree_w_2",Kind.TREE,180,735,70,106),
        d("tree_market_1",Kind.TREE,1580,510,76,116),d("tree_market_2",Kind.TREE,2240,610,72,110),
        d("tree_market_3",Kind.TREE,1640,1110,78,118),d("tree_east_1",Kind.TREE,2260,1040,72,110),
        d("tree_south_1",Kind.TREE,240,1370,76,116),d("tree_south_2",Kind.TREE,380,1410,72,108),
        d("tree_south_3",Kind.TREE,1240,1410,76,116),d("tree_south_4",Kind.TREE,1370,1370,72,108),
        a("fence_market_n",Kind.FENCE,Axis.NW_SE,1920,615,230,30),a("fence_market_s",Kind.FENCE,Axis.NE_SW,1900,990,250,30),
        a("fence_south_w",Kind.FENCE,Axis.NW_SE,475,1430,210,30),a("fence_south_e",Kind.FENCE,Axis.NE_SW,1110,1430,210,30),

        // Opening-screen 2.5D composition around the three coherent buildings.
        d("tree_opening_west",Kind.TREE,230,585,82,122),d("tree_opening_plaza",Kind.TREE,950,650,78,118),
        a("fence_west_house",Kind.FENCE,Axis.NW_SE,260,625,120,30),a("fence_plaza_house",Kind.FENCE,Axis.NE_SW,955,690,130,30),
        a("plaza_shop_sign",Kind.SIGN,Axis.NE_SW,812,570,38,64),
        a("plaza_shop_bench",Kind.BENCH,Axis.NW_SE,720,590,72,24),

        // Entrance-adjacent vegetation; ground contacts stay outside building footprints.
        d("bush_north_hall_w",Kind.BUSH,620,304,44,30),d("bush_north_hall_e",Kind.BUSH,720,304,44,30),
        d("bush_east_shop_w",Kind.BUSH,1280,684,44,30),d("bush_east_shop_e",Kind.BUSH,1410,684,44,30),
        d("bush_market_shop_w",Kind.BUSH,1715,604,44,30),d("bush_market_shop_e",Kind.BUSH,1845,604,44,30),
        d("bush_south_house_w",Kind.BUSH,1095,1344,44,30),d("bush_south_house_e",Kind.BUSH,1235,1344,44,30),
        d("bush_outer_w",Kind.BUSH,1510,1280,52,34),d("bush_outer_e",Kind.BUSH,1600,1288,52,34),

        d("south_gate_post_w",Kind.GATEPOST,700,1558,34,94),d("south_gate_post_e",Kind.GATEPOST,880,1558,34,94)
    ));
    result.sort(Comparator.comparingLong(Decoration::depthKey));
    return Collections.unmodifiableList(result);
  }
  private static Decoration d(String id,Kind kind,float x,float y,float w,float h){return new Decoration(id,kind,Axis.NONE,x,y,w,h);}
  private static Decoration a(String id,Kind kind,Axis axis,float x,float y,float w,float h){return new Decoration(id,kind,axis,x,y,w,h);}
}
