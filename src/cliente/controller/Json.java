/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Json {

    private Thread recebimentoThread = null;
    private JSONObject jsonThread = null;

    protected void iniciaThreadRecebimentoJSON(DatagramSocket socket, String ip, String porta) {
        recebimentoThread = new Thread(() -> {
            try {
                DatagramPacket mensagemPkt = new DatagramPacket(new byte[10000], 10000, InetAddress.getByName(ip), Integer.parseInt(porta));
                mensagemPkt.setData(new byte[10000]);
                socket.receive(mensagemPkt);
                String receiveStr = new String(mensagemPkt.getData());
                receiveStr = receiveStr.trim();
                JSONObject jsonObj = new JSONObject(receiveStr);
                System.out.println("\n[CLIENTE]: Mensagem recebida: " + jsonObj.toString());
                System.out.println("\n[CLIENTE]: Mensagem recebida de " + ip + ":" + porta);
                jsonThread = jsonObj;

            } catch (Exception ex) {
                System.out.println("Problema na Thread de login");
            }

        });

        recebimentoThread.start();
    }

    protected JSONObject receberJSON() {

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            System.out.println("Problema na Thread de login");

        }
        recebimentoThread.interrupt();
        return jsonThread;
    }

    protected boolean enviarJSON(JSONObject obj, DatagramSocket socket, String ip, String porta) {
        try {

            String mensagemStr = obj.toString();
            byte[] messageByte = mensagemStr.getBytes();

            DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, InetAddress.getByName(ip), Integer.parseInt(porta));
            socket.send(packet);
            System.out.println("[CLIENTE]: Mensagem enviada: " + mensagemStr);
            System.out.println("[CLIENTE]: Mensagem enviada para " + ip + ":" + porta);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
