package com.projectdark.mobile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Deterministic, checksummed codec for RPG mutable state. Canonical definitions are never serialized. */
public final class RpgSaveCodec {
  public enum Status { DECODED, CHECKSUM_MISMATCH, UNSUPPORTED_SCHEMA, MALFORMED }
  public static final class DecodeResult {
    public final Status status;
    public final RpgSaveSnapshot snapshot;
    private DecodeResult(Status status,RpgSaveSnapshot snapshot){this.status=status;this.snapshot=snapshot;}
  }

  private static final int MAGIC=0x50445247; // PDRG
  private static final int DIGEST_BYTES=32;
  private static final int MAX_COLLECTION_ENTRIES=4096;

  public byte[] encode(RpgSaveSnapshot snapshot){
    if(snapshot==null)throw new IllegalArgumentException("snapshot is required");
    try{
      ByteArrayOutputStream payloadBytes=new ByteArrayOutputStream();
      DataOutputStream out=new DataOutputStream(payloadBytes);
      out.writeInt(MAGIC);
      out.writeInt(snapshot.schemaVersion);
      writeString(out,snapshot.progressionNode==null?null:snapshot.progressionNode.name());
      writeString(out,snapshot.currentJobCode);
      writeNullableInt(out,snapshot.normalLevel);
      writeNullableLong(out,snapshot.normalExp);
      writeNullableLong(out,snapshot.gold);
      out.writeLong(snapshot.lastCombatSequence);
      writeIntMap(out,snapshot.inventory);
      writeStringMap(out,snapshot.equipmentBySlot);
      writeStringSet(out,snapshot.learnedActionIds);
      out.flush();
      byte[] payload=payloadBytes.toByteArray(),digest=sha256(payload);
      ByteArrayOutputStream result=new ByteArrayOutputStream(payload.length+DIGEST_BYTES);
      result.write(payload);result.write(digest);
      return result.toByteArray();
    }catch(IOException impossible){throw new IllegalStateException("in-memory save encoding failed",impossible);}
  }

  public DecodeResult decode(byte[] bytes){
    if(bytes==null||bytes.length<=DIGEST_BYTES)return result(Status.MALFORMED,null);
    byte[] payload=Arrays.copyOf(bytes,bytes.length-DIGEST_BYTES);
    byte[] stored=Arrays.copyOfRange(bytes,bytes.length-DIGEST_BYTES,bytes.length);
    if(!MessageDigest.isEqual(stored,sha256(payload)))return result(Status.CHECKSUM_MISMATCH,null);
    try(DataInputStream in=new DataInputStream(new ByteArrayInputStream(payload))){
      if(in.readInt()!=MAGIC)return result(Status.MALFORMED,null);
      int schema=in.readInt();
      if(schema!=RpgSaveSnapshot.CURRENT_SCHEMA_VERSION)return result(Status.UNSUPPORTED_SCHEMA,null);
      String nodeName=readString(in),job=readString(in);
      RpgProgressionState.ProgressionNode node=nodeName==null?null:RpgProgressionState.ProgressionNode.valueOf(nodeName);
      Integer level=readNullableInt(in);
      Long exp=readNullableLong(in),gold=readNullableLong(in);
      long lastSequence=in.readLong();
      Map<String,Integer> inventory=readIntMap(in);
      Map<String,String> equipment=readStringMap(in);
      Set<String> actions=readStringSet(in);
      if(in.available()!=0)return result(Status.MALFORMED,null);
      return result(Status.DECODED,new RpgSaveSnapshot(schema,node,job,level,exp,gold,lastSequence,inventory,equipment,actions));
    }catch(Exception malformed){return result(Status.MALFORMED,null);}
  }

  private static DecodeResult result(Status status,RpgSaveSnapshot snapshot){return new DecodeResult(status,snapshot);}
  private static void writeString(DataOutputStream out,String value)throws IOException{out.writeBoolean(value!=null);if(value!=null)out.writeUTF(value);}
  private static String readString(DataInputStream in)throws IOException{return in.readBoolean()?in.readUTF():null;}
  private static void writeNullableInt(DataOutputStream out,Integer value)throws IOException{out.writeBoolean(value!=null);if(value!=null)out.writeInt(value);}
  private static Integer readNullableInt(DataInputStream in)throws IOException{return in.readBoolean()?in.readInt():null;}
  private static void writeNullableLong(DataOutputStream out,Long value)throws IOException{out.writeBoolean(value!=null);if(value!=null)out.writeLong(value);}
  private static Long readNullableLong(DataInputStream in)throws IOException{return in.readBoolean()?in.readLong():null;}

  private static void writeIntMap(DataOutputStream out,Map<String,Integer> values)throws IOException{
    Map<String,Integer> sorted=new TreeMap<>(values);out.writeInt(sorted.size());
    for(Map.Entry<String,Integer> entry:sorted.entrySet()){writeString(out,entry.getKey());out.writeInt(entry.getValue());}
  }
  private static Map<String,Integer> readIntMap(DataInputStream in)throws IOException{
    int size=readSize(in);Map<String,Integer> result=new LinkedHashMap<>();
    for(int i=0;i<size;i++){String key=required(readString(in));if(result.put(key,in.readInt())!=null)throw new IOException("duplicate key");}return result;
  }
  private static void writeStringMap(DataOutputStream out,Map<String,String> values)throws IOException{
    Map<String,String> sorted=new TreeMap<>(values);out.writeInt(sorted.size());
    for(Map.Entry<String,String> entry:sorted.entrySet()){writeString(out,entry.getKey());writeString(out,entry.getValue());}
  }
  private static Map<String,String> readStringMap(DataInputStream in)throws IOException{
    int size=readSize(in);Map<String,String> result=new LinkedHashMap<>();
    for(int i=0;i<size;i++){String key=required(readString(in)),value=required(readString(in));if(result.put(key,value)!=null)throw new IOException("duplicate key");}return result;
  }
  private static void writeStringSet(DataOutputStream out,Set<String> values)throws IOException{
    Set<String> sorted=new TreeSet<>(values);out.writeInt(sorted.size());for(String value:sorted)writeString(out,value);
  }
  private static Set<String> readStringSet(DataInputStream in)throws IOException{
    int size=readSize(in);Set<String> result=new LinkedHashSet<>();for(int i=0;i<size;i++)if(!result.add(required(readString(in))))throw new IOException("duplicate value");return result;
  }
  private static int readSize(DataInputStream in)throws IOException{int size=in.readInt();if(size<0||size>MAX_COLLECTION_ENTRIES)throw new IOException("invalid collection size");return size;}
  private static String required(String value)throws IOException{if(value==null||value.isEmpty())throw new IOException("required string missing");return value;}
  private static byte[] sha256(byte[] bytes){try{return MessageDigest.getInstance("SHA-256").digest(bytes);}catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}}
}
