/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;
import cliente.vo.Cliente;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONObject;
/**
 *
 * @author felipesoares
 */
public class Login {
    
    private Cliente cliente;
    
    public static void main(String[] args){
        new Login();
    }

    public Login() {
    }
    
    public void Logar(Cliente cliente, byte[] hashedPwd){
        this.cliente = cliente;
        
        JSONObject obj = new JSONObject();
        obj.put("tipo", "0");
        obj.put("ra", cliente.getRa());
        obj.put("senha", new String(hashedPwd));
        System.out.println(obj.toString()+new String(hashedPwd));
        enviarJSON(obj);        
    }
       
    public boolean enviarJSON(JSONObject obj){
        try{                       
            
            String mensagemStr = obj.toString();
            byte[] messageByte = mensagemStr.getBytes();
            
            DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, InetAddress.getByName("10.20.8.118"), 20000);
            System.out.println(mensagemStr);
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.send(packet);
            System.out.println("\nMensagem enviada com sucesso!\n\n");
            return true;
        } catch(Exception e) { return false;}
    }
    
}
