package th.cash.fr.doc;

import java.util.Vector;

import java.sql.Connection;
import java.sql.SQLException;

import th.cash.model.User;

/**
 * Чек продажи
 */
public class SaleCheck extends Check
{

  public SaleCheck()
  {
    setDocType(FD_SALE_TYPE);
  }

  private String auth_code = "0";
  public void setAuthCode(String acode) { auth_code = acode; }

//123;28.08.07;15:31:43;11;3;19;4;83805;1;7;1;7;
//124;28.08.07;15:31:48;11;3;19;4;83805;1;7;1;7;
//125;28.08.07;15:31:48;11;3;19;4;83805;1;7;1;7;
//126;28.08.07;15:31:48;11;3;19;4;83805;1;7;1;7;
//127;28.08.07;15:32:34;12;3;19;2;83805;1;7;-1;-7;
//128;28.08.07;15:32:59;11;3;19;4;17745;1;6.3;1;6.3;
//129;28.08.07;15:33:01;11;3;19;4;17745;1;6.3;1;6.3;
//130;28.08.07;15:33:08;11;3;19;4;32308;1;68;0.386;26.25;
//131;28.08.07;15:33:12;11;3;19;4;1500;1;143.1;0.39;55.81;
//132;28.08.07;15:33:18;11;3;19;4;385;1;75.5;0.372;28.09;
//133;28.08.07;15:33:25;11;3;19;4;6791;1;5.5;1;5.5;
//134;28.08.07;15:33:27;11;3;19;4;6791;1;5.5;1;5.5;
//135;28.08.07;15:33:29;11;3;19;4;6791;1;5.5;1;5.5;
//136;28.08.07;15:33:55;75;3;19;4;1;1;50.1;0;4.56;
//137;28.08.07;15:33:55;75;3;19;4;2;1;110.15;0;16.79;
//138;28.08.07;15:33:55;40;3;19;4;0;0;0;1;160.25;
//139;28.08.07;15:33:55;55;3;19;4;0;0;0;0;160.25;

  public void close(User u)
  {
    close(u, auth_code);
  }

  public void close(User u, String acode)
  {
    removeCloseOrCancelData();
  
    super.close(u);

    /*
    int sz = transactions.size();
    Transaction t;
    */
    // Есть особенность, когда закрывается чек, который не был закрыт с первой попытки
    // транзакции оплаты и закрытия уже есть , поэтому нужно их убрать ...
    /*
    if (close_tr_added)
    {
      boolean to_remove = true;
      while (sz > 0 && to_remove)
      {
        t = (Transaction)transactions.get(sz - 1);
        to_remove = t.getType() == Transaction.PAYMENT || t.getType() == Transaction.CLOSE_CHECK || t.getType() == Transaction.CANCEL_CHECK;
        if (to_remove) transactions.remove(--sz); // удаляем последний
      }
    }
    */

    // теперь главное - итоги чека
    // если есть скидка на чек - добавляем транзакцию детализации
    if (getCheckDscSum() != 0)
    {
      // для совместимости - еще и транзакция итога по скидкам
      int tt;
      tt = getCheckDscPc() == 0 ? Transaction.ITOG_DSC_SUM : Transaction.ITOG_DSC_PC;
      transactions.add(
        new Transaction(-1, null, tt, getCashNum(), getDocNum(), u.getCode().intValue(), "0", 1, 0, getCheckDscPc() == 0 ? getCheckDscSum() : getCheckDscPc(), getCheckDscSum(), null, null));

      // и детализация      
      transactions.add(
        new Transaction(-1, null, Transaction.CHECK_DSC_DET, getCashNum(), getDocNum(), u.getCode().intValue(), "0", 1, 0, getCheckDscPc(), getCheckDscSum(), null, null));
    }

    // оплата наличными 
    if (getNalSum() > 0)
      transactions.add(
        Transaction.createPayment(getCashNum(), getDocNum(), u.getCode().intValue(), "0", getChange(), 1, getNalSum()));

    // оплата кредитом
    if (getBnSum() > 0)
      transactions.add(
        Transaction.createPayment(getCashNum(), getDocNum(), u.getCode().intValue(), acode, getChange(), 2, getBnSum()));

     // собственно транзакция закрытия
    transactions.add(
      Transaction.createCloseCheck(getCashNum(), getDocNum(), u.getCode().intValue(), getSum()));

    // ставим признак того, что транзакции закрытия чека добавлены
//    close_tr_added = true;
  }

  public void cancel(User u)
  {
//removeCloseData(); -->
    removeCloseOrCancelData();
    
    super.cancel(u);
    
//    int sz = transactions.size();
//    Transaction t;
//    t = Transaction.createCancelCheck(getCashNum(), getDocNum(), u.getCode().intValue(), getSum());
//    if (cancel_tr_added) transactions.set(sz - 1, t); else transactions.add(t);  -->
    transactions.add(
      Transaction.createCancelCheck(getCashNum(), getDocNum(), u.getCode().intValue(), getSum()));

    // признак того, что добавлена транзакция отмены чека
//    cancel_tr_added = true;
  }



}