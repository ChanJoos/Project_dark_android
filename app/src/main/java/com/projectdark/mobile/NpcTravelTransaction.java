package com.projectdark.mobile;
import java.util.function.BooleanSupplier;
/** Localized scene +100Gold transaction; callers validate destination/unlock before invoking. */
final class NpcTravelTransaction {
 static final long FARE=100;
 enum Result { OK,INSUFFICIENT_GOLD,INVALID,SAVE_FAILED }
 static Result run(RuntimeState s,BooleanSupplier move,BooleanSupplier save){if(!s.player().alive)return Result.INVALID;long gold=s.rpg().gold();if(gold<FARE)return Result.INSUFFICIENT_GOLD;RuntimeState.TravelSnapshot before=s.captureTravel();s.rpg().restoreGold(gold-FARE);boolean moved=false;
  try{moved=move.getAsBoolean();if(moved&&save.getAsBoolean())return Result.OK;}catch(RuntimeException e){/* Restore the exact original live scene and Gold below. */}
  s.restoreTravel(before);s.rpg().restoreGold(gold);return moved?Result.SAVE_FAILED:Result.INVALID;
 }
 private NpcTravelTransaction(){}
}
