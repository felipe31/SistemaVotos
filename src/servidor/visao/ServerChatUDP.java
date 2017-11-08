/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.visao;

import servidor.vo.Sala;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;
import servidor.controller.BancoClienteSingleton;
import servidor.controller.BancoSalasSingleton;
import servidor.vo.Cliente;
import servidor.vo.Voto;

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
    private BancoClienteSingleton bancoCliente = BancoClienteSingleton.getInstance();
    private BancoSalasSingleton bancoSala = BancoSalasSingleton.getInstance();
    private boolean isConectado = false;

    private ServerChatUDP() {
        initComponents();

        table = iniciaJTable(clientesJTable);

        this.CriaJanela();

        
    }
    
    private void pararServidor(){
        execServidor.interrupt();
        serverDatagram.close();
        execServidor = null;
    }
    
    private void iniciarServidor(){
        try {

            serverDatagram = new DatagramSocket(Integer.parseInt(jTextFieldPorta.getText()));

            execServidor = new Thread(() -> {
                serverTextArea.setText("[SERVIDOR]: Servidor iniciado na porta " + serverDatagram.getLocalPort() + "\n");

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

                        System.out.println("\n[SERVIDOR]: Mensagem recebida: " + receiveStr);

                        JSONObject jSONObject = new JSONObject(receiveStr);
                        String ip = receivePkt.getAddress().toString().split("/")[1];

                        switch ((int) jSONObject.get("tipo")) {
                            case 0:
                                System.out.println("Login");
                                if (verificaLogin(jSONObject.getString("ra"), jSONObject.get("senha").toString()) != null) {
                                    addConexao((String) jSONObject.get("ra"), ip, receivePkt.getPort());
                                    confimarLogin(jSONObject, ip, receivePkt.getPort());
                                    enviarListaSalas(ip, receivePkt.getPort());
                                } else {
                                    System.out.println("Usuário incorreto tentou se conectar.");
                                    JSONObject json = new JSONObject();
                                    json.put("tipo", 1);
                                    enviarMensagem(json.toString(), ip, receivePkt.getPort());
                                }
                                break;
                                
                                
                            case 3:
                                //criar sala
                                BancoSalasSingleton bancoSalasSingleton = BancoSalasSingleton.getInstance();
                                ArrayList<Voto> opcoes = new ArrayList<>();
                                
                                JSONArray jArray = jSONObject.getJSONArray( "opcoes" );
                                System.out.println(jArray);
                                
                                Iterator it = jArray.iterator();
                                while(it.hasNext())
                                {
                                    JSONObject jsono = (JSONObject) it.next();
                                    Voto voto = new Voto(jsono.getString("nome"));
                                    opcoes.add(voto);
                                }
                                String criador_ra = getConectado(ip, String.valueOf(receivePkt.getPort()))[0];
                                enviaSalaBroadcast(bancoSalasSingleton.criarSala(criador_ra,jSONObject.getString("nome"), jSONObject.getString("descricao"), jSONObject.getString("fim"), opcoes));   
                                
                                break;

                            case 10:
                                //logout
                                System.out.println("Logout");
                                removeConexao(ip, receivePkt.getPort());
                                break;
                            default:
                                System.out.println("Datagrama não suportado");

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
    //OOOOOLD AINDA NAO ALTERADO DO CHAT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void encaminharMensagem(String[] mensagem, String ip, int porta) {

        enviarMensagem("4#" + ip + "#" + porta + "#" + mensagem[3], mensagem[1], Integer.parseInt(mensagem[2]));

    }

    private boolean isBroadcast(String ip, String porta) {
        if (ip.equals("999.999.999.999") && porta.equals("99999")) {
            return true;
        }
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
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void enviarListaSalas(String ip, int porta) {

        Iterator it = bancoSala.getBancoSala().iterator();
        while (it.hasNext()) {
            Sala sala = (Sala) it.next();
            
            enviaSala(sala, ip, porta);
               
        }
    }
    
    /**
     * Retorna login se login for válido
     * null se não for válido
*/            
    private Cliente verificaLogin(String ra, String senha) {

        Cliente cliente = bancoCliente.getCliente(ra);
        //   Arrays.toString(senha).replace(" ","").equals(json.get("senha").toString());
        if(cliente == null) return null;
        System.out.println(cliente.getSenha() + "\n" + senha);
        if (cliente.getSenha().equals(senha)) {
            return cliente;
        }

        return null;
    }

    private void removeConexao(String ip, int porta) {
        int idx = encontraIndice(clientesConectados, ip, porta);
        if (idx > -1) {
            table.removeRow(idx);
            clientesConectados.remove(idx);
            // Precisa atualizar os outros conectados
//            enviaListaConectados("999.999.999.999", 99999);
            //mandar datagrama 2 para todos os conectados

        }
    }

    private void addConexao(String ra, String ip, int porta) {
        table.addRow(new Object[]{ra, ip, porta});
        clientesConectados.add(new String[]{ra, ip, String.valueOf(porta)});
        //  enviaListaConectados("999.999.999.999", 99999);
        //mandar datagrama 2 para todos os conectados
    }

    protected boolean confimarLogin(JSONObject json, String ip, int porta) {

        JSONObject obj = new JSONObject();

        obj.put("tipo", 2);
        obj.put("nome", bancoCliente.getCliente(json.getString("ra")).getNome());
        obj.put("tamanho", bancoSala.getQtdSalas());

        String mensagemStr = obj.toString();

        if (enviarMensagem(mensagemStr, ip, porta)) {
            System.out.println("\n[SERVIDOR]: Login confirmado com sucesso!");
            return true;
        } else {
            System.out.println("\n[SERVIDOR]: Erro ao confirmar o login!");
        }
        return false;
    }

    private boolean enviarMensagem(String mensagemStr, String ip, int porta) {

        DatagramPacket enviar = null;
        try {
            if (isBroadcast(ip, String.valueOf(porta))) {
                System.out.println("isBroadcast TRUE");
                for (String[] str : clientesConectados) {
                    enviarMensagem(mensagemStr, str[1], Integer.parseInt(str[2]));
                    
                }
            } else {
                enviar = new DatagramPacket(mensagemStr.getBytes(), mensagemStr.getBytes().length,
                        InetAddress.getByName(ip), porta);
                serverDatagram.send(enviar);
                System.out.println("\n[SERVIDOR]: Mensagem enviada: " + mensagemStr);
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
        jLabel1 = new javax.swing.JLabel();
        jTextFieldPorta = new javax.swing.JTextField();
        jButtonConectar = new javax.swing.JButton();

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

        jLabel1.setText("Porta:");

        jTextFieldPorta.setText("20000");

        jButtonConectar.setText("Conectar");
        jButtonConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConectarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldPorta, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonConectar)
                .addContainerGap())
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldPorta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonConectar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
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

    private void jButtonConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConectarActionPerformed
        if(isConectado){
            pararServidor();
            isConectado = false;
            jButtonConectar.setText("Conectar");
            jTextFieldPorta.setEnabled(true);
        } else{
            jTextFieldPorta.setEnabled(false);
            isConectado = true;
            jButtonConectar.setText("Desconectar");
            iniciarServidor();
        }
    }//GEN-LAST:event_jButtonConectarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable clientesJTable;
    private javax.swing.JButton jButtonConectar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldPorta;
    private javax.swing.JTextArea serverTextArea;
    // End of variables declaration//GEN-END:variables

    private void enviaSalaBroadcast(Sala sala) {
        enviaSala(sala, "999.999.999.999", 99999);
    }

    private void enviaSala(Sala sala, String ip, int porta) {
        JSONObject json;
        json = new JSONObject();

        json.put("tipo", 11);
        json.put("id", sala.getId());
        json.put("nome", sala.getNome());
        json.put("descricao", sala.getDescricao());
        json.put("criador", sala.getCriador_nome());
        json.put("inicio", sala.getInicio());
        json.put("fim", sala.getFim());
        json.put("status", sala.getStatus());
        json.put("mensagens", sala.getMensagens());

        String mensagemStr = json.toString();
        enviarMensagem(mensagemStr, ip, porta);
    }

    private String[] getConectado(String ip, String porta) {
        Iterator it = clientesConectados.iterator();
        String[] clienteConectado;
        while(it.hasNext()) {
            clienteConectado = (String[]) it.next();
            if(clienteConectado[1].equals(ip) && clienteConectado[2].equals(porta))
                return clienteConectado;
        }
        
        return null;
    }
    private String[] getConectado(String ra) {
        Iterator it = clientesConectados.iterator();
        String[] clienteConectado;
        while(it.hasNext()) {
            clienteConectado = (String[]) it.next();
            if(clienteConectado[0].equals(ra))
                return clienteConectado;
        }
        
        return null;
    }

}
