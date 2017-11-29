/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente.controller;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import cliente.vo.*;
import java.net.DatagramSocket;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
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
    private final JScrollPane scrollPane;
    private String[] clientesConectados;
    private String[] votos;
    private Thread recebimentoThread;
    private final Json jsonOp;
    private final DefaultTableModel votosTableModel;
    private final DefaultTableModel clientesTableModel;
    private final int id_sala;
    private long ultimoTimestamp = 0;

    public Sala(Cliente cliente, DatagramSocket clienteSocket, JTextPane jTextPaneMensagens, JTable jTableClientesConectados, JTable jTableVotos, JScrollPane scrollPane, int id_sala) {
        this.jsonOp = new Json();
        this.cliente = cliente;
        this.clientSocket = clienteSocket;
        this.jTextPaneMensagens = jTextPaneMensagens;
        this.jTableClientesConectados = jTableClientesConectados;
        this.jTableVotos = jTableVotos;
        this.votosTableModel = (DefaultTableModel) this.jTableVotos.getModel();
        this.clientesTableModel = (DefaultTableModel) this.jTableClientesConectados.getModel();
        this.scrollPane = scrollPane;
        this.id_sala = id_sala;
    }

    ///////////////////////////// TRATAMENTO DE MENSAGENS ////////////////////////////////////////////////////////
    private void addMensagemVisao(String mensagem, String origem, String timestamp, boolean isClienteAtual) {
        SimpleAttributeSet texto = new SimpleAttributeSet();
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(Long.valueOf(timestamp) * 1000));
        StyledDocument doc = jTextPaneMensagens.getStyledDocument();
        try {
            if (ultimoTimestamp > Long.valueOf(timestamp)) {
                StyleConstants.setForeground(texto, Color.MAGENTA);
            } else {
                StyleConstants.setForeground(texto, Color.GRAY);
                ultimoTimestamp = Long.valueOf(timestamp);
            }

            if (isClienteAtual) {

                doc.insertString(doc.getLength(), "> ", texto);

            }
            doc.insertString(doc.getLength(), date + " - " + origem + " diz:\n", texto);
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
        if (json.has("criador") && json.has("mensagem")) {

            String timestamp;
            try {
                timestamp = json.getString("timestamp");
            } catch (Exception e) {
                timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            }
            if(cliente.getNome().equals(json.getString("criador"))) isClienteAtual = true;
            addMensagemVisao(json.getString("mensagem"), json.getString("criador"), timestamp, isClienteAtual);
            try {
                jTextPaneMensagens.setCaretPosition(jTextPaneMensagens.getDocument().getLength());
            } catch (Exception e) {
            }

        } else {
            System.out.println("MENSAGEM MAL FORMADA " + json.toString());
        }

    }

    public void enviarMensagemSala(String mensagem) {

//8 = mensagem do cliente pro servidor
//{
//	"tipo":8,
//	"criador":"nome do cara",
//	"mensagem":"de até 1000 caracteres"
//}
        JSONObject json = new JSONObject();

        json.put("tipo", 14);
        json.put("criador", cliente.getNome());
        json.put("mensagem", mensagem);
        jsonOp.enviarJSON(json, clientSocket, cliente.getIp(), cliente.getPorta());

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// CLIENTES CONECTADOS ////////////////////////////////////////////////////////
    /**
     * recebe os usuarios conectados e adiciona se possuem nome na tabela de
     * clientes
     *
     * @param jsonConectados caso contrario, retorna mensagem mal formada
     * protocolo -1
     */
    public void receberClientesConectados(JSONArray jsonConectados) {
        JSONObject jsonObj;
        clientesTableModel.getDataVector().removeAllElements();
        for (int i = 0; i < jsonConectados.length(); i++) {
            jsonObj = jsonConectados.getJSONObject(i);
            if (jsonObj.has("nome")) {
                clientesTableModel.addRow(new Object[]{jsonObj.getString("nome")});
            } else {
                //mensagem mal formada
            }
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// TRATAMENTO DE VOTOS ////////////////////////////////////////////////////////
    public void receberVotacao(JSONArray jsonArray) {
        JSONObject json;
        votosTableModel.getDataVector().removeAllElements();
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            votosTableModel.addRow(json.keySet().toArray());
        }
    }

    public void enviarVoto(String opcao) {
//        15 = voto
//{
//	"tipo":15,
//	"sala":id_da_sala,
//	"opcao":"nome_da_opção"
//}
        JSONObject json = new JSONObject();

        json.put("tipo", 15);
        json.put("sala", id_sala);
        json.put("opcao", opcao);

        jsonOp.enviarJSON(json, clientSocket, cliente.getIp(), cliente.getPorta());
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    void receberAlteracaoClienteConectado(JSONObject json) {
//        16 = desconectar/conectar usuário
//{
//	"tipo":16,
//	"adicionar":true/false,
//	"nome":"nome_do_usuario"
//}
        if (json.has("adicionar") && json.has("nome")) {
            if (json.getBoolean("adicionar")) {
                clientesTableModel.addRow(new Object[]{json.getString("nome")});
            } else {
                for (int i = 0; i < jTableClientesConectados.getRowCount(); i++) {
                    if (clientesTableModel.getValueAt(i, 0).equals(json.getString("nome"))) {
                        clientesTableModel.removeRow(i);
                    }
                }
            }
        } else {
            //mensagem mal formada
        }

    }

    public void sairSala() {
        JSONObject json = new JSONObject();
        json.put("tipo", 11);
        jsonOp.enviarJSON(json, clientSocket, cliente.getIp(), cliente.getPorta());
    }

    public int getId_sala() {
        return id_sala;
    }

    
}
