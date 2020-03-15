package in.lubble.app.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;

import static in.lubble.app.firebase.RealtimeDbHelper.getDmMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;

public class ReportMsgBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "ReportMsgBottomSheet";

    private LinearLayout reportContainer, doneContainer;
    private RadioGroup reportRadioGroup;
    private MaterialButton reportBtn;
    private TextView okayTv;
    private String reporterUid, groupId, chatId;
    private boolean isDm;

    public static ReportMsgBottomSheet newInstance(String reporterUid, String groupId, String chatId, boolean isDm) {

        Bundle args = new Bundle();
        args.putString("reporterUid", reporterUid);
        args.putString("groupId", groupId);
        args.putString("chatId", chatId);
        args.putBoolean("isDm", isDm);
        ReportMsgBottomSheet fragment = new ReportMsgBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
        Analytics.triggerScreenEvent(getContext(), this.getClass());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.frag_report_msg_bottom_sheet, container, false);

        reportContainer = view.findViewById(R.id.container_report);
        doneContainer = view.findViewById(R.id.container_done);
        reportRadioGroup = view.findViewById(R.id.rg_report_msg);
        reportBtn = view.findViewById(R.id.btn_report);
        okayTv = view.findViewById(R.id.tv_okay);
        reportBtn.setEnabled(false);
        reportContainer.setVisibility(View.VISIBLE);
        doneContainer.setVisibility(View.GONE);

        if (getArguments() != null) {
            reporterUid = getArguments().getString("reporterUid");
            groupId = getArguments().getString("groupId");
            chatId = getArguments().getString("chatId");
            isDm = getArguments().getBoolean("isDm");
        } else {
            dismiss();
        }

        final ArrayList<String> reasonList = new ArrayList<>();
        reasonList.add("I just don't like it");
        reasonList.add("Spam");
        reasonList.add("Nudity");
        populateRadioGroup(reasonList);

        reportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                reportBtn.setEnabled(checkedId != -1);
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedId = reportRadioGroup.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Toast.makeText(requireContext(), "Please select a reason to help us decide an appropriate action", Toast.LENGTH_SHORT).show();
                } else {
                    reportBtn.setEnabled(false);
                    DatabaseReference msgRef;
                    if (isDm) {
                        msgRef = getDmMessagesRef().child(groupId).child(chatId);
                    } else {
                        msgRef = getMessagesRef().child(groupId).child(chatId);
                    }
                    final HashMap<String, Object> reporterMap = new HashMap<>();
                    reporterMap.put("time", System.currentTimeMillis());
                    reporterMap.put("reasonId", checkedId);

                    Map<String, Object> childUpdates = new HashMap<>();
                    final DatabaseReference reporterRef = msgRef.child("reporters").child(reporterUid);
                    childUpdates.put(reporterRef.toString().substring(reporterRef.getRoot().toString().length()), reporterMap);
                    childUpdates.put("chat_reports/" + chatId, ServerValue.TIMESTAMP);
                    FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                reportContainer.setVisibility(View.GONE);
                                doneContainer.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getContext(), "Something went wrong. Plz try again or contact support", Toast.LENGTH_SHORT).show();
                                reportBtn.setEnabled(true);
                            }
                        }
                    });
                    final Bundle bundle = new Bundle();
                    bundle.putBoolean("isDm", isDm);
                    bundle.putString("reporterUid", reporterUid);
                    bundle.putString("groupId", groupId);
                    bundle.putString("chatId", chatId);
                    Analytics.triggerEvent(AnalyticsEvents.REPORT_MSG, bundle, getContext());
                }
            }
        });

        okayTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    private void populateRadioGroup(ArrayList<String> reasonList) {
        for (int i = 0; i < reasonList.size(); i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setId(i);
            rb.setText(reasonList.get(i));
            reportRadioGroup.addView(rb);
        }
    }

}
