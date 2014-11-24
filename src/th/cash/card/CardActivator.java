package th.cash.card;

import java.util.Properties;
import th.cash.dev.FiscalPrinter;
import java.sql.Connection;

import java.util.Date;

// ���� ��� �������� ���� �� �������, ��� �������� �� ��������� ������
  // ����� ����������� ��������� � ���������

public interface CardActivator 
{
  public void setParams(Properties cfg);


  // ��������� ������ ��������� ����� (� �����������, ��� �������� ����� �����)
  // ���������� ������ �����������
  // 1
  public Object requestSum(String card_data, double sum);

  // ����� ����� ������������ �������� � ������ ����������� ����

  // ������ ����������� ����
  public String printBankCheck(FiscalPrinter fp, Object auth_data ) throws Exception;

  // �������� ���������� ���������� ��� ��������� ������ �� ������ 
  public String saveBankTrans(Object auth_data);

  
  // 
  public String makeReport(Connection c, int kkm_num, int cashier_num, boolean log_tr_to_file);


  public Date getBankShiftDate() throws Exception;

}