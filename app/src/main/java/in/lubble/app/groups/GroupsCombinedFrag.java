package in.lubble.app.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import in.lubble.app.R;
import in.lubble.app.explore.ExploreFrag;

import static in.lubble.app.utils.UiUtils.reduceDragSensitivity;

public class GroupsCombinedFrag extends Fragment {

    private static boolean isNewUser;

    public GroupsCombinedFrag() {
        // Required empty public constructor
    }

    public static GroupsCombinedFrag newInstance(boolean isNewUserInThisLubble) {
        GroupsCombinedFrag groupsCombinedFrag = new GroupsCombinedFrag();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isNewUserInThisLubble", isNewUserInThisLubble);
        groupsCombinedFrag.setArguments(bundle);
        return groupsCombinedFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_groups_combined, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout_groups);
        ViewPager2 viewPager = view.findViewById(R.id.tab_pager);

        isNewUser = requireArguments().getBoolean("isNewUserInThisLubble", false);

        MyTabPagerAdapter tabPager = new MyTabPagerAdapter(requireActivity());
        viewPager.setAdapter(tabPager);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
                        tab.setText(position == 0 ? "Chats" : "Explore Groups")
        ).attach();
        reduceDragSensitivity(viewPager);

        return view;
    }

    static class MyTabPagerAdapter extends FragmentStateAdapter {
        MyTabPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return GroupListFragment.newInstance(isNewUser);
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