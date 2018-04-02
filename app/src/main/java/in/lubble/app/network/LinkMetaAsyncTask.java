package in.lubble.app.network;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by shadow-admin on 31/7/17.
 */

public class LinkMetaAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "LinkMetaAsyncTask";

    private String url;
    private LinkMetaListener listener;

    public LinkMetaAsyncTask(String url, LinkMetaListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            String description = doc.select("meta[name=description]").attr("content");
            listener.onMetaFetched(title, description);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
    }

}
