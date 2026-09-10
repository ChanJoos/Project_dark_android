package com.projectdark.mobile.world;

/** Static contract audit preventing opening-village props from regressing to flat screen-space decoration. */
public final class AdaptedMillesDecoration25DAudit {
  private AdaptedMillesDecoration25DAudit(){}

  public static boolean verify(){
    boolean openingTree=false,openingFence=false,shopSign=false,shopBench=false;
    long previous=Long.MIN_VALUE;
    for(AdaptedMillesDecorationLayer.Decoration d:AdaptedMillesDecorationLayer.decorations()){
      if(d.depthKey()<previous)return false;
      previous=d.depthKey();
      if(!"ADAPTED/B".equals(d.evidence)||!d.status.contains("ISOMETRIC_25D"))return false;
      if(d.width<=0f||d.height<=0f)return false;
      if((d.kind==AdaptedMillesDecorationLayer.Kind.FENCE
          ||d.kind==AdaptedMillesDecorationLayer.Kind.SIGN
          ||d.kind==AdaptedMillesDecorationLayer.Kind.BENCH)
          &&d.axis==AdaptedMillesDecorationLayer.Axis.NONE)return false;
      if("tree_opening_west".equals(d.id)||"tree_opening_plaza".equals(d.id))openingTree=true;
      if("fence_west_house".equals(d.id)||"fence_plaza_house".equals(d.id))openingFence=true;
      if("plaza_shop_sign".equals(d.id))shopSign=true;
      if("plaza_shop_bench".equals(d.id))shopBench=true;
    }
    return openingTree&&openingFence&&shopSign&&shopBench;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("AdaptedMillesDecoration25DAudit failed");
    System.out.println("AdaptedMillesDecoration25DAudit PASS");
  }
}
