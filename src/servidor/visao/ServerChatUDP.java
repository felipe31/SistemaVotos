/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.visao;

import java.net.BindException;
import servidor.vo.Sala;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;
import servidor.controller.BancoClienteSingleton;
import servidor.controller.BancoSalasSingleton;
import servidor.vo.Cliente;
import servidor.vo.Mensagem;
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

    private void pararServidor() {
        execServidor.interrupt();
        execServidor = null;
        clientesConectados.clear();
        table.setNumRows(0);
        serverDatagram.close();
    }

    private void iniciarServidor() throws Exception {

        serverDatagram = new DatagramSocket(Integer.parseInt(jTextFieldPorta.getText()));

        execServidor = iniciaThread();
    }

    private Thread iniciaThread() {
        Thread thread = new Thread(() -> {
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

                    // System.out.println("\n[SERVIDOR]: Mensagem recebida: " + receiveStr);
                    JSONObject jSONObject = new JSONObject(receiveStr);
                    String ip = receivePkt.getAddress().toString().split("/")[1];
                    System.out.println("[SERVIDOR] <- [IP: " + ip + "  PORTA: " + receivePkt.getPort() + "] :  MENSAGEM RECEBIDA: " + receiveStr);
                    if (jSONObject.has("tipo")) {
                        switch ((int) jSONObject.get("tipo")) {
                            case -1:
                                break;
                            case 0:
                                if (jSONObject.has("ra") && jSONObject.has("senha")) {
                                    System.out.println("[SERVIDOR] <- [IP: " + ip + "  PORTA: " + receivePkt.getPort() + "] : PEDIDO DE SOLICITAÇÃO DE LOGIN ");
                                    // System.out.println("\n[SERVIDOR]: Solicitação de login");

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
                                } else {
                                    mensagemMalFormada(jSONObject, ip, receivePkt.getPort());
                                }

                                break;

                            case 3:
                                boolean status = true;
                                //criar sala
                                if (jSONObject.has("opcoes") && jSONObject.has("nome") && jSONObject.has("descricao") && jSONObject.has("fim")) {
                                    System.out.println("[SERVIDOR] <- [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] : PEDIDO DE CRIAÇÃO DE SALA ");
                                    BancoSalasSingleton bancoSalasSingleton = BancoSalasSingleton.getInstance();
                                    ArrayList<Voto> opcoes = new ArrayList<>();

                                    JSONArray jArray = jSONObject.getJSONArray("opcoes");
                                    System.out.println(jArray);

                                    Iterator it = jArray.iterator();
                                    while (it.hasNext()) {
                                        JSONObject jsono = (JSONObject) it.next();
                                        if (jsono.has("nome")) {
                                            Voto voto = new Voto(jsono.getString("nome"));
                                            opcoes.add(voto);
                                        } else {
                                            opcoes.clear();
                                            status = false;
                                        }
                                    }
                                    if (status = true) {

                                        String criador_ra = getConectado(ip, String.valueOf(receivePkt.getPort()))[0];
                                        enviaSalaBroadcast(bancoSalasSingleton.criarSala(criador_ra, jSONObject.getString("nome"), jSONObject.getString("descricao"), jSONObject.getString("fim"), opcoes), true);
                                        status = true;
                                    } else {
                                        mensagemMalFormada(jSONObject, ip, receivePkt.getPort());
                                    }

                                } else {
                                    mensagemMalFormada(jSONObject, ip, receivePkt.getPort());
                                }
                                break;
                            case 5:
                                // solicitação de acesso à sala
                                System.out.println("[SERVIDOR] <- [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] : SOLICITAÇÃO DE ACESSO A SALA");

                                //  System.out.println("\n[SERVIDOR]: Solicitação de acesso à sala");
                                if (jSONObject.has("id")) {
                                    concederAcessoSala(jSONObject.getInt("id"), ip, String.valueOf(receivePkt.getPort()));
                                } else {
                                    mensagemMalFormada(jSONObject, ip, receivePkt.getPort());
                                }

                                break;
                            case 8:
                                //enviar mensagem para a sala que vc esta
                                encaminharMensagem(BancoSalasSingleton.getInstance().getSala(BancoClienteSingleton.getInstance().getCliente(
                                        getConectado(ip, String.valueOf(receivePkt.getPort()))[0]).getSalaAtual()),
                                        jSONObject.getString("criador"), jSONObject.getString("mensagem"));
                                break;
                            case 10:
                                //logout
                                System.out.println("Logout");
                                removeConexao(ip, receivePkt.getPort());
                                break;
                            case 15:
                                System.out.println("[IP: " + ip + " PORTA: " + receivePkt.getPort() + "] -> [SERVIDOR] : VOTO");
                                if (armazenarVoto(jSONObject.getInt("sala"), jSONObject.getString("opcao"), ip, receivePkt.getPort()));
                                enviarMensagem(jSONObject.toString(), ip, receivePkt.getPort());
                                break;
                            default:
                                mensagemMalFormada(jSONObject, ip, receivePkt.getPort());
                                System.out.println("Datagrama não suportado");

                        }
                    }
                    Thread.sleep(100);
                }

            } catch (SocketException e) {
            } catch (Exception e) {
                System.out.println(e);
                execServidor = iniciaThread();
            }
        });

        thread.start();
        return thread;
    }

    // PROCESSAMENTO DOS DATAGRAMAS RECEBIDOS
    //OOOOOLD AINDA NAO ALTERADO DO CHAT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

    private void concederAcessoSala(int idSala, String ip, String porta) {
        /*
        TODO:
    OK      Cadastrar usuário na sala
    OK      Enviar todos os usuários conectados na sala
            Enviar mensagens
         */
        BancoSalasSingleton salasBanco = BancoSalasSingleton.getInstance();
        BancoClienteSingleton clientesBanco = BancoClienteSingleton.getInstance();
        Sala sala = salasBanco.getSala(idSala);
        Cliente cliente = clientesBanco.getCliente(getConectado(ip, porta)[0]);

        enviarClienteASerConectadoSala(sala, cliente.getNome());

        // Cadastra usuário na sala
        sala.addClienteConectado(cliente);
        cliente.setSalaAtual(idSala);

        // Envia a lista atualizada para todos os clientes da sala
        enviarClientesConectadosSala(sala, ip, porta);

        enviarHistoricoSala(sala, ip, porta);
        enviarVotosSala(sala, ip, Integer.parseInt(porta));
    }

    private void encaminharMensagemHistorico(String ip, String porta, Mensagem mensagem) {
        JSONObject json = new JSONObject();
        json.put("tipo", 9);
        //json.put("id", );
        json.put("timestamp", mensagem.getTimestamp());
        json.put("criador", mensagem.getCriador());
        json.put("mensagem", mensagem.getMensagem());
        enviarMensagem(json.toString(), ip, receivePkt.getPort());

    }

    private void encaminharMensagem(Sala sala, String criador, String mensagem) {
//        9 = mensagem do servidor
//{
//	"tipo":9,
//	"id":numero_da_mensagem,
//	"timestamp":"unix_time",
//	"criador":"nome do cara que escreveu a mensagem",
//	"mensagem":"string de até 1000 caracteres"
//}
        String aux = String.valueOf(System.currentTimeMillis() / 1000);
        JSONObject json = new JSONObject();
        json.put("tipo", 9);
        //json.put("id", );
        json.put("timestamp", aux);
        json.put("criador", criador);
        json.put("mensagem", mensagem);
        String[] conectado;

        Mensagem mensagem1 = new Mensagem();
        mensagem1.setCriador(criador);
        mensagem1.setMensagem(mensagem);
        mensagem1.setTimestamp(Long.valueOf(aux));

        sala.getMensagemDaSala().add(mensagem1);

        for (Cliente c : sala.getClientesConectados()) {
            conectado = getConectado(c.getRa());
            enviarMensagem(json.toString(), conectado[1], Integer.parseInt(conectado[2]));
        }

    }

    private void addVoto(int sala, String ip, String porta) {
        /*
        TODO: 
            Add voto na sala
            Add voto no cliente
         */
    }

    private void enviarListaSalas(String ip, int porta) {

        Iterator it = bancoSala.getBancoSala().iterator();
        while (it.hasNext()) {
            Sala sala = (Sala) it.next();
            enviaSala(sala, ip, porta, false);
        }
    }

    /**
     * Retorna login se login for válido null se não for válido
     */
    private Cliente verificaLogin(String ra, String senha) {
        if (getConectado(ra) != null) {
            return null;
        }

        Cliente cliente = bancoCliente.getCliente(ra);
        //   Arrays.toString(senha).replace(" ","").equals(json.get("senha").toString());
        if (cliente == null) {
            return null;
        }
        //System.out.println(cliente.getSenha() + "\n" + senha);
        if (cliente.getSenha().equals(senha)) {
            return cliente;
        }
        return null;
    }

    private void removeConexao(String ip, int porta) {
        String portaStr = String.valueOf(porta);
        try {
            int idx = 0;
            for (Object v : table.getDataVector()) {
                System.out.println("analisando - " + ((Vector) v).get(1) + "/" + ((Vector) v).get(2));
                if (((Vector) v).get(1).equals(ip) && ((Vector) v).get(2).equals(porta)) {
                    table.removeRow(idx);
                    clientesConectados.remove(idx);
                    return;
                }
                idx++;
            }
        } catch (Exception e) {
            System.out.println("Erro. Causa:" + e.getCause().toString());

        }

    }

    private void addConexao(String ra, String ip, int porta) {
        table.addRow(new Object[]{ra, ip, porta});
        clientesConectados.add(new String[]{ra, ip, String.valueOf(porta)});
        Cliente cliente = BancoClienteSingleton.getInstance().getCliente(ra);
        cliente.setIp(ip);
        cliente.setPorta(String.valueOf(porta));
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
            System.out.println("[SERVIDOR] -> [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] :  CONFIRMAÇÃO DE LOGIN BEM SUCEDIDA");
            // System.out.println("\n[SERVIDOR]: Login confirmado com sucesso!");
            return true;
        } else {
            System.out.println("[SERVIDOR] -> [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] :  CONFIRMAÇÃO DE LOGIN MAL SUCEDIDA");
            //   System.out.println("\n[SERVIDOR]: Erro ao confirmar o login!");
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
                System.out.println("[SERVIDOR] -> [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] :  MENSAGEM ENVIADA: " + mensagemStr);
                //  System.out.println("\n[SERVIDOR]: Mensagem enviada: "+ mensagemStr );
            }

        } catch (Exception e) {
            System.out.println("[SERVIDOR] -> [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] : Erro no método enviarMensagem!!! \n" + e);
            //System.out.println("\n[SERVIDOR]: Erro no método enviarMensagem!!!!\n" + e);

            return false;
        }
        return true;
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
        if (isConectado) {
            try {
                pararServidor();
                isConectado = false;
                jButtonConectar.setText("Conectar");
                jTextFieldPorta.setEnabled(true);
            } catch (Exception e) {
                System.out.println("Exception ao desconectar o servidor: " + e.getCause().toString());
            }
        } else {
            try {
                jTextFieldPorta.setEnabled(false);
                isConectado = true;
                jButtonConectar.setText("Desconectar");
                iniciarServidor();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Não foi possível conectar!\nTente uma porta diferente!", "Erro!", JOptionPane.ERROR_MESSAGE);
                isConectado = false;
                jButtonConectar.setText("Conectar");
                jTextFieldPorta.setEnabled(true);
            }
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

    private void enviaSalaBroadcast(Sala sala, boolean novaSala) {
        enviaSala(sala, "999.999.999.999", 99999, novaSala);
    }

    private void enviaSala(Sala sala, String ip, int porta, boolean novaSala) {
        JSONObject json;
        json = new JSONObject();

        if (novaSala) {
            json.put("tipo", 4);
        } else {
            json.put("tipo", 11);
            json.put("mensagens", sala.getQtdMensagens());
        }

        json.put("id", sala.getId());
        json.put("nome", sala.getNome());
        json.put("descricao", sala.getDescricao());
        json.put("criador", sala.getCriador_nome());
        json.put("inicio", sala.getInicio());
        json.put("fim", sala.getFim());
        json.put("status", sala.getStatus());

        String mensagemStr = json.toString();
        enviarMensagem(mensagemStr, ip, porta);
    }

    private String[] getConectado(String ip, String porta) {
        Iterator it = clientesConectados.iterator();
        String[] clienteConectado;
        while (it.hasNext()) {
            clienteConectado = (String[]) it.next();
            if (clienteConectado[1].equals(ip) && clienteConectado[2].equals(porta)) {
                return clienteConectado;
            }
        }

        return null;
    }

    private String[] getConectado(String ra) {
        Iterator it = clientesConectados.iterator();
        String[] clienteConectado;
        while (it.hasNext()) {
            clienteConectado = (String[]) it.next();
            if (clienteConectado[0].equals(ra)) {
                return clienteConectado;
            }
        }

        return null;
    }

    private void mensagemMalFormada(JSONObject jsonMsg, String ip, Integer porta) {
        JSONObject json = new JSONObject();
        
        json.put("tipo", -1);
        json.put("pacote", jsonMsg);
        System.out.println("[SERVIDOR] -> [IP: " + ip + " PORTA: " + receivePkt.getPort() + "] :  MENSAGEM ENVIADA: " + jsonMsg.toString());
        enviarMensagem(json.toString(), ip, porta);
    }

    private void enviarClientesConectadosSala(Sala sala, String ip, String porta) {
//        6 = historico e usuários, do servidor
//{
//	"tipo":6,
//	"tamanho":666 //id_maximo
//	"usuarios":[
//		{"nome":"nome_do_cara"},
//		...
//	]
//}

        String[] ipPortaConectado;
        JSONObject json = new JSONObject();
        JSONArray jsonArrayConectados = new JSONArray();
        JSONObject jsonConectado;
        json.put("tipo", 6);

        for (Cliente c : sala.getClientesConectados()) {
            jsonConectado = new JSONObject();
            jsonConectado.put("nome", c.getNome());
            jsonArrayConectados.put(jsonConectado);
            ipPortaConectado = getConectado(c.getRa());
        }
        json.put("usuarios", jsonArrayConectados);
        enviarMensagem(json.toString(), ip, Integer.parseInt(porta));
    }

    private void enviarHistoricoSala(Sala sala, String ip, String porta) {

        Iterator it = sala.getMensagemDaSala().iterator();
        while (it.hasNext()) {
            Mensagem m = (Mensagem) it.next();
//            System.out.println(m.getMensagem());
//            System.out.println(m.getCriador());
//            System.out.println(m.getTimestamp());
//            System.out.println(m.getId());
//            JSONObject json = new JSONObject();
//            json.put("tipo",9);
//            json.put("id",m.getTimestamp());
//            JSONArray jsonArrayConectados = new JSONArray();

            //encaminharMensagem(sala, porta, porta);
            encaminharMensagemHistorico(ip, porta, m);

        }

        //        6 = historico e usuários, do servidor
//{
//	"tipo":6,
//	"tamanho":666 //id_maximo
//	"mensagens":[
//		{"nome":"nome_do_cara"},
//		...
//	]
//}
//        JSONObject json = new JSONObject();
//        JSONArray jsonArrayConectados = new JSONArray();
//        JSONObject jsonConectado;
//        json.put("tipo", 6);
//        
//        for(Cliente c : sala.getClientesConectados()){
//            jsonConectado = new JSONObject();
//            jsonConectado.put("nome", c.getNome());
//            jsonArrayConectados.put(jsonConectado);
//        }
//        json.put("usuarios", jsonArrayConectados);
//        
//        enviarMensagem(json.toString(), ip, Integer.parseInt(porta));
//    }
    }

    private void enviarVotosSala(Sala sala, String ip, int porta) {
//        7 = status da votação
//{
//	"tipo":7,
//	"acabou":false,
//	"resultados":[                 // explodir tamanho do udp?
//		{"nome_da_opcao":numero_de_votos}, // 0 enquanto não terminou?
//		...
//	]
//}

        JSONObject jsonVoto, json = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        json.put("tipo", 7);
        json.put("acabou", (Long.parseLong(sala.getFim()) < System.currentTimeMillis() / 1000));

        for (Voto v : sala.getOpcoes()) {
            jsonVoto = new JSONObject();
            jsonVoto.put(v.getDescricao(), v.getContador());
            jsonArray.put(jsonVoto);
        }

        json.put("resultados", jsonArray);
        enviarMensagem(json.toString(), ip, porta);
    }

    private boolean armazenarVoto(int idSala, String opcao, String ip, int porta) {
        BancoSalasSingleton salasBanco = BancoSalasSingleton.getInstance();
        Sala sala = salasBanco.getSala(idSala);
        Cliente cliente = BancoClienteSingleton.getInstance().getCliente(getConectado(ip, String.valueOf(porta))[0]);

        return sala.addVoto(opcao, cliente);
    }

    private void enviarClienteASerConectadoSala(Sala sala, String nome) {
//        16 = desconectar/conectar usuário
//{
//	"tipo":16,
//	"adicionar":true/false,
//	"nome":"nome_do_usuario"
//}

        JSONObject json = new JSONObject();

        json.put("tipo", 16);
        json.put("adicionar", true);
        json.put("nome", nome);

        sala.getClientesConectados().forEach((c) -> {
            enviarMensagem(json.toString(), c.getIp(), Integer.parseInt(c.getPorta()));
        });
    }
}
