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
    if("horizontal".equals(motion))return new int[]{back?8:6,back?0:4,back?2:7}[n];
    if("vertical".equals(motion))return new int[]{back?8:6,back?9:12,back?10:13}[n];
    return new int[]{back?8:6,back?0:4,back?1:5}[n];
  }
  // Coordinates in the actual cropped BODY bitmap, before source-pivot registration.
  // Previous root-relative guesses attached several frames to the legs.
  static float[] hand(String group,int n){
    float[][] c={{16,8},{13,17},{13,17},{13,25},{5,9},{16,20},{21,16},{18,18},{13,28},{4,9},{13,17},{17,18},{3,8},{13,21}};
    float[][] e={{9,25},{9,25},{4,17},{4,17},{4,17},{4,17},{14,26},{18,22}};
    return "e".equals(group)?e[n-16]:c[n];
  }
  static float[] hand(String group,int n,int width,int height){
    float[] point=hand(group,n);
    int[] widths="c".equals(group)?new int[]{19,16,16,19,19,20,23,24,17,19,16,21,19,17}:new int[]{16,16,16,16,16,16,17,20};
    int[] heights="c".equals(group)?new int[]{51,52,52,48,51,49,49,48,46,60,48,46,61,43}:new int[]{52,52,52,52,52,52,55,54};
    int i="c".equals(group)?n:n-16;
    return new float[]{point[0]*width/widths[i],point[1]*height/heights[i]};
  }
  void layer(Canvas c,String motion,boolean back,int n,float handX,float handY,float bodyScale){
    JSONObject f=manifest.optJSONObject("frames").optJSONObject(motion).optJSONArray(back?"back":"front").optJSONObject(n);
    float scale=bodyScale*(float)manifest.optDouble("sourceScale",.5),w=f.optInt("w")*scale,h=f.optInt("h")*scale;
    float x=Math.round(handX-(float)f.optDouble("gripX")*scale),y=Math.round(handY-(float)f.optDouble("gripY")*scale);
    c.drawBitmap(atlas,new Rect(f.optInt("sx"),f.optInt("sy"),f.optInt("sx")+f.optInt("w"),f.optInt("sy")+f.optInt("h")),new RectF(x,y,x+w,y+h),paint);
  }
  void carry(Canvas c,CharacterRenderer.Pose p,float x,float y,float bodyScale){
    // Back-facing recovery grip needs five source pixels of lateral clearance from the sleeve.
    // BODY/step/depth/source bytes remain unchanged; attachment is an adapted registration.
    if(back(p.direction)&&p.state==CharacterRenderer.State.WALK&&CharacterRenderer.paperDollAtlasColumn(p.state,CharacterRenderer.presentationWalkClock())==4)x+=(west(p.direction)?-1:1)*5f*bodyScale;
    c.save();if(west(p.direction))c.scale(-1,1,x,y);layer(c,"basic",back(p.direction),0,x,y,bodyScale*(float)manifest.optDouble("carryBodyRatio",43f/51f));c.restore();
  }
}
