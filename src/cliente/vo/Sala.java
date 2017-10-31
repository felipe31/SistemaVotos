/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.vo;

import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Sala {
    private String criador;
    private String nome;
    private String descricao;
    private JSONObject votos;
    private final String id;

    public Sala(String id, String criador, String nome, String descricao, JSONObject votos) {
        this.criador = criador;
        this.nome = nome;
        this.descricao = descricao;
        this.votos = votos;
        this.id = id;
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

}
