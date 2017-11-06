/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.controller;

import java.sql.Timestamp;
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
        addSala(new Sala(String.valueOf(i), "1600001", "Crypt X Graphs" + i++, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(new Timestamp(System.currentTimeMillis())), opcoes));

        addSala(new Sala(String.valueOf(i), "1600001", "Crypt X Graphs" + i++, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(new Timestamp(System.currentTimeMillis())), opcoes));

        addSala(new Sala(String.valueOf(i), "1600001", "Crypt X Graphs" + i++, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(new Timestamp(System.currentTimeMillis())), opcoes));

        addSala(new Sala(String.valueOf(i), "1600001", "Crypt X Graphs" + i++, "Discussão para decidir disciplina para o próximo semestre letivo",
                String.valueOf(new Timestamp(System.currentTimeMillis())), opcoes));
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

    public Sala getCliente(String ra) {
        for (Sala c : bancoSala) {
            if (c.getId().equals(ra)) {
                return c;
            }
        }
        return null;
    }

    public int getQtdSalas() {
        return bancoSala.size();
    }

    public void criarSala(String ra, String nome, String descricao, String fim, ArrayList<Voto> opcoesx) {
        addSala(new Sala(String.valueOf(i), ra, nome,
                descricao, fim, opcoesx));

    }
}
