/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.visao;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;
import servidor.controller.BancoClienteSingleton;
import servidor.vo.Cliente;

/**
 *
 * @author felipesoares
 */
public class ServerChatUDP extends javax.swing.JPanel {

    private JFrame frame;
    private DefaultTableModel table;
    private ArrayList<String[]> clientesConectados = new ArrayList<>();
    private DatagramPacket receivePkt;
    private byte[] buffer = null;
    private Thread execServidor;
    private DatagramSocket serverDatagram = null;

    private ServerChatUDP() {
        initComponents();

        table = iniciaJTable(clientesJTable);

        this.CriaJanela();

        try {


            serverDatagram = new DatagramSocket(20000);
            
            execServidor = new Thread(() -> {
                serverTextArea.setText("[SERVIDOR]: Servidor iniciado na porta "+serverDatagram.getLocalPort()+"\n");

                try {
                    
                    while (true) {
                        buffer = new byte[1024];
                        receivePkt = new DatagramPacket(buffer, buffer.length);
                        serverDatagram.receive(receivePkt);
                        
                        String receiveStr = new String(receivePkt.getData());
                        
                        receiveStr = receiveStr.trim();
                        
                        serverTextArea.setText(serverTextArea.getText() + "\n"
                                + receivePkt.getAddress().toString().split("/")[1] + ":"
                                + receivePkt.getPort() + "\n" + receiveStr);
                        
                        System.out.println("\n[SERVIDOR]: Mensagem recebida: "+receiveStr);
                        
                        JSONObject jSONObject = new JSONObject(receiveStr);
                        String ip = receivePkt.getAddress().toString().split("/")[1];
                        
                        switch ((int)jSONObject.get("tipo"))
                        {
                            case 0:
                                System.out.println("Login");
                                if(verificaLogin(jSONObject.getString("ra"))!= null){
                                    addConexao((String) jSONObject.get("ra"), ip, receivePkt.getPort());
                                }else{
                                    JOptionPane.showMessageDialog(frame, "Usuário incorreto");
                                }
                                break;
                                
                        }
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    if (serverDatagram != null) {
                        serverDatagram.close();
                    }
                }
            });

            execServidor.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // PROCESSAMENTO DOS DATAGRAMAS RECEBIDOS
    
    private Cliente verificaLogin(String ra){
        BancoClienteSingleton bancoCliente = BancoClienteSingleton.getInstance();
        return bancoCliente.getCliente(ra);
    }
    
    private void removeConexao(String ip, int porta) {
        int idx = encontraIndice(clientesConectados, ip, porta);
        JOptionPane.showMessageDialog(frame, idx);
        if (idx > -1) {
            table.removeRow(idx);
            clientesConectados.remove(idx);

            enviaListaConectados("999.999.999.999", 99999);
            //mandar datagrama 2 para todos os conectados

        }
    }

    private void addConexao(String ra, String ip, int porta) {
        table.addRow(new Object[]{ra, ip, porta});
        clientesConectados.add(new String[]{ra, ip, String.valueOf(porta)});
        confimarLogin(ip, porta);
        //  enviaListaConectados("999.999.999.999", 99999);
        //mandar datagrama 2 para todos os conectados
    }

    private void encaminharMensagem(String[] mensagem, String ip, int porta) {

        enviarMensagem("4#" + ip + "#" + porta + "#" + mensagem[3], mensagem[1], Integer.parseInt(mensagem[2]));

    }

    private boolean isBroadcast(String ip, String porta) {
        if (ip.equals("999.999.999.999") && porta.equals("99999")) {
            return true;
        }
        return false;
    }

    protected boolean confimarLogin(String ip, int porta) {
        JSONObject obj = new JSONObject();
        obj.put("tipo", 2);
        obj.put("nome", "sucess");
        obj.put("tamanho", 0);

        String mensagemStr = obj.toString();
        
        if(enviarMensagem(mensagemStr, ip, porta)){
            System.out.println("\n[SERVIDOR]: Login confirmado com sucesso!");
            return true;
        }else
            System.out.println("\n[SERVIDOR]: Erro ao confirmar o login!");
        return false;
    }

    public boolean enviaListaConectados(String ip, int porta) {
        String mensagem = "";

        mensagem += clientesConectados.stream().map((str) -> "#" + str[1] + "#" + str[2] + "#" + str[0]).reduce(mensagem + "2", String::concat);

        if (!enviarMensagem(mensagem, ip, porta)) {
            return false;
        }

        return true;
    }

    private boolean enviarMensagem(String mensagemStr, String ip, int porta) {

        DatagramPacket enviar = null;
        try {
            if (isBroadcast(ip, String.valueOf(porta))) {
                for (String[] str : clientesConectados) {
                    System.out.println(mensagemStr + "-" + str[1] + "-" + str[2]);
                    enviar = new DatagramPacket(mensagemStr.getBytes(), mensagemStr.getBytes().length,
                            InetAddress.getByName(str[1]), Integer.parseInt(str[2]));
                    serverDatagram.send(enviar);
                }
            } else {
                enviar = new DatagramPacket(mensagemStr.getBytes(), mensagemStr.getBytes().length,
                        InetAddress.getByName(ip), porta);
                serverDatagram.send(enviar);
                System.out.println("\n[SERVIDOR]: Mensagem enviada: "+mensagemStr);
            }

        } catch (Exception e) {
            System.out.println("\n[SERVIDOR]: Erro no método enviarMensagem!!!!\n" + e);

            return false;
        }
        return true;
    }

    private int encontraIndice(ArrayList<String[]> string, String ip, int porta) {
        int idx = 0;
        for (String[] str : string) {

            if (str[1].equals(ip) && str[2].equals(String.valueOf(porta))) {
                return idx;
            }
            idx++;
        }

        return -1;
    }

    // MÉTODOS PARA CRIAÇÃO E CONFIGURAÇÃO DA JANELA
    public static void main(String[] argv) {
        new ServerChatUDP();
    }

    private void CriaJanela() {
        frame = new JFrame("Mensagem");
        frame.setContentPane(jPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }

    private DefaultTableModel iniciaJTable(JTable table) {
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionModel(selectionModel);

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        tableModel.setColumnIdentifiers(new Object[]{"RA", "IP", "Porta"});
        tableModel.setNumRows(0);

        return tableModel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        serverTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        clientesJTable = new javax.swing.JTable();

        serverTextArea.setColumns(20);
        serverTextArea.setRows(5);
        jScrollPane1.setViewportView(serverTextArea);

        clientesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(clientesJTable);

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable clientesJTable;
    private javax.swing.JPanel jPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea serverTextArea;
    // End of variables declaration//GEN-END:variables

}
