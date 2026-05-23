package com.example.financas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financas.model.ResumoMensal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrincipalActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {
            "Moradia",
            "Alimentacao",
            "Transporte",
            "Compras",
            "Lazer",
            "Outros"
    };

    private TextView tvSair;
    private Button btnAdicionarGasto;
    private Button btnAdicionarSalario;
    private TextView tvGastoMesAtual;
    private TextView tvGastoMesPassado;
    private TextView tvPercentual;
    private TextView tvDiferenca;
    private TextView tvMaiorCategoria;
    private TextView tvNomeMaiorCategoria;
    private TextView tvPercentMaiorCategoria;
    private TextView tvResumoCategorias;
    private ImageView ivMaiorCategoria;
    private TextView tvEstadoVazio;
    private TextView tvSalarioMes;
    private TextView tvGastosMesTopbar;
    private View resumoCards;
    private View secaoDetalhesGastos;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration gastosListener;
    private ListenerRegistration salarioListener;
    private NumberFormat formatoMoeda;
    private double salarioMesAtual;
    private double totalGastosMesAtual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

        iniciarComponentes();
        configurarCliques();
        observarSalario();
        observarGastos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gastosListener != null) {
            gastosListener.remove();
        }

        if (salarioListener != null) {
            salarioListener.remove();
        }
    }

    private void iniciarComponentes() {
        tvSair = findViewById(R.id.tvSair);
        btnAdicionarGasto = findViewById(R.id.btnAdicionarGasto);
        btnAdicionarSalario = findViewById(R.id.btnAdicionarSalario);
        tvGastoMesAtual = findViewById(R.id.tvGastoMesAtual);
        tvGastoMesPassado = findViewById(R.id.tvGastoMesPassado);
        tvPercentual = findViewById(R.id.tvPercentual);
        tvDiferenca = findViewById(R.id.tvDiferenca);
        tvMaiorCategoria = findViewById(R.id.tvMaiorCategoria);
        tvNomeMaiorCategoria = findViewById(R.id.tvNomeMaiorCategoria);
        tvPercentMaiorCategoria = findViewById(R.id.tvPercentMaiorCategoria);
        tvResumoCategorias = findViewById(R.id.tvResumoCategorias);
        ivMaiorCategoria = findViewById(R.id.ivMaiorCategoria);
        tvEstadoVazio = findViewById(R.id.tvEstadoVazio);
        tvSalarioMes = findViewById(R.id.tvSalarioMes);
        tvGastosMesTopbar = findViewById(R.id.tvGastosMesTopbar);
        resumoCards = findViewById(R.id.resumoCards);
        secaoDetalhesGastos = findViewById(R.id.secaoDetalhesGastos);
    }

    private void configurarCliques() {
        tvSair.setOnClickListener(view -> sair());
        btnAdicionarGasto.setOnClickListener(view -> abrirAdicionarGasto());
        btnAdicionarSalario.setOnClickListener(view -> abrirAdicionarSalario());
        configurarCliqueCategoria(R.id.itemMoradia, "Moradia");
        configurarCliqueCategoria(R.id.itemAlimentacao, "Alimentacao");
        configurarCliqueCategoria(R.id.itemTransporte, "Transporte");
        configurarCliqueCategoria(R.id.itemCompras, "Compras");
        configurarCliqueCategoria(R.id.itemLazer, "Lazer");
        configurarCliqueCategoria(R.id.itemOutros, "Outros");
    }

    private void observarSalario() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            voltarParaLogin();
            return;
        }

        salarioListener = db.collection("usuarios")
                .document(usuarioAtual.getUid())
                .collection("salarios")
                .document(obterMesAnoAtual())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        salarioMesAtual = 0;
                        atualizarSaldoDisponivel();
                        return;
                    }

                    Double salario = snapshot.getDouble("valor");
                    salarioMesAtual = salario != null ? salario : 0;
                    atualizarSaldoDisponivel();
                });
    }

    private void observarGastos() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            voltarParaLogin();
            return;
        }

        gastosListener = db.collection("usuarios")
                .document(usuarioAtual.getUid())
                .collection("gastos")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        return;
                    }

                    if (snapshot.isEmpty()) {
                        mostrarEstadoVazio();
                        return;
                    }

                    double totalMesAtual = 0;
                    double totalMesPassado = 0;
                    boolean temGastoNoMesAtual = false;
                    Map<String, Double> totaisPorCategoria = criarMapaCategorias();

                    Date inicioMesAtual = obterInicioMes(0);
                    Date inicioProximoMes = obterInicioMes(1);
                    Date inicioMesPassado = obterInicioMes(-1);

                    for (DocumentSnapshot documento : snapshot.getDocuments()) {
                        Double valor = documento.getDouble("valor");
                        Date data = documento.getDate("data");
                        String categoria = documento.getString("categoria");

                        if (valor == null || data == null) {
                            continue;
                        }

                        if (data.compareTo(inicioMesAtual) >= 0 && data.compareTo(inicioProximoMes) < 0) {
                            temGastoNoMesAtual = true;
                            totalMesAtual += valor;
                            somarCategoria(totaisPorCategoria, categoria, valor);
                        } else if (data.compareTo(inicioMesPassado) >= 0 && data.compareTo(inicioMesAtual) < 0) {
                            totalMesPassado += valor;
                        }
                    }

                    if (!temGastoNoMesAtual) {
                        mostrarEstadoVazio();
                        return;
                    }

                    mostrarEstadoComGastos();
                    atualizarResumo(totalMesAtual, totalMesPassado, totaisPorCategoria);
                });
    }

    private void mostrarEstadoVazio() {
        tvEstadoVazio.setVisibility(View.VISIBLE);
        resumoCards.setVisibility(View.GONE);
        secaoDetalhesGastos.setVisibility(View.GONE);
        totalGastosMesAtual = 0;
        tvGastosMesTopbar.setText("Gastos: " + formatarMoeda(0));
        atualizarSaldoDisponivel();
    }

    private void mostrarEstadoComGastos() {
        tvEstadoVazio.setVisibility(View.GONE);
        resumoCards.setVisibility(View.VISIBLE);
        secaoDetalhesGastos.setVisibility(View.VISIBLE);
    }

    private Map<String, Double> criarMapaCategorias() {
        Map<String, Double> totais = new HashMap<>();

        for (String categoria : CATEGORIAS) {
            totais.put(categoria, 0.0);
        }

        return totais;
    }

    private void somarCategoria(Map<String, Double> totaisPorCategoria, String categoria, double valor) {
        String categoriaFinal = totaisPorCategoria.containsKey(categoria) ? categoria : "Outros";
        Double totalAtual = totaisPorCategoria.get(categoriaFinal);
        totaisPorCategoria.put(categoriaFinal, totalAtual + valor);
    }

    private Date obterInicioMes(int deslocamentoMes) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.DAY_OF_MONTH, 1);
        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        calendario.add(Calendar.MONTH, deslocamentoMes);
        return calendario.getTime();
    }

    private String obterMesAnoAtual() {
        Calendar calendario = Calendar.getInstance();
        int mes = calendario.get(Calendar.MONTH) + 1;
        int ano = calendario.get(Calendar.YEAR);
        return String.format(Locale.getDefault(), "%04d-%02d", ano, mes);
    }

    private void atualizarResumo(
            double totalMesAtual,
            double totalMesPassado,
            Map<String, Double> totaisPorCategoria
    ) {
        double diferenca = totalMesAtual - totalMesPassado;
        double percentual = totalMesPassado > 0 ? (diferenca / totalMesPassado) * 100 : 0;
        String maiorCategoria = encontrarMaiorCategoria(totaisPorCategoria);
        double valorMaiorCategoria = totaisPorCategoria.get(maiorCategoria);

        totalGastosMesAtual = totalMesAtual;
        tvGastoMesAtual.setText(formatarMoeda(totalMesAtual));
        tvGastosMesTopbar.setText("Gastos: " + formatarMoeda(totalMesAtual));
        atualizarSaldoDisponivel();
        tvGastoMesPassado.setText(formatarMoeda(totalMesPassado));
        tvDiferenca.setText(formatarMoeda(diferenca));
        tvPercentual.setText(String.format(Locale.getDefault(), "%.0f%%", percentual));
        tvMaiorCategoria.setText(formatarMoeda(valorMaiorCategoria));
        tvNomeMaiorCategoria.setText(maiorCategoria);
        tvPercentMaiorCategoria.setText(" - " + calcularPercentualCategoria(valorMaiorCategoria, totalMesAtual) + "%");
        ivMaiorCategoria.setImageResource(obterIconeCategoria(maiorCategoria));
        tvResumoCategorias.setText("Classificacao por categoria - total de " + formatarMoeda(totalMesAtual));

        atualizarCorComparacao(diferenca);
        atualizarCategorias(totaisPorCategoria, totalMesAtual);
    }

    private void atualizarSaldoDisponivel() {
        double saldoDisponivel = salarioMesAtual - totalGastosMesAtual;
        tvSalarioMes.setText("Disponivel: " + formatarMoeda(saldoDisponivel));
        tvSalarioMes.setTextColor(saldoDisponivel < 0
                ? Color.parseColor("#EF4444")
                : Color.parseColor("#111827"));
        salvarResumoMensal(saldoDisponivel);
    }

    private void salvarResumoMensal(double saldoDisponivel) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();

        if (usuarioAtual == null) {
            return;
        }

        Calendar calendario = Calendar.getInstance();
        int mes = calendario.get(Calendar.MONTH) + 1;
        int ano = calendario.get(Calendar.YEAR);
        String mesAno = obterMesAnoAtual();

        ResumoMensal resumo = new ResumoMensal(
                salarioMesAtual,
                totalGastosMesAtual,
                saldoDisponivel,
                mes,
                ano,
                mesAno,
                new Date()
        );

        db.collection("usuarios")
                .document(usuarioAtual.getUid())
                .collection("resumosMensais")
                .document(mesAno)
                .set(resumo);
    }

    private String encontrarMaiorCategoria(Map<String, Double> totaisPorCategoria) {
        String maiorCategoria = "Outros";
        double maiorValor = -1;

        for (String categoria : CATEGORIAS) {
            Double valor = totaisPorCategoria.get(categoria);

            if (valor != null && valor > maiorValor) {
                maiorValor = valor;
                maiorCategoria = categoria;
            }
        }

        return maiorCategoria;
    }

    private void atualizarCorComparacao(double diferenca) {
        int cor = diferenca <= 0 ? Color.parseColor("#10B981") : Color.parseColor("#EF4444");
        tvDiferenca.setTextColor(cor);
        tvPercentual.setTextColor(cor);
    }

    private int calcularPercentualCategoria(double valorCategoria, double totalMesAtual) {
        return totalMesAtual > 0 ? (int) Math.round((valorCategoria / totalMesAtual) * 100) : 0;
    }

    private void atualizarCategorias(Map<String, Double> totaisPorCategoria, double totalMesAtual) {
        configurarItemCategoria(R.id.itemMoradia, "Moradia", totaisPorCategoria.get("Moradia"), totalMesAtual);
        configurarItemCategoria(R.id.itemAlimentacao, "Alimentacao", totaisPorCategoria.get("Alimentacao"), totalMesAtual);
        configurarItemCategoria(R.id.itemTransporte, "Transporte", totaisPorCategoria.get("Transporte"), totalMesAtual);
        configurarItemCategoria(R.id.itemCompras, "Compras", totaisPorCategoria.get("Compras"), totalMesAtual);
        configurarItemCategoria(R.id.itemLazer, "Lazer", totaisPorCategoria.get("Lazer"), totalMesAtual);
        configurarItemCategoria(R.id.itemOutros, "Outros", totaisPorCategoria.get("Outros"), totalMesAtual);
    }

    private void configurarItemCategoria(int itemId, String nome, Double valor, double totalMesAtual) {
        View item = findViewById(itemId);
        TextView tvNomeCategoria = item.findViewById(R.id.tvNomeCategoria);
        TextView tvValorCategoria = item.findViewById(R.id.tvValorCategoria);
        TextView tvPercentCategoria = item.findViewById(R.id.tvPercentCategoria);
        ImageView ivIconeCategoria = item.findViewById(R.id.ivIconeCategoria);
        View progressFill = item.findViewById(R.id.progressFill);

        double valorSeguro = valor != null ? valor : 0;
        int percentual = totalMesAtual > 0 ? (int) Math.round((valorSeguro / totalMesAtual) * 100) : 0;

        tvNomeCategoria.setText(nome);
        tvValorCategoria.setText(formatarMoeda(valorSeguro));
        tvPercentCategoria.setText(percentual + "%");
        ivIconeCategoria.setImageResource(obterIconeCategoria(nome));
        atualizarBarraCategoria(progressFill, percentual);
    }

    private int obterIconeCategoria(String categoria) {
        if ("Moradia".equals(categoria)) {
            return R.drawable.ic_home;
        } else if ("Alimentacao".equals(categoria)) {
            return R.drawable.ic_food;
        } else if ("Transporte".equals(categoria)) {
            return R.drawable.ic_transport;
        } else if ("Compras".equals(categoria)) {
            return R.drawable.ic_shopping;
        } else if ("Lazer".equals(categoria)) {
            return R.drawable.ic_leisure;
        } else {
            return R.drawable.ic_more;
        }
    }

    private void atualizarBarraCategoria(View progressFill, int percentual) {
        progressFill.post(() -> {
            View parent = (View) progressFill.getParent();
            int larguraTotal = parent.getWidth();
            progressFill.getLayoutParams().width = (larguraTotal * percentual) / 100;
            progressFill.requestLayout();
        });
    }

    private String formatarMoeda(double valor) {
        return formatoMoeda.format(valor);
    }

    private void configurarCliqueCategoria(int itemId, String categoria) {
        View item = findViewById(itemId);
        item.setOnClickListener(view -> abrirGastosCategoria(categoria));
        item.setClickable(true);
    }

    private void abrirGastosCategoria(String categoria) {
        Intent intent = new Intent(PrincipalActivity.this, GastosCategoriaActivity.class);
        intent.putExtra(GastosCategoriaActivity.EXTRA_CATEGORIA, categoria);
        startActivity(intent);
    }

    private void abrirAdicionarGasto() {
        Intent intent = new Intent(PrincipalActivity.this, AdicionarGastoActivity.class);
        startActivity(intent);
    }

    private void abrirAdicionarSalario() {
        Intent intent = new Intent(PrincipalActivity.this, AdicionarSalarioActivity.class);
        startActivity(intent);
    }

    private void sair() {
        auth.signOut();
        voltarParaLogin();
    }

    private void voltarParaLogin() {
        Intent intent = new Intent(PrincipalActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
