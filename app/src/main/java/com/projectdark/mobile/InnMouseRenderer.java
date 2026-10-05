package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
import java.util.EnumMap;

/** Source-captured gray mouse silhouettes, shared by the inn and opening field.
 * Unobserved poses stay explicitly shared source stills; no fabricated animation.
 */
final class InnMouseRenderer {
  private final EnumMap<CharacterRenderer.Direction,Bitmap> images=new EnumMap<>(CharacterRenderer.Direction.class);
  private final Paint p=new Paint();
  InnMouseRenderer(Context context){
    p.setFilterBitmap(false);p.setAntiAlias(false);
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      String suffix=d==CharacterRenderer.Direction.SE?"se":d==CharacterRenderer.Direction.NE?"ne":"nw";
      try(InputStream in=context.getAssets().open("interiors/v91/mouse_"+suffix+".png")){
        Bitmap source=BitmapFactory.decodeStream(in);if(source==null)throw new IllegalStateException(suffix);
        Rect alpha=bounds(source);images.put(d,Bitmap.createBitmap(source,alpha.left,alpha.top,alpha.width(),alpha.height()));
      }catch(Exception e){throw new IllegalStateException("Mouse source "+d,e);}
    }
  }
  private static Rect bounds(Bitmap b){int l=b.getWidth(),t=b.getHeight(),r=0,bt=0;
    for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>0){l=Math.min(l,x);t=Math.min(t,y);r=Math.max(r,x+1);bt=Math.max(bt,y+1);}
    if(r<=l||bt<=t)throw new IllegalStateException("Empty rat crop");return new Rect(l,t,r,bt);
  }
  void draw(Canvas c,RuntimeState.Monster mouse,float x,float y){
    Bitmap b=images.get(mouse.visualFacing.presentation());
    p.setColor(Color.WHITE);p.setColorFilter(mouse.hitFlash>0?new PorterDuffColorFilter(0xffe9d6b1,PorterDuff.Mode.SRC_ATOP):null);
    // Keep the small source silhouette recognizable at the game camera scale.
    float width=48f,height=width*b.getHeight()/b.getWidth();
    c.drawBitmap(b,null,new RectF(x-width/2,y-height,x+width/2,y),p);p.setColorFilter(null);
  }
}
