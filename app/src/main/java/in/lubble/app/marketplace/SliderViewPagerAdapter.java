package in.lubble.app.marketplace;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class SliderViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<SliderData> sliderData;

    public SliderViewPagerAdapter(FragmentManager fm, ArrayList<SliderData> questionData) {
        super(fm);
        this.sliderData = questionData;
    }

    @Override
    public Fragment getItem(int position) {
        return SlidePageFrag.newInstance(sliderData.get(position));
    }

    public void updateList(ArrayList<SliderData> questionData) {
        this.sliderData.clear();
        this.sliderData = questionData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return sliderData.size();
    }
}