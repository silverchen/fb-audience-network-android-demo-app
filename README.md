Android Demo App for Facebook Audience Network
==============================================
News feed app to demonstrate native ad integration with [Facebook Audience Network](https://developers.facebook.com/docs/audience-network).

<p align="center"><img width=40% src="https://user-images.githubusercontent.com/6683103/42722935-207d8fac-8787-11e8-8ee8-b3b0631cf274.png"></p>

## Getting started

1. Follow this [guide](https://www.facebook.com/business/help/1195459597167215) to register an account for Audience Network and setup the ad placements.

2. Get the API key from News API [here](https://newsapi.org/register).

3. Replace the ad placements ids and API key in _fb-audience-network-android-demo-app\app\src\main\java\com\silver\mynews\api\Constants.java_.
```groovy
package com.silver.mynews.api;

public interface Constants {
    String BASE_URL = "https://newsapi.org/v2/";
    String API_KEY = "YOUR_NEWS_API_API_KEY";
    int DEFAULT_PAGE_SIZE = 20;
    String NATIVE_AD_PLACEMENT_ID = "YOUR_NATIVE_AD_PLACEMENT_ID";
    String INTERSTITIAL_AD_PLACEMENT_ID = "YOUR_INTERSTITIAL_AD_PLACEMENT_ID";
    String BANNER_AD_PLACEMENT_ID = "YOUR_BANNER_AD_PLACEMENT_ID";
    int NUM_ADS = 5;
}
```

## Built With
* [Retrofit](https://github.com/square/retrofit) - HTTP client
* [Piccaso](https://github.com/square/picasso) - Image download and caching
* [FinestWebView](https://github.com/TheFinestArtist/FinestWebView-Android) - Customizable webview
* [News API](https://newsapi.org/) - Source of content

## License
This project is licensed under the MIT License.

## More examples:
[Sample apps from Facebook](https://github.com/fbsamples/audience-network)
