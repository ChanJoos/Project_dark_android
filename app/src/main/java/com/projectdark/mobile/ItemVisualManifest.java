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
    /** Text/item-existence evidence is tracked separately and never upgrades image readiness by itself. */
    public final String textEvidenceUrl,textEvidenceKind;
    /** Only positively identified image evidence belongs here. */
    public final String imageSourceUrl,imageProvenance;

    Entry(String itemId,String name,String equipSlot,boolean visible,RenderLayer layer,
        VisualStatus iconStatus,VisualStatus equippedStatus,String textEvidenceUrl,String textEvidenceKind,
        String imageSourceUrl,String imageProvenance){
      this.itemId=itemId;this.name=name;this.equipSlot=equipSlot;
      this.characterVisibleCandidate=visible;this.renderLayer=layer;
      this.inventoryIconStatus=iconStatus;this.equippedSpriteStatus=equippedStatus;
      this.textEvidenceUrl=textEvidenceUrl;this.textEvidenceKind=textEvidenceKind;
      this.imageSourceUrl=imageSourceUrl;this.imageProvenance=imageProvenance;
    }

    public boolean readyForEquippedRender(){return equippedSpriteStatus==VisualStatus.READY_FOR_RENDER&&imageSourceUrl!=null;}
  }

  private final Map<String,Entry> byItemId;

  private ItemVisualManifest(Map<String,Entry> entries){
    byItemId=Collections.unmodifiableMap(new LinkedHashMap<>(entries));
  }

  /**
   * Builds an exhaustive visual manifest from all canonical runtime item definitions.
   * No item is silently dropped: unresolved image evidence starts PENDING_SOURCE.
   * Known textual evidence is seeded without falsely promoting image status.
   */
  public static ItemVisualManifest from(RpgProgressionState rpg){
    Map<String,Entry> out=new LinkedHashMap<>();
    if(rpg==null)return new ItemVisualManifest(out);
    for(Map.Entry<String,RpgProgressionState.ItemDefinition> row:rpg.itemDefinitions().entrySet()){
      RpgProgressionState.ItemDefinition def=row.getValue();
      String slot=def==null?null:def.equipSlot;
      RenderLayer layer=layerForSlot(slot);
      boolean visible=layer!=RenderLayer.NONE;
      ShieldVisualEvidenceCatalog.Evidence shield=ShieldVisualEvidenceCatalog.get(row.getKey());
      String textUrl=shield==null?null:shield.textEvidenceUrl;
      String textKind=shield==null?null:shield.textEvidenceKind.name();
      String imageUrl=shield==null?null:shield.imageSourceUrl;
      String imageProv=shield==null?null:shield.imageProvenance;
      VisualStatus iconStatus=imageUrl==null?VisualStatus.PENDING_SOURCE:VisualStatus.PENDING_CROP;
      VisualStatus equippedStatus=!visible?VisualStatus.NO_SOURCE_FOUND:
          (imageUrl==null?VisualStatus.PENDING_SOURCE:VisualStatus.PENDING_CROP);
      out.put(row.getKey(),new Entry(
          row.getKey(),def==null?row.getKey():def.name,slot,visible,layer,
          iconStatus,equippedStatus,textUrl,textKind,imageUrl,imageProv));
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

  public List<Entry> pendingImageAcquisition(){
    List<Entry> out=new ArrayList<>();
    for(Entry e:byItemId.values()){
      if(e.characterVisibleCandidate&&e.equippedSpriteStatus==VisualStatus.PENDING_SOURCE)out.add(e);
    }
    return Collections.unmodifiableList(out);
  }

  public List<Entry> readyForEquippedRender(){
    List<Entry> out=new ArrayList<>();
    for(Entry e:byItemId.values())if(e.readyForEquippedRender())out.add(e);
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
