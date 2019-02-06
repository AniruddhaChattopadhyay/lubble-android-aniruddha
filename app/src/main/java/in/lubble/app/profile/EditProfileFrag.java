package in.lubble.app.profile;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.StringUtils;
import permissions.dispatcher.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.utils.DateTimeUtils.OFFICIAL_DATE_YEAR;
import static in.lubble.app.utils.FileUtils.*;

@RuntimePermissions
public class EditProfileFrag extends Fragment {

    private static final String TAG = "EditProfileFrag";
    private static final int REQUEST_CODE_COVER_PIC = 560;
    private static final int REQUEST_CODE_DP = 211;

    private ImageView profilePicIv;
    private TextView fullNameTv;
    private TextView lubbleTv;
    private TextInputLayout bioTil;
    private TabLayout genderTabLayout;
    private TextInputLayout jobTitleTil;
    private TextInputLayout companyTil;
    private TextInputLayout schoolTil;
    private TextView bdayTv;
    private Switch ageSwitch;
    private Button saveBtn;
    private View rootView;
    private String currentPhotoPath;
    private ProgressBar progressBar;
    private Uri newProfilePicUri = null;
    private Uri newCoverPicUri = null;
    private ProfileData fetchedProfileData;
    private long bdayEpochTime = 0L;

    public EditProfileFrag() {
        // Required empty public constructor
    }

    public static EditProfileFrag newInstance() {
        return new EditProfileFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profilePicIv = rootView.findViewById(R.id.iv_profilePic);
        fullNameTv = rootView.findViewById(R.id.tv_name);
        lubbleTv = rootView.findViewById(R.id.tv_lubble);
        bioTil = rootView.findViewById(R.id.til_bio);
        genderTabLayout = rootView.findViewById(R.id.tablayout_gender);
        jobTitleTil = rootView.findViewById(R.id.til_job_title);
        companyTil = rootView.findViewById(R.id.til_company);
        schoolTil = rootView.findViewById(R.id.til_college);
        bdayTv = rootView.findViewById(R.id.tv_bday);
        ageSwitch = rootView.findViewById(R.id.switch_age);
        saveBtn = rootView.findViewById(R.id.btn_save_profile);
        progressBar = rootView.findViewById(R.id.progressBar_profile);

        rootView.findViewById(R.id.iv_dp_edit_overlay).setVisibility(View.VISIBLE);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        // single listener becoz data wudnt have changed going from profile to edit profile.
        getThisUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    fetchedProfileData = dataSnapshot.getValue(ProfileData.class);
                    fullNameTv.setText(fetchedProfileData.getInfo().getName());
                    lubbleTv.setText(fetchedProfileData.getLocality());
                    bioTil.getEditText().setText(fetchedProfileData.getBio());
                    if (fetchedProfileData.getGender() != -1) {
                        genderTabLayout.getTabAt(fetchedProfileData.getGender()).select();
                    }
                    jobTitleTil.getEditText().setText(fetchedProfileData.getJobTitle());
                    companyTil.getEditText().setText(fetchedProfileData.getCompany());
                    schoolTil.getEditText().setText(fetchedProfileData.getSchool());
                    if (fetchedProfileData.getBirthdate() > 0L) {
                        bdayTv.setText("Birthdate: " + DateTimeUtils.getDateFromLong(fetchedProfileData.getBirthdate()));
                        bdayTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                        bdayTv.setBackground(null);
                        bdayTv.setPadding(0, 0, 0, 0);
                        bdayTv.setOnClickListener(null);
                        bdayEpochTime = fetchedProfileData.getBirthdate();
                    }
                    ageSwitch.setChecked(fetchedProfileData.getIsAgePublic());
                    GlideApp.with(getContext())
                            .load(fetchedProfileData.getProfilePic())
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .circleCrop()
                            .into(profilePicIv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(EditProfileFrag.this, REQUEST_CODE_DP);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bdayEpochTime == 0L) {
                    Toast.makeText(requireContext(), "Please set birthdate to verify age", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newProfilePicUri != null) {
                    getContext().startService(new Intent(getContext(), UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "profile_pic_" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, newProfilePicUri)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "user_profile/" + FirebaseAuth.getInstance().getUid())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }
                if (newCoverPicUri != null) {
                    getContext().startService(new Intent(getContext(), UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "cover_pic" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, newCoverPicUri)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "user_profile/" + FirebaseAuth.getInstance().getUid())
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }

                getThisUserRef().child("bio").setValue(StringUtils.getStringFromTil(bioTil));
                getThisUserRef().child("gender").setValue(genderTabLayout.getSelectedTabPosition());
                getThisUserRef().child("jobTitle").setValue(StringUtils.getStringFromTil(jobTitleTil));
                getThisUserRef().child("company").setValue(StringUtils.getStringFromTil(companyTil));
                getThisUserRef().child("school").setValue(StringUtils.getStringFromTil(schoolTil));
                getThisUserRef().child("birthdate").setValue(bdayEpochTime);
                getThisUserRef().child("isAgePublic").setValue(ageSwitch.isChecked());
                getFragmentManager().popBackStack();
            }
        });

        if (bdayEpochTime == 0L) {
            bdayTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SpinnerDatePickerDialogBuilder()
                            .context(getContext())
                            .callback(new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    Calendar myCalendar = Calendar.getInstance();
                                    myCalendar.set(Calendar.YEAR, year);
                                    myCalendar.set(Calendar.MONTH, monthOfYear);
                                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    String myFormat = OFFICIAL_DATE_YEAR;
                                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

                                    bdayTv.setText(sdf.format(myCalendar.getTime()));
                                    bdayTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                                    bdayEpochTime = myCalendar.getTimeInMillis();
                                }
                            })
                            .spinnerTheme(R.style.NumberPickerStyle)
                            .showTitle(true)
                            .showDaySpinner(true)
                            .defaultDate(2000, 0, 1)
                            .build()
                            .show();
                }
            });
        }

        return rootView;
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void startPhotoPicker(int REQUEST_CODE) {
        try {
            File cameraPic = createImageFile(getContext());
            currentPhotoPath = cameraPic.getAbsolutePath();
            Intent pickImageIntent = getPickImageIntent(getContext(), cameraPic);
            startActivityForResult(pickImageIntent, REQUEST_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE_DP || requestCode == REQUEST_CODE_COVER_PIC) && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(getContext(), uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }

            if (requestCode == REQUEST_CODE_DP) {
                newProfilePicUri = Uri.fromFile(imageFile);
                GlideApp.with(getContext())
                        .load(imageFile)
                        .centerCrop()
                        .circleCrop()
                        .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                        .into(profilePicIv);
            }

        } else {
            Log.e(TAG, "onActivityResult: Failed to get photo");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        EditProfileFragPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(getContext(), request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(getContext(), R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

}
