package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
final class PresentationAssetBytes {
  // Renderers only read these atlases. A shared hash-addressed decode avoids
  // allocating the same source once per quick-slot/view and cross-job route.
  private static final Map<String,WeakReference<Bitmap>> bitmaps=new LinkedHashMap<>();
  static synchronized Bitmap bitmap(Context c,String path,String sourceHash)throws IOException{
    String key=path+":"+sourceHash;
    WeakReference<Bitmap> reference=bitmaps.get(key);Bitmap bitmap=reference==null?null:reference.get();
    if(bitmap!=null&&!bitmap.isRecycled())return bitmap;
    try(InputStream in=c.getAssets().open(path)){bitmap=BitmapFactory.decodeStream(in);}
    if(bitmap==null)throw new IOException("Missing presentation bitmap: "+path);
    bitmaps.put(key,new WeakReference<>(bitmap));
    bitmaps.entrySet().removeIf(e->e.getValue().get()==null);
    return bitmap;
  }
  static byte[] read(Context c,String path)throws IOException{try(InputStream in=c.getAssets().open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
}
