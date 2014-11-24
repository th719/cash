/*
 * DocCashe.java
 *
 * Created on 20 Ноябрь 2007 г., 17:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package th.cash.ui.sale;

import th.common.db.VectorDataCashe;
import th.common.db.VRow;
import th.common.util.RMath;

import th.cash.fr.doc.Position;

/**
 * Кэш данных для визуализации документа продажи/покупки
 * @author lazarev
 */
public class DocCashe extends VectorDataCashe {
    
    
    public final static int OPER_TYPE = 0;
    public final static int NUM_POS   = 1;
    public final static int GNAME     = 2;
    public final static int QUAN      = 3;
    public final static int PRICE     = 4;
    public final static int SUM       = 5;
    // hidden data
    public final static int POS_DATA  = 6;
    
    public final static int NUM_COLS  = 8;
    
    /** Creates a new instance of DocCashe */
    public DocCashe() {
        super(20, 20);
    }
    
    
    // перенумеровать действительные позиции
    public void renumber()
    {
        int i = 0;
        VRow row;
        int num = 0;
        for (i = 0; i < size(); i++)
        {
          row = (VRow)get(i);
          if ( !Position.CANCEL_POS.equals(row.get(OPER_TYPE)) )
          {
            row.set(NUM_POS, new Integer(++num));
            ((Position)row.get(POS_DATA)).setNum((Integer)row.get(NUM_POS));
          }
        }
    }

    public int getNumPos()
    {
      int i = 0;
      int cnt = 0; // количество регистраций (без сторно)
      VRow row;
      for (i = 0; i < size(); i++)
      {
        row = (VRow)get(i);
        if (!Position.CANCEL_POS.equals(row.get(OPER_TYPE))) cnt++;
      }
        
      return cnt + 1;
    }
        
    
    // создать новую строку
    public VRow createNewRow_()
    {
        return createNewRow(NUM_COLS);
    }


    public void initRowByPos(VRow row, Position p)    
    {
      row.set(NUM_POS, p.getNum());
      row.set(OPER_TYPE, p.getType());
      row.set(QUAN, p.getQuantity());
      row.set(PRICE, p.getPrice());
      row.set(SUM, p.getSum());
      row.set(GNAME, p.getGname());
      row.set(POS_DATA, p);
    }
    
    
}
