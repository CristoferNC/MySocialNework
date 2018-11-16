package com.example.cristofer.mysocialnework.ProfileUser;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cristofer.mysocialnework.MainActivity;
import com.example.cristofer.mysocialnework.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileInformationActivity extends AppCompatActivity {

    //FIELD'S
    private EditText txtUserName;
    private EditText txtUserNameFull;
    private EditText txtUserCountry;
    private Button btnSaveInformation;
    private LinearLayout layout;
    private DrawerLayout mainLayout;
    private CircleImageView myProfileImageView;
    private String currentUserId;
    private ProgressDialog myProgressDialog;
    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;
    private StorageReference userProfileImageRef;
    private StorageReference storage;
    final static int Gallery_Pick = 1;

    //FIELD'S URI
    Uri imageHoldUri = null;

    //FIREBASE FIELD'S
    private FirebaseAuth myAuth;
    private DatabaseReference myDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_information);

        //ASSING ID'S
        txtUserName = findViewById(R.id.txtInformationUserName);
        txtUserNameFull = findViewById(R.id.txtInformationName);
        txtUserCountry = findViewById(R.id.txtInformationCountry);
        btnSaveInformation = findViewById(R.id.btnInformationSave);
        myProfileImageView = findViewById(R.id.myCircleImageInformation);
        layout = findViewById(R.id.LayoutUserProfile);
        mainLayout = findViewById(R.id.myDrawerLayoutMain);

        //PROGRESS DIALOG
        myProgressDialog = new ProgressDialog(this);

        //FIREBASE INSTANCE
        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        myDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        //SAVE INFORMATION ONCLICK
        btnSaveInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveInformationAccount();
            }
        });

        //DATABASE REF
        myDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(UserProfileInformationActivity.this).load(image).placeholder(R.drawable.profile).into(myProfileImageView);
                    }
                    else
                    {
                        Toast.makeText(UserProfileInformationActivity.this, "Primero Seleccione Una Imagen De Perfil.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //IMAGE ONCLICK
        myProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profilePicSelection();
            }
        });

        myDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(UserProfileInformationActivity.this).load(image).placeholder(R.drawable.profile).into(myProfileImageView);

//                        String url = "https://firebasestorage.googleapis.com/v0/b/mysocialnework.appspot.com/o/Profile%20Images%2F6UiSQv1Y2KUKUffDB9QteGquh4S2.jpg?alt=media&token=e0b5a578-081d-4a65-b768-6aed7dd22ef4";
//                        Glide.with(UserProfileInformationActivity.this).load(image).into(myProfileImageView);
                    }
                    else
                    {
                        Toast.makeText(UserProfileInformationActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //SAVE URI FROM GALLERY
        if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }else if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ){
            //SAVE URI FROM CAMERA

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }


        //image crop library code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                myProgressDialog.setTitle("Estableciendo Imagen de Perfil");
                myProgressDialog.setMessage("Espere un momento...");
                myProgressDialog.show();
                myProgressDialog.setCanceledOnTouchOutside(true);

                imageHoldUri = result.getUri();
                StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(imageHoldUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(UserProfileInformationActivity.this, "Imagen Guardada Exitosamente! en firebase Storage", Snackbar.LENGTH_LONG).show();
                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            myDatabaseReference.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if(task.isSuccessful()){

                                        Intent selfIntent = new Intent(UserProfileInformationActivity.this, UserProfileInformationActivity.class);
                                        startActivity(selfIntent);
                                        Toast.makeText(UserProfileInformationActivity.this, "Imagen Guardada Exitosamente en Firebase Databse", Snackbar.LENGTH_LONG).show();
                                        myProgressDialog.dismiss();
                                    } else{
                                        String messageError = task.getException().getMessage();
                                        Toast.makeText(UserProfileInformationActivity.this, "Error: "+messageError, Snackbar.LENGTH_LONG).show();
                                        myProgressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });

                myProfileImageView.setImageURI(imageHoldUri);
            } else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }
    }


    private void profilePicSelection() {
        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

        final CharSequence[] items = {"Capturar", "Desde la galeria",
                "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileInformationActivity.this);
        builder.setTitle("Agrega Tu Foto!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Capturar")) {
                    cameraIntent();
                } else if (items[item].equals("Desde la galeria")) {
                    galleryIntent();
                } else if (items[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        //CHOOSE CAMERA
        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        //CHOOSE IMAGE FROM GALLERY
        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    private void SaveInformationAccount() {
        String userName = txtUserName.getText().toString();
        String userFullName = txtUserNameFull.getText().toString();
        String userCountry = txtUserCountry.getText().toString();

        if(TextUtils.isEmpty(userName)){
            Snackbar.make(layout, "Debe Ingresar Su Nombre De Usuario...", Snackbar.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userFullName)){
            Snackbar.make(layout, "Debe Ingresar Su Nombre Completo...", Snackbar.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userCountry)){
            Snackbar.make(layout, "Debe Ingresar Su Pais...", Snackbar.LENGTH_SHORT).show();
        }
        else{
            myProgressDialog.setTitle("Guardando Información");
            myProgressDialog.setMessage("Espere un momento...");
            myProgressDialog.show();
            myProgressDialog.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", userName);
            userMap.put("fullname", userFullName);
            userMap.put("country", userCountry);
            userMap.put("status", "Estoy usando esta red social");
            userMap.put("gender", "Vacio");
            userMap.put("dob", "Vacio");
            userMap.put("relationshipstatus", "Vacio");
            myDatabaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        UserToMain();
                        Toast.makeText(UserProfileInformationActivity.this, "Cuenta Creada Exitosamente!", Toast.LENGTH_SHORT).show();
                        myProgressDialog.dismiss();
                    }
                    else{
                        String messageError = task.getException().getMessage();
                        Snackbar.make(layout, "Error: "+ messageError, Snackbar.LENGTH_LONG).show();
                        myProgressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void UserToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
