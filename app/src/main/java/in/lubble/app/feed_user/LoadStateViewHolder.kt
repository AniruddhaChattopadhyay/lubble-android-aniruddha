package `in`.lubble.app.feed_user

import `in`.lubble.app.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView

class LoadStateViewHolder(parent: ViewGroup,
                          retry: () -> Unit
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
                .inflate(R.layout.load_state_item, parent, false)
) {
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMsg: TextView
    private val retry = itemView.setOnClickListener { retry() }

    fun bind(loadState: LoadState) {
        progressBar = itemView.findViewById(R.id.progressbar_paging)
        errorMsg = itemView.findViewById(R.id.tv_error_paging)

        if (loadState is LoadState.Error) {
            errorMsg.text = loadState.error.localizedMessage + "\nTap to retry"
        }

        progressBar.isVisible = loadState is LoadState.Loading
        errorMsg.isVisible = loadState is LoadState.Error
    }
}

// Adapter that displays a loading spinner when
// state = LoadState.Loading, and an error message and retry
// button when state is LoadState.Error.
class PagingLoadStateAdapter(
        private val retry: () -> Unit
) : LoadStateAdapter<LoadStateViewHolder>() {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
    ) = LoadStateViewHolder(parent, retry)

    override fun onBindViewHolder(
            holder: LoadStateViewHolder,
            loadState: LoadState
    ) = holder.bind(loadState)
}