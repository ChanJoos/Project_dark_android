#!/usr/bin/env python3
"""Reproducible presentation choices; never changes combat or the source Master."""
import csv, json, shutil
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'app/src/main/assets/skill-presentation'
OUT.mkdir(parents=True,exist_ok=True)
catalog=json.loads((ROOT/'app/src/main/assets/skills/catalog.json').read_text())
profiles={
 'CAST':{'group':'f','back':[1,1,0],'front':[3,3,2]},
 'MARTIAL_CAST':{'group':'f','back':[0,1,0],'front':[2,3,2],'phases':[0,.2,.8]},
 'CHARGE_CAST':{'group':'f','back':[0,1,0],'front':[2,3,2],'phases':[0,1/3,.8]},
 'TRIPLE_PUNCH':{'group':'e','back':[0,1,0,1,0,1,0],'front':[2,3,2,3,2,3,2],'phases':[0,1/3,.44,.55,.66,.77,.88]},
 'PUNCH':{'group':'e','back':[0,1,0],'front':[2,3,2]},
 'THRUST':{'group':'e','back':[8,10,8],'front':[12,14,12]},
 'THROW':{'group':'d','back':[0,1,2],'front':[3,4,5]},
 'FRONT_KICK':{'group':'d','back':[0,1,2,1,0],'front':[3,4,5,4,3],'phases':[0,.15,1/3,.7,.85]},
 'SIDE_KICK':{'group':'d','back':[10,11,12,13,10],'front':[14,15,16,17,14],'phases':[0,.15,1/3,.7,.85]},
 'SPIN_KICK':{'group':'d','back':[10,11,12,13,14,15,16,17,10],'front':[14,15,16,17,10,11,12,13,14],'phases':[0,.15,1/3,.43,.52,.60,.68,.76,.85]},
 'JUMP_KICK':{'group':'c','back':[24,25,26],'front':[27,28,29]},
 'SWING':{'group':'02','back':[0],'front':[2]},
 'TWO_HAND':{'group':'c','back':[0,1,2,3],'front':[4,5,6,7],'phases':[0,.23,1/3,.5]},
 'JUMP':{'group':'f','back':[4],'front':[5]},
 'RAISE':{'group':'f','back':[0,1,0],'front':[2,3,2]},
 'IDLE':{'group':'01','back':[0],'front':[1]},
 'EQUIPPED_BASIC':{'group':'02','back':[0],'front':[2]},
}
decisions=list(csv.DictReader((ROOT/'docs/SKILL_ACTION_DECISIONS.csv').open()))
decisions_by_id={r['id']:r for r in decisions}
assert len(decisions_by_id)==len(catalog)==221
assert set(decisions_by_id)=={…62537 tokens truncated…axMp);}
  r.restoreBaseResources(12000,9000);s.applyDerivedGrowth();assertEquals(r.finalStats().maxHp,s.player().maxHp);assertEquals(r.finalStats().maxMp,s.player().maxMp);
 }
}
