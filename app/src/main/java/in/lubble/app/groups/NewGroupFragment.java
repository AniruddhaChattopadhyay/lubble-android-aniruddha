package in.lubble.app.groups;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.database.*;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.GroupData;
import in.lubble.app.utils.ChatUtils;
import permissions.dispatcher.*;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.firebase.RealtimeDbHelper.*;
import static in.lubble.app.utils.FileUtils.*;

@RuntimePermissions
public class NewGroupFragment extends Fragment {

    private static final String TAG = "NewGroupFragment";
    private static final int REQUEST_CODE_GROUP_PIC = 90;

    private ImageView groupIv;
    private EditText groupName;
    private EditText groupDesc;
    private EditText groupQues;
    private Spinner spinner;
    private String currentPhotoPath;
    private DatabaseReference userGroupRef;
    private DatabaseReference createJoinRef;
    private ProgressDialog progressDialog;
    private Uri picUri = null;
    private Query query;
    private ChildEventListener childEventListener;
    private boolean isPvt;

    public NewGroupFragment() {
        // Required empty public constructor
    }

    public static NewGroupFragment newInstance() {
        return new NewGroupFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userGroupRef = getUserGroupsRef();
        createJoinRef = getCreateOrJoinGroupRef();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_new_group, container, false);

        groupIv = view.findViewById(R.id.iv_new_group);
        groupName = view.findViewById(R.id.et_group_title);
        groupDesc = view.findViewById(R.id.et_group_desc);
        groupQues = view.findViewById(R.id.et_group_question);
        spinner = view.findViewById(R.id.spinner_privacy);
        Button createBtn = view.findViewById(R.id.container_create_group);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isdataValid()) {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setTitle(getString(R.string.creating_new_group));
                    progressDialog.setMessage(getString(R.string.all_please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    final GroupData groupData = new GroupData();
                    groupData.setTitle(groupName.getText().toString().trim());
                    groupData.setDescription(groupDesc.getText().toString());
                    groupData.setQuestion(groupQues.getText().toString());
                    groupData.setIsPrivate(isPvt);

                    Log.d(TAG, "onClick: ");
                    DatabaseReference pushRef = createJoinRef.push();
                    pushRef.setValue(groupData);
                    confirmGroupDone(pushRef.getKey());
                }
            }

            private boolean isdataValid() {
                if (TextUtils.isEmpty(groupName.getText().toString())) {
                    groupName.setError("Uh-oh. A girl has no name, but a group must have one");
                    return false;
                }
                if (TextUtils.isEmpty(groupDesc.getText().toString())) {
                    groupDesc.setError("Uh-oh! Group must have a description. A purpose in life.");
                    return false;
                }
                if (TextUtils.isEmpty(groupQues.getText().toString())) {
                    groupQues.setError("Whoa! A joining question is required, to create a tighter community");
                    return false;
                }
                groupName.setError(null);
                groupDesc.setError(null);
                groupQues.setError(null);
                return true;
            }
        });

        groupIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewGroupFragmentPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(NewGroupFragment.this, REQUEST_CODE_GROUP_PIC);
            }
        });

        final MySpinnerAdapter mySpinnerAdapter = new MySpinnerAdapter(getContext(), R.layout.item_privacy_spinner, new String[2]);
        spinner.setAdapter(mySpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isPvt = position == 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GROUP_PIC && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(getContext(), uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }
            picUri = Uri.fromFile(imageFile);
            GlideApp.with(getContext())
                    .load(imageFile)
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .circleCrop()
                    .into(groupIv);
        }
    }

    private void confirmGroupDone(final String pushId) {
        query = userGroupRef.child(pushId);
        childEventListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (picUri != null) {
                    getContext().startService(new Intent(getContext(), UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, "profile_pic_" + System.currentTimeMillis() + ".jpg")
                            .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups/" + pushId)
                            .setAction(UploadFileService.ACTION_UPLOAD));
                }
                // add the joining question as group's first message
                final ChatData chatData = ChatUtils.createGroupChatdata(groupQues.getText().toString());
                final DatabaseReference chatPushRef = getMessagesRef().child(pushId).push();
                chatPushRef.setValue(chatData);
                // make this first chat msg's ID as group's question ID which joinees will reply to
                getLubbleGroupsRef().child(pushId).child("questionChatId").setValue(chatPushRef.getKey());

                progressDialog.dismiss();
                final Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(EXTRA_GROUP_ID, pushId);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (query != null) {
            query.removeEventListener(childEventListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        NewGroupFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
