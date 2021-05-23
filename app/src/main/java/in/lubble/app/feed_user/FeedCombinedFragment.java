package in.lubble.app.feed_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import in.lubble.app.R;
import in.lubble.app.feed_groups.FeedGroupsFrag;

public class FeedCombinedFragment extends Fragment {

    public FeedCombinedFragment() {
        // Required empty public constructor
    }

    public static FeedCombinedFragment newInstance() {
        return new FeedCombinedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_feed_combined, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout_feed);
        ViewPager viewPager = view.findViewById(R.id.tab_pager);

        MyTabPagerAdapter tabPager = new MyTabPagerAdapter(getFragmentManager());
        viewPager.setAdapter(tabPager);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    static class MyTabPagerAdapter extends FragmentStatePagerAdapter {
        MyTabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2; // One for each tab, 3 in our example.
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FeedFrag();
                case 1:
                    return new FeedGroupsFrag();
                default:
                    throw new IllegalArgumentException();
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "Nearby Feed" : "Groups";
        }
    }

}