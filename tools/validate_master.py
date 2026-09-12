#!/usr/bin/env python3
"""Validate preserved source CSVs and promoted canonical master tables.

The original conversion manifest describes the historical 92-sheet baseline. PROJECT DARK may
promote explicitly declared canonical tables in-place or add new canonical tables; those rows are
validated structurally without pretending they still match the historical XLSX byte/schema claim.
"""
import argparse
import collections
import csv
import hashlib
import io
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

# Explicit canonical generation adopted on 2026-09-12. Asset_Master intentionally supersedes the
# tiny historical baseline table; the other files are additive canonical tables. Keep this list
# explicit so arbitrary unexpected CSVs still fail CI.
CANONICAL_OVERRIDES = {'Asset_Master.csv'}
CANONICAL_ADDITIONS = {
    'Asset_Animation_Frame_Master.csv',
    'Asset_Animation_Semantics.csv',
    'External_Source_Index.csv',
    'Item_Enhancement_Rule_Master.csv',
    'Source_Fact_Master.csv',
}

def sha(data):
    return hashlib.sha256(data).hexdigest()

def address(column, row):
    name = ''
    while column:
        column, n = divmod(column - 1, 26)
        name = chr(65 + n) + name
    return name + str(row)

def parse_csv(path):
    raw = path.read_bytes()
    text = raw.decode('utf-8-sig', errors='strict')
    rows = list(csv.reader(io.StringIO(text, newline=''), strict=True))
    return raw, rows

def audit(root=ROOT):
    master = root / 'master'
    source = json.loads((master / 'CONVERSION_MANIFEST.json').read_text('utf-8'))
    schemas = json.loads((master / 'schema.json').read_text('utf-8'))
    errors, findings, sheets, tables = [], [], [], {}
    expected = {s['file'] for s in source['sheets']}
    allowed = expected | CANONICAL_ADDITIONS
    actual = {p.name for p in (master / 'data').glob('*.csv')}
    missing = expected - actual
    unexpected = actual - allowed
    if missing or unexpected:
        errors.append({'type': 'SHEET_SET', 'missing': sorted(missing), 'extra': sorted(unexpected)})

    for s in source['sheets']:
        path = master / 'data' / s['file']
        if not path.exists():
            continue
        try:
            raw, rows = parse_csv(path)
        except (UnicodeError, csv.Error) as exc:
            errors.append({'sheet': s['sheet'], 'type': 'ENCODING_OR_CSV', 'error': str(exc)})
            continue
        if not rows:
            errors.append({'sheet': s['sheet'], 'type': 'EMPTY_CSV'})
            continue
        tables[s['sheet']] = rows
        widths = sorted(set(map(len, rows)))
        formulas = [{'cell': address(c, r), 'formula': value}
                    for r, row in enumerate(rows, 1) for c, value in enumerate(row, 1)
                    if value.startswith('=')]
        nonempty = sum(value != '' for row in rows for value in row)
        promoted = s['file'] in CANONICAL_OVERRIDES
        if promoted:
            findings.append({
                'sheet': s['sheet'],
                'type': 'CANONICAL_OVERRIDE_SOURCE_FIDELITY_NOT_APPLICABLE',
                'file': s['file'],
                'interpretation': 'Canonical promoted table supersedes historical manifest shape/hash; current CSV is structurally validated instead.'
            })
            if len(widths) != 1:
                errors.append({'sheet': s['sheet'], 'type': 'RAGGED_COLUMNS', 'widths': widths})
            if not rows[0] or any(not name for name in rows[0]):
                errors.append({'sheet': s['sheet'], 'type': 'INVALID_CANONICAL_HEADER'})
        else:
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
                    if col < len(row) and row[col]:
                        ids[row[col]].append(number)
                for identity, locations in ids.items():
                    if len(locations) > 1:
                        findings.append({'sheet': s['sheet'], 'type': 'REPEATED_ID_COLUMN', 'column': name,
                                         'value': identity, 'rows': locations,
                                         'interpretation': 'Primary key only if schema declares it; source IDs may repeat.'})
        sheets.append({'sheet': s['sheet'], 'path': 'master/data/'+s['file'], 'sha256': sha(raw),
                       'rows_including_header': len(rows), 'data_rows': len(rows)-1,
                       'columns': max(widths) if widths else 0,
                       'csv_nonempty_fields': nonempty,
                       'source_manifest_populated_cells': None if promoted else s['cells'],
                       'formulas': formulas, 'canonical_override': promoted})

    # Additive canonical tables are structurally audited, not compared to the historical XLSX.
    for filename in sorted(CANONICAL_ADDITIONS):
        path = master / 'data' / filename
        if not path.exists():
            errors.append({'type': 'MISSING_CANONICAL_ADDITION', 'file': filename})
            continue
        try:
            raw, rows = parse_csv(path)
        except (UnicodeError, csv.Error) as exc:
            errors.append({'type': 'CANONICAL_ENCODING_OR_CSV', 'file': filename, 'error': str(exc)})
            continue
        if not rows:
            errors.append({'type': 'EMPTY_CANONICAL_CSV', 'file': filename})
            continue
        widths = sorted(set(map(len, rows)))
        if len(widths) != 1:
            errors.append({'type': 'CANONICAL_RAGGED_COLUMNS', 'file': filename, 'widths': widths})
        if not rows[0] or any(not name for name in rows[0]):
            errors.append({'type': 'INVALID_CANONICAL_HEADER', 'file': filename})
        findings.append({'type': 'CANONICAL_ADDITION_STRUCTURAL_PASS', 'file': filename,
                         'rows_including_header': len(rows), 'columns': max(widths) if widths else 0,
                         'sha256': sha(raw)})

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
    historical_cells = sum(s['source_manifest_populated_cells'] or 0 for s in sheets)
    return {
        'status': 'AVAILABLE_CSV_INTEGRITY_PASS' if not errors else 'FAIL',
        'xlsx_fidelity': 'UNVERIFIED_ORIGINAL_XLSX_NOT_AVAILABLE',
        'source_xlsx_sha256_claim': source['source_xlsx_sha256'],
        'empty_field_semantics': 'CSV empty strings preserved. Original null vs empty-string/type/cache cannot be recovered.',
        'historical_sheet_count_present': len(sheets),
        'canonical_addition_count': len(CANONICAL_ADDITIONS),
        'rows_including_headers': sum(s['rows_including_header'] for s in sheets),
        'data_rows_excluding_one_header_per_sheet': sum(s['data_rows'] for s in sheets),
        'csv_nonempty_fields': sum(s['csv_nonempty_fields'] for s in sheets),
        'source_manifest_populated_cells_non_overridden': historical_cells,
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
    print('Preserved findings:', len(report['findings']))
    raise SystemExit(bool(report['errors']))
