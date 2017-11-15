/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor.vo;

import java.util.Comparator;

/**
 *
 * @author Samsung
 */
public class ComparadorDeMensagem implements Comparator<Mensagem> {

    @Override
    public int compare(Mensagem t, Mensagem t1) {
       if (t.getTimestamp() < t1.getTimestamp()) return -1;
        else if (t.getTimestamp() > t1.getTimestamp()) return +1;
        else return 0;
    }
    
}
