"""Validate the source-stage mine catalog against retained Master and runtime admissions.

This does not certify monster identity, map parity or Android gameplay.
"""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def validate():
    data = json.loads((ROOT / 'master/source/mine_hunt_v208/catalog.json').read_text())
    source = ROOT / data['evidence']['itemMaster']['path']
    assert hashlib.sha256(source.read_bytes()).hexdigest() == data['evidence']['itemMaster']['sha256']
    rows = {r['itemId']: r for r in json.loads(source.read_text())['records']}
    columns = {'AC': 9, 'HIT': 10, 'DAM': 11, 'HP': 12, 'MP': 13,
               'STR': 14, 'DEX': 15, 'INT': 16, 'WIS': 17, 'CON': 18}
    admitted = {x['itemId'] for x in json.loads((ROOT / 'master/source/equipment/horror_v184/runtime_catalog.json').read_text())['admitted']}
    assert len(data['items']) == len({x['itemId'] for x in data['items']}) == 5
    for item in data['items']:
        row = rows[item['itemId']]
        cells = row['cells']
        assert (item['name'], item['sourceRow']) == (row['displayName'], row['sourceRow'])
        assert (item['weight'], item['durability'], item['requiredLevel']) == (cells[2], cells[8], cells[21])
        assert item['stats'] == {key: cells[index] for key, index in columns.items() if cells[index]}
        assert (item['runtimeAdmission'] == 'EXISTING') == (item['itemId'] in admitted)
        assert item['monsterBindings'] == [] and item['dropProbability'] is None
        assert item['sexStatPolicy'] == 'SAME_STATS_USER_CONFIRMED'
    mine = data['requestedMap']
    assert mine['shape'] == 'SQUARE' and mine['entryStatus'] == 'REQUESTED_NOT_WIRED'
    assert all(mine[k] is None for k in ('width', 'height', 'tiles', 'collision', 'roster'))
    reward = data['rewardPolicy']
    assert reward['groundDrops'] is False
    assert all(reward[k] is None for k in ('experience', 'gold', 'dropProbability'))
    assert data['status'] == 'PARTIAL_SOURCE_ONLY_NOT_PLAYABLE'
    print('PASS: 5 armor rows match retained Master; 3 existing admissions; unknown mine data stays unadmitted.')


if __name__ == '__main__':
    validate()
