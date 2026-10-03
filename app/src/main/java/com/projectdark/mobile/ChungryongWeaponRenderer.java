package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.nio.charset.StandardCharsets;

/** Labelled capture-derived blade/trails; original BODY and shared combat remain authoritative. */
final class ChungryongWeaponRenderer {
  static final String APPEARANCE="mw_chungryong",ITEM="IT_WEAPON_CHUNGRYONG";
  static final String[] MOTIONS={"basic","twohand","horizontal","vertical"};
  final Bitmap atlas,icon;final JSONObject manifest;private final Paint paint=new Paint();
  private static Bitmap sharedAtlas,sharedIcon;private static JSONObject sharedManifest;
  ChungryongWeaponRenderer(Context c){synchronized(ChungryongWeaponRenderer.class){
    if(sharedAtlas==null)try{
      Bitmap a=BitmapFactory.decodeStream(c.getAssets().open("weapons/chungryong/atlas.png"));
      Bitmap i=BitmapFactory.decodeStream(c.getAssets().open("weapons/chungryong/icon.png"));
      JSONObject j=new JSONObject(new String(PresentationAssetBytes.read(c,"weapons/chungryong/manifest.json"),StandardCharsets.UTF_8));
      if(a==null||i==null)throw new IllegalStateException("missing weapon pixels");sharedAtlas=a;sharedIcon=i;sharedManifest=j;
    }catch(Exception e){throw new IllegalStateException("Chungryong source projection",e);}
    atlas=sharedAtlas;icon=sharedIcon;manifest=sharedManifest;
  }paint.setFilterBitmap(false);}
  static boolean equipped(String id){return APPEARANCE.equals(id);}
  static String motion(String skillId){
    if(skillId==null)return "basic";
    switch(skillId){
      case "SK_전사_002":case "SK_전사_005":case "SK_전사_006":case "SK_전사_020":return "twohand";
      case "SK_전사_001":case "SK_전사_003":case "SK_전사_016":case "SK_전사_017":return "horizontal";
      case "SK_전사_015":case "SK_전사_021":case "SK_전사_023":return "vertical";
      default:return "basic";
    }
  }
  static int frame(String motion,float phase){
    float q=Math.max(0,Math.min(1,phase));if(q>=.8f)return 0;
    if("twohand".equals(motion))return q<.16f?0:q<.25f?1:q<1f/3f?2:3;
    return q<.18f?0:q<1f/3f?1:2;
  }
  static boolean back(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;}
  static boolean west(CharacterRenderer.Direction d){return d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW;}
  static String group(String motion){return "twohand".equals(motion)?"e":"c";}
  static int bodyIndex(String motion,boolean back,int n){
    if("twohand".equals(motion))return (back?16:20)+n;
    if("horizontal".equals(motion))return (back?8:6)+(n==2?1:0);
    if("vertical".equals(motion))return n==0?(back?0:3):(back?10:12)+(n==2?1:0);
    return (back?0:3)+n;
  }
  // Explicit source-pixel hand attachment per BODY pose, relative to its authored root.
  static float[] hand(String group,int n){
    float[][] c={{5,-28},{4,-32},{9,-30},{4,-29},{4,-35},{10,-31},{6,-28},{13,-25},{5,-28},{10,-24},{5,-32},{8,-48},{4,-32},{15,-14}};
    float[][] e={{6,-32},{5,-26},{6,-37},{10,-36},{5,-32},{9,-25},{7,-32},{10,-35}};
    return "e".equals(group)?e[n-16]:c[n];
  }
  void layer(Canvas c,String motion,boolean back,int n,float handX,float handY,float bodyScale){
    JSONObject f=manifest.optJSONObject("frames").optJSONObject(motion).optJSONArray(back?"back":"front").optJSONObject(n);
    float scale=bodyScale*(float)manifest.optDouble("sourceScale",.6),w=f.optInt("w")*scale,h=f.optInt("h")*scale;
    float x=handX-(float)f.optDouble("gripX")*scale,y=handY-(float)f.optDouble("gripY")*scale;
    c.drawBitmap(atlas,new Rect(f.optInt("sx"),f.optInt("sy"),f.optInt("sx")+f.optInt("w"),f.optInt("sy")+f.optInt("h")),new RectF(x,y,x+w,y+h),paint);
  }
  void carry(Canvas c,CharacterRenderer.Pose p,float x,float y,float bodyScale){
    c.save();if(west(p.direction))c.scale(-1,1,x,y);layer(c,"basic",back(p.direction),0,x,y,bodyScale);c.restore();
  }
}
