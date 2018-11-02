package com.wadektech.mtihanirevise.fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.wadektech.mtihanirevise.pojo.User;
import com.wadektech.mtihanirevise.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
        CircleImageView profileImage ;
        TextView userName ;
        DatabaseReference databaseReference ;
        FirebaseUser firebaseUser ;
        StorageReference storageReference ;
        private Uri imageUri ;
        private StorageTask<UploadTask.TaskSnapshot> uploadTask ;

    public ProfileFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        userName = view.findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads") ;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    userName.setText(user.getUsername());
                }
                if (user != null) {
                    if (user.getImageURL().equals("default")){
                        profileImage.setImageResource(R.drawable.profile);
                    }else  {
                        Picasso.with(getContext())
                                .load(user.getImageURL())
                                .into(profileImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        return view;
    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver() ;
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton() ;
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage(){
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Uploading image...");
        pDialog.show();

        if (imageUri != null){
            final StorageReference sReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));
            uploadTask = sReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful()){
                        throw task.getException();
                    }
                    return sReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String , Object> map = new HashMap<>();
                        map.put("imageURL" , mUri);
                        databaseReference.updateChildren(map);
                        pDialog.dismiss();
                    }
                    else {
                        Toast.makeText(getContext(),"Upload Fail", Toast.LENGTH_SHORT).show();
                        pDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(),"Please select image!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 86 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData() ;
            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(),"Upload is in progress...", Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }
    }
}
