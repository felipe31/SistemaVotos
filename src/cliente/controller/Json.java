/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Json {
    private Thread recebimentoThread = null;
    private JSONObject jsonThread = null;

    
    protected JSONObject receberJSON(DatagramSocket socket, String ip, String porta) {
        recebimentoThread = new Thread(() -> {
            try{
                DatagramPacket mensagemPkt = new DatagramPacket(new byte[10000], 10000, InetAddress.getByName(ip), Integer.parseInt(porta));
                mensagemPkt.setData(new byte[10000]);
                socket.receive(mensagemPkt);
                String receiveStr = new String(mensagemPkt.getData());
                receiveStr = receiveStr.trim();
                JSONObject jsonObj = new JSONObject(receiveStr);
                System.out.println("\n[CLIENTE]: Mensagem recebida: "+jsonObj.toString());
                jsonThread = jsonObj;
            }catch(UnknownHostException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SocketException e){
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            } catch (IOException e){
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            }
                
        });
        
        recebimentoThread.start();
        try{
            Thread.sleep(100);
        } catch (InterruptedException e){
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        }
        recebimentoThread.interrupt();
        return jsonThread;
    }

    protected boolean enviarJSON(JSONObject obj, DatagramSocket socket, String ip, String porta) {
        try {

            String mensagemStr = obj.toString();
            byte[] messageByte = mensagemStr.getBytes();

            DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, InetAddress.getByName(ip), Integer.parseInt(porta));
            System.out.println("[CLIENTE]: Mensagem a ser enviada: "+mensagemStr);
            socket.send(packet);
            System.out.println("\n[CLIENTE]: Mensagem enviada com sucesso!\n");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
     
    
}
