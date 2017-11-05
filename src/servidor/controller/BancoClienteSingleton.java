/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import servidor.vo.Cliente;

/**
 *
 * @author felipesoares
 */
public final class BancoClienteSingleton {

    private static final BancoClienteSingleton instance = new BancoClienteSingleton();
    private BancoClienteSingleton() {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest("12345".getBytes(StandardCharsets.UTF_8));
            String replace = Arrays.toString(hash).replace(" ","");
            String senha = replace;
            addCliente(new Cliente("Cauê Felchar", "1687638", senha, "Ciência da Computação", "8", "", ""));
            addCliente(new Cliente("Vithor Tozetto Ferreira", "1687824", senha, "Ciência da computação", "7", "", ""));
            addCliente(new Cliente("Felipe Soares", "1600001", senha, "Ciência da Computação", "8", "", ""));
            addCliente(new Cliente("Víctor Muniz dos Santos", "1687816", senha, "Ciência da Computação", "8", "", ""));
            addCliente(new Cliente("Rodrigo", "1488635", senha, "Ciência da Computação", "6", "", ""));
        } catch (NoSuchAlgorithmException e){}
    }
    public static BancoClienteSingleton getInstance(){ return instance;}
    
    private final ArrayList<Cliente> bancoCliente = new ArrayList<>();
    
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
