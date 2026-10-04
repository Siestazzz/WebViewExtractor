# Fragment transaction identity regression

The synthetic Activity uses the real native Activity.getFragmentManager / FragmentManager.beginTransaction / FragmentTransaction.add(Fragment,String) / commit signatures. Each Fragment installs a distinct bridge registration from onActivityCreated. A single committed transaction must emit first, an uncommitted transaction must emit nothing, and two transactions from the same manager with only the second committed must emit second alone.

The development implementation initially passed the first two controls but emitted both first and second in the two-transaction case. Its transaction identity omitted the beginTransaction instruction offset, merging distinct transactions in one caller. After preserving that offset in the symbolic return and engine identity, all three controls pass. Failed results and compiled-class hashes are retained alongside the post-fix source hashes.

An initial diagnostic accidentally read the generic capability name instead of registration_name; that harness error was corrected before deriving these results. The positive control passes in the corrected pre-fix fixture, which isolates the remaining cross-binding failure.

This proves same-method distinct-callsite isolation only. Source review also identified the need to include the caller method identity for equal offsets in different methods; that follow-up is separate from this measured result. Repeated calls to one helper/allocation site may still merge under the analyzer's bounded object abstraction. XML/navigation installation and transaction removal must be audited separately; this three-case test is not evidence of those semantics.

Run org.example.FragmentTransactionIsolationProbe after compiling it against the appropriate isolated engine classes and the frozen test helpers. Optional modes: single, uncommitted, two. Its default main runs all three and fails on any extra/missing registration. This is a generic fixture with no real-App names.
