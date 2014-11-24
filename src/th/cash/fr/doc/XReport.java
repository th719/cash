package th.cash.fr.doc;
import th.cash.dev.FiscalPrinter;

public class XReport extends Report 
{
  public XReport()
  {
    setDocType(FD_XREPORT_TYPE);
  }

//  public void print(FiscalPrinter fp) throws Exception
//  {
//    int pc = 0;
//    try
//    {
//      nalSum = fp.getNalSum(); // сумма наличности в кассе
//      sum = fp.getMoneyReg(193) - fp.getMoneyReg(195);
//      if (pc <= pr_cnt)
//        fp.makeXReport();
//      pc++;
//      repNum = fp.getOperReg(159);
//
//      transactions.add( Transaction.createZRep(cashNum, docNum, cashierId, repNum, nalSum, sum));
//    } finally 
//    {
//      pr_cnt = pc;
//    }
//  }
  
}