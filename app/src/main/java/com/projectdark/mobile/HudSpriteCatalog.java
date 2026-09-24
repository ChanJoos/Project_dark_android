package com.projectdark.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Raster HUD pieces authored as separate, replaceable PNG assets. */
final class HudSpriteCatalog {
  enum Sprite { ATTACK, AUTO, EMPTY_SLOT, QUEST, INVENTORY, STATUS, EQUIPMENT, PORTRAIT, HP_BAR, MP_BAR, EXP_BAR }

  private final Bitmap attack, auto, emptySlot, quest, inventory, status, equipment, portrait, hpBar, mpBar, expBar;
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  HudSpriteCatalog(Context context) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    attack = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_attack_button, options);
    auto = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_auto_button, options);
    emptySlot = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_empty_slot, options);
    quest = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_quest_icon, options);
    inventory = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_inventory_icon, options);
    status = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_status_icon, options);
    equipment = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_equipment_icon, options);
    portrait = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_portrait_frame, options);
    hpBar = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_hp_bar, options);
    mpBar = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_mp_bar, options);
    expBar = BitmapFactory.decodeResource(context.getResources(), R.drawable.hud_exp_bar, options);
  }

  void draw(Canvas canvas, Sprite sprite, RectF destination, int alpha) {
    Bitmap bitmap = bitmap(sprite);
    if (bitmap == null) return;
    paint.setAlpha(Math.max(0, Math.min(255, alpha)));
    canvas.drawBitmap(bitmap, null, destination, paint);
    paint.setAlpha(255);
  }

  private Bitmap bitmap(Sprite sprite) {
    switch (sprite) {
      case ATTACK: return attack;
      case AUTO: return auto;
      case EMPTY_SLOT: return emptySlot;
      case QUEST: return quest;
      case INVENTORY: return inventory;
      case STATUS: return status;
      case EQUIPMENT: return equipment;
      case PORTRAIT: return portrait;
      case HP_BAR: return hpBar;
      case MP_BAR: return mpBar;
      case EXP_BAR: return expBar;
      default: return null;
    }
  }
}
