/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import cliente.vo.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Sala {

    private final Cliente cliente;
    private final DatagramSocket clientSocket;
    private final JTextPane jTextPaneMensagens;
    private final JTable jTableClientesConectados;
    private final JTable jTableVotos;
    private String[] clientesConectados;
    private String[] votos;
    private Thread recebimentoThread;
    private final Json jsonOp;
    private final DefaultTableModel votosTableModel;
    private final DefaultTableModel clientesTableModel;

    public Sala(Cliente cliente, DatagramSocket clienteSocket, JTextPane jTextPaneMensagens, JTable jTableClientesConectados, JTable jTableVotos) {
        this.jsonOp = new Json();
        this.cliente = cliente;
        this.clientSocket = clienteSocket;
        this.jTextPaneMensagens = jTextPaneMensagens;
        this.jTableClientesConectados = jTableClientesConectados;
        this.jTableVotos = jTableVotos;
//        this.clientesConectados = clientesConectados;
//        this.votos = votos;
        this.votosTableModel = (DefaultTableModel) this.jTableVotos.getModel();
        this.clientesTableModel = (DefaultTableModel) this.jTableClientesConectados.getModel();

    }

    ///////////////////////////// TRATAMENTO DE MENSAGENS ////////////////////////////////////////////////////////
    private void addMensagemVisao(String mensagem, String origem, boolean isClienteAtual) {
        SimpleAttributeSet texto = new SimpleAttributeSet();

        StyledDocument doc = jTextPaneMensagens.getStyledDocument();
        try {

            StyleConstants.setForeground(texto, Color.GRAY);

            if (isClienteAtual) {

                doc.insertString(doc.getLength(), "> ", texto);

            }
            doc.insertString(doc.getLength(), origem + " diz:\n", texto);
            StyleConstants.setForeground(texto, Color.BLACK);
            doc.insertString(doc.getLength(), mensagem + "\n\n", texto);

        } catch (BadLocationException ble) {
            System.out.println("Couldn't insert text into text pane.");
            return;
        }
    }

    public void receberMensagem(JSONObject json) {
//    9 = mensagem do servidor
//	"tipo":9,
//	"id":numero_da_mensagem,
//	"timestamp":"unix_time",
//	"criador":"nome do cara que escreveu a mensagem",
//	"mensagem":"string de até 1000 caracteres"
        boolean isClienteAtual = false;
        if (json.getString("criador").equals(cliente.getNome())) {
            isClienteAtual = true;
        }
        addMensagemVisao(json.getString("mensagem"), json.getString("criador"), isClienteAtual);
    }

    public void enviarMensagemSala(String mensagem) {

//8 = mensagem do cliente pro servidor
//{
//	"tipo":8,
//	"criador":"nome do cara",
//	"mensagem":"de até 1000 caracteres"
//}
        JSONObject json = new JSONObject();

        json.put("tipo", 8);
        json.put("criador", cliente.getNome());
        json.put("mensagem", mensagem);
        jsonOp.enviarJSON(json, clientSocket, cliente.getIp(), cliente.getPorta());

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// CLIENTES CONECTADOS ////////////////////////////////////////////////////////
    public void receberClientesConectados(JSONArray jsonConectados) {
        JSONObject jsonObj;
        clientesTableModel.getDataVector().removeAllElements();
        for (int i = 0; i < jsonConectados.length(); i++) {
            jsonObj = jsonConectados.getJSONObject(i);
            clientesTableModel.addRow(new Object[]{jsonObj.getString("nome")}); 
        }

    }

}
