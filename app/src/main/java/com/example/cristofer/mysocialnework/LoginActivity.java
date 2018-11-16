package com.example.cristofer.mysocialnework;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText txtLoginEmail;
    private EditText txtLoginPassword;
    private Button btnSignup;
    private ImageButton btnLogin;
    private RelativeLayout layout;
    private ProgressDialog myProgressDialog;

    //FIREBASE FIELD'S
    FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ASSING ID'S
        txtLoginEmail = findViewById(R.id.txtLoginEmail);
        txtLoginPassword = findViewById(R.id.txtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.loginBtnSignup);
        layout = findViewById(R.id.LoginLayout);

        //PROGRESS DIALOG INSTANCE
        myProgressDialog = new ProgressDialog(this);

        //FIREBASE INSTANCE
        myAuth = FirebaseAuth.getInstance();

        //SIGNUP ONCLICK
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserToRegister();
            }
        });

        //LOGIN ONCLICK
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowingUserToLogin();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = myAuth.getCurrentUser();
        if(currentUser != null){
            UserToMain();
        }
    }

    private void AllowingUserToLogin() {
        String email = txtLoginEmail.getText().toString();
        String pass = txtLoginPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Snackbar.make(layout, "Debe Ingresar Su Email...", Snackbar.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(pass)){
            Snackbar.make(layout, "Debe Ingresar Su Contraseña...", Snackbar.LENGTH_SHORT).show();
        }
        else{
            myProgressDialog.setTitle("Iniciando Sesión");
            myProgressDialog.setMessage("Espere un momento...");
            myProgressDialog.show();
            myProgressDialog.setCanceledOnTouchOutside(true);

            myAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        UserToMain();
                        myProgressDialog.dismiss();

                    } else{
                        String messageError = task.getException().getMessage();
                        Snackbar.make(layout, "Error: "+ messageError, Snackbar.LENGTH_SHORT).show();
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

    private void UserToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
