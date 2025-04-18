package com.example.gsyvideoplayer;


import static androidx.media3.common.PlaybackException.*;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.extractor.mp4.Track;

import com.example.gsyvideoplayer.databinding.ActivityDetailPlayerBinding;
import com.google.common.collect.ImmutableList;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class DetailPlayer extends AppCompatActivity {


    private boolean isPlay;
    private boolean isPause;

    private OrientationUtils orientationUtils;

    private ActivityDetailPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailPlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        String url = getUrl();

        //binding.detailPlayer.setUp(url, false, null, "测试视频");
        //binding.detailPlayer.setLooping(true);
        //binding.detailPlayer.setShowPauseCover(false);

        //如果视频帧数太高导致卡画面不同步
        //VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 30);
        //如果视频seek之后从头播放
//        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
//        List<VideoOptionModel> list = new ArrayList<>();
//        list.add(videoOptionModel);
//        GSYVideoManager.instance().setOptionModelList(list);

        //GSYVideoManager.instance().setTimeOut(4000, true);


        /***************rtsp 配置****************/
        /*VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
        VideoOptionModel videoOptionModel3 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        VideoOptionModel videoOptionModel4 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        VideoOptionModel videoOptionMode04 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);//是否开启缓冲
        VideoOptionModel videoOptionMode14 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);//是否限制输入缓存数
        VideoOptionModel videoOptionMode15 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
        VideoOptionModel videoOptionMode17 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzedmaxduration", 100);//分析码流时长:默认1024*1000
        list.add(videoOptionModel2);
        list.add(videoOptionModel3);
        list.add(videoOptionModel4);
        list.add(videoOptionMode04);
        list.add(videoOptionMode14);
        list.add(videoOptionMode15);
        list.add(videoOptionMode17);
        GSYVideoManager.instance().setOptionModelList(list);*/
        /***************rtsp 配置****************/


        VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);

        /// ijk rtmp
       /*VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,rtmp,rtsp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);*/

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        //binding.detailPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, binding.detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        /**仅仅横屏旋转，不变直*/
        //orientationUtils.setOnlyRotateLand(true);

        //ProxyCacheManager.DEFAULT_MAX_SIZE = 1024 * 1024 * 1024 * 1024;
        //ProxyCacheManager.DEFAULT_MAX_COUNT = 8;

        Map<String, String> header = new HashMap<>();
        header.put("ee", "33");
        header.put("allowCrossProtocolRedirects", "true");
        header.put("User-Agent", "GSY");
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setThumbImageView(imageView).setIsTouchWiget(true).setRotateViewAuto(false)
            //仅仅横屏旋转，不变直
            //.setOnlyRotateLand(true)
            .setRotateWithSystem(false).setLockLand(true).setAutoFullWithSize(true).setShowFullAnimation(false).setNeedLockFull(true).setSeekOnStart(3000).setUrl(url).setMapHeadData(header).setCacheWithPlay(false).setSurfaceErrorPlay(false).setVideoTitle("测试视频").setVideoAllCallBack(new GSYSampleCallBack() {
                @Override
                public void onPrepared(String url, Object... objects) {
                    Debuger.printfError("***** onPrepared **** " + objects[0]);
                    Debuger.printfError("***** onPrepared **** " + objects[1]);
                    super.onPrepared(url, objects);
                    //开始播放了才能旋转和全屏
                    orientationUtils.setEnable(binding.detailPlayer.isRotateWithSystem());
                    isPlay = true;


                    //设置 seek 的临近帧。
                    if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                        ((Exo2PlayerManager) binding.detailPlayer.getGSYVideoManager().getPlayer()).setSeekParameter(SeekParameters.NEXT_SYNC);
                        Debuger.printfError("***** setSeekParameter **** ");
                    }


                    if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                        IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = player.getTrackSelector().getCurrentMappedTrackInfo();
                        boolean hadVideo = false;
                        if (mappedTrackInfo != null) {
                            for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                                TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                                if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        for (int k = 0; k < trackGroup.length; k++) {
                                            Debuger.printfError("####### Audio " + trackGroup.getFormat(k).toString() + " #######");
                                        }
                                    }
                                } else if (C.TRACK_TYPE_VIDEO == mappedTrackInfo.getRendererType(i)) {
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        for (int k = 0; k < trackGroup.length; k++) {
                                            Debuger.printfError("####### Video " + trackGroup.getFormat(k).toString() + " #######");
                                        }
                                    }
                                    hadVideo = true;
                                } else {
                                    for (int j = 0; j < rendererTrackGroups.length; j++) {
                                        TrackGroup trackGroup = rendererTrackGroups.get(j);
                                        Debuger.printfError("####### Other " + trackGroup.getFormat(0).toString() + " #######");
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onEnterFullscreen(String url, Object... objects) {
                    super.onEnterFullscreen(url, objects);
                    Debuger.printfError("***** onEnterFullscreen **** " + objects[0]);//title
                    Debuger.printfError("***** onEnterFullscreen **** " + objects[1]);//当前全屏player
                }

                @Override
                public void onAutoComplete(String url, Object... objects) {
                    super.onAutoComplete(url, objects);
//                    IPlayerManager playerManager = binding.detailPlayer.getGSYVideoManager().getPlayer();
//                    if (playerManager instanceof SystemPlayerManager) {
//                        playerManager.release();
//                    }
                }

                @Override
                public void onComplete(String url, Object... objects) {
                    super.onComplete(url, objects);
//                    IPlayerManager playerManager = binding.detailPlayer.getGSYVideoManager().getPlayer();
//                    if (playerManager instanceof SystemPlayerManager) {
//                        playerManager.release();
//                    }
                }

                @Override
                public void onClickStartError(String url, Object... objects) {
                    super.onClickStartError(url, objects);
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    super.onQuitFullscreen(url, objects);
                    Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                    Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//当前非全屏player

                    // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                    // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                    if (orientationUtils != null) {
                        orientationUtils.backToProtVideo();
                    }
                }

                @Override
                public void onPlayError(String url, Object... objects) {
                    super.onPlayError(url, objects);
                    if (objects[2] != null && binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                        Debuger.printfError("#######################");
                        int code = ((int) objects[2]);
                        String errorStatus = "****";
                        switch (code) {
                            case ERROR_CODE_UNSPECIFIED:
                                errorStatus = "ERROR_CODE_UNSPECIFIED";
                                break;
                            case ERROR_CODE_REMOTE_ERROR:
                                errorStatus = "ERROR_CODE_REMOTE_ERROR";
                                break;
                            case ERROR_CODE_BEHIND_LIVE_WINDOW:
                                errorStatus = "ERROR_CODE_BEHIND_LIVE_WINDOW";
                                break;
                            case ERROR_CODE_TIMEOUT:
                                errorStatus = "ERROR_CODE_TIMEOUT";
                                break;
                            case ERROR_CODE_FAILED_RUNTIME_CHECK:
                                errorStatus = "ERROR_CODE_FAILED_RUNTIME_CHECK";
                                break;
                            case ERROR_CODE_IO_UNSPECIFIED:
                                errorStatus = "ERROR_CODE_IO_UNSPECIFIED";
                                break;
                            case ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
                                errorStatus = "ERROR_CODE_IO_NETWORK_CONNECTION_FAILED";
                                break;
                            case ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                                errorStatus = "ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT";
                                break;
                            case ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE:
                                errorStatus = "ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE";
                                break;
                            case ERROR_CODE_IO_BAD_HTTP_STATUS:
                                errorStatus = "ERROR_CODE_IO_BAD_HTTP_STATUS";
                                break;
                            case ERROR_CODE_IO_FILE_NOT_FOUND:
                                errorStatus = "ERROR_CODE_IO_FILE_NOT_FOUND";
                                break;
                            case ERROR_CODE_IO_NO_PERMISSION:
                                errorStatus = "ERROR_CODE_IO_NO_PERMISSION";
                                break;
                            case ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED:
                                errorStatus = "ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED";
                                break;
                            case ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE:
                                errorStatus = "ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE";
                                break;
                            case ERROR_CODE_PARSING_CONTAINER_MALFORMED:
                                errorStatus = "ERROR_CODE_PARSING_CONTAINER_MALFORMED";
                                break;
                            case ERROR_CODE_PARSING_MANIFEST_MALFORMED:
                                errorStatus = "ERROR_CODE_PARSING_MANIFEST_MALFORMED";
                                break;
                            case ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED";
                                break;
                            case ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED";
                                break;
                            case ERROR_CODE_DECODER_INIT_FAILED:
                                errorStatus = "ERROR_CODE_DECODER_INIT_FAILED";
                                break;
                            case ERROR_CODE_DECODER_QUERY_FAILED:
                                errorStatus = "ERROR_CODE_DECODER_QUERY_FAILED";
                                break;
                            case ERROR_CODE_DECODING_FAILED:
                                errorStatus = "ERROR_CODE_DECODING_FAILED";
                                break;
                            case ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES:
                                errorStatus = "ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES";
                                break;
                            case ERROR_CODE_DECODING_FORMAT_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_DECODING_FORMAT_UNSUPPORTED";
                                break;
                            case ERROR_CODE_AUDIO_TRACK_INIT_FAILED:
                                errorStatus = "ERROR_CODE_AUDIO_TRACK_INIT_FAILED";
                                break;
                            case ERROR_CODE_AUDIO_TRACK_WRITE_FAILED:
                                errorStatus = "ERROR_CODE_AUDIO_TRACK_WRITE_FAILED";
                                break;
                            case ERROR_CODE_DRM_UNSPECIFIED:
                                errorStatus = "ERROR_CODE_DRM_UNSPECIFIED";
                                break;
                            case ERROR_CODE_DRM_SCHEME_UNSUPPORTED:
                                errorStatus = "ERROR_CODE_DRM_SCHEME_UNSUPPORTED";
                                break;
                            case ERROR_CODE_DRM_PROVISIONING_FAILED:
                                errorStatus = "ERROR_CODE_DRM_PROVISIONING_FAILED";
                                break;
                            case ERROR_CODE_DRM_CONTENT_ERROR:
                                errorStatus = "ERROR_CODE_DRM_CONTENT_ERROR";
                                break;
                            case ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED:
                                errorStatus = "ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED";
                                break;
                            case ERROR_CODE_DRM_DISALLOWED_OPERATION:
                                errorStatus = "ERROR_CODE_DRM_DISALLOWED_OPERATION";
                                break;
                            case ERROR_CODE_DRM_SYSTEM_ERROR:
                                errorStatus = "ERROR_CODE_DRM_SYSTEM_ERROR";
                                break;
                            case ERROR_CODE_DRM_DEVICE_REVOKED:
                                errorStatus = "ERROR_CODE_DRM_DEVICE_REVOKED";
                                break;
                            case ERROR_CODE_DRM_LICENSE_EXPIRED:
                                errorStatus = "ERROR_CODE_DRM_LICENSE_EXPIRED";
                                break;
                            case CUSTOM_ERROR_CODE_BASE:
                                errorStatus = "CUSTOM_ERROR_CODE_BASE";
                                break;
                        }
                        Debuger.printfError(errorStatus);
                        Debuger.printfError("#######################");
                    }
                }
            }).setLockClickListener(new LockClickListener() {
                @Override
                public void onClick(View view, boolean lock) {
                    if (orientationUtils != null) {
                        //配合下方的onConfigurationChanged
                        orientationUtils.setEnable(!lock);
                    }
                }
            }).setGSYVideoProgressListener(new GSYVideoProgressListener() {
                @Override
                public void onProgress(long progress, long secProgress, long currentPosition, long duration) {
                    Debuger.printfLog(" progress " + progress + " secProgress " + secProgress + " currentPosition " + currentPosition + " duration " + duration);
                }
            }).build(binding.detailPlayer);

        binding.detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                binding.detailPlayer.startWindowFullscreen(DetailPlayer.this, true, true);
            }
        });


        binding.openBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                fileSearch();
            }
        });

        binding.pip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DetailPlayer.this.enterPictureInPictureMode();
                }
            }
        });

        ///exo 切换音轨
        binding.change.setOnClickListener(new View.OnClickListener() {
            int index = 0;

            @Override
            public void onClick(View view) {
                if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                    IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                    TrackSelector trackSelector = player.getTrackSelector();
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = player.getTrackSelector().getCurrentMappedTrackInfo();

                    if (mappedTrackInfo != null) {
                        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                            if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                                if (index == 0) {
                                    index = 1;
                                } else {
                                    index = 0;
                                }
                                if (rendererTrackGroups.length <= 1) {
                                    return;
                                }
                                TrackGroup trackGroup = rendererTrackGroups.get(index);
                                TrackSelectionParameters parameters = trackSelector.getParameters().buildUpon().setForceHighestSupportedBitrate(true).setOverrideForType(new TrackSelectionOverride(trackGroup, 0)).build();
                                trackSelector.setParameters(parameters);
                            }
                        }
                    }
                } else {
                    Toast.makeText(DetailPlayer.this, "当前不是 Exo 内核或者未播放", Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.showTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                    if (binding.detailPlayer.isInPlayingState()) {
                        IjkExo2MediaPlayer player = ((IjkExo2MediaPlayer) binding.detailPlayer.getGSYVideoManager().getPlayer().getMediaPlayer());
                        List<String> list = new ArrayList<>();
                        Tracks track = player.getCurrentTracks();
                        for (int i = 0; i < track.getGroups().size(); i++) {
                            Tracks.Group group = track.getGroups().get(i);
                            if (C.TRACK_TYPE_AUDIO == group.getType() || C.TRACK_TYPE_VIDEO == group.getType()) {
                                for (int j = 0; j < group.getMediaTrackGroup().length; j++) {
                                    list.add("- " + group.getMediaTrackGroup().getFormat(j) + "\n");
                                }
                            }
                            showOption(list.toArray(new String[0]));
                        }
                    }
                } else {
                    Toast.makeText(DetailPlayer.this, "当前不是 Exo 内核或者未播放", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {

        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }

        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) {
            // Continue playback
        } else {
            getCurPlay().onVideoPause();
            isPause = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        getCurPlay().onVideoResume(false);
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getCurPlay().release();
        }
        //GSYPreViewManager.instance().releaseMediaPlayer();
        if (orientationUtils != null) orientationUtils.releaseListener();
    }


    /**
     * orientationUtils 和  binding.detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            binding.detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }

    private void showOption(final String[] list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("显示可切换轨道");
        builder.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void resolveNormalVideoUI() {
        //增加title
        binding.detailPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    private GSYVideoPlayer getCurPlay() {
        if (binding.detailPlayer.getFullWindowPlayer() != null) {
            return binding.detailPlayer.getFullWindowPlayer();
        }
        return binding.detailPlayer;
    }


    private String getUrl() {

        //String url = "android.resource://" + getPackageName() + "/" + R.raw.test;
        //注意，用ijk模式播放raw视频，这个必须打开
        GSYVideoManager.instance().enableRawPlay(getApplicationContext());

        ///exo 播放 raw
        //String url = "rawresource://" + getPackageName() + "/" + R.raw.test;

        ///exo raw 支持


        ///exo raw 支持
        ///String url =  "assets:///test1.mp4";


        //断网自动重新链接，url前接上ijkhttphook:
        //String url = "ijkhttphook:https://res.exexm.com/cw_145225549855002";

        //String url = "https://cos.icxl.xyz/c03328206d894477a3f8c9767a4de5649342908.mov";
        //String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //String url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"; // new mp4
        //String url = "https://video.xintujing.cn/d0f2f304vodtranscq1251091601/e27f66955285890796832323682/v.f230.m3u8";
        //String url = "https://teamcircle-test.s3.amazonaws.com/public/cbbe6181a0414339b8c20527741d3dd6.mp4";
        //String url = "http://las-tech.org.cn/kwai/las-test_ld500d.flv";//flv 直播
        //String url = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
        //String url = "http://hjq-1257036536.cos.ap-shanghai.myqcloud.com/m3u8/m1/oxcdes
        //String url =  "http://ipsimg-huabei2.speiyou.cn/010/video/other/20180427/40288b156241ec6301624243bdf7021e/40288b156290270d0162a3e7eb2e0726/1524814477/movie.mp4";
        //String url =  "http://ipsimg-huabei2.speiyou.cn/010/video/other/20180424/40288b156290270d0162a3db8cdd033e/40288b156290270d0162a3e8207f074f/e787a64c-f2d0-48fe-896d-246af05f111a.mp4";

        //String url =  "http://video.7k.cn/app_video/20171202/6c8cf3ea/v.m3u8.mp4";
        //String url =  "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8";
        //String url = "rtmp://ctc-zhenjiang04.rt1.gensee.com/5324e855b28b453db7b0ec226598b76c_171391_0_8801038305_1591077225_205d01b8/video";
        //String url = "http://video1.dgtle.com/backend%2F2020%2F3%2F0%2F%E6%88%91%E6%B2%A1%E6%9C%89%E7%BB%99%E4%B8%80%E5%8A%A08Pro%E5%81%9A%E8%AF%84%E6%B5%8B_%E5%8D%B4%E5%B8%A6%E7%9D%80%E5%AE%83%E6%BC%82%E6%B5%81.mp4_1080.mp4";
        //String url = "http://yongtaizx.xyz/20191230/t2Axgh3k/index.m3u8";
        //String url = "http://123.56.109.212:8035/users/bfe52074fba74247853caa764b522731/films/orig/aa4c3451-0468-452a-a189-bd064a1963e5-鹿鼎记下.mp4";
        //String url = "http://static.hnyequ.cn/yequ_iOS/4940735da1227890e6a261937223e0d2_828x1472.mp4"; // 竖
        //String url = "http://39.104.119.42/elevator-1.0/api/downFile?path=demo.ogv";
        //String url = "http://pointshow.oss-cn-hangzhou.aliyuncs.com/transcode/ORIGINAL/Mnbc61586842828593.mp4";// 竖
        //ssl error
        //String url =  "http://qlqfj2ujf.hn-bkt.clouddn.com/aijianji-fuwupeixunshipin_index.m3u8";
        //String url =  "http://122.228.250.223/al.flv.huya.com/src/1394565191-1394565191-5989611887484993536-2789253838-10057-A-0-1-imgplus.flv?ali_dispatch_cold_stream=on&ali_redirect_ex_hot=0";
        //String url =  "http://1258557277.vod2.myqcloud.com/204551f3vodcq1258557277/8cc724f05285890813366287037/playlist_eof.m3u8";
        //String url =  "http://video.85tstss.com/record/live-nianhui-all_x264.mp4 ";
        //String url =  "https://ops-aiops.oss-cn-hongkong.aliyuncs.com/vod/6103_42349_nvrendesuipian2020H265_play.ts";
        //String url =  "https://us-4.wl-cdn.com/hls/20200225/fde4f8ef394731f38d68fe6d601cfd56/index.m3u8";
        //String url =  "https://cdn61.ytbbs.tv/cn/tv/55550/55550-1/play.m3u8?md5=v4sI4lWlo4XojzeAjgBGaQ&expires=1521204012&token=55550";
        //String url =  "http://1253492636.vod2.myqcloud.com/2e5fc148vodgzp1253492636/d08af82d4564972819086152830/plHZZoSkje0A.mp4";

        //String url = "rtsp://ajj:12345678@218.21.217.122:65523/h264/ch40/sub/av_stream";
        //String url = "rtsp://ajj:ajj12345678@218.21.217.122:65522/h264/ch15/sub/av_stream";
        //String url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
        //String url = "http://s.swao.cn/o_1c4gm8o1nniu1had13bk1t0l1rq64m.mov";
        //String url = "http://api.ciguang.tv/avideo/play?num=02-041-0491&type=flv&v=1&client=android";
        //String url = "http://video.7k.cn/app_video/20171213/276d8195/v.m3u8.mp4";
        //String url = "http://103.233.191.21/riak/riak-bucket/6469ac502e813a4c1df7c99f364e70c1.mp4";
        //String url = "http://7xjmzj.com1.z0.glb.cl ouddn.com/20171026175005_JObCxCE2.mp4";
        //String url = "https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8";
        //String url = "http://cdn.tiaobatiaoba.com/Upload/square/2017-11-02/1509585140_1279.mp4";

        //String url = "http://hcjs2ra2rytd8v8np1q.exp.bcevod.com/mda-hegtjx8n5e8jt9zv/mda-hegtjx8n5e8jt9zv.m3u8";
        //String url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
        //String url = "http://ocgk7i2aj.bkt.clouddn.com/17651ac2-693c-47e9-b2d2-b731571bad37";
        //String url = "http://111.198.24.133:83/yyy_login_server/pic/YB059284/97778276040859/1.mp4";
        //String url = "http://vr.tudou.com/v2proxy/v?sid=95001&id=496378919&st=3&pw=";
        //String url = "http://pl-ali.youku.com/playlist/m3u8?type=mp4&ts=1490185963&keyframe=0&vid=XMjYxOTQ1Mzg2MA==&ep=ciadGkiFU8cF4SvajD8bYyuwJiYHXJZ3rHbN%2FrYDAcZuH%2BrC6DPcqJ21TPs%3D&sid=04901859548541247bba8&token=0524&ctype=12&ev=1&oip=976319194";
        String url = "https://flipfit-cdn.akamaized.net/flip_hls/6656423247ffe600199e8363-15125d/video_h1.m3u8";
        //String url = "https://res.exexm.com/cw_145225549855002";
        //String url = "http://storage.gzstv.net/uploads/media/huangmeiyan/jr05-09.mp4";//mepg
        //String url = "https://zh-files.oss-cn-qingdao.aliyuncs.com/20170808223928mJ1P3n57.mp4";//90度
        //String url = "https://media.w3.org/2010/05/sintel/trailer.mp4";//90度
        //String url = " String source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //String url = "http://video.cdn.aizys.com/zzx3.9g.mkv";//long
        //String url = "rtsp://admin:wh123456@112.44.163.248:554/h264/ch01/main/av_stream";//long
        //String url = "https://aliyuncdnsaascloud.xjhktv.com/video/A%20Lin%2B%E5%80%AA%E5%AD%90%E5%86%88-%E4%B8%8D%E5%B1%91%E5%AE%8C%E7%BE%8E%5B%E5%9B%BD%5D%5B1080P%5D.mp4";//track
        //String url = "rtmp://pull.sportslive.top/ECOTIME/2022?auth_key=1672823664-0-0-3c01d7b2ba9772929e792d8a2a5fac82";
        //String url = "http://t.grelighting.cn/m3u8/TVBYNUdCUXN5MDhQSXJYTTJtN3lMUVZtTGJ0dlZXOEk=.m3u8"; //伪装 png\bmp 的m3u8
        //String url = "https://cdn.clicli.cc/static/103701-50881f3281e9a0f6c8b2c96230b922a4.m3u8"; //伪装 png 的m3u8
        return url;
    }

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || resultCode != Activity.RESULT_OK) return;
        if (requestCode == READ_REQUEST_CODE) {
            getPathForSearch(data.getData());
        }
    }


    private void getPathForSearch(Uri uri) {
        String[] selectionArgs = new String[]{DocumentsContract.getDocumentId(uri).split(":")[1]};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + "=?", selectionArgs, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                if (index > -1) {
                    binding.detailPlayer.setUp(uri.toString(), false, "File");
                    binding.detailPlayer.startPlayLogic();
                }
            }
            cursor.close();
        }
    }

    protected void fileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
