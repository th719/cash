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

// чек: продажи или возврата
public abstract class Check extends FDoc
{

  // дополнительно для чеков продажи / возврата
  protected Vector positions = null;   // список позиций
  protected boolean canceled = false;  // признак отмены чека

  // признак того, что транзакции закрытия чека уже добавлены
//  protected boolean close_tr_added = false; 
//  protected boolean cancel_tr_added = false;

  // счетчики позиций при печати документа
  protected int pr_count = 0; 
//  protected int pr_section = 0, pr_count = 0; 
  // pr_section - номер блока, pr_section - номер строки в блоке
  // номера блоков:
  // 0 - чек не печатался, 1 - позиции, 2 - сторно, 3 - сумма чека, 
  // 4 - продажа, 5 - налоги, 6 - закр или отмена
  
//  private boolean _pr_rest = true; // признак того, что после ошибки чек допечатывается


  // в чеке сумма указывается уже со скидкой (FDoc)
  // поэтому процент и сумма скидки указывается отдельно
  private double psum = 0;                        // сумма по позициям
  private double ch_dsc_pc = 0, ch_dsc_sum = 0;   // если скидка суммой - то процент равен 0
  private String dsc_check_text = null;           // текст для чека 

  public void setPercentCheckDsc(double pc)  // установить процентную скидку на чек
  {
    ch_dsc_pc = pc;
    recalkCheckDsc();
  }

  public void setSumCheckDsc(double s)   // установить суммовую скидку на чек 
  {
    ch_dsc_sum = s;
    recalkCheckDsc();
  }

  public void cancelCheckDsc()   // отменить скидку на чек
  {
    ch_dsc_pc = 0;
    ch_dsc_sum = 0;
    dsc_check_text = null;
    recalkCheckDsc();
  }

  // процент скидки на чек
  public double getCheckDscPc() { return ch_dsc_pc; }

  // сумма скидки в чеке 
  public double getCheckDscSum() { return ch_dsc_sum; }

  // просто сумма позиций в чеке 
  public double getCheckPosSum() { return psum; }
  
  // пересчет скидки ()
  private void recalkCheckDsc()
  { 
    // если процентная скидка то пересчитываем сумму скидки
    if (ch_dsc_pc != 0) ch_dsc_sum = RMath.round( psum * ch_dsc_pc / 100, 2);
    // если же скидка суммой - ничего не пересчитваем, так и остается
    sum = psum - ch_dsc_sum;  // меняем сумму со скидкой
  }

  // установить фиксированную скидку для чека  
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

  // добавить позицию к чеку
  public void addPosition(Position p)
  {
    if (positions == null) positions = new Vector();

    positions.add(p);

    if (p.getSum() != null)
    {
      psum = RMath.round( psum + p.getSum().doubleValue(), 2); // меняем сумму по позициям
      recalkCheckDsc(); // пересчитываем
    }
  }

  // **************************************************************************
  // добавить с фиксацией в кеше транзакций
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

    // в t нет кода транзакции
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

  // сторнировать позицию из списка позиций (не путать с транзакциями)
  public void stornoPosition(Position p)
  {
    if (p.getSum() != null) 
    {
      psum = RMath.round(psum - p.getSum().doubleValue(), 2);
      recalkCheckDsc();
    }
  }

  // **************************************************************************
  // используется при интерактивном создании чека !!! ???
  public void stornoPos(Connection c, Position p, User u)
    throws SQLException
  {
    stornoPosition(p);

    if (transactions == null) transactions = new Vector();

    double p_quan = p.getQuantity().doubleValue(), p_sum = p.getSum().doubleValue();

    // если это сторно продажи - то кол-во и сумма с отрицательным знаком
    if (Position.SALE_POS.equals(p.getType()) )
    {
      p_quan = -p_quan; p_sum = -p_sum;
    }

    Transaction t = null;
    t = Transaction.createStornoPos(getCashNum(), getDocNum(), u.getCode().intValue(), 
             p.getGoodId().intValue(), 0, p.getPrice().doubleValue(), 
             p_quan, p_sum, p.getGname(), p.getGtaxId());
    
    // в t нет кода транзакции
    transactions.add(t);
//    saveCurTr(c, t);
    DbConverter.saveTransToBuf(c, t);
  }

  // установить список позиций в чеке (восстановление чека)
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

  // установить номер документа 
  public void setDocNum(int num)
  {
    super.setDocNum(num);

    // проставляем номер во всех транзакциях
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

  // пересчет суммы чека
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

  // налоги в чеке (суммы по группам налога)
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
        
        // добавляем значение налога
        taxSum.setSum(taxSum.getSum() + _calcNalog(p.getSum().doubleValue(), taxSum.getTaxPc())); 
      }
    }
    return taxes;
  }

  // сумма налога в позиции
  private double _calcNalog(double sum, double pc)
  {
    return RMath.round( sum - sum * 100 / (100 + pc), 2 );
  }


  // отметить отмену чека
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

  // удаляет транзакции закрытия чека (нужно для корректной отчены чека)
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

  // для removeCloseOrCancelData()
  // добавлены и транзакции итогов по скидкам
  private final static int[] CH_ITOG_TR = 
    new int[]{Transaction.PAYMENT, Transaction.CLOSE_CHECK, Transaction.CANCEL_CHECK,
              Transaction.ITOG_DSC_SUM, Transaction.ITOG_DSC_PC, Transaction.CHECK_DSC_DET};

  private boolean inList(int[] lst, int val)
  {
    int size = lst.length, i = 0;
    while(i < size && val != lst[i]) i++;
    return i < size;
  }

  // Зачистка итогов в чеке ---
  // убирает последние транзакции закрытия или отмены чека
  // нужно для отработки ошибок, когда документ завершен,
  // но ошибки не позволили зафиксировать его в фискальной 
  // памяти или в журнале 
              
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
        if (to_remove) transactions.remove(--sz); // удаляем последний
      }
//      close_tr_added = false;
//      cancel_tr_added = false; // в принципе можно обойтись без флажков ... если продумать ...
//    }
  }
  
   
  // **************************************************************************
  // условие печати строк, сделано для возможности отработки ошибок
  // строка,позиция, или операция закрытия печатается, если чек еще 
  // не печатался ни разу или если это недопечатанный остаток чека
  // перед повторной печатью  чек, разумеется, менять нельзя!!!
  private boolean _pprint(int pc)
  {
    return pc >= pr_count;
  }
  
  // ширина строки при печати 
  private final static int WCH_NUM = 36;
  // вопросики по дизайну чека, однако
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

      // печатаем как строки все позиции чека
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


      // отдельно печатаем сторнированные позиции
      if (storno_pos != null)
      {

        if (_pprint(pc))
          fp.printString( sfmt.getCenterString( "Сторно", ' ', WCH_NUM ));
        pc++;
      
        for (i = 0; i < storno_pos.size(); i++)
        {
          p = (Position)storno_pos.get(i);

          if (_pprint(pc))
            fp.printString(sfmt.getPosString(p, WCH_NUM));
          pc++;
        }
      }


      // подчек и печатаем строкой итог чека
      // 2.04.2009 отменено по служебке
/*
      if (_pprint(pc))
        fp.printString(sfmt.getSEString(null, '=', null, WCH_NUM));
      pc++;

      if (_pprint(pc))
        fp.printString(sfmt.getSEString("Всего", '.', sfmt.getMoneyFmt().format(getSum()), WCH_NUM));
      pc++;
*/

      // фиксируем абстрактную операцию продажи или возврата - одну на весь чек
      switch (type_fd)
      {
        case FD_SALE_TYPE   : if (_pprint(pc)) fp.salePostition(1, sum, ""); pc++; break;
        case FD_RETURN_TYPE : if (_pprint(pc)) fp.saleReturnPostition(1, sum, ""); pc++; break;
      }


      // печатаем суммы налогов, по каждой из групп
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

      // печатаем информацию о скидке / надбавке
      if (getCheckDscSum() != 0)
      {
        if (_pprint(pc)) 
            fp.printString(sfmt.getSEString(dsc_check_text, '.', sfmt.getMoneyFmt().format(getCheckDscSum()), WCH_NUM));
      }

      // отмена или закрытие чека
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