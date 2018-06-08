package com.silver.mynews;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.silver.mynews.api.EverythingApi;
import com.silver.mynews.model.Article;
import com.silver.mynews.model.Everything;
import com.thefinestartist.finestwebview.FinestWebView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.silver.mynews.api.Constants.*;

public class MainActivity extends AppCompatActivity implements NewsAdapter.OnItemClickListener {
    //public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerRefreshLayout mRecyclerRefreshLayout;
    private RecyclerView rvNews;
    private NewsAdapter mNewsAdapter;
    private final CustomOnScrollListener mCustomOnScrollListener = new CustomOnScrollListener();
    private boolean mIsLoading;
    private boolean mIsTheEnd;
    private int mPage = 1;
    private int mTotalPages = 0;
    private List<Article> mArticles = new ArrayList<>();
    private List<NativeAd> mAds = new ArrayList<>();
    private NativeAdsManager mAdsManager;
//    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleIntent(getIntent());

        setupAdsManager();
        setupRecycleView();
        setupData();

//        interstitialAd = new InterstitialAd(this, INTERSTITIAL_AD_PLACEMENT_ID);
//        // Set listeners for the Interstitial Ad
//        interstitialAd.setAdListener(new InterstitialAdListener() {
//            @Override
//            public void onInterstitialDisplayed(Ad ad) {
//                // Interstitial displayed callback
//            }
//
//            @Override
//            public void onInterstitialDismissed(Ad ad) {
//                // Interstitial dismissed callback
//            }
//
//            @Override
//            public void onError(Ad ad, AdError adError) {
//                // Ad error callback
//                Toast.makeText(MainActivity.this, "Error: " + adError.getErrorMessage(),
//                    Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                // Show the ad when it's done loading.
//                interstitialAd.show();
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//                // Ad clicked callback
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//                // Ad impression logged callback
//            }
//        });
//
//        // For auto play video ads, it's recommended to load the ad
//        // at least 30 seconds before it is shown
//        interstitialAd.loadAd();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearchQuery(query);
        }
    }

    @Override
    protected void onDestroy() {
        rvNews.removeOnScrollListener(mCustomOnScrollListener);
        rvNews = null;
        mAdsManager.setListener(null);
        mAdsManager = null;
        mRecyclerRefreshLayout.setOnRefreshListener(null);
        mRecyclerRefreshLayout = null;

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                performSearchQuery(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                refreshData();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_search:
                //onSearchRequested();
                return true;
            case R.id.menu_sort:
                // TODO Add sort function
                //Intent intent = new Intent(this, DisplayMessageActivity.class);
                //intent.putExtra(EXTRA_MESSAGE, message);
                //startActivity(intent);
                return true;
        }

        return false;
    }

    @Override
    public void onItemClick(Article item) {
        new FinestWebView.Builder(this)
            .titleDefault("Loading...")
            .iconDefaultColorRes(R.color.Color_White)
            .webViewUseWideViewPort(true)
            .webViewSaveFormData(true)
            .webViewSavePassword(true)
            .webViewDomStorageEnabled(true)
            .webViewAppCacheEnabled(true)
            .webViewEnableSmoothTransition(true)
            .webViewCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK)
            .webViewRenderPriority(WebSettings.RenderPriority.HIGH)
            .webViewLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS)
            .show(item.getUrl());
    }

    public class CustomOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            RecyclerView.LayoutManager manager = view.getLayoutManager();
            if (manager.getChildCount() > 0) {
                int count = manager.getItemCount();
                int last = ((RecyclerView.LayoutParams) manager
                    .getChildAt(manager.getChildCount() - 1).getLayoutParams()).getViewAdapterPosition();

                if (last == count - 5 && mArticles.size() > 0 && !mIsLoading && !mIsTheEnd) {
                    loadMoreData();
                }
            }
        }
    }

    private void setupAdsManager() {
        mAdsManager = new NativeAdsManager(this, NATIVE_AD_PLACEMENT_ID, NUM_ADS);
        mAdsManager.setListener(new NativeAdsManager.Listener() {
            @Override
            public void onAdsLoaded() {
                if (mAdsManager.getUniqueNativeAdCount() != 0) {
                    mAds.clear();
                    for (int i = 0; i < mAdsManager.getUniqueNativeAdCount(); i++) {
                        mAds.add(mAdsManager.nextNativeAd());
                    }
                    mNewsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onAdError(AdError adError) {
                Log.w(TAG, "Facebook Ad Error: '" + adError.getErrorMessage() + "'");
            }
        });

        mAdsManager.loadAds(NativeAd.MediaCacheFlag.ALL);
    }

    private void setupRecycleView() {
        mRecyclerRefreshLayout = findViewById(R.id.refreshLayout);
        mRecyclerRefreshLayout.setNestedScrollingEnabled(true);
        mRecyclerRefreshLayout.setRefreshView(new RefreshView(this), new ViewGroup.LayoutParams(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()),
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())
        ));

        mRecyclerRefreshLayout.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvNews = findViewById(R.id.rvNews);
        rvNews.setLayoutManager(layoutManager);
        //rvNews.setHasFixedSize(true);
        rvNews.addOnScrollListener(mCustomOnScrollListener);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.news_item_divider));
        rvNews.addItemDecoration(itemDecorator);

        mNewsAdapter = new NewsAdapter(this, mArticles, mAds, this, getResources().getInteger(R.integer.ad_position));
        rvNews.setAdapter(mNewsAdapter);
    }

    private void setupData() {
        mIsLoading = true;
        mRecyclerRefreshLayout.setRefreshing(true);

        callEverythingApi(new Callback<Everything>() {
            @Override
            public void onResponse(Call<Everything> call, Response<Everything> response) {
                mIsLoading = false;
                mRecyclerRefreshLayout.setRefreshing(false);

                Everything body = response.body();
                if (body != null) {
                    mTotalPages = body.getTotalResults();
                    mArticles.addAll(body.getArticles());
                    mNewsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Everything> call, Throwable t) {
                mIsLoading = false;
                mRecyclerRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshData() {
        mPage = 1;
        mIsTheEnd = false;
        mIsLoading = true;

        callEverythingApi(new Callback<Everything>() {
            @Override
            public void onResponse(Call<Everything> call, Response<Everything> response) {
                mIsLoading = false;
                mRecyclerRefreshLayout.setRefreshing(false);

                Everything body = response.body();
                if (body != null) {
                    mTotalPages = body.getTotalResults();
                    mArticles.clear();
                    mArticles.addAll(body.getArticles());
                    mNewsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Everything> call, Throwable t) {
                mIsLoading = false;
                mRecyclerRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreData() {
        if (mPage * DEFAULT_PAGE_SIZE >= mTotalPages) {
            mIsTheEnd = true;
            return;
        }

        mPage += 1;
        mIsLoading = true;

        callEverythingApi(new Callback<Everything>() {
            @Override
            public void onResponse(Call<Everything> call, Response<Everything> response) {
                mIsLoading = false;

                Everything body = response.body();
                if (body != null) {
                    mArticles.addAll(body.getArticles());
                    mNewsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Everything> call, Throwable t) {
                mIsLoading = false;
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearchQuery(String query) {
        mPage = 1;
        mIsTheEnd = false;
        mIsLoading = true;
        mRecyclerRefreshLayout.setRefreshing(true);

        callEverythingApiWithQuery(query, new Callback<Everything>() {
            @Override
            public void onResponse(Call<Everything> call, Response<Everything> response) {
                mIsLoading = false;
                mRecyclerRefreshLayout.setRefreshing(false);

                Everything body = response.body();
                if (body != null) {
                    mTotalPages = body.getTotalResults();
                    mArticles.clear();
                    mArticles.addAll(body.getArticles());
                    mNewsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Everything> call, Throwable t) {
                mIsLoading = false;
                mRecyclerRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callEverythingApi(Callback<Everything> callback) {
        callEverythingApiWithQuery(null, callback);
    }

    private void callEverythingApiWithQuery(String query, Callback<Everything> callback) {
        Call<Everything> call = EverythingApi.ApiClient().get(mPage, DEFAULT_PAGE_SIZE, "bbc.co.uk,wsj.com,techcrunch.com,engadget.com,nytimes.com", "en", null, query);
        call.enqueue(callback);
    }
}
