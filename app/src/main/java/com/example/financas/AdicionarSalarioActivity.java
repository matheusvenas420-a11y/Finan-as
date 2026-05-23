package com.example.financas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financas.model.Salario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdicionarSalarioActivity extends AppCompatActivity {

    private EditText edtValorSalario;
    private Button btnSalvarSalario;
    private View btnVoltar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean atualizandoValor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_salario);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        iniciarComponentes();
        configurarMascaraValor();
        configurarCliques();
    }

    private void iniciarComponentes() {
        edtValorSalario = findViewById(R.id.edtValorSalario);
        btnSalvarSalario = findViewById(R.id.btnSalvarSalario);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void configurarCliques() {
        btnVoltar.setOnClickListener(view -> finish());
        btnSalvarSalario.setOnClickListener(view -> salvarSalario());
    }

    private void configurarMascaraValor() {
        edtValorSalario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (atualizandoValor) {
                    return;
                }

                String apenasDigitos = editable.toString().replaceAll("[^0-9]", "");

                if (apenasDigitos.isEmpty()) {
                    return;
                }

                atualizandoValor = true;

                double valor = Long.parseLong(apenasDigitos) / 100.0;
                String valorFormatado = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor)
                        .replace(".", ",");

                edtValorSalario.setText(valorFormatado);
                edtValorSalario.setSelection(valorFormatado.length());

                atualizandoValor = false;
            }
        });
    }

    private void salvarSalario() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            mostrarMensagem("Faca login novamente");
            return;
        }

        double valor = converterValor(edtValorSalario.getText().toString());

        if (valor <= 0) {
            mostrarMensagem("Informe um valor valido");
            return;
        }

        Date dataAtual = new Date();
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(dataAtual);

        int mes = calendario.get(Calendar.MONTH) + 1;
        int ano = calendario.get(Calendar.YEAR);
        String mesAno = String.format(Locale.getDefault(), "%04d-%02d", ano, mes);

        Salario salario = new Salario(valor, dataAtual, mes, ano, mesAno);

        db.collection("usuarios")
                .document(usuarioAtual.getUid())
                .collection("salarios")
                .document(mesAno)
                .set(salario)
                .addOnSuccessListener(unused -> {
                    mostrarMensagem("Salario salvo");
                    finish();
                })
                .addOnFailureListener(e -> mostrarMensagem("Erro ao salvar salario"));
    }

    private double converterValor(String valorTexto) {
        try {
            String apenasDigitos = valorTexto.replaceAll("[^0-9]", "");

            if (apenasDigitos.isEmpty()) {
                return 0;
            }

            return Long.parseLong(apenasDigitos) / 100.0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void mostrarMensagem(String mensagem) {
        Snackbar.make(btnSalvarSalario, mensagem, Snackbar.LENGTH_SHORT).show();
    }
}
