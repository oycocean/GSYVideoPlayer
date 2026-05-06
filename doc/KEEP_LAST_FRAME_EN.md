# Keep Last Frame Demo

`KeepLastFrameVideo` is a demo-level player for validating the behavior of keeping the last rendered frame after natural playback completion. It does not change the base player's default completion state and does not affect other player pages globally.

## Goals

- After natural completion, optionally keep the current render view instead of showing the cover immediately.
- Use a flag so the same page can compare keep-last-frame behavior with the default cover state.
- Playback errors, manual release, and page exit still follow the normal release path.
- Fullscreen and normal players copy the flag consistently.

## Demo Entry

Main page entry: `Keep last frame`

Main classes:

- `app/src/main/java/com/example/gsyvideoplayer/KeepLastFrameDemoActivity.java`
- `app/src/main/java/com/example/gsyvideoplayer/video/KeepLastFrameVideo.java`
- `app/src/main/res/layout/activity_keep_last_frame_demo.xml`

## Basic Usage

```java
keepLastFrameVideo.setKeepLastFrameWhenComplete(true);
```

Disable it to restore the default completion state:

```java
keepLastFrameVideo.setKeepLastFrameWhenComplete(false);
```

The demo uses `isLastAutoCompleteRetainedSurface()` to check whether the render view was actually retained for the latest natural completion.

## Implementation Notes

When the flag is enabled, `KeepLastFrameVideo#onAutoCompletion()` does not call the base default completion release flow. It:

- Sets state to `CURRENT_STATE_AUTO_COMPLETE`.
- Keeps the current render view.
- Hides the cover layer and shows completion controls.
- Releases audio focus, network listener, and keep-screen-on flag.
- Dispatches `onAutoComplete`.

`onCompletion()`, replay, and page destroy still use the normal release flow to avoid long-lived resources.

## Notes

- This is a demo-level capability for validating business behavior, not a global default.
- Whether the frame can be kept depends on the current render view and whether the app releases the Surface manually.
- If the business must show cover, ads, or recommendations after completion, keep the default completion state.
- Moving this into base components needs a full design for Surface lifecycle, cover visibility, replay, fullscreen cloning, and small-window behavior.

## Regression Checklist

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.gsyvideoplayer/.MainActivity
```

Manual checks:

- Enter `Keep last frame`.
- Enable keep-last-frame, play to natural completion, and confirm the last frame remains visible without showing the cover.
- Disable keep-last-frame, replay to completion, and confirm the default cover state returns.
- Replay after completion and confirm playback starts again.
- Enter and exit fullscreen during playback, then wait for completion and confirm the flag still works.
- Check logcat for no `FATAL EXCEPTION`, `IllegalStateException`, or Surface-related crash.
