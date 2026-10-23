package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** One original source sprite per service role, including future TownInteriorDef registrations. */
public final class TownServiceArt {
 private static final Map<String,Integer> HEIGHTS=new HashMap<>();
 public static float labelOffset(String id){return Math.max(64,HEIGHTS.getOrDefault(role(id),56)+8);}
 private static final Map<String,String> ROLES=new HashMap<>();
 public static void register(String id,String kind){if("INN".equals(kind)||"EQUIPMENT".equals(kind)||"REAGENT".equals(kind)||"BANK".equals(kind))ROLES.put(id,kind.toLowerCase(Locale.ROOT));}
 static String role(String id){if(TownTravel.isManager(id))return "travel";if("death_reagent_knight".equals(id))return "death-knight";if(id.endsWith("_reagent"))return "reagent";if(id.endsWith("_bank")||id.contains("_bank_"))return "bank";String known=ROLES.get(id);if(known!=null)return known;if(id.endsWith("_inn"))return "inn";if(id.endsWith("_equipment")||id.endsWith("_weapon")||id.endsWith("_armor")||id.endsWith("_clothing")||id.equals("piet_supplier")||id.equals("abel_supplier"))return "equipment";return null;}
 private final Map<String,Bitmap> images=new HashMap<>();private final Paint paint=new Paint();
 TownServiceArt(Context c){for(String role:new String[]{"inn","equipment","reagent","bank","travel","death-knight"})try(java.io.InputStream in=c.getAssets().open((role.equals("inn")||role.equals("equipment")?"town-v247/":"town-v248/")+role+".png")){BitmapFactory.Options opt=new BitmapFactory.Options();opt.inScaled=false;Bitmap b=BitmapFactory.decodeStream(in,null,opt);images.put(role,b);HEIGHTS.put(role,b.getHeight());}catch(java.io.IOException e){throw new IllegalStateException("Missing original service sprite "+role,e);}paint.setFilterBitmap(false);}
 boolean draw(Canvas c,String id,float x,float y){Bitmap b=images.get(role(id));if(b==null)return false;c.drawBitmap(b,x-b.getWidth()/2f,y-b.getHeight(),paint);return true;}
}
