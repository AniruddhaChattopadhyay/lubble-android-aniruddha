package in.lubble.app.marketplace;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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