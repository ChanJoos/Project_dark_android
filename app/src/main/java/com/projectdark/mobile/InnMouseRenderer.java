package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
import java.util.EnumMap;

/** Project-authored adapted directional mouse animations shared by the inn and opening field. */
final class InnMouseRenderer {
  private final EnumMap<CharacterRenderer.Direction,Bitmap[]> images=new EnumMap<>(CharacterRenderer.Direction.class);
  private final Paint p=new Paint();
  private static final String[] ACTIVITY={"idle","walk_a","walk_b","attack"};
  InnMouseRenderer(Context context){
    p.setFilterBitmap(false);p.setAntiAlias(false);
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      String suffix=d.name().toLowerCase(java.util.Locale.ROOT);
      Bitmap[] frames=new Bitmap[ACTIVITY.length];
      for(int i=0;i<frames.length;i++)try(InputStream in=context.getAssets().open("interiors/v108/field_mouse_"+suffix+"_"+ACTIVITY[i]+".png")){
        Bitmap source=BitmapFactory.decodeStream(in);if(source==null)throw new IllegalStateException(suffix+"/"+ACTIVITY[i]);
        if(source.getWidth()!=64||source.getHeight()!=64)throw new IllegalStateException("Invalid 64x64 mouse frame: "+suffix+"/"+ACTIVITY[i]);
        frames[i]=source;
      }catch(Exception e){throw new IllegalStateException("Mouse source "+d+"/"+ACTIVITY[i],e);}
      images.put(d,frames);
    }
  }
  void draw(Canvas c,RuntimeState.Monster mouse,float x,float y){
    Bitmap b=images.get(mouse.visualFacing.presentation())[activityFrame(mouse)];
    p.setColor(Color.WHITE);p.setColorFilter(mouse.hitFlash>0?new PorterDuffColorFilter(0xffe9d6b1,PorterDuff.Mode.SRC_ATOP):null);
    c.drawBitmap(b,null,new RectF(x-32f,y-64f,x+32f,y),p);p.setColorFilter(null);
  }

  static int activityFrame(RuntimeState.Monster mouse){return mouse.attackPrimed||mouse.attackVisualRemaining>0f?3:mouse.isMoving?1+(((int)(mouse.animationClock*8f)&1)):0;}
}
