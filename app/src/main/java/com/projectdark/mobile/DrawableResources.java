package com.projectdark.mobile;
import android.content.res.Resources;
/** Drawable names belong to the installed resource table, which may differ from the Java namespace. */
final class DrawableResources {
 private DrawableResources() {}
 static int id(Resources resources,String name){
  if(resources==null||name==null)return 0;
  String resourcePackage=resources.getResourcePackageName(R.drawable.hud_attack_button);
  return resources.getIdentifier(name,"drawable",resourcePackage);
 }
}
