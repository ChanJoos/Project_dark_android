package com.projectdark.mobile;
/** User canon: front100 / side150 / rear200, applied once per basic hit. */
final class BasicPositionDamageV259 {
 static int percent(RuntimeState.Player attacker,RuntimeState.Monster defender){
  CharacterRenderer.Direction facing=defender.visualFacing.presentation();
  CharacterRenderer.Direction relative=CanonicalActorFacing.quantize(attacker.x-defender.x,attacker.y-defender.y,facing);
  if(relative==facing)return 100;
  if(facing==CharacterRenderer.Direction.NW&&relative==CharacterRenderer.Direction.SE||facing==CharacterRenderer.Direction.SE&&relative==CharacterRenderer.Direction.NW||facing==CharacterRenderer.Direction.NE&&relative==CharacterRenderer.Direction.SW||facing==CharacterRenderer.Direction.SW&&relative==CharacterRenderer.Direction.NE)return 200;
  return 150;
 }
 static int apply(int damage,RuntimeState.Player attacker,RuntimeState.Monster defender){if(damage<=0||defender==null)return Math.max(0,damage);return(int)Math.min(Integer.MAX_VALUE,((long)damage*percent(attacker,defender)+50)/100);}
 private BasicPositionDamageV259(){}
}
