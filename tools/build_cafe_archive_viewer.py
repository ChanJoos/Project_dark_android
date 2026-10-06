#!/usr/bin/env python3
"""Build a local-only review viewer from snapshots; never fetch or execute source HTML."""
import html
from html.parser import HTMLParser
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT/'master/source/skill_fx/naver_cafe_archive_20261001'

class SafeBody(HTMLParser):
    allowed = {'div','p','span','b','strong','em','i','u','br','hr','table','tbody','tr','td','th','ul','ol','li','h1','h2','h3','h4'}
    def __init__(self, paths):
        super().__init__(); self.paths=paths; self.result=[]; self.ordinal=0; self.skip=0
    def handle_starttag(self, tag, attrs):
        if tag in {'script','style','iframe'}: self.skip+=1; return
        if self.skip: return
        if tag=='img':
            path=self.paths[self.ordinal]; self.ordinal+=1
            self.result.append(f'<img src="{html.escape(path,quote=True)}" alt="source image {self.ordinal}" loading="lazy">')
        elif tag in self.allowed:
            safe=''.join(f' {k}="{html.escape(v,quote=True)}"' for k,v in attrs if k in {'colspan','rowspan'} and v and v.isdigit())
            self.result.append(f'<{tag}{safe}>')
    def handle_endtag(self, tag):
        if tag in {'script','style','iframe'}: self.skip=max(0,self.skip-1); return
        if not self.skip and tag in self.allowed and tag not in {'br','hr'}: self.result.append(f'</{tag}>')
    def handle_data(self, value):
        if not self.skip: self.result.append(html.escape(value))

def generate():
    manifest=json.loads((ARCHIVE/'manifest.json').read_text())
    media=json.loads((ARCHIVE/'media.json').read_text())['media']
    lookup={(ref['articleId'],ref['imageIndex']):r for r in media for ref in r['references']}
    entries=[]
    for article in manifest['articles']:
        aid=article['articleId'];doc=json.loads((ARCHIVE/f'{aid}.json').read_text())
        paths=[os.path.relpath(ROOT/lookup[(aid,i)]['path'],ARCHIVE) for i in range(len(doc['images']))]
        if doc.get('html'):
            parser=SafeBody(paths);parser.feed(doc['html']);body=''.join(parser.result)
            assert parser.ordinal==len(paths),(aid,parser.ordinal,len(paths))
        else:
            body='<pre>'+html.escape(doc['text'])+'</pre>'+''.join(f'<img src="{html.escape(p,quote=True)}" loading="lazy" alt="source image {i}">' for i,p in enumerate(paths))
        title=html.escape(doc.get('title',article['label']))
        entries.append(f'<section id="a{aid}"><h2>{aid} · {title}</h2><a href="{aid}.json">Exact snapshot JSON</a><div class="source">{body}</div></section>')
    nav=''.join(f'<a href="#a{a["articleId"]}">{a["articleId"]} {html.escape(a["label"])}</a>' for a in manifest['articles'])
    output='<!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>PROJECT DARK offline skill sources</title><style>body{margin:0;background:#171b22;color:#ececf1;font:16px/1.7 sans-serif}main{max-width:1000px;margin:auto;padding:24px}nav{display:flex;flex-wrap:wrap;gap:12px}a{color:#83bfff}section{margin:36px 0;padding:20px;background:#fff;color:#222;border-radius:8px}section a{color:#165ca7}.source{overflow-wrap:anywhere}.source img{max-width:100%;height:auto;display:inline-block;vertical-align:middle;margin:8px}pre{white-space:pre-wrap}table{max-width:100%;border-collapse:collapse}td,th{padding:4px;border:1px solid #ddd}</style><main><h1>Offline skill source archive</h1><p>Historical fan references. Captured GIFs are source footage; these pages do not prove complete runtime mechanics. All images below load from retained local files.</p><nav>'+nav+'</nav>'+''.join(entries)+'</main></html>\n'
    (ARCHIVE/'index.html').write_text(output)
    print('OFFLINE_VIEWER',len(entries),'articles',sum(len(json.loads((ARCHIVE/f'{a["articleId"]}.json').read_text())['images']) for a in manifest['articles']),'local image references')

if __name__=='__main__':generate()
