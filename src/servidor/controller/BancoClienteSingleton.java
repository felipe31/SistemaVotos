/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.controller;

import java.util.ArrayList;
import servidor.vo.Cliente;

/**
 *
 * @author felipesoares
 */
public class BancoClienteSingleton {

    private static BancoClienteSingleton instance = new BancoClienteSingleton();
    private BancoClienteSingleton() {
        addCliente(new Cliente("Felipe Soares", "1600001", "", ""));
    }
    public static BancoClienteSingleton getInstance(){ return instance;}
    
    private ArrayList<Cliente> bancoCliente = new ArrayList<>();
    
    public void addCliente(Cliente cliente){
        bancoCliente.add(cliente);
    }
    
    public Cliente getCliente(String ra){
        for(Cliente c : bancoCliente){
            if(c.getRa().equals(ra))
                return c;
        }
        return null;
    }
    
}
