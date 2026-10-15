package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
import java.util.EnumMap;

/** Project-authored adapted directional mouse animations shared by the inn and opening field. */
final class InnMouseRenderer {
  private final EnumMap<CharacterRenderer.Direction,Bitmap[]> images=new EnumMap<>(CharacterRenderer.Direction.class);
  private final EnumMap<CharacterRenderer.Direction,Bitmap[]> faded=new EnumMap<>(CharacterRenderer.Direction.class),hits=new EnumMap<>(CharacterRenderer.Direction.class),fadedHits=new EnumMap<>(CharacterRenderer.Direction.class);
  private final Paint p=new Paint();
  private static final String[] ACTIVITY={"idle","walk_a","walk_b","attack"};
  static final float FRAME_SIZE=40f,GROUND_BASELINE=59f;
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
      Bitmap[] f=new Bitmap[frames.length],h=new Bitmap[frames.length],fh=new Bitmap[frames.length];
      for(int i=0;i<frames.length;i++){frames[i].prepareToDraw();f[i]=MonsterCursePalette.variant(frames[i],true,false);h[i]=MonsterCursePalette.variant(frames[i],false,true);fh[i]=MonsterCursePalette.variant(frames[i],true,true);f[i].prepareToDraw();h[i].prepareToDraw();fh[i].prepareToDraw();}
      faded.put(d,f);hits.put(d,h);fadedHits.put(d,fh);
    }
  }
  void draw(Canvas c,RuntimeState.Monster mouse,float x,float y){
    draw(c,mouse,x,y,null);
  }
  void draw(Canvas c,RuntimeState.Monster mouse,float x,float y,SkillEffectState effects){
    boolean cursed=MonsterCursePalette.active(effects,mouse.id),hit=mouse.hitFlash>0;
    EnumMap<CharacterRenderer.Direction,Bitmap[]> variants=cursed?(hit?fadedHits:faded):(hit?hits:images);
    Bitmap b=variants.get(mouse.visualFacing.presentation())[activityFrame(mouse)];
    p.setColor(Color.WHITE);p.setColorFilter(null);
    float top=y-GROUND_BASELINE*FRAME_SIZE/64f;
    c.drawBitmap(b,null,new RectF(x-FRAME_SIZE/2f,top,x+FRAME_SIZE/2f,top+FRAME_SIZE),p);p.setColorFilter(null);
  }

  static int activityFrame(RuntimeState.Monster mouse){return mouse.attackPrimed||mouse.attackVisualRemaining>0f?3:mouse.isMoving?1+(((int)(mouse.animationClock*8f)&1)):0;}
}
