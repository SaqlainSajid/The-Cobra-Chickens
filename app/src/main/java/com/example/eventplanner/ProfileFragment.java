// OpenAI, 2024, ChatGPT

package com.example.eventplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Represents the fragment for displaying and editing a user's profile.
 */
public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ProfileFragment";
    private ImageView profilePic;
    private Button editPic;
    private Button delPic;
    private TextView userName, userContact, userHomepage;
    private Button editDetails;
    private Button location;
    private Button adminLogin;
    private User user;
    private String userId;
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private FirebaseUser user_test;
    private FirebaseAuth auth_test;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // inflate the layout for the profile fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //getting all the layout objects
        profilePic = view.findViewById(R.id.profilePic);
        editPic = view.findViewById(R.id.editProfilePic);
        delPic = view.findViewById(R.id.deleteProfilePic);
        userName = view.findViewById(R.id.profileName);
        userContact = view.findViewById(R.id.profileContact);
        userHomepage = view.findViewById(R.id.profileHomepage);
        location = view.findViewById(R.id.location);
        editDetails = view.findViewById(R.id.editProfile);
        adminLogin = view.findViewById(R.id.adminLoginBtn);

        auth_test = FirebaseAuth.getInstance();
        user_test = auth_test.getCurrentUser();

        getUser();

        //listening for edit button
        editDetails.setOnClickListener(new View.OnClickListener() {
            EditText editName, editContact, editHomepage;
            Button saveBtn;
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.edit_dialog);
                dialog.show();

                editName = dialog.findViewById(R.id.editName);
                editContact = dialog.findViewById(R.id.editContact);
                editHomepage = dialog.findViewById(R.id.editHomepage);
                saveBtn = dialog.findViewById(R.id.saveBtn);

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = "";
                        String contact = "";
                        String homePage = "";

                        //getting user input
                        name = editName.getText().toString().trim();
                        contact = editContact.getText().toString().trim();
                        homePage = editHomepage.getText().toString().trim();

                        //updating the user object and the textviews
                        userName.setText("Name:" + name);
                        userContact.setText("Contact:" +contact);
                        userHomepage.setText("Homepage:"+homePage);

                        usersRef = db.collection("users");
                        usersRef.document(userId).update("Name", name, "Contact", contact, "Homepage",homePage);
                        user.setName(name);
                        user.setContactInformation(contact);
                        user.setHomepage(homePage);

                        if (name != null && name.equals("")) {
                            Glide.with(requireContext()).load("https://www.gravatar.com/avatar/" + userId + "?d=identicon").into(profilePic);
                        }
                        else {
                            Glide.with(requireContext()).load("https://www.gravatar.com/avatar/" + name + "?d=identicon").into(profilePic);
                        }

                        dialog.dismiss();
                    }
                });

            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef = db.collection("users");

                if(user.getGeolocationTrackingEnabled()){
                    location.setText("OFF");
                    usersRef.document(userId).update("Location", false);
                    user.setGeolocationTrackingEnabled(false);
                } else {
                    location.setText("ON");
                    usersRef.document(userId).update("Location", true);
                    user.setGeolocationTrackingEnabled(true);
                }
            }
        });

        editPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);

            }
        });

        delPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProfilePic();
            }
        });

        adminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AdminActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void deleteProfilePic() {

        userId = user_test.getUid();
        usersRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String ProfilePicUrl = document.getString("ProfilePic");
                        String usrname = document.getString("Name");
                        if(ProfilePicUrl != null && !ProfilePicUrl.equals("")){
                            usersRef.document(userId).update("ProfilePic", "");

                            if (usrname != null && usrname.equals("")) {
                                Glide.with(requireContext()).load("https://www.gravatar.com/avatar/" + userId + "?d=identicon").into(profilePic);
                            }
                            else {
                                Glide.with(requireContext()).load("https://www.gravatar.com/avatar/" + usrname + "?d=identicon").into(profilePic);
                            }

                        } else {
                            Toast toast = Toast.makeText(getContext(), "You can't delete default Profile Picture", Toast.LENGTH_LONG);
                            toast.show();
                        }

                    } else {
                        Log.d(TAG,"No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }


    ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageuri = result.getData().getData();
                    uploadImage(imageuri);
                }
            }
    );

    private void uploadImage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + userId + ".png");

        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Image upload successful
                // Get the download URL of the uploaded image
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Update the user document with the profile picture URI
                    usersRef.document(userId).update("ProfilePic", uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                // Update the user object with the new profile picture URI
                                user.setProfilePicture(uri.toString());
//                                // Update the ImageView with the new profile picture
                                Glide.with(requireContext()).load(uri).into(profilePic);

                            })
                            .addOnFailureListener(e -> {
                                // Handle errors updating the user document
                                Log.e(TAG, "Error updating user document", e);
                                Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    // Handle errors getting the download URL
                    Log.e(TAG, "Error getting download URL", e);
                    Toast.makeText(requireContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Image upload failed
                Log.e(TAG, "Image upload failed", task.getException());
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(snapshot -> {
            // You can track the progress of the upload here if needed
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            Log.d(TAG, "Upload is " + progress + "% done");
        });
    }

    private void getUser(){
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        userId = user_test.getUid();

        usersRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("Name");
                        String contact = document.getString("Contact");
                        String homePage = document.getString("Homepage");
                        String profilePicUrl = document.getString("ProfilePic");
                        boolean usrlocation = document.getBoolean("Location");

                        ArrayList<String> checkedInto = (ArrayList<String>) document.get("checkedInto");
                        ArrayList<String> signedUpFor = (ArrayList<String>) document.get("myEvents");
                        ArrayList<String> organizing = (ArrayList<String>) document.get("checkedInto");

                        if(profilePicUrl != null && !profilePicUrl.equals("")){
                            String profilePicURI = document.getString("ProfilePic");
                            user = new User(userId, name, homePage, contact, profilePicURI, usrlocation, signedUpFor, checkedInto, organizing);
                            Glide.with(requireContext()).load(profilePicURI).into(profilePic);
                        } else {

                            if (name != null && name.equals("")) {
                                user = new User(userId, name, homePage, contact, null, usrlocation, signedUpFor, checkedInto, organizing);
                                Glide.with(requireContext()).load("https://www.gravatar.com/avatar/" + userId + "?d=identicon").into(profilePic);
                            }
                            else {
                                user = new User(userId, name, homePage, contact, null, usrlocation, signedUpFor, checkedInto, organizing);
                                Glide.with(requireContext()).load("https://www.gravatar.com/avatar/" + user.getName() + "?d=identicon").into(profilePic);
                            }
                        }
                        //setting layout objects to the User object's values
                        userName.setText("Name: " + user.getName());
                        userContact.setText("Contact: " + user.getContactInformation());
                        userHomepage.setText("Homepage: " + user.getHomepage());
                        if (user.getGeolocationTrackingEnabled()) {
                            location.setText("ON");
                        } else {
                            location.setText("OFF");
                        }


                    } else {
                        Log.d(TAG,"No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
}