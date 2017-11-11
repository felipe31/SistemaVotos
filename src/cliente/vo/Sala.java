/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.vo;

import java.util.ArrayList;
import org.json.JSONObject;
import servidor.vo.Voto;
/**
 * this.inicio = new Timestamp(System.currentTimeMillis());
 * @author felipesoares
 */
public class Sala {
    private String criador;
    private String nome;
    private String descricao;
    private ArrayList<Voto> opcoes;
    private final int id;
    private final String inicio;
    private String fim;
    private boolean status;
    private int qtdMensagens;
    
    public Sala(int id, String criador, String nome, String descricao, String inicio, String fim, ArrayList<Voto> opcoes, boolean status, int qtdMensagens) {
        this.criador = criador;
        this.nome = nome;
        this.descricao = descricao;
        this.opcoes = opcoes;
        this.id = id;
        this.inicio = inicio;
        this.fim = fim;
        this.status = status;
        this.qtdMensagens = qtdMensagens;
    }

    public String getCriador() {
        return criador;
    }

    public void setCriador(String criador) {
        this.criador = criador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ArrayList<Voto> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(ArrayList<Voto> opcoes) {
        this.opcoes = opcoes;
    }

    public int getId() {
        return id;
    }

    public String getFim() {
        return fim;
    }

    public void setFim(String fim) {
        this.fim = fim;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getQtdMensagens() {
        return qtdMensagens;
    }

    public void setQtdMensagens(int qtdMensagens) {
        this.qtdMensagens = qtdMensagens;
    }

    public String getInicio() {
        return inicio;
    }

    
}
