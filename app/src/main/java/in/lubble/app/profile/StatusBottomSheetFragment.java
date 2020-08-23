package in.lubble.app.profile;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatMoreFragment;
import in.lubble.app.firebase.RealtimeDbHelper;

public class StatusBottomSheetFragment extends BottomSheetDialogFragment {

    private List<String> statusList = new ArrayList<>();
    private ShimmerRecyclerView recyclerView;
    private StatusBottomSheetAdapter mAdapter;
    private EditText customEt;
    private MaterialButton customSetBtn;
    private MaterialButton setStatus;
    private TextInputLayout customStatusLayout;
    private int selectedPos = -1;
    private View view_snackbar;
    @Nullable
    private ChatMoreFragment.FlairUpdateListener flairUpdateListener;

    public StatusBottomSheetFragment(View v) {
        view_snackbar = v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.bottom_sheet_status, container, false);
        recyclerView = rootview.findViewById(R.id.recycler_view);
        customEt = rootview.findViewById(R.id.custom_et);
        customSetBtn = rootview.findViewById(R.id.custom_btn);
        setStatus = rootview.findViewById(R.id.set_status_btn);
        mAdapter = new StatusBottomSheetAdapter(statusList);
        customStatusLayout = rootview.findViewById(R.id.custom_status_layout);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.showShimmerAdapter();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Movie movie = statusList.get(position);
                selectedPos = position;
                if (statusList.get(position).equalsIgnoreCase("Custom")) {
                    customStatusLayout.setVisibility(View.VISIBLE);
                    customEt.setFilters(new InputFilter[]{EMOJI_FILTER});
                    customSetBtn.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    setStatus.setVisibility(View.GONE);
                    customSetBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String statusText = customEt.getText().toString();
                            if (statusText.toLowerCase().contains("admin") || statusText.toLowerCase().contains("moderator")) {
                                Toast.makeText(getContext(), "You can not choose " + statusText + " without administrative privileges", Toast.LENGTH_SHORT).show();
                                customEt.setText("");
                                Analytics.triggerEvent(AnalyticsEvents.SET_STATUS_FOR_CUSTOM_STATUS_CLICKED, getContext());
                            } else {
                                RealtimeDbHelper.getThisUserRef().child("info").child("badge").setValue(statusText);
                                Snackbar snackbar = Snackbar
                                        .make(view_snackbar, statusText + " is selected as badge!", Snackbar.LENGTH_LONG);
                                snackbar.show();
                                if (flairUpdateListener != null) {
                                    flairUpdateListener.onFlairUpdated();
                                }
                                dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        setStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerEvent(AnalyticsEvents.SET_STATUS_CLICKED, getContext());
                if (selectedPos == -1) {
                    Toast.makeText(getContext(), "Please choose a badge first", Toast.LENGTH_SHORT).show();
                } else {
                    RealtimeDbHelper.getThisUserRef().child("info").child("badge").setValue(statusList.get(selectedPos));
                    Snackbar snackbar = Snackbar
                            .make(view_snackbar, statusList.get(selectedPos) + " is selected as badge!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    if (flairUpdateListener != null) {
                        flairUpdateListener.onFlairUpdated();
                    }
                    dismiss();
                }
            }
        });

        getBlockList();
        return rootview;
    }

    private void getBlockList() {
        RealtimeDbHelper.getLubbleBlocksRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> arr = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    statusList.add(dataSnapshot.getKey());
                }
                statusList.add("Custom");
                recyclerView.hideShimmerAdapter();
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ChatMoreFragment.FlairUpdateListener) {
            try {
                flairUpdateListener = (ChatMoreFragment.FlairUpdateListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement FlairUpdateListener");
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}