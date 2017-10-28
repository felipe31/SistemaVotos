/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import cliente.vo.Cliente;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    public void Logar(Cliente cliente, byte[] hashedPwd) {
        this.cliente = cliente;

        JSONObject obj = new JSONObject();
        obj.put("tipo", 0);
        obj.put("ra", cliente.getRa());
        obj.put("senha", new String(hashedPwd));
        //   System.out.println(obj.toString()+new String(hashedPwd));
        enviarJSON(obj);
//        abrirRecepcaoMensagens();

    }

//    private boolean abrirRecepcaoMensagens() {
//   
//            rxMensagensThread = new Thread(() -> {
//                try {
//                    String[] datagrama;
//                    DatagramPacket mensagemPkt = new DatagramPacket(new byte[10000], 10000, InetAddress.getByName("localhost"), 20000);
//                    mensagemPkt.setData(new byte[10000]);
//                   String receiveStr = new String(mensagemPkt.getData());
//                            receiveStr = receiveStr.trim();
//                            System.out.println( receiveStr);
//
//                            // JSONParser parser = new JSONParser();
//                            JSONObject jSONObject = new JSONObject(receiveStr);
//                    String receiveStr = new String(mensagemPkt.getData());
//                  //  receiveStr = receiveStr.trim();
//                   // datagrama = receiveStr.split("#");
//                    System.out.println(receiveStr);
//                    int aux = 0;
//                   // System.out.println(datagrama[3]);
//                    switch (aux) {
//                        case '2':
//                            conexaoAceita = true;
//                            // atualizaListaContatos(datagrama);
//                            break;
//
//                        case '4':
//                            //recepcaoMensagem(datagrama);
//                            break;
//                        default:
//                            System.out.println(receiveStr);
//                      //  respostaTextArea.setText(respostaTextArea.getText()+" "+"\nMensagem inválida recebida!");
//                        // enviar mensagem avisando o erro pra quem mandou
//                        }
//
//                    datagrama = null;
//
//                    Thread.sleep(500);
//                } catch (UnknownHostException ex) {
//                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            });
//            rxMensagensThread.start();
//
//            return true;
//
//        }
    

       
    

    public boolean enviarJSON(JSONObject obj) {
        try {

            String mensagemStr = obj.toString();
            byte[] messageByte = mensagemStr.getBytes();

            DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, InetAddress.getByName("localhost"), 20000);
            // System.out.println(mensagemStr);
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.send(packet);
            System.out.println("\nMensagem enviada com sucesso!\n\n");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
