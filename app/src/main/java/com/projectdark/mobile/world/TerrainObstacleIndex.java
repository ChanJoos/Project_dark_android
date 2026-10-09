package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Immutable terrain broad phase. Final intersection is exactly the original radius/AABB rule. */
public final class TerrainObstacleIndex {
 private static final int CELL=128;
 private final Map<Long,List<RectF>> cells=new HashMap<>();
 public TerrainObstacleIndex(List<RectF> obstacles){for(RectF r:obstacles)for(int y=cell(r.top);y<=cell(r.bottom);y++)for(int x=cell(r.left);x<=cell(r.right);x++)cells.computeIfAbsent(key(x,y),k->new ArrayList<>()).add(r);}
 private static int cell(float n){return (int)Math.floor(n/CELL);}
 private static long key(int x,int y){return ((long)x<<32)^(y&0xffffffffL);}
 public boolean blocked(float x,float y,float radius){for(int yy=cell(y-radius);yy<=cell(y+radius);yy++)for(int xx=cell(x-radius);xx<=cell(x+radius);xx++){List<RectF> a=cells.get(key(xx,yy));if(a!=null)for(RectF r:a)if(x+radius>r.left&&x-radius<r.right&&y+radius>r.top&&y-radius<r.bottom)return true;}return false;}
}
