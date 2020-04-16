package in.lubble.app.marketplace;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import in.lubble.app.leaderboard.LeaderboardSlideFrag;

public class SliderViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<SliderData> sliderData;
    private boolean showLeaderboard;

    public SliderViewPagerAdapter(FragmentManager fm, ArrayList<SliderData> questionData, boolean showLeaderboard) {
        super(fm);
        this.sliderData = questionData;
        this.showLeaderboard = showLeaderboard;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == sliderData.size() && showLeaderboard) {
            return LeaderboardSlideFrag.newInstance();
        } else {
            return SlidePageFrag.newInstance(sliderData.get(position));
        }
    }

    public void updateList(ArrayList<SliderData> questionData) {
        this.sliderData.clear();
        this.sliderData = questionData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return showLeaderboard ? sliderData.size() + 1 : sliderData.size();
    }
}