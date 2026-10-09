"""Paired exact-input collision-work gate. Timings are JVM measurements, not phone FPS."""
import json,pathlib,statistics
root=pathlib.Path('app/build/reports/forest-performance')
a=json.loads((root/'V116_TAPS_BASELINE.json').read_text());b=json.loads((root/'TAPS.json').read_text())
summary=[]
for old,new in zip(a['scenes'],b['scenes']):
 assert old['map']==new['map'] and old['tiles']==new['tiles'] and old['monsters']==new['monsters']
 for key in ('taps','routes'):
  assert len(old[key])==len(new[key])
  for x,y in zip(old[key],new[key]):
   for field in ('x','y','status','steps'): assert x[field]==y[field],(old['map'],key,field,x,y)
 old_calls=sum(r['occupancy'] for r in old['routes']);new_calls=sum(r['occupancy'] for r in new['routes'])
 assert new_calls<old_calls*.5,(old['map'],old_calls,new_calls)
 summary.append(dict(map=old['map'],baselineOccupancy=old_calls,candidateOccupancy=new_calls,baselineTapMedianMs=statistics.median(r['ms'] for r in old['taps']),candidateTapMedianMs=statistics.median(r['ms'] for r in new['taps']),baselineRouteMedianMs=statistics.median(r['ms'] for r in old['routes']),candidateRouteMedianMs=statistics.median(r['ms'] for r in new['routes'])))
(root/'TAP_COMPARISON.json').write_text(json.dumps(dict(baseline=a['source'],candidate=b['source'],scenes=summary,phone='PENDING'),indent=2)+'\n')
print(json.dumps(summary,indent=2))
