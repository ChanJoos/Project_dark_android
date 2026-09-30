package com.projectdark.mobile;

import android.content.Context;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Definitions are read-only Master projections; saved state contains IDs and proficiency only. */
public final class SkillBook {
  public static final int SLOT_COUNT=8;
  public static final class Entry {
    public final String id,name,job,stage,kind,circle,effect,target,range,resource,limit,requirements,requirementStatus,legacy,evidence;
    public final SkillDef runtime;
    private final JSONObject requirementValues;
    Entry(JSONObject j,SkillDef runtime){
      id=j.optString("id");name=j.optString("name");job=j.optString("job");stage=j.optString("stage");kind=j.optString("kind");circle=j.optString("circle");effect=j.optString("effect");target=j.optString("target");range=j.optString("range");resource=j.optString("resource");limit=j.optString("limit");requirements=j.optString("requirements");requirementStatus=j.optString("requirementStatus");legacy=j.optString("legacy");evidence=j.optString("evidence");this.runtime=runtime;requirementValues=j.optJSONObject("requirementValues");
    }
    public String requirementsFor(RpgProgressionState r){
      Map<String,String> jobs=new HashMap<>();jobs.put("전사","WARRIOR");jobs.put("도적","ROGUE");jobs.put("마법사","MAGE");jobs.put("성직자","CLERIC");jobs.put("무도가","MARTIAL_ARTIST");
      String jobState="공통".equals(job)?"공통 직업 조건 충족":jobs.containsKey(job)?(jobs.get(job).equals(r.currentJobCode())?"직업 조건 충족":"직업 미충족 · 현재 "+r.currentJobCode()):"검증용 조건";
      StringBuilder out=new StringBuilder(jobState);String[] keys={"요구Lv","STR","INT","WIS","CON","DEX"};int[] values={r.normalLevel()==null?0:r.normalLevel(),r.str(),r.intel(),r.wis(),r.con(),r.dex()};
      for(int i=0;i<keys.length;i++){String required=requirementValues==null?"":requirementValues.optString(keys[i]);if(required.isEmpty())continue;out.append("\n").append(keys[i]).append(" ").append(values[i]).append(" / ").append(required);try{out.append(values[i]>=Integer.parseInt(required)?" 충족":" 부족");}catch(NumberFormatException ex){out.append(" 판정 미확정");}}
      return out+"\n자료: "+requirements+"\n승급/선행/재료의 전체 판정은 미확정";
    }
    public boolean magic(){return "마법".equals(kind);}
    public boolean jobMatches(String code){if("공통".equals(job)||"검증용".equals(job))return true;String expected="전사".equals(job)?"WARRIOR":"도적".equals(job)?"ROGUE":"무도가".equals(job)?"MARTIAL_ARTIST":"마법사".equals(job)?"MAGE":"성직자".equals(job)?"CLERIC":"";return expected.equals(code);}
  }
  private final LinkedHashMap<String,Entry> entries=new LinkedHashMap<>();
  private final LinkedHashMap<String,Integer> learned=new LinkedHashMap<>();
  private final Map<String,String> captureConditions=new LinkedHashMap<>();
  private JSONObject learningPolicy;
  JSONObject learningPolicy(String id){return learningPolicy==null?null:learningPolicy.optJSONObject(id);}
  public String summary(String id){JSONObject row=learningPolicy(id);return row==null?"":row.optString("summary");}
  public String captureConditions(String id){String v=captureConditions.get(id);return v==null?get(id).legacy:v;}
  private final String[] slots=new String[SLOT_COUNT];
  private final String[] testSlots=new String[SLOT_COUNT];
  private boolean testAccess;
  private java.util.function.Supplier<String> jobSource;
  public void bindJob(java.util.function.Supplier<String> source){jobSource=source;}
  public boolean jobAllowed(String id){Entry e=get(id);return e!=null&&(testAccess||jobSource==null||e.jobMatches(jobSource.get()));}
  public boolean testAccess(){return testAccess;}
  public void setTestAccess(boolean value){testAccess=value;}
  public boolean previewable(String id){Entry e=get(id);return e!=null&&!"검증용".equals(e.job);}
  public String[] testSlots(){return testSlots.clone();}
  public void restoreTestSlots(String[] values){for(int i=0;i<SLOT_COUNT;i++)testSlots[i]=values!=null&&i<values.length&&previewable(values[i])?values[i]:null;}
  public static SkillBook load(Context context){
    SkillBook book=new SkillBook();
    try(InputStream in=context.getAssets().open("skills/catalog.json")){
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);
      JSONArray list=new JSONArray(new String(bytes.toByteArray(),StandardCharsets.UTF_8));
      for(int i=0;i<list.length();i++){JSONObject j=list.getJSONObject(i);Entry e=new Entry(j,SkillRuntimeCatalog.get(j.optString("id")));if(e.id.isEmpty()||book.entries.put(e.id,e)!=null)throw new IllegalStateException("duplicate skill ID");}
      try(InputStream captures=context.getAssets().open("skills/acquisition_captures.json")){
        ByteArrayOutputStream raw=new ByteArrayOutputStream();while((n=captures.read(buffer))!=-1)raw.write(buffer,0,n);
        JSONObject data=new JSONObject(new String(raw.toByteArray(),StandardCharsets.UTF_8));Iterator<String> ids=data.keys();while(ids.hasNext()){String id=ids.next();if(!book.entries.containsKey(id))throw new IllegalStateException("unknown capture skill");book.captureConditions.put(id,data.getJSONObject(id).getString("conditions"));}
      }
      try(InputStream policy=context.getAssets().open("skills/mobile_learning.json")){
        ByteArrayOutputStream raw=new ByteArrayOutputStream();while((n=policy.read(buffer))!=-1)raw.write(buffer,0,n);
        book.learningPolicy=new JSONObject(new String(raw.toByteArray(),StandardCharsets.UTF_8));
        for(String id:book.entries.keySet())if(book.learningPolicy.optJSONObject(id)==null)throw new IllegalStateException("missing mobile learning policy: "+id);
      }
      for(SkillDef d:new SkillDef[]{SkillDef.SKILL_PROTO,SkillDef.KICK_PROTO,SkillDef.CAST_PROTO}){
        JSONObject j=new JSONObject().put("id",d.id).put("name",d==SkillDef.CAST_PROTO?"시험 마법":d==SkillDef.KICK_PROTO?"시험 발차기":"시험 기술")
          .put("job","검증용").put("kind",d.actionClass==SkillDef.ActionClass.MAGIC?"마법":"기술").put("stage","B 테스트")
          .put("effect","기존 전투 연결 검증용 · 원작 스킬 아님").put("target","적").put("range",d.range+" px (B)").put("resource","MP "+d.mpCost+" (B)")
          .put("requirements","자동 습득 없음 · 테스트/습득 시스템 전용").put("requirementStatus","B").put("evidence","B");
        book.entries.put(d.id,new Entry(j,d));
      }
    }catch(Exception e){throw new IllegalStateException("Skill catalog unavailable",e);}return book;
  }
  public Entry get(String id){return entries.get(id);}
  public Collection<Entry> entries(){return Collections.unmodifiableCollection(entries.values());}
  public List<Entry> list(boolean magic,boolean learnedOnly){List<Entry> result=new ArrayList<>();for(Entry e:entries.values())if(e.magic()==magic&&(!"검증용".equals(e.job)||learned(e.id))&&(!learnedOnly||learned(e.id)))result.add(e);return result;}
  public boolean learned(String id){return testAccess&&previewable(id)||learned.containsKey(id);}
  public int proficiency(String id){Integer v=learned.get(id);return v==null?0:v;}
  /** Called by a validated acquisition service. UI cannot grant skills. */
  public boolean learn(String id,int proficiency){if(!entries.containsKey(id)||proficiency<0||proficiency>100)return false;learned.put(id,proficiency);return true;}
  public boolean usable(String id){Entry e=get(id);return e!=null&&learned(id)&&jobAllowed(id)&&(e.runtime!=null||testAccess&&previewable(id));}
  public int basicHits(){int hits=1;for(SkillActionContract.Rule r:SkillActionContract.all())if(r.mode.equals("LINKED")&&learned(r.id)&&jobAllowed(r.id))hits=Math.max(hits,r.hits);if(learned("SK_도적_031")&&jobAllowed("SK_도적_031"))hits++;return hits;}
  public String slot(int index){return index>=0&&index<SLOT_COUNT?(testAccess?testSlots:slots)[index]:null;}
  public boolean assign(int index,String id){if(index<0||index>=SLOT_COUNT||!learned(id))return false;(testAccess?testSlots:slots)[index]=id;return true;}
  /** Adapted progression: each resolved action advances proficiency once, capped at 100. */
  public void practiced(String id){if(!testAccess&&learned.containsKey(id))learned.put(id,Math.min(100,proficiency(id)+1));}
  public void clearSlot(int index){if(index>=0&&index<SLOT_COUNT)(testAccess?testSlots:slots)[index]=null;}
  public JSONObject snapshot(){try{JSONArray s=new JSONArray();for(String id:slots)s.put(id==null?JSONObject.NULL:id);return new JSONObject().put("version",1).put("learned",new JSONObject(learned)).put("slots",s);}catch(JSONException e){throw new IllegalStateException(e);}}
  /** Transactional validation: malformed/newer snapshots preserve the original save. */
  public boolean restore(JSONObject j){
    try{
      if(j.getInt("version")!=1)return false;
      JSONObject l=j.getJSONObject("learned");Map<String,Integer> next=new LinkedHashMap<>();
      Iterator<String> it=l.keys();while(it.hasNext()){String id=it.next();Object v=l.get(id);if(get(id)==null||!(v instanceof Integer)||((Integer)v)<0||((Integer)v)>100)return false;next.put(id,(Integer)v);}
      JSONArray s=j.getJSONArray("slots");if(s.length()!=SLOT_COUNT)return false;String[] nextSlots=new String[SLOT_COUNT];
      for(int i=0;i<SLOT_COUNT;i++){if(s.isNull(i))continue;Object v=s.get(i);if(!(v instanceof String))return false;String id=(String)v;Entry e=get(id);if(e==null||!next.containsKey(id))return false;nextSlots[i]=id;}
      learned.clear();learned.putAll(next);System.arraycopy(nextSlots,0,slots,0,SLOT_COUNT);return true;
    }catch(JSONException e){return false;}
  }
}
