package th.cash.fr.doc;

import th.cash.dev.FiscalPrinter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Z-�����
 */
public class ZReport extends Report
{
  
  public ZReport()
  {
    setDocType(FD_ZREPORT_TYPE);
  }

  // ���������� ����������� � ������ print()
  // ����� ������������� ����������� ������ ��� ������ 
//  public void print(FiscalPrinter fp) throws Exception
//  {
//    int pc = 0;
//    try
//    {
//      nalSum = fp.getNalSum(); // ����� ���������� � �����
//      sum = fp.getMoneyReg(193) - fp.getMoneyReg(195);
//      if (pc <= pr_cnt)
//        fp.makeClrZReport();
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