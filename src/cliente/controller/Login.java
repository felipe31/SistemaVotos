/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import cliente.vo.Cliente;
import java.net.DatagramSocket;
import java.net.SocketException;
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

    public JSONObject Logar(Cliente cliente, String hashedPwd) {
        this.cliente = cliente;

        JSONObject obj = new JSONObject();
        obj.put("tipo", 0);
        obj.put("ra", cliente.getRa());
        obj.put("senha", hashedPwd);
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            return null;
        }
        jsonOp.iniciaThreadRecebimentoJSON(clientSocket, cliente.getIp(), cliente.getPorta());
        if (jsonOp.enviarJSON(obj, clientSocket, cliente.getIp(), cliente.getPorta())) {
            JSONObject jsonObj = jsonOp.receberJSON();
            if (jsonObj != null) {
                if (jsonObj.has("tipo")) {
                    if (jsonObj.getInt("tipo") == 1 || jsonObj.getInt("tipo") == -1) {
                        return null;
                    }
                    return jsonObj;
                }
            }
        }
        return null;
    }

    public boolean Deslogar() {
        JSONObject json = new JSONObject();
        json.put("tipo", 3);
        return jsonOp.enviarJSON(json, clientSocket, cliente.getIp(), cliente.getPorta());
    }

}
