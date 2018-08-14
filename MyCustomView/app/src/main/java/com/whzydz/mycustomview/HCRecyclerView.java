package com.whzydz.mycustomview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hczhang on 2018/8/10.
 */
public class HCRecyclerView extends RecyclerView{
    //保存上一次的坐标
    private float mLastX;
    private float mLastY;

    private Scroller mScroller;
    //当前操作的item
    private View itemView;
    //上一个操作过的item
    private View preView;

    //水平滑动的最大距离
    private int maxScroll;

    //正在垂直滑动
    private boolean scrollingV = false;

    //动画时间
    private final int ANIM_INTERVAL = 200;

    private DeleteListener deleteListener;

    public HCRecyclerView(Context context) {
        this(context, null);
    }

    public HCRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HCRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext(),new LinearInterpolator());
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            itemView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        LayoutManager layoutManager = getLayoutManager();

        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                //获取点击的view
                View view = findChildViewUnder(x, y);
                if(view == null) {
                    return false;
                }
                //获取View对象的ViewHolder
                HCViewHolder holder = (HCViewHolder)getChildViewHolder(view);
                final int pos = holder.getAdapterPosition();
                //获取item中的linearlayout，通过scroll这个线性布局，侧滑效果
                itemView = holder.getItemView();
                View delView = holder.getDeleteView();

                maxScroll = delView.getWidth();
                //设置删除监听器
                delView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(deleteListener != null) {
                            deleteListener.onDelete(pos);
                        }
                    }
                });

                //关闭上一个展开的View
                if(preView != null) {
                    //备份一个，防止在动画完毕之前用户执行了up操作，而导致动画的View发生变化
                    final View tmpView = preView;
                    int scrollPre = tmpView.getScrollX();
                    if(scrollPre > 0) {
                        ValueAnimator anim = ValueAnimator.ofInt(scrollPre, 0);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int currentValue = (int) animation.getAnimatedValue();
                                tmpView.scrollTo(currentValue, 0);
                            }
                        });
                        anim.setDuration(ANIM_INTERVAL);
                        anim.start();
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                //获取上次的落点与当前的坐标之间的差值
                float dx = mLastX - x;
                float dy = mLastY - y;

                //获取已经滑动的距离
                int scrollMove = itemView.getScrollX();

                //计算移动夹角，小于45度，开始侧滑
                double tanValue = Math.abs(dy) / Math.abs(dx);
                double angle = Math.toDegrees(Math.atan(tanValue));

                if (angle < 20 && !scrollingV) {
                    //如果开始侧滑，禁止上下滑动
                    if(layoutManager instanceof HCLinearLayoutManager) {
                        ((HCLinearLayoutManager)layoutManager).setCanScrollVertically(false);
                    }
                    //已经达到滑动的最左
                    if (scrollMove + dx >= maxScroll){
                        itemView.scrollTo(maxScroll, 0);
                        return true;
                        //已经达到滑动的最右
                    }else if (scrollMove + dx <= 0){
                        itemView.scrollTo(0,0);
                        return true;
                    }

                    itemView.scrollBy((int)dx,0);
                } else if(((HCLinearLayoutManager)layoutManager).canScrollVertically()) {
                    scrollingV = true;
                }

                break;
            case MotionEvent.ACTION_UP:
            default:
                //获取已经滑动的距离
                int scrollUp = itemView.getScrollX();
                //过半就弹出来，否则收回去
                if(scrollUp > maxScroll / 2) {
                    mScroller.startScroll(scrollUp, 0, maxScroll - scrollUp, 0, ANIM_INTERVAL);
                } else {
                    mScroller.startScroll(scrollUp, 0, -scrollUp, 0, ANIM_INTERVAL);
                }

                invalidate();
                //如果UP，使能上下滑动
                if(layoutManager instanceof HCLinearLayoutManager) {
                    ((HCLinearLayoutManager)layoutManager).setCanScrollVertically(true);
                }
                scrollingV = false;
                //up后将该操作item保存起来
                preView = itemView;
                break;
        }

        mLastX = x;
        mLastY = y;

        return super.onTouchEvent(e);
    }

    /**
     *
     * @param <T>:数据集的数据类型
     * @param <VH>：自定义的ViewHolder
     */
    public static abstract class HCAdapter <T, VH extends ViewHolder> extends Adapter {
        private Context context;
        protected List<T> data;
        private int itemLayoutId;

        //删除按钮的padding
        private final int PADDING = 24;

        //删除按钮的颜色
        private final int COLOR_DEL_BG = 0xffff0000;
        //删除文字的颜色
        private final int COLOR_DEL_TXT = 0xffffffff;

        /**
         * adapter构造器
         * @param context：上下文
         * @param data：数据集合，每一个元素，代表了控件id与数据的对应关系
         * @param itemLayoutId：每一行的view
         */
        public HCAdapter(Context context, List<T> data, int itemLayoutId) {
            this.context = context;
            this.data = data;
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout container = new LinearLayout(context);
            container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 150));
            container.setOrientation(LinearLayout.HORIZONTAL);

            //添加每一行的自定义显示内容
            View v = LayoutInflater.from(context).inflate(itemLayoutId, container, false);
            container.addView(v, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            //添加每一行最后固定的删除按钮
            TextView tvDel = new TextView(context);
            tvDel.setId(R.id.tv_delete_item);
            tvDel.setText(R.string.delete);
            tvDel.setTextColor(COLOR_DEL_TXT);
            tvDel.setBackgroundColor(COLOR_DEL_BG);
            tvDel.setGravity(Gravity.CENTER);
            //计算字符串长度
            float tvLength = tvDel.getPaint().measureText(context.getString(R.string.delete));
            //设置删除按钮左右padding
            tvDel.setPadding(PADDING, PADDING, PADDING, PADDING);
            int delLength = Math.round(tvLength + PADDING * 2);
            container.addView(tvDel, new LinearLayoutCompat.LayoutParams(delLength, ViewGroup.LayoutParams.MATCH_PARENT));

            VH holder = createMyViewHolder(container);

            return holder;
        }

        //创建自己的ViewHolder，但是必须继承HCViewHolder
        public abstract VH createMyViewHolder(View v);

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        public T getItem(int position) {
            return data.get(position);
        }

        public void removeItem(int position) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class HCViewHolder extends RecyclerView.ViewHolder {
        protected View itemView;
        protected View deleteView;

        public HCViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            deleteView = itemView.findViewById(R.id.tv_delete_item);
        }

        public View getItemView() {
            return itemView;
        }

        public View getDeleteView() {
            return deleteView;
        }
    }

    public static class HCLinearLayoutManager extends LinearLayoutManager {
        private boolean canScrollVertically = true;
        private boolean canScrollHorizontally = true;

        public HCLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public boolean canScrollVertically() {
            return this.canScrollVertically;
        }

        @Override
        public boolean canScrollHorizontally() {
            return this.canScrollHorizontally;
        }

        public void setCanScrollVertically(boolean canScrollVertically) {
            this.canScrollVertically = canScrollVertically;
        }

        public void setCanScrollHorizontally(boolean canScrollHorizontally) {
            this.canScrollHorizontally = canScrollHorizontally;
        }
    }

    public static interface DeleteListener {
        public void onDelete(int pos);
    }

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }
}
