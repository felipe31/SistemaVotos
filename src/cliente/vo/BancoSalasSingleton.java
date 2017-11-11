/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.vo;

import cliente.vo.Sala;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public final class BancoSalasSingleton {

    private static final BancoSalasSingleton instance = new BancoSalasSingleton();
    private BancoSalasSingleton() {
    }
    public static BancoSalasSingleton getInstance(){ return instance;}

    private final ArrayList<Sala> bancoSala = new ArrayList<>();
    
    public void addSala(Sala sala){
        bancoSala.add(sala);
    }
    
    public ArrayList<Sala> getBancoSala(){
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
    public int getQtdSalas(){
        return bancoSala.size();
    }
}
