package com.example.financas;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.example.financas.model.Usuario;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity extends AppCompatActivity {

    EditText nome, email, senha, confirmarSenha;
    Button cadastrar;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        nome = findViewById(R.id.edtNome);
        email = findViewById(R.id.edtEmail);
        senha = findViewById(R.id.edtSenha);
        confirmarSenha = findViewById(R.id.edtConfirmarSenha);

        cadastrar = findViewById(R.id.btnCadastrar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    public void cadastrar(View v){

        String txtNome = nome.getText().toString();
        String txtEmail = email.getText().toString();
        String txtSenha = senha.getText().toString();
        String txtConfirmarSenha = confirmarSenha.getText().toString();

        if(txtNome.isEmpty() ||
                txtEmail.isEmpty() ||
                txtSenha.isEmpty() ||
                txtConfirmarSenha.isEmpty()){

            Snackbar snac = Snackbar.make(
                    v,
                    "Preencha todos os campos",
                    Snackbar.LENGTH_SHORT
            );

            snac.setBackgroundTint(Color.BLACK);
            snac.setTextColor(Color.WHITE);
            snac.show();

        } else if(!txtSenha.equals(txtConfirmarSenha)){

            Snackbar snac = Snackbar.make(
                    v,
                    "As senhas são diferentes",
                    Snackbar.LENGTH_SHORT
            );

            snac.setBackgroundTint(Color.BLACK);
            snac.setTextColor(Color.WHITE);
            snac.show();

        } else {

            cadastrarUsuario(v);

        }

    }

    private void cadastrarUsuario(View v){

        String txtNome = nome.getText().toString();
        String txtEmail = email.getText().toString();
        String txtSenha = senha.getText().toString();

        auth.createUserWithEmailAndPassword(txtEmail, txtSenha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            String idUsuario = auth
                                    .getCurrentUser()
                                    .getUid();

                            Usuario usuario = new Usuario(txtNome, txtEmail);

                            db.collection("usuarios")
                                    .document(idUsuario)
                                    .set(usuario);

                            Intent intent = new Intent(
                                    CadastroActivity.this,
                                    MainActivity.class
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            String erro;

                            try {
                                throw task.getException();
                            } catch (Exception e){
                                erro = "Erro ao cadastrar";
                            }

                            Snackbar snac = Snackbar.make(
                                    v,
                                    erro,
                                    Snackbar.LENGTH_SHORT
                            );

                            snac.setBackgroundTint(Color.BLACK);
                            snac.setTextColor(Color.WHITE);
                            snac.show();

                        }

                    }
                });

    }

    public void voltar(View v){

        Intent intent = new Intent(
                CadastroActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();

    }
}
