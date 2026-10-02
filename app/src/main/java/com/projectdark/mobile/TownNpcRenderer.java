package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.TownInteriorDef;
import java.util.*;
/** Role palette only; approved BODY and all original wearable bytes remain untouched. */
final class TownNpcRenderer {
 private final CharacterRenderer body=new CharacterRenderer();private final Map<TownInteriorDef.Kind,Bitmap> cache=new EnumMap<>(TownInteriorDef.Kind.class);private final Paint p=new Paint();
 void draw(Canvas c,TownInteriorDef d,float x,float y){Bitmap b=cache.get(d.kind);if(b==null){b=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);body.draw(new Canvas(b),new CharacterRenderer.Pose(64,112,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,d.outfit,null,CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
  for(int yy=0;yy<128;yy++)for(int xx=0;xx<128;xx++){int color=b.getPixel(xx,yy);if(Color.alpha(color)==0)continue;float[] hsv=new float[3];Color.colorToHSV(color,hsv);
   if(hsv[0]>265&&hsv[0]<325&&hsv[1]>.25f&&yy<79){hsv[0]=d.kind==TownInteriorDef.Kind.CHURCH?0:28;hsv[1]=d.kind==TownInteriorDef.Kind.CHURCH?.04f:.55f;hsv[2]=d.kind==TownInteriorDef.Kind.CHURCH?Math.min(1,hsv[2]+.35f):hsv[2]*.72f;b.setPixel(xx,yy,Color.HSVToColor(Color.alpha(color),hsv));}
   else if(d.kind==TownInteriorDef.Kind.CHURCH&&hsv[0]>8&&hsv[0]<65&&hsv[1]>.45f&&yy>75){hsv[0]=42;hsv[1]=.15f;hsv[2]=Math.min(1,.4f+hsv[2]*.58f);b.setPixel(xx,yy,Color.HSVToColor(Color.alpha(color),hsv));}
  }cache.put(d.kind,b);}c.drawBitmap(b,x-64,y-112,p);
 }
}
