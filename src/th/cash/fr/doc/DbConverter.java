package th.cash.fr.doc;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.ResultSet;


import th.cash.model.SprModel;
import th.cash.model.Tax;
import th.cash.model.User;

import java.util.Date;
import java.util.Vector;

/**
 * Класс для сохранения/чтения документов
 * методы статические, не синхронизированы ... вызывать аккуратно!
 * 
 */
public class DbConverter 
{


  /**
   * Сохраняет все транзакции документа и чистим буффер COMMITED !!!
   */

  private final static String SAVE_TR_STMT = 
    "insert into t_tr(id,t_d,t_t,k_n,ch_n,ca_n,s_d,i_d,f1,f2,f3) values(?,?,?,?,?,?,?,?,?,?,?)";

  private final static String SAVE_ARCH_STMT = 
    "insert into t_tr_arch(id,t_d,t_t,k_n,ch_n,ca_n,s_d,i_d,f1,f2,f3) values(?,?,?,?,?,?,?,?,?,?,?)";

  private final static String SEQ_VAL_STMT = "select nextval('t_tr_id_seq')";

  private final static String CLEAR_BF_STMT = "delete from t_check_bf";

  public static void saveFDoc(Connection c, FDoc doc, boolean tr_log) throws SQLException
  {
    Vector trv = doc.transactions;

    saveTrVector(c, trv, tr_log);

    if (trv != null && trv.size() > 0)
    {
      
      // чистим буффер
      // Z, X -отчет может закрываться и при открытом чеке,
      // поэтому очистку не делаем
      if (doc.getTypeId() != FDoc.FD_ZREPORT_TYPE && doc.getTypeId() != FDoc.FD_XREPORT_TYPE)
      {
        Statement st = c.createStatement();
        st.executeUpdate(CLEAR_BF_STMT);
        st.close();
      }
        
      c.commit();
    }

  }

  // метод без commit !
  public static void saveTrVector(Connection c, Vector trv, boolean tr_log) throws SQLException
  {

    int num_tr = trv == null ? 0 : trv.size();
      
    if (num_tr > 0)
    {

      LogConverter lconv = null;
      if (tr_log) lconv = new LogConverter();
      // сохраняем транзакции
      Statement sel_seq = c.createStatement();
      ResultSet rs;
        
      PreparedStatement tr_pst = c.prepareStatement(SAVE_TR_STMT);
      PreparedStatement tr_arch_pst = c.prepareStatement(SAVE_ARCH_STMT);
      Transaction t;
      int id;
        
      for (int i = 0; i < num_tr; i++)
      {
        t = (Transaction)trv.get(i);

        rs = sel_seq.executeQuery(SEQ_VAL_STMT);
        rs.next();
        id = rs.getInt(1);
        rs.close();

        if (t.getType() != t.REG_VALUE) // 24 - игнорируется
        {
          setTransParams(tr_pst, id, t);
          tr_pst.executeUpdate();
        }

        setTransParams(tr_arch_pst, id, t);
        tr_arch_pst.executeUpdate();

        if (tr_log) lconv.logTransaction(id, t);
      }

      sel_seq.close();
      tr_pst.close();
      tr_arch_pst.close();

    }
  }

  /**
   * сохраняет одну транзакцию ... COMMITED!
   */
  public static void saveTransaction(Connection c, Transaction t, boolean tr_log) throws SQLException
  {
    // читаем номер транзакции
    Statement sel_seq = c.createStatement();
    ResultSet rs = sel_seq.executeQuery(SEQ_VAL_STMT);
    rs.next();
    int id = rs.getInt(1);
    rs.close();
    sel_seq.close();
        
    PreparedStatement tr_pst = c.prepareStatement(SAVE_TR_STMT);
    PreparedStatement tr_arch_pst = c.prepareStatement(SAVE_ARCH_STMT);

    // пишем в таблицу обмена
    setTransParams(tr_pst, id, t);
    tr_pst.executeUpdate();

    // пишем в архив
    setTransParams(tr_arch_pst, id, t);
    tr_arch_pst.executeUpdate();

    tr_pst.close();
    tr_arch_pst.close();

    c.commit();    

    if (tr_log) new LogConverter().logTransaction(id, t);
  }


  /**
   * Подстановка параметров в выражение для сохранения транзакции
   */
  private static void setTransParams(PreparedStatement pst, int id, Transaction t) throws SQLException
  {
    int pcnt = 0; 
    pst.setInt(++pcnt, id);
    pst.setTimestamp(++pcnt, new Timestamp( t.getTrDate().getTime() ));
    pst.setInt(++pcnt, t.getType());
    pst.setInt(++pcnt, t.getKkmNum());
    pst.setInt(++pcnt, t.getCheckNum());
    pst.setInt(++pcnt, t.getCashierNum());
    pst.setString(++pcnt, t.getStr());
    pst.setInt(++pcnt, t.getIval());
    pst.setDouble(++pcnt, t.getD10());
    pst.setDouble(++pcnt, t.getD11());
    pst.setDouble(++pcnt, t.getD12());
  }

  /**
   * сохраняем в буффер текущую транзакцию  COMMITED!
   */
  private final static String SAVE_CHECK_BF = 
      "insert into t_check_bf(t_d,t_t,k_n,ch_n,ca_n,s_d,i_d,f1,f2,f3,gname) values(?,?,?,?,?,?,?,?,?,?,?)";
  protected static void saveTransToBuf(Connection c, Transaction t) throws SQLException
  {
    PreparedStatement pst = c.prepareStatement(SAVE_CHECK_BF);
    int pcnt = 0;

    pst.setTimestamp(++pcnt, new Timestamp( t.getTrDate().getTime() ));
    pst.setInt(++pcnt, t.getType());
    pst.setInt(++pcnt, t.getKkmNum());
    pst.setInt(++pcnt, t.getCheckNum());
    pst.setInt(++pcnt, t.getCashierNum());
    pst.setString(++pcnt, t.getStr());
    pst.setInt(++pcnt, t.getIval());
    pst.setDouble(++pcnt, t.getD10());
    pst.setDouble(++pcnt, t.getD11());
    pst.setDouble(++pcnt, t.getD12());
    pst.setString(++pcnt, t.getGname());

    pst.executeUpdate();

    c.commit();

//    if (tr_log) new LogConverter().logTransaction(id, t);

  }


  /**
   * восстанавливает не закрытый  чек продажи или возврата
   */
  public static FDoc createFDocFromBuf(Connection c, SprModel model) throws SQLException
  {
    Vector tr = loadTransFromBuf(c);

    if (tr == null) return null;

    FDoc res; 
    Transaction t;

    // определяем тип документа по первой транзакции
    t = (Transaction)tr.get(0);

    switch (t.getType())
    {
      case Transaction.REG_POS : res = new SaleCheck(); break;
      case Transaction.RET_POS : res = new ReturnCheck(); break;
      default : res = null;
    }

    if (res != null)
    {
      Vector ch_pos = new Vector(); // заполняем позиции в чеке

      // код кассира для чека должен ставиться при закрытии
      //res.setCashierId();
      //res.setCashierName();
      res.setCashNum(t.getKkmNum());
      res.setDocNum(t.getCheckNum());

      Vector pos = new Vector();
      Position p = null;

      // var
      Integer good_id; // код товара
      String gname;
      Integer ptype; // тип позиции
      Double price;
      Double quan;

      Tax tax;
      Double nalPc = null;
      String tname = null;
      Integer gtaxId;
      
      int i = 0;
      while (i < tr.size())
      {
        t = (Transaction)tr.get(i);

        if (t.getType() == t.REG_POS || t.getType() == t.RET_POS || t.getType() == t.STORNO_POS)
        {
          // разбор транзакции (одинаков для регистрации, возврата и сторно)
          good_id = new Integer(t.getStr()); // код
          gname = t.getGname();              // наим. товара
          price = new Double(t.getD10());    // цена
          quan = new Double(t.getD11());     // количество 
          gtaxId = t.getGtaxId();            // налог 

          if (gtaxId != null)
          {
            tax = model.searchTax(gtaxId);
            if (tax == null)
            {
              gtaxId = null;
              nalPc = null;
              tname = null;
            } else
            {
              nalPc = tax.getTaxPc();
              tname = tax.getTaxName();
            }
          } 

          switch (t.getType())
          {

            case Transaction.REG_POS : // регистрация 
                ch_pos.add( p = new Position(null, ptype = Position.SALE_POS, gname,
                            quan, price, good_id, null, null, gtaxId, nalPc, tname) ); 
              break;

            case Transaction.RET_POS : // возврат продажи
                ch_pos.add( p = new Position(null, ptype = Position.SALE_RET_POS, gname,
                            quan = new Double(-quan.doubleValue()), price, good_id, null, null, gtaxId, nalPc, tname) ); 
              break;

            case Transaction.STORNO_POS : // сторно какой то из позиций
                int st_pos_index = 0; // индекс позиции которую будем сторнировать
                boolean finded = false;
                // в чеке продажи, в сторно отр. кол-во, в возврате продажи - положительное
                quan = new Double( (res instanceof SaleCheck) ? -quan.doubleValue() : quan.doubleValue()); 
                // по коду товара, цене и количеству из транзакции ищем позицию
                while (st_pos_index < ch_pos.size() && !finded)
                {
                  p = (Position)ch_pos.get( st_pos_index );

                  // если это не сторно - сравниваем 
                  if ( !Position.CANCEL_POS.equals(p.getType()))
                    finded = good_id.equals(p.getGoodId()) && price.equals(p.getPrice()) && quan.equals(p.getQuantity());
                  if (!finded) st_pos_index++;
                }
                if (finded)
                  p.setType(Position.CANCEL_POS);
              break;

            default : ptype = null;
          }
            
        } else

        if (t.getType() == t.REG_VALUE) // если регистрация единицы, добавляем ш/к
                                        // для предыдущей в списке
        {
          int pos_index = -1;
          if (ch_pos.size() > 0) pos_index = ch_pos.size() - 1;
          if (pos_index >= 0)
          {
            p = (Position)ch_pos.get(pos_index);
            p.setBarcode(t.getStr());
            p.setKoef(new Double(t.getD11()));
          }
        }
        i++;
      }

      // транзакции
      ((Check)res).setTranscactions(tr);
      // позиции
      ((Check)res).setPositions(ch_pos);
    }
    
    
    return res;
  }


  /**
   * Чтение транзакций из временной таблицы
   */
  private final static String SEL_TR = 
    "select id,t_d,t_t,k_n,ch_n,ca_n,s_d,i_d,f1,f2,f3,gname,gt_id from t_check_bf order by id";
  private static Vector loadTransFromBuf(Connection c) throws SQLException
  {
    Statement st = c.createStatement();
    ResultSet rs = st.executeQuery(SEL_TR);

    Vector res = new Vector();
    Transaction t;
    int id, ival;
    
    Timestamp t_d;
    int t_t, k_n, ch_n, ca_n;
    String s_d;
    int i_d;
    double f1,f2,f3, dval;
    String gname;
    Integer gtax_id;
    int pcnt;
    
    while (rs.next())
    {
      pcnt = 0;
      id = rs.getInt(++pcnt);
      t_d = rs.getTimestamp(++pcnt);
      t_t = rs.getInt(++pcnt);
      k_n = rs.getInt(++pcnt);
      ch_n = rs.getInt(++pcnt);
      ca_n = rs.getInt(++pcnt);
      s_d = rs.getString(++pcnt);
      i_d = rs.getInt(++pcnt); i_d = rs.wasNull() ? 0 : i_d;
      f1 = rs.getDouble(++pcnt); f1 = rs.wasNull() ? 0 : f1;
      f2 = rs.getDouble(++pcnt); f2 = rs.wasNull() ? 0 : f2;
      f3 = rs.getDouble(++pcnt); f3 = rs.wasNull() ? 0 : f3;
      gname = rs.getString(++pcnt);
      ival = rs.getInt(++pcnt); gtax_id = rs.wasNull() ? null : new Integer(ival);

      t = new Transaction(id, new Date(t_d.getTime()), t_t, k_n, ch_n, ca_n, s_d, 
                          i_d, f1, f2, f3, gname, gtax_id);

      res.add(t);
    }
    rs.close();
    st.close();

    return res.size() == 0 ? null : res;
  }
  
}