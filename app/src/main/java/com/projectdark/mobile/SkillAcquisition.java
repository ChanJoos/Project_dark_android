package com.projectdark.mobile;

import java.util.*;

/** User-approved capture conditions. No automatic grant, no guessing unresolved prerequisites. */
public final class SkillAcquisition {
  private final SkillBook book;
  public SkillAcquisition(SkillBook book){this.book=book;}
  private static String jobCode(String job){switch(job){case "전사":return "WARRIOR";case "도적":return "ROGUE";case "무도가":return "MARTIAL_ARTIST";case "마법사":return "MAGE";case "성직자":return "CLERIC";default:return "";}}
  private Map<String,String> conditions(SkillBook.Entry e){Map<String,String> m=new LinkedHashMap<>();for(String part:book.captureConditions(e.id).split(" · ")){int space=part.indexOf(' ');if(space>0)m.put(part.substring(0,space),part.substring(space+1));}return m;}
  public List<String> blockers(SkillBook.Entry e,RpgProgressionState r){
    List<String> out=new ArrayList<>();if(e==null){out.add("기술을 선택하세요");return out;}if(book.learned(e.id))return out;
    if(!"공통".equals(e.job)&&!jobCode(e.job).equals(r.currentJobCode()))out.add(e.job+" 직업 필요");
    if(!"일반".equals(e.stage))out.add("승급 습득 경로 준비 중");
    Map<String,String> q=conditions(e);
    if(q.isEmpty())out.add("습득 조건 확인 중");
    String[] keys={"STR","INT","WIS","CON","DEX"};int[] values={r.str(),r.intel(),r.wis(),r.con(),r.dex()};
    for(int i=0;i<keys.length;i++){String v=q.get(keys[i]);if(v==null)continue;try{int n=Integer.parseInt(v);if(values[i]<n)out.add(keys[i]+" "+values[i]+" / "+n);}catch(NumberFormatException ex){out.add(keys[i]+" 조건 확인 중");}}
    String prior=q.get("Prerequisite_Skill");if(prior!=null){String[] names=prior.split(";"),levels=q.getOrDefault("Required_Prerequisite_Level","").split(";");
      for(int i=0;i<names.length;i++){SkillBook.Entry prerequisite=null;for(SkillBook.Entry x:book.entries())if(x.job.equals(e.job)&&x.name.equals(names[i])){if(prerequisite!=null){prerequisite=null;break;}prerequisite=x;}
        if(prerequisite==null||i>=levels.length){out.add(names[i]+" 선행 조건 확인 중");continue;}try{int n=Integer.parseInt(levels[i]);if(!book.learned(prerequisite.id)||book.proficiency(prerequisite.id)<n)out.add(names[i]+" 숙련 "+book.proficiency(prerequisite.id)+" / "+n);}catch(NumberFormatException ex){out.add(names[i]+" 숙련 조건 확인 중");}}
    }
    // Capture tables do not resolve event/item/gold costs for the final circle.
    if("5".equals(e.circle))out.add("5서클 재료·이벤트 조건 확인 중");
    return out;
  }
  public boolean learn(String id,RpgProgressionState r){SkillBook.Entry e=book.get(id);return e!=null&&!book.learned(id)&&blockers(e,r).isEmpty()&&book.learn(id,0);}
  public String description(SkillBook.Entry e,RpgProgressionState r){
    if(book.learned(e.id))return "습득 완료 · 숙련도 "+book.proficiency(e.id)+"%";
    Map<String,String> q=conditions(e);List<String> lines=new ArrayList<>();lines.add("직업  "+e.job);
    for(String k:new String[]{"STR","INT","WIS","CON","DEX"})if(q.containsKey(k))lines.add(k+"  "+q.get(k));
    if(q.containsKey("Prerequisite_Skill"))lines.add("선행  "+q.get("Prerequisite_Skill").replace(';','·')+"\n숙련  "+q.getOrDefault("Required_Prerequisite_Level","확인 중").replace(';','·'));
    List<String> blocked=blockers(e,r);lines.add(blocked.isEmpty()?"습득 가능":"미충족  "+String.join(" · ",blocked));return String.join("\n",lines);
  }
}
