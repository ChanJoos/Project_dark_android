package com.projectdark.mobile;

/** Measured actor feet in the retained labelled cafe GIFs, distinct from old importer guesses.
 * Original GIF/atlas bytes remain untouched. These are runtime projection registrations.
 */
final class MartialCaptureRegistration {
 static float[] foot(String id,boolean caster){
  String n=id.substring(id.lastIndexOf('_')+1);
  switch(n){
   case "002":case "006":case "007":case "010":case "014":return caster?new float[]{118,105}:new float[]{145,118};
   case "009":case "011":case "015":case "018":case "023":return caster?new float[]{100,96}:new float[]{128,109};
   case "021":return caster?new float[]{118,94}:new float[]{145,105};
   case "019":return new float[]{100,96};
   case "032":return new float[]{94,96};
   default:return new float[]{100,100};
  }
 }
 static boolean recipient(String id,boolean caster){SkillActionContract.Rule r=SkillActionContract.get(id);return !caster&&r!=null&&(r.damage()||!r.selfAnchored());}
 static boolean projectile(String id,boolean caster){return !caster&&(id.equals("SK_무도가_009")||id.equals("SK_무도가_015"));}
 private MartialCaptureRegistration(){}
}
