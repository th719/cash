package th.cash.card;

import java.util.Properties;
import th.cash.dev.FiscalPrinter;
import java.sql.Connection;

import java.util.Date;

// фейс для закрытия чека по безналу, код разделен на несколько этапов
  // может потребовать доработки и изменений

public interface CardActivator 
{
  public void setParams(Properties cfg);


  // запросить снятие указанной суммы (с интерфейсом, где вводится номер карты)
  // возвращает данные авторизации
  // 1
  public Object requestSum(String card_data, double sum);

  // далее касса отрабатывает закрытие и печать фискального чека

  // печать банковского чека
  public String printBankCheck(FiscalPrinter fp, Object auth_data ) throws Exception;

  // фиксация банковской транзакции для суточного отчета по картам 
  public String saveBankTrans(Object auth_data);

  
  // 
  public String makeReport(Connection c, int kkm_num, int cashier_num, boolean log_tr_to_file);


  public Date getBankShiftDate() throws Exception;

}