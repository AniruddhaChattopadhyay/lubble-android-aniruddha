package in.lubble.app.feed_user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import in.lubble.app.R;
import in.lubble.app.services.FeedServices;
import io.getstream.core.exceptions.StreamException;

public class AddPostForFeed extends AppCompatActivity {

    private Button postSubmitBtn;
    private TextInputLayout postText;
    private EditText groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post_for_feed);
        postSubmitBtn = findViewById(R.id.post_btn);
        postText = findViewById(R.id.post_edt_txt);
        groupName = findViewById(R.id.group_name_add_post);

        postSubmitBtn.setOnClickListener(v->{
            String text = postText.getEditText().getText().toString();
            String groupNameText = groupName.getText().toString();
            boolean result = true;
            if(text!=null) {
                try {
                    result = FeedServices.post(text,groupNameText);
                } catch (StreamException e) {
                    e.printStackTrace();
                }
            }
                if(result)
                    setResult(RESULT_OK);
                finish();
        });
    }
}