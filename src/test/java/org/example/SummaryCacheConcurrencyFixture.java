package org.example;

import java.util.*;
import java.util.concurrent.*;
import org.jf.dexlib2.*;
import org.jf.dexlib2.immutable.instruction.*;
import static org.example.CapabilitySelfTest.*;

/** A slow contextual resolver must not block reads of published base summaries. */
final class SummaryCacheConcurrencyFixture {
    public static void main(String[] args)throws Exception {run();}
    static void run()throws Exception {
        var plain=method("Lcache/Methods;","plain",List.of(),9,0,List.of(end()),false);
        var guard=method("Lcache/Methods;","guard",List.of("Z"),9,1,List.of(new ImmutableInstruction21t(Opcode.IF_EQZ,0,3),end(),end()),false);
        var flow=new DexFlow(new CapabilityIndex(),System.nanoTime()+30_000_000_000L);
        var expected=flow.summary(plain);flow.summary(guard);
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(8);
        try {
            var writer=pool.submit(()->flow.summary(guard,v->{entered.countDown();try{release.await();}catch(InterruptedException ex){throw new RuntimeException(ex);}return DexFlow.V.literal("number","0");}));
            check(entered.await(5,TimeUnit.SECONDS),"Resolver never entered guarded summary");
            var reader=pool.submit(()->flow.summary(plain));
            try {check(reader.get(2,TimeUnit.SECONDS)==expected,"Base cache returned a different summary");}
            catch(TimeoutException ex){throw new AssertionError("Published base read blocked behind unrelated contextual resolver",ex);}
            finally {release.countDown();}
            writer.get(5,TimeUnit.SECONDS);
            var fresh=new DexFlow(new CapabilityIndex(),System.nanoTime()+30_000_000_000L);
            var start=new CountDownLatch(1);List<Future<DexFlow.Summary>> readers=new ArrayList<>();
            for(int i=0;i<8;i++)readers.add(pool.submit(()->{start.await();return fresh.summary(plain);}));
            start.countDown();var first=readers.get(0).get(5,TimeUnit.SECONDS);
            for(var f:readers)check(f.get(5,TimeUnit.SECONDS)==first,"Concurrent miss published duplicate base summaries");
            check(fresh.decoded==1&&fresh.cache.size()==1,"Concurrent miss inflated decode count/cache");
        } finally {release.countDown();pool.shutdownNow();}
        System.out.println("SummaryCacheConcurrencyFixture PASS: published base reads bypass contextual lock; concurrent misses decode once.");
    }
}
