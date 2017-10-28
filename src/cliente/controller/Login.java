/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import cliente.visao.Home;
import cliente.vo.Cliente;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Login {

    private Cliente cliente;
    private Thread rxMensagensThread;
    private DatagramSocket clientSocket = null;
    Boolean conexaoAceita;

    public static void main(String[] args) {
        new Login();
    }

    public Login() {
    }

    public JSONObject Logar(Cliente cliente, byte[] hashedPwd) {
        this.cliente = cliente;

        JSONObject obj = new JSONObject();
        obj.put("tipo", 0);
        obj.put("ra", cliente.getRa());
        obj.put("senha", new String(hashedPwd));
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(enviarJSON(obj)){
            JSONObject jsonObj = receberJSON();
            if(jsonObj != null){
                return jsonObj;
            }
        }
        return null;
    }

    private JSONObject receberJSON() {

        try {
            DatagramPacket mensagemPkt = new DatagramPacket(new byte[10000], 10000, InetAddress.getByName("127.0.0.1"), 20000);
            mensagemPkt.setData(new byte[10000]);
            clientSocket.receive(mensagemPkt);
            String receiveStr = new String(mensagemPkt.getData());
            receiveStr = receiveStr.trim();
            JSONObject jsonObj = new JSONObject(receiveStr);
            System.out.println(jsonObj.toString());
            return jsonObj;

        } catch (UnknownHostException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException e){
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e){
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
    

       
    

    public boolean enviarJSON(JSONObject obj) {
        try {

            String mensagemStr = obj.toString();
            byte[] messageByte = mensagemStr.getBytes();

            DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, InetAddress.getByName("127.0.0.1"), 20000);
            System.out.println(mensagemStr);
            clientSocket.send(packet);
            System.out.println("\nMensagem enviada com sucesso!\n\n");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
