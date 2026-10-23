package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** One original source sprite per service role, including future TownInteriorDef registrations. */
public final class TownServiceArt {
 private static final Map<String,String> ROLES=new HashMap<>();
 public static void register(String id,String kind){if("INN".equals(kind)||"EQUIPMENT".equals(kind))ROLES.put(id,"INN".equals(kind)?"inn":"equipment");}
 static String role(String id){String known=ROLES.get(id);if(known!=null)return known;if(id.endsWith("_inn"))return "inn";if(id.endsWith("_equipment")||id.endsWith("_weapon")||id.endsWith("_armor")||id.endsWith("_clothing")||id.equals("piet_supplier")||id.equals("abel_supplier"))return "equipment";return null;}
 private final Map<String,Bitmap> images=new HashMap<>();private final Paint paint=new Paint();
 TownServiceArt(Context c){for(String role:new String[]{"inn","equipment"})try(java.io.InputStream in=c.getAssets().open("town-v247/"+role+".png")){BitmapFactory.Options opt=new BitmapFactory.Options();opt.inScaled=false;images.put(role,BitmapFactory.decodeStream(in,null,opt));}catch(java.io.IOException e){throw new IllegalStateException("Missing original service sprite "+role,e);}paint.setFilterBitmap(false);}
 boolean draw(Canvas c,String id,float x,float y){Bitmap b=images.get(role(id));if(b==null)return false;c.drawBitmap(b,x-b.getWidth()/2f,y-b.getHeight(),paint);return true;}
}
