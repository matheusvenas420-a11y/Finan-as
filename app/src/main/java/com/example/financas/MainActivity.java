package com.example.financas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText email, senha;
    Button logar, cadastrar;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email = findViewById(R.id.edtEmail);
        senha = findViewById(R.id.edtSenha);
        logar = findViewById(R.id.btnLogar);
        cadastrar = findViewById(R.id.btnCadastrar);
    }
    public void logar(View v){
        String mail = email.getText().toString();
        String password = senha.getText().toString();

        if(mail.isEmpty() || password.isEmpty()){
            Snackbar snac = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_SHORT);
            snac.setBackgroundTint(Color.BLACK);
            snac.setTextColor(Color.WHITE);
            snac.show();
        } else {
            logarUsuario(v);
        }
    }
    private void logarUsuario(View v) {
        String mail = email.getText().toString();
        String password = senha.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            irTelaPrincipal(v);
                        } else {
                            String erro;
                            try {
                                throw task.getException();
                            } catch (Exception e){
                                erro = "Email ou senha incorretos";
                            }

                            Snackbar snac = Snackbar.make(v, erro, Snackbar.LENGTH_SHORT);
                            snac.setBackgroundTint(Color.BLACK);
                            snac.setTextColor(Color.WHITE);
                            snac.show();
                        }
                    }
                });
    }
    private void irTelaPrincipal(View v){
        Intent i = new Intent(
                MainActivity.this,
                PrincipalActivity.class
        );
        startActivity(i);
        finish();
    }
    public void irCadastro(View v){
        Intent i2 = new Intent(
                MainActivity.this,
                CadastroActivity.class
        );
        startActivity(i2);
        finish();
    }

    public void esqueciSenha(View v) {
        Intent intent = new Intent(MainActivity.this, RecuperarSenhaActivity.class);
        intent.putExtra("email", email.getText().toString().trim());
        startActivity(intent);
    }

    private void mostrarMensagem(View v, String mensagem) {
        Snackbar snac = Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT);
        snac.setBackgroundTint(Color.BLACK);
        snac.setTextColor(Color.WHITE);
        snac.show();
    }
}
