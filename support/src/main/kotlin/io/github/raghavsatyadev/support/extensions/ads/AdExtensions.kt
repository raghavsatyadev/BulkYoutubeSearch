@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.raghavsatyadev.support.extensions.ads

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import io.github.raghavsatyadev.support.BuildConfig
import kotlinx.coroutines.launch

object AdExtensions {
    private var interstitialAd: InterstitialAd? = null

    // region Activity
    fun ComponentActivity.showInterstitialAd(onAdClosed: () -> Unit) {
        interstitialAd?.let {
            it.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdClosed()
                    super.onAdDismissedFullScreenContent()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    onAdClosed()
                    super.onAdFailedToShowFullScreenContent(p0)
                }
            }
            it.show(this)
            loadInterstitialAd()
        } ?: {
            if (BuildConfig.DEBUG) {
                onAdClosed()
            } else {
                loadInterstitialAd()
            }
        }
    }

    fun ComponentActivity.loadAds(adView: AdView) {
        showBannerAd(adView)
        loadInterstitialAd()
    }

    fun ComponentActivity.loadInterstitialAd() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                AdUtil.loadInterstitialAd(this@loadInterstitialAd) {
                    interstitialAd = it
                }
            }
        }
    }

    fun ComponentActivity.showBannerAd(adView: AdView) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AdUtil.loadBannerAd(this@showBannerAd, adView)
            }
        }
    }
    // endregion

    // region Fragment
    fun Fragment.showInterstitialAd(onAdClosed: () -> Unit) {
        requireActivity().showInterstitialAd(onAdClosed)
    }

    fun Fragment.loadAds(adView: AdView) {
        requireActivity().loadAds(adView)
    }

    fun Fragment.loadInterstitialAd() {
        requireActivity().loadInterstitialAd()
    }

    fun Fragment.showBannerAd(adView: AdView) {
        requireActivity().showBannerAd(adView)
    }
    // endregion
}