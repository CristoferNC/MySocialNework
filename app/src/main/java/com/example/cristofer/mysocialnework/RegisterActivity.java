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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.cristofer.mysocialnework.ProfileUser.UserProfileInformationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //FIELD'S
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtPasswordConfirm;
    private ImageButton btnSignup;
    private Button btnSignin;
    private RelativeLayout layout;
    private ProgressDialog myProgressDialog;
    private CheckBox myCheck;

    //FIREBASE FIELD'S
    FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ASSING ID'S
        txtEmail = findViewById(R.id.txtRegisterEmail);
        txtPassword = findViewById(R.id.txtRegisterPassword);
        txtPasswordConfirm = findViewById(R.id.txtRegisterPasswordConfirm);
        btnSignup = findViewById(R.id.registerBtnSignup);
        btnSignin = findViewById(R.id.btnSignin);
        layout = findViewById(R.id.RegisterLayout);
        myCheck = findViewById(R.id.myCheckbox);

        //PROGRESS DIALOG INSTANCE
        myProgressDialog = new ProgressDialog(this);

        //FIREBASE INSTACE
        myAuth = FirebaseAuth.getInstance();

        //ONCLICK LISTENER
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
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

    private void createNewAccount() {
        //FIELD'S
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String passwordConfirm = txtPasswordConfirm.getText().toString();

        if(TextUtils.isEmpty(email)){
            Snackbar.make(layout, "Debe Ingresar Su Email...", Snackbar.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Snackbar.make(layout, "Debe Ingresar Su Contraseña...", Snackbar.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(passwordConfirm)){
            Snackbar.make(layout, "Debe Confirmar Su Contraseña...", Snackbar.LENGTH_SHORT).show();
        }
        else if(!password.equals(passwordConfirm) ){
            Snackbar.make(layout, "Su Contraseña Debe Ser igual En Ambos Campos...", Snackbar.LENGTH_SHORT).show();
        }
        else{
            myProgressDialog.setTitle("Creando Cuenta");
            myProgressDialog.setMessage("Espere un momento...");
            myProgressDialog.show();
            myProgressDialog.setCanceledOnTouchOutside(true);

            myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        UserToProfile();
                        Snackbar.make(layout, "Cuenta Creada Exitosamente!", Snackbar.LENGTH_LONG).show();
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


    private void UserToProfile() {
        Intent intent = new Intent(this, UserProfileInformationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
