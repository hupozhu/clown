package com.sampson.android.demo.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

/**
 * Created by Administrator on 2016/3/25.
 */
public class ReboundScrollView extends HorizontalScrollView {

    private static final int DEFAULT_DURATION = 300;
    /**
     * 对象动画
     */
    private ObjectAnimator scrollAnimator;
    /**
     * 速率追踪
     */
    private VelocityTracker mVelocityTracker;
    private Spring mSpring;
    private float mLastMotionX, mLastMotionY;
    /**
     * 这是一个距离表示滑动手势要大于这个距离才开始滑动
     */
    private float mTouchSlop;
    /**
     * 获得允许执行一个fling手势动作的最大速度值
     */
    private int mMaxVelocity;
    private boolean mIsDragging;
    private boolean doReboundAnim = false;
    /**
     * 装子view的数组
     */
    private View[] mChildViews;
    private View centerView;
    /**
     * 子布局的总数
     */
    private int childCount = 0;
    private int centerViewIndex;
    /**
     * 当前滑动过的x轴距离 和 当前滑动之后停下的x坐标
     */
    private int mCurrentScrollX, mCurrentScrollXEnd;
    private int dstScrollX;
    // onMeasure中得到的layout的宽度
    private int mWidth;
    private int mViewWidth;
    public static final int NORMAL_ANIM = 0;
    public static final int REBOUND_ANIM = 1;
    private int ANIM_TYPE = NORMAL_ANIM;

    private int firstViewIndex;
    private LinearLayout viewContainer;
    private View firstView;

    public ReboundScrollView(Context context) {
        this(context, null, 0);
    }

    public ReboundScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReboundScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 1、初始化动画等一下配置相关
     */
    @SuppressLint("NewApi")
    private void init(Context context) {

        viewContainer = (LinearLayout) getChildAt(0);

        // 关于硬件加速
        setLayerType(LAYER_TYPE_HARDWARE, null);
        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mMaxVelocity = viewConfiguration.getScaledMaximumFlingVelocity();

        // 初始化一个对象动画
        scrollAnimator = ObjectAnimator.ofInt(this, scrollAnim, 0, 0);//Android4.0以上api
        // 设置动画执行时间
        scrollAnimator.setDuration(DEFAULT_DURATION);
        // 设置动画加速效果
        scrollAnimator.setInterpolator(new DecelerateInterpolator());
        scrollAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mVelocityTracker = VelocityTracker.obtain();

        // Facebook的动画引擎系统，一套模板代码不需要深究
        SpringSystem springSystem = SpringSystem.create();
        mSpring = springSystem.createSpring();
        SpringConfig config = new SpringConfig(70, 9);
        mSpring.setSpringConfig(config);
        mSpring.setCurrentValue(0);
        mSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                if (doReboundAnim) {
                    int x = -(int) (spring.getCurrentValue() * scrollDx) + beginScrollX;
                    if (x <= 0 || (x + mWidth) >= computeHorizontalScrollRange()) { // 这里具体的值在不同情况下不同
                        // 当scrollView的宽度是match
                        // parent时候用mWidth
                        doReboundAnim = false;
                    }
                    scrollTo(x, 0);
                }
            }
        });
    }

    private boolean onDownAllowDrag(float lastMotionX, float lastMotionY) {
        return true;
    }

    private void stopScrolling() {
        if (scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }
    }

    private boolean checkTouchSlop(float dx, float dy) {
        return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
    }

    private boolean onMoveAllowDrag(float dx, float dy) {
        return Math.abs(dx) > Math.abs(dy);
    }

    private void onMoveEvent(float dx, float dy) {
        scrollTo(mCurrentScrollX - (int) dx, 0);

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mCurrentScrollX = l;
        mCurrentScrollXEnd = l + mWidth;
        // setVisibleChildRotation();
        setVisibleChildZoom();
    }

    /**
     * 旋转可见的部分，滑动之后layout的中心点
     */
    private int scrollViewCenterX;
    /**
     * 子布局的左右间隔
     */
    private int viewLeft, viewRight;
    private float scale;
    private float alpha;
    /**
     * 屏幕中心x坐标
     */
    private float windowCenterX;
    /**
     * 当前可见部分
     */
    private boolean isFirstVisible = false;

    /**
     * @Description: 这个方法让子布局来进行缩放
     */
    private void setVisibleChildZoom() {
        isFirstVisible = false;
        scrollViewCenterX = (mCurrentScrollX + mCurrentScrollXEnd) / 2;
        windowCenterX = mCurrentScrollX + mWidth / 2;

        for (int i = 0; i < childCount; ++i) {
            viewLeft = mChildViews[i].getLeft();
            viewRight = mChildViews[i].getRight();
            boolean isViewInit = viewRight - viewLeft == 0 ? false : true;
            if (mCurrentScrollX <= viewLeft && mCurrentScrollXEnd >= viewLeft || mCurrentScrollX <= viewRight && mCurrentScrollXEnd >= viewRight) {
                isFirstVisible = true;
                scale = 1 - Math.abs((float) ((((viewLeft + viewRight) / 2 - scrollViewCenterX) / (float) mWidth) * 0.2));
                alpha = 1 - Math.abs((float) ((viewLeft + viewRight) / 2 - scrollViewCenterX) / (float) mWidth);
                mChildViews[i].setScaleX(scale);
                mChildViews[i].setScaleY(scale);
                mChildViews[i].setAlpha(alpha);
                /**
                 * 找出处于屏幕中间的view
                 * */
                if (!(viewLeft <= windowCenterX && viewRight >= windowCenterX)) {
                    continue;
                }
                if (centerView != mChildViews[i]) {
                    centerView = mChildViews[i];
                    centerViewIndex = i;
                    if (mScrollChangeListener != null && isViewInit) {
                        mScrollChangeListener.OnScrollChange(i);
                    }
                }
            } else if (isFirstVisible) {
                /**
                 * 不再遍历后面不可见部分
                 * */
                break;
            }
        }
    }

    private int scrollDx;
    private int beginScrollX;

    private void startReboundAnim() {
        scrollDx = mCurrentScrollX - dstScrollX;
        beginScrollX = mCurrentScrollX;
        mSpring.setCurrentValue(0f);
        doReboundAnim = true;
        mSpring.setEndValue(1);
    }

    private void stopReboundAnim() {
        doReboundAnim = false;
        double stopValue = mSpring.getCurrentValue();
        mSpring.setCurrentValue(stopValue);
    }

    private void startScrollAnim() {
        scrollAnimator.setIntValues(mCurrentScrollX, dstScrollX);
        scrollAnimator.start();
    }

    private void onActionUp() {
        mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
        final int initialVelocity = (int) mVelocityTracker.getXVelocity();//获取到x方向的速率
        mVelocityTracker.clear();

        calcScrollDst(initialVelocity);

        if (ANIM_TYPE == NORMAL_ANIM)
            startScrollAnim();
        else
            startReboundAnim();
    }

    private void calcScrollDst(int initialVelocity) {
        if (Math.abs(initialVelocity) < 300) {//慢速滑动
//            dstScrollX = centerView.getLeft() + centerView.getWidth() / 2 - mWidth / 2;//算出view中心点，到屏幕中心点的距离差
            dstScrollX = firstView.getLeft() - 0;
            startScrollAnim(); //开始做mCurrentScrollX的属性变换
            return;
        }

//        if (initialVelocity > 0) {//快速右滑
//            if (centerViewIndex >= 1) {
//                dstScrollX = mChildViews[centerViewIndex - 1].getLeft() + mChildViews[centerViewIndex - 1].getWidth() / 2 - mWidth / 2;
//            } else {
//                dstScrollX = centerView.getLeft() + centerView.getWidth() / 2 - mWidth / 2;
//            }
//        } else {    //快速左滑
//            if (centerViewIndex <= childCount - 2) {
//                dstScrollX = mChildViews[centerViewIndex + 1].getLeft() + mChildViews[centerViewIndex + 1].getWidth() / 2 - mWidth / 2;
//            } else {
//                dstScrollX = centerView.getLeft() + centerView.getWidth() / 2 - mWidth / 2;
//            }
//        }
        if(initialVelocity > 0){
            if(firstViewIndex >= 1){
                dstScrollX = viewContainer.getChildAt(firstViewIndex-1).getLeft();
            }else{
                dstScrollX = centerView.getLeft();
            }
        }else{
            //TODO:还要再想想
//            if(firstViewIndex <= viewContainer.getChildCount()-2){
//                dstScrollX = viewContainer.getChildAt()
//            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        View view;
//        mWidth = getWidth();

        // 给子view设置中点坐标
//        if (mChildViews != null) {
//            mViewWidth = mChildViews[0].getWidth();
//            for (int i = 0; i < childCount; ++i) {
//                view = mChildViews[i];
//                view.setPivotX(view.getWidth() / 2);
//                view.setPivotY(view.getHeight() / 2);
//            }
//
//            mCurrentScrollX = getScrollX();
//            mCurrentScrollXEnd = mCurrentScrollX + mWidth;
//            //使可见的view进行缩放，并找到中间的view
//            setVisibleChildZoom();
//        }

        mCurrentScrollX = getScrollX();
        View view = viewContainer.getChildAt(0);
        //第一个显示的view,index从0开始
        firstViewIndex = mCurrentScrollX / view.getWidth();
        firstView = viewContainer.getChildAt(firstViewIndex);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // TODO:如果动画没有执行完成就不监听手势
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();

                if (onDownAllowDrag(mLastMotionX, mLastMotionY)) {//始终返回true
                    mIsDragging = false;// action down 未开始drag
                    if (ANIM_TYPE == NORMAL_ANIM) {//初始化为normal_anim
                        stopScrolling();//停止scrollAnimator动画
                    } else
                        stopReboundAnim();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                if (checkTouchSlop(x - mLastMotionX, y - mLastMotionY)) {//判断x方向滑动距离有效
                    mIsDragging = true;
                    mLastMotionX = x;
                    mLastMotionY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                onActionUp();
                break;
        }
        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                // if (x - mLastMotionX < 0) {
                // isScrollLeft = true;
                // } else {
                // isScrollLeft = false;
                // }
                onMoveEvent(x - mLastMotionX, y - mLastMotionY);//scroll一定的距离
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                onActionUp();
                break;
        }
        return true;
    }

    public void setChildViews(View[] views) {
        mChildViews = views;
        childCount = mChildViews.length;
    }

    private int dx;
    @SuppressLint("NewApi")
    Property<ReboundScrollView, Integer> scrollAnim = new Property<ReboundScrollView, Integer>(Integer.class, "mCurrentScrollX") {
        @Override
        public Integer get(ReboundScrollView object) {
            return object.mCurrentScrollX;
        }

        @Override
        public void set(ReboundScrollView object, Integer value) {
            scrollTo(value, 0);
        }
    };

    private OnScrollChangeListener mScrollChangeListener;

    public interface OnScrollChangeListener {
        public void OnScrollChange(int centerViewIndex);
    }

    public void setScrollChangeListener(OnScrollChangeListener scrollChangeListener) {
        mScrollChangeListener = scrollChangeListener;
    }

    public void setAnimType(int animType) {
        if (animType != NORMAL_ANIM && animType != REBOUND_ANIM) {
            throw new IllegalArgumentException("animType should be NORMAL_ANIM or REBOUND_ANIM");
        }
        ANIM_TYPE = animType;
    }

    public void smoothScrollViewToIndex(int index) {
        if (0 <= index && index <= mChildViews.length)
            smoothScrollTo(mViewWidth * index, 0);
        // loadImage(index);
    }

}