package com.example.financas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financas.model.Gasto;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

public class AdicionarGastoActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {
            "Moradia",
            "Alimentacao",
            "Transporte",
            "Compras",
            "Lazer",
            "Outros"
    };
    private static final String[] FORMAS_PAGAMENTO = {
            "Pix",
            "Dinheiro",
            "Débito",
            "Crédito"
    };

    private EditText edtDescricaoGasto;
    private EditText edtValorGasto;
    private EditText edtParcelas;
    private Spinner spCategoriaGasto;
    private Spinner spFormaPagamento;
    private LinearLayout containerParcelas;
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
        configurarFormaPagamento();
        configurarCliques();
    }

    private void iniciarComponentes() {
        edtDescricaoGasto = findViewById(R.id.edtDescricaoGasto);
        edtValorGasto = findViewById(R.id.edtValorGasto);
        edtParcelas = findViewById(R.id.edtParcelas);
        spCategoriaGasto = findViewById(R.id.spCategoriaGasto);
        spFormaPagamento = findViewById(R.id.spFormaPagamento);
        containerParcelas = findViewById(R.id.containerParcelas);
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

    private void configurarFormaPagamento() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                FORMAS_PAGAMENTO
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormaPagamento.setAdapter(adapter);
        spFormaPagamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean pagamentoCredito = "Crédito".equals(FORMAS_PAGAMENTO[position]);
                containerParcelas.setVisibility(pagamentoCredito ? View.VISIBLE : View.GONE);

                if (!pagamentoCredito) {
                    edtParcelas.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                containerParcelas.setVisibility(View.GONE);
            }
        });
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
        String formaPagamento = spFormaPagamento.getSelectedItem().toString();
        int parcelas = obterParcelas(formaPagamento);

        if (descricao.isEmpty() || valorTexto.isEmpty()) {
            mostrarMensagem("Preencha todos os campos");
            return;
        }

        double valor = converterValor(valorTexto);

        if (valor <= 0) {
            mostrarMensagem("Informe um valor valido");
            return;
        }

        if ("Crédito".equals(formaPagamento) && parcelas <= 0) {
            mostrarMensagem("Informe a quantidade de parcelas");
            return;
        }

        Date dataAtual = new Date();
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(dataAtual);

        int mes = calendario.get(Calendar.MONTH) + 1;
        int ano = calendario.get(Calendar.YEAR);
        String mesAno = String.format(Locale.getDefault(), "%04d-%02d", ano, mes);

        Gasto gasto = new Gasto(
                descricao,
                valor,
                categoria,
                formaPagamento,
                parcelas,
                dataAtual,
                mes,
                ano,
                mesAno
        );

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

    private int obterParcelas(String formaPagamento) {
        if (!"Crédito".equals(formaPagamento)) {
            return 1;
        }

        try {
            String textoParcelas = edtParcelas.getText().toString().trim();

            if (textoParcelas.isEmpty()) {
                return 0;
            }

            return Integer.parseInt(textoParcelas);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void mostrarMensagem(String mensagem) {
        Snackbar.make(btnSalvarGasto, mensagem, Snackbar.LENGTH_SHORT).show();
    }
}
