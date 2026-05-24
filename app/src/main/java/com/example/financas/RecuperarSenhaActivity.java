package com.example.financas;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private EditText edtEmailRecuperacao;
    private Button btnEnviarRecuperacao;
    private View btnVoltar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        edtEmailRecuperacao = findViewById(R.id.edtEmailRecuperacao);
        btnEnviarRecuperacao = findViewById(R.id.btnEnviarRecuperacao);
        btnVoltar = findViewById(R.id.btnVoltar);

        String emailPreenchido = getIntent().getStringExtra("email");
        if (emailPreenchido != null) {
            edtEmailRecuperacao.setText(emailPreenchido);
        }

        btnVoltar.setOnClickListener(view -> finish());
        btnEnviarRecuperacao.setOnClickListener(view -> enviarRecuperacao());
    }

    private void enviarRecuperacao() {
        String email = edtEmailRecuperacao.getText().toString().trim();

        if (email.isEmpty()) {
            mostrarMensagem("Informe seu email");
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mostrarMensagem("Link de recuperação enviado para seu email");
                    } else {
                        mostrarMensagem("Erro ao enviar email de recuperação");
                    }
                });
    }

    private void mostrarMensagem(String mensagem) {
        Snackbar snac = Snackbar.make(btnEnviarRecuperacao, mensagem, Snackbar.LENGTH_SHORT);
        snac.setBackgroundTint(Color.BLACK);
        snac.setTextColor(Color.WHITE);
        snac.show();
    }
}
