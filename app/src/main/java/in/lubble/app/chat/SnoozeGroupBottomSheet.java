package in.lubble.app.chat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.notifications.SnoozedGroupsSharedPrefs;
import in.lubble.app.utils.CompleteListener;

import static in.lubble.app.notifications.SnoozedGroupsSharedPrefs.DISABLED_NOTIFS_TS;

public class SnoozeGroupBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = BuildConfig.APPLICATION_ID + "ARG_GROUP_ID";
    private static final String ARG_INTERFACE = BuildConfig.APPLICATION_ID + "ARG_INTERFACE";
    private static final String ARG_SNOOZE_SRC = BuildConfig.APPLICATION_ID + "ARG_SNOOZE_SRC";
    @Nullable
    private CompleteListener completeListener;
    private String analyticsSource;

    public static SnoozeGroupBottomSheet newInstance(String groupId, String src, CompleteListener completeListener) {
        final SnoozeGroupBottomSheet fragment = new SnoozeGroupBottomSheet();
        final Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putSerializable(ARG_INTERFACE, completeListener);
        args.putString(ARG_SNOOZE_SRC, src);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_snooze_group, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = view.findViewById(R.id.rv_snooze_list);
        String groupId = getArguments().getString(ARG_GROUP_ID);
        this.completeListener = (CompleteListener) getArguments().getSerializable(ARG_INTERFACE);
        analyticsSource = getArguments().getString(ARG_SNOOZE_SRC);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SnoozePeriodAdapter(groupId));

    }

    private class SnoozePeriodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView text;
        final String groupId;

        SnoozePeriodViewHolder(View view, String groupId) {
            super(view);
            this.groupId = groupId;
            text = itemView.findViewById(R.id.text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            long snoozeMillis = 0L;
            switch (getAdapterPosition()) {
                case 0:
                    snoozeMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(4);
                    break;
                case 1:
                    snoozeMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8);
                    break;
                case 2:
                    snoozeMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(48);
                    break;
                case 3:
                    snoozeMillis = DISABLED_NOTIFS_TS;
                    break;
            }
            SnoozedGroupsSharedPrefs.getInstance().getPreferences().edit().putLong(groupId, snoozeMillis).apply();
            Toast.makeText(LubbleApp.getAppContext(), "Snoozed", Toast.LENGTH_SHORT).show();
            if (completeListener != null) {
                completeListener.onComplete(true);
            }

            Bundle bundle = new Bundle();
            bundle.putString("src", analyticsSource);
            bundle.putString("group_id", groupId);
            Analytics.triggerEvent(AnalyticsEvents.GROUP_SNOOZED, bundle, getContext());

            dismiss();
        }
    }

    private class SnoozePeriodAdapter extends RecyclerView.Adapter<SnoozePeriodViewHolder> {

        private final ArrayList<String> snoozePeriodList = new ArrayList<>();
        private String groupId;

        SnoozePeriodAdapter(String groupId) {
            snoozePeriodList.add("Snooze for 4 hours");
            snoozePeriodList.add("Snooze for 8 hours");
            snoozePeriodList.add("Snooze for 2 days");
            snoozePeriodList.add("Disable");
            this.groupId = groupId;
        }

        @NonNull
        @Override
        public SnoozePeriodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SnoozePeriodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_sheet_list_snooze_group, parent, false), groupId);
        }

        @Override
        public void onBindViewHolder(SnoozePeriodViewHolder holder, int position) {
            holder.text.setText(snoozePeriodList.get(position));
        }

        @Override
        public int getItemCount() {
            return snoozePeriodList.size();
        }

    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        completeListener.onComplete(false);
    }
}
