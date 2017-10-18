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
public class Cliente {
    
    String nome;
    String ra;
    String ip;
    String porta;

    public Cliente(String nome, String ra, String ip, String porta) {
        this.nome = nome;
        this.ra = ra;
        this.ip = ip;
        this.porta = porta;
    }

    public Cliente() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPorta() {
        return porta;
    }

    public void setPorta(String porta) {
        this.porta = porta;
    }
    
    
    
}
