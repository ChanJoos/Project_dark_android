package com.projectdark.mobile;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.LinkedHashMap;
import java.util.Map;
final class EquipmentVisualRegistry {
 static final class Visual {final String id;final Bitmap atlas;final SourceEquipmentRegistration registration;Visual(String id,Bitmap a,SourceEquipmentRegistration r){this.id=id;atlas=a;registration=r;}}
 private final Context context;private final Resources resources;private final Map<String,Visual> cache=new LinkedHashMap<>();
 EquipmentVisualRegistry(Context c){context=c;resources=c==null?null:c.getResources();}
 Visual get(String appearanceId){if(appearanceId==null||context==null)return null;if(cache.containsKey(appearanceId))return cache.get(appearanceId);Bitmap b=null;try{int id=resources.getIdentifier("equip_"+appearanceId.toLowerCase()+"_source","drawable",context.getPackageName());if(id!=0){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeResource(resources,id,o);}}catch(Throwable ignored){}SourceEquipmentRegistration r=SourceEquipmentRegistration.load(context,appearanceId.toLowerCase());Visual v=b==null||r==null?null:new Visual(appearanceId,b,r);cache.put(appearanceId,v);return v;}
}