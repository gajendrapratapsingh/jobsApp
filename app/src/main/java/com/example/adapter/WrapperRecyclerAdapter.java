package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.example.jobs.R;
import com.example.model.Job;
import com.example.util.AppUtil;
import com.example.util.Events;
import com.example.util.GlideApp;
import com.example.util.NetworkUtils;
import com.example.util.OnLoadMoreListener;
import com.example.util.RvOnClickListener;
import com.example.util.TryAgainListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.button.MaterialButton;
import com.ixidev.gdpr.GDPRChecker;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;

public abstract class WrapperRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean mShouldLoadMore = true;
    private boolean mIsLoading = false;
    private final boolean isNativeAd = AppUtil.isNative;
    private final int nativeAdCount = AppUtil.nativeAdCount;
    private OnLoadMoreListener mLoadMoreListener;
    private TryAgainListener tryAgainListener;
    RvOnClickListener<T> rvOnClickListener;

    Activity activity;
    ArrayList<T> mList;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_EMPTY = 2;
    public static final int STATE_ERROR = 3;

    public static final int TYPE_AD = 0;
    public static final int TYPE_CONTENT = 1;
    public static final int TYPE_LOADING = 1000;
    public static final int TYPE_EMPTY = 1001;
    public static final int TYPE_ERROR = 1002;
    public static final int TYPE_LOAD_MORE = 1003;

    private int state = STATE_NORMAL;

    public WrapperRecyclerAdapter(Activity activity) {
        this.activity = activity;
    }

    public WrapperRecyclerAdapter(Activity activity, ArrayList<T> mList) {
        this.activity = activity;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_AD:
                return new AdViewHolder(getOtherViewHolder(parent, R.layout.row_native_ad));
            case TYPE_LOADING:
                return new LoadingViewHolder(getOtherViewHolder(parent, R.layout.view_state_loading));
            case TYPE_EMPTY:
                return new EmptyViewHolder(getOtherViewHolder(parent, R.layout.view_state_empty));
            case TYPE_ERROR:
                return new ErrorViewHolder(getOtherViewHolder(parent, NetworkUtils.isConnected(activity) ? R.layout.view_state_error : R.layout.view_state_no_internet));
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(getOtherViewHolder(parent, R.layout.view_state_loadmore));
        }
        // return originalAdapter.onCreateViewHolder(parent, viewType);
        return onCreateVH(parent, viewType);
    }

    @Override
    @SuppressLint("InflateParams")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CONTENT) {
            T item = mList.get(position);
            onBindVH(holder, position, item);
            if (mShouldLoadMore && !mIsLoading) {
                int threshold = getVisibleThreshold();
                boolean hasReachedThreshold = position >= getCount() - threshold;
                if (hasReachedThreshold) {
                    mIsLoading = true;
                    if (mLoadMoreListener != null) {
                        mLoadMoreListener.onLoadMore();
                    }
                }
            }
        } else if (holder.getItemViewType() == TYPE_AD) {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            if (adViewHolder.adViewLayout.getChildCount() == 0 && isNativeAd) {
                switch (AppUtil.adNetworkType) {
                    case AppUtil.admobAd:
                        AdLoader adLoader = new AdLoader.Builder(activity, AppUtil.nativeId)
                                .forNativeAd(nativeAd -> {
                                    NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_admob, adViewHolder.adViewLayout, false);
                                    populateNativeAdmob(nativeAd, adView);
                                    adViewHolder.adViewLayout.removeAllViews();
                                    adViewHolder.adViewLayout.addView(adView);
                                    adViewHolder.adViewLayout.setVisibility(View.VISIBLE);
                                }).build();
                        GDPRChecker.Request request = GDPRChecker.getRequest();
                        AdRequest.Builder builder = new AdRequest.Builder();
                        if (request == GDPRChecker.Request.NON_PERSONALIZED) {
                            Bundle extras = new Bundle();
                            extras.putString("npa", "1");
                            builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
                        }
                        adLoader.loadAd(builder.build());
                        break;
                    case AppUtil.facebookAd:
                        LinearLayout adView = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_facebook, adViewHolder.adViewLayout, false);
                        com.facebook.ads.NativeAd nativeAd = new com.facebook.ads.NativeAd(activity, AppUtil.nativeId);
                        NativeAdListener nativeAdListener = new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(Ad ad) {

                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {

                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                if (nativeAd != ad) {
                                    return;
                                }
                                populateNativeFacebook(nativeAd, adView, adViewHolder.adViewLayout);
                            }

                            @Override
                            public void onAdClicked(Ad ad) {

                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {

                            }
                        };
                        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
                        break;
                    case AppUtil.startAppAd:
                        StartAppNativeAd startAppNativeAd = new StartAppNativeAd(activity);
                        startAppNativeAd.setPreferences(new NativeAdPreferences().setAdsNumber(1).setAutoBitmapDownload(true).setPrimaryImageSize(2));
                        startAppNativeAd.loadAd(new AdEventListener() {
                            @Override
                            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                if (startAppNativeAd.getNativeAds().size() > 0) {
                                    populateNativeStartApp(startAppNativeAd, adViewHolder.adViewLayout);
                                }
                            }

                            @Override
                            public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {

                            }
                        });
                        break;
                    case AppUtil.appLovinMaxAd:
                        MaxNativeAdLoader maxNativeAdLoader = new MaxNativeAdLoader(AppUtil.nativeId, activity);
                        maxNativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                            @Override
                            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                                super.onNativeAdLoaded(maxNativeAdView, maxAd);
                                if (maxNativeAdView != null) {
                                    maxNativeAdView.setPadding(0, 0, 0, 10);
                                    maxNativeAdView.setBackgroundColor(Color.WHITE);
                                    adViewHolder.adViewLayout.removeAllViews();
                                    adViewHolder.adViewLayout.addView(maxNativeAdView);
                                    adViewHolder.adViewLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        maxNativeAdLoader.loadAd();
                        break;
                    case AppUtil.wortiseAd:
                        GoogleNativeAd googleNativeAd = new GoogleNativeAd(activity, AppUtil.nativeId, new GoogleNativeAd.Listener() {
                            @Override
                            public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {

                            }

                            @Override
                            public void onNativeFailed(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.wortise.ads.AdError adError) {

                            }

                            @Override
                            public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                            }

                            @Override
                            public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.google.android.gms.ads.nativead.NativeAd nativeAd) {
                                NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_wortise, null);
                                populateNativeAdmob(nativeAd, adView);
                                adViewHolder.adViewLayout.removeAllViews();
                                adViewHolder.adViewLayout.addView(adView);
                                adViewHolder.adViewLayout.setVisibility(View.VISIBLE);
                            }
                        });
                        googleNativeAd.load();
                        break;
                }
            }
        }
    }


    protected abstract RecyclerView.ViewHolder onCreateVH(@NonNull ViewGroup parent, int viewType);

    protected abstract void onBindVH(@NonNull RecyclerView.ViewHolder holder, int position, T item);

    @Override
    public int getItemViewType(int position) {
        switch (state) {
            case STATE_LOADING:
                return TYPE_LOADING;
            case STATE_EMPTY:
                return TYPE_EMPTY;
            case STATE_ERROR:
                return TYPE_ERROR;
        }
        return isLoadMore(position) ? TYPE_LOAD_MORE : isAdPosition(position) ? TYPE_AD : TYPE_CONTENT; //isAdPosition(position) ? TYPE_AD :
    }


    @Override
    public int getItemCount() {
        switch (state) {
            case STATE_LOADING:
            case STATE_EMPTY:
            case STATE_ERROR:
                return 1;
        }

        int actualCount = getCount();
        if (actualCount == 0 || !mShouldLoadMore) {
            return actualCount;
        } else {
            return actualCount + 1;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_CONTENT ? 1 : gridManager.getSpanCount();
                }
            });
        }
    }

    private View getOtherViewHolder(ViewGroup parent, int layout) {
        return LayoutInflater.from(activity).inflate(layout, parent, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setState(int state) {
        this.state = state;
        notifyDataSetChanged();
    }

    private int getState() {
        return state;
    }

    private boolean isAdPosition(int pos) {
        return isNativeAd && pos != -1 && mList.get(pos) == null;
    }

    private boolean isLoadMore(int pos) {
        return pos == getCount() && mShouldLoadMore;
    }

    private static class AdViewHolder extends RecyclerView.ViewHolder {
        LinearLayout adViewLayout;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            adViewLayout = itemView.findViewById(R.id.adViewLayout);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {

        MaterialButton btnTryAgain;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            btnTryAgain = itemView.findViewById(R.id.btnTryAgain);
            btnTryAgain.setOnClickListener(view -> {
                if (tryAgainListener != null) {
                    tryAgainListener.onTryAgain();
                }
            });
        }
    }

    private class ErrorViewHolder extends RecyclerView.ViewHolder {
        MaterialButton btnTryAgain;

        public ErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            btnTryAgain = itemView.findViewById(R.id.btnTryAgain);
            btnTryAgain.setOnClickListener(view -> {
                if (tryAgainListener != null) {
                    tryAgainListener.onTryAgain();
                }
            });
        }
    }

    private static class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        public LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void onLoading() {
        setState(STATE_LOADING);
    }

    public void onError() {
        setState(STATE_ERROR);
    }

    public void onEmpty() {
        setState(STATE_EMPTY);
    }

    public void onNormal() {
        setState(STATE_NORMAL);
    }

    public void setOnTryAgainListener(TryAgainListener listener) {
        this.tryAgainListener = listener;
    }

    public void setOnItemClickListener(RvOnClickListener<T> clickListener) {
        this.rvOnClickListener = clickListener;
    }

    /**
     * Set as false when you don't want the recycler view to load more data.
     * This will also remove the loading view
     */

    public void setShouldLoadMore(boolean shouldLoadMore) {
        this.mShouldLoadMore = shouldLoadMore;
    }

    /**
     * Registers a callback to be notified when there is a need to load more data
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    /**
     * This informs the adapter that <code>itemCount</code> more data has been loaded,
     * starting from <code>positionStart</code>
     * <p>
     * This also calls <code>notifyItemRangeInserted(int, int)</code>,
     * so the implementing class only needs to call this method
     *
     * @param positionStart Position of the first item that was inserted
     * @param itemCount     Number of items inserted
     */
    public void moreDataLoaded(int positionStart, int itemCount) {
        mIsLoading = false;
        notifyItemRemoved(positionStart); // remove the loading view
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public int getVisibleThreshold() {
        return 5;
    }

    public void setListAll(ArrayList<T> data) {
        if (mList == null) mList = new ArrayList<>();
        mList.clear();
        if (data != null && !data.isEmpty()) {
            if (isNativeAd) {
                wrapperAdList(data);
            } else {
                mList.addAll(data);
            }

        }
        invalidateState(mList.size());
    }

    private void wrapperAdList(ArrayList<T> data) {
        for (int i = 0; i < data.size(); i++) {
            mList.add(data.get(i));
            int lastAd = mList.lastIndexOf(null);
            if ((mList.size() - lastAd) % nativeAdCount == 0) {
                mList.add(null);
            }
        }
    }

    private int getCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setListMore(ArrayList<T> data) {
        int currSize = mList.size();
        if (isNativeAd) {
            wrapperAdList(data);
        } else {
            mList.addAll(data);
        }
        moreDataLoaded(currSize, mList.size() - currSize);
    }

    private void invalidateState(int size) {
        if (size > 0) {
            onNormal();
        } else {
            onEmpty();
        }
    }

    public void onEvent(Events.SaveJob saveJob) {
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i) != null) {
                    Object obj = mList.get(i);
                    if (obj instanceof Job) {
                        Job item = (Job) obj;
                        if (item.getJobId().equals(saveJob.getJobId())) {
                            if (saveJob.isRemoved()) {
                                mList.remove(i);
                                notifyItemRemoved(i);
                                if (mList.isEmpty()) {
                                    onEmpty();
                                }
                            } else {
                                item.setJobSaved(saveJob.isSave());
                                notifyItemChanged(i, item);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void populateNativeAdmob(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    private void populateNativeFacebook(com.facebook.ads.NativeAd nativeAd, LinearLayout adView, LinearLayout adViewLayout) {
        adViewLayout.addView(adView);
        NativeAdLayout nativeAdLayout = new NativeAdLayout(activity);
        AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);

        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        com.facebook.ads.MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adViewLayout,
                nativeAdMedia,
                clickableViews);
    }

    private void populateNativeStartApp(StartAppNativeAd startAppNativeAd, LinearLayout adViewLayout) {
        RelativeLayout nativeAdView = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, adViewLayout, false);

        ImageView icon = nativeAdView.findViewById(R.id.icon);
        TextView title = nativeAdView.findViewById(R.id.title);
        TextView description = nativeAdView.findViewById(R.id.description);
        Button button = nativeAdView.findViewById(R.id.button);

        GlideApp.with(activity)
                .load(startAppNativeAd.getNativeAds().get(0).getImageUrl())
                .into(icon);
        title.setText(startAppNativeAd.getNativeAds().get(0).getTitle());
        description.setText(startAppNativeAd.getNativeAds().get(0).getDescription());
        button.setText(startAppNativeAd.getNativeAds().get(0).isApp() ? "Install" : "Open");

        adViewLayout.removeAllViews();
        adViewLayout.addView(nativeAdView);
        adViewLayout.setVisibility(View.VISIBLE);
    }
}
