package com.projectdark.mobile;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renderer-only directional/state visual binding contract.
 * Unverified or unavailable frames stay null/PENDING_CROP; no synthetic asset is promoted to canon.
 */
public final class DirectionalVisualBinding {
  public static final String PENDING_CROP="PENDING_CROP";

  private final Map<CharacterRenderer.State,CharacterRenderer.DirectionalVisualSet> stateSets=
      new EnumMap<>(CharacterRenderer.State.class);

  public DirectionalVisualBinding bind(CharacterRenderer.State state,
      CharacterRenderer.DirectionalVisualSet set){
    if(state==null)throw new IllegalArgumentException("state required");
    if(set==null)stateSets.remove(state);else stateSets.put(state,set);
    return this;
  }

  /** Returns a verified/admitted asset ref for one state+direction, or null while unresolved. */
  public String resolve(CharacterRenderer.State state,CharacterRenderer.Direction direction){
    if(state==null||direction==null)return null;
    CharacterRenderer.DirectionalVisualSet set=stateSets.get(state);
    if(set==null)return null;
    String ref=set.forDirection(direction);
    return isResolved(ref)?ref:null;
  }

  public boolean hasResolved(CharacterRenderer.State state,CharacterRenderer.Direction direction){
    return resolve(state,direction)!=null;
  }

  public boolean stateUnresolved(CharacterRenderer.State state){
    CharacterRenderer.DirectionalVisualSet set=stateSets.get(state);
    if(set==null)return true;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())if(isResolved(set.forDirection(d)))return false;
    return true;
  }

  public int resolvedDirectionCount(CharacterRenderer.State state){
    int count=0;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())if(hasResolved(state,d))count++;
    return count;
  }

  private static boolean isResolved(String ref){
    return ref!=null&&!ref.trim().isEmpty()&&!PENDING_CROP.equals(ref);
  }
}
