package com.example.financas.model;

import java.util.Date;

public class ResumoMensal {

    private double salario;
    private double totalGastos;
    private double saldoRestante;
    private int mes;
    private int ano;
    private String mesAno;
    private Date atualizadoEm;

    public ResumoMensal() {
    }

    public ResumoMensal(
            double salario,
            double totalGastos,
            double saldoRestante,
            int mes,
            int ano,
            String mesAno,
            Date atualizadoEm
    ) {
        this.salario = salario;
        this.totalGastos = totalGastos;
        this.saldoRestante = saldoRestante;
        this.mes = mes;
        this.ano = ano;
        this.mesAno = mesAno;
        this.atualizadoEm = atualizadoEm;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public double getTotalGastos() {
        return totalGastos;
    }

    public void setTotalGastos(double totalGastos) {
        this.totalGastos = totalGastos;
    }

    public double getSaldoRestante() {
        return saldoRestante;
    }

    public void setSaldoRestante(double saldoRestante) {
        this.saldoRestante = saldoRestante;
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

    public Date getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Date atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
