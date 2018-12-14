package in.lubble.app.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.GlideApp;
import in.lubble.app.R;

import static android.widget.RelativeLayout.ALIGN_BOTTOM;

public class NewPollActiv extends AppCompatActivity {

    private static final String TAG = "NewPollActiv";

    private RelativeLayout pollChoiceRelLayout;
    private EditText choice2Et;
    private EditText choice3Et;
    private EditText choice4Et;
    private ImageView addChoiceIv;
    private RelativeLayout pollExpiryContainer;
    private TextView pollExpiryTv;
    private int choiceCount = 2;
    private int expiryDayCount = 1;

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewPollActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poll);

        ImageView closeIv = findViewById(R.id.iv_poll_close);
        Button sendPollBtn = findViewById(R.id.btn_send_poll);
        ImageView pollDpIv = findViewById(R.id.iv_poll_dp);
        EditText askQuesEt = findViewById(R.id.et_ask_question);

        pollChoiceRelLayout = findViewById(R.id.container_poll_choices);
        EditText choice1Et = findViewById(R.id.et_poll_choice_1);
        choice2Et = findViewById(R.id.et_poll_choice_2);
        choice3Et = findViewById(R.id.et_poll_choice_3);
        choice4Et = findViewById(R.id.et_poll_choice_4);

        addChoiceIv = findViewById(R.id.iv_poll_add);
        pollExpiryContainer = findViewById(R.id.container_poll_expiry);
        pollExpiryTv = findViewById(R.id.tv_poll_expiry);

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addChoiceIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choiceCount < 4) {
                    choiceCount++;
                    addPollChoice(choiceCount == 3 ? choice3Et : choice4Et);
                }
                if (choiceCount == 4) {
                    addChoiceIv.setVisibility(View.INVISIBLE);
                }
            }
        });

        pollExpiryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        GlideApp.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding).circleCrop().into(pollDpIv);

    }

    private void openDatePicker() {
        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(7);
        picker.setValue(expiryDayCount);

        FrameLayout layout = new FrameLayout(this);
        layout.addView(picker, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle("Number of days")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expiryDayCount = picker.getValue();
                        pollExpiryTv.setText(getResources().getQuantityString(R.plurals.day_count, expiryDayCount, picker.getValue()));
                        picker.setValue(expiryDayCount);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addPollChoice(EditText editText) {
        editText.setVisibility(View.VISIBLE);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addChoiceIv.getLayoutParams();
        layoutParams.addRule(ALIGN_BOTTOM, editText.getId());
        addChoiceIv.setLayoutParams(layoutParams);
    }

}
