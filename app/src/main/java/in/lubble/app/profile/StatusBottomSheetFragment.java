package in.lubble.app.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;

public class StatusBottomSheetFragment extends BottomSheetDialogFragment {
    private List<String> statusList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StatusBottomSheetAdapter mAdapter;
    private EditText customEt;
    private MaterialButton customSetBtn;
    private MaterialButton setStatus;
    private TextInputLayout customStatusLayout;
    private int selectedPos = -1;
    public StatusBottomSheetFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.bottom_sheet_status, container, false);
        recyclerView = rootview.findViewById(R.id.recycler_view);
        customEt = rootview.findViewById(R.id.custom_et);
        customSetBtn = rootview.findViewById(R.id.custom_btn);
        setStatus = rootview.findViewById(R.id.set_status_btn);
        mAdapter = new StatusBottomSheetAdapter(statusList);
        customStatusLayout = rootview.findViewById(R.id.custom_status_layout);
        // vertical RecyclerView
        // keep movie_list_row.xml width to `match_parent`
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());

        // horizontal RecyclerView
        // keep movie_list_row.xml width to `wrap_content`
        // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);
        String abc = FirebaseAuth.getInstance().getUid();

        // row click listener
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Movie movie = statusList.get(position);
                selectedPos = position;
                if (statusList.get(position).equals("Custom")) {
                    customStatusLayout.setVisibility(View.VISIBLE);
                    customSetBtn.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    setStatus.setVisibility(View.GONE);
                    customSetBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String statusText = customEt.getText().toString();
                            if(statusText.toLowerCase().contains("admin") || statusText.toLowerCase().contains("moderator")){
                                Toast.makeText(getContext(), "You can not choose "+statusText + " without administrative previledges", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                RealtimeDbHelper.getThisUserRef().child("info").child("badge").setValue(statusText);
                                Toast.makeText(getContext(), statusText + " is selected as status!", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        setStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPos == -1){
                    Toast.makeText(getContext(), "Please choose a option for your status", Toast.LENGTH_SHORT).show();
                }
                else{
                    RealtimeDbHelper.getThisUserRef().child("info").child("badge").setValue(statusList.get(selectedPos));
                    Toast.makeText(getContext(), statusList.get(selectedPos) + " is selected as status!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

        getBlockList();
        return rootview;
    }

    private void getBlockList() {
        RealtimeDbHelper.getLubbleBlocksRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> arr = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    statusList.add(dataSnapshot.getKey());
                }
                statusList.add("Custom");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}