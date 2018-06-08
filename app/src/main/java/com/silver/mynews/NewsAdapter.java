package com.silver.mynews;

import android.content.Context;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.NativeAd;
import com.silver.mynews.databinding.AdItemBinding;
import com.silver.mynews.databinding.NewsItemBinding;
import com.silver.mynews.model.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;
    private static final int TYPE_AD = 1;
    private static final float SCREEN_TO_WIDTH_RATIO = 0.8f;
    private static final double WIDTH_TO_HEIGHT_RATIO = 2.11;
    private List<Article> mArticles;
    private List<NativeAd> mAds;
    private final OnItemClickListener mItemClickListener;
    private int mAdPosition;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(Article item);
    }

    public NewsAdapter(Context context, List<Article> articles, List<NativeAd> ads, OnItemClickListener itemClickListener, int adPosition) {
        mContext = context;
        mArticles = articles;
        mAds = ads;
        mItemClickListener = itemClickListener;
        mAdPosition = adPosition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_AD) {
            AdItemBinding adItemBinding = AdItemBinding.inflate(inflater, parent, false);
            return new AdViewHolder(adItemBinding);
        }

        NewsItemBinding newsItemBinding = NewsItemBinding.inflate(inflater, parent, false);
        return new ArticleViewHolder(newsItemBinding);
    }

    @Override
    public int getItemViewType(int position) {
        if (mAds.size() > 0 && position > 0 && position % mAdPosition == 0 && position / mAdPosition < mAds.size()) {
            return TYPE_AD;
        }
        return TYPE_ARTICLE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_AD) {
            int adjustedPosition = position / mAdPosition;
            if (adjustedPosition >= 0 && adjustedPosition < mAds.size()) {
                ((AdViewHolder) holder).bind(mAds.get(adjustedPosition));
            }
        } else {
            int adjustedPosition = position - Math.min((position / mAdPosition), mAdPosition - 1);
            if (adjustedPosition >= 0 && adjustedPosition < mArticles.size()) {
                ((ArticleViewHolder) holder).bind(mArticles.get(adjustedPosition));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mArticles.size() + mAds.size();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        NewsItemBinding mNewsItemBinding;

        public ArticleViewHolder(NewsItemBinding binding) {
            super(binding.getRoot());

            mNewsItemBinding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        void bind(Article data) {
            // Download and load image
            Picasso.get()
                .load(data.getUrlToImage())
                .placeholder(R.color.colorBackground)
                .into(mNewsItemBinding.ivCoverImg);

            // Set text
            mNewsItemBinding.tvTitle.setText(data.getTitle());
            mNewsItemBinding.tvDescription.setText(data.getDescription());
            mNewsItemBinding.tvSource.setText(data.getSource().getName());

            if (data.getPublishedAt() != null) {
                long now = System.currentTimeMillis();
                String relativeTime = DateUtils.getRelativeTimeSpanString(data.getPublishedAt().getTime(), now, DateUtils.SECOND_IN_MILLIS).toString();
                mNewsItemBinding.tvPostedDateTime.setText(relativeTime);
            } else {
                mNewsItemBinding.tvPostedDateTime.setText("");
            }
        }

        @Override
        public void onClick(View view) {
            int index = getAdapterPosition();
            mItemClickListener.onItemClick(mArticles.get(index));
        }
    }

    class AdViewHolder extends RecyclerView.ViewHolder {
        AdItemBinding mAdItemBinding;

        public AdViewHolder(AdItemBinding binding) {
            super(binding.getRoot());

            mAdItemBinding = binding;
        }

        void bind(NativeAd data) {
            // Set the Text.
            mAdItemBinding.tvTitle.setText(data.getAdTitle());
            mAdItemBinding.tvSocialContext.setText(data.getAdSocialContext());
            mAdItemBinding.tvDescription.setText(data.getAdBody());
            mAdItemBinding.btnCTA.setText(data.getAdCallToAction());

            // Download and display the ad icon.
            NativeAd.Image adIcon = data.getAdIcon();
            NativeAd.downloadAndDisplayImage(adIcon, mAdItemBinding.ivCoverImg);

            // Add the AdChoices icon
            mAdItemBinding.adChoicesContainer.removeAllViewsInLayout();
            AdChoicesView adChoicesView = new AdChoicesView(mContext, data, true);
            mAdItemBinding.adChoicesContainer.addView(adChoicesView);

            // Download and display the cover image.
            mAdItemBinding.adMediaView.setNativeAd(data);

            // Register the Title and CTA button to listen for clicks.
            List<View> clickableViews = new ArrayList<>();
            clickableViews.add(mAdItemBinding.tvTitle);
            clickableViews.add(mAdItemBinding.ivCoverImg);
            clickableViews.add(mAdItemBinding.adMediaView);
            clickableViews.add(mAdItemBinding.btnCTA);
            data.registerViewForInteraction(mAdItemBinding.adContainer, clickableViews);

            //configureLayoutToAspectRatio(mAdItemBinding.adMediaView, mAdItemBinding.adContainer);
        }

        /*private void configureLayoutToAspectRatio(final View view, final View containerView) {
//            DisplayMetrics displayMetrics = view.getContext().getResources().getDisplayMetrics();
//            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
//            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

//            WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
//            Display display = wm.getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);

//            float cal = (containerView.getContext().g() * view.getMeasuredWidth() / view.getMeasuredHeight());
//
//            view.setLayoutParams(
//                new FrameLayout.LayoutParams(
//                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cal, mContext.getResources().getDisplayMetrics()),
//                    FrameLayout.LayoutParams.MATCH_PARENT));

//            view.setLayoutParams(
//                new FrameLayout.LayoutParams(
//                    (int) (getWidth(view.getContext()) / WIDTH_TO_HEIGHT_RATIO),
//                    FrameLayout.LayoutParams.MATCH_PARENT));

//            view.measure(0,0);
//
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
//                (int) view.getMeasuredWidth(),
//                (int) view.getMeasuredHeight()
//            );

            view.post(new Runnable() {
                @Override
                public void run() {
                    if (containerView.getWidth() > 0) {
                        final int w = containerView.getWidth();
                        final int h = containerView.getWidth() * view.getWidth() / view.getHeight();

                        if (view.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
                            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, mContext.getResources().getDisplayMetrics()),
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, mContext.getResources().getDisplayMetrics())
                            );
                            view.setLayoutParams(layoutParams);
                        } else {
                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, mContext.getResources().getDisplayMetrics()),
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, mContext.getResources().getDisplayMetrics())
                            );
                            view.setLayoutParams(layoutParams);
                        }
                    }
                }
            });
        }*/

//        private int getWidth(Context context) {
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            Display display = wm.getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);
//            return (int) (size.x * SCREEN_TO_WIDTH_RATIO);
//        }
    }
}
