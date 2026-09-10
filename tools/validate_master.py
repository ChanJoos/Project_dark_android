#!/usr/bin/env python3
"""Validate every supplied CSV byte/field; never assert unavailable XLSX fidelity."""
import argparse
import collections
import csv
import hashlib
import io
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def sha(data):
    return hashlib.sha256(data).hexdigest()

def address(column, row):
    name = ''
    while column:
        column, n = divmod(column - 1, 26)
        name = chr(65 + n) + name
    return name + str(row)

def audit(root=ROOT):
    master = root / 'master'
    source = json.loads((master / 'CONVERSION_MANIFEST.json').read_text('utf-8'))
    schemas = json.loads((master / 'schema.json').read_text('utf-8'))
    errors, findings, sheets, tables = [], [], [], {}
    expected = {s['file'] for s in source['sheets']}
    actual = {p.name for p in (master / 'data').glob('*.csv')}
    if expected != actual or len(actual) != source['sheet_count']:
        errors.append({'type': 'SHEET_SET', 'missing': sorted(expected-actual), 'extra': sorted(actual-expected)})
    for s in source['sheets']:
        path = master / 'data' / s['file']
        if not path.exists():
            continue
        raw = path.read_bytes()
        try:
            text = raw.decode('utf-8-sig', errors='strict')
            rows = list(csv.reader(io.StringIO(text, newline=''), strict=True))
        except (UnicodeError, csv.Error) as exc:
            errors.append({'sheet': s['sheet'], 'type': 'ENCODING_OR_CSV', 'error': str(exc)})
            continue
        tables[s['sheet']] = rows
        widths = sorted(set(map(len, rows)))
        formulas = [{'cell': address(c, r), 'formula': value}
                    for r, row in enumerate(rows, 1) for c, value in enumerate(row, 1)
                    if value.startswith('=')]
        nonempty = sum(value != '' for row in rows for value in row)
        for field, observed in [('sha256', sha(raw)), ('rows', len(rows)), ('cols', max(widths)), ('formulas', len(formulas))]:
            if observed != s[field]:
                errors.append({'sheet': s['sheet'], 'field': field, 'expected': s[field], 'actual': observed})
        if widths != [s['cols']]:
            errors.append({'sheet': s['sheet'], 'type': 'RAGGED_COLUMNS', 'widths': widths})
        if rows[0] != schemas[s['sheet']]['columns']:
            errors.append({'sheet': s['sheet'], 'type': 'REQUIRED_COLUMNS_OR_ORDER_CHANGED'})
        if nonempty != s['cells']:
            findings.append({'sheet': s['sheet'], 'type': 'SOURCE_POPULATED_COUNT_UNVERIFIABLE',
                             'source_manifest_cells': s['cells'], 'csv_nonempty_fields': nonempty,
                             'difference': s['cells']-nonempty})
        for col, name in enumerate(rows[0]):
            if name.endswith('_ID'):
                ids = collections.defaultdict(list)
                for number, row in enumerate(rows[1:], 2):
                    if row[col]:
                        ids[row[col]].append(number)
                for identity, locations in ids.items():
                    if len(locations) > 1:
                        findings.append({'sheet': s['sheet'], 'type': 'REPEATED_ID_COLUMN', 'column': name,
                                         'value': identity, 'rows': locations,
                                         'interpretation': 'Primary key only if schema declares it; source IDs may repeat.'})
        sheets.append({'sheet': s['sheet'], 'path': 'master/data/'+s['file'], 'sha256': sha(raw),
                       'rows_including_header': len(rows), 'data_rows': len(rows)-1, 'columns': s['cols'],
                       'csv_nonempty_fields': nonempty, 'source_manifest_populated_cells': s['cells'],
                       'formulas': formulas})
    # Explicit semantic joins only. Names/free prose are never silently treated as IDs.
    references = [
        ('Skill_Requirements', 'Skill_ID', 'Skill_Master', 'Skill_ID', None),
        ('NPC_Runtime_Master', 'Quest_Link', 'Quest_Runtime_Master', 'Quest_ID', ','),
        ('Quest_Runtime_Master', 'Prerequisite', 'Quest_Runtime_Master', 'Quest_ID', None),
        ('Item_Master', 'Source_ID', 'Sources', 'Source_ID', None),
    ]
    checked = []
    for src, col, target, key, separator in references:
        src_rows, target_rows = tables[src], tables[target]
        c, k = src_rows[0].index(col), target_rows[0].index(key)
        valid = {r[k] for r in target_rows[1:] if r[k]}
        count = 0
        for number, row in enumerate(src_rows[1:], 2):
            values = row[c].split(separator) if separator else [row[c]]
            for value in values:
                value = value.strip()
                if not value or (col == 'Prerequisite' and not value.startswith('Q_')):
                    continue
                count += 1
                if value not in valid:
                    findings.append({'type': 'UNRESOLVED_REFERENCE', 'sheet': src, 'row': number,
                                     'column': col, 'value': value, 'target': target})
        checked.append({'from': src+'.'+col, 'to': target+'.'+key, 'values_checked': count})
    for s in sheets:
        for f in s['formulas']:
            if '#REF!' in f['formula']:
                findings.append({'type': 'BROKEN_FORMULA_REFERENCE', 'sheet': s['sheet'], **f})
    return {
        'status': 'AVAILABLE_CSV_INTEGRITY_PASS' if not errors else 'FAIL',
        'xlsx_fidelity': 'UNVERIFIED_ORIGINAL_XLSX_NOT_AVAILABLE',
        'source_xlsx_sha256_claim': source['source_xlsx_sha256'],
        'empty_field_semantics': 'CSV empty strings preserved. Original null vs empty-string/type/cache cannot be recovered.',
        'sheet_count': len(sheets),
        'rows_including_headers': sum(s['rows_including_header'] for s in sheets),
        'data_rows_excluding_one_header_per_sheet': sum(s['data_rows'] for s in sheets),
        'csv_nonempty_fields': sum(s['csv_nonempty_fields'] for s in sheets),
        'source_manifest_populated_cells': sum(s['source_manifest_populated_cells'] for s in sheets),
        'formula_string_count': sum(len(s['formulas']) for s in sheets),
        'reference_checks': checked,
        'reference_scope': 'Explicit joins above; free-text drops, coordinates, narrative and name-based relationships remain PENDING_ADAPTER.',
        'errors': errors, 'findings': findings, 'sheets': sheets,
    }

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--write-report', action='store_true')
    args = parser.parse_args()
    report = audit()
    if args.write_report:
        (ROOT / 'master' / 'VALIDATION_REPORT.json').write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(json.dumps({k:v for k,v in report.items() if k not in ('sheets','findings')}, ensure_ascii=False, indent=2))
    print('Preserved source findings:', len(report['findings']))
    raise SystemExit(bool(report['errors']))
