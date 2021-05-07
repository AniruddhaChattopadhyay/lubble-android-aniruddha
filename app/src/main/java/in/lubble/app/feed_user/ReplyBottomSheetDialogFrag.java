package in.lubble.app.feed_user;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.MissingFormatArgumentException;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.UiUtils;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Reaction;

import static in.lubble.app.utils.UiUtils.getCircularProgressDrawable;

public class ReplyBottomSheetDialogFrag extends BottomSheetDialogFragment {

    private static final String ARG_ACT_ID = "LBL_REPLY_ARG_ACT_ID";

    private EditText replyEt;
    private ImageView replyIv;
    private final String userId = FirebaseAuth.getInstance().getUid();

    public static ReplyBottomSheetDialogFrag newInstance(String activityId) {
        Bundle args = new Bundle();
        args.putString(ARG_ACT_ID, activityId);
        ReplyBottomSheetDialogFrag fragment = new ReplyBottomSheetDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.frag_reply_bottom_sheet, container, false);

        replyEt = view.findViewById(R.id.et_reply);
        replyIv = view.findViewById(R.id.iv_reply);

        if (getArguments() != null) {
            getArguments().getString(ARG_ACT_ID);
        } else {
            throw new MissingFormatArgumentException("Activity ID missing");
        }

        replyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String inputString = editable.toString();
                if (editable.length() > 0 && inputString.trim().length() > 0) {
                    replyIv.setAlpha(1f);
                } else {
                    replyIv.setAlpha(0.4f);
                }
            }
        });

        replyIv.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(replyEt.getText().toString())) {
                postComment(getArguments().getString(ARG_ACT_ID));
            } else {
                Toast.makeText(getContext(), "Reply can't be empty", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        new Handler().postDelayed(() -> {
            UiUtils.showKeyboard(requireContext(), replyEt);
        }, 400);

        GlideApp.with(getContext())
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .apply(new RequestOptions().override(UiUtils.dpToPx(24), UiUtils.dpToPx(24)))
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE) //caches final image after transformations
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        replyEt.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        replyEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle_grey_24dp, 0, 0, 0);
                    }
                });
    }

    private void postComment(String activityId) {
        try {
            CircularProgressDrawable circularProgressDrawable = getCircularProgressDrawable(getContext());
            replyIv.setImageDrawable(circularProgressDrawable);
            Reaction comment = new Reaction.Builder()
                    .kind("comment")
                    .userID(userId)
                    .activityID(activityId)
                    .extraField("text", replyEt.getText().toString().trim())
                    .build();
            FeedServices.getTimelineClient().reactions().add(comment).whenComplete((reaction, throwable) -> {
                if (isAdded() && getActivity() != null) {
                    if (throwable != null) {
                        replyIv.setImageResource(R.drawable.ic_send_white_24dp);
                        Toast.makeText(getContext(), "Reply Failed!", Toast.LENGTH_SHORT).show();
                    } else {
                        getActivity().runOnUiThread(() -> {
                            replyEt.clearFocus();
                            replyEt.setText("");
                            dismiss();
                            //todo update item initCommentRecyclerView(holder, activity);
                        });
                    }
                }
            });
        } catch (StreamException e) {
            e.printStackTrace();
            //todo
        }
    }

}