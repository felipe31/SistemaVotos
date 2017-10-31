/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.controller;

/**
 *
 * @author felipesoares
 */
public class BancoSalasSingleton {

    private BancoSalasSingleton instance = new BancoSalasSingleton();
    private BancoSalasSingleton() {
        
    }
    public BancoSalasSingleton getInstance(){ return instance;}
 
}
