package in.lubble.app.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import in.lubble.app.R;
import in.lubble.app.explore.ExploreFrag;

import static in.lubble.app.utils.UiUtils.reduceDragSensitivity;

public class GroupsCombinedFrag extends Fragment {

    public GroupsCombinedFrag() {
        // Required empty public constructor
    }

    public static GroupsCombinedFrag newInstance() {
        return new GroupsCombinedFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_groups_combined, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout_groups);
        ViewPager2 viewPager = view.findViewById(R.id.tab_pager);

        MyTabPagerAdapter tabPager = new MyTabPagerAdapter(this);
        viewPager.setAdapter(tabPager);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
                        tab.setText(position == 0 ? "Chats" : "Explore Groups")
        ).attach();
        reduceDragSensitivity(viewPager);

        return view;
    }

    static class MyTabPagerAdapter extends FragmentStateAdapter {
        MyTabPagerAdapter(Fragment frag) {
            super(frag);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return GroupListFragment.newInstance();
                case 1:
                    return new ExploreFrag();
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}