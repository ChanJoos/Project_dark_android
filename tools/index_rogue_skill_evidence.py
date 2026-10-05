#!/usr/bin/env python3
"""Index exact retained Rogue headings and DOM-ordered media, without runtime mutation."""
import csv
import hashlib
import json
import re
from html.parser import HTMLParser
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / 'master/source/skill_fx/naver_cafe_archive_20261001'
HEADING = re.compile(r'^\s*(\d+(?:\.\d+)*\.)\s*([^:\n]+?)\s*:')

class Nodes(HTMLParser):
    def __init__(self):
        super().__init__()
        self.nodes = []
        self.ordinal = 0
    def handle_data(self, value):
        if value.strip():
            self.nodes.append({'text': value.strip()})
    def handle_starttag(self, tag, attrs):
        if tag == 'img':
            self.nodes.append({'imageIndex': self.ordinal, 'src': dict(attrs).get('src')})
            self.ordinal += 1

def generate():
    master = list(csv.DictReader((ROOT/'master/data/Skill_Master.csv').open(encoding='utf-8-sig')))
    media = json.loads((ARCHIVE/'media.json').read_text())['media']
    lookup = {(ref['articleId'], ref['imageIndex']): row for row in media for ref in row['references']}
    rows = []
    for article in (245450, 245456):
        path = ARCHIVE/f'{article}.json'
        doc = json.loads(path.read_text())
        parser = Nodes(); parser.feed(doc['html'])
        assert parser.ordinal == len(doc['images'])
        starts = [(i, HEADING.match(n.get('text', ''))) for i, n in enumerate(parser.nodes)]
        starts = [(i, m) for i, m in starts if m]
        for ordinal, (start, match) in enumerate(starts):
            end = starts[ordinal+1][0] if ordinal+1 < len(starts) else len(parser.nodes)
            section_nodes = parser.nodes[start:end]
            names = [name.strip() for name in match[2].split(',')]
            text = '\n'.join(n['text'] for n in section_nodes if 'text' in n)
            ids = [r['Skill_ID'] for r in master if r['스킬명'].strip() in names and r['직업'] in ('도적','공통')]
            demonstrations = []
            for node in section_nodes:
                if 'imageIndex' not in node: continue
                m = lookup.get((article,node['imageIndex']))
                # Icons often precede the next heading. Only GIFs are demonstration candidates.
                if m and m.get('format') == 'GIF':
                    demonstrations.append({'imageIndex':node['imageIndex'], 'path':m['path'], 'sha256':m['sha256'],
                                           'frames':m['frames'], 'durationsMs':m['durationsMs'],
                                           'bindingStatus':'LABELLED_SECTION_CANDIDATE_VISUAL_REVIEW_REQUIRED'})
            stats = {}
            for label, key in [('힘','STR'),('인트','INT'),('위즈','WIS'),('콘','CON'),('덱스','DEX')]:
                found = re.search(label+r'\s*:\s*(\d+)',text)
                if found: stats[key] = int(found[1])
            def field(label):
                m = re.search(label+r'\s*:\s*([^\n]+)',text)
                return m[1].strip() if m else None
            rows.append({'articleId':article,'sectionNumber':match[1], 'name':match[2].strip(),
                         'sourceNames':names,'candidateSkillIds':ids,
                         'bindingStatus':'EXACT_NAME_JOB_MATCH' if len(ids)==1 else 'COMBINED_OR_UNBOUND_REVIEW_REQUIRED',
                         'sourceKind':'마법' if article==245456 else '기술', 'rawSection':text,
                         'sourceSnapshotSha256':hashlib.sha256(path.read_bytes()).hexdigest(),
                         'sourceUrl':doc['url'],'evidence':'FAN_HISTORICAL_SOURCE_2015',
                         'sourceStats':stats,'sourceMaterialsRaw':field(r'필요\s*준비물'),
                         'sourcePrerequisiteRaw':field('선수과목'),'sourceTrainerRaw':field('배우는 장소'),
                         'sourceMotionLines':[line for line in text.splitlines() if '모션' in line],
                         'sourceEffectLines':[line for line in text.splitlines() if '이펙' in line],
                         'demonstrationCandidates':demonstrations,'runtimeChangeApplied':False})
    assert len(rows)==38, len(rows)
    conflicts = [
        {'articleId':245450,'name':'습격','field':'demonstrationIdentity','status':'DO_NOT_BIND_AUTOMATICALLY',
         'reason':'The author explicitly says the shown effect was acquired from another character as 기습. Do not assign this GIF to 습격 solely from its section or filename.'},
        {'articleId':245450,'name':'백슬래쉬','field':'iconIdentityAndGold','status':'UNRESOLVED',
         'reason':'Author calls the icon 윈드블레이드? and prints Gold as 20,00,000. Preserve the ambiguity; do not guess icon identity or normalize currency to 2M/20M.'},
        {'articleId':245456,'name':'하이드','field':'breakOnAction','status':'BODY_COMMENT_CONFLICT',
         'reason':'Body says other actions break stealth; visible comments name exceptions 센스몬스터, 센스, 콘푸지오. Preserve both as historical fan evidence, not accepted current mechanics.'},
        {'articleId':245450,'name':'함정파기','field':'availability','status':'BODY_COMMENT_CONFLICT',
         'reason':'Body lists requirements; a visible comment says the skill was removed in an earlier patch. Do not implement availability from the body alone.'},
        {'articleId':245456,'name':'하이더','field':'targetAndPrerequisite','status':'REVIEW_RUNTIME',
         'reason':'Source targets another user and treats the removed 함정파기 prerequisite as uncertain. Existing self-only project presentation is not original service behavior.'},
        {'articleId':245450,'name':'연막탄터뜨리기','field':'recipientScope','status':'VIEWPORT_VS_MAP_UNRESOLVED',
         'reason':'Source observes all visible monsters, excludes group members and says about 3s; author explicitly does not establish viewport vs whole map.'},
    ]
    result = {'revision':'ROGUE_SOURCE_EVIDENCE_2015_V1','rows':rows,'conflicts':conflicts,
              'visibleCommentPolicy':'Only skill-relevant conflict observations from loaded comments are indexed. Raw comments, commenter profiles and account data are not retained; all comment pages are not claimed complete.',
              'scopePolicy':'2차 궁사/어빌 sections remain source inventory outside the current runtime progression scope.'}
    (ARCHIVE/'rogue_evidence.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
    print('ROGUE_SOURCE_EVIDENCE',len(rows),'sections',sum(len(r['demonstrationCandidates']) for r in rows),'GIF candidates')
    return result

if __name__ == '__main__': generate()
