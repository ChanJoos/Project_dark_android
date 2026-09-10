package com.projectdark.mobile;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full CSV-backed skill repository. This deliberately consumes the packaged canonical Master files
 * instead of duplicating hundreds of skill rows as Java constants.
 */
public final class RpgCanonicalSkillRepository {
  public static final String SKILL_MASTER="Skill_Master.csv";
  public static final String RUNTIME_OVERRIDES="Skill_Runtime_Overrides.csv";
  public static final String VISUAL_MANIFEST="Skill_Visual_Manifest.csv";

  public static final class SkillRecord {
    public final String skillId,name,job,stage,type,resource,resourceAmount,requiredLevel,requiredStats,requirementText;
    public final String sourceUrl,evidence,status,note,circle,actionClass,target,range,element,effect,activationLimit,mastery,prerequisite;
    public final String runtimeDataStatus,effectEvidence,detailSourceUrl,verificationNote,runtimeInclusion;
    public final String acquisitionOverride,acquisitionOverrideSource,acquisitionOverrideEvidence;

    SkillRecord(Map<String,String> r,RuntimeOverride override){
      skillId=v(r,"Skill_ID");name=v(r,"스킬명");job=v(r,"직업");stage=v(r,"단계");type=v(r,"종류");resource=v(r,"소모자원");
      resourceAmount=v(r,"소모량");requiredLevel=v(r,"요구Lv");requiredStats=v(r,"요구스탯");requirementText=v(r,"선행/재료/Gold");
      sourceUrl=v(r,"Source_URL");evidence=v(r,"Evidence");status=v(r,"상태");note=v(r,"비고");circle=v(r,"서클");actionClass=v(r,"행동분류");
      target=v(r,"대상");range=v(r,"사거리/범위");element=v(r,"속성");effect=v(r,"핵심효과");activationLimit=v(r,"발동조건/제한");mastery=v(r,"숙련도/실패");
      prerequisite=v(r,"선행스킬");runtimeDataStatus=v(r,"Runtime_Data_Status");effectEvidence=v(r,"Effect_Evidence");detailSourceUrl=v(r,"Detail_Source_URL");
      verificationNote=v(r,"검증메모");runtimeInclusion=v(r,"Runtime_Inclusion");
      acquisitionOverride=override==null?null:override.ruleValue;
      acquisitionOverrideSource=override==null?null:override.sourceUrl;
      acquisitionOverrideEvidence=override==null?null:override.evidence;
    }

    public boolean runtimeIncluded(){return !"EXCLUDE".equalsIgnoreCase(runtimeInclusion);}
    public boolean acquisitionRequirementRemoved(){return "REMOVED".equalsIgnoreCase(acquisitionOverride);}
  }

  public static final class VisualSource {
    public final String visualId,scope,sourcePage,directAssetUrl,oid,sourceStatus,mappingStatus,evidence,notes;
    VisualSource(Map<String,String> r){
      visualId=v(r,"Visual_ID");scope=v(r,"Scope");sourcePage=v(r,"Source_Page");directAssetUrl=v(r,"Direct_Asset_URL");oid=v(r,"OID");
      sourceStatus=v(r,"Source_Status");mappingStatus=v(r,"Mapping_Status");evidence=v(r,"Evidence");notes=v(r,"Notes");
    }
  }

  private static final class RuntimeOverride {
    final String skillId,ruleType,ruleValue,sourceUrl,evidence,status,notes;
    RuntimeOverride(Map<String,String> r){skillId=v(r,"Skill_ID");ruleType=v(r,"Rule_Type");ruleValue=v(r,"Rule_Value");sourceUrl=v(r,"Source_URL");evidence=v(r,"Evidence");status=v(r,"Status");notes=v(r,"Notes");}
  }

  private final Map<String,SkillRecord> byId;
  private final List<VisualSource> visualSources;

  private RpgCanonicalSkillRepository(Map<String,SkillRecord> byId,List<VisualSource> visualSources){
    this.byId=Collections.unmodifiableMap(new LinkedHashMap<>(byId));
    this.visualSources=Collections.unmodifiableList(new ArrayList<>(visualSources));
  }

  public static RpgCanonicalSkillRepository load(Context context) throws IOException {
    if(context==null)throw new IllegalArgumentException("context");
    List<Map<String,String>> overrideRows=readCsv(context,RUNTIME_OVERRIDES);
    Map<String,RuntimeOverride> overrides=new LinkedHashMap<>();
    for(Map<String,String> row:overrideRows){RuntimeOverride o=new RuntimeOverride(row);if(o.skillId!=null&&!o.skillId.isEmpty())overrides.put(o.skillId,o);}

    List<Map<String,String>> skillRows=readCsv(context,SKILL_MASTER);
    Map<String,SkillRecord> skills=new LinkedHashMap<>();
    for(Map<String,String> row:skillRows){
      String id=v(row,"Skill_ID");if(id==null||id.isEmpty())continue;
      skills.put(id,new SkillRecord(row,overrides.get(id)));
    }

    List<VisualSource> visuals=new ArrayList<>();
    for(Map<String,String> row:readCsv(context,VISUAL_MANIFEST))visuals.add(new VisualSource(row));
    return new RpgCanonicalSkillRepository(skills,visuals);
  }

  public Map<String,SkillRecord> byId(){return byId;}
  public SkillRecord find(String skillId){return byId.get(skillId);}
  public List<VisualSource> visualSources(){return visualSources;}

  public List<SkillRecord> forJob(String koreanJob){
    List<SkillRecord> out=new ArrayList<>();
    for(SkillRecord r:byId.values())if(koreanJob!=null&&koreanJob.equals(r.job)&&r.runtimeIncluded())out.add(r);
    return Collections.unmodifiableList(out);
  }

  public List<SkillRecord> forJobAndCircle(String koreanJob,String circle){
    List<SkillRecord> out=new ArrayList<>();
    for(SkillRecord r:byId.values())if(koreanJob!=null&&koreanJob.equals(r.job)&&circle!=null&&circle.equals(r.circle)&&r.runtimeIncluded())out.add(r);
    return Collections.unmodifiableList(out);
  }

  public boolean hasOfficialVisualSource(){
    for(VisualSource v:visualSources)if("O".equals(v.evidence)&&"SOURCE_FOUND".equals(v.sourceStatus))return true;
    return false;
  }

  private static List<Map<String,String>> readCsv(Context context,String assetName) throws IOException {
    try(BufferedReader br=new BufferedReader(new InputStreamReader(context.getAssets().open(assetName),StandardCharsets.UTF_8))){
      String headerLine=br.readLine();if(headerLine==null)return Collections.emptyList();
      if(headerLine.startsWith("\ufeff"))headerLine=headerLine.substring(1);
      List<String> headers=parseCsvLine(headerLine);List<Map<String,String>> rows=new ArrayList<>();String line;
      while((line=br.readLine())!=null){
        if(line.trim().isEmpty())continue;List<String> cells=parseCsvLine(line);Map<String,String> row=new LinkedHashMap<>();
        for(int i=0;i<headers.size();i++)row.put(headers.get(i),i<cells.size()?cells.get(i):"");rows.add(row);
      }
      return rows;
    }
  }

  /** RFC4180-style single-record parser; Skill_Master has quoted commas but no embedded record newlines. */
  static List<String> parseCsvLine(String line){
    List<String> out=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false;
    for(int i=0;i<line.length();i++){
      char c=line.charAt(i);
      if(c=='\"'){
        if(quoted&&i+1<line.length()&&line.charAt(i+1)=='\"'){cell.append('\"');i++;}else quoted=!quoted;
      }else if(c==','&&!quoted){out.add(cell.toString());cell.setLength(0);}else cell.append(c);
    }
    out.add(cell.toString());return out;
  }

  private static String v(Map<String,String> row,String key){String value=row.get(key);return value==null?"":value;}
}
