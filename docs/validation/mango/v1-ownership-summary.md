# v1 Activity ownership audit

Audited all 70 Activity records in `test/runs/v1/com.hunantv.imgo.activity/0/output/capabilities.json` against independently decompiled source. Counts: valid=65, wrong=0, uncertain=5.

`valid` means at least one Activity-to-WebView/Fragment ownership path is supported. It does not validate every fact attributed to that Activity. Very large repeated fact sets remain a separate over-attribution defect.

## Highest-risk fanout

- `com.mgtv.erlang.ErlangLiveActivity`: 1854 facts
- `com.mgtv.ui.player.VodPlayerPageActivity`: 1357 facts
- `com.mgtv.ui.videoplay.MGVideoPlayActivity`: 775 facts
- `com.mgtv.sanlang.SanlangLiveActivity`: 549 facts
- `com.mgtv.ui.cornucopia.clip.video.VideoClipActivity`: 413 facts
- `com.mgtv.meet.VideoHallGalleryActivity`: 324 facts
- `com.mgtv.meet.square.MeetSquareActivity`: 324 facts
- `com.mgtv.dalang.DalangLiveActivity`: 279 facts
- `com.mgtv.ui.player.chatroom.ChatRoomActivity`: 279 facts
- `com.mg.ec.cards.SmallCardActivity`: 278 facts

## Main error types

- Shared Fragment/helper fanout multiplies the same settings, clients and bridges into many live/player Activities. A legitimate nested Web Fragment does not prove every candidate fact is reachable in every Activity configuration.
- Global settings appear beside Activity facts and must never establish ownership.
- Base-class and SDK wrapper propagation is useful only when the concrete Activity-to-base/factory edge and the returned WebView identity both resolve.
- A class name containing Web/H5 is supporting context, not proof; the JSONL evidence records actual source references.
