package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.ui.auth.IdpResponse;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;

import static in.lubble.app.auth.LoginActivity.REQUEST_LOCATION;

public class NameFrag extends Fragment {

    private static final String TAG = "NameFrag";

    private TextInputLayout fullNameTil;
    private Button continueBtn;

    public NameFrag() {
        // Required empty public constructor
    }

    public static NameFrag newInstance(IdpResponse response) {
        NameFrag fragment = new NameFrag();
        Bundle args = new Bundle();
        args.putParcelable("idpResponse", response);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_name, container, false);

        fullNameTil = rootView.findViewById(R.id.til_full_name);
        continueBtn = rootView.findViewById(R.id.btn_continue);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        final Parcelable idpResponse = getArguments().getParcelable("idpResponse");

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(fullNameTil.getEditText().getText())) {
                    LubbleSharedPrefs.getInstance().setFullName(fullNameTil.getEditText().getText().toString().trim());

                    final Intent intent = new Intent(getContext(), LocationActivity.class);
                    intent.putExtra("idpResponse", idpResponse);
                    getActivity().startActivityForResult(intent, REQUEST_LOCATION);
                }
            }
        });
        return rootView;
    }


}
