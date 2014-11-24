package th.cash.fr.doc;

import th.cash.dev.FiscalPrinter;
//import th.cash.fr.err.FrErrHandler;

public abstract class Report extends FDoc 
{
  protected int repNum;      // ����� ������
  protected int pr_cnt = 0;  // ������� ��������� ������

  public void print(FiscalPrinter fp) throws Exception
  {
    int pc = 0;
    try
    {
      nalSum = fp.getNalSum(); // ����� ���������� � �����
      sum = fp.getMoneyReg(193) - fp.getMoneyReg(195);
      int tt = 0;
      if (pc <= pr_cnt)
      {
        if (getTypeId() == FD_XREPORT_TYPE) { fp.makeXReport(); tt = Transaction.XREP; }
        if (getTypeId() == FD_ZREPORT_TYPE) { fp.makeClrZReport(); tt = Transaction.ZREP; }
//        FrErrHandler.checkContinuePrint(fp);
      }
      pc++;

      if (getTypeId() == FD_XREPORT_TYPE)
        repNum = fp.getOperReg(158);
      else 
        repNum = fp.getOperReg(159); // ��� ����������� (�� ��� �������)

      Transaction t = new Transaction(-1, null, tt, cashNum, docNum, cashierId, 
        String.valueOf(repNum), 0, nalSum, 0, sum, null, null);
     
      transactions.add( t );
    } finally 
    {
      pr_cnt = pc;
    }
  }
  
}