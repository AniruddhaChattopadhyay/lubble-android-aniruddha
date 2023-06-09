package in.lubble.app.network;

import android.os.AsyncTask;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by shadow-admin on 31/7/17.
 */

public class LinkMetaAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "LinkMetaAsyncTask";

    private String url;
    private LinkMetaListener listener;

    public LinkMetaAsyncTask(String url, LinkMetaListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Document doc = Jsoup.connect(url).timeout(10 * 1000).get();

            String title = "";
            Elements metaOgTitle = doc.select("meta[property=og:title]");
            if (metaOgTitle != null && !TextUtils.isEmpty(metaOgTitle.attr("content"))) {
                title = metaOgTitle.attr("content");
            } else {
                title = doc.title();
            }

            Elements metaOgDesc = doc.select("meta[name=og:description]");
            String description = "";
            if (metaOgDesc != null && !TextUtils.isEmpty(metaOgDesc.attr("content"))) {
                description = metaOgDesc.attr("content");
            } else {
                description = doc.select("meta[name=description]").attr("content");
            }
            String imgUrl = "";
            Elements metaOgImage = doc.select("meta[property=og:image]");
            if (metaOgImage != null && !TextUtils.isEmpty(metaOgImage.attr("content"))) {
                imgUrl = metaOgImage.attr("content");
            } else {
                imgUrl = null;
            }
            listener.onMetaFetched(title, description, imgUrl);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onMetaFailed();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onMetaFailed();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void a) {
        super.onPostExecute(a);
    }

}
