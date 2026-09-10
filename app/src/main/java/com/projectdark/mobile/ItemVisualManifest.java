package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Character-owned read-only visual catalog for every canonical item definition exposed by RPG.
 * Item identity/equip rules remain RPG-owned; this class only tracks image/provenance/render readiness.
 */
public final class ItemVisualManifest {
  public enum VisualStatus { PENDING_SOURCE, SOURCE_FOUND, PENDING_IDENTIFICATION, PENDING_CROP, READY_FOR_RENDER, NO_SOURCE_FOUND }
  public enum Surface { INVENTORY_ICON, EQUIPPED_SPRITE }
  public enum RenderLayer { NONE, HAIR, EQUIPMENT, MAIN_HAND, OFF_HAND, ACCESSORY }

  public static final class Entry {
    public final String itemId,name,equipSlot;
    public final boolean characterVisibleCandidate;
    public final RenderLayer renderLayer;
    public final VisualStatus inventoryIconStatus,equippedSpriteStatus;
    public final String sourceUrl,provenance;

    Entry(String itemId,String name,String equipSlot,boolean visible,RenderLayer layer,
        VisualStatus iconStatus,VisualStatus equippedStatus,String sourceUrl,String provenance){
      this.itemId=itemId;this.name=name;this.equipSlot=equipSlot;
      this.characterVisibleCandidate=visible;this.renderLayer=layer;
      this.inventoryIconStatus=iconStatus;this.equippedSpriteStatus=equippedStatus;
      this.sourceUrl=sourceUrl;this.provenance=provenance;
    }
  }

  private final Map<String,Entry> byItemId;

  private ItemVisualManifest(Map<String,Entry> entries){
    byItemId=Collections.unmodifiableMap(new LinkedHashMap<>(entries));
  }

  /**
   * Builds an exhaustive visual manifest from all canonical runtime item definitions.
   * No item is silently dropped: unresolved image evidence starts PENDING_SOURCE.
   */
  public static ItemVisualManifest from(RpgProgressionState rpg){
    Map<String,Entry> out=new LinkedHashMap<>();
    if(rpg==null)return new ItemVisualManifest(out);
    for(Map.Entry<String,RpgProgressionState.ItemDefinition> row:rpg.itemDefinitions().entrySet()){
      RpgProgressionState.ItemDefinition def=row.getValue();
      String slot=def==null?null:def.equipSlot;
      RenderLayer layer=layerForSlot(slot);
      boolean visible=layer!=RenderLayer.NONE;
      out.put(row.getKey(),new Entry(
          row.getKey(),def==null?row.getKey():def.name,slot,visible,layer,
          VisualStatus.PENDING_SOURCE,
          visible?VisualStatus.PENDING_SOURCE:VisualStatus.NO_SOURCE_FOUND,
          null,"U"));
    }
    return new ItemVisualManifest(out);
  }

  public Entry get(String itemId){return byItemId.get(itemId);}
  public Map<String,Entry> all(){return byItemId;}
  public int totalCount(){return byItemId.size();}

  public List<Entry> characterVisibleCandidates(){
    List<Entry> out=new ArrayList<>();
    for(Entry e:byItemId.values())if(e.characterVisibleCandidate)out.add(e);
    return Collections.unmodifiableList(out);
  }

  /** Presentation-only slot mapping. It does not redefine RPG equip semantics. */
  public static RenderLayer layerForSlot(String slot){
    if(slot==null)return RenderLayer.NONE;
    if("무기".equals(slot))return RenderLayer.MAIN_HAND;
    if("방패".equals(slot))return RenderLayer.OFF_HAND;
    if("장갑".equals(slot)||"각반".equals(slot)||"신발".equals(slot)||"갑옷".equals(slot)||"의복".equals(slot))return RenderLayer.EQUIPMENT;
    if("귀걸이".equals(slot)||"목걸이".equals(slot)||"반지".equals(slot)||"벨트".equals(slot))return RenderLayer.ACCESSORY;
    return RenderLayer.NONE;
  }
}
