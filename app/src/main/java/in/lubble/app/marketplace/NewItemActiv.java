package in.lubble.app.marketplace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import org.json.JSONObject;

import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.utils.StringUtils.isValidString;

public class NewItemActiv extends AppCompatActivity {

    private static final String TAG = "NewItemActiv";

    private ScrollView parentScrollView;
    private TextInputLayout nameTil;
    private TextInputLayout descTil;
    private TextInputLayout mrpTil;
    private TextInputLayout sellingPriceTil;
    private Button submitBtn;

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewItemActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        parentScrollView = findViewById(R.id.scrollview_parent);
        nameTil = findViewById(R.id.til_item_name);
        descTil = findViewById(R.id.til_item_desc);
        mrpTil = findViewById(R.id.til_item_mrp);
        sellingPriceTil = findViewById(R.id.til_item_sellingprice);
        submitBtn = findViewById(R.id.btn_submit);

        Analytics.triggerScreenEvent(this, this.getClass());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidationPassed()) {
                    uploadNewItem();
                }
            }
        });
    }

    private void uploadNewItem() {

        HashMap<String, Object> params = new HashMap<>();

        params.put("name", nameTil.getEditText().getText().toString());
        params.put("category", 1);
        params.put("mrp", mrpTil.getEditText().getText().toString());
        params.put("selling_price", sellingPriceTil.getEditText().getText().toString());
        params.put("client_timestamp", System.currentTimeMillis());

        final JSONObject jsonObject = new JSONObject(params);

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.uploadNewItem(body).enqueue(new Callback<Endpoints.ResponseBean>() {
            @Override
            public void onResponse(Call<Endpoints.ResponseBean> call, Response<Endpoints.ResponseBean> response) {
                final Endpoints.ResponseBean responseBean = response.body();
                Log.d(TAG, "onResponse: ");
            }

            @Override
            public void onFailure(Call<Endpoints.ResponseBean> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });
    }

    private boolean isValidationPassed() {
        if (!isValidString(nameTil.getEditText().getText().toString().trim())) {
            nameTil.setError(getString(R.string.event_name_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            nameTil.setError(null);
        }
        if (!isValidString(descTil.getEditText().getText().toString())) {
            descTil.setError(getString(R.string.event_desc_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            descTil.setError(null);
        }
        if (!isValidString(mrpTil.getEditText().getText().toString())) {
            mrpTil.setError(getString(R.string.event_organizer_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            mrpTil.setError(null);
        }
        if (!isValidString(sellingPriceTil.getEditText().getText().toString())) {
            sellingPriceTil.setError(getString(R.string.event_date_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            sellingPriceTil.setError(null);
        }
        return true;
    }

}
