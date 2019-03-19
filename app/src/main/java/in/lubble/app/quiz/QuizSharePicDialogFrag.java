package in.lubble.app.quiz;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;

import java.util.Random;

import static in.lubble.app.Constants.APP_NOTIF_CHANNEL;
import static in.lubble.app.utils.RoundedCornersTransformation.CornerType.ALL;

public class QuizSharePicDialogFrag extends DialogFragment {

    private static final String TAG = "QuizSharePicDialogFrag";
    private static final String KEY_URI = "QuizSharePicDialog_URI";

    private ImageView screenieIv;
    private TextView shareTv;
    private TextView saveTv;
    private ProgressBar shareProgressbar;
    private Uri picUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme);
    }

    public static QuizSharePicDialogFrag newInstance(Uri picUri) {

        Bundle args = new Bundle();
        args.putParcelable(KEY_URI, picUri);
        QuizSharePicDialogFrag fragment = new QuizSharePicDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        shareTv.setVisibility(View.VISIBLE);
        shareProgressbar.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_quiz_share_pic, container);

        screenieIv = view.findViewById(R.id.iv_screenshot);
        shareTv = view.findViewById(R.id.tv_share);
        saveTv = view.findViewById(R.id.tv_save);
        shareProgressbar = view.findViewById(R.id.progressbar_share);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.trans_gray)));

        if (getArguments() == null) {
            Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
            dismiss();
        }

        picUri = getArguments().getParcelable(KEY_URI);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new FitCenter(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0, ALL));
        GlideApp.with(requireContext()).load(picUri).apply(requestOptions).into(screenieIv);

        shareTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareTv.setVisibility(View.INVISIBLE);
                shareProgressbar.setVisibility(View.VISIBLE);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                intent.putExtra(Intent.EXTRA_STREAM, picUri);
                try {
                    startActivity(Intent.createChooser(intent, "Share Screenshot"));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(requireContext(), "No App Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(picUri,
                                "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), APP_NOTIF_CHANNEL)
                        .setSmallIcon(R.drawable.ic_lubble_notif)
                        .setContentTitle("Quiz Result Image Saved")
                        .setColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                        .setContentText("Click to view in gallery")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(new Random().nextInt(), builder.build());
            }
        });

        return view;
    }
}
