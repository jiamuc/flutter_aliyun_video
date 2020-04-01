package com.sm9i.aliyun_video.aliyun.view.control;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;
import com.sm9i.aliyun_video.R;
import com.sm9i.aliyun_video.aliyun.base.UIConfigManager;
import com.sm9i.aliyun_video.aliyun.base.utils.FastClickUtil;
import com.sm9i.aliyun_video.aliyun.base.widget.FanProgressBar;
import com.sm9i.aliyun_video.aliyun.common.image.ImageLoaderImpl;
import com.sm9i.aliyun_video.aliyun.common.utils.image.ImageLoaderOptions;
import com.sm9i.aliyun_video.aliyun.view.BaseScrollPickerView;
import com.sm9i.aliyun_video.aliyun.view.StringScrollPicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 美颜  gif  滤镜 注释掉
 * <p>
 * 更换录制为 拍照
 */
public class ControlView extends RelativeLayout implements View.OnTouchListener {
    private static final String TAG = ControlView.class.getSimpleName();
    private static final int MAX_ITEM_COUNT = 5;
    //美颜和gif
    private LinearLayout llBeautyFace;
    // private LinearLayout llGifEffect;
    private ImageView ivReadyRecord;
    private ImageView aliyunSwitchLight;
    private ImageView aliyunSwitchCamera;
    //完成
    private TextView aliyunComplete;
    private ImageView aliyunBack;
    private LinearLayout aliyunRecordLayoutBottom;
    private LinearLayout aliyunRateBar;
    private TextView aliyunRateQuarter;
    private TextView aliyunRateHalf;
    private TextView aliyunRateOrigin;
    private TextView aliyunRateDouble;
    private TextView aliyunRateDoublePower2;
    private TextView aliyunRecordDuration;
    private FrameLayout aliyunRecordBtn;
    private FanProgressBar aliyunRecordProgress;
    private TextView aliyunDelete;
    // private LinearLayout mAlivcMusic;
    private TextView mRecordTipTV;
    private FrameLayout mTitleView;
    private StringScrollPicker mPickerView;
    private ControlViewListener mListener;
    // private ImageView mIvMusicIcon;
    // private LinearLayout mLlFilterEffect;
    // private TextView mTvMusic;
    private LinearLayout mAlivcAspectRatio;
    private TextView mTvAspectRatio;
    private ImageView mIVAspectRatio;
    //录制模式
    public static RecordMode recordMode;
    //是否有录制片段，true可以删除，不可选择音乐、拍摄模式view消失
    private boolean hasRecordPiece = false;
    //是否可以完成录制，录制时长大于最小录制时长时为true
    private boolean canComplete = false;
    //音乐选择是否弹出
    private boolean isMusicSelViewShow = false;
    //其他弹窗选择是否弹出
    private boolean isEffectSelViewShow = false;
    //闪光灯类型
    private FlashType flashType = FlashType.OFF;
    //摄像头类型
    private CameraType cameraType = CameraType.FRONT;
    //录制速度
    private RecordRate recordRate = RecordRate.STANDARD;
    //录制状态，开始、暂停、准备,只是针对UI变化
    private RecordState recordState = RecordState.STOP;
    //录制按钮宽度
    private int itemWidth;
    //是否倒计时拍摄中
    private boolean isCountDownRecording = false;
    //是否实际正在录制，由于结束录制时UI立刻变化，但是尚未真正结束录制，所以此时不能继续录制视频否则会崩溃
    private boolean isRecording = false;
    //
    private int mAspectRatio = AliyunSnapVideoParam.RATIO_MODE_9_16;
    //录制类型 true 为合拍
    private Boolean mRecorderType = false;

    public ControlView(Context context) {
        this(context, null);
    }

    public ControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        calculateItemWidth();
        //Inflate布局
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_recorder_view_control, this, true);
        assignViews();
        //设置view的监听事件
        setViewListener();
        //更新view的显示
        updateAllViews();
    }

    private void assignViews() {
        mAlivcAspectRatio = findViewById(R.id.alivc_record_change_aspect_ratio_layout);
        mTvAspectRatio = findViewById(R.id.alivc_record_aspect_ratio_tv_change);
        mIVAspectRatio = findViewById(R.id.alivc_aspect_iv_ratio);
        ivReadyRecord = (ImageView) findViewById(R.id.aliyun_ready_record);
        aliyunSwitchLight = (ImageView) findViewById(R.id.aliyun_switch_light);
        // mLlFilterEffect = findViewById(R.id.alivc_record_effect_filter);
        aliyunSwitchCamera = (ImageView) findViewById(R.id.aliyun_switch_camera);
        // mIvMusicIcon = findViewById(R.id.alivc_record_iv_music);
        aliyunComplete = findViewById(R.id.aliyun_complete);
        aliyunBack = (ImageView) findViewById(R.id.aliyun_back);
        aliyunRecordLayoutBottom = (LinearLayout) findViewById(R.id.aliyun_record_layout_bottom);
        aliyunRateBar = (LinearLayout) findViewById(R.id.aliyun_rate_bar);
        aliyunRateQuarter = (TextView) findViewById(R.id.aliyun_rate_quarter);
        aliyunRateHalf = (TextView) findViewById(R.id.aliyun_rate_half);
        aliyunRateOrigin = (TextView) findViewById(R.id.aliyun_rate_origin);
        aliyunRateDouble = (TextView) findViewById(R.id.aliyun_rate_double);
        aliyunRateDoublePower2 = (TextView) findViewById(R.id.aliyun_rate_double_power2);
        aliyunRecordDuration = (TextView) findViewById(R.id.aliyun_record_duration);
        aliyunRecordBtn = (FrameLayout) findViewById(R.id.aliyun_record_bg);
        aliyunRecordProgress = (FanProgressBar) findViewById(R.id.aliyun_record_progress);
        aliyunDelete = (TextView) findViewById(R.id.aliyun_delete);
        llBeautyFace = findViewById(R.id.ll_beauty_face);
        // llGifEffect = findViewById(R.id.ll_gif_effect);
        mPickerView = findViewById(R.id.alivc_video_picker_view);
        mTitleView = findViewById(R.id.alivc_record_title_view);
        mRecordTipTV = findViewById(R.id.alivc_record_tip_tv);
//        mAlivcMusic = findViewById(R.id.alivc_music);
        // mTvMusic = findViewById(R.id.tv_music);
        //uiStyleConfig
        //音乐按钮
        //倒计时
        //完成的按钮图片 - 不可用
        //底部滤镜，美颜，美肌对应的按钮图片
        //底部动图mv对应的按钮图片
        UIConfigManager.setImageResourceConfig(
                new ImageView[]{ivReadyRecord, findViewById(R.id.iv_beauty_face), findViewById(R.id.iv_gif_effect)}
                , new int[]{R.attr.countdownImage, R.attr.faceImage, R.attr.magicImage}
                , new int[]{R.mipmap.alivc_svideo_icon_magic, R.mipmap.alivc_svideo_icon_beauty_face, R.mipmap.alivc_svideo_icon_gif_effect}
        );

        //回删对应的图片
        //拍摄中红点对应的图片
        UIConfigManager.setImageResourceConfig(
                new TextView[]{aliyunDelete, aliyunRecordDuration}
                , new int[]{0, 0}
                , new int[]{R.attr.deleteImage, R.attr.dotImage}
                , new int[]{R.mipmap.alivc_svideo_icon_delete, R.mipmap.alivc_svideo_record_time_tip});
        //切换摄像头的图片
        aliyunSwitchCamera.setImageDrawable(getSwitchCameraDrawable());
        List<String> strings = new ArrayList<>(2);

        strings.add(getResources().getString(R.string.tab_photo));

        strings.add(getResources().getString(R.string.tab_video));
        mPickerView.setData(strings);

        //向上的三角形对应的图片
        mPickerView.setCenterItemBackground(UIConfigManager.getDrawableResources(getContext(), R.attr.triangleImage, R.mipmap.alivc_svideo_icon_selected_indicator));
    }

    /**
     * 获取切换摄像头的图片的selector
     *
     * @return Drawable
     */
    private Drawable getSwitchCameraDrawable() {

        Drawable drawable = UIConfigManager.getDrawableResources(getContext(), R.attr.switchCameraImage, R.mipmap.alivc_svideo_icon_magic_turn);
        Drawable pressDrawable = drawable.getConstantState().newDrawable().mutate();
        pressDrawable.setAlpha(66);//透明度60%
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},
                pressDrawable);
        stateListDrawable.addState(new int[]{},
                drawable);
        return stateListDrawable;
    }


    /**
     * 设置view的隐藏和显示
     * 和当前状态有关
     *
     * @param isShow 是否隐藏
     */
    private void setViewVisibility(boolean isShow) {
        aliyunRateBar.setVisibility(isShow ? VISIBLE : GONE);
        aliyunComplete.setVisibility(isShow ? VISIBLE : GONE);
        ivReadyRecord.setVisibility(isShow ? VISIBLE : GONE);
    }

    /**
     * 给各个view设置监听
     */
    private void setViewListener() {
        mPickerView.setOnSelectedListener(new BaseScrollPickerView.OnSelectedListener() {
            @Override
            public void onSelected(BaseScrollPickerView baseScrollPickerView, int position) {
                Log.i(TAG, "onSelected:" + position);
                if (position == 0) {
                    recordMode = RecordMode.PHOTO;
                    if (mListener != null) {
                        mListener.changeType(false);
                    }

                    setViewVisibility(false);
                } else {
                    recordMode = RecordMode.VIDEO;
                    if (mListener != null) {
                        mListener.changeType(true);
                    }
                    setViewVisibility(true);
                }
                updateRecordBtnView();
            }
        });

        // 返回按钮
        aliyunBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if (mListener != null) {
                    mListener.onBackClick();
                }
            }
        });

        // 准备录制
        ivReadyRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if (isRecording) {
                    return;
                }
                if (recordState == RecordState.STOP) {
                    recordState = RecordState.READY;
                    updateAllViews();
                    if (mListener != null) {
                        mListener.onReadyRecordClick(false);
                    }
                } else {
                    recordState = RecordState.STOP;
                    if (mListener != null) {
                        mListener.onReadyRecordClick(true);
                    }
                }

            }
        });

        // 闪光灯
        aliyunSwitchLight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }

                if (flashType == FlashType.ON) {
                    flashType = FlashType.OFF;
                } else {
                    flashType = FlashType.ON;
                }

                updateLightSwitchView();
                if (mListener != null) {
                    mListener.onLightSwitch(flashType);
                }
            }
        });

        // 切换相机
        aliyunSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if (mListener != null) {
                    mListener.onCameraSwitch();
                }
            }
        });

        // 下一步(跳转编辑)
        aliyunComplete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if (mListener != null) {
                    mListener.onNextClick();
                }
            }
        });
        aliyunRateQuarter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                recordRate = RecordRate.VERY_FLOW;
                if (mListener != null) {
                    mListener.onRateSelect(recordRate.getRate());
                }
                updateRateItemView();
            }
        });
        aliyunRateHalf.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                recordRate = RecordRate.FLOW;
                if (mListener != null) {
                    mListener.onRateSelect(recordRate.getRate());
                }
                updateRateItemView();
            }
        });
        aliyunRateOrigin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                recordRate = RecordRate.STANDARD;
                if (mListener != null) {
                    mListener.onRateSelect(recordRate.getRate());
                }
                updateRateItemView();
            }
        });
        aliyunRateDouble.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                recordRate = RecordRate.FAST;
                if (mListener != null) {
                    mListener.onRateSelect(recordRate.getRate());
                }
                updateRateItemView();
            }
        });
        aliyunRateDoublePower2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                recordRate = RecordRate.VERY_FAST;
                if (mListener != null) {
                    mListener.onRateSelect(recordRate.getRate());
                }
                updateRateItemView();
            }
        });
        // 点击美颜
        llBeautyFace.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    if (FastClickUtil.isFastClick()) {
                        return;
                    }
                    mListener.onBeautyFaceClick();
                }
            }
        });
        // 点击音乐
//        mAlivcMusic.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (FastClickUtil.isFastClick()) {
//                    return;
//                }
//                if (mListener != null) {
//                    mListener.onMusicClick();
//                }
//            }
//        });
        // 点击回删
        aliyunDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeleteClick();
                }
            }
        });
        // 点击动图
//        llGifEffect.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (FastClickUtil.isFastClick()) {
//                    return;
//                }
//                if (mListener != null) {
//                    mListener.onGifEffectClick();
//                }
//            }
//        });
//        mLlFilterEffect.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (FastClickUtil.isFastClick()) {
//                    return;
//                }
//                if (mListener != null) {
//                    mListener.onFilterEffectClick();
//                }
//            }
//        });
        mAlivcAspectRatio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                changeAspectRatio();

                if (mListener != null) {
                    mListener.onChangeAspectRatioClick(mAspectRatio);
                }

            }
        });
        //长按拍需求是按下就拍抬手停止拍
        aliyunRecordBtn.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (FastClickUtil.isRecordWithOtherClick()) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (recordState != RecordState.COUNT_DOWN_RECORDING) {
                if (recordMode == RecordMode.PHOTO) {
                    mListener.takePhoto(true);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP) {
            if (recordState == RecordState.COUNT_DOWN_RECORDING) {
                if (mListener != null) {
                    mListener.onStopRecordClick();
                    setRecordState(RecordState.STOP);
                    //停止拍摄后立即展示回删
                    if (hasRecordPiece) {
                        setHasRecordPiece(true);
                    }

                }
            } else {
                if (recordMode == RecordMode.PHOTO) {

                } else {
                    if (recordState == RecordState.RECORDING) {
                        if (mListener != null) {
                            mListener.onStopRecordClick();
                            setRecordState(RecordState.STOP);
                            //停止拍摄后立即展示回删
                            if (hasRecordPiece) {
                                setHasRecordPiece(true);
                            }
                        }
                    } else {
                        if (mListener != null && !isRecording) {
                            mListener.onStartRecordClick();
                        }
                    }
                    updateRateItemView();
                }
            }
//
//            if (recordState == RecordState.COUNT_DOWN_RECORDING) {
//                if (mListener != null) {
//                    mListener.onStopRecordClick();
//                    setRecordState(RecordState.STOP);
//                    //停止拍摄后立即展示回删
//                    if (hasRecordPiece) {
//                        setHasRecordPiece(true);
//                    }
//
//                }
//            } else {
//                if (recordMode == RecordMode.VIDEO) {
//                    if (mListener != null && recordState == RecordState.RECORDING) {
//                        mListener.onStopRecordClick();
//                        setRecordState(RecordState.STOP);
//                        //停止拍摄后立即展示回删
//                        if (hasRecordPiece) {
//                            setHasRecordPiece(true);
//                        }
//                    }
//                } else {
//                    ///拍照
////                    if (recordState == RecordState.RECORDING) {
////                        if (mListener != null) {
////                            mListener.onStopRecordClick();
////                            setRecordState(RecordState.STOP);
////                            //停止拍摄后立即展示回删
////                            if (hasRecordPiece) {
////                                setHasRecordPiece(true);
////                            }
////                        }
////                    } else {
////                        if (mListener != null && !isRecording) {
////                            mListener.onStartRecordClick();
////                        }
////                    }
//                }
//            }

        }
        return true;
    }

    /**
     * 改变录制按钮大小
     *
     * @param scaleRate
     */
    private void recordBtnScale(float scaleRate) {
        LayoutParams recordBgLp = (LayoutParams) aliyunRecordBtn.getLayoutParams();
        recordBgLp.width = (int) (itemWidth * scaleRate);
        recordBgLp.height = (int) (itemWidth * scaleRate);
        aliyunRecordBtn.setLayoutParams(recordBgLp);
    }

    /**
     * 获取录制按钮宽高
     */
    private void calculateItemWidth() {
        itemWidth = getResources().getDisplayMetrics().widthPixels / MAX_ITEM_COUNT;
    }

    /**
     * 更新所有视图
     */
    private void updateAllViews() {
        //准备录制和音乐选择的时候所有view隐藏
        if (isMusicSelViewShow || recordState == RecordState.READY) {
            setVisibility(GONE);

        } else {
            setVisibility(VISIBLE);
            updateBottomView();
            updateTittleView();
//            updateRateItemView();

        }
        setViewVisibility(recordMode == RecordMode.VIDEO);


    }

    /**
     * 更新顶部视图
     */
    private void updateTittleView() {
        if (recordState == RecordState.STOP) {
            mTitleView.setVisibility(VISIBLE);
            // mAlivcMusic.setVisibility(VISIBLE);
            // mLlFilterEffect.setVisibility(VISIBLE);
            mAlivcAspectRatio.setVisibility(VISIBLE);
            updateLightSwitchView();
            updateMusicSelView();
            updateCompleteView();
        } else {
            mTitleView.setVisibility(GONE);
            //   mAlivcMusic.setVisibility(GONE);
            //  mLlFilterEffect.setVisibility(GONE);
            mAlivcAspectRatio.setVisibility(GONE);

        }
        if (mRecorderType) {
            mAlivcAspectRatio.setVisibility(GONE);
            mTitleView.setVisibility(VISIBLE);
//            mAlivcMusic.setVisibility(GONE);
        }
    }

    /**
     * 更新完成录制按钮
     */
    private void updateCompleteView() {
        if (canComplete) {
            aliyunComplete.setSelected(true);
            aliyunComplete.setEnabled(true);
            //完成的按钮图片 - 可用
            //UIConfigManager.setImageResourceConfig(aliyunComplete, R.attr.finishImageAble, R.mipmap.alivc_svideo_icon_next_complete);
        } else {
            aliyunComplete.setSelected(false);
            aliyunComplete.setEnabled(false);
            //完成的按钮图片 - 不可用
            //UIConfigManager.setImageResourceConfig(aliyunComplete, R.attr.finishImageUnable, R.mipmap.alivc_svideo_icon_next_not_ready);
        }
    }

    /**
     * 倒计时按钮是否可以点击
     *
     * @param isClickable true: 可点击, 按钮白色, false: 不可点击, 按钮置灰
     */
    public void updataCutDownView(boolean isClickable) {
        if (isClickable) {
            ivReadyRecord.setColorFilter(null);
            ivReadyRecord.setClickable(true);
        } else {
            ivReadyRecord.setColorFilter(ContextCompat.getColor(getContext(), R.color.alivc_record_color_filter));
            ivReadyRecord.setClickable(false);
        }
        UIConfigManager.setImageResourceConfig(ivReadyRecord, R.attr.countdownImage, R.mipmap.alivc_svideo_icon_magic);
    }

    /**
     * 更新音乐弹窗按钮
     */
    private void updateMusicSelView() {
        Drawable ratioDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.alivc_svideo_icon_aspect_ratio);
        if (hasRecordPiece) {
            //已经开始录制不允许更改音乐
            // mAlivcMusic.setClickable(false);
            // mIvMusicIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.alivc_record_color_filter), PorterDuff.Mode.MULTIPLY);
            //  mTvMusic.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_record_color_filter));
            //已经开始录制不允许更改画幅
            mIVAspectRatio.setColorFilter(ContextCompat.getColor(getContext(), R.color.alivc_record_color_filter), PorterDuff.Mode.MULTIPLY);
            mAlivcAspectRatio.setClickable(false);
            mIVAspectRatio.setImageDrawable(ratioDrawable);
            mTvAspectRatio.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_record_color_filter));
        } else {
            // mIvMusicIcon.clearColorFilter();
            mIVAspectRatio.clearColorFilter();
            mIVAspectRatio.setImageDrawable(ratioDrawable);
            //   mTvMusic.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_common_font_white));
            // mAlivcMusic.setClickable(true);
            mAlivcAspectRatio.setClickable(true);
            mTvAspectRatio.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_common_font_white));

        }
    }

    /**
     * 更新底部控制按钮
     */
    private void updateBottomView() {
        if (isEffectSelViewShow) {
            aliyunRecordLayoutBottom.setVisibility(GONE);
        } else {
            aliyunRecordLayoutBottom.setVisibility(VISIBLE);
            updateModeSelView();
            updateRateItemView();
            updateRecordBtnView();
            updateDeleteView();
            if (recordState == RecordState.STOP) {
                //其他按钮现实
                llBeautyFace.setVisibility(VISIBLE);
                //  llGifEffect.setVisibility(VISIBLE);
            } else {
                //llGifEffect.setVisibility(INVISIBLE);
                llBeautyFace.setVisibility(INVISIBLE);
            }
        }

    }

    /**
     * 更新速录选择按钮
     */
    private void updateRateItemView() {
        if (recordState == RecordState.RECORDING || recordState == RecordState.COUNT_DOWN_RECORDING
                || recordState == RecordState.READY) {
            aliyunRateBar.setVisibility(INVISIBLE);
        } else {
            aliyunRateBar.setVisibility(VISIBLE);
            aliyunRateQuarter.setSelected(false);
            aliyunRateHalf.setSelected(false);
            aliyunRateOrigin.setSelected(false);
            aliyunRateDouble.setSelected(false);
            aliyunRateDoublePower2.setSelected(false);
            switch (recordRate) {
                case VERY_FLOW:
                    aliyunRateQuarter.setSelected(true);
                    break;
                case FLOW:
                    aliyunRateHalf.setSelected(true);
                    break;
                case FAST:
                    aliyunRateDouble.setSelected(true);
                    break;
                case VERY_FAST:
                    aliyunRateDoublePower2.setSelected(true);
                    break;
                default:
                    aliyunRateOrigin.setSelected(true);
            }
        }

    }


    private void changeAspectRatio() {
        switch (mAspectRatio) {
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                mAspectRatio = AliyunSnapVideoParam.RATIO_MODE_3_4;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                mAspectRatio = AliyunSnapVideoParam.RATIO_MODE_1_1;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                mAspectRatio = AliyunSnapVideoParam.RATIO_MODE_9_16;
                break;
            default:
                mAspectRatio = AliyunSnapVideoParam.RATIO_MODE_9_16;
                break;
        }
    }

    /**
     * 更新拍摄模式选择view
     */
    private void updateModeSelView() {
        if (hasRecordPiece || recordState == RecordState.RECORDING || recordState == RecordState.COUNT_DOWN_RECORDING) {
            mPickerView.setVisibility(GONE);
        } else {
            mPickerView.setVisibility(VISIBLE);
            if (recordMode == RecordMode.PHOTO) {
                mPickerView.setSelectedPosition(0);
            } else {
                mPickerView.setSelectedPosition(1);
            }
        }
    }

    /**
     * 更新删除按钮
     */
    private void updateDeleteView() {

        if (!hasRecordPiece || recordState == RecordState.RECORDING
                || recordState == RecordState.COUNT_DOWN_RECORDING) {
            aliyunDelete.setVisibility(GONE);
        } else {
            aliyunDelete.setVisibility(VISIBLE);
        }
    }

    /**
     * 更新录制按钮状态
     */
    private void updateRecordBtnView() {
        if (recordState == RecordState.STOP) {
            recordBtnScale(1f);
            //拍摄按钮图片 - 未开始拍摄
            UIConfigManager.setImageBackgroundConfig(aliyunRecordBtn, R.attr.videoShootImageNormal, R.mipmap.alivc_svideo_bg_record_storp);
            aliyunRecordDuration.setVisibility(GONE);
            mRecordTipTV.setVisibility(VISIBLE);
            if (recordMode == RecordMode.PHOTO) {
//                mRecordTipTV.setText(R.string.alivc_recorder_control_press);
                mRecordTipTV.setText(R.string.alivc_recorder_control_click);
            } else {
                mRecordTipTV.setText(R.string.alivc_recorder_control_click);
            }
        } else if (recordState == RecordState.COUNT_DOWN_RECORDING) {
            mRecordTipTV.setVisibility(GONE);
            aliyunRecordDuration.setVisibility(VISIBLE);
            recordBtnScale(1.25f);
            aliyunRecordBtn.setBackgroundResource(R.mipmap.alivc_svideo_bg_record_pause);
            //拍摄按钮图片 - 拍摄中
            UIConfigManager.setImageBackgroundConfig(aliyunRecordBtn, R.attr.videoShootImageShooting, R.mipmap.alivc_svideo_bg_record_pause);

        } else {
            mRecordTipTV.setVisibility(GONE);
            aliyunRecordDuration.setVisibility(VISIBLE);
            recordBtnScale(1.25f);
            if (recordMode == RecordMode.VIDEO) {
                aliyunRecordBtn.setBackgroundResource(R.mipmap.alivc_svideo_bg_record_start);
                //拍摄按钮图片 - 长按中
                UIConfigManager.setImageBackgroundConfig(aliyunRecordBtn, R.attr.videoShootImageShooting, R.mipmap.alivc_svideo_bg_record_pause);
            } else {
                //PHOTO
                //拍摄按钮图片 - 拍摄中

            }
        }
    }

    /**
     * 更新闪光灯按钮
     */
    private void updateLightSwitchView() {
        if (cameraType == CameraType.FRONT) {
            aliyunSwitchLight.setClickable(false);
            // 前置摄像头状态, 闪光灯图标变灰
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.mipmap.aliyun_svideo_icon_magic_light_off);
            aliyunSwitchLight.setImageDrawable(drawable);
            aliyunSwitchLight.setColorFilter(ContextCompat.getColor(getContext(), R.color.alivc_record_color_filter), PorterDuff.Mode.MULTIPLY);

        } else if (cameraType == CameraType.BACK) {
            aliyunSwitchLight.setClickable(true);
            // 后置摄像头状态, 清除过滤器
            aliyunSwitchLight.clearColorFilter();
            switch (flashType) {
                case ON:
                    aliyunSwitchLight.setSelected(true);
                    aliyunSwitchLight.setActivated(false);
                    UIConfigManager.setImageResourceConfig(aliyunSwitchLight, R.attr.lightImageOpen, R.mipmap.aliyun_svideo_icon_magic_light);
                    break;
                case OFF:
                    aliyunSwitchLight.setSelected(true);
                    aliyunSwitchLight.setActivated(true);
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.mipmap.aliyun_svideo_icon_magic_light_off);
                    aliyunSwitchLight.setImageDrawable(drawable);
                    break;
                default:
                    break;
            }
        }

    }

    public FlashType getFlashType() {
        return flashType;
    }

    public void setFlashType(FlashType flashType) {
        this.flashType = flashType;
        updateLightSwitchView();
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public RecordState getRecordState() {
        if (recordState.equals(RecordState.COUNT_DOWN_RECORDING) || recordState.equals(RecordState.RECORDING)) {
            return RecordState.RECORDING;
        }
        return recordState;
    }

    public void setRecordState(RecordState recordState) {
        if (recordState == RecordState.RECORDING) {
            if (this.recordState == RecordState.READY) {
                this.recordState = RecordState.COUNT_DOWN_RECORDING;
            } else {
                this.recordState = recordState;
            }
        } else {
            this.recordState = recordState;
        }
        updateAllViews();
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 是否有录制片段
     *
     * @param hasRecordPiece
     */
    public void setHasRecordPiece(boolean hasRecordPiece) {
        this.hasRecordPiece = hasRecordPiece;
        updateModeSelView();
        updateDeleteView();
        updateMusicSelView();
    }

    /**
     * 音乐选择弹窗显示回调
     *
     * @param musicSelViewShow
     */
    public void setMusicSelViewShow(boolean musicSelViewShow) {
        isMusicSelViewShow = musicSelViewShow;
        updateAllViews();
    }

    /**
     * 其他特效选择弹窗回调
     *
     * @param effectSelViewShow
     */
    public void setEffectSelViewShow(boolean effectSelViewShow) {
        isEffectSelViewShow = effectSelViewShow;
        updateBottomView();
    }

    /**
     * 设置录制事件，录制过程中持续被调用
     *
     * @param recordTime
     */
    public void setRecordTime(String recordTime) {
        aliyunRecordDuration.setText(recordTime);
    }

    /**
     * 添加各个控件点击监听
     *
     * @param mListener
     */
    public void setControlViewListener(ControlViewListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 设置摄像头类型，并刷新页面，摄像头切换后被调用
     *
     * @param cameraType
     */
    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
        updateLightSwitchView();
    }

    /**
     * 设置complete按钮是否可以点击
     *
     * @param enable
     */
    public void setCompleteEnable(boolean enable) {
        canComplete = enable;
        updateCompleteView();
    }

//    /**
//     * 设置应用音乐icon
//     *
//     * @param icon
//     */
//    public void setMusicIcon(String icon) {
//        new ImageLoaderImpl()
//
//                .loadImage(getContext(), icon, new ImageLoaderOptions.Builder()
//                        .circle()
//                        .error(R.mipmap.aliyun_svideo_music)
//                        .crossFade()
//                        .build())
//                .into(mIvMusicIcon);
//    }

    /**
     * 设置应用音乐icon
     *
     * @param id
     */
//    public void setMusicIconId(@DrawableRes int id) {
//        mIvMusicIcon.setImageResource(id);
//    }

    /**
     * 设置画幅比例
     */
    public void setAspectRatio(int radio) {
        mAspectRatio = radio;
    }

    /**
     * 设置是普通录制还是合拍，合拍不需要音乐和切画幅
     */
    public void setRecordType(Boolean recordType) {
        mRecorderType = recordType;
        if (mRecorderType) {
            mPickerView.setData(Collections.singletonList(getResources().getString(R.string.tab_video)));
        }
        updateTittleView();
    }
}