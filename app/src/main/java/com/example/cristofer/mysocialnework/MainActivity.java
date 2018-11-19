package com.example.cristofer.mysocialnework;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.cristofer.mysocialnework.Post.PostActivity;
import com.example.cristofer.mysocialnework.Post.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import com.example.cristofer.mysocialnework.ProfileUser.UserProfileInformationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity{



    //FIELD'S
    private NavigationView myNavigationView;
    private DrawerLayout myDrawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView myListPost;
    private Toolbar myToolBar;
    private ImageButton btnAddNewPost;
    private CircleImageView navCircleImage;
    private static LocationManager locationManager;
    private TextView navProfileName;
    private static TextView tvLocationCity;
    double lon;
    double lat;
    private TextView textDate;
    private TextView textTime;

    //FIREBASE
    private FirebaseAuth myAuth;
    private DatabaseReference myDatabaseReference;
    private DatabaseReference postsRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FIREBASE INSTACE
        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        myDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Post");

        //ASSING ID'S
        myNavigationView = findViewById(R.id.myNavigationViewMain);
        myDrawerLayout = findViewById(R.id.myDrawerLayoutMain);
        myListPost = findViewById(R.id.myUsersPostList);
        myToolBar = findViewById(R.id.toolbarMain);
        btnAddNewPost = findViewById(R.id.btnAddNewPost);
        textDate = findViewById(R.id.textDate);
        tvLocationCity = findViewById(R.id.textLocationCity);

        //ACTION BAR SUPPORT
        setSupportActionBar(myToolBar);
        getSupportActionBar().setTitle("Inicio");


        //DRAWER TOOGLE
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, myDrawerLayout,R.string.drawer_open, R.string.drawer_close);
        myDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //RECYCLE VIEW
        myListPost.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =  new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myListPost.setLayoutManager(linearLayoutManager);

        //INFLATING NAV_VIEW
        View nv = myNavigationView.inflateHeaderView(R.layout.nav_header);
        navCircleImage = nv.findViewById(R.id.nav_myCircleImageView);
        navProfileName = nv.findViewById(R.id.nav_tvUserName);

        //DATABASE REFERENCE
        myDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("fullname")){
                        String fullName = dataSnapshot.child("fullname").getValue().toString();
                        navProfileName.setText(fullName);
                    }

                    if (dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navCircleImage);
                    } else{
                        Snackbar.make(myDrawerLayout,"El Nombre De Perfil No Existe...", Snackbar.LENGTH_LONG).show();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //LISTERNER NAVEGATION VIEW
        myNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                MenuSelector(menuItem);
                return false;
            }
        });

        //LISTERNER BTN NEW POST
        btnAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserToPost();
                hereLocation();
            }
        });

        DisplayAllUsersPost();
    }

    private void DisplayAllUsersPost() {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (
                        Posts.class,
                        R.layout.all_post_layout,
                        PostsViewHolder.class,
                        postsRef
                ) {

            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts posts, int i) {
                viewHolder.setFullname(posts.getFullname());
                viewHolder.setDate(posts.getDate());
                viewHolder.setDescription(posts.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), posts.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(), posts.getPostimage());

            }
        };
        myListPost.setAdapter(firebaseRecyclerAdapter);
    }

    public static  class PostsViewHolder extends RecyclerView.ViewHolder{

        View myView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        public void setFullname(String fullname)
        {
            TextView username =  myView.findViewById(R.id.profilePostUserName);
            username.setText(fullname);
        }

        public void setProfileimage(Context context, String profileimage)
        {
            CircleImageView image =  myView.findViewById(R.id.postProfileImage);
            Picasso.with(context).load(profileimage).into(image);
        }

        public void set(View myView) {
        }

        public void setDate(String date)
        {
            TextView PostDate = myView.findViewById(R.id.textDate);
            PostDate.setText("  " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = myView.findViewById(R.id.textDescription);
            PostDescription.setText(description);
        }

        public void setPostimage(Context context1,  String postimage)
        {
            ImageView PostImage = myView.findViewById(R.id.postImage);
            Picasso.with(context1).load(postimage).into(PostImage);
        }

        public void setLocation(String location){
            TextView tvLocation = myView.findViewById(R.id.textLocationCity);
            tvLocation.setText(location);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = myAuth.getCurrentUser();
        if(currentUser == null){
            UserToLogin();
        } else{
            CheckUserExistence();
        }
    }

    private void hereLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            //TODO

            return ;
        }

        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        onLocationChanged(location);
        loc_finc(location);

    }

    private void onLocationChanged(Location location) {
        lon = location.getLongitude();
        lat = location.getLatitude();
        tvLocationCity.setText("Long: "+lon +"Lat: " + lat);
    }

    private void loc_finc(Location location){
        try{
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String country = addresses.get(0).getCountryName();
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            tvLocationCity.setText("City: " +city);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void UserToPost() {
        Intent addPostIntent = new Intent(this, PostActivity.class);
        startActivity(addPostIntent);
    }

    private void CheckUserExistence() {
        final String currentUserId = myAuth.getCurrentUser().getUid();

        myDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId)){
                    UserToProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void UserToProfile() {
        Intent intent = new Intent(this, UserProfileInformationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void UserToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void MenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.navegation_post:
                UserToPost();
                break;

            case R.id.navegation_profile:
                Snackbar.make(myDrawerLayout, "Perfil", Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.navegation_home:
                Snackbar.make(myDrawerLayout, "Inicio", Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.navegation_friends:
                Snackbar.make(myDrawerLayout, "Amigos", Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.navegation_find_friends:
                Snackbar.make(myDrawerLayout, "Buscando Amigos", Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.navegation_messages:
                Snackbar.make(myDrawerLayout, "Mensajes", Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.navegation_settings:
                Snackbar.make(myDrawerLayout, "Ajustes", Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.navegation_logout:
                myAuth.signOut();
                UserToLogin();
                break;
        }

    }



}
