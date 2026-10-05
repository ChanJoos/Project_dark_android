package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
import java.util.EnumMap;

/** Project-authored adapted mouse sprites, shared by the inn and opening field.
 * Still frames are directional; no walk or attack animation is claimed.
 */
final class InnMouseRenderer {
  private final EnumMap<CharacterRenderer.Direction,Bitmap> images=new EnumMap<>(CharacterRenderer.Direction.class);
  private final Paint p=new Paint();
  InnMouseRenderer(Context context){
    p.setFilterBitmap(false);p.setAntiAlias(false);
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      String suffix=d.name().toLowerCase(java.util.Locale.ROOT);
      try(InputStream in=context.getAssets().open("interiors/v108/field_mouse_"+suffix+".png")){
        Bitmap source=BitmapFactory.decodeStream(in);if(source==null)throw new IllegalStateException(suffix);
        if(source.getWidth()!=64||source.getHeight()!=64)throw new IllegalStateException("Invalid 64x64 mouse frame: "+suffix);
        images.put(d,source);
      }catch(Exception e){throw new IllegalStateException("Mouse source "+d,e);}
    }
  }
  void draw(Canvas c,RuntimeState.Monster mouse,float x,float y){
    Bitmap b=images.get(mouse.visualFacing.presentation());
    p.setColor(Color.WHITE);p.setColorFilter(mouse.hitFlash>0?new PorterDuffColorFilter(0xffe9d6b1,PorterDuff.Mode.SRC_ATOP):null);
    c.drawBitmap(b,null,new RectF(x-32f,y-64f,x+32f,y),p);p.setColorFilter(null);
  }
}
