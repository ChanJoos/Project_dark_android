"""Finish historical generator projections with accepted later source overlays."""
from pathlib import Path
import runpy
T=Path(__file__).resolve().parent
for name in ['apply_skill_feedback_v231.py','apply_rogue_v232.py','apply_rogue_v233.py','apply_rogue_v234.py','apply_mage_v235.py','apply_rogue_v236.py','apply_mage_v243.py']:
 runpy.run_path(str(T/name))
runpy.run_path(str(T/'replay_accepted_skill_metadata_v267.py'),run_name='__main__')
