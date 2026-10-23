package com.projectdark.mobile;
import android.graphics.Canvas;
/** One centered two-panel bag surface; logical source windows retain their hit geometry. */
final class InventoryPairV262 {
 static final float EQUIP_SCALE=.9f,INV_SCALE=.86f;
 static final float EQUIP_X=12-224*EQUIP_SCALE,EQUIP_Y=45-35*EQUIP_SCALE;
 static final float INV_X=424-328*INV_SCALE,INV_Y=45-48*INV_SCALE;
 static void equipment(Canvas c){c.translate(EQUIP_X,EQUIP_Y);c.scale(EQUIP_SCALE,EQUIP_SCALE);}
 static void inventory(Canvas c){c.translate(INV_X,INV_Y);c.scale(INV_SCALE,INV_SCALE);}
 static float equipmentX(float x){return(x-EQUIP_X)/EQUIP_SCALE;}
 static float equipmentY(float y){return(y-EQUIP_Y)/EQUIP_SCALE;}
 static float inventoryX(float x){return(x-INV_X)/INV_SCALE;}
 static float inventoryY(float y){return(y-INV_Y)/INV_SCALE;}
 private InventoryPairV262(){}
}
