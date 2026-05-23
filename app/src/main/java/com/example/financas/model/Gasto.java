package com.example.financas.model;

import java.util.Date;

public class Gasto {

    private String descricao;
    private double valor;
    private String categoria;
    private String formaPagamento;
    private int parcelas;
    private Date data;
    private int mes;
    private int ano;
    private String mesAno;

    public Gasto() {
    }

    public Gasto(
            String descricao,
            double valor,
            String categoria,
            String formaPagamento,
            int parcelas,
            Date data,
            int mes,
            int ano,
            String mesAno
    ) {
        this.descricao = descricao;
        this.valor = valor;
        this.categoria = categoria;
        this.formaPagamento = formaPagamento;
        this.parcelas = parcelas;
        this.data = data;
        this.mes = mes;
        this.ano = ano;
        this.mesAno = mesAno;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public int getParcelas() {
        return parcelas;
    }

    public void setParcelas(int parcelas) {
        this.parcelas = parcelas;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getMesAno() {
        return mesAno;
    }

    public void setMesAno(String mesAno) {
        this.mesAno = mesAno;
    }
}
