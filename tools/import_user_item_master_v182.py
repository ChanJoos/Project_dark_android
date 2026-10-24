"""Lossless user item source admission; unidentified columns remain unidentified.

Requires the two supplied xlsx files as arguments. Never overwrites legacy Master
snapshots or guesses equipment slots/art from item names.
"""
from pathlib import Path
import csv, hashlib, json, shutil, sys
import openpyxl

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'master/source/items/user_master_20261018'
DEST = ROOT / 'master/canonical/User_Item_Master_20261018.json'

def main(paths):
    assert len(paths) == 2
    SOURCE.mkdir(parents=True, exist_ok=True)
    sources, tables = [], []
    for number, path in enumerate(map(Path, paths), 1):
        snapshot = SOURCE / f'items_{number:02}.xlsx'
        shutil.copyfile(path, snapshot)
        workbook = openpyxl.load_workbook(path, read_only=True, data_only=False)
        sheets = []
        for sheet in workbook:
            rows = list(sheet.values)
            target = SOURCE / f'items_{number:02}_{sheet.title}.csv'
            with target.open('w', newline='', encoding='utf-8') as stream:
                csv.writer(stream).writerows(rows)
            sheets.append(dict(sheet=sheet.title, rows=len(rows), columns=sheet.max_column,
                               csv=target.name))
            if sheet.title == 'Sheet1':
                tables.append(rows)
        sources.append(dict(file=snapshot.name, originalName=path.name,
                            sha256=hashlib.sha256(path.read_bytes()).hexdigest(), sheets=sheets))
    first, latest = tables
    assert len(first) == len(latest) and first[0] == latest[0]
    differences = [dict(row=i+1, before=list(a), after=list(b))
                   for i, (a,b) in enumerate(zip(first, latest)) if a != b]
    # The second supplied file is the later snapshot; preserve duplicate names by row ID.
    records = [dict(itemId=f'USER_ITEM_20261018_R{i:05}', sourceRow=i,
                    displayName=row[0], uniqueName=row[1], cells=list(row),
                    evidence='U', runtimeStatus='PENDING_IDENTITY_SLOT_ART_MAPPING')
               for i,row in enumerate(latest[1:], 2) if any(v is not None for v in row)]
    result = dict(revision='USER_MASTER_20261018', columns=list(latest[0]),
                  sources=sources, differences=differences, records=records)
    DEST.write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n')
    (SOURCE/'receipt.json').write_text(json.dumps(dict(sources=sources,
        recordCount=len(records), differences=differences,
        unknownColumns=[i+1 for i,v in enumerate(latest[0]) if v is None],
        canonical=str(DEST.relative_to(ROOT))),ensure_ascii=False,indent=2)+'\n')
    # Verify every original cell, including unknown columns, without dropping duplicate names.
    recovered=json.loads(DEST.read_text())['records']
    assert [r['cells'] for r in recovered] == [list(row) for row in latest[1:] if any(v is not None for v in row)]
    assert len({r['itemId'] for r in recovered}) == len(recovered)
    print('ITEM_MASTER_LOSSLESS_VERIFIED', len(records), 'records;', len(differences), 'changed source rows')

if __name__ == '__main__':
    main(sys.argv[1:])
