package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.ChoiceData;

import java.util.ArrayList;
import java.util.MissingFormatArgumentException;

import static android.widget.RelativeLayout.ALIGN_BOTTOM;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.models.ChatData.POLL;

public class NewPollActiv extends AppCompatActivity {

    private static final String TAG = "NewPollActiv";

    private static final String EXTRA_GROUP_ID = "in.lubble.app.newpoll.GROUP_ID";

    private EditText askQuesEt;
    private RelativeLayout pollChoiceRelLayout;
    private EditText choice1Et;
    private EditText choice2Et;
    private EditText choice3Et;
    private EditText choice4Et;
    private ImageView addChoiceIv;
    private RelativeLayout pollExpiryContainer;
    private TextView pollExpiryTv;
    private int choiceCount = 2;
    private int expiryDayCount = 1;
    private String groupId;

    public static void open(Context context, String grouId) {
        final Intent intent = new Intent(context, NewPollActiv.class);
        intent.putExtra(EXTRA_GROUP_ID, grouId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poll);

        if (getIntent().hasExtra(EXTRA_GROUP_ID)) {
            groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        } else {
            throw new MissingFormatArgumentException("no GROUP ID passed while opening NewPollActiv");
        }

        ImageView closeIv = findViewById(R.id.iv_poll_close);
        LinearLayout sendPollContainer = findViewById(R.id.container_send_poll);
        ImageView pollDpIv = findViewById(R.id.iv_poll_dp);
        askQuesEt = findViewById(R.id.et_ask_question);

        pollChoiceRelLayout = findViewById(R.id.container_poll_choices);
        choice1Et = findViewById(R.id.et_poll_choice_1);
        choice2Et = findViewById(R.id.et_poll_choice_2);
        choice3Et = findViewById(R.id.et_poll_choice_3);
        choice4Et = findViewById(R.id.et_poll_choice_4);

        addChoiceIv = findViewById(R.id.iv_poll_add);
        pollExpiryContainer = findViewById(R.id.container_poll_expiry);
        pollExpiryContainer.setVisibility(View.GONE);
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
                //openDatePicker();
            }
        });

        GlideApp.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.ic_account_circle_black_no_padding).circleCrop().into(pollDpIv);

        sendPollContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(askQuesEt.getText()) && !TextUtils.isEmpty(choice1Et.getText()) && !TextUtils.isEmpty(choice2Et.getText())) {

                    final ChatData chatData = new ChatData();
                    chatData.setAuthorUid(FirebaseAuth.getInstance().getUid());
                    chatData.setIsDm(false);
                    chatData.setMessage(getString(R.string.poll_msg_body));
                    chatData.setPollQues(askQuesEt.getText().toString());
                    chatData.setCreatedTimestamp(System.currentTimeMillis());
                    chatData.setServerTimestamp(ServerValue.TIMESTAMP);
                    chatData.setType(POLL);
                    chatData.setChoiceList(getChoices());

                    getMessagesRef().child(groupId).push().setValue(chatData);
                    finish();
                }
            }
        });

    }

    private ArrayList<ChoiceData> getChoices() {
        final ArrayList<ChoiceData> list = new ArrayList<>();
        ChoiceData choiceData = new ChoiceData();
        choiceData.setTitle(choice1Et.getText().toString());
        list.add(choiceData);
        choiceData = new ChoiceData();
        choiceData.setTitle(choice2Et.getText().toString());
        list.add(choiceData);
        if (!TextUtils.isEmpty(choice3Et.getText())) {
            choiceData = new ChoiceData();
            choiceData.setTitle(choice3Et.getText().toString());
            list.add(choiceData);
        }
        if (!TextUtils.isEmpty(choice4Et.getText())) {
            choiceData = new ChoiceData();
            choiceData.setTitle(choice4Et.getText().toString());
            list.add(choiceData);
        }
        return list;
    }

    private void openDatePicker() {
        /*final NumberPicker picker = new NumberPicker(this);
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
                .show();*/
    }

    private void addPollChoice(EditText editText) {
        editText.setVisibility(View.VISIBLE);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addChoiceIv.getLayoutParams();
        layoutParams.addRule(ALIGN_BOTTOM, editText.getId());
        addChoiceIv.setLayoutParams(layoutParams);
    }

}
