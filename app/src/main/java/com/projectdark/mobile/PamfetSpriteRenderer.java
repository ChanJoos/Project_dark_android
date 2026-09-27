package com.projectdark.mobile;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.InputStream;

/** Renderer for the explicitly non-canonical Pamfet v3 sprite draft. */
public final class PamfetSpriteRenderer {
  public static final String MONSTER_ID="POTE_PURPLE";
  public static final String ASSET_STATUS="B/ADAPTED/PAMFET_DRAFT_V3";
  public static final float DRAW_SIZE=48f;
  private static final String ROOT="monsters/pamfet/purple/";

  private final AssetManager assets;
  private final Bitmap[][] frames=new Bitmap[2][4];
  private final Paint pixel=new Paint();
  private final Paint marker=new Paint(Paint.ANTI_ALIAS_FLAG);

  public PamfetSpriteRenderer(Context context){
    assets=context==null?null:context.getAssets();
    pixel.setAntiAlias(false);pixel.setFilterBitmap(false);pixel.setDither(false);
    for(int direction=0;direction<4;direction++){
      frames[0][direction]=load(assetPath(false,direction));
      frames[1][direction]=load(assetPath(true,direction));
    }
  }

  public boolean canRender(String monsterId){return MONSTER_ID.equals(monsterId);}

  public boolean assetsReady(){
    for(Bitmap[] state:frames)for(Bitmap frame:state)if(frame==null||frame.isRecycled())return false;
    return true;
  }

  public Bitmap frame(boolean attacking,CharacterRenderer.Direction direction){
    int index=directionIndex(direction);
    return index<0?null:frames[attacking?1:0][index];
  }

  public static String assetPath(boolean attacking,int direction){
    if(direction<0||direction>=4)return null;
    String[] names={"upper_left","upper_right","lower_left","lower_right"};
    return ROOT+(attacking?"attack_roll_":"idle_")+names[direction]+".png";
  }

  public void draw(Canvas canvas,RuntimeState.Monster monster,boolean selected){
    if(canvas==null||monster==null||!canRender(monster.id))return;
    boolean attacking=monster.state==RuntimeState.Monster.State.ATTACK;
    int direction=directionIndex(monster.visualFacing.presentation());
    if(direction<0)return;
    Bitmap bitmap=frames[attacking?1:0][direction];
    if(bitmap==null)return;

    float x=monster.x,y=monster.y;
    marker.setStyle(Paint.Style.FILL);
    marker.setColor(0x64000000);
    canvas.drawOval(new RectF(x-13f,y-2.4f,x+13f,y+2.4f),marker);
    if(selected){
      marker.setStyle(Paint.Style.STROKE);marker.setStrokeWidth(1.5f);marker.setColor(0xaaffdd66);
      canvas.drawOval(new RectF(x-16f,y-4f,x+16f,y+4f),marker);marker.setStyle(Paint.Style.FILL);
    }

    pixel.setAlpha(monster.hitFlash>0f?205:255);
    float left=Math.round(x-DRAW_SIZE*.5f),top=Math.round(y-DRAW_SIZE+3f);
    canvas.drawBitmap(bitmap,null,new RectF(left,top,left+DRAW_SIZE,top+DRAW_SIZE),pixel);
    pixel.setAlpha(255);
    if(monster.hitFlash>0f){
      marker.setStyle(Paint.Style.STROKE);marker.setStrokeWidth(1.8f);marker.setColor(0x99ff8065);
      canvas.drawOval(new RectF(x-14f,y-34f,x+14f,y-5f),marker);marker.setStyle(Paint.Style.FILL);
    }
  }

  private Bitmap load(String path){
    if(assets==null)return null;
    try(InputStream in=assets.open(path)){
      BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      return BitmapFactory.decodeStream(in,null,options);
    }catch(Exception ignored){return null;}
  }

  private static int directionIndex(CharacterRenderer.Direction direction){
    if(direction==null)return -1;
    switch(direction){
      case NW:return 0;
      case NE:return 1;
      case SW:return 2;
      case SE:return 3;
      default:return -1;
    }
  }
}
