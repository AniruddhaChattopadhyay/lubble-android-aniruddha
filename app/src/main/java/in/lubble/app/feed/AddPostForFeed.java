package in.lubble.app.feed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import in.lubble.app.R;
import in.lubble.app.feed.services.FeedServices;
import io.getstream.client.Feed;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;
import static android.app.Activity.RESULT_OK;

public class AddPostForFeed extends AppCompatActivity {

    private Button postSubmitBtn;
    private TextInputLayout postText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post_for_feed);
        postSubmitBtn = findViewById(R.id.post_btn);
        postText = findViewById(R.id.post_edt_txt);

        postSubmitBtn.setOnClickListener(v->{
            String text = postText.getEditText().getText().toString();
            boolean result = true;
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
                finish();
        });
    }
}