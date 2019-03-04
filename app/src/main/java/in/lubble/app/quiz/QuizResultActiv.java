package in.lubble.app.quiz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;

public class QuizResultActiv extends AppCompatActivity {

    public static void open(Context context) {
        context.startActivity(new Intent(context, QuizResultActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        final int cuisineAnswerId = AnswerSharedPrefs.getInstance().getPreferences().getInt("0", 0);

        RealtimeDbHelper.getLubbleRef().child("quiz/wheretonight/places").orderByChild("cuisine").equalTo(cuisineAnswerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    PlaceData placeData = child.getValue(PlaceData.class);
                    Toast.makeText(QuizResultActiv.this, placeData.getName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
