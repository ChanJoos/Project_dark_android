"""Preserve an actual continuous video crop for review. No generated spell identity or live mapping."""
from pathlib import Path
import subprocess,json,hashlib,zipfile,io,argparse
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];p=argparse.ArgumentParser();p.add_argument('video',type=Path);a=p.parse_args();S=R/'master/source/narcoli_video_v252';S.mkdir(exist_ok=True);O=R/'review252';O.mkdir(exist_ok=True)
# In this supplied recording the wall glyph is isolated at x1195,y0,180x210.
start=.2;fps=25;duration=.6;crop=[1195,0,180,210]
raw=subprocess.check_output(['ffmpeg','-v','error','-ss',str(start),'-i',str(a.video),'-t',str(duration),'-vf',f'fps={fps},crop=180:210:1195:0','-f','rawvideo','-pix_fmt','rgb24','-']);size=180*210*3;assert len(raw)%size==0;frames=[Image.frombytes('RGB',(180,210),raw[i:i+size])for i in range(0,len(raw),size)];assert len(frames)==15
records=[]
with zipfile.ZipFile(S/'video_frames.zip','w',zipfile.ZIP_DEFLATED)as z:
 for i,im in enumerate(frames):
  b=io.BytesIO();im.save(b,format='PNG');name=f'frame-{i:02}.png';z.writestr(name,b.getvalue());records.append(dict(file=name,requestedSampleSeconds=round(start+i/fps,3),rgbSha256=hashlib.sha256(im.tobytes()).hexdigest()))
# Uniform2xnearest preview only. Lossless sampled source RGB retained above.
big=[im.resize((360,420),Image.Resampling.NEAREST)for im in frames];poster=Image.new('RGB',(360*5,420))
for i,n in enumerate([0,3,6,9,12]):poster.paste(big[n],(i*360,0))
poster.save(O/'narcoli-video-frames.png');poster.save(S/'review-frames.png')
# GIF necessarily quantizes RGB; no actor removal, color thresholding, or reconstructed pixels.
contact=Image.new('RGB',(360,420*len(big)))
for i,im in enumerate(big):contact.paste(im,(0,i*420))
pal=contact.quantize(colors=256,dither=Image.Dither.NONE);gif=[im.quantize(palette=pal,dither=Image.Dither.NONE)for im in big]
gif[0].save(O/'narcoli-video.gif',save_all=True,append_images=gif[1:],duration=40,loop=0,optimize=False);(S/'narcoli-video.gif').write_bytes((O/'narcoli-video.gif').read_bytes())
receipt=dict(status='VIDEO_EXTRACTED_REVIEW_NOT_APPLIED',video=a.video.name,videoSha256=hashlib.sha256(a.video.read_bytes()).hexdigest(),durationSeconds=duration,startSeconds=start,sampleFps=fps,crop=crop,frames=records,interpretation='User-requested Narcoli review: pink Z appears small, grows, rises and disperses. This is the supplied205939video, not the earlier205405deathvideo. No new runtime mapping. Background preserved; no invented or painted pixels.',preview='2xnearest;GIF palette quantization only; exact sampled RGB retained in video_frames.zip')
(S/'receipt.json').write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n');print('Continuous15video samples retained;600msreviewGIF; no runtime application')
