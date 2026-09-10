package com.projectdark.mobile;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renderer-only BODY/HAIR/EQUIPMENT/WEAPON/EFFECT binding table.
 * Each layer resolves independently by common State x NW/NE/SW/SE.
 * Missing or PENDING_CROP refs remain unresolved and preserve procedural fallback.
 */
public final class LayeredDirectionalVisualBinding {
  private final Map<CharacterRenderer.Layer,DirectionalVisualBinding> layers=
      new EnumMap<>(CharacterRenderer.Layer.class);

  public LayeredDirectionalVisualBinding bind(CharacterRenderer.Layer layer,
      DirectionalVisualBinding binding){
    if(layer==null)throw new IllegalArgumentException("layer required");
    if(binding==null)layers.remove(layer);else layers.put(layer,binding);
    return this;
  }

  public DirectionalVisualBinding binding(CharacterRenderer.Layer layer){
    return layer==null?null:layers.get(layer);
  }

  public String resolve(CharacterRenderer.Layer layer,CharacterRenderer.State state,
      CharacterRenderer.Direction direction){
    DirectionalVisualBinding binding=binding(layer);
    return binding==null?null:binding.resolve(state,direction);
  }

  public boolean hasResolved(CharacterRenderer.Layer layer,CharacterRenderer.State state,
      CharacterRenderer.Direction direction){
    return resolve(layer,state,direction)!=null;
  }

  public int resolvedLayerCount(CharacterRenderer.State state,CharacterRenderer.Direction direction){
    int count=0;
    for(CharacterRenderer.Layer layer:CharacterRenderer.Layer.values())
      if(hasResolved(layer,state,direction))count++;
    return count;
  }

  public boolean unresolved(){
    for(CharacterRenderer.Layer layer:CharacterRenderer.Layer.values()){
      DirectionalVisualBinding binding=layers.get(layer);
      if(binding==null)continue;
      for(CharacterRenderer.State state:CharacterRenderer.State.values())
        if(!binding.stateUnresolved(state))return false;
    }
    return true;
  }
}
