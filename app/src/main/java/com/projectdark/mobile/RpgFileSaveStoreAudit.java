package com.projectdark.mobile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/** Process-restart persistence audit including primary corruption and last-known-good fallback. */
public final class RpgFileSaveStoreAudit {
  private RpgFileSaveStoreAudit(){}

  public static boolean verify(){
    Path directory=null;
    try{
      directory=Files.createTempDirectory("project-dark-rpg-save-");
      RpgFileSaveStore store=new RpgFileSaveStore(directory.toFile(),"player-1");
      RpgSaveSnapshot first=snapshot(7L,1);
      if(store.save(first)!=RpgFileSaveStore.SaveStatus.SAVED)return false;
      RpgFileSaveStore.LoadResult firstLoad=store.load();
      if(firstLoad.status!=RpgFileSaveStore.LoadStatus.LOADED||firstLoad.snapshot.lastCombatSequence!=7L)return false;

      RpgSaveSnapshot second=snapshot(8L,2);
      if(store.save(second)!=RpgFileSaveStore.SaveStatus.SAVED)return false;
      RpgFileSaveStore.LoadResult secondLoad=store.load();
      if(secondLoad.status!=RpgFileSaveStore.LoadStatus.LOADED||secondLoad.snapshot.inventory.get("IT_GLOVE_LEATHER")!=2)return false;

      // Simulate a torn/corrupt newest save. Loading must recover the previous complete snapshot.
      Files.write(directory.resolve("player-1.sav"),new byte[]{1,2,3,4});
      RpgFileSaveStore.LoadResult recovered=store.load();
      if(recovered.status!=RpgFileSaveStore.LoadStatus.LOADED_BACKUP)return false;
      if(recovered.snapshot.lastCombatSequence!=7L||recovered.snapshot.inventory.get("IT_GLOVE_LEATHER")!=1)return false;

      byte[] encoded=new RpgSaveCodec().encode(second);encoded[3]^=1;
      if(new RpgSaveCodec().decode(encoded).status!=RpgSaveCodec.Status.CHECKSUM_MISMATCH)return false;
      return true;
    }catch(Exception failure){return false;}
    finally{
      if(directory!=null)try{
        Files.deleteIfExists(directory.resolve("player-1.tmp"));
        Files.deleteIfExists(directory.resolve("player-1.sav"));
        Files.deleteIfExists(directory.resolve("player-1.bak"));
        Files.deleteIfExists(directory);
      }catch(Exception ignored){}
    }
  }

  private static RpgSaveSnapshot snapshot(long sequence,int quantity){
    return new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,
        RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",1,null,null,sequence,
        Collections.singletonMap("IT_GLOVE_LEATHER",quantity),Collections.emptyMap(),Collections.singleton("skill_proto"));
  }

  public static void main(String[] args){if(!verify())throw new IllegalStateException("RPG file save audit failed");}
}
