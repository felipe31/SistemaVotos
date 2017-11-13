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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author felipesoares
 */
public class Sala {

    private Cliente cliente;
    private DatagramSocket clientSocket;
    private JTextPane jTextPaneMensagens;
    private JTable jTableClientesConectados, jTableVotos;
    private String[] clientesConectados;
    private String[] votos;
    private Thread recebimentoThread;

    public Sala(Cliente cliente, DatagramSocket clienteSocket, JTextPane jTextPaneMensagens, JTable jTableClientesConectados, JTable jTableVotos) {
        this.cliente = cliente;
        this.clientSocket = clienteSocket;
        this.jTextPaneMensagens = jTextPaneMensagens;
        this.jTableClientesConectados = jTableClientesConectados;
        this.jTableVotos = jTableVotos;
//        this.clientesConectados = clientesConectados;
//        this.votos = votos;

    }

    private void addMensagemVisao(String mensagem, String origem, boolean isClienteAtual) {
        SimpleAttributeSet texto = new SimpleAttributeSet();

        StyledDocument doc = jTextPaneMensagens.getStyledDocument();
        try {

            if (isClienteAtual) {
                StyleConstants.setAlignment(texto, StyleConstants.ALIGN_RIGHT);
                jTextPaneMensagens.setParagraphAttributes(texto, true);

            } else {
                StyleConstants.setAlignment(texto, StyleConstants.ALIGN_LEFT);
                jTextPaneMensagens.setParagraphAttributes(texto, true);

            }
            StyleConstants.setForeground(texto, Color.GRAY);
            doc.insertString(doc.getLength(), origem + " diz:\n", texto);
            StyleConstants.setForeground(texto, Color.BLACK);
            doc.insertString(doc.getLength(), mensagem + "\n\n", texto);

        } catch (BadLocationException ble) {
            System.out.println("Couldn't insert text into text pane.");
            return;
        }
    }

    public void recepcaoMensagem(JSONObject json) {
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
}
