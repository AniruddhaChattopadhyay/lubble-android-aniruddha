package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;

import static in.lubble.app.auth.LoginActivity.RC_SIGN_IN;

public class WelcomeFrag extends Fragment {

    public WelcomeFrag() {
        // Required empty public constructor
    }

    public static WelcomeFrag newInstance() {
        return new WelcomeFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);


        rootView.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAuthActivity();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAuthActivity() {
        Bundle params = new Bundle();
        params.putString(AuthUI.EXTRA_DEFAULT_COUNTRY_CODE, "in");
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders
                .add(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER)
                        .setParams(params)
                        .build());

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.drawable.ic_android_black_24dp)
                .setAvailableProviders(selectedProviders)
                .setIsSmartLockEnabled(true, true)
                .setAllowNewEmailAccounts(true)
                .build();
        getActivity().startActivityForResult(intent, RC_SIGN_IN);
    }

}
