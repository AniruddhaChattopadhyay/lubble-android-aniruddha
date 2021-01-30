package in.lubble.app.chat;

import android.text.SpannableString;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import in.lubble.app.utils.Command;

public class CustomURLSpan extends URLSpan {
    @Nullable
    private final Command mClickAction;

    CustomURLSpan(@NonNull String url, @Nullable Command clickAction) {
        super(url);
        this.mClickAction = clickAction;
    }

    @Override
    public void onClick(View widget) {
        mClickAction.execute();
        super.onClick(widget);
    }

    public static void clickifyTextView(TextView tv, Command clickAction) {
        SpannableString current = new SpannableString(tv.getText());
        URLSpan[] spans =
                current.getSpans(0, current.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);

            current.removeSpan(span);
            current.setSpan(new CustomURLSpan(span.getURL(), clickAction), start, end, 0);
            tv.setText(current);
        }
    }
}
