package th.cash.dev;

import java.util.Date;
import th.cash.fr.state.StateA;

public interface FiscalPrinter 
{

  public void init() throws Exception;

  public byte[] getReply();

  // установить имя пользователя
  public void setUserName(String name) throws Exception;

//  public ShortStateFr shortStateRequest() throws Exception;
  
  // гудок
  public void beep() throws Exception;

  // синхронизация времени
  public void setDataTime(Date data) throws Exception;
  public void setDataTime(Date data, StateA state) throws Exception;

  // печать строки (чековая/контрольная лента  переключается в настройках)
  public void printString(String s) throws Exception;


  public void printBoldString(String s) throws Exception;

  public void printDocHeader(String s) throws Exception;

  public void cutCheck() throws Exception;

  public void transportTape(int num_rows, boolean check_tape) throws Exception;

  // прерывание тестового прогона
  public void interruptTest() throws Exception;

  // чек продажи
  public void openCheck() throws Exception;

  public void salePostition(double quan, double price, String text) throws Exception;

  public void saleReturnPostition(double quan, double price, String text) throws Exception;

  public void cancelPosition(double quan, double price, String text) throws Exception;

  public void calcelCheck() throws Exception;  

  public double closeCheck(double nal, double bn, String text) throws Exception;


  // внесение / выплата
  public void payInOper(double sum) throws Exception;

  public void payOutOper(double sum) throws Exception;


  // чтение регистров
  public int getLastDocNum() throws Exception;

  // сумма наличности в кассе
  public double getNalSum() throws Exception;
  
  
  public int getOperReg(int reg_num) throws Exception;
  
  public double getMoneyReg(int reg_num) throws Exception;

  // X-отчет
  public void makeXReport() throws Exception;

  // закрытие смены
  public void makeClrZReport() throws Exception;

  // открыть денежный ящик
  public void openMoneyBox() throws Exception;

  public void continuePrint() throws Exception;
  
  public void close() throws Exception;

  public StateA stateRequest() throws Exception;

}