package com.example.cristofer.mysocialnework.Post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.cristofer.mysocialnework.MainActivity;
import com.example.cristofer.mysocialnework.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private ImageButton btnPostImage;
    private TextView tvPostDescripcion;
    private Button btnAddPost;
    private static final int Gallery_Pick = 1;
    private Uri imageUri;
    private ProgressDialog myProgressDialog;
    private String Description;
    private String saveCurrentDate;
    private String saveCurrentTime;
    private String postRandomName;
    private String downloadUrl;
    private String current_user_id;
    private  LocationManager locationManager;
    double lon;
    double lat;
    private TextView tvPostLocation;
    private Button btnAddLocationPost;

    //FIREBASE FIELD'S
    private FirebaseAuth myAuth;
    private StorageReference postImagesReference;
    private DatabaseReference userRef;
    private DatabaseReference postReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //ASING ID'S
        myToolbar = findViewById(R.id.updatePostToolbar);
        btnPostImage = findViewById(R.id.btnPostImage);
        tvPostDescripcion = findViewById(R.id.tvPostDescription);
        btnAddPost = findViewById(R.id.btnAddPost);

        //FIREBASE INSTANCE
        myAuth = FirebaseAuth.getInstance();
        postImagesReference = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Post");
        current_user_id = myAuth.getCurrentUser().getUid();


        //TOOLBAR
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Actualizar Post");

        //PROGRESS INSTANCE
        myProgressDialog = new ProgressDialog(this);

        //ONCLICK IMAGEBUTTON

        btnPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        Description = tvPostDescripcion.getText().toString();

        if(imageUri == null)
        {
            Toast.makeText(this, "Por favor seleccione una imagen para postear...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            myProgressDialog.setTitle("Añadiendo Post");
            myProgressDialog.setMessage("Espere un momento...");
            myProgressDialog.show();
            myProgressDialog.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();

        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = postImagesReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw  task.getException();
                        }
                        downloadUrl = filePath.getDownloadUrl().toString();
                        return  filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(Task<Uri> task) {
                        downloadUrl = task.getResult().toString();
                        Toast.makeText(PostActivity.this, "Url: "+ downloadUrl, Toast.LENGTH_SHORT).show();
                        SavingPostInformation();
                    }
                });
            }
        });
    }

    private void SavingPostInformation() {

        //LOCATION
        //DATABASE
        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {


                if(dataSnapshot.exists())
                {

                    //SAVE INFORMATION
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    postReference.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        UserToMain();
                                        Toast.makeText(PostActivity.this, "Post Creado Exitosamente!", Toast.LENGTH_SHORT).show();
                                        myProgressDialog.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(PostActivity.this, "Ocurrio Un Error Al Subir El Post.", Toast.LENGTH_SHORT).show();
                                        myProgressDialog.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            btnPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home){
            UserToMain();
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserToMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }
}
