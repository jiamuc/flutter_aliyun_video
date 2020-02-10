///*
// * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
// */
//
//package com.sm9i.aliyun_video.aliyun.util;
//
//import android.content.Context;
//import android.graphics.Rect;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.aliyun.common.global.AliyunTag;
//import com.aliyun.crop.AliyunCropCreator;
//import com.aliyun.crop.struct.CropParam;
//import com.aliyun.crop.supply.AliyunICrop;
//import com.aliyun.crop.supply.CropCallback;
//import com.aliyun.svideo.base.Constants;
//import com.aliyun.svideo.media.MediaInfo;
//import com.aliyun.svideo.sdk.external.struct.MediaType;
//import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
//import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
//import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
//import com.duanqu.transcode.NativeParser;
//
///**
// * 合拍转码视频专用
// * 转码 满足以下任一条件
// * 1.分辨率大于300P
// */
//
//public class MixVideoTranscoder {
//
//    /**
//     * 大于360P的视频需要进行转码
//     */
//    public final static int WIDTH = 360, HEIGHT = 640;
//
//    private static final String TAG = MixVideoTranscoder.class.getSimpleName();
//    private MediaInfo mMediaInfo;
//    private AliyunICrop mAlivcCrop;
//    private TranscoderListener mTranscoderListener;
//    private boolean isTranscode;
//    private CropParam mCropParam = null;
//
//    private TranscodeTask mTranscodeTask;
//
//    public void addMedia(MediaInfo mediaInfo) {
//        mMediaInfo = mediaInfo;
//    }
//
//    public boolean isTranscode() {
//        return isTranscode;
//    }
//
//    public void init(Context context) {
//        mAlivcCrop = AliyunCropCreator.createCropInstance(context);
//    }
//
//    public void start() {
//        if (mMediaInfo == null) {
//            Log.e(TAG, "not have mediaInfo");
//            return;
//        }
//        mTranscodeTask = new TranscodeTask();
//        mTranscodeTask.execute();
//    }
//
//    private class TranscodeTask extends AsyncTask<Void, Long, CropParam> {
//
//        @Override
//        protected CropParam doInBackground(Void... voids) {
//            if (isCancelled()) {
//                return null;
//            }
//            return loadVideoCropInfo(mMediaInfo);
//        }
//
//        @Override
//        protected void onPostExecute(CropParam cropParam) {
//            super.onPostExecute(cropParam);
//            if (cropParam != null) {
//                mCropParam = cropParam;
//                transcodeVideo(cropParam);
//            }
//        }
//    }
//
//    public void setTranscodeListener(TranscoderListener transcoderListener) {
//        this.mTranscoderListener = transcoderListener;
//    }
//
//    public void cancel() {
//        if (mTranscodeTask != null) {
//            mTranscodeTask.cancel(true);
//        }
//        if (mAlivcCrop != null) {
//            mAlivcCrop.cancel();
//        }
//        if (mTranscoderListener != null) {
//            mTranscoderListener.onCancelComplete();
//        }
//    }
//
//    private void transcodeVideo(CropParam cropParam) {
//
//        mAlivcCrop.setCropParam(cropParam);
//        mAlivcCrop.setCropCallback(mTranscodeCallback);
//        mAlivcCrop.startCrop();
//        Log.i(TAG, "log_editor_media_transcode :" + cropParam.getInputPath());
//    }
//
//    public interface TranscoderListener {
//        void onError(Throwable e, int errorCode);
//
//        void onProgress(int progress);
//
//        void onComplete(MediaInfo resultVideos);
//
//        void onCancelComplete();
//    }
//
//    private CropCallback mTranscodeCallback = new CropCallback() {
//        @Override
//        public void onProgress(int percent) {
//            int progress = percent;
//            Log.e(TAG, "progress..." + progress + "__percent: " + percent);
//            if (mTranscoderListener != null) {
//                mTranscoderListener.onProgress(progress);
//            }
//        }
//
//        @Override
//        public void onError(int code) {
//            if (mTranscoderListener != null) {
//                mTranscoderListener.onError(new Throwable("transcode error, error code = " + code), code);
//            }
//        }
//
//        @Override
//        public void onComplete(long duration) {
//
//            if (mTranscoderListener != null) {
//                replaceOutputPath();
//                isTranscode = true;
//                mTranscoderListener.onComplete(mMediaInfo);
//            }
//        }
//
//
//        @Override
//        public void onCancelComplete() {
//
//        }
//    };
//
//    private CropParam loadVideoCropInfo(MediaInfo info) {
//        int frameWidth = 0;
//        int frameHeight = 0;
//        long duration = 0;
//        try {
//            NativeParser nativeParser = new NativeParser();
//            nativeParser.init(info.filePath);
//            try {
//                frameWidth = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_WIDTH));
//                frameHeight = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_HEIGHT));
//            } catch (Exception e) {
//                Log.e(AliyunTag.TAG, "parse rotation failed");
//            }
//            nativeParser.release();
//            nativeParser.dispose();
//            duration = mAlivcCrop.getVideoDuration(info.filePath);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //目前只对360p以上对视频转码,填充模式
//        if (frameWidth * frameHeight > WIDTH * HEIGHT) {
//            Log.d(TAG, "need transcode...path..." + info.filePath);
//            CropParam cropParam = new CropParam();
//            cropParam.setInputPath(info.filePath);
//            String outputPath = Constants.SDCardConstants.OUTPUT_PATH_DIR + System.currentTimeMillis() + Constants.SDCardConstants.TRANSCODE_SUFFIX;
//            cropParam.setOutputPath(outputPath);
//            int outputWidth = 0;
//            int outputHeight = 0;
//
//            if (frameWidth * frameHeight > WIDTH * HEIGHT) {
//                //尺寸过大裁剪时重置宽高
//                outputHeight = HEIGHT;
//                outputWidth = WIDTH;
//            }
//            cropParam.setOutputHeight(outputHeight);
//            cropParam.setOutputWidth(outputWidth);
//            cropParam.setCropRect(new Rect(0, 0, frameWidth, frameHeight));
//            cropParam.setScaleMode(VideoDisplayMode.FILL);
//            cropParam.setQuality(VideoQuality.SSD);
//            cropParam.setGop(5);
//            cropParam.setFrameRate(30);
//            cropParam.setCrf(23);
//            cropParam.setVideoCodec(VideoCodecs.H264_SOFT_FFMPEG);
//            cropParam.setEndTime(duration);
//            cropParam.setMediaType(MediaType.ANY_VIDEO_TYPE);
//            return cropParam;
//        }
//        return null;
//    }
//
//
//    private void replaceOutputPath() {
//
//        if (mMediaInfo != null && mCropParam != null) {
//            mMediaInfo.filePath = mCropParam.getOutputPath();
//        }
//    }
//
//    public void release() {
//        if (mAlivcCrop != null) {
//            mAlivcCrop.dispose();
//            mAlivcCrop = null;
//        }
//        if (mTranscodeCallback != null) {
//            mTranscodeCallback = null;
//        }
//        if (mTranscoderListener != null) {
//            mTranscoderListener = null;
//        }
//    }
//}