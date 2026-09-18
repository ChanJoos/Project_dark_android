package com.projectdark.mobile;
/** [V-CANDIDATE] Historical natural-element matrix; unknown/modern elements remain neutral. */
public final class ElementalDamageFormula {
 private ElementalDamageFormula(){}
 public static int apply(int raw,String atk,String def){if(raw<=0)return 0;String a=n(atk),d=n(def);if("NONE".equals(a)||"NONE".equals(d))return raw;if(a.equals(d))return scale(raw,.33);
   if(weak(a,d))return raw;if(next(a,d))return scale(raw,.60);if(opposed(a,d))return scale(raw,.38);return raw;}
 private static boolean weak(String a,String d){return ("WATER".equals(a)&&"FIRE".equals(d))||("EARTH".equals(a)&&"WATER".equals(d))||("WIND".equals(a)&&"EARTH".equals(d))||("FIRE".equals(a)&&"WIND".equals(d));}
 private static boolean next(String a,String d){return ("WATER".equals(a)&&"WIND".equals(d))||("EARTH".equals(a)&&"FIRE".equals(d))||("WIND".equals(a)&&"WATER".equals(d))||("FIRE".equals(a)&&"EARTH".equals(d));}
 private static boolean opposed(String a,String d){return ("WATER".equals(a)&&"EARTH".equals(d))||("EARTH".equals(a)&&"WIND".equals(d))||("WIND".equals(a)&&"FIRE".equals(d))||("FIRE".equals(a)&&"WATER".equals(d));}
 private static int scale(int x,double m){return Math.max(1,(int)Math.round(x*m));}
 private static String n(String x){if(x==null)return"NONE";if(x.equals("바다")||x.equals("수")||x.equalsIgnoreCase("WATER"))return"WATER";if(x.equals("대지")||x.equals("토")||x.equalsIgnoreCase("EARTH"))return"EARTH";if(x.equals("바람")||x.equals("풍")||x.equalsIgnoreCase("WIND"))return"WIND";if(x.equals("화염")||x.equals("화")||x.equalsIgnoreCase("FIRE"))return"FIRE";return x.toUpperCase();}
}