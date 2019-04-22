package in.lubble.app.chat.books;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import in.lubble.app.BaseActivity;
import in.lubble.app.R;

public class BookCheckoutActiv extends BaseActivity {

    private static final String TAG = "BookCheckoutActiv";

    private Button addressBtn;

    public static void open(Context context) {
        context.startActivity(new Intent(context, BookCheckoutActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activ_book_checkout);

        addressBtn = findViewById(R.id.btn_address);

        addressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressChooserActiv.open(BookCheckoutActiv.this);
            }
        });

    }
}
