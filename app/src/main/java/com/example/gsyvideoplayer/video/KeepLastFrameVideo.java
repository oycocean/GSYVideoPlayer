package com.example.gsyvideoplayer.video;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import moe.codeest.enviews.ENDownloadView;

/**
 * Demo-only player that keeps the current render view on natural playback completion.
 */
public class KeepLastFrameVideo extends StandardGSYVideoPlayer {

    private boolean mKeepLastFrameWhenComplete = true;

    private boolean mLastAutoCompleteRetainedSurface;

    public KeepLastFrameVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public KeepLastFrameVideo(Context context) {
        super(context);
    }

    public KeepLastFrameVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onAutoCompletion() {
        if (!mKeepLastFrameWhenComplete) {
            mLastAutoCompleteRetainedSurface = false;
            super.onAutoCompletion();
            return;
        }

        mLastAutoCompleteRetainedSurface = mTextureViewContainer != null
            && mTextureViewContainer.getChildCount() > 0;

        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);

        mSaveChangeViewTIme = 0;
        mCurrentPosition = 0;

        if (!mIfCurrentIsFullscreen) {
            getGSYVideoManager().setLastListener(null);
        }

        if (mAudioFocusManager != null) {
            mAudioFocusManager.abandonAudioFocus();
        }
        if (mContext instanceof Activity) {
            try {
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        releaseNetWorkState();

        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onAutoComplete keepLastFrame");
            mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
        }
        mHadPlay = false;
    }

    @Override
    public void onCompletion() {
        mLastAutoCompleteRetainedSurface = false;
        super.onCompletion();
    }

    @Override
    protected void startButtonLogic() {
        mLastAutoCompleteRetainedSurface = false;
        super.startButtonLogic();
    }

    @Override
    protected void changeUiToCompleteShow() {
        if (!mKeepLastFrameWhenComplete) {
            super.changeUiToCompleteShow();
            return;
        }

        Debuger.printfLog("changeUiToCompleteShow keepLastFrame");

        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mStartButton, VISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mLockScreen, (mIfCurrentIsFullscreen && mNeedLockFull) ? VISIBLE : GONE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        updateStartImage();
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        if (from instanceof KeepLastFrameVideo && to instanceof KeepLastFrameVideo) {
            KeepLastFrameVideo fromPlayer = (KeepLastFrameVideo) from;
            KeepLastFrameVideo toPlayer = (KeepLastFrameVideo) to;
            toPlayer.mKeepLastFrameWhenComplete = fromPlayer.mKeepLastFrameWhenComplete;
            toPlayer.mLastAutoCompleteRetainedSurface = fromPlayer.mLastAutoCompleteRetainedSurface;
        }
        super.cloneParams(from, to);
        if (from instanceof KeepLastFrameVideo && to instanceof KeepLastFrameVideo) {
            KeepLastFrameVideo fromPlayer = (KeepLastFrameVideo) from;
            KeepLastFrameVideo toPlayer = (KeepLastFrameVideo) to;
            toPlayer.mKeepLastFrameWhenComplete = fromPlayer.mKeepLastFrameWhenComplete;
            toPlayer.mLastAutoCompleteRetainedSurface = fromPlayer.mLastAutoCompleteRetainedSurface;
        }
    }

    public void setKeepLastFrameWhenComplete(boolean keepLastFrameWhenComplete) {
        this.mKeepLastFrameWhenComplete = keepLastFrameWhenComplete;
    }

    public boolean isKeepLastFrameWhenComplete() {
        return mKeepLastFrameWhenComplete;
    }

    public boolean isLastAutoCompleteRetainedSurface() {
        return mLastAutoCompleteRetainedSurface;
    }
}
