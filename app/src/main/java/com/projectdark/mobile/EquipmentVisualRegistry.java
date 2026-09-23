package com.projectdark.mobile;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
final class EquipmentVisualRegistry {
 static final class Visual {final String id;final Bitmap atlas;final SourceEquipmentRegistration registration;Visual(String id,Bitmap a,SourceEquipmentRegistration r){this.id=id;atlas=a;registration=r;}}
 private final Context context;private final Resources resources;private final Map<String,Visual> cache=new LinkedHashMap<>();
 EquipmentVisualRegistry(Context c){context=c;resources=c==null?null:c.getResources();}
 Visual get(String appearanceId){if(appearanceId==null||context==null)return null;if(cache.containsKey(appearanceId))return cache.get(appearanceId);Bitmap b=null;String a=appearanceId.toLowerCase();try{String[] names={"equip_"+a+"_source","player_weapon_"+a,"player_shoes_"+a+"_source","player_shoes_"+a,"player_hat_"+a+"_source","player_hat_"+a,"player_shield_"+a+"_source","player_shield_"+a,"player_armor_"+a+"_idle_walk","player_shirt_"+a+"_source"};for(String name:names){int id=resources.getIdentifier(name,"drawable",context.getPackageName());if(id!=0){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeResource(resources,id,o);if(b!=null)break;}}if(b==null)b=loadMasterAsset(a);}catch(Throwable ignored){}SourceEquipmentRegistration r=SourceEquipmentRegistration.load(context,a);Visual v=b==null?null:new Visual(appearanceId,b,r);cache.put(appearanceId,v);return v;}
 private Bitmap loadMasterAsset(String a){String folder=a.startsWith("mw")||a.startsWith("ww")?"weapon":a.startsWith("mu")||a.startsWith("wu")?"armor":a.startsWith("ml")||a.startsWith("wl")?"shoes":a.startsWith("mh")||a.startsWith("wh")?"hair":a.startsWith("ms")||a.startsWith("ws")?"shield":null;if(folder==null)return null;try{String root="assets/items/"+folder+"/";String[] names=context.getAssets().list(root);if(names==null)return null;for(String name:names){if(!name.toLowerCase().startsWith(a+"_")||!name.toLowerCase().endsWith(".webp"))continue;InputStream in=context.getAssets().open(root+name);BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;Bitmap out=BitmapFactory.decodeStream(in,null,o);in.close();if(out!=null)return out;}}catch(Throwable ignored){}try{File dir=new File(context.getApplicationInfo().sourceDir);File project=new File(dir,"../../../../master/assets/items/"+folder);File[] files=project.listFiles();if(files!=null)for(File f:files){String n=f.getName().toLowerCase();if(n.startsWith(a+"_")&&n.endsWith(".webp")){InputStream in=new FileInputStream(f);Bitmap out=BitmapFactory.decodeStream(in);in.close();if(out!=null)return out;}}}catch(Throwable ignored){}return null;}
}