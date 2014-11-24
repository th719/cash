package th.cash.fr.state;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

//import th.cash.log.LogUtil;
import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.FrException;

import org.apache.log4j.Logger;

// абстрактный класс, работа с режимами, подрежимами, флагами
// общие элементы...
public abstract class StateA implements StateConst  // added interface StateConst
{

  protected byte   operNum;            // Порядковый номер оператора (1 байт) 1?30
  protected int    flagsFr;            // Флаги ФР (2 байта) 
  protected byte   modeFr;             // Режим ФР (1 байт) 
  protected byte   subModeFr;          // Подрежим ФР (1 байт)

  protected int    modeNum, modeStatus; // расшифровка - номер и статус режима

  protected final static String LOG_PREF = "FR"; 
  protected Logger log = Logger.getLogger(LOG_PREF + '.' + this.getClass().getName());


  public abstract void initFromFr(FrDrv fr) throws FrException;

  public abstract void decodeParams(byte[] data) throws FrException;

  public abstract void printAll();

  private StringBuffer log_buf = null;

  protected String getDTStr(byte[] dt, char div)
  {
    return FrUtil.get2SigStr(dt[0]) + div + FrUtil.get2SigStr(dt[1]) + div + FrUtil.get2SigStr(dt[2]);
  }
  
  //*********************************************************************
  // выделение битов флага
  private boolean getIntFlag(int fb, int bit_num)
  {
    int mask = 1 << bit_num;
    return (fb & mask) > 0;
  }

  //*********************************************************************
  // flagsFr
  // 0 - Рулон операционного журнала (0 ? нет, 1 ? есть) 
  public boolean isFFrRollLog()
  {
    return getIntFlag(flagsFr, 0);
  }
  // 1 - Рулон чековой ленты (0 ? нет, 1 ? есть) 
  public boolean isFFrRollCheck()
  {
    return getIntFlag(flagsFr, 1);
  }
  // 2 - Верхний датчик подкладного документа (0 ? нет, 1 ? да) 
  public boolean isFFrTopSensorPd()
  {
    return getIntFlag(flagsFr, 2);
  }
  // 3 - Нижний датчик подкладного документа (0 ? нет, 1 ? да)
  public boolean isFFrBotSensorPd()
  {
    return getIntFlag(flagsFr, 3);
  }
  // 4 - Положение десятичной точки (0 ? 0 знаков, 1 ? 2 знака)
  public boolean isFFrDecimalDotPos()
  {
    return getIntFlag(flagsFr, 4);
  }
  // 5 - ЭКЛЗ (0 ? нет, 1 ? есть) 
  public boolean isFFrEKLZ()
  {
    return getIntFlag(flagsFr, 5);
  }
  // 6 - Оптический датчик операционного журнала (0 ? бумаги нет, 1 ? бумага есть) 
  public boolean isFFrOptSensorLog()
  {
    return getIntFlag(flagsFr, 6);
  }
  // 7 -  Оптический датчик чековой ленты (0 ? бумаги нет, 1 ? бумага есть) 
  public boolean isFFrOptSensorCheck()
  {
    return getIntFlag(flagsFr, 7);
  }
  // 8 - Рычаг термоголовки контрольной ленты (0 ? поднят, 1 ? опущен) 
  public boolean isFFrLeverPrintLog()
  {
    return getIntFlag(flagsFr, 8);
  }
  // 9 -  Рычаг термоголовки чековой ленты (0 ? поднят, 1 ? опущен) 
  public boolean isFFrLeverPrintCheck()
  {
    return getIntFlag(flagsFr, 9);
  }
  // 10 - Крышка корпуса ФР (0 ? опущена, 1 ? поднята) 
  public boolean isFFrCoverOpened()
  {
    return getIntFlag(flagsFr, 10);
  }
  // 11 - Денежный ящик (0 ? закрыт, 1 ? окрыт) 
  public boolean isFFrMoneyBoxOpened()
  {
    return getIntFlag(flagsFr, 11);
  }
  // 12 - Отказ правого датчика принтера (0 ? нет, 1 ? да) 
  public boolean isFFrRightPrintSensor()
  {
    return getIntFlag(flagsFr, 12);
  }
  // 13 - Отказ левого датчика принтера (0 ? нет, 1 ? да) 
  public boolean isFFrLeftPrintSensor()
  {
    return getIntFlag(flagsFr, 13);
  }
  // 14 -  ЭКЛЗ почти заполнена (0 ? нет, 1 ? да) 
  public boolean isFFrEKLZFull()
  {
    return getIntFlag(flagsFr, 14);
  }
  // 15 - Увеличенная точность количества (0 ? нормальная точность, 1 ? увеличенная точность) [для ККМ без ЭКЛЗ] 
  public boolean isFFrHighPrecisionQuantity()
  {
    return getIntFlag(flagsFr, 15);
  }

  //************************************************************************
  // основные режимы:
  // "Принтер в рабочем режиме."
  public boolean isMFrPrinterOk()
  {
    return modeNum == 0;
  }

  // "Открытая смена, 24 часа не кончились."
  public boolean isMFrOpenedShiftOk()
  {
    return modeNum == 2;
  }

  // "Открытая смена, 24 часа кончились."
  public boolean isMFrOpenedShiftExceed()
  {
    return modeNum == 3;
  }

  // "Закрытая смена."
  public boolean isMFrClosedShift()
  {
    return modeNum == 4;
  }


  //************************************************************************
  protected void setModeFr(byte mode)
  {
    modeFr = mode;
    modeNum = modeFr & 0x0000000F;
    modeStatus = modeFr & 0x000000F0 >>> 4;
  }

  public byte getModeFr()
  {
    return modeFr;
  }

  public int getModeNum()
  {
    return modeNum;
  }

  public int getModeStatus()
  {
    return modeStatus;
  }

  public String getModeNumStr()
  {
    switch (modeNum)
    {
      case 0  : return "Принтер в рабочем режиме.";
      case 1  : return "Выдача данных."; 
      case 2  : return "Открытая смена, 24 часа не кончились."; 
      case 3  : return "Открытая смена, 24 часа кончились.";
      case 4  : return "Закрытая смена.";
      case 5  : return "Блокировка по неправильному паролю налогового инспектора.";
      case 6  : return "Ожидание подтверждения ввода даты.";
      case 7  : return "Разрешение изменения положения десятичной точки.";
      case 8  : return "Открытый документ";
      case 9  : return "Режим разрешения технологического обнуления. В этот режим ККМ переходит по "+
                       "включению питания, если некорректна информация в энергонезависимом ОЗУ ККМ.";
      case 10 : return "Тестовый прогон.";
      case 11 : return "Печать полного фис. отчета.";
      case 12 : return "Печать отчёта ЭКЛЗ.";
      case 13 : return "Работа с фискальным подкладным документом";
      case 14 : return "Печать подкладного документа.";
      case 15 : return "Фискальный подкладной документ сформирован.";
      default : return "Неизвестный номер режима";
    }
  }

  public String getModeStatusStr()
  {
    switch (modeNum)
    {
      case 8  :
        switch (modeStatus)
        {
          case 0 : return "Продажа.";
          case 1 : return "Покупка."; 
          case 2 : return "Возврат продажи.";
          case 3 : return "Возврат покупки.";         
          default : return "";
        }
      case 13 :
        switch (modeStatus)
        {
          case 0 : return "Продажа (открыт).";
          case 1 : return "Покупка (открыт)."; 
          case 2 : return "Возврат продажи (открыт).";
          case 3 : return "Возврат покупки (открыт).";         
          default : return "";
        }
      case 14 :
        switch (modeStatus)
        {
          case 0 : return "Ожидание загрузки.";
          case 1 : return "Загрузка и позиционирование."; 
          case 2 : return "Позиционирование.";
          case 3 : return "Печать.";         
          case 4 : return "Печать закончена.";         
          case 5 : return "Выброс документа.";         
          case 6 : return "Ожидание извлечения.";         
          default : return "";
        }
      default : return "";
    }
  }

  //************************************************************************
  public byte getSubModeFr()
  {
    return subModeFr;
  }

  public String getSubModeFrStr()
  {
    switch (subModeFr)
    {
      case 0 : return "Бумага есть - ФР не в фазе печати операции - может принимать от хоста команды, "+
        "связанные с печатью на том документе, датчик которого сообщает о наличии бумаги.";
      case 1 : return "Пассивное отсутствие бумаги - ФР не в фазе печати операции - не принимает от "+
        "хоста команды, связанные с печатью на том документе, датчик которого сообщает об "+
        "отсутствии бумаги.";
      case 2 : return "Активное отсутствие бумаги - ФР в фазе печати операции - принимает только "+
        "команды, не связанные с печатью. Переход из этого подрежима только в подрежим 3.";
      case 3 : return "После активного отсутствия бумаги - ФР ждет команду продолжения печати. Кроме "+
        "этого принимает команды, не связанные с печатью.";
      case 4 : return "Фаза печати операции полных фискальных отчетов - ФР не принимает от хоста "+
        "команды, связанные с печатью, кроме команды прерывания печати.";
      case 5 : return "Фаза печати операции - ФР не принимает от хоста команды, связанные с печатью.";
      default : return "Неизвестный подрежим";
    }
  }


  protected final static char TAB = '\t';

  // debug printing
  protected void printFlags()
  {
    debug("{************************ Флаги **************************");
    debug("Рулон операционного журнала" + TAB + isFFrRollLog());
    debug("Рулон чековой ленты" + TAB + isFFrRollCheck());
    debug("Верхний датчик подкладного документа" + TAB + isFFrTopSensorPd()); 
    debug("Нижний датчик подкладного документа" + TAB + isFFrBotSensorPd());
    debug("Положение десятичной точки" + TAB + isFFrDecimalDotPos());
    debug("ЭКЛЗ (0 ? нет, 1 ? есть)" + TAB + isFFrEKLZ());
    debug("Оптический датчик операционного журнала (0 ? бумаги нет, 1 ? бумага есть)" + TAB + isFFrOptSensorLog());
    debug("Оптический датчик чековой ленты (0 ? бумаги нет, 1 ? бумага есть)" + TAB + isFFrOptSensorCheck());
    debug("Рычаг термоголовки контрольной ленты (0 ? поднят, 1 ? опущен)" + TAB + isFFrLeverPrintLog());
    debug("Рычаг термоголовки чековой ленты (0 ? поднят, 1 ? опущен)" + TAB + isFFrLeverPrintCheck());
    debug("Крышка корпуса ФР (0 ? опущена, 1 ? поднята)" + TAB + isFFrCoverOpened());
    debug("Денежный ящик (0 ? закрыт, 1 ? окрыт)" + TAB + isFFrMoneyBoxOpened());
    debug("Отказ правого датчика принтера (0 ? нет, 1 ? да)" + TAB + isFFrRightPrintSensor());
    debug("Отказ левого датчика принтера (0 ? нет, 1 ? да)" + TAB + isFFrLeftPrintSensor());
    debug("ЭКЛЗ почти заполнена (0 ? нет, 1 ? да)" + TAB + isFFrEKLZFull());
    debug("Увеличенная точность количества (0 ? нормальная точность, 1 ? увеличенная точность)" + TAB + isFFrHighPrecisionQuantity());
    debug("************************ Флаги **************************}");
  }

  protected void printModes()
  {
    debug("{************************ Режимы **************************");

    debug("Режим:" + TAB + getModeNum() + " (" + getModeNumStr() + ")");

    debug("Подрежим:" + TAB + getSubModeFr() + " (" + getSubModeFrStr() + ")");

    debug("Статус:" + TAB + getModeStatus() + " (" + getModeStatusStr() + ")" );
    
    debug("************************ Режимы **************************}");
  }

  
  public void printAll(StringBuffer sb)
  {
    log_buf = sb;

    printAll();

    log_buf = null;
  }

  // вывод для StringBuffer или Logger
  protected void debug(String s)
  {
    if (log.isDebugEnabled()) log.debug(s);
    if (log_buf != null) { log_buf.append(s); log_buf.append('\n'); }
  }
  
}