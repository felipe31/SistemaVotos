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
            
            String senha = sha256("123");
            addCliente(new Cliente("Cauê Felchar", "1687638", senha, "Ciência da Computação", "8", "", ""));
            addCliente(new Cliente("Vithor Tozetto Ferreira", "1687824", senha, "Ciência da computação", "7", "", ""));
            addCliente(new Cliente("Felipe Soares", "1600001", senha, "Ciência da Computação", "8", "", ""));
            addCliente(new Cliente("Víctor Muniz dos Santos", "1687816", senha, "Ciência da Computação", "8", "", ""));
            addCliente(new Cliente("Rodrigo", "1488635", senha, "Ciência da Computação", "6", "", ""));
            addCliente(new Cliente("123", "123", senha, "Ciência da Computação", "6", "", ""));
        } catch (RuntimeException e){}
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
    
    public String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
