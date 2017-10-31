/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.vo;

/**
 *
 * @author felipesoares
 */
public class Cliente {

    private String nome;
    private String ra;
    private String porta;
    private String ip;
    
    public Cliente() {
    }

    public Cliente(String nome, String ra, String ip, String porta) {
        this.nome = nome;
        this.ra = ra;
        this.porta = porta;
        this.ip = ip;
    }

    public String getNome() {
        return nome;
    }

    public String getRa() {
        return ra;
    }

    public String getPorta() {
        return porta;
    }

    public String getIp() {
        return ip;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public void setPorta(String porta) {
        this.porta = porta;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    
    
    
    
}
