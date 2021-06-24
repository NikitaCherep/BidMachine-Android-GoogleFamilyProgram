package io.bidmachine.googlefamilyprogram.presentation.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import io.bidmachine.BidMachine
import io.bidmachine.banner.BannerListener
import io.bidmachine.banner.BannerRequest
import io.bidmachine.banner.BannerSize
import io.bidmachine.banner.BannerView
import io.bidmachine.googlefamilyprogram.R
import io.bidmachine.googlefamilyprogram.databinding.ActivityMainBinding
import io.bidmachine.interstitial.InterstitialAd
import io.bidmachine.interstitial.InterstitialListener
import io.bidmachine.interstitial.InterstitialRequest
import io.bidmachine.models.AuctionResult
import io.bidmachine.nativead.NativeAd
import io.bidmachine.nativead.NativeListener
import io.bidmachine.nativead.NativeRequest
import io.bidmachine.nativead.view.NativeAdContentLayout
import io.bidmachine.rewarded.RewardedAd
import io.bidmachine.rewarded.RewardedListener
import io.bidmachine.rewarded.RewardedRequest
import io.bidmachine.utils.BMError
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val BID_MACHINE_SELLER_ID = "1"

        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    private var bannerRequest: BannerRequest? = null
    private var bannerView: BannerView? = null

    private var interstitialRequest: InterstitialRequest? = null
    private var interstitialAd: InterstitialAd? = null

    private var rewardedRequest: RewardedRequest? = null
    private var rewardedAd: RewardedAd? = null

    private var nativeRequest: NativeRequest? = null
    private var nativeAd: NativeAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        binding.cbTestMode.setOnCheckedChangeListener { _, isChecked ->
            BidMachine.setTestMode(isChecked)
        }
        binding.cbLoggingEnable.setOnCheckedChangeListener { _, isChecked ->
            BidMachine.setLoggingEnabled(isChecked)
        }
        binding.cbCoppa.setOnCheckedChangeListener { _, isChecked ->
            BidMachine.setCoppa(isChecked)
        }
        binding.cbGdpr.setOnCheckedChangeListener { _, isChecked ->
            BidMachine.setSubjectToGDPR(isChecked)
        }
        binding.cbConsent.setOnCheckedChangeListener { _, isChecked ->
            BidMachine.setConsentConfig(isChecked, null)
        }
        binding.etUSPrivacyString.addTextChangedListener {
            BidMachine.setUSPrivacyString(it.toString())
        }
        binding.bInitialize.setOnClickListener {
            initialize()
            enableLoadButton()
        }
        binding.bLoadBanner.setOnClickListener {
            loadBanner()
        }
        binding.bShowBanner.setOnClickListener {
            showBannerAd()
        }
        binding.bLoadInterstitial.setOnClickListener {
            loadInterstitial()
        }
        binding.bShowInterstitial.setOnClickListener {
            showInterstitialAd()
        }
        binding.bLoadRewarded.setOnClickListener {
            loadRewarded()
        }
        binding.bShowRewarded.setOnClickListener {
            showRewardedAd()
        }
        binding.bLoadNative.setOnClickListener {
            loadNative()
        }
        binding.bShowNative.setOnClickListener {
            showNativeAd()
        }
    }

    private fun initialize() {
        logMessage("initialize")

        BidMachine.setTestMode(binding.cbTestMode.isChecked)
        BidMachine.setLoggingEnabled(binding.cbLoggingEnable.isChecked)
        BidMachine.setCoppa(binding.cbCoppa.isChecked)
        BidMachine.setSubjectToGDPR(binding.cbGdpr.isChecked)
        BidMachine.setConsentConfig(binding.cbConsent.isChecked, null)
        BidMachine.setUSPrivacyString(binding.etUSPrivacyString.text.toString())

        BidMachine.initialize(this, BID_MACHINE_SELLER_ID)
    }

    private fun enableLoadButton() {
        binding.bLoadBanner.isEnabled = true
        binding.bLoadInterstitial.isEnabled = true
        binding.bLoadRewarded.isEnabled = true
        binding.bLoadNative.isEnabled = true
    }

    private fun addAdView(view: View) {
        binding.adContainer.removeAllViews()
        binding.adContainer.addView(view)
    }

    private fun loadBanner() {
        binding.bShowBanner.isEnabled = false

        destroyBanner()

        bannerRequest = BannerRequest.Builder()
            .setSize(BannerSize.Size_320x50)
            .setListener(object : BannerRequest.AdRequestListener {
                override fun onRequestSuccess(bannerRequest: BannerRequest,
                                              auctionResult: AuctionResult) {
                    runOnUiThread {
                        loadBannerAd(bannerRequest)
                    }
                }

                override fun onRequestFailed(bannerRequest: BannerRequest,
                                             bmError: BMError) {
                    runOnUiThread {
                        logMessage("BannerRequest", "onRequestFailed", true)
                    }
                }

                override fun onRequestExpired(bannerRequest: BannerRequest) {
                    runOnUiThread {
                        logMessage("BannerRequest", "onRequestExpired", true)
                    }
                }
            })
            .build().apply {
                request(this@MainActivity)
            }

        logMessage("loadBanner")
    }

    private fun loadBannerAd(bannerRequest: BannerRequest) {
        logMessage("loadBannerAd")

        bannerView = BannerView(this).apply {
            setListener(BannerAdListener())
            load(bannerRequest)
        }
    }

    private fun showBannerAd() {
        logMessage("showBannerAd")

        binding.bShowBanner.isEnabled = false

        bannerView?.takeIf {
            it.canShow()
        }?.apply {
            addAdView(this)
        } ?: logMessage("show error - ad object not loaded")
    }

    private fun destroyBanner() {
        logMessage("destroyBanner")

        binding.adContainer.removeAllViews()

        bannerView?.apply {
            setListener(null)
            destroy()
        }
        bannerRequest?.destroy()
    }

    private fun loadInterstitial() {
        binding.bShowInterstitial.isEnabled = false

        destroyInterstitial()

        interstitialRequest = InterstitialRequest.Builder()
            .setListener(object : InterstitialRequest.AdRequestListener {
                override fun onRequestSuccess(interstitialRequest: InterstitialRequest,
                                              auctionResult: AuctionResult) {
                    runOnUiThread {
                        loadInterstitialAd(interstitialRequest)
                    }
                }

                override fun onRequestFailed(interstitialRequest: InterstitialRequest,
                                             bmError: BMError) {
                    runOnUiThread {
                        logMessage("InterstitialRequest", "onRequestFailed", true)
                    }
                }

                override fun onRequestExpired(interstitialRequest: InterstitialRequest) {
                    runOnUiThread {
                        logMessage("InterstitialRequest", "onRequestExpired", true)
                    }
                }
            })
            .build().apply {
                request(this@MainActivity)
            }

        logMessage("loadInterstitial")
    }

    private fun loadInterstitialAd(interstitialRequest: InterstitialRequest) {
        logMessage("loadInterstitialAd")

        interstitialAd = InterstitialAd(this).apply {
            setListener(InterstitialAdListener())
            load(interstitialRequest)
        }
    }

    private fun showInterstitialAd() {
        logMessage("showInterstitialAd")

        binding.bShowInterstitial.isEnabled = false

        interstitialAd?.takeIf {
            it.canShow()
        }?.show() ?: logMessage("show error - ad object not loaded")
    }

    private fun destroyInterstitial() {
        logMessage("destroyInterstitial")

        interstitialAd?.apply {
            setListener(null)
            destroy()
        }
        interstitialRequest?.destroy()
    }

    private fun loadRewarded() {
        binding.bShowRewarded.isEnabled = false

        destroyRewarded()

        rewardedRequest = RewardedRequest.Builder()
            .setListener(object : RewardedRequest.AdRequestListener {
                override fun onRequestSuccess(rewardedRequest: RewardedRequest,
                                              auctionResult: AuctionResult) {
                    runOnUiThread {
                        loadRewardedAd(rewardedRequest)
                    }
                }

                override fun onRequestFailed(rewardedRequest: RewardedRequest,
                                             bmError: BMError) {
                    runOnUiThread {
                        logMessage("RewardedRequest", "onRequestFailed", true)
                    }
                }

                override fun onRequestExpired(rewardedRequest: RewardedRequest) {
                    runOnUiThread {
                        logMessage("RewardedRequest", "onRequestExpired", true)
                    }
                }
            })
            .build().apply {
                request(this@MainActivity)
            }

        logMessage("loadRewarded")
    }

    private fun loadRewardedAd(rewardedRequest: RewardedRequest) {
        logMessage("loadRewardedAd")

        rewardedAd = RewardedAd(this).apply {
            setListener(RewardedAdListener())
            load(rewardedRequest)
        }
    }

    private fun showRewardedAd() {
        logMessage("showRewardedAd")

        binding.bShowRewarded.isEnabled = false

        rewardedAd?.takeIf {
            it.canShow()
        }?.show() ?: logMessage("show error - ad object not loaded")
    }

    private fun destroyRewarded() {
        logMessage("destroyRewarded")

        rewardedAd?.apply {
            setListener(null)
            destroy()
        }
        rewardedRequest?.destroy()
    }

    private fun loadNative() {
        binding.bShowNative.isEnabled = false

        destroyNative()

        nativeRequest = NativeRequest.Builder()
            .setListener(object : NativeRequest.AdRequestListener {
                override fun onRequestSuccess(nativeRequest: NativeRequest,
                                              auctionResult: AuctionResult) {
                    runOnUiThread {
                        loadNativeAd(nativeRequest)
                    }
                }

                override fun onRequestFailed(nativeRequest: NativeRequest,
                                             bmError: BMError) {
                    runOnUiThread {
                        logMessage("NativeRequest", "onRequestFailed", true)
                    }
                }

                override fun onRequestExpired(nativeRequest: NativeRequest) {
                    runOnUiThread {
                        logMessage("NativeRequest", "onRequestExpired", true)
                    }
                }
            })
            .build().apply {
                request(this@MainActivity)
            }

        logMessage("loadNative")
    }

    private fun loadNativeAd(nativeRequest: NativeRequest) {
        logMessage("loadNativeAd")

        nativeAd = NativeAd(this).apply {
            setListener(NativeAdListener())
            load(nativeRequest)
        }
    }

    private fun showNativeAd() {
        logMessage("showNativeAd")

        binding.bShowNative.isEnabled = false

        nativeAd?.takeIf {
            it.canShow()
        }?.let { nativeAd ->
            (LayoutInflater
                .from(this@MainActivity)
                .inflate(R.layout.native_ad,
                         binding.adContainer,
                         false) as NativeAdContentLayout).apply {
                bind(nativeAd)
                registerViewForInteraction(nativeAd)

                addAdView(this)
            }
        } ?: logMessage("show error - ad object not loaded")
    }

    private fun destroyNative() {
        logMessage("destroyNative")

        binding.adContainer.removeAllViews()
        nativeAd?.apply {
            setListener(null)
            unregisterView()
            destroy()
        }
        nativeRequest?.destroy()
    }

    private fun logMessage(message: String, isShowToast: Boolean = false) {
        Log.d(TAG, message)
        if (isShowToast) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun logMessage(subTag: String, message: String, isShowToast: Boolean = false) {
        String.format("%s - %s", subTag, message).also {
            logMessage(it, isShowToast)
        }
    }

    private fun logError(subTag: String, message: String, bmError: BMError) {
        String.format("%s - %s", subTag, message).also {
            Log.d(TAG, String.format("%s: %s", it, bmError.toString()))
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }


    private inner class BannerAdListener : BannerListener {

        override fun onAdLoaded(bannerView: BannerView) {
            logMessage(javaClass.simpleName, "onAdLoaded", true)

            binding.bShowBanner.isEnabled = true
        }

        override fun onAdLoadFailed(bannerView: BannerView, bmError: BMError) {
            logError(javaClass.simpleName, "onAdLoadFailed", bmError)
        }

        override fun onAdShown(bannerView: BannerView) {
            logMessage(javaClass.simpleName, "onAdShown")
        }

        override fun onAdImpression(bannerView: BannerView) {
            logMessage(javaClass.simpleName, "onAdImpression")
        }

        override fun onAdClicked(bannerView: BannerView) {
            logMessage(javaClass.simpleName, "onAdClicked")
        }

        override fun onAdExpired(bannerView: BannerView) {
            logMessage(javaClass.simpleName, "onAdExpired")
        }

    }

    private inner class InterstitialAdListener : InterstitialListener {

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            logMessage(javaClass.simpleName, "onAdLoaded", true)

            binding.bShowInterstitial.isEnabled = true
        }

        override fun onAdLoadFailed(interstitialAd: InterstitialAd, bmError: BMError) {
            logError(javaClass.simpleName, "onAdLoadFailed", bmError)
        }

        override fun onAdShown(interstitialAd: InterstitialAd) {
            logMessage(javaClass.simpleName, "onAdShown")
        }

        override fun onAdShowFailed(interstitialAd: InterstitialAd, bmError: BMError) {
            logError(javaClass.simpleName, "onAdShowFailed", bmError)
        }

        override fun onAdImpression(interstitialAd: InterstitialAd) {
            logMessage(javaClass.simpleName, "onAdImpression")
        }

        override fun onAdClicked(interstitialAd: InterstitialAd) {
            logMessage(javaClass.simpleName, "onAdClicked")
        }

        override fun onAdClosed(interstitialAd: InterstitialAd, finished: Boolean) {
            logMessage(javaClass.simpleName, "onAdClosed")
        }

        override fun onAdExpired(interstitialAd: InterstitialAd) {
            logMessage(javaClass.simpleName, "onAdExpired")
        }

    }

    private inner class RewardedAdListener : RewardedListener {

        override fun onAdLoaded(rewardedAd: RewardedAd) {
            logMessage(javaClass.simpleName, "onAdLoaded", true)

            binding.bShowRewarded.isEnabled = true
        }

        override fun onAdLoadFailed(rewardedAd: RewardedAd, bmError: BMError) {
            logError(javaClass.simpleName, "onAdLoadFailed", bmError)
        }

        override fun onAdShown(rewardedAd: RewardedAd) {
            logMessage(javaClass.simpleName, "onAdShown")
        }

        override fun onAdShowFailed(rewardedAd: RewardedAd, bmError: BMError) {
            logError(javaClass.simpleName, "onAdShowFailed", bmError)
        }

        override fun onAdImpression(rewardedAd: RewardedAd) {
            logMessage(javaClass.simpleName, "onAdImpression")
        }

        override fun onAdClicked(rewardedAd: RewardedAd) {
            logMessage(javaClass.simpleName, "onAdClicked")
        }

        override fun onAdClosed(rewardedAd: RewardedAd, finished: Boolean) {
            logMessage(javaClass.simpleName, "onAdClosed")
        }

        override fun onAdExpired(rewardedAd: RewardedAd) {
            logMessage(javaClass.simpleName, "onAdExpired")
        }

        override fun onAdRewarded(rewardedAd: RewardedAd) {
            logMessage(javaClass.simpleName, "onAdRewarded")
        }

    }

    private inner class NativeAdListener : NativeListener {

        override fun onAdLoaded(nativeAd: NativeAd) {
            logMessage(javaClass.simpleName, "onAdLoaded", true)

            binding.bShowNative.isEnabled = true
        }

        override fun onAdLoadFailed(nativeAd: NativeAd, bmError: BMError) {
            logError(javaClass.simpleName, "onAdLoadFailed", bmError)
        }

        override fun onAdShown(nativeAd: NativeAd) {
            logMessage(javaClass.simpleName, "onAdShown")
        }

        override fun onAdImpression(nativeAd: NativeAd) {
            logMessage(javaClass.simpleName, "onAdImpression")
        }

        override fun onAdClicked(nativeAd: NativeAd) {
            logMessage(javaClass.simpleName, "onAdClicked")
        }

        override fun onAdExpired(nativeAd: NativeAd) {
            logMessage(javaClass.simpleName, "onAdExpired")
        }

    }

}