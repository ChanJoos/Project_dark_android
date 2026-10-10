"""Project reviewed field receipts into runtime and exact named shield artwork."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'master/source/equipment/stats_v130'

def main():
    assert json.loads((ROOT/'master/changes/EQUIPMENT-PERFORMANCE-V130.json').read_text())['status'] == 'ACCEPTED'
    data = json.loads((SRC/'catalog.json').read_text())
    lines = ['package com.projectdark.mobile;', 'import java.util.*;',
             '/** Generated reviewed performance receipts; adapted fields are explicitly labelled. */',
             'final class SourceEquipmentPerformance {', ' static void install(RpgProgressionState r){']
    for row in data['weapons']:
        evidence = 'ADAPTED' if row['existingAdaptedOptions'] or row['status']=='PROJECT_ADAPTED' else row['status']
        kv = ','.join(json.dumps(k)+','+str(v) for k,v in row['stats'].items())
        level = 'null' if row['level'] is None else str(row['level'])
        lines.append(f'  r.installEquipmentPerformance("{row["id"]}",stats({kv}),{level},RpgProgressionState.Evidence.{evidence});')
    for row in data['shields']:
        kv = ','.join(json.dumps(k)+','+str(v) for k,v in row['stats'].items())
        level = 'null' if row['level'] is None else str(row['level'])
        lines.append('  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition('+','.join([
            json.dumps(row['id']),json.dumps(row['name'],ensure_ascii=False),'RpgProgressionState.SHIELD_SLOT',
            'null',level,'Collections.<String>emptySet()','false','null','null',f'stats({kv})',
            'RpgProgressionState.Evidence.'+row['status']])+'));')
    lines += [' }',' static String note(String id){switch(id){']
    for row in data['weapons']+data['shields']:
        lines.append(' case '+json.dumps(row['id'])+':return '+json.dumps(row['note'],ensure_ascii=False)+';')
    lines += [' default:return "";}}',
              ' private static Map<String,Integer> stats(Object... kv){Map<String,Integer> m=new LinkedHashMap<>();for(int i=0;i<kv.length;i+=2)m.put((String)kv[i],(Integer)kv[i+1]);return m;}', '}']
    (ROOT/'app/src/main/java/com/projectdark/mobile/SourceEquipmentPerformance.java').write_text('\n'.join(lines)+'\n')
    print('EQUIPMENT_PERFORMANCE',len(data['weapons']),'weapons',len(data['shields']),'named shields')

if __name__ == '__main__':
    main()
