# 完成后保留最后一帧 Demo

`KeepLastFrameVideo` 是一个 Demo 级播放器，用来验证“自然播放完成后停留在最后一帧”的交互语义。它不会改变基础播放器的默认完成态，也不会全局影响其他播放器页面。

## 目标

- 播放自然结束时，可以选择保留当前 render view 画面，不立刻显示封面。
- 通过 flag 控制行为，业务可以在同一个页面里对比“保留最后一帧”和“默认封面态”。
- 播放失败、主动释放、退出页面等非自然完成场景仍走原有释放流程。
- 全屏和非全屏切换时同步该 flag。

## Demo 入口

主页面入口：`完成保留最后一帧`

主要类：

- `app/src/main/java/com/example/gsyvideoplayer/KeepLastFrameDemoActivity.java`
- `app/src/main/java/com/example/gsyvideoplayer/video/KeepLastFrameVideo.java`
- `app/src/main/res/layout/activity_keep_last_frame_demo.xml`

## 基本用法

```java
keepLastFrameVideo.setKeepLastFrameWhenComplete(true);
```

关闭后恢复默认完成态：

```java
keepLastFrameVideo.setKeepLastFrameWhenComplete(false);
```

Demo 内部通过 `isLastAutoCompleteRetainedSurface()` 判断本次自然完成时是否确实保留了 render view。

## 实现说明

`KeepLastFrameVideo#onAutoCompletion()` 在开启 flag 时不会调用基础类默认的完成释放逻辑，而是：

- 设置状态为 `CURRENT_STATE_AUTO_COMPLETE`。
- 保留当前 render view。
- 隐藏封面层，显示完成态控制 UI。
- 释放音频焦点、网络监听和屏幕常亮 flag。
- 触发 `onAutoComplete` 回调。

`onCompletion()`、重新播放和页面销毁仍走原释放逻辑，避免资源长期残留。

## 注意事项

- 这是 Demo 级能力，适合先验证业务交互，不建议直接理解为全局播放器默认行为。
- 是否能保留画面取决于当前 render view 是否仍存在，以及业务是否主动释放 Surface。
- 如果业务完成后必须展示封面、广告或推荐位，应保持默认完成态。
- 如果要下沉到基础库，需要同时设计 Surface 生命周期、封面层、重播、全屏复制、小窗等场景。

## 回归建议

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.gsyvideoplayer/.MainActivity
```

真机检查：

- 进入 `完成保留最后一帧`。
- 开启“保留最后一帧”，播放到自然结束，确认停留在最后一帧且不显示封面。
- 关闭“保留最后一帧”，重新播放到结束，确认回到默认封面态。
- 播放结束后点击重播，确认能重新开始。
- 播放中进入全屏、退出全屏，再等播放结束，确认 flag 仍生效。
- 退出页面后扫 logcat，确认没有 `FATAL EXCEPTION`、`IllegalStateException` 或 Surface 相关 crash。
