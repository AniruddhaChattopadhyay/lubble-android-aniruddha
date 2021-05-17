package `in`.lubble.app.feed_groups

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.getstream.cloud.CloudFlatFeed
import io.getstream.core.exceptions.StreamException
import io.getstream.core.models.EnrichedActivity
import io.getstream.core.options.EnrichmentFlags
import io.getstream.core.options.Limit
import io.getstream.core.options.Offset
import java.io.IOException

class FeedPagingSource(
        private val cloudFlatFeed: CloudFlatFeed,
        private val limit: Int
) : PagingSource<Int, EnrichedActivity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EnrichedActivity> {
        try {
            // Start refresh at 0 if undefined.
            val offset = params.key ?: 0
            val response = cloudFlatFeed.getEnrichedActivities(Limit(limit), Offset(offset),
                    EnrichmentFlags()
                            .withReactionCounts()
                            .withOwnReactions()
                            .withRecentReactions())
                    .join()
            val nextKey = if (response.size < limit) {
                //fewer items were returned than the max limit, i.e. this is the last page
                null
            } else {
                offset + limit
            }
            return LoadResult.Page(
                    data = response,
                    prevKey = null, // Only paging forward.
                    nextKey = nextKey
            )
        } catch (e: IOException) {
            // IOException for network failures.
            return LoadResult.Error(e)
        } catch (e: StreamException) {
            // StreamException for any Stream errors.
            return LoadResult.Error(e)
        } catch (e: Exception) {
            // unexpected error
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, EnrichedActivity>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}