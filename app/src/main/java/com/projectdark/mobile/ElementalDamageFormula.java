package com.projectdark.mobile;
/** V258: source-table projection, defense rows / attack columns. raw is the standard four-element advantageous hit (18,300 reference). */
public final class ElementalDamageFormula {
 private ElementalDamageFormula(){}
 private static final String[] IDS={"NONE","WATER","EARTH","WIND","FIRE","WOOD","METAL","DARK","LIFE"};
 private static final int[][] SOURCE_DAMAGE={
  {6200,24500,24500,24500,24500,24500,24500,24500,24500},
  {3800,6200,18300,11000,6900,9000,5300,13450,13450},
  {3800,6900,6200,18300,11000,20000,9000,13450,13450},
  {3800,11000,6900,6200,18300,9000,20000,13450,13450},
  {3800,18300,11000,6900,6200,5300,9000,13450,13450},
  {3800,9000,5300,9000,20000,6200,13000,13450,13450},
  {3800,20000,9000,5300,9000,13000,6200,13450,13450},
  {3800,8800,8800,8800,8800,8800,8800,6200,16000},
  {3800,9800,9800,9800,9800,9800,9800,16000,6200}
 };
 public static int apply(int raw,String attack,String defense){
  if(raw<=0)return 0;int a=index(attack),d=index(defense);if(a<0||d<0)return raw;
  long hit=((long)raw*SOURCE_DAMAGE[d][a]+9150)/18300;
  return (int)Math.min(Integer.MAX_VALUE,Math.max(1,hit));
 }
 static int index(String value){String n=normalize(value);for(int i=0;i<IDS.length;i++)if(IDS[i].equals(n))return i;return -1;}
 public static String normalize(String value){
  if(value==null||value.isEmpty())return "NONE";
  switch(value){case "무속성":case "무":return "NONE";case "물":case "바다":case "수":return "WATER";case "땅":case "대지":case "토":return "EARTH";case "바람":case "풍":return "WIND";case "불":case "화염":case "화":return "FIRE";case "목":case "숲":return "WOOD";case "금":case "강철":return "METAL";case "암흑":return "DARK";case "생명":case "빛":return "LIFE";default:String n=value.toUpperCase(java.util.Locale.ROOT);return "LIGHT".equals(n)?"LIFE":n;}
 }
}
