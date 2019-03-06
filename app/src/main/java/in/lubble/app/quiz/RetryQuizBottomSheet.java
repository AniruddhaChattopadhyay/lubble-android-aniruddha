package in.lubble.app.quiz;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import in.lubble.app.R;

public class RetryQuizBottomSheet extends BottomSheetDialogFragment {

    public static RetryQuizBottomSheet newInstance() {

        Bundle args = new Bundle();

        RetryQuizBottomSheet fragment = new RetryQuizBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.frag_retry_quiz, null);
        dialog.setContentView(view);


    }
}
