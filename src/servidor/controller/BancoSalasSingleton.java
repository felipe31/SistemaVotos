/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.controller;

import servidor.vo.Sala;
import java.util.ArrayList;
import org.json.JSONObject;
import servidor.vo.Voto;

/**
 *
 * @author felipesoares
 */
public final class BancoSalasSingleton {

    ArrayList<Voto> opcoes = new ArrayList<>();

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    private static final BancoSalasSingleton instance = new BancoSalasSingleton();
    int i = 0;

    private BancoSalasSingleton() {

        Voto v = new Voto("sim");
        Voto v2 = new Voto("nao");

        opcoes.add(v);
        opcoes.add(v2);

        JSONObject json = new JSONObject();
        json.put("crypt", 15);
        json.put("graphs", 16);
        criarSala("1600001", "Crypt X Graphs" + i+1, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(System.currentTimeMillis()/1000), opcoes);

        criarSala("1600001", "Crypt X Graphs" + i+1, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(System.currentTimeMillis()/1000), opcoes);

        criarSala("1600001", "Crypt X Graphs" + i+1, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(System.currentTimeMillis()/1000), opcoes);

        criarSala("1600001", "Crypt X Graphs" + i+1, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(System.currentTimeMillis()/1000), opcoes);

    }

    public static BancoSalasSingleton getInstance() {
        return instance;
    }

    private final ArrayList<Sala> bancoSala = new ArrayList<>();

    public void addSala(Sala sala) {
        bancoSala.add(sala);
    }

    public ArrayList<Sala> getBancoSala() {
        return bancoSala;
    }

    public Sala getSala(int id) {
        for (Sala s : bancoSala) {
            if (s.getId()==id) {
                return s;
            }
        }
        return null;
    }

    public int getQtdSalas() {
        return bancoSala.size();
    }

    public Sala criarSala(String criador_ra, String nome, String descricao, String fim, ArrayList<Voto> opcoesx) {
        String criador_nome = BancoClienteSingleton.getInstance().getCliente(criador_ra).getNome();
        Sala sala = new Sala(i++, criador_ra, criador_nome, nome,
                descricao, fim, opcoesx);
        addSala(sala);
        return sala;
    }

}
