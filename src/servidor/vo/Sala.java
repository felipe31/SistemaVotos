/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.vo;

import org.json.JSONObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author felipesoares
 */
public class Sala {

    private String criador;
    private String nome;
    private String descricao;
    private final String id;
    private Timestamp inicio;
    private Timestamp fim;
    private Boolean status = true;
    private int mensagens;
    private ArrayList<Voto> opcoes = new ArrayList<>();

    public Sala(String id, String criador, String nome, String descricao, String fim, ArrayList<Voto> opcoes) {
        this.criador = criador;
        this.nome = nome;
        this.descricao = descricao;
        this.id = id;
        this.inicio = new Timestamp(System.currentTimeMillis());
        this.fim = Timestamp.valueOf(fim);;
        this.mensagens = 0;
        this.opcoes = opcoes;

    }

    public ArrayList<Voto> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(ArrayList<Voto> opcoes) {
        this.opcoes = opcoes;
    }

    public Timestamp getInicio() {
        return inicio;
    }

    public void setInicio(Timestamp inicio) {
        this.inicio = inicio;
    }

    public Timestamp getFim() {
        return fim;
    }

    public void setFim(Timestamp fim) {
        this.fim = fim;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public int getMensagens() {
        return mensagens;
    }

    public void setMensagens(int mensagens) {
        this.mensagens = mensagens;
    }

//    public Sala(String id, String criador, String nome, String descricao, JSONObject votos) {
//        this.criador = criador;
//        this.nome = nome;
//        this.descricao = descricao;
//        this.votos = votos;
//        this.id = id;
//        this.inicio = new Timestamp(System.currentTimeMillis());
//        this.fim = new Timestamp(System.currentTimeMillis());
//        this.mensagens = 0;
//        
//
//     
//    }
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

    public String getId() {
        return id;
    }

}
