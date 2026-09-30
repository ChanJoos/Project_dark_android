package com.projectdark.mobile;

import android.content.Context;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Read-only source pose / two-channel presentation contract. Never grants skills or damage. */
final class SkillPresentationCatalog {
  static final class Entry {
    final String id,name,motion,caster,target,targetSheet,targetAnchor;final int targetRow;
    Entry(JSONObject j){id=j.optString("id");name=j.optString("name");motion=j.optString("motion");caster=j.optString("caster");target=j.optString("target");targetSheet=j.optString("targetSheet");targetAnchor=j.optString("targetAnchor");targetRow=j.optInt("targetRow",-1);}
  }
  final JSONObject profiles,frames;final Map<String,Entry> entries=new LinkedHashMap<>();
  SkillPresentationCatalog(Context c){try{JSONObject j=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/catalog.json"),StandardCharsets.UTF_8));profiles=j.getJSONObject("profiles");frames=j.getJSONObject("frames");JSONArray a=j.getJSONArray("skills");for(int i=0;i<a.length();i++){Entry e=new Entry(a.getJSONObject(i));if(entries.put(e.id,e)!=null)throw new IllegalStateException("duplicate presentation ID");}}catch(Exception e){throw new IllegalStateException("skill presentation catalog",e);}}
  Entry get(String id){return entries.get(id);}
  String frameKey(String body,String motion,CharacterRenderer.Direction d,float phase){
    JSONObject p=profiles.optJSONObject(motion);if(p==null)return null;String group=p.optString("group");
    boolean back=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
    JSONArray a=p.optJSONArray(back?"back":"front");if(a==null||a.length()==0)return null;
    int n=Math.min(a.length()-1,(int)(Math.max(0,Math.min(.999f,phase))*a.length()));
    return body+"/"+group+"/"+a.optInt(n);
  }
}
