package `in`.lubble.app.utils

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import org.jetbrains.annotations.NotNull

class TrackingViewModel : ViewModel() {

    private val TAG = "TrackingFlow"
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

}