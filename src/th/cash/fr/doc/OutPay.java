package th.cash.fr.doc;

import th.cash.dev.FiscalPrinter;
import java.sql.Connection;
import java.sql.SQLException;
import th.cash.model.User;

/**
 * Выплата
 */
public class OutPay extends Pay
{
  public OutPay()
  {
    setDocType(FD_PAY_OUT_TYPE);
  }

  public void close(User u)
  {
    super.close(u);
    Transaction t;
    t = Transaction.createPayOut(getCashNum(), getDocNum(), u.getCode().intValue(), getSum());
    transactions.clear();
    transactions.add(t);
  }

  public void print(FiscalPrinter fp ) throws Exception
  {
    fp.payOutOper(getSum());
  }
  
}