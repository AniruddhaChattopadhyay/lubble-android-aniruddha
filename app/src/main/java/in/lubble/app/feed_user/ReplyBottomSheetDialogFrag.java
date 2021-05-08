package in.lubble.app.feed_user;

import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.MissingFormatArgumentException;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.widget.ReplyEditText;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Reaction;

public class ReplyBottomSheetDialogFrag extends BottomSheetDialogFragment {

    private static final String ARG_ACT_ID = "LBL_REPLY_ARG_ACT_ID";

    private ReplyEditText replyEt;
    private ImageView replyIv;
    private ProgressBar progressBar;
    private final String userId = FirebaseAuth.getInstance().getUid();
    private ReplyListener replyListener;

    public static ReplyBottomSheetDialogFrag newInstance(String activityId) {
        Bundle args = new Bundle();
        args.putString(ARG_ACT_ID, activityId);
        ReplyBottomSheetDialogFrag fragment = new ReplyBottomSheetDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            replyListener = (ReplyListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement ReplyListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_reply_bottom_sheet, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        replyEt = view.findViewById(R.id.et_reply);
        replyIv = view.findViewById(R.id.iv_reply);
        progressBar = view.findViewById(R.id.progressbar_reply);

        replyEt.setFrag(this);

        String replyText = LubbleSharedPrefs.getInstance().getReplyBottomSheet();
        if (!TextUtils.isEmpty(replyText)) {
            replyEt.append(replyText);
        }

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

        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                UiUtils.showKeyboard(getActivity(), replyEt);
            }
        }, 600);

        GlideApp.with(requireContext())
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
            replyIv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Reaction comment = new Reaction.Builder()
                    .kind("comment")
                    .userID(userId)
                    .activityID(activityId)
                    .extraField("text", replyEt.getText().toString().trim())
                    .build();
            FeedServices.getTimelineClient().reactions().add(comment).whenComplete((reaction, throwable) -> {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (throwable != null) {
                            Toast.makeText(getContext(), "Reply Failed!", Toast.LENGTH_SHORT).show();
                            replyIv.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            replyEt.clearFocus();
                            replyEt.setText("");
                            LubbleSharedPrefs.getInstance().setReplyBottomSheet(null);
                            replyListener.onReplied();
                            dismiss();
                        }
                    });
                }
            });
        } catch (StreamException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            replyIv.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    public void onBackPressed() {
        dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (replyListener != null) {
            replyListener.onDismissed();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (replyEt.getText() != null && replyEt.getText().length() > 0) {
            LubbleSharedPrefs.getInstance().setReplyBottomSheet(replyEt.getText().toString());
        }
        if (replyListener != null) {
            replyListener.onDismissed();
        }
    }

}