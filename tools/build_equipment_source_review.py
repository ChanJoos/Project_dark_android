"""Build review contact sheets and a byte-preserving source delivery archive."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import csv, hashlib, json, math, re, zipfile

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT/'master/source/equipment/atwiki_20261009'
OUT = ROOT/'master/source/equipment/review_20261009'
FONT = ROOT/'app/src/main/assets/ui-fonts/Pretendard-Regular.otf'
CATEGORIES = {'グリーブス':('leggings','각반'), 'ネックレス':('necklaces','목걸이'),
 'ベルト':('belts','벨트'), '手袋・腕輪':('gloves','장갑·팔찌'),
 '靴':('shoes','신발'), 'イヤリング':('earrings','귀걸이'), '指輪':('rings','반지')}

def display_name(e):
    label=e['labelSource']
    label=label.replace('ゼムストーン','잼스톤')
    for source,translated in [('青靴','파란신발'),('灰色靴','회색신발'),('緑靴','녹색신발'),('シルクブーツ','실크부츠'),('皮ブーツ','가죽부츠'),('サフィアン','사피안'),('クリムゾン','크림슨'),('マジックブーツ','매직부츠'),('ロオ','로오'),('賢哲','현철'),('紅玉','홍옥'),('珊瑚','산호'),('虎目','호안석'),('サファイア','사파이어'),('古陋','고루'),('緑玉','녹옥'),('体力','체력'),('ブルーセピア','블루세피아'),('シルバーアクアリング','실버아쿠아링'),('ゴールドアクアリング','골드아쿠아링'),('ルビナ','루비나'),('指輪','반지')]:
        label=label.replace(source,translated)
    if e['categorySourceLabel']=='ベルト' and len(e.get('sourceAliases',[]))>1:
        for term,name in [('皮ベルト','가죽벨트'),('銀ベルト','은벨트'),('金ベルト','금벨트'),('昇炎','승염벨트')]:
            if term in label:return name+' (표에서 속성 공용)'
    kor=re.search(r'[（(]([^()（）]*[가-힣][^()（）]*)[）)]',label)
    if kor: return kor.group(1).strip()
    translated=label
    for a,b in [('火炎の','화염의'),('火の','화염의'),('風の','바람의'),('大地の','대지의'),('地の','대지의'),('海の','바다의'),('水の','바다의'),('カン','칸의'),('暗黒の','암흑의'),('生命の','생명의'),('皮','가죽'),('真珠','진주'),('紫水晶','자수정'),('碧玉','벽옥'),('マギラ','마기라(원문)'),('ゼム','젬'),('木の','숲의'),('金の','금속성의'),('銀','은'),('金','금'),('クリスタル','크리스탈'),('ゼムストーン','잼스톤'),('ルーンストーン','룬스톤'),('ルナサ','루나사'),('ネックレス','목걸이'),('ベルト','벨트')]:
        translated=translated.replace(a,b)
    return translated.replace("の","의")

def main():
    OUT.mkdir(exist_ok=True)
    # Git retains the complete original byte archive; materialize missing files
    # when regenerating a review from a fresh checkout.
    if (SRC/'original_sources.zip').exists():
        with zipfile.ZipFile(SRC/'original_sources.zip') as z:
            assert z.testzip() is None
            for member in z.infolist():
                assert Path(member.filename).name==member.filename
                target=SRC/member.filename
                if not target.exists():target.write_bytes(z.read(member))
    entries=json.loads((SRC/'manifest.json').read_text())
    retained=[e for e in entries if e['status']=='RETAINED']
    for e in retained:
        assert hashlib.sha256((SRC/e['path']).read_bytes()).hexdigest()==e['sha256']
        Image.open(SRC/e['path']).verify()
        e['displayName']=display_name(e)
        e['displayNameAuthority']='SOURCE_KOREAN_LABEL_OR_REVIEW_TRANSLATION_NOT_CANONICAL'
        e['sourceArchive']='original_sources.zip'
    font=ImageFont.truetype(str(FONT),19);small=ImageFont.truetype(str(FONT),15);title=ImageFont.truetype(str(FONT),31)
    counts={}
    for category,(slug,name) in CATEGORIES.items():
        rows=[e for e in retained if e['categorySourceLabel']==category];counts[name]=len(rows)
        if not rows:continue
        sheet=Image.new('RGB',(1120,110+math.ceil(len(rows)/4)*220),'#f3f0e7');d=ImageDraw.Draw(sheet)
        d.text((25,15),f'{name} 원본 아이콘 {len(rows)}개 · 추가 확보',font=title,fill='#222222')
        d.text((25,62),'원본 파일 첫 프레임을 정수배 확대 / 이름: 원문 한국어 또는 검수용 번역 / 게임 연결 전',font=small,fill='#555555')
        for i,e in enumerate(rows):
            x=(i%4)*280+10;y=105+(i//4)*220
            d.rounded_rectangle((x,y,x+270,y+207),9,fill='white')
            original=Image.open(SRC/e['path']).convert('RGBA');factor=max(1,min(4,120//max(original.size)))
            enlarged=original.resize((original.width*factor,original.height*factor),Image.Resampling.NEAREST)
            d.rectangle((x+70,y+10,x+200,y+140),fill='#282a2e')
            sheet.paste(enlarged,(x+135-enlarged.width//2,y+75-enlarged.height//2),enlarged)
            name=e['displayName'];chunks=[name[k:k+16] for k in range(0,len(name),16)]
            for line,text in enumerate(chunks[:2]):d.text((x+10,y+143+line*22),text,font=font,fill='#222222')
            d.text((x+10,y+187),f'{original.width}×{original.height} / {Image.open(SRC/e["path"]).format}',font=small,fill='#666666')
        sheet.save(OUT/(slug+'_review.png'))
    (SRC/'manifest.json').write_text(json.dumps(entries,ensure_ascii=False,indent=2)+'\n')
    with (OUT/'image_inventory.csv').open('w',newline='') as f:
        w=csv.writer(f);w.writerow(['Category','ReviewName','OriginalSourceLabel','File','Size','SHA256','SourceURL','SourceAliases'])
        for e in retained:w.writerow([CATEGORIES[e['categorySourceLabel']][1],e['displayName'],e['labelSource'],e['path'],'x'.join(map(str,e['size'])),e['sha256'],e['sourceUrl'],' | '.join(e['sourceAliases'])])
    audit={'status':'SOURCE_RECOVERED_REVIEW_PENDING','countsByCategory':counts,'retainedImageFiles':len(retained),
      'uniqueSha256':len({e['sha256'] for e in retained}),'unavailable':len(entries)-len(retained),
      'previousBatch':'11 files =5 icons+6 tooltips,6 item references; not11 equipment icons',
      'runtime':'New GIFs are source/review evidence only; no runtime image replacements in this commit',
      'phoneCombatFeel':'EXCLUDED_USER_REPORTED_ALREADY_DONE','sourceUrl':'https://w.atwiki.jp/takesi100/pages/217.html',
      'sourceScope':'Historical Korean-game fan wiki written in Japanese. Names and values require current/canonical cross-check; table reuses some images across elemental variants.'}
    (OUT/'audit.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2)+'\n')
    circle_names={'가죽각반':(2,11,-2),'동각반':(3,41,-4),'은각반':(4,71,-6),'금각반':(5,99,-8)}
    with (OUT/'circle_coverage.csv').open('w',newline='') as f:
        w=csv.writer(f);w.writerow(['Item','Circle','SourceMinLevel','SourceAC','ImageStatus','ImagePath','SourceURL'])
        for name,(circle,level,ac) in circle_names.items():
            e=next((e for e in retained if e['displayName']==name),None)
            w.writerow([name,circle,level,ac,'RETAINED_REVIEW_PENDING' if e else 'UNAVAILABLE',e['path'] if e else '',e['sourceUrl'] if e else ''])
    assert all(any(e['displayName']==name for e in retained) for name in circle_names),'Missing baseline legging icon'
    readme='''# 장비 이미지 전체 검수\n\n앞서 확보한11개는 아이콘5개와 능력치캡처6개(6종 자료)다. 기존11개는 previous_11/ 및 previous_11_review.png에 전부 포함했다. 실제 게임 화면 캡처이며 JPEG 원본을 유지했다. 골든플레이트각반은 금각반과 별개다. 흑요석워리어목걸이는 툴팁만 있다.\n\n이번 추가 수집은 한국판 어둠의전설을 다룬 일본어 팬 위키의 오래된 장비 표와 원본 GIF다. 부위별로 보관했고 가죽각반(2서클/Lv11), 동각반(3서클/Lv41), 은각반(4서클/Lv71), 금각반(5서클/Lv99)의 이미지도 각자 포함했다. 서클 정보와 AC는 넥슨 커뮤니티1641/6222의 역사 자료와 교차 확인했다. 이름이 일본어만 있는 행의 한국어 표시는 검수용 번역이며 canonical 명칭 확정이 아니다. 일부 속성별 벨트는 표에서 같은 아이콘을 재사용한다. 이를 속성마다 다른 원본 그림을 확보했다고 세지 않았다.\n\n출처: https://minimob.tistory.com/99 / https://w.atwiki.jp/takesi100/pages/217.html / https://w.atwiki.jp/takesi100/pages/155.html / https://lod.nexon.com/community/game/1641?SearchBoard=1\n\n새 원본 이미지는 검수 대기 자료로 저장했다. 현재 게임에 전체 연결됐다고 주장하지 않는다. 누락/접근실패는 audit.json과 원본 manifest에 기록했다. 성능·직업·착용조건은 별도 출처 확인이 필요하다. 휴대폰 타격감은 완료/제외.\n'''
    (OUT/'README.md').write_text(readme)
    with zipfile.ZipFile(SRC/'original_sources.zip','w',zipfile.ZIP_DEFLATED) as z:
        for p in sorted(SRC.iterdir()):
            if p.suffix in ['.html','.json'] or p.name in {e['path'] for e in retained}:z.write(p,p.name)
    archive=OUT/'PROJECT_DARK_equipment_image_review.zip'
    old=ROOT/'master/source/equipment/accessories_20261009'
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
        for e in retained:z.write(SRC/e['path'],'new_original_icons/'+e['path'])
        z.write(SRC/'manifest.json','new_original_icons/manifest.json')
        for p in sorted(old.glob('*.jpg')):z.write(p,'previous_11/'+p.name)
        z.write(old/'image_manifest.json','previous_11/image_manifest.json')
        for p in sorted((ROOT/'master/source/items/tistory_20261009').glob('*.png')):z.write(p,'earlier_references/'+p.name)
        z.write(ROOT/'app/src/main/assets/equipment-icons/it_ring_threelinegold.png','earlier_references/three_line_gold_ring_extracted.png')
        for p in sorted(OUT.iterdir()):
            if p.suffix in ['.png','.csv','.md','.json']:z.write(p,p.name)
    with zipfile.ZipFile(archive) as z:assert z.testzip() is None
    print(json.dumps(audit,ensure_ascii=False))

if __name__=='__main__':main()
