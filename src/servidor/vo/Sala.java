/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author felipesoares
 */
public class Sala {

    private String criador_ra;
    private String criador_nome;
    private String nome;
    private String descricao;
    private ArrayList<Voto> opcoes;
    private int qtdVotos;
    private final int id;
    private String inicio;
    private String fim;
    private boolean status = true;
    private int qtdMensagens;
    private HashSet<Cliente> clientesConectados;
    private ArrayList<Mensagem> mensagemDaSala = new ArrayList<>();

    public ArrayList<Mensagem> getMensagemDaSala() {
        int id = 0;
        Collections.sort(mensagemDaSala, new ComparadorDeMensagem());
        Iterator it = mensagemDaSala.iterator();
        while (it.hasNext()) {
            Mensagem m = (Mensagem) it.next();
            m.setId(id);
            id++;
        }
        return mensagemDaSala;
    }

    public void setMensagemDaSala(ArrayList<Mensagem> mensagemDaSala) {
        this.mensagemDaSala = mensagemDaSala;
    }

    public Sala(int id, String criador_ra, String criador_nome, String nome, String descricao, String fim, ArrayList<Voto> opcoes) {
        this.criador_ra = criador_ra;
        this.criador_nome = criador_nome;
        this.nome = nome;
        this.descricao = descricao;
        this.id = id;
        this.inicio = String.valueOf(System.currentTimeMillis() / 1000);
        this.fim = String.valueOf(fim);
        this.qtdMensagens = 0;
        this.opcoes = opcoes;
        clientesConectados = new HashSet<>();
        qtdVotos = 0;
    }

    public ArrayList<Voto> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(ArrayList<Voto> opcoes) {
        this.opcoes = opcoes;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFim() {
        return fim;
    }

    public void setFim(String fim) {
        this.fim = fim;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
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

    public String getCriador_ra() {
        return criador_ra;
    }

    public void setCriador_ra(String criador_ra) {
        this.criador_ra = criador_ra;
    }

    public String getCriador_nome() {
        return criador_nome;
    }

    public void setCriador_nome(String criador_nome) {
        this.criador_nome = criador_nome;
    }

    public int getId() {
        return id;
    }

    public HashSet<Cliente> getClientesConectados() {
        return clientesConectados;
    }

    public Cliente getClienteConectado(String ra) {

        for (Cliente c : clientesConectados) {
            if (c.getRa().equals(ra)) {
                return c;
            }
        }
        return null;
    }

    public Cliente getClienteConectado(String ip, String porta) {
        for (Cliente c : clientesConectados) {
            if (c.getIp().equals(ip) && c.getPorta().equals(porta)) {
                return c;
            }
        }
        return null;
    }

    public void addClienteConectado(Cliente cliente) {
        clientesConectados.add(cliente);
    }

    public void removeClienteConectado(Cliente cliente) {
        clientesConectados.remove(cliente);
    }

    public boolean addVoto(String descricaoVoto, Cliente cliente) {
        int flag = 0;
        String votou = cliente.jaVotouNaSala(id);
        if (votou != null) {
            if (votou.equals(descricaoVoto)) {

                return true;
            } else {
                for (Voto v : opcoes) {
                    if (v.getDescricao().equals(descricaoVoto)) {
                        v.setContador(v.getContador() + 1);
                        cliente.addVoto(id, descricaoVoto);
                        qtdVotos++;
                        flag++;
                        if (flag == 2) {
                            return true;
                        }
                    } else if (v.getDescricao().equals(votou)) {
                        v.setContador(v.getContador() - 1);
                        qtdVotos--;
                        flag++;
                        if (flag == 2) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (Voto v : opcoes) {
                if (v.getDescricao().equals(descricaoVoto)) {
                    v.setContador(v.getContador() + 1);
                    cliente.addVoto(id, descricaoVoto);
                    qtdVotos++;
                    return true;
                }
            }
        }
        return false;
    }

    public int getQtdVotos() {
        return qtdVotos;
    }
}
