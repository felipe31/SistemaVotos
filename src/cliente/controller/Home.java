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
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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
    private DatagramSocket clienteSocket = null;
    private int qtdSalas;
    private DefaultTableModel salasTabela;
    private cliente.controller.Sala salaCtrl = null;
    private cliente.visao.Sala salaVisao = null;
    private cliente.visao.Home homeVisao;
    private boolean votoRetornou;

    public Home(cliente.visao.Home homeVisao, Cliente cliente, DatagramSocket clienteSocket, DefaultTableModel salasTabela, int qtdSalas) {
        this.homeVisao = homeVisao;
        this.cliente = cliente;
        this.clienteSocket = clienteSocket;
        this.qtdSalas = qtdSalas;
        this.salasTabela = salasTabela;
        abrirRecepcaoJSON(clienteSocket, cliente.getIp(), cliente.getPorta());
    }

    private void receberSala(JSONObject json, boolean novaSala) {
        BancoSalasSingleton bancoSalas = BancoSalasSingleton.getInstance();

        int mensagens;
        if (json.has("mensagens")) {
            mensagens = novaSala ? 0 : json.getInt("mensagens");
        } else {
            mensagens = 0;
        }
        if (json.has("id") && json.has("criador") && json.has("nome") && json.has("descricao") && json.has("inicio") && json.has("fim") && json.has("status")) {
            Sala sala = new Sala(json.getInt("id"),
                    json.getString("criador"),
                    json.getString("nome"),
                    json.getString("descricao"),
                    json.getString("inicio"),
                    json.getString("fim"),
                    null,
                    json.getBoolean("status"),
                    mensagens);
            bancoSalas.addSala(sala);
            addSalaVisao(sala);
        }
        else
        {
            System.out.println("MENSAGEM MAL FORMADA: " + json.toString());
        }

    }

    private void addSalaVisao(Sala sala) {
        String[] dados;
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date((long) Long.parseLong(sala.getFim()) * 1000));
        dados = new String[]{String.valueOf(sala.getId()), sala.getNome(),
            sala.getDescricao(), sala.isStatus() ? "Ativo" : "Inativo",
            sala.getCriador(),
            String.valueOf(c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR)
            + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE))};
        if (salasTabela != null) {
            salasTabela.addRow(dados);
        } else {
            System.out.println("Deu pau ao imprimir sala na tabela");
        }
    }

    public void solicitarAcessoSala(String id) {

//5 = cliente pedindo acesso a sala
//{
//	"tipo":5,
//	"id":id_da_sala_que_o_cliente_quer_entrar
//}
        salaVisao = new cliente.visao.Sala(homeVisao, cliente, clienteSocket, Integer.parseInt(id));
        salaVisao.setVisible(true);
        homeVisao.setVisible(false);
        salaCtrl = salaVisao.getSalaCtrl();

        BancoSalasSingleton bancoSalas = BancoSalasSingleton.getInstance();

        Sala sala = bancoSalas.getSala(Integer.parseInt(id));
        if (sala != null) {
            JSONObject json = new JSONObject();
            json.put("tipo", 5);
            json.put("id", sala.getId());
            jsonOp.enviarJSON(json, clienteSocket, cliente.getIp(), cliente.getPorta());
        }

    }

    public void criaSala(JSONObject jsonSala) {
        jsonSala.put("tipo", 3);
        jsonOp.enviarJSON(jsonSala, clienteSocket, cliente.getIp(), cliente.getPorta());
    }

    private JSONObject abrirRecepcaoJSON(DatagramSocket socket, String ip, String porta) {
        recebimentoThread = new Thread(() -> {
            try {
                String receiveStr;
                DatagramPacket mensagemPkt = new DatagramPacket(new byte[10000], 10000, InetAddress.getByName(ip), Integer.parseInt(porta));
                while (true) {
                    mensagemPkt.setData(new byte[10000]);
                    socket.receive(mensagemPkt);
                    receiveStr = new String(mensagemPkt.getData());
                    receiveStr = receiveStr.trim();
                    JSONObject jsonObj = new JSONObject(receiveStr);
                    System.out.println("\n[CLIENTE]: Mensagem recebida: " + jsonObj.toString());

                    if (jsonObj.has("tipo")) {
                        //System.out.println("\n[CLIENTE]: Recepção de mensagem");

                        switch (jsonObj.getInt("tipo")) {
                            case -1:
                                System.out.println("\n[CLIENTE]: Mensagem mal formada");

                                break;
                            case 11:
                                System.out.println("\n[CLIENTE]: Recepção de salas após o login");
                                receberSala(jsonObj, false);
                                break;
                            case 4:
                                System.out.println("\n[CLIENTE]: Recepção de sala nova");
                                receberSala(jsonObj, true);
                                break;
                            case 6:
                                System.out.println("\n[CLIENTE]: Recepção de clientes conectados");
                                if (salaCtrl != null) {
                                    if (jsonObj.has("usuarios")) {
                                        salaCtrl.receberClientesConectados(jsonObj.getJSONArray("usuarios"));
                                    } else {
                                        //mensagem mal formada
                                    }

                                }

                                break;
                            case 7:
                                if (salaCtrl != null) {
                                    System.out.println("\n[CLIENTE]: Recepção de status da votação");
                                    salaCtrl.receberVotacao(jsonObj.getJSONArray("resultados"));
                                }
                                break;

                            case 9:
                                System.out.println("\n[CLIENTE]: Recepção de mensagem");

                                if (salaCtrl != null) {
                                    salaCtrl.receberMensagem(jsonObj);
                                }
                                break;
                            case 15:
                                if (salaCtrl != null) {
                                    votoRetornou = true;
                                    System.out.println("\n[CLIENTE]: Confirmação de voto");
                                    JOptionPane.showMessageDialog(null, "Voto realizado com sucesso!\nVocê votou na opção:\n" + jsonObj.getString("opcao"), "Voto realizado", JOptionPane.INFORMATION_MESSAGE);
                                }
                                break;
                            case 16:
                                if (salaCtrl != null) {
                                    salaCtrl.receberAlteracaoClienteConectado(jsonObj);
                                }
                                break;
                            default:
                                mensagemMalFormada(jsonObj, ip, porta);
                                System.out.println("Datagrama não suportado");
                        }
                    } else {
                    }

                    receiveStr = null;
                    Thread.sleep(100);
                }
            } catch (UnknownHostException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SocketException e) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            } catch (IOException e) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            } catch (InterruptedException e) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
            }

        }
        );

        recebimentoThread.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    private void mensagemMalFormada(JSONObject jsonMsg, String ip, String porta) {
        JSONObject json = new JSONObject();

        json.put("tipo", -1);
        json.put("pacote", jsonMsg);
        jsonOp.enviarJSON(json, clienteSocket, ip, porta);
    }

    public void setSala(cliente.visao.Sala sv, cliente.controller.Sala sc) {
        this.salaVisao = sv;
        this.salaCtrl = sc;

    }

    public void iniciaThreadVoto() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (!votoRetornou) {
                    JOptionPane.showMessageDialog(null, "Não foi possível confirmar o voto!\nTente novamente.", "Erro na confirmação do voto", JOptionPane.ERROR_MESSAGE);
                } else {
                    votoRetornou = false;

                }
            } catch (InterruptedException ex) {
                Logger.getLogger(cliente.visao.Home.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

}
