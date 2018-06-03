package in.lubble.app.auth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;

/**
 * Created by ishaan on 17/4/18.
 */

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        final SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle(getString(R.string.intro_page1_title));
        sliderPage1.setDescription(getString(R.string.intro_page1_subtitle));
        sliderPage1.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));
        sliderPage1.setImageDrawable(R.drawable.slide1);

        final SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getString(R.string.intro_page2_title));
        sliderPage2.setDescription(getString(R.string.intro_page2_subtitle));
        sliderPage2.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));
        sliderPage2.setImageDrawable(R.drawable.slide2);

        final SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle(getString(R.string.intro_page3_title));
        sliderPage3.setDescription(getString(R.string.intro_page3_subtitle));
        sliderPage3.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));
        sliderPage3.setImageDrawable(R.drawable.slide3);

        addSlide(AppIntroFragment.newInstance(sliderPage1));
        addSlide(AppIntroFragment.newInstance(sliderPage2));
        addSlide(AppIntroFragment.newInstance(sliderPage3));

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        setSeparatorColor(ContextCompat.getColor(this, R.color.white));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        LubbleSharedPrefs.getInstance().setIsAppIntroShown(true);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}