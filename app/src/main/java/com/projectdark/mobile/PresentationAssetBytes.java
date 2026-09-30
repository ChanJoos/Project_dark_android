package com.projectdark.mobile;
import android.content.Context;
import java.io.*;
final class PresentationAssetBytes {
  static byte[] read(Context c,String path)throws IOException{try(InputStream in=c.getAssets().open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
}
