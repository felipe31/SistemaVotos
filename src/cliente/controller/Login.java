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
    private DatagramSocket clientSocket = null;

    public DatagramSocket getClientSocket() {
        return clientSocket;
    }
    private Json jsonOp = new Json();
    
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
        obj.put("senha", hashedPwd);
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(jsonOp.enviarJSON(obj, clientSocket, cliente.getIp(), cliente.getPorta())){
            JSONObject jsonObj = jsonOp.receberJSON(clientSocket, cliente.getIp(), cliente.getPorta());
            if(jsonObj != null){
                return jsonObj;
            }
        }
        return null;
    }
    
    public boolean Deslogar() {
        JSONObject json = new JSONObject();
        json.put("tipo", 10);
        return jsonOp.enviarJSON(json, clientSocket, cliente.getIp(), cliente.getPorta());
    }

}
