package com.example.financas.model;

import java.util.Date;

public class Salario {

    private double valor;
    private Date data;
    private int mes;
    private int ano;
    private String mesAno;

    public Salario() {
    }

    public Salario(double valor, Date data, int mes, int ano, String mesAno) {
        this.valor = valor;
        this.data = data;
        this.mes = mes;
        this.ano = ano;
        this.mesAno = mesAno;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
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
