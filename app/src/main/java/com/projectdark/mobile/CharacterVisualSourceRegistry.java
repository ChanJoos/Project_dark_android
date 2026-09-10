package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Renderer-side visual evidence registry. This records provenance/readiness only; it does not ship
 * or redistribute source pixels. Nexon-hosted community screenshots are [V], not automatically [O].
 */
public final class CharacterVisualSourceRegistry {
  public enum Subject { PLAYER, NPC, MONSTER, MULTI_ACTOR }
  public enum Evidence { O, V, FAN, PENDING }
  public enum PixelStatus { PREVIEW_VERIFIED, PAGE_CONFIRMED_FETCH_PENDING, UNSEEN }
  public enum DirectionStatus { IDENTIFIED, MULTIPLE_VISIBLE_UNCROPPED, UNKNOWN }
  public enum CropStatus { APPROVED, REFERENCE_ONLY, PENDING_CROP }

  public static final class Source {
    public final String id,pageUrl,assetUrl,note;
    public final Subject subject;
    public final Evidence evidence;
    public final PixelStatus pixelStatus;
    public final DirectionStatus directionStatus;
    public final CropStatus cropStatus;

    public Source(String id,String pageUrl,String assetUrl,Subject subject,Evidence evidence,
        PixelStatus pixelStatus,DirectionStatus directionStatus,CropStatus cropStatus,String note){
      this.id=id;this.pageUrl=pageUrl;this.assetUrl=assetUrl;this.subject=subject;this.evidence=evidence;
      this.pixelStatus=pixelStatus;this.directionStatus=directionStatus;this.cropStatus=cropStatus;
      this.note=note;
    }

    /** Binding requires a positively identified directional crop, not merely a visible screenshot. */
    public boolean bindable(){
      return (evidence==Evidence.O||evidence==Evidence.V)
          &&pixelStatus==PixelStatus.PREVIEW_VERIFIED
          &&directionStatus==DirectionStatus.IDENTIFIED
          &&cropStatus==CropStatus.APPROVED
          &&assetUrl!=null&&!assetUrl.trim().isEmpty();
    }
  }

  // [V] Nexon-hosted community screenshot; preview pixel was directly visible in source search.
  public static final Source NX_COMMUNITY_DESERT_2022=new Source(
      "NX_COMMUNITY_DESERT_2022",
      "https://lod.nexon.com/Community/Screenshot?Category2=3&Page=2",
      "https://storage.nexon.com/dsk01/06/NX_FILE/Board/196608/05/2/000/00/68/5053135543227976333.png",
      Subject.MULTI_ACTOR,Evidence.V,PixelStatus.PREVIEW_VERIFIED,
      DirectionStatus.MULTIPLE_VISIBLE_UNCROPPED,CropStatus.REFERENCE_ONLY,
      "Nexon-hosted gameplay preview with small isometric actor silhouettes; not admitted as a sprite crop.");

  // [V] Historical Nexon community page explicitly describing a gathering of game characters.
  public static final Source NX_COMMUNITY_CHARACTERS_20040513=new Source(
      "NX_COMMUNITY_CHARACTERS_20040513",
      "https://lod.nexon.com/Community/screenshot/121162?Category2=2",
      null,Subject.MULTI_ACTOR,Evidence.V,PixelStatus.PAGE_CONFIRMED_FETCH_PENDING,
      DirectionStatus.UNKNOWN,CropStatus.PENDING_CROP,
      "Historical character reference page; image payload still requires positive pixel inspection.");

  // [V] Dense modern gameplay scene useful for proportion/direction vocabulary, not legacy crop authority.
  public static final Source NX_COMMUNITY_PLAZA_20251207=new Source(
      "NX_COMMUNITY_PLAZA_20251207",
      "https://lod.nexon.com/community/screenshot/141935",
      null,Subject.MULTI_ACTOR,Evidence.V,PixelStatus.PAGE_CONFIRMED_FETCH_PENDING,
      DirectionStatus.MULTIPLE_VISIBLE_UNCROPPED,CropStatus.REFERENCE_ONLY,
      "Modern multi-character scene; reference-only until individual directional pixels are isolated and approved.");

  private static final List<Source> SOURCES=Collections.unmodifiableList(Arrays.asList(
      NX_COMMUNITY_DESERT_2022,NX_COMMUNITY_CHARACTERS_20040513,NX_COMMUNITY_PLAZA_20251207));

  private CharacterVisualSourceRegistry(){}
  public static List<Source> all(){return SOURCES;}
  public static int bindableCount(){int n=0;for(Source s:SOURCES)if(s.bindable())n++;return n;}
  public static boolean preservesEvidenceGate(){
    if(SOURCES.isEmpty())return false;
    for(Source s:SOURCES){
      if(s.id==null||s.pageUrl==null||s.evidence==null||s.pixelStatus==null||s.directionStatus==null||s.cropStatus==null)return false;
      if(s.cropStatus!=CropStatus.APPROVED&&s.bindable())return false;
    }
    return bindableCount()==0;
  }
}
