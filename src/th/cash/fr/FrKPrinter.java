package th.cash.fr;

import java.util.Date;
import th.cash.dev.FiscalPrinter;
import th.cash.fr.state.*;

import th.common.util.RMath;

/**
 * 
 * Реализация интерфейса FiscalPrinter
 * 
 */
public class FrKPrinter implements FiscalPrinter 
{

  private FrDrv fr;

  private boolean text_on_ctype = false; // печать текста на контрольной ленте
  private int def_section = 0;
  private final static byte ZERO = (byte)0;
  
  public FrKPrinter(String port_name, int timeout) throws Exception
  {
    fr = new FrDrv(port_name, timeout);

    fr.setDebug(false);

//    fr.init(FrConst.AVAIL_SPD[FrConst.MAX_SPD_CODE]);

    
  }

  public void init() throws Exception
  {
    fr.init();
    fr.open();
    
  }
  

  public FrDrv getFrDrv()
  {
    return fr;
  }

  private long doubleToLong(double d)
  {
    return Math.round(d);
  }

  // сокращенная команда для записи имени пользователя
  public void setUserName(String name) throws FrException
  {
    fr.setTabData((byte)2, 30, 2,  name, 21);
  }

  public void setTabCell(int tab, int row, int col, Object data, int size) throws FrException
  {
    if (data == null) data = "";
    fr.setTabData((byte)tab, row, col, data, size);
  }

  public StateA stateRequest() throws FrException
  {
    FullStateFr state = new FullStateFr();
    state.initFromFr(getFrDrv());
    return state;
  }

  public void beep() throws FrException
  {
    fr.beep();
  }

  public void setDataTime(Date data) throws FrException
  {
    byte[] dt = new byte[3];
    byte[] tm = new byte[3];
    FrUtil.dateToBytes(data, dt, tm);

    fr.setTime(tm);
    fr.setDate(dt);
    fr.confirmDate(dt);
  }

  // с подтверждением предыдущей даты , если ошибка ...
  public void setDataTime(Date data, StateA state) throws FrException
  {
    byte[] dt = new byte[3];
    byte[] tm = new byte[3];
    FrUtil.dateToBytes(data, dt, tm);

    fr.setTime(tm);
    try
    {
      fr.setDate(dt);
      fr.confirmDate(dt);
    } catch (FrException ex)
    {
      if (ex.getErrorCode() == FrErrors.ERR_CURDATE_LESS_DATE_FM)
      {
        // если некоректна дата, то подтверждаем предыдущую
        FrUtil.dateToBytes(((FullStateFr)state).getCurDate(), dt, tm);
        fr.confirmDate(dt);
      }
      throw ex;
      
    }
  }


  public byte[] getReply()
  {
    return fr.getReplyParams();
  }

  public void printString(String s) throws FrException
  {
    fr.printString(s, text_on_ctype, true);
  }


  public void printBoldString(String s) throws FrException
  {
    fr.printBoldString(s, false, true);
  }

  public void printDocHeader(String s) throws FrException
  {
    fr.printDocHeader(s, 0);
  }

  public void cutCheck() throws FrException
  {
    fr.cutCheck(false);
  }


  public void transportTape(int num_rows, boolean check_tape) throws FrException
  {
    fr.transportTape(num_rows, check_tape);
  }

  public void interruptTest() throws FrException
  {
    fr.interruptTest();
  }
  public void openCheck() throws FrException
  {
    fr.openCheck((byte)0);
  }

  // количество в штуках или килограммах, цена в рублях
  public void salePostition(double quan, double price, String text) throws FrException
  {
    fr.salePosition(doubleToLong(quan * 1000), doubleToLong(price * 100), def_section, ZERO, ZERO, ZERO, ZERO, text);
  }

  public void saleReturnPostition(double quan, double price, String text) throws FrException
  {
    fr.saleReturnPosition(doubleToLong(quan * 1000), doubleToLong(price * 100), def_section, ZERO, ZERO, ZERO, ZERO, text);
  }

  public void cancelPosition(double quan, double price, String text) throws FrException
  {
    fr.cancelPosition(doubleToLong(quan * 1000), doubleToLong(price * 100), def_section, ZERO, ZERO, ZERO, ZERO, text);
  }

  public void calcelCheck() throws FrException
  {
    fr.calcelCheck();
  }

  // оплата наличными 
  public double closeCheck(double nal, String text) throws FrException
  {
    return closeCheck(nal, 0, text);
  }

  public double closeCheck(double nal, double bn, String text) throws FrException
  {
    fr.closeCheck(doubleToLong(nal * 100), doubleToLong(bn * 100), 0, 0, 0, ZERO, ZERO, ZERO, ZERO, text);
    byte[] rpar = fr.getReplyParams();
    long val = FrUtil.longFromBytes(rpar, 1, 5);
    return RMath.round( (double)val / (double)100 , 2);
  }

  public void payInOper(double sum) throws FrException
  {
    fr.payInOper(doubleToLong(sum * 100));
  }

  public void payOutOper(double sum) throws FrException
  {
    fr.payOutOper(doubleToLong(sum *  100));
  }

   

  public int getLastDocNum() throws FrException
  {
    return getOperReg(152);
  }

  public double getNalSum() throws FrException
  {
    return getMoneyReg(241);
  }

  // пока не определены
  public int getOperReg(int reg_num) throws FrException
  {
    fr.getOperReg((byte)(reg_num & 0x000000FF));
    byte[] rpar = fr.getReplyParams();
    int val = FrUtil.intFromBytes(rpar, 1, 2);
    return val;
  }

  public double getMoneyReg(int reg_num) throws FrException
  {
    fr.getMoneyReg((byte)(reg_num & 0x000000FF));
    byte[] rpar = fr.getReplyParams();
    long val = FrUtil.longFromBytes(rpar, 1, 6);
    return RMath.round( (double)val / (double)100 , 2);
  }

  public void makeXReport() throws FrException
  {
    fr.makeXReport();
  }

  public void makeClrZReport() throws FrException
  {
    fr.makeClrZReport();
  }

  public void openMoneyBox() throws FrException
  {
    fr.openMoneyBox((byte)0);
  }

  public void continuePrint() throws FrException
  {
    fr.continuePrint();
  }

  public void close() throws Exception
  {
    fr.close();
  }

}