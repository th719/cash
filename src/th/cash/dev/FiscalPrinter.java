package th.cash.dev;

import java.util.Date;
import th.cash.fr.state.StateA;

public interface FiscalPrinter 
{

  public void init() throws Exception;

  public byte[] getReply();

  // ���������� ��� ������������
  public void setUserName(String name) throws Exception;

//  public ShortStateFr shortStateRequest() throws Exception;
  
  // �����
  public void beep() throws Exception;

  // ������������� �������
  public void setDataTime(Date data) throws Exception;
  public void setDataTime(Date data, StateA state) throws Exception;

  // ������ ������ (�������/����������� �����  ������������� � ����������)
  public void printString(String s) throws Exception;


  public void printBoldString(String s) throws Exception;

  public void printDocHeader(String s) throws Exception;

  public void cutCheck() throws Exception;

  public void transportTape(int num_rows, boolean check_tape) throws Exception;

  // ���������� ��������� �������
  public void interruptTest() throws Exception;

  // ��� �������
  public void openCheck() throws Exception;

  public void salePostition(double quan, double price, String text) throws Exception;

  public void saleReturnPostition(double quan, double price, String text) throws Exception;

  public void cancelPosition(double quan, double price, String text) throws Exception;

  public void calcelCheck() throws Exception;  

  public double closeCheck(double nal, double bn, String text) throws Exception;


  // �������� / �������
  public void payInOper(double sum) throws Exception;

  public void payOutOper(double sum) throws Exception;


  // ������ ���������
  public int getLastDocNum() throws Exception;

  // ����� ���������� � �����
  public double getNalSum() throws Exception;
  
  
  public int getOperReg(int reg_num) throws Exception;
  
  public double getMoneyReg(int reg_num) throws Exception;

  // X-�����
  public void makeXReport() throws Exception;

  // �������� �����
  public void makeClrZReport() throws Exception;

  // ������� �������� ����
  public void openMoneyBox() throws Exception;

  public void continuePrint() throws Exception;
  
  public void close() throws Exception;

  public StateA stateRequest() throws Exception;

}