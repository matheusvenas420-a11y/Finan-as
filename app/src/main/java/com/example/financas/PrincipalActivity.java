package com.example.financas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PrincipalActivity extends AppCompatActivity {

    private TextView tvSair;
    private TextView tabGastos;
    private TextView tabInvestimentos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        iniciarComponentes();
        configurarCliques();
    }

    private void iniciarComponentes() {
        tvSair = findViewById(R.id.tvSair);
        tabGastos = findViewById(R.id.tabGastos);
        tabInvestimentos = findViewById(R.id.tabInvestimentos);
    }

    private void configurarCliques() {
        tvSair.setOnClickListener(view -> sair());

        tabGastos.setOnClickListener(view -> selecionarAbaGastos());
        tabInvestimentos.setOnClickListener(view -> selecionarAbaInvestimentos());
    }

    private void selecionarAbaGastos() {
        tabGastos.setBackgroundResource(R.drawable.bg_tab_active);
        tabGastos.setTextColor(getColorCompat("#111827"));

        tabInvestimentos.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        tabInvestimentos.setTextColor(getColorCompat("#6B7280"));
    }

    private void selecionarAbaInvestimentos() {
        tabInvestimentos.setBackgroundResource(R.drawable.bg_tab_active);
        tabInvestimentos.setTextColor(getColorCompat("#111827"));

        tabGastos.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        tabGastos.setTextColor(getColorCompat("#6B7280"));
    }

    private int getColorCompat(String cor) {
        return android.graphics.Color.parseColor(cor);
    }

    private void sair() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(PrincipalActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
