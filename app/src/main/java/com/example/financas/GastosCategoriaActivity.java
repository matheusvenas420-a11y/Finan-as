package com.example.financas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GastosCategoriaActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORIA = "categoria";

    private TextView tvTituloCategoria;
    private TextView tvSubtituloCategoria;
    private TextView tvListaVazia;
    private LinearLayout containerGastos;
    private View btnVoltar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration gastosListener;
    private NumberFormat formatoMoeda;
    private SimpleDateFormat formatoData;
    private String categoria;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos_categoria);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        formatoData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);

        if (categoria == null || categoria.isEmpty()) {
            categoria = "Outros";
        }

        iniciarComponentes();
        configurarTela();
        observarGastosCategoria();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gastosListener != null) {
            gastosListener.remove();
        }
    }

    private void iniciarComponentes() {
        tvTituloCategoria = findViewById(R.id.tvTituloCategoria);
        tvSubtituloCategoria = findViewById(R.id.tvSubtituloCategoria);
        tvListaVazia = findViewById(R.id.tvListaVazia);
        containerGastos = findViewById(R.id.containerGastos);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void configurarTela() {
        tvTituloCategoria.setText(categoria);
        tvSubtituloCategoria.setText("Gastos de " + categoria + " no mes atual, organizados por data.");
        btnVoltar.setOnClickListener(view -> finish());
    }

    private void observarGastosCategoria() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            finish();
            return;
        }

        gastosListener = db.collection("usuarios")
                .document(usuarioAtual.getUid())
                .collection("gastos")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        return;
                    }

                    List<DocumentSnapshot> gastos = new ArrayList<>();
                    String mesAnoAtual = obterMesAnoAtual();

                    for (DocumentSnapshot documento : snapshot.getDocuments()) {
                        String categoriaGasto = documento.getString("categoria");
                        String mesAno = documento.getString("mesAno");

                        if (categoria.equals(categoriaGasto) && mesAnoAtual.equals(mesAno)) {
                            gastos.add(documento);
                        }
                    }

                    gastos.sort(Comparator.comparing(
                            documento -> {
                                Date data = documento.getDate("data");
                                return data != null ? data : new Date(0);
                            },
                            Comparator.reverseOrder()
                    ));

                    preencherLista(gastos);
                });
    }

    private void preencherLista(List<DocumentSnapshot> gastos) {
        containerGastos.removeAllViews();

        if (gastos.isEmpty()) {
            tvListaVazia.setVisibility(View.VISIBLE);
            return;
        }

        tvListaVazia.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (DocumentSnapshot gasto : gastos) {
            View item = inflater.inflate(R.layout.item_gasto_categoria, containerGastos, false);

            TextView tvNomeGasto = item.findViewById(R.id.tvNomeGasto);
            TextView tvValorGasto = item.findViewById(R.id.tvValorGasto);
            TextView tvDataGasto = item.findViewById(R.id.tvDataGasto);
            TextView tvFormaPagamento = item.findViewById(R.id.tvFormaPagamento);
            TextView tvParcelas = item.findViewById(R.id.tvParcelas);

            String descricao = gasto.getString("descricao");
            Double valor = gasto.getDouble("valor");
            Date data = gasto.getDate("data");
            String formaPagamento = gasto.getString("formaPagamento");
            Long parcelas = gasto.getLong("parcelas");

            tvNomeGasto.setText(descricao != null ? descricao : "Gasto");
            tvValorGasto.setText(formatarMoeda(valor != null ? valor : 0));
            tvDataGasto.setText("Data: " + (data != null ? formatoData.format(data) : "-"));
            tvFormaPagamento.setText("Pagamento: " + (formaPagamento != null ? formaPagamento : "-"));

            if (parcelas != null && parcelas > 1) {
                tvParcelas.setVisibility(View.VISIBLE);
                tvParcelas.setText("Parcelas: " + parcelas);
            } else {
                tvParcelas.setVisibility(View.GONE);
            }

            item.setOnClickListener(view -> mostrarOpcoesGasto(gasto));
            containerGastos.addView(item);
        }
    }

    private void mostrarOpcoesGasto(DocumentSnapshot gasto) {
        String descricao = gasto.getString("descricao");

        new AlertDialog.Builder(this)
                .setTitle(descricao != null ? descricao : "Gasto")
                .setItems(new String[]{"Deletar gasto"}, (dialog, which) -> deletarGasto(gasto))
                .show();
    }

    private void deletarGasto(DocumentSnapshot gasto) {
        gasto.getReference().delete();
    }

    private String obterMesAnoAtual() {
        Calendar calendario = Calendar.getInstance();
        int mes = calendario.get(Calendar.MONTH) + 1;
        int ano = calendario.get(Calendar.YEAR);
        return String.format(Locale.getDefault(), "%04d-%02d", ano, mes);
    }

    private String formatarMoeda(double valor) {
        return formatoMoeda.format(valor);
    }
}
