package com.wadektech.mtihanirevise.fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.wadektech.mtihanirevise.database.MtihaniDatabase;
import com.wadektech.mtihanirevise.persistence.MtihaniRevise;
import com.wadektech.mtihanirevise.repository.MtihaniRepository;
import com.wadektech.mtihanirevise.room.User;
import com.wadektech.mtihanirevise.ui.ChatActivity;
import com.wadektech.mtihanirevise.ui.MessageActivity;
import com.wadektech.mtihanirevise.ui.PDFViewerActivity;
import com.wadektech.mtihanirevise.ui.PaperPerSubject;
import com.wadektech.mtihanirevise.ui.StatusUpdate;
import com.wadektech.mtihanirevise.utils.Constants;
import com.wadektech.mtihanirevise.viewmodels.UsersViewModel;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    CircleImageView profileImage;
    TextView userName, statusDisplay;
    TextView userEmail ;
            DatabaseReference databaseReference;
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
        ImageButton btnStatus = view.findViewById(R.id.update);
        statusDisplay = view.findViewById(R.id.status_display);
        statusDisplay.setText(Constants.getStatus());
        userName.setText(Constants.getUserName());
        TextView navigateBack = view.findViewById(R.id.nav_back);
        userEmail = view.findViewById(R.id.user_email);
        userEmail.setText(Constants.getEmail());
        navigateBack.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            startActivity(intent);
        });

FirebaseFirestore
        .getInstance()
        .collection("Users")
        .document(Constants.getUserId())
        .get()
        .addOnSuccessListener(requireActivity(), snapshot -> {
            if(snapshot != null){
                if(snapshot.exists()){
                    User user = snapshot.toObject(User.class);
                    if(user != null){
                        String status = user.getUpdate();
                        SharedPreferences pfs =
                                Objects.requireNonNull(MtihaniRevise.Companion.getApp())
                                .getApplicationContext().getSharedPreferences(Constants.myPreferences, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pfs.edit();
                        editor.putString(Constants.status, status);
                        editor.apply();
                    }
                }
            }
        });

        btnStatus.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StatusUpdate.class);
            startActivity(intent);
        });
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        SharedPreferences pfs = requireActivity().getSharedPreferences(Constants.myPreferences,Context.MODE_PRIVATE);
        String imageURL = pfs.getString(Constants.imageURL,"");
        assert imageURL != null;
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
                                                    .load(Constants.getImageURL())
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .into(profileImage, new Callback() {
                                                        @Override
                                                        public void onSuccess() {

                                                        }

                                                        @Override
                                                        public void onError() {
                                                            Picasso.with(getActivity())
                                                                    .load(Constants.getImageURL())
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
                                Timber.d("error %s", task.getException().toString());
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
        ContentResolver contentResolver = requireContext().getContentResolver();
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
                    throw Objects.requireNonNull(task.getException());
                }
                return sReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    String mUri = downloadUri.toString();
                        SharedPreferences pfs = Objects.requireNonNull(MtihaniRevise
                                .Companion
                                .getApp())
                                .getApplicationContext().getSharedPreferences(Constants.myPreferences, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pfs.edit();
                        editor.putString(Constants.imageURL, mUri);
                        editor.apply();

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
