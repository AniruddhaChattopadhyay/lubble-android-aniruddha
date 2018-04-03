package in.lubble.app.network;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
            Document doc = Jsoup.connect(url).timeout(30 * 1000).get();
            String title = doc.title();
            String description = doc.select("meta[name=description]").attr("content");
            listener.onMetaFetched(title, description);
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
