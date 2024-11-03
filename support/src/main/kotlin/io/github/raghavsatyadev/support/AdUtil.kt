package io.github.raghavsatyadev.support

import android.app.Activity
import android.view.Display
import androidx.core.hardware.display.DisplayManagerCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.github.raghavsatyadev.support.listeners.ResultListener


object AdUtil {
    fun loadBannerAd(activity: Activity, adContainerView: AdView) {
        val adView = AdView(activity)
        adView.setAdSize(getAdSize(activity, adContainerView))
        adView.adUnitId = getBannerAdID(activity)
        adContainerView.removeAllViews()
        adContainerView.addView(adView)


        // Start loading the ad in the background.
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun getBannerAdID(activity: Activity): String {
        return activity.getString(
            if (BuildConfig.DEBUG) {
                R.string.admob_test_banner_ad_unit_id
            } else {
                R.string.admob_banner_ad_unit_id
            }
        )
    }

    private fun getAdSize(activity: Activity, adContainerView: AdView): AdSize {
        // Determine the screen width (less decorations) to use for the ad width.
        val defaultDisplay =
            DisplayManagerCompat.getInstance(activity).getDisplay(Display.DEFAULT_DISPLAY)
        val metrics = activity.createDisplayContext(defaultDisplay!!).resources.displayMetrics

        val density = metrics.density

        var adWidthPixels = adContainerView.width

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = metrics.widthPixels
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }


    fun loadInterstitialAd(activity: Activity, listener: ResultListener<InterstitialAd?>) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity, getInterstitialAdID(activity), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.setImmersiveMode(true)
                    listener.onResult(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    listener.onResult(null)
                }
            })
    }

    fun InterstitialAd.showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        if (BuildConfig.DEBUG) {
            onAdClosed()
        } else {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClosed()
                }
            }
            show(activity)
        }
    }

    private fun getInterstitialAdID(activity: Activity): String {
        return activity.getString(
            if (BuildConfig.DEBUG) {
                R.string.admob_test_interstitial_ad_unit_id
            } else {
                R.string.admob_interstitial_ad_unit_id
            }
        )
    }
}