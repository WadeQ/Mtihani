package com.wadektech.mtihanirevise.fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.persistence.MtihaniRevise;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.ui.StatusUpdate;
import com.wadektech.mtihanirevise.utils.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    CircleImageView profileImage;
    TextView userName, statusDisplay;
    DatabaseReference databaseReference;
    //FirebaseUser firebaseUser;
    StorageReference storageReference;
    public static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.username);
        Button btnStatus = view.findViewById(R.id.update);

        statusDisplay = view.findViewById(R.id.status_display);
        userName.setText(Constants.getUserName());
        //firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //assert firebaseUser != null;
       // String userID = firebaseUser.getUid();
       /* databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String val = dataSnapshot.child("update").getValue(String.class);
                    if (val != null) {
                        statusDisplay.setText(val);
                    } else {
                        Toast.makeText(getContext(), "Null object!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
FirebaseFirestore
        .getInstance()
        .collection("Users")
        .document(Constants.getUserId())
        .get()
        .addOnSuccessListener(getActivity(), snapshot -> {
            if(snapshot != null){
                if(snapshot.exists()){
                    User user = snapshot.toObject(User.class);
                    if(user != null){
                        statusDisplay.setText(user.getUpdate());
                    }
                }
            }
        });
        btnStatus.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StatusUpdate.class);
            startActivity(intent);
        });

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        //firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        /**
         * Replaced code with Firestore code
         */
        /* databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    userName.setText(user.getUsername());
                }
                if (user != null) {
                    if (user.getImageURL().equals("default")) {
                        profileImage.setImageResource(R.drawable.profile);
                    } else {
                        final int defaultImageResId = R.drawable.profile;
                        Picasso.with(getContext())
                                .load(user.getImageURL())
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(profileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext())
                                                .load(user.getImageURL())
                                                .error(defaultImageResId)
                                                .into(profileImage);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });*/
        SharedPreferences pfs = getActivity().getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        String imageURL = pfs.getString(Constants.imageURL,"");
        if(imageURL.equals("")|| imageURL.equals("default")) {
            FirebaseFirestore
                    .getInstance()
                    .collection("Users")
                    .document(/*firebaseUser.getUid()*/Constants.getUserId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    User user = task.getResult().toObject(User.class);
                                    if (user != null) {
                                        userName.setText(user.getUsername());
                                        if (user.getImageURL().equals("default")) {
                                            profileImage.setImageResource(R.drawable.profile);
                                        } else {
                                            final int defaultImageResId = R.drawable.profile;
                                            Picasso.with(getContext())
                                                    .load(user.getImageURL())
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(profileImage, new Callback() {
                                                        @Override
                                                        public void onSuccess() {

                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Picasso.with(getActivity())
                                                                    .load(user.getImageURL())
                                                                    .error(defaultImageResId)
                                                                    .into(profileImage);
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }

                        } else {
                            if (task.getException() != null) {
                                Log.d("ProfileFragment", "error " + task.getException().toString());
                            }
                        }
                    });
        }else{
            Picasso.with(getActivity())
                    .load(imageURL)
                    .error(R.drawable.profile)
                    .into(profileImage);
        }
        profileImage.setOnClickListener(v -> openImage());
        return view;
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Uploading image...");
        pDialog.show();

        if (imageUri != null) {
            final StorageReference sReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            uploadTask = sReference.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return sReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();
                        SharedPreferences pfs = MtihaniRevise.getApp()
                                .getApplicationContext().getSharedPreferences(Constants.myPreferences, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pfs.edit();
                        editor.putString(Constants.imageURL, mUri);
                        editor.apply();
                    /**
                     * Replaced code with Firestore
                     */
                    /*databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    HashMap<String , Object> map = new HashMap<>();
                    map.put("imageURL" , mUri);
                    databaseReference.updateChildren(map);*/

                    FirebaseFirestore
                            .getInstance()
                            .collection("Users")
                            .document(/*firebaseUser.getUid()*/Constants.getUserId())
                            .update("imageURL", mUri);
                    pDialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Upload Fail", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            });
        } else {
            Toast.makeText(getContext(), "Please select image!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload is in progress...", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
}
