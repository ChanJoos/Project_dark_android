#!/usr/bin/env python3
"""Index retained source sections without fetching pages or changing runtime rules."""
import csv
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / 'master/source/skill_fx/naver_cafe_archive_20261001'
master = list(csv.DictReader((ROOT / 'master/data/Skill_Master.csv').open(encoding='utf-8-sig')))

def clean(value):
    return value.replace('\ufeff', '').replace('\u200b', '').strip()

def ids_for(name, job):
    return [r['Skill_ID'] for r in master if clean(r['스킬명']) == name and r['직업'] in (job, '공통')]

def line_field(text, label):
    match = re.search(label + r'\s*:?\s*([^\n]+)', text)
    return match.group(1).strip() if match else None

rows = []
for article_id in [191105, 191110, 191114, 191241, 191445, 191777, 401229]:
    path = ARCHIVE / f'{article_id}.json'
    doc = json.loads(path.read_text())
    text = '\n'.join(line.strip() for line in doc['text'].splitlines() if line.strip())
    pattern = r'(?:^|\n)\s*\d+\.\s*\[([^\]]+)\]' if article_id != 401229 else r'(?:^|\n)\s*\d+\.([^\n]+)'
    headings = list(re.finditer(pattern, text))
    for index, heading in enumerate(headings):
        name = clean(heading.group(1))
        section = text[heading.start():headings[index + 1].start() if index + 1 < len(headings) else len(text)].strip()
        stats = {}
        for label, stat in [('힘', 'STR'), ('인트', 'INT'), ('위즈', 'WIS'), ('콘', 'CON'), ('덱스', 'DEX')]:
            match = re.search(label + r'\s*:?\s*(\d+)', section)
            if match:
                stats[stat] = int(match.group(1))
        job = '전사' if article_id != 401229 else None
        matches = ids_for(name, job) if job else [r['Skill_ID'] for r in master if clean(r['스킬명']) == name]
        cooldown = re.search(r'딜레이\s*:\s*([^★]+)★', section)
        rows.append({'articleId': article_id, 'name': name, 'candidateSkillIds': matches,
                     'bindingStatus': 'EXACT_NAME_JOB_MATCH' if len(matches) == 1 else 'REVIEW_REQUIRED',
                     'sourceSnapshotSha256': hashlib.sha256(path.read_bytes()).hexdigest(),
                     'sourceUrl': doc['url'], 'evidence': 'FAN_HISTORICAL_SOURCE', 'rawSection': section,
                     'sourceStats': stats, 'sourceMaterialsRaw': line_field(section, '준비물'),
                     'sourcePrerequisiteRaw': line_field(section, '선수과목'),
                     'sourceCooldownRaw': cooldown.group(1).strip() if cooldown else line_field(section, '쿨타임'),
                     'sourceManaCostRaw': line_field(section, '마나소모'),
                     'sourceFormulaLines': [line for line in section.splitlines() if line.startswith('데미지 =')],
                     'sourceMeasurementContext': 'AC0; 공격 바다 / 방어 바람; article-declared base damage' if article_id == 401229 else None,
                     'sourceMotionLines': [line for line in section.splitlines() if '모션' in line],
                     'sourceEffectLines': [line for line in section.splitlines() if '이펙트' in line],
                     'runtimeChangeApplied': False})

# Retain exact labelled table bindings already audited against the historic source.
classic = json.loads((ROOT / 'master/source/skill_fx/naver_classic_2020/definitions.json').read_text())
for row in classic:
    article_id = int(row['article'].rstrip('/').split('/')[-1])
    path = ARCHIVE / f'{article_id}.json'
    if path.exists():
        rows.append({'articleId': article_id, 'name': row['name'], 'candidateSkillIds': [row['id']] if row['id'] else [],
                     'bindingStatus': 'EXISTING_LABELLED_TABLE_BINDING' if row['id'] else 'UNBOUND_OR_EXCLUDED',
                     'sourceSnapshotSha256': hashlib.sha256(path.read_bytes()).hexdigest(),
                     'sourceUrl': row['article'], 'evidence': row['evidence'],
                     'retainedDefinition': row,
                     'runtimeChangeAppliedInThisCheckpoint': False})

result = {'revision': 'CAFE_OFFLINE_SKILL_EVIDENCE_V1', 'rows': rows,
          'conflicts': [
              {'articleIds': [191445, 191777, 401229], 'field': 'finisherCoefficientAndCooldown',
               'status': 'KEEP_SOURCE_SPECIFIC',
               'reason': '2013 narrative coefficients/cooldowns and later measurements use different dates and stated AC/element conditions. They are not interchangeable current server canon.'},
              {'articleIds': [415456], 'skillName': '다라밀공', 'field': 'INT', 'tableValue': 33,
               'explicitCorrection': 53, 'policy': 'EXPLICIT_SOURCE_CORRECTION_OVERRIDES_OLD_TABLE',
               'existingDefinitionValue': next(r['stats']['INT'] for r in classic if r['name'] == '다라밀공')}
          ],
          'applicationPolicy': 'Historical class/trainer/prerequisite information is source evidence. Keep the user-approved project acquisition policy; changes require accepted Master records and runtime verification.'}
(ARCHIVE / 'skill_evidence.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
print('OFFLINE_SKILL_EVIDENCE_INDEX', len(rows), 'sections/table rows', len(result['conflicts']), 'explicit conflict records')
