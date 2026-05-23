package com.example.financas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdicionarGastoActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {
            "Moradia",
            "Alimentacao",
            "Transporte",
            "Compras",
            "Lazer",
            "Outros"
    };

    private EditText edtDescricaoGasto;
    private EditText edtValorGasto;
    private Spinner spCategoriaGasto;
    private Button btnSalvarGasto;
    private View btnVoltar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean atualizandoValor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_gasto);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        iniciarComponentes();
        configurarMascaraValor();
        configurarCategoria();
        configurarCliques();
    }

    private void iniciarComponentes() {
        edtDescricaoGasto = findViewById(R.id.edtDescricaoGasto);
        edtValorGasto = findViewById(R.id.edtValorGasto);
        spCategoriaGasto = findViewById(R.id.spCategoriaGasto);
        btnSalvarGasto = findViewById(R.id.btnSalvarGasto);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void configurarCategoria() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CATEGORIAS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoriaGasto.setAdapter(adapter);
    }

    private void configurarMascaraValor() {
        edtValorGasto.addTextChangedListener(new TextWatcher() {
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

                edtValorGasto.setText(valorFormatado);
                edtValorGasto.setSelection(valorFormatado.length());

                atualizandoValor = false;
            }
        });
    }

    private void configurarCliques() {
        btnVoltar.setOnClickListener(view -> finish());
        btnSalvarGasto.setOnClickListener(view -> salvarGasto());
    }

    private void salvarGasto() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            mostrarMensagem("Faca login novamente");
            return;
        }

        String descricao = edtDescricaoGasto.getText().toString().trim();
        String valorTexto = edtValorGasto.getText().toString().trim();
        String categoria = spCategoriaGasto.getSelectedItem().toString();

        if (descricao.isEmpty() || valorTexto.isEmpty()) {
            mostrarMensagem("Preencha todos os campos");
            return;
        }

        double valor = converterValor(valorTexto);

        if (valor <= 0) {
            mostrarMensagem("Informe um valor valido");
            return;
        }

        Date dataAtual = new Date();
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(dataAtual);

        int mes = calendario.get(Calendar.MONTH) + 1;
        int ano = calendario.get(Calendar.YEAR);

        Map<String, Object> gasto = new HashMap<>();
        gasto.put("descricao", descricao);
        gasto.put("valor", valor);
        gasto.put("categoria", categoria);
        gasto.put("data", dataAtual);
        gasto.put("mes", mes);
        gasto.put("ano", ano);
        gasto.put("mesAno", String.format("%04d-%02d", ano, mes));

        db.collection("usuarios")
                .document(usuarioAtual.getUid())
                .collection("gastos")
                .add(gasto)
                .addOnSuccessListener(documentReference -> {
                    mostrarMensagem("Gasto adicionado");
                    finish();
                })
                .addOnFailureListener(e -> mostrarMensagem("Erro ao adicionar gasto"));
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
        Snackbar.make(btnSalvarGasto, mensagem, Snackbar.LENGTH_SHORT).show();
    }
}
