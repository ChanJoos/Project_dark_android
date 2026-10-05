#!/usr/bin/env python3
"""Preserve publicly returned article bytes and only the media URLs they contain."""
import concurrent.futures, hashlib, json, re, shutil, urllib.parse, urllib.request
from pathlib import Path
import lxml.html
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'master/source/skill_fx/naver_magic_recovered_20261001'
SOURCE.mkdir(exist_ok=True)
QA = ROOT.parent / 'qa'
catalog = json.loads((ROOT/'app/src/main/assets/skills/catalog.json').read_text())
lookup = {(r['job'], r['name'].replace(' ', '')): r['id'] for r in catalog}
articles = [221564, 221555, 254766, 255589, 416054]
media = []
for article in articles:
    path = SOURCE/f'{article}.json'
    if not path.exists():
        shutil.copyfile(QA/f'recovered_{article}.json', path)
    data = json.loads(path.read_text())['result']['article']
    body = data['scrap'] if data.get('scrap', {}).get('contentElements') else data
    for index, element in enumerate(body['contentElements']):
        if element['type'] != 'IMAGE':
            continue
        url = element['json']['image']['url']
        nested = urllib.parse.parse_qs(urllib.parse.urlparse(url).query).get('src')
        if nested:
            url = nested[0].strip('"')
        rawname = urllib.parse.urlparse(url).path.rsplit('/', 1)[-1]
        try:
            filename = urllib.parse.unquote(rawname, encoding='utf-8', errors='strict')
        except UnicodeDecodeError:
            filename = urllib.parse.unquote(rawname, encoding='cp949')
        ext = Path(filename).suffix.lower()
        if ext not in ['.png', '.gif', '.jpg', '.jpeg']:
            ext = '.png'
        target = SOURCE/(hashlib.sha256(url.encode()).hexdigest()[:16]+ext)
        media.append(dict(articleId=article, index=index, url=url, filename=filename, path=str(target.relative_to(ROOT))))

def fetch(row):
    p = ROOT/row['path']
    if not p.exists():
        req = urllib.request.Request(row['url'], headers={'User-Agent':'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=30) as response:
            p.write_bytes(response.read())
    im = Image.open(p)
    return dict(row, sha256=hashlib.sha256(p.read_bytes()).hexdigest(), bytes=p.stat().st_size,
                width=im.width, height=im.height, frames=getattr(im, 'n_frames', 1))

with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
    media = list(pool.map(fetch, media))
(SOURCE/'media.json').write_text(json.dumps(dict(media=media), ensure_ascii=False, indent=2)+'\n')
by_index = {(m['articleId'], m['index']): m for m in media}
data = json.loads((SOURCE/'416054.json').read_text())['result']['article']['scrap']
rows = []
for table in lxml.html.fromstring(data['contentHtml']).xpath('//table'):
    trs = table.xpath('.//tr')
    cells = [c.text_content().strip() for c in trs[5].xpath('./td')]
    source_name = cells[0]
    name = re.sub(r'\s*Lv\.?\s*\d+\s*$', '', source_name).strip()
    refs = [int(x) for x in re.findall(r'CONTENT-ELEMENT-(\d+)', table.text_content())]
    icon = by_index[416054, refs[0]]
    demos = [by_index[416054, i] for i in refs if by_index[416054, i]['frames']>1 and 'emoticon' not in by_index[416054, i]['url']]
    # Two source table typos are resolved by the labelled GIF and description.
    correction = None
    if demos and demos[0]['filename'] == '수페라에나르마.gif':
        assert source_name == '엑스벨라룸'
        name = '수페라에나르마'
        correction = 'TABLE_DUPLICATE_LABEL; GIF_FILENAME_AND_GROUP_ARMOR_DESCRIPTION'
    if name == '디바르디아':
        assert demos[0]['filename'] == '디바르데아.gif'
        name = '디바르데아'
        correction = 'TABLE_SPELLING; EXACT_LABELLED_GIF_FILENAME'
    matched = lookup.get(('성직자', name.replace(' ', '')))
    if matched:
        name = next(r['name'] for r in catalog if r['id'] == matched)
    rows.append(dict(id=matched, name=name, sourceName=source_name, labelResolution=correction, job='성직자',
                     article='https://m.cafe.naver.com/ca-fe/web/cafes/13434008/articles/416054',
                     icon=icon, demonstration=demos[0] if demos else None,
                     tableCells=[c.text_content().strip() for c in table.xpath('.//td')],
                     evidence='FAN_SEO_CLASSIC_2020_RECOVERED', mechanicStatus='UNCHANGED'))
(SOURCE/'definitions.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2)+'\n')
print('Recovered', len(media), 'media;', sum(m['frames']>1 for m in media), 'animations;', len(rows), 'Cleric rows;', sum(bool(r['id']) for r in rows), 'exact catalog matches')
