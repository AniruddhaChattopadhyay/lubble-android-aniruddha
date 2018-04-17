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
        sliderPage1.setTitle("The private social network for you & your neighbours");
        sliderPage1.setDescription("Lubble lets you connect with verified people living in your locality");
        sliderPage1.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));

        final SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle("Connect with verified neighbours");
        sliderPage2.setDescription("Form interest groups, plan stuff together with your neighbours in your locality");
        sliderPage2.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));

        final SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle("Stay informed about your locality");
        sliderPage3.setDescription("See announcements, alerts, safety tips for your locality.");
        sliderPage3.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));

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