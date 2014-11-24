package th.cash.fr.doc;

import java.util.Vector;
import java.util.Date;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import th.cash.dev.FiscalPrinter;

import java.text.NumberFormat;
import java.util.Locale;

import th.cash.model.User;

import th.common.util.RMath;
import th.cash.model.FixedDsc;

// ���: ������� ��� ��������
public abstract class Check extends FDoc
{

  // ������������� ��� ����� ������� / ��������
  protected Vector positions = null;   // ������ �������
  protected boolean canceled = false;  // ������� ������ ����

  // ������� ����, ��� ���������� �������� ���� ��� ���������
//  protected boolean close_tr_added = false; 
//  protected boolean cancel_tr_added = false;

  // �������� ������� ��� ������ ���������
  protected int pr_count = 0; 
//  protected int pr_section = 0, pr_count = 0; 
  // pr_section - ����� �����, pr_section - ����� ������ � �����
  // ������ ������:
  // 0 - ��� �� ���������, 1 - �������, 2 - ������, 3 - ����� ����, 
  // 4 - �������, 5 - ������, 6 - ���� ��� ������
  
//  private boolean _pr_rest = true; // ������� ����, ��� ����� ������ ��� ��������������


  // � ���� ����� ����������� ��� �� ������� (FDoc)
  // ������� ������� � ����� ������ ����������� ��������
  private double psum = 0;                        // ����� �� ��������
  private double ch_dsc_pc = 0, ch_dsc_sum = 0;   // ���� ������ ������ - �� ������� ����� 0
  private String dsc_check_text = null;           // ����� ��� ���� 

  public void setPercentCheckDsc(double pc)  // ���������� ���������� ������ �� ���
  {
    ch_dsc_pc = pc;
    recalkCheckDsc();
  }

  public void setSumCheckDsc(double s)   // ���������� �������� ������ �� ��� 
  {
    ch_dsc_sum = s;
    recalkCheckDsc();
  }

  public void cancelCheckDsc()   // �������� ������ �� ���
  {
    ch_dsc_pc = 0;
    ch_dsc_sum = 0;
    dsc_check_text = null;
    recalkCheckDsc();
  }

  // ������� ������ �� ���
  public double getCheckDscPc() { return ch_dsc_pc; }

  // ����� ������ � ���� 
  public double getCheckDscSum() { return ch_dsc_sum; }

  // ������ ����� ������� � ���� 
  public double getCheckPosSum() { return psum; }
  
  // �������� ������ ()
  private void recalkCheckDsc()
  { 
    // ���� ���������� ������ �� ������������� ����� ������
    if (ch_dsc_pc != 0) ch_dsc_sum = RMath.round( psum * ch_dsc_pc / 100, 2);
    // ���� �� ������ ������ - ������ �� ������������, ��� � ��������
    sum = psum - ch_dsc_sum;  // ������ ����� �� �������
  }

  // ���������� ������������� ������ ��� ����  
  public void setFixedCheckDiscount(Connection c, FixedDsc fd, User u) 
  {
    boolean is_sum = fd.getIsSum() != null && fd.getIsSum().booleanValue();
    boolean is_dsc = fd.getIsDsc() != null && fd.getIsDsc().booleanValue();

    if (is_sum)
      setSumCheckDsc( (is_dsc ? 1 : -1 ) * fd.getDscValue().doubleValue());
    else
      setPercentCheckDsc( (is_dsc ? 1 : -1 ) * fd.getDscValue().doubleValue());

    dsc_check_text = fd.getCheckText();

/*
    if (transactions == null) transactions = new Vector();

    Transaction t = null;

    t = Transaction.createCheckDscDet(getCashNum(), null, 1, 0, ch_dsc_pc, ch_dsc_sum);

    transactions.add(t);*/
  }

  // �������� ������� � ����
  public void addPosition(Position p)
  {
    if (positions == null) positions = new Vector();

    positions.add(p);

    if (p.getSum() != null)
    {
      psum = RMath.round( psum + p.getSum().doubleValue(), 2); // ������ ����� �� ��������
      recalkCheckDsc(); // �������������
    }
  }

  // **************************************************************************
  // �������� � ��������� � ���� ����������
  public void addPos(Connection c, Position p, User u)
    throws SQLException
  {
    addPosition(p);

    if (transactions == null) transactions = new Vector();

    Transaction t = null;

    int ttype = type_fd == FD_SALE_TYPE ? Transaction.REG_POS : Transaction.RET_POS;

    double quan = p.getQuantity().doubleValue();
    double sum = p.getSum().doubleValue();

    if (ttype == Transaction.RET_POS) { quan = -quan; sum = -sum; }
    
    t = new Transaction(-1, null, ttype, 
             getCashNum(), getDocNum(), u.getCode().intValue(), 
             String.valueOf(p.getGoodId()), 0, p.getPrice().doubleValue(), 
             quan, sum, 
             p.getGname(), p.getGtaxId());

    // � t ��� ���� ����������
    transactions.add(t);

//    saveCurTr(c, t);
    DbConverter.saveTransToBuf(c, t);
     

    if (p.getBarcode() != null && p.getKoef() != null)
    {
      t = Transaction.createRegValue(getCashNum(), getDocNum(), u.getCode().intValue(), 
        p.getBarcode(), 0, p.getKoef().doubleValue());
      transactions.add(t);
//      saveCurTr(c, t);
      DbConverter.saveTransToBuf(c, t);
    }
  }

  // ������������ ������� �� ������ ������� (�� ������ � ������������)
  public void stornoPosition(Position p)
  {
    if (p.getSum() != null) 
    {
      psum = RMath.round(psum - p.getSum().doubleValue(), 2);
      recalkCheckDsc();
    }
  }

  // **************************************************************************
  // ������������ ��� ������������� �������� ���� !!! ???
  public void stornoPos(Connection c, Position p, User u)
    throws SQLException
  {
    stornoPosition(p);

    if (transactions == null) transactions = new Vector();

    double p_quan = p.getQuantity().doubleValue(), p_sum = p.getSum().doubleValue();

    // ���� ��� ������ ������� - �� ���-�� � ����� � ������������� ������
    if (Position.SALE_POS.equals(p.getType()) )
    {
      p_quan = -p_quan; p_sum = -p_sum;
    }

    Transaction t = null;
    t = Transaction.createStornoPos(getCashNum(), getDocNum(), u.getCode().intValue(), 
             p.getGoodId().intValue(), 0, p.getPrice().doubleValue(), 
             p_quan, p_sum, p.getGname(), p.getGtaxId());
    
    // � t ��� ���� ����������
    transactions.add(t);
//    saveCurTr(c, t);
    DbConverter.saveTransToBuf(c, t);
  }

  // ���������� ������ ������� � ���� (�������������� ����)
  protected void setPositions(Vector pos)
  {
    positions = pos;
    recalcSum();
  }

  public Vector getPositions()
  {
    return positions;
  }

  protected void setTranscactions(Vector tr)
  {
    transactions = tr;
  }

  // ���������� ����� ��������� 
  public void setDocNum(int num)
  {
    super.setDocNum(num);

    // ����������� ����� �� ���� �����������
    if (transactions != null)
    {
      Transaction t;
      for (int i = 0; i < transactions.size(); i++)
      {
        t = (Transaction)transactions.get(i);
        t.setCheckNum(num);
      }
    }
  }

  // �������� ����� ����
  private void recalcSum()
  {
    int i = 0;
    psum = 0;
    Position p;
    while (i < positions.size())
    {
      p = (Position)positions.get(i);
      if (!Position.CANCEL_POS.equals(p.getType()))
        psum += p.getSum().doubleValue(); 
      i++;
    }
    /// !!!!
    psum = RMath.round(psum, 2);
    recalkCheckDsc();
  }

  // ������ � ���� (����� �� ������� ������)
  private Vector calkTaxes()
  {
    Vector taxes = null;
    Position p;
    Integer gtax_id = null;
    Double tpc = null;
    TaxSum taxSum = null;
    boolean finded;
    for (int i = 0; i < positions.size(); i++)
    {
      p = (Position)positions.get(i);
      gtax_id = p.getGtaxId();
      tpc = p.getNalPc();
      if (!Position.CANCEL_POS.equals(p.getType()) &&  gtax_id != null && tpc != null)
      {
        taxSum = null;
        if (taxes == null) taxes = new Vector(); 

        int j = 0;
        TaxSum ts;
        while (j < taxes.size() && taxSum == null)
        {
          ts = (TaxSum)taxes.get(j);
          if (gtax_id.equals(ts.getGtaxId()))
            taxSum = ts;
          else
            j++;
        }

        if (taxSum == null)
          taxes.add( taxSum = new TaxSum(gtax_id, p.getTname(), tpc.doubleValue()));
        
        // ��������� �������� ������
        taxSum.setSum(taxSum.getSum() + _calcNalog(p.getSum().doubleValue(), taxSum.getTaxPc())); 
      }
    }
    return taxes;
  }

  // ����� ������ � �������
  private double _calcNalog(double sum, double pc)
  {
    return RMath.round( sum - sum * 100 / (100 + pc), 2 );
  }


  // �������� ������ ����
  public void cancel(User u)
  {
    canceled = true;
    setCashierId(u.getCode().intValue());
    setCashierName(u.getName());
  }

  public boolean isCanceled()
  {
    return canceled;
  }

  // ������� ���������� �������� ���� (����� ��� ���������� ������ ����)
   /*
  protected void removeCloseData()
  {
    if (close_tr_added)
    {
      int sz = transactions.size();
      if (sz >= 2) 
      {
        transactions.remove(sz - 1); 
        transactions.remove(sz - 2);
      }

      close_tr_added = false;
    }
  }
  */

  // ��� removeCloseOrCancelData()
  // ��������� � ���������� ������ �� �������
  private final static int[] CH_ITOG_TR = 
    new int[]{Transaction.PAYMENT, Transaction.CLOSE_CHECK, Transaction.CANCEL_CHECK,
              Transaction.ITOG_DSC_SUM, Transaction.ITOG_DSC_PC, Transaction.CHECK_DSC_DET};

  private boolean inList(int[] lst, int val)
  {
    int size = lst.length, i = 0;
    while(i < size && val != lst[i]) i++;
    return i < size;
  }

  // �������� ������ � ���� ---
  // ������� ��������� ���������� �������� ��� ������ ����
  // ����� ��� ��������� ������, ����� �������� ��������,
  // �� ������ �� ��������� ������������� ��� � ���������� 
  // ������ ��� � ������� 
              
  protected void removeCloseOrCancelData()
  {
//    if (close_tr_added || cancel_tr_added)
//    {
      boolean to_remove = true;
      int sz = transactions.size();
      Transaction t;
      while (sz > 0 && to_remove)
      {
        t = (Transaction)transactions.get(sz - 1);
        to_remove = inList(CH_ITOG_TR, t.getType());
        if (to_remove) transactions.remove(--sz); // ������� ���������
      }
//      close_tr_added = false;
//      cancel_tr_added = false; // � �������� ����� �������� ��� ������� ... ���� ��������� ...
//    }
  }
  
   
  // **************************************************************************
  // ������� ������ �����, ������� ��� ����������� ��������� ������
  // ������,�������, ��� �������� �������� ����������, ���� ��� ��� 
  // �� ��������� �� ���� ��� ���� ��� �������������� ������� ����
  // ����� ��������� �������  ���, ����������, ������ ������!!!
  private boolean _pprint(int pc)
  {
    return pc >= pr_count;
  }
  
  // ������ ������ ��� ������ 
  private final static int WCH_NUM = 36;
  // ��������� �� ������� ����, ������
  public void print(FiscalPrinter fp) throws Exception
  {
    int pc = 0;
    try
    {
      Vector taxes = calkTaxes();

      int i;
    
      Position p;

      StrFormat sfmt = new StrFormat();
      Vector storno_pos = null;

      // �������� ��� ������ ��� ������� ����
      for (i = 0; i < positions.size();i++)
      {
        p = (Position)positions.get(i);

        if (_pprint(pc))
          fp.printString(sfmt.getPosString(p, WCH_NUM));
        pc++;

        if (Position.CANCEL_POS.equals(p.getType()))
        {
          if (storno_pos == null) storno_pos = new Vector();
          storno_pos.add(p);
        }
      }


      // �������� �������� �������������� �������
      if (storno_pos != null)
      {

        if (_pprint(pc))
          fp.printString( sfmt.getCenterString( "������", ' ', WCH_NUM ));
        pc++;
      
        for (i = 0; i < storno_pos.size(); i++)
        {
          p = (Position)storno_pos.get(i);

          if (_pprint(pc))
            fp.printString(sfmt.getPosString(p, WCH_NUM));
          pc++;
        }
      }


      // ������ � �������� ������� ���� ����
      // 2.04.2009 �������� �� ��������
/*
      if (_pprint(pc))
        fp.printString(sfmt.getSEString(null, '=', null, WCH_NUM));
      pc++;

      if (_pprint(pc))
        fp.printString(sfmt.getSEString("�����", '.', sfmt.getMoneyFmt().format(getSum()), WCH_NUM));
      pc++;
*/

      // ��������� ����������� �������� ������� ��� �������� - ���� �� ���� ���
      switch (type_fd)
      {
        case FD_SALE_TYPE   : if (_pprint(pc)) fp.salePostition(1, sum, ""); pc++; break;
        case FD_RETURN_TYPE : if (_pprint(pc)) fp.saleReturnPostition(1, sum, ""); pc++; break;
      }


      // �������� ����� �������, �� ������ �� �����
      if (taxes != null && taxes.size() > 0)
      {
        TaxSum ts; 
        for (i = 0; i < taxes.size(); i++)
        {
          ts = (TaxSum)taxes.get(i);

          if (_pprint(pc)) 
            fp.printString(sfmt.getSEString(ts.getTname(), '.', sfmt.getMoneyFmt().format(ts.getSum()), WCH_NUM));
          pc++;
        }
      }

      // �������� ���������� � ������ / ��������
      if (getCheckDscSum() != 0)
      {
        if (_pprint(pc)) 
            fp.printString(sfmt.getSEString(dsc_check_text, '.', sfmt.getMoneyFmt().format(getCheckDscSum()), WCH_NUM));
      }

      // ������ ��� �������� ����
      if (isCanceled())
      {
        if (_pprint(pc)) 
          fp.calcelCheck();
        pc++;
      }
      else
      {
        if (_pprint(pc)) fp.closeCheck(nalSum, bnSum, "");
        pc++;
      }
    } finally 
    {
//      pr_section = ps;
      pr_count = pc;
    }
  }

//  public boolean isNoPrinted() { return pr_section == 0; }
//  public boolean isPrintErr() { return pr_section > 0 && !isPrintOk(); } 
//  public boolean isPrintOk() { return pr_section == 6 && pr_count == 1; }
//
//  public int getPrintedSection() { return pr_section; }
//  public int getPrintedCount() { return pr_count; }

  


}