package th.cash.model;

import java.util.Properties;
import java.util.Enumeration;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

public class Settings 
{
  // фиксированные ключи к таблице t_set
  // загрузка   
  public final static String UI_LOAD_FILE       = "load_file";
  public final static String UI_LOAD_FLAG       = "load_flag";
  public final static String UI_LOAD_ENABLED    = "load_enabled"; 

  // выгрузка
  public final static String UI_UNLOAD_FILE     = "unload_file";
  public final static String UI_UNLOAD_FLAG     = "unload_flag";
  public final static String UI_UNLOAD_ENABLED  = "unload_enabled";

  // некоторые ссылки на таблицу с настройками 
  public final static String P_CASH_NUM         = "CASH_NUM";   // новое свойство  для номера кассы
  public final static String FM_CASH_NUM        = "FR_T[1].1.1"; // пока поддерживаются оба
  public final static String FM_CLEAR_ZREP      = "FR_T[1].1.2";
  public final static String FM_OPEN_MONEYBOX   = "FR_T[1].1.7";

  // использовать весовве датчики для индикации конца ленты
  public final static String USE_WEIGHT_SENSORS = "use_weight_sensors";

  // ограничения по кол-вам и суммам
  public final static String MAX_INT_QUAN       = "max_int_quan";
  public final static String MAX_DOUBLE_QUAN    = "max_double_quan";
  public final static String MAX_MONEY_SUM      = "max_money_sum";
  public final static String MAX_CHECK_SUM      = "max_check_sum";

  // added new settings
  // отметки
  // о авторизации/ выходе
  private final static String LOGIN_MARK        = "login_mark";
  // о запуске / выключении
  private final static String START_MARK        = "start_mark";
  // записывать транзакции в файл (log4j)
  private final static String LOG_TRANS_TO_FILE = "log_trans_to_file";
  // транзакция о открытии денежного ящика с клавиатуры
  private final static String MONEYBOX_MARK     = "moneybox_mark";
  // транзакция диагностики о завершении ЭКЛЗ
  private final static String DIAGN_EKLZ_FULL   = "diagn_eklz_full";

  // разница во времени между ПК и ФР при которой работа блокируется
  private final static String WORK_LOCK_DIF     = "work_lock_dif";
  // разница во времени, при которой ФР синхронизируется с компом
  private final static String TIME_DIF_SEC      = "time_dif_sec";

  // дисплей, сообщение о заблокированной кассе (окно авторизации)
  // строки разделяются "\n"
  private final static String LOCKED_CD_TEXT    = "locked_cd_text";
  // дисплей, сообщение о простое кассы (окно продаж)
  private final static String AWAY_CD_TEXT      = "away_cd_text";

  //
  private final static String CREDIT_PAY_ENABLED = "credit_pay_enabled";

  // номер терминала для б/н оплаты
  public final static String TRM_NUM            = "trm_num";  

  // номер кассы (для ухода от FR_T[1].1.1 и от записи в таблицы ФР)
  private final static String KKM_NUM            = "kkm_num";

  // разрешение скидок
  private final static String DISCOUNTS_ENABLED  = "discounts_enabled";

  // шаблон имени свойства для правила 
  private final static String GOOD_ROLE_PREF = "RULE_";

  // **************************************************************** 
  private Properties allSet;
  private Properties frModes;  // для таблицы1 тип и режимы кассы
  private Properties frCheckText; // для таблицы 

  // префикс таблиц
  protected final static String FR_PREF = "FR_T";

  private String fr_modes_pref;
  private String fr_check_text_pref;
  // имена свойств именуются как FR_T[01].1.2
  // номер таблицы, номер строки, номер столбца

  private  NumberFormat nfmt = NumberFormat.getInstance(Locale.ENGLISH);
  

  private Integer cashNumber;
  private Integer moneyBoxOpen;
  private Integer clearZrep;

  private String sprLoadFile, sprLoadFlag, repUnloadFile, repUnloadFlag;
  private boolean loadEnabled = false, unloadEnabled = false;


  // значения по умолчанию
  private final static boolean DEF_USE_WEIGHT_SENSORS = false;

  // ограничители 
  private final static double DEF_INT_QUAN_LIMIT = 1000;
  private final static double DEF_DOUBLE_QUAN_LIMIT = 100;
  private final static double DEF_MONEY_SUM_LIMIT = 10000000;
  private final static double DEF_CHECK_SUM_LIMIT = 10000000;

  // транзакции
  private final static boolean DEF_LOGIN_MARK    = false;
  private final static boolean DEF_START_MARK    = false;

  private final static boolean DEF_LOG_TRANS_TO_FILE  = false;

  private final static boolean DEF_MONEYBOX_MARK = false;
  private final static boolean DEF_DIAGN_EKLZ_FULL    = false;

  private final static int DEF_WORK_LOCK_DIF = 5400;  // полтора часа
  private final static int DEF_TIME_DIF_SEC = 1;


  private final static boolean DEF_CREDIT_PAY_ENABLED = false;

  private final static boolean DEF_DSC_ENABLED = false;

  // инициализация отдельных параметров настройки
  private boolean use_weight_sensors = DEF_USE_WEIGHT_SENSORS;
  
  private double max_int_quan = DEF_INT_QUAN_LIMIT;      // максимальное целое кол-во
  private double max_double_quan = DEF_DOUBLE_QUAN_LIMIT;   // максимальное дробное кол-во (вес)
  private double max_money_sum = DEF_MONEY_SUM_LIMIT; // максимальная сумма ден. операции 
  private double max_check_sum = DEF_CHECK_SUM_LIMIT; // максимальная сумма чека

  private boolean login_mark = DEF_LOGIN_MARK;
  private boolean start_mark = DEF_START_MARK;
  private boolean log_trans_to_file = DEF_LOG_TRANS_TO_FILE;
  private boolean moneybox_mark = DEF_MONEYBOX_MARK;
  private boolean diagn_eklz_full = DEF_DIAGN_EKLZ_FULL;

  // разница между ФР и ПК, при которой блокируется продажа 
  private int work_lock_dif = DEF_WORK_LOCK_DIF;
  // разница во времени, при которой устанавливается врема на ФР
  private int time_dif_sec = DEF_TIME_DIF_SEC;

  private String locked_cd_text1 = null, locked_cd_text2 = null;
  private String away_cd_text1 = null, away_cd_text2 = null;

  // бн. оплата, константы и настройки
  private boolean creadit_pay_enabled = DEF_CREDIT_PAY_ENABLED;

  // скидки, все виды 
  private boolean discounts_enabled = DEF_DSC_ENABLED;


  private String trm_num = null;

  private int kkm_num = -1;

  private Vector sale_rules = null;
  
  public Settings()
  {
    // префиксы
    fr_modes_pref = getFrTabPref(1);
    fr_check_text_pref = getFrTabPref(4);

    // наборы свойств
    allSet = new Properties();
    frModes = new Properties();
    frCheckText = new Properties();

    nfmt.setGroupingUsed(false);
  }

  // ************************************************************************
  public Properties getAllSet()
  {
    return allSet;
  }

  public Properties getFrModes()
  {
    return frModes;
  }

  public Properties getFrCheckText()
  {
    return frCheckText;
  }


  // префикс таблицы
  private String getFrTabPref(int tab)
  {
    return FR_PREF + "[" + tab + "]";
  }

  // имя совойства, индексы начинаются с 1
  private String getFrPropName(int tab, int row_num, int col_num)
  {
    return getFrTabPref(tab) + "." + row_num + "." + col_num;
  }

  // по имени свойства определям интедксы
  public int[] getTabRowCol(String s)
  {
    int[] res = null;

    try {    

      int tab_num, row_num, col_num;
      int i = 0, len = s.length();
      StringBuffer sb;
      char c = 0;

      // читаем номер таблицы
      while (i < len && !Character.isDigit( c = s.charAt(i) )) i++;
      if (i == len) return res;
      sb = new StringBuffer(String.valueOf( c )); i++;
      while (i < len && Character.isDigit( c = s.charAt(i) )) { sb.append( c ); i++; }
      tab_num = Integer.parseInt(sb.toString());

      // читаем номер строки
      while (i < len && !Character.isDigit( c = s.charAt(i) )) i++;
      if (i == len) return res;
      sb = new StringBuffer(String.valueOf( c )); i++;
      while (i < len && Character.isDigit( c = s.charAt(i) )) { sb.append( c ); i++; }
      row_num = Integer.parseInt(sb.toString());

      // номер колонки
      while (i < len && !Character.isDigit( c = s.charAt(i) )) i++;
      if (i == len) return res;
      sb = new StringBuffer(String.valueOf( c )); i++;
      while (i < len && Character.isDigit( c = s.charAt(i) )) { sb.append( c ); i++; }
      col_num = Integer.parseInt(sb.toString());

      res = new int[] {tab_num, row_num, col_num};
    } catch (Exception ex)
    {
    }
    
    return res;
  }


  public void clear()
  {
    allSet.clear();
    frModes.clear();
    frCheckText.clear();
  }

  public void putProperty(String key, String val)
  {
    allSet.setProperty(key,val);
    
    if (key.startsWith( fr_modes_pref )) frModes.setProperty(key, val); else
      if (key.startsWith( fr_check_text_pref )) frCheckText.setProperty(key, val);
  }

  public void removeProperty(Object key)
  {
    allSet.remove(key);
    frModes.remove(key);
    frCheckText.remove(key);
  }

  private boolean getBoolProperty(String val, boolean def) 
  {
    return val == null ? def : "true".equalsIgnoreCase(val);
  }

  private Integer getIntegerProperty(String val) { return val == null ? null : new Integer(val); }

  private Double getDoubleProperty(String val) { 
    if (val == null)
      return null;
    else
    {
      try 
      {
        return (Double)nfmt.parse(val);
      } catch (Exception ex)
      {
        return null;
      }
    }
  }

  // инициализация свойств (с default значениями)
  protected void initDefProps()
  {
    // новое название свойства
    cashNumber = getIntegerProperty(allSet.getProperty(P_CASH_NUM));
    if (cashNumber == null) // для совместимости старое
      cashNumber = getIntegerProperty(allSet.getProperty(FM_CASH_NUM));
      
    moneyBoxOpen = getIntegerProperty(allSet.getProperty(FM_OPEN_MONEYBOX, "1"));
    clearZrep = getIntegerProperty(frModes.getProperty(FM_CLEAR_ZREP, "1"));

    sprLoadFile = allSet.getProperty(UI_LOAD_FILE);
    sprLoadFlag = allSet.getProperty(UI_LOAD_FLAG);
    loadEnabled = getBoolProperty(allSet.getProperty(UI_LOAD_ENABLED), false);

    repUnloadFile = allSet.getProperty(UI_UNLOAD_FILE);
    repUnloadFlag = allSet.getProperty(UI_UNLOAD_FLAG);
    unloadEnabled = getBoolProperty(allSet.getProperty(UI_UNLOAD_ENABLED), false);

    use_weight_sensors = getBoolProperty(allSet.getProperty(USE_WEIGHT_SENSORS), DEF_USE_WEIGHT_SENSORS);

    Double d;

    d = getDoubleProperty(allSet.getProperty(MAX_INT_QUAN));
    max_int_quan = d == null ? DEF_INT_QUAN_LIMIT : d.doubleValue();

    d = getDoubleProperty(allSet.getProperty(MAX_DOUBLE_QUAN));
    max_double_quan = d == null ? DEF_DOUBLE_QUAN_LIMIT : d.doubleValue();

    d = getDoubleProperty(allSet.getProperty(MAX_MONEY_SUM));
    max_money_sum = d == null ? DEF_MONEY_SUM_LIMIT : d.doubleValue();

    d = getDoubleProperty(allSet.getProperty(MAX_CHECK_SUM));
    max_check_sum = d == null ? DEF_CHECK_SUM_LIMIT : d.doubleValue();


    login_mark = getBoolProperty(allSet.getProperty(LOGIN_MARK), DEF_LOGIN_MARK);
    start_mark = getBoolProperty(allSet.getProperty(START_MARK), DEF_START_MARK);
    log_trans_to_file = getBoolProperty(allSet.getProperty(LOG_TRANS_TO_FILE), DEF_LOG_TRANS_TO_FILE);
    moneybox_mark = getBoolProperty(allSet.getProperty(MONEYBOX_MARK), DEF_MONEYBOX_MARK);
    diagn_eklz_full = getBoolProperty(allSet.getProperty(DIAGN_EKLZ_FULL), DEF_DIAGN_EKLZ_FULL);

    Integer ival;
    ival = getIntegerProperty(allSet.getProperty(WORK_LOCK_DIF));
    work_lock_dif = ival == null ? DEF_WORK_LOCK_DIF : ival.intValue();

    ival = getIntegerProperty(allSet.getProperty(TIME_DIF_SEC));
    time_dif_sec = ival == null ? DEF_TIME_DIF_SEC : ival.intValue();

    String s;
    
    locked_cd_text1 = null;
    locked_cd_text2 = null;
    s = allSet.getProperty(LOCKED_CD_TEXT);
    if (s != null)
    {
      StringTokenizer tok = new StringTokenizer(s, "\n");
      if (tok.hasMoreTokens()) locked_cd_text1 = tok.nextToken();
      if (tok.hasMoreTokens()) locked_cd_text2 = tok.nextToken();
    }

    away_cd_text1 = null;
    away_cd_text2 = null;
    s = allSet.getProperty(AWAY_CD_TEXT);
    if (s != null)
    {
      StringTokenizer tok = new StringTokenizer(s, "\n");
      if (tok.hasMoreTokens()) away_cd_text1 = tok.nextToken();
      if (tok.hasMoreTokens()) away_cd_text2 = tok.nextToken();
    }

    creadit_pay_enabled = getBoolProperty(allSet.getProperty(CREDIT_PAY_ENABLED), DEF_CREDIT_PAY_ENABLED);

    trm_num = allSet.getProperty(TRM_NUM); // при оплате безналом должно быть значение TODO

    discounts_enabled = getBoolProperty(allSet.getProperty(DISCOUNTS_ENABLED), DEF_DSC_ENABLED);


    // правила при регистрации товара 
    Enumeration en = allSet.propertyNames();
    String pn;
    sale_rules = new Vector(1, 10);
    while (en.hasMoreElements())
    {
      pn = (String)en.nextElement();
      if (pn.startsWith(GOOD_ROLE_PREF))
      {
        SecSaleRule rule = null;
        try
        {
          rule = new SecSaleRule( allSet.getProperty(pn));
        } catch (Exception ex)
        {
          rule = null;
        }
        if (rule != null)  sale_rules.add(rule);
      }
    }

  }

  // *************************************************************************
  // GET some properties

  public Integer getCashNumber()  {    return cashNumber;  }

  public boolean isLoadEnabled()  {    return loadEnabled;  }

  public String getRepUnloadFile()  {    return repUnloadFile;  }

  public String getRepUnloadFlag()  {    return repUnloadFlag;  }

  public String getSprLoadFile()  {    return sprLoadFile;  }

  public String getSprLoadFlag()  {    return sprLoadFlag;  }

  public boolean isUnloadEnabled()  {    return unloadEnabled;  }

  public boolean isUseWeightSensors()  {    return use_weight_sensors;  }

  public double getMaxCheckSum()  {    return max_check_sum;  }

  public double getMaxDoubleQuan()  {    return max_double_quan;  }

  public double getMaxIntQuan()  {    return max_int_quan;  }

  public double getMaxMoneySum()  {    return max_money_sum;  }

  public boolean isLoginMark()  {    return login_mark;  }

  public boolean isStartMark()  {    return start_mark;  }

  public boolean isLogTransToFile()  { return log_trans_to_file; }

  public boolean isMoneyboxMark()  { return moneybox_mark; }

  public boolean isDiagnEKLZFull()  { return diagn_eklz_full; }

  public int getWorkLocalDif() { return work_lock_dif; }

  public int getTimeDifSec() { return time_dif_sec; }

  public String getLockedCdText1() { return locked_cd_text1; }

  public String getLockedCdText2() { return locked_cd_text2; }

  public String getAwayCdText1() { return away_cd_text1; }

  public String getAwayCdText2() { return away_cd_text2; }

  public boolean isCreditPayEnabled() { return creadit_pay_enabled; }

  public String getTrmNum() { return trm_num; }

  public boolean isDiscountsEnabled() { return discounts_enabled; }

  public Vector getGoodRules() {  return sale_rules;  }
  
}