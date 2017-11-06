/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.vo;

import org.json.JSONObject;
import java.sql.Timestamp;
/**
 * this.inicio = new Timestamp(System.currentTimeMillis());
 * @author felipesoares
 */
public class Sala {
    private String criador;
    private String nome;
    private String descricao;
    private JSONObject votos;
    private final String id;
    private final String inicio;
    private String fim;
    private boolean status;
    private int qtdMensagens;
    
    public Sala(String id, String criador, String nome, String descricao, JSONObject votos, String inicio, String fim, boolean status, int qtdMensagens) {
        this.criador = criador;
        this.nome = nome;
        this.descricao = descricao;
        this.votos = votos;
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

    public JSONObject getVotos() {
        return votos;
    }

    public void setVotos(JSONObject votos) {
        this.votos = votos;
    }

    public String getId() {
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
