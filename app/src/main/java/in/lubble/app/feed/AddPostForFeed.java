package in.lubble.app.feed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import in.lubble.app.R;

public class AddPostForFeed extends AppCompatActivity {

    private MaterialButton postSubmitBtn;
    private EditText postText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post_for_feed);
        postSubmitBtn = findViewById(R.id.post_btn);
        postText = findViewById(R.id.post_edt_txt);

        postSubmitBtn.setOnClickListener(v->{
            String text = postText.getText().toString();
            /*boolean result = true;
            if(text!=null) {
//                if (FeedServices.client != null) {
//                    CloudFlatFeed feed = FeedServices.client.flatFeed("user");
//                    try {
//                        feed.addActivity(
//                                Activity
//                                        .builder()
//                                        .actor("SU:" + "c4ZIgCriHdcU5avx70AgY0000jj1")
//                                        .verb("post")
//                                        .object("picture:10")
//                                        .extraField("message", text)
//                                        .extraField("photoLink", "https://expertphotography.com/wp-content/uploads/2011/06/how-to-take-good-pictures-image2.jpg")
//                                        .extraField("authorName", "Ramu")
//                                        .build()
//                        ).join();
//                    } catch (StreamException e) {
//                        e.printStackTrace();
//                    }
                    result = FeedServices.post(text);
                }
                if(result)
                    setResult(RESULT_OK);
                finish();*/
            openGroupSelectionActivity();
        });

    }

    private void openGroupSelectionActivity() {
        Intent groupSelectionActivIntent = GroupSelectionActiv.getIntent(this);
        groupSelectionActivIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(groupSelectionActivIntent);
        //finish();
    }
}