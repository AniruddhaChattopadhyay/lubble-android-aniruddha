package in.lubble.app.feed_user;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import io.getstream.core.models.EnrichedActivity;

public class FeedPostComparator extends DiffUtil.ItemCallback<EnrichedActivity> {
    @Override
    public boolean areItemsTheSame(@NonNull EnrichedActivity oldItem,
                                   @NonNull EnrichedActivity newItem) {
        return oldItem.getID().equals(newItem.getID());
    }

    @Override
    public boolean areContentsTheSame(@NonNull EnrichedActivity oldItem,
                                      @NonNull EnrichedActivity newItem) {
        return oldItem.equals(newItem);
    }
}
