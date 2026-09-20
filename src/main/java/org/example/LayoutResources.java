package org.example;

import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Minimal bounded resources.arsc reader for layout paths and aliases.
 * Format: AOSP libs/androidfw/include/androidfw/ResourceTypes.h.
 * Supports sparse/offset16 type tables and compact entries; does not resolve dynamic packages.
 */
final class LayoutResources {
 final ByteBuffer b;
 String[] strings=new String[0];
 LayoutResources(byte[] data){b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);}
 int u8(int p){check(p,1,b.limit());return b.get(p)&255;}
 int u16(int p){check(p,2,b.limit());return b.getShort(p)&65535;}
 int i32(int p){check(p,4,b.limit());return b.getInt(p);}
 void check(int p,int n,int end){if(p<0||n<0||(long)p+n>end)throw new IllegalArgumentException("resource_bounds:"+p);}
 int end(int p,int limit){check(p,8,limit);int size=i32(p+4);int header=u16(p+2);if(header<8||size<header)throw new IllegalArgumentException("resource_chunk_size");check(p,size,limit);return p+size;}
 void read(ApkInventory out){
  if(u16(0)!=2)throw new IllegalArgumentException("not_resource_table");int end=end(0,b.limit());
  for(int p=u16(2);p<end;){int next=end(p,end);if(u16(p)==1)strings=pool(p,next);else if(u16(p)==0x200)pkg(p,next,out);p=next;}
 }
 void pkg(int p,int end,ApkInventory out){
  int header=u16(p+2);if(header<284)throw new IllegalArgumentException("package_header");
  int pkg=i32(p+8),typeOffset=header>=288?i32(p+284):0,types=i32(p+268);
  if(types==0){out.errors.add("resource_inherited_package_types:"+pkg);return;}
  check(p+types,28,end);String[] names=pool(p+types,end(p+types,end));
  for(int c=p+header;c<end;){int next=end(c,end);if(u16(c)==0x201){
   check(c,20,next);int type=u8(c+8);if(type>0&&type<=names.length&&names[type-1].equals("layout"))type(c,next,pkg,type+typeOffset,out);
  }c=next;}
 }
 void type(int p,int end,int pkg,int type,ApkInventory out){
  int header=u16(p+2),flags=u8(p+9),count=i32(p+12),start=i32(p+16);
  if((flags&~3)!=0||(flags&3)==3)throw new IllegalArgumentException("unsupported_type_flags:"+flags);
  if(count<0||count>65536)throw new IllegalArgumentException("resource_entry_count");
  int width=(flags&2)!=0?2:4;check(p+header,count*width,end);
  if(start<header+count*width||start>end-p)throw new IllegalArgumentException("resource_entries_start");
  for(int i=0;i<count;i++){
   int index=i,offset;
   if((flags&1)!=0){index=u16(p+header+i*4);offset=u16(p+header+i*4+2)*4;}
   else if((flags&2)!=0){int n=u16(p+header+i*2);if(n==65535)continue;offset=n*4;}
   else{offset=i32(p+header+i*4);if(offset==-1)continue;}
   if(offset<0)throw new IllegalArgumentException("resource_entry_offset");
   long entry=(long)p+start+offset;if(entry>Integer.MAX_VALUE)throw new IllegalArgumentException("resource_entry_overflow");
   int e=(int)entry;check(e,8,end);int ef=u16(e+2);if((ef&1)!=0){out.errors.add("complex_layout_entry:"+index);continue;}
   int valueType,data;
   if((ef&8)!=0){valueType=ef>>>8;data=i32(e+4);}else{int size=u16(e);if(size<8)throw new IllegalArgumentException("resource_entry_size");int v=e+size;check(v,8,end);valueType=u8(v+3);data=i32(v+4);}
   int id=(pkg<<24)|(type<<16)|index;
   if(valueType==3){if(data<0||data>=strings.length)throw new IllegalArgumentException("resource_string_index");out.layoutResources.computeIfAbsent(id,k->new TreeSet<>()).add(strings[data]);}
   else if(valueType==1)out.layoutAliases.computeIfAbsent(id,k->new TreeSet<>()).add(data);
   else out.errors.add("unsupported_layout_value:"+id+":"+valueType);
  }
 }
 String[] pool(int p,int end){
  check(p,28,end);int header=u16(p+2),count=i32(p+8),flags=i32(p+16),start=i32(p+20);
  if(count<0||count>2000000)throw new IllegalArgumentException("string_count");check(p+header,count*4,end);
  String[] result=new String[count];
  for(int i=0;i<count;i++){
   long absolute=(long)p+start+i32(p+header+i*4);if(absolute<0||absolute>Integer.MAX_VALUE)throw new IllegalArgumentException("string_offset");
   int[] cursor={(int)absolute};
   if((flags&256)!=0){length8(cursor,end);int bytes=length8(cursor,end);check(cursor[0],bytes+1,end);result[i]=new String(b.array(),cursor[0],bytes,StandardCharsets.UTF_8);}
   else{int chars=length16(cursor,end);if(chars>Integer.MAX_VALUE/2-1)throw new IllegalArgumentException("string_length");check(cursor[0],chars*2+2,end);result[i]=new String(b.array(),cursor[0],chars*2,StandardCharsets.UTF_16LE);}
  }
  return result;
 }
 int length8(int[] p,int end){check(p[0],1,end);int n=u8(p[0]++);if((n&128)!=0){check(p[0],1,end);n=((n&127)<<8)|u8(p[0]++);}return n;}
 int length16(int[] p,int end){check(p[0],2,end);int n=u16(p[0]);p[0]+=2;if((n&32768)!=0){check(p[0],2,end);n=((n&32767)<<16)|u16(p[0]);p[0]+=2;}return n;}
}
