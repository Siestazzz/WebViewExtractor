# Isolating the v11/v12 login-host regression

The full ten-App batch loses fifteen source expectations for HotelFlagShipLoginActivity relative to v10. The same APK and frozen jars, analyzed with only this host scheduled, match all fifteen in both versions. Both produce fourteen fact records, and the same-oracle semantic delta is empty. This distinguishes retained propagation capability from insufficient progress in the full batch; it does not make the production regression acceptable or prove a particular cost source.

v10:75.363s total /19,669 contexts. v12:79.948s /21,613 contexts. Each reaches local context limits. These are diagnostic runs with one host, not APK performance measurements or acceptance results. The original full-batch misses remain in every score.

Reproduce from the same frozen jars, with eight CPUs and16GiB JVM heap:

```sh
mkdir -p test/runs/single-host-probe-classes
javac -cp test/runs/generic-v12.jar -d test/runs/single-host-probe-classes test/diagnostics/SingleHostProbe.java
timeout --signal=TERM --kill-after=2 180 taskset -c 32-39 java -Xmx16g -XX:ActiveProcessorCount=8 -cp test/runs/single-host-probe-classes:test/runs/generic-v12.jar org.example.SingleHostProbe test/apks/ctrip.android.view.apk ctrip.android.hotel.order.view.flagship.HotelFlagShipLoginActivity test/runs/ctrip-login-single-v12/capabilities.json
```

Repeat with generic-v10.jar and a different output directory. The probe is generic and accepts any host; the host name appears only in this diagnostic invocation, never in production rules. Structured results retain report/source hashes and per-host coverage.
