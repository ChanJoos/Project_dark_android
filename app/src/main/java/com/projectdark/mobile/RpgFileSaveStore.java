package com.projectdark.mobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** RPG-owned crash-safe file store. Callers supply an app-private directory. */
public final class RpgFileSaveStore {
  public enum SaveStatus { SAVED, IO_ERROR }
  public enum LoadStatus { LOADED, LOADED_BACKUP, EMPTY, CORRUPT, IO_ERROR }
  public static final class LoadResult {
    public final LoadStatus status;
    public final RpgSaveSnapshot snapshot;
    private LoadResult(LoadStatus status,RpgSaveSnapshot snapshot){this.status=status;this.snapshot=snapshot;}
  }

  private final File primary,temp,backup;
  private final RpgSaveCodec codec=new RpgSaveCodec();

  public RpgFileSaveStore(File privateDirectory,String slotId){
    if(privateDirectory==null)throw new IllegalArgumentException("privateDirectory is required");
    if(slotId==null||!slotId.matches("[A-Za-z0-9._-]+"))throw new IllegalArgumentException("invalid slotId");
    primary=new File(privateDirectory,slotId+".sav");temp=new File(privateDirectory,slotId+".tmp");backup=new File(privateDirectory,slotId+".bak");
  }

  public SaveStatus save(RpgSaveSnapshot snapshot){
    try{
      File parent=primary.getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs()&&!parent.isDirectory())return SaveStatus.IO_ERROR;
      byte[] bytes=codec.encode(snapshot);
      try(FileOutputStream out=new FileOutputStream(temp)){out.write(bytes);out.flush();out.getFD().sync();}
      if(primary.isFile())moveReplace(primary,backup);
      moveReplace(temp,primary);
      return SaveStatus.SAVED;
    }catch(Exception failure){return SaveStatus.IO_ERROR;}
  }

  public LoadResult load(){
    if(!primary.exists()&&!backup.exists())return new LoadResult(LoadStatus.EMPTY,null);
    try{
      RpgSaveCodec.DecodeResult current=decode(primary);
      if(current!=null&&current.status==RpgSaveCodec.Status.DECODED)return new LoadResult(LoadStatus.LOADED,current.snapshot);
      RpgSaveCodec.DecodeResult previous=decode(backup);
      if(previous!=null&&previous.status==RpgSaveCodec.Status.DECODED)return new LoadResult(LoadStatus.LOADED_BACKUP,previous.snapshot);
      return new LoadResult(LoadStatus.CORRUPT,null);
    }catch(IOException failure){return new LoadResult(LoadStatus.IO_ERROR,null);}
  }

  private RpgSaveCodec.DecodeResult decode(File file)throws IOException{return file.isFile()?codec.decode(Files.readAllBytes(file.toPath())):null;}
  private static void moveReplace(File from,File to)throws IOException{
    try{Files.move(from.toPath(),to.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
    catch(AtomicMoveNotSupportedException unsupported){Files.move(from.toPath(),to.toPath(),StandardCopyOption.REPLACE_EXISTING);}
  }
}
