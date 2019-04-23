package in.lubble.app.chat.books;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

public class PhoneBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "RetryQuizBottomSheet";

    private TextInputLayout phoneTil;
    private Button submitBtn;
    private ProgressBar progressBar;

    public static PhoneBottomSheet newInstance() {

        Bundle args = new Bundle();

        PhoneBottomSheet fragment = new PhoneBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.frag_phone_sheet, null);
        dialog.setContentView(view);

        Analytics.triggerScreenEvent(requireContext(), this.getClass());

        phoneTil = view.findViewById(R.id.til_phone);
        submitBtn = view.findViewById(R.id.btn_submit_phone);
        progressBar = view.findViewById(R.id.progressbar_phone);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(phoneTil.getEditText().getText()) && phoneTil.getEditText().getText().length() == 10) {
                    submitBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    getThisUserRef().child("phone").setValue(phoneTil.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                dismiss();
                            } else {
                                submitBtn.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Please enter correct phone number of 10 digits", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
