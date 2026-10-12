package com.projectdark.mobile.world;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import com.projectdark.mobile.world.AbelTownGeometry;
import com.projectdark.mobile.world.PoteFieldRenderer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/* loaded from: classes3.dex */
public final class AbelTownRenderer {
    private static final int CHUNK = 256;
    private static final int GUTTER = 2;
    private static final int LIMIT = 32;
    private final Context context;
    private final Paint p = new Paint();
    private final Map<String, Bitmap> assets = new HashMap();
    private final LinkedHashMap<Long, Bitmap> chunks = new LinkedHashMap<>(32, 0.75f, true);
    private final RectF dst = new RectF();
    private final Path rim = new Path();
    private final Path slab = new Path();
    private final Paint floorPaint = new Paint();
    private final Map<String, BitmapShader> materials = new HashMap();
    private final Map<String, Path> floorAreas = new LinkedHashMap();
    private final Path waterRim = new Path();

    private static float fx(float u, float v) {
        return ((u - v) * 32.0f) + 1536.0f;
    }

    private static float fy(float u, float v) {
        return ((u + v) * 16.0f) + 96.0f;
    }

    private static void diamond(Path path, float u, float v, float radius) {
        float x = fx(u, v);
        float y = fy(u, v);
        path.moveTo(x, y - (radius * 32.0f));
        path.lineTo((radius * 64.0f) + x, y);
        path.lineTo(x, (32.0f * radius) + y);
        path.lineTo(x - (64.0f * radius), y);
        path.close();
    }

    private void floorGeometry() {
        String[] strArr = {"stone_material", "cobble_material", "water_material", "grass_material"};
        for (int i = 0; i < 4; i++) {
            String kind = strArr[i];
            this.floorAreas.put(kind, new Path());
        }
        for (int u = 0; u <= 44; u++) {
            for (int v = 0; v <= 44; v++) {
                String kind2 = AbelTownGeometry.waterCell(u, v) ? "water_material" : AbelTownGeometry.garden(u, v) ? "grass_material" : AbelTownGeometry.road(u, v) ? "cobble_material" : "stone_material";
                diamond(this.floorAreas.get(kind2), u, v, 0.5f);
                if (AbelTownGeometry.waterCell(u, v)) {
                    float x = fx(u, v);
                    float y = fy(u, v);
                    if (!AbelTownGeometry.waterCell(u - 1, v)) {
                        this.waterRim.moveTo(x - 32.0f, y);
                        this.waterRim.lineTo(x, y - 16.0f);
                    }
                    if (!AbelTownGeometry.waterCell(u, v - 1)) {
                        this.waterRim.moveTo(x, y - 16.0f);
                        this.waterRim.lineTo(x + 32.0f, y);
                    }
                    if (!AbelTownGeometry.waterCell(u + 1, v)) {
                        this.waterRim.moveTo(x + 32.0f, y);
                        this.waterRim.lineTo(x, y + 16.0f);
                    }
                    if (!AbelTownGeometry.waterCell(u, v + 1)) {
                        this.waterRim.moveTo(x, 16.0f + y);
                        this.waterRim.lineTo(x - 32.0f, y);
                    }
                }
            }
        }
    }

    public AbelTownRenderer(Context context) {
        this.context = context;
        this.p.setFilterBitmap(false);
        this.p.setAntiAlias(false);
        floorGeometry();
    }

    private Bitmap image(String name) {
        Bitmap b = this.assets.get(name);
        if (b != null) {
            return b;
        }
        if(name.startsWith("bench_")){b=(name.equals("bench_sw")||name.equals("bench_se"))?new MillesSourceStyle(context.getAssets()).completeBench(name.equals("bench_se")?"bench_video_cutout_02":"bench_video_cutout_01"):completeBench(name);assets.put(name,b);return b;}
        try {
            InputStream in = this.context.getAssets().open("abel-town-v144/" + name + ".png");
            try {
                Bitmap b2 = BitmapFactory.decodeStream(in);
                if (b2 == null) {
                    throw new IOException(name);
                }
                b2.prepareToDraw();
                this.assets.put(name, b2);
                if (in != null) {
                    in.close();
                }
                return b2;
            } finally {
            }
        } catch (IOException e) {
            throw new IllegalStateException("Missing Abel town sprite " + name, e);
        }
    }

    public int floorCacheEntries() {
        return this.chunks.size();
    }

    public long floorCachePixelBytes() {
        long size = 0;
        for (Bitmap b : this.chunks.values()) {
            size += b.getAllocationByteCount();
        }
        return size;
    }

    public void prepare() {
        String[] strArr = {"stone", "cobble", "water", "grass", "bench_ne", "bench_nw", "bench_se", "bench_sw", "stone_material", "cobble_material", "water_material", "grass_material", "lamp", "notice", "fence", "tree", "fountain", "church", "inn", "bank", "forge", "house"};
        for (int i = 0; i < 22; i++) {
            String s = strArr[i];
            image(s);
        }
    }

    public void draw(Canvas c,WorldRuntimeAdapter w,List<PoteFieldRenderer.ActorDraw> actors){
        float ox=w.camera().cameraX(),oy=w.camera().cameraY();c.drawColor(0xff474f53);
        for(int cy=(int)Math.floor(oy/CHUNK);cy<=(int)Math.floor((oy+c.getHeight())/CHUNK);cy++)for(int cx=(int)Math.floor(ox/CHUNK);cx<=(int)Math.floor((ox+c.getWidth())/CHUNK);cx++){
            long key=((long)cx<<32)^(cy&0xffffffffL);Bitmap b=chunks.get(key);
            if(b==null){b=Bitmap.createBitmap(CHUNK+GUTTER*2,CHUNK+GUTTER*2,Bitmap.Config.ARGB_8888);paintFloor(new Canvas(b),cx*CHUNK-GUTTER,cy*CHUNK-GUTTER);chunks.put(key,b);if(chunks.size()>LIMIT){Iterator<Map.Entry<Long,Bitmap>> it=chunks.entrySet().iterator();it.next().getValue().recycle();it.remove();}}
            dst.set(cx*CHUNK-GUTTER-ox,cy*CHUNK-GUTTER-oy,(cx+1)*CHUNK+GUTTER-ox,(cy+1)*CHUNK+GUTTER-oy);c.drawBitmap(b,null,dst,p);
        }
        List<PoteFieldRenderer.ActorDraw> ordered=new ArrayList<>(actors);
        for(AbelTownGeometry.Building b:AbelTownGeometry.BUILDINGS)ordered.add(new PoteFieldRenderer.ActorDraw(b.x(),b.y(),280,b.width,()->{building(c,b,ox,oy);if(b.interior!=null){p.setColor(0xff27343a);p.setTextSize(9);p.setTextAlign(Paint.Align.CENTER);c.drawText(b.title,b.doorX()-ox,b.doorY()-oy+20,p);p.setTextAlign(Paint.Align.LEFT);}}));
        for(AbelTownGeometry.Prop prop:AbelTownGeometry.PROPS)ordered.add(new PoteFieldRenderer.ActorDraw(prop.x,prop.y,120,prop.width,()->sprite(c,prop.asset,prop.x-ox,prop.y-oy,prop.width,prop.mirror)));
        ordered.sort(Comparator.comparingDouble(a->a.y));for(PoteFieldRenderer.ActorDraw a:ordered)a.draw.run();
    }

    private void building(Canvas c, AbelTownGeometry.Building b, float ox, float oy) {
        Bitmap im = image(b.asset);
        float px = b.asset.equals("church") ? 114.5f : b.asset.equals("inn") ? 106.0f : b.asset.equals("bank") ? 90.0f : b.asset.equals("forge") ? 117.0f : 108.5f;
        float py = b.asset.equals("church") ? 267.0f : b.asset.equals("inn") ? 269.0f : b.asset.equals("bank") ? 275.0f : b.asset.equals("forge") ? 265.0f : 256.0f;
        float scale = b.width / im.getWidth();
        sprite(c, b.asset, (b.doorX() - ((px - (im.getWidth() / 2.0f)) * scale)) - ox, (b.doorY() + ((im.getHeight() - py) * scale)) - oy, b.width, false);
    }

    private void material(Canvas c, String name, Path area) {
        BitmapShader shader = this.materials.get(name);
        if (shader == null) {
            shader = new BitmapShader(image(name), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            this.materials.put(name, shader);
        }
        this.floorPaint.setShader(shader);
        this.floorPaint.setStyle(Paint.Style.FILL);
        this.floorPaint.setColor(-1);
        this.floorPaint.setAlpha(255);
        this.floorPaint.setAntiAlias(false);
        c.drawPath(area, this.floorPaint);
    }

    private void paintFloor(Canvas c,float ox,float oy){
        c.drawColor(0xff474f53);c.save();c.translate(-ox,-oy);
        // Independent artwork grid: the original large stone slabs are twice the walk lattice.
        material(c,"stone_material",floorAreas.get("stone_material"));
        c.save();c.clipPath(floorAreas.get("stone_material"));floorPaint.setShader(null);floorPaint.setAntiAlias(false);floorPaint.setStyle(Paint.Style.FILL);floorPaint.setAlpha(255);
        for(int u=-2;u<=46;u+=2)for(int v=-2;v<=46;v+=2){float x=fx(u,v),y=fy(u,v);if(x<ox-70||x>ox+c.getWidth()+70||y<oy-40||y>oy+c.getHeight()+40)continue;
            slab.reset();diamond(slab,u,v,.985f);int tone=((u*13+v*7)&3);floorPaint.setColor(new int[]{0xffb4c0bd,0xffbac5c1,0xffb6c1bd,0xffbdc7c3}[tone]);floorPaint.setAlpha(24);c.drawPath(slab,floorPaint);
            floorPaint.setColor(0xffc6cec6);floorPaint.setAlpha(85);floorPaint.setStrokeWidth(1);c.drawLine(x-63,y,x,y-31,floorPaint);c.drawLine(x,y-31,x+63,y,floorPaint);
        }c.restore();
        material(c,"cobble_material",floorAreas.get("cobble_material"));floorPaint.setShader(null);floorPaint.setStyle(Paint.Style.FILL);floorPaint.setColor(0xffb3b8ad);floorPaint.setAlpha(65);c.drawPath(floorAreas.get("cobble_material"),floorPaint);
        fill(c,floorAreas.get("water_material"),0xff23617c);c.save();c.clipPath(floorAreas.get("water_material"));
        // World-phased quiet ripples; no paving-derived repeated water bitmap.
        for(int yy=(int)oy/18*18;yy<oy+c.getHeight()+18;yy+=18)for(int xx=(int)ox/31*31;xx<ox+c.getWidth()+31;xx+=31){int h=hash(xx,yy);if((h&3)==0)continue;float x=xx+(h&15),y=yy+((h>>>4)&7);floorPaint.setColor((h&4)==0?0xff33758b:0xff2c6b83);floorPaint.setStrokeWidth(1);c.drawLine(x,y,x+9+(h&7),y-1,floorPaint);if((h&7)==1){floorPaint.setColor(0xff49879a);c.drawLine(x+2,y+2,x+6,y+2,floorPaint);}}
        c.restore();
        fill(c,floorAreas.get("grass_material"),0xff47662e);c.save();c.clipPath(floorAreas.get("grass_material"));
        for(int yy=(int)oy/5*5;yy<oy+c.getHeight()+5;yy+=5)for(int xx=(int)ox/9*9;xx<ox+c.getWidth()+9;xx+=9){int h=hash(xx,yy);floorPaint.setColor((h&1)==0?0xff526e34:0xff3e5d29);float x=xx+(h&7),y=yy+((h>>>3)&3);c.drawLine(x,y,x+2,y-1,floorPaint);}
        c.restore();floorPaint.setShader(null);floorPaint.setStyle(Paint.Style.STROKE);floorPaint.setStrokeWidth(7);floorPaint.setColor(0xff536b70);c.drawPath(waterRim,floorPaint);floorPaint.setStrokeWidth(2);floorPaint.setColor(0xffb6c4bc);c.drawPath(waterRim,floorPaint);floorPaint.setStyle(Paint.Style.FILL);c.restore();
    }
    private static int hash(int x,int y){int h=x*374761393+y*668265263;h=(h^(h>>>13))*1274126177;return h^(h>>>16);}
    private void fill(Canvas c,Path area,int color){floorPaint.setShader(null);floorPaint.setStyle(Paint.Style.FILL);floorPaint.setColor(color);floorPaint.setAlpha(255);floorPaint.setAntiAlias(false);c.drawPath(area,floorPaint);}
    /** Adapted complete iron supports; four geometric orientations with a registered frontmost foot. */
    private Bitmap completeBench(String name){
        Bitmap b=Bitmap.createBitmap(72,68,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint q=new Paint();q.setAntiAlias(false);q.setStrokeCap(Paint.Cap.SQUARE);
        boolean alongDown=name.endsWith("sw")||name.endsWith("ne");float ax=24,ay=alongDown?12:-12;
        float bx=name.endsWith("se")||name.endsWith("ne")?7:-7,by=name.endsWith("se")||name.endsWith("sw")?3.5f:-3.5f;
        float cx=36,gy=52,sy=gy-19;
        // Feet and supports meet the same ground projection; neither cutouts nor color-key matting.
        for(int end:new int[]{-1,1})for(int side:new int[]{-1,1}){float x=cx+end*ax*.86f+side*bx*.72f,y=sy+end*ay*.86f+side*by*.72f;
            q.setColor(0xff20282a);q.setStrokeWidth(4);c.drawLine(x,y,x+end*1.5f,y+17,q);q.setColor(0xff566065);q.setStrokeWidth(1);c.drawLine(x-1,y+2,x-1,y+17,q);q.setColor(0xff222a2d);c.drawRect(x-4,y+17,x+5,y+20,q);
        }
        // Back slats lie on the opposite side from the pool-facing seat edge.
        for(int slat=0;slat<3;slat++){float y=sy-by-17+slat*4;wood(c,q,cx-ax-bx,y-ay,cx+ax-bx,y+ay,3.5f);}
        for(int strip=0;strip<4;strip++){float t=-1+strip*.6f;wood(c,q,cx-ax+t*bx,sy-ay+t*by,cx+ax+t*bx,sy+ay+t*by,3);}
        for(int end:new int[]{-1,1}){q.setColor(0xff20292b);q.setStrokeWidth(2);c.drawLine(cx+end*ax-bx,sy+end*ay-by-18,cx+end*ax-bx,sy+end*ay-by,q);Path arm=new Path();arm.moveTo(cx+end*ax-bx,sy+end*ay-by-7);arm.quadTo(cx+end*ax+bx+end*3,sy+end*ay+by-12,cx+end*ax+bx,sy+end*ay+by);c.drawPath(arm,q);}
        return b;
    }
    private void wood(Canvas c,Paint q,float x0,float y0,float x1,float y1,float width){q.setStrokeWidth(width);q.setColor(0xff713b20);c.drawLine(x0,y0+1,x1,y1+1,q);q.setStrokeWidth(width-1);q.setColor(0xffb77735);c.drawLine(x0,y0,x1,y1,q);q.setStrokeWidth(1);q.setColor(0xffd19950);c.drawLine(x0,y0-1,x1,y1-1,q);}

    private void sprite(Canvas c, String name, float x, float y, float width, boolean mirror) {
        Bitmap b = image(name);
        float h = (b.getHeight() * width) / b.getWidth();
        if ((width / 2.0f) + x < 0.0f || x - (width / 2.0f) > c.getWidth() || y < 0.0f || y - h > c.getHeight()) {
            return;
        }
        c.save();
        if (mirror) {
            c.translate(x * 2.0f, 0.0f);
            c.scale(-1.0f, 1.0f);
        }
        this.dst.set(x - (width / 2.0f), y - h, (width / 2.0f) + x, y);
        this.p.setColor(-1);
        this.p.setAlpha(255);
        c.drawBitmap(b, (Rect) null, this.dst, this.p);
        c.restore();
    }
}
