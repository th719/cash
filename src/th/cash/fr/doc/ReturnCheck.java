package th.cash.fr.doc;


import java.util.Vector;

import th.cash.dev.FiscalPrinter;
import java.sql.Connection;
import java.sql.SQLException;

import th.cash.model.User;

/**
 * Чек возврата (переопределено закрытие и отмена)
 */
public class ReturnCheck extends Check 
{
  public ReturnCheck()
  {
    setDocType(Check.FD_RETURN_TYPE);
  }
// example:
//515;28.08.07;16:44:05;13;3;44;2;83805;1;7;-1;-7;
//516;28.08.07;16:44:09;13;3;44;2;83805;1;7;-1;-7;
//517;28.08.07;16:44:12;13;3;44;2;17745;1;6.3;-1;-6.3;
//518;28.08.07;16:44:16;13;3;44;2;17745;1;6.3;-1;-6.3;
//519;28.08.07;16:44:20;13;3;44;2;83805;1;7;-1;-7;
//520;28.08.07;16:44:31;75;3;44;2;1;1;-33.6;0;-3.06;
//521;28.08.07;16:44:31;40;3;44;2;0;0;0;1;-33.6;
//522;28.08.07;16:44:31;55;3;44;2;0;0;0;0;-33.6;

  public void close(User u)
  {
    removeCloseOrCancelData();
  
    super.close(u);



    // если есть скидка на чек - добавляем транзакцию детализации
    // определиться со знаками getCheckDscSum() - сумму скидки берем с минусом
    // и процедуру восстановления
    if (getCheckDscSum() != 0)
    {
      // для совместимости - еще и транзакция итога по скидкам
      int tt;
      tt = getCheckDscPc() == 0 ? Transaction.ITOG_DSC_SUM : Transaction.ITOG_DSC_PC;
      transactions.add(
        new Transaction(-1, null, tt, getCashNum(), getDocNum(), u.getCode().intValue(), "0", 1, 0, getCheckDscPc() == 0 ? -getCheckDscSum() : getCheckDscPc(), -getCheckDscSum(), null, null));

      // и детализация      
      transactions.add(
        new Transaction(-1, null, Transaction.CHECK_DSC_DET, getCashNum(), getDocNum(), u.getCode().intValue(), "0", 1, 0, getCheckDscPc(), -getCheckDscSum(), null, null));
    }

    // оплата наличными (для возврата ТОЛЬКО НАЛИЧНЫЕ)
    if (getNalSum() > 0)
      transactions.add(
        Transaction.createPayment(getCashNum(), getDocNum(), u.getCode().intValue(), "0", -getChange(), 1, -getNalSum()));


     // собственно транзакция закрытия
    transactions.add(
      Transaction.createCloseCheck(getCashNum(), getDocNum(), u.getCode().intValue(), -getSum()));


    // ставим признак того, что транзакции закрытия чека добавлены
//    close_tr_added = true;

  }


  public void cancel(User u)
  {
    removeCloseOrCancelData();
    
    super.cancel(u);
/*
    int sz = transactions.size();
    
    Transaction t = Transaction.createCancelCheck(getCashNum(), getDocNum(), u.getCode().intValue(), -getSum());
    if (cancel_tr_added) transactions.set(sz - 1, t); else transactions.add(t);
*/
    transactions.add(
      Transaction.createCancelCheck(getCashNum(), getDocNum(), u.getCode().intValue(), -getSum()));

    // признак того, что добавлена транзакция отмены чека
//    cancel_tr_added = true;  убрано, определяется проверкой списка транзакций
  }

}