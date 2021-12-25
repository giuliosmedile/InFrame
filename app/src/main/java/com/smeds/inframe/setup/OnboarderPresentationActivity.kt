package com.smeds.inframe.setup

import android.content.Intent
import android.os.Bundle
import com.chyrta.onboarder.OnboarderActivity
import com.chyrta.onboarder.OnboarderPage
import com.smeds.inframe.R

class OnboarderPresentationActivity : OnboarderActivity() {

    val onboarderPages = mutableListOf<OnboarderPage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onboarding1 = R.raw.onboarding_1
        val page1 = OnboarderPage(
            R.string.onboardingTitle1,
            R.string.onboardingSub1,
            onboarding1
        )
        page1.setBackgroundColor(R.color.colorPrimaryDark)
        onboarderPages.add(
            page1
        )

        val onboarding2 = R.raw.onboarding_2
        val page2 = OnboarderPage(
            R.string.onboardingTitle2,
            R.string.onboardingSub2,
            onboarding2
        )
        page2.setBackgroundColor(R.color.colorPrimaryDark)
        onboarderPages.add(
            page2
        )

        val onboarding3 = R.raw.onboarding_3
        val page3 = OnboarderPage(
            R.string.onboardingTitle3,
            R.string.onboardingSub3,
            onboarding3
        )
        page3.setBackgroundColor(R.color.colorPrimaryDark)
        onboarderPages.add(
            page3
        )

        val onboarding4 = R.raw.onboarding_1
        val page4 = OnboarderPage(
            R.string.onboardingTitle4,
            R.string.onboardingSub4,
            onboarding4
        )
        page4.setBackgroundColor(R.color.colorPrimaryDark)
        onboarderPages.add(
            page4
        )

        setFinishButtonTitle(R.string.beginSetup)
        setActiveIndicatorColor(R.color.colorSecondaryDark)
        setInactiveIndicatorColor(R.color.purple_500)
        setOnboardPagesReady(onboarderPages)
    }

    override fun onSkipButtonPressed() {
        onFinishButtonPressed()
    }

    override fun onFinishButtonPressed() {
        val intent : Intent = Intent(this, SetupActivity::class.java)
        startActivity(intent)
    }
}
