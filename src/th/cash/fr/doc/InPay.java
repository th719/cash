package th.cash.fr.doc;

import th.cash.dev.FiscalPrinter;
import java.sql.Connection;
import java.sql.SQLException;

import th.cash.model.User;

/**
 * Внесение
 */

public class InPay extends Pay
{

  public InPay()
  {
    setDocType(FD_PAY_IN_TYPE);
  }

  public void close(User u)
  {
    super.close(u);
    Transaction t;
    t = Transaction.createPayIn(getCashNum(), getDocNum(), u.getCode().intValue(), getSum());
    transactions.clear();
    transactions.add(t);
  }
  

  public void print(FiscalPrinter fp) throws Exception
  {
    fp.payInOper(getSum());      
  }


  
}