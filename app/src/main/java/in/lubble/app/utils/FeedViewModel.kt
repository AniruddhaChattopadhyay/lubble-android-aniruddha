package `in`.lubble.app.utils

import `in`.lubble.app.feed_groups.FeedPagingSource
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import androidx.paging.*
import io.getstream.cloud.CloudFlatFeed
import io.getstream.core.models.EnrichedActivity
import org.jetbrains.annotations.NotNull

class FeedViewModel : ViewModel() {

    private val TAG = "FeedViewModel"
    private val liveData = MutableLiveData<VisibleState>()
    val distinctLiveData: LiveData<VisibleState> =
            Transformations.distinctUntilChanged(liveData).debounce()

    private fun <T> LiveData<T>.debounce(duration: Long = 2000L) = MediatorLiveData<T>().also { mld ->
        val source = this
        val handler = Handler(Looper.getMainLooper())

        val runnable = Runnable {
            mld.value = source.value
        }

        mld.addSource(source) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, duration)
        }
    }

    fun onScrolled(visibleState: @NotNull VisibleState) {
        liveData.postValue(visibleState)
    }

    fun loadPaginatedActivities(cloudFlatFeed: CloudFlatFeed, limit: Int, rankingAlgo: String?): LiveData<PagingData<EnrichedActivity>> {
        val pager: Pager<Int, EnrichedActivity> = Pager(
                PagingConfig(limit, 1), null
        ) { FeedPagingSource(cloudFlatFeed, limit, rankingAlgo) }

        return pager.liveData.cachedIn(viewModelScope)
    }

}