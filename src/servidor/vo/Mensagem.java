/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.vo;

/**
 *
 * @author Samsung
 */
public class Mensagem {
    private int id;
    private String criador;
    private String mensagem;
    private Long timestamp;

    public Mensagem(String ra, String mensagem, Long timestamp) {
        this.criador = ra;
        this.mensagem = mensagem;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    

    public Mensagem() {
    }

    public String getCriador() {
        return criador;
    }

    public void setCriador(String ra) {
        this.criador = ra;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    
    
    
}
