/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import cliente.vo.BancoSalasSingleton;
import cliente.vo.Cliente;
import cliente.vo.Sala;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Home {
    
    private Cliente cliente;
    private Json jsonOp = new Json();
    private Thread recebimentoThread = null;
    private JSONObject jsonThread = null;
    private DatagramSocket clienteSocket = null;
    private int qtdSalas;
    private DefaultTableModel salasTabela;

    public Home(Cliente cliente, DatagramSocket clienteSocket, DefaultTableModel salasTabela, int qtdSalas) {
        this.cliente = cliente;
        this.clienteSocket = clienteSocket;
        this.qtdSalas = qtdSalas;
        this.salasTabela = salasTabela;
        abrirRecepcaoJSON(clienteSocket, cliente.getIp(), cliente.getPorta());
    }
    
    private void recepcaoSala(JSONObject json){
        BancoSalasSingleton bancoSalas = BancoSalasSingleton.getInstance();
        Sala sala = new Sala(json.getInt("id"),
                json.getString("criador"),
                json.getString("nome"),
                json.getString("descricao"),
                null,
                json.getString("inicio"),
                json.getString("fim"), json.getBoolean("status"),
                0);
        bancoSalas.addSala(sala);
        if(sala != null)
            addSalaVisao(sala);
        else
            System.out.println("Deu pau ao receber sala");
    }
        
    private void addSalaVisao(Sala sala){
        String [] dados;
        dados = new String[]{sala.getNome(), sala.getDescricao(), sala.isStatus()?"Ativo":"Inativo"};
        if(salasTabela != null)
            salasTabela.addRow(dados);
        else
            System.out.println("Deu pau ao imprimir sala na tabela");
    }
    
    public void criaSala(JSONObject jsonSala) {
        jsonSala.put("tipo", 3);
        jsonOp.enviarJSON(jsonSala, clienteSocket, cliente.getIp(), cliente.getPorta());
    }
    
    protected JSONObject abrirRecepcaoJSON(DatagramSocket socket, String ip, String porta) {
        recebimentoThread = new Thread(() -> {
            try{
                String receiveStr;
                DatagramPacket mensagemPkt = new DatagramPacket(new byte[10000], 10000, InetAddress.getByName(ip), Integer.parseInt(porta));
                while(true){
                    mensagemPkt.setData(new byte[10000]);
                    socket.receive(mensagemPkt);
                    receiveStr = new String(mensagemPkt.getData());
                    receiveStr = receiveStr.trim();
                    JSONObject jsonObj = new JSONObject(receiveStr);
                    System.out.println("\n[CLIENTE]: Mensagem recebida: "+jsonObj.toString());
                    jsonThread = jsonObj;
                    
                    switch(jsonObj.getInt("tipo")){
                        case 11:
                            recepcaoSala(jsonObj);
                            break;
                        case 4:
                            recepcaoSala(jsonObj);
                            break;
                        default:
                            // enviar mensagem avisando o erro pra quem mandou
                    }
                    
                    receiveStr = null;
                    Thread.sleep(500);
                }
            }catch(UnknownHostException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SocketException e){
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            } catch (IOException e){
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            } catch (InterruptedException e){
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            }
                
        });
        
            recebimentoThread.start();
        try{
            Thread.sleep(100);
        } catch (InterruptedException e){
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
     }
}
