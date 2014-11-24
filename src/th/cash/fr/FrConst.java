package th.cash.fr;

public interface FrConst 
{
  // -----------   common const
  public final static byte STX = 2;

  public final static byte ENQ = 5;

  public final static byte ACK = 6;

  public final static byte NAK = 21;

  // ------------------------------
  // состояния ФР
  public final static int S_NO_LINK = 1;
  public final static int S_FR_WAIT_CMD  = 3;
  public final static int S_FR_SEND_DATA = 4;
  public final static int S_FR_UNKNOWN = 7;
  
  // -------------------------------

  public final static int[] AVAIL_SPD = new int[]{2400, 4800, 9600, 19200, 38400, 57600, 115200};
  public final static int[] INIT_SEQ = new int[]{6, 1, 0, 2, 3, 4, 5};
//  public final static int[] AVAIL_SPD = new int[]{115200, 4800, 2400, 9600, 19200, 38400, 57600};

  

  public final static int DEF_SPD_CODE = 1;
  public final static int MIN_SPD_CODE = 0;
  public final static int MAX_SPD_CODE = 6;


  // --------------   элементы протокола   -------------------  
  // параметр для настройки последовательного порта (enableReceiveTimeout())
  public final static int DEF_RECEIVE_TIMEOUT = 100; // ms

  // итервал между попытками чтения из потока порта
  public final static long DEF_WAIT_TIME = 10;     // ms

  // количество попыток чтения управляющего символа
  public final static int DEF_SPEC_BYTE_READ_LIMIT = 100; 

  // количество попыток чтения байтов сообщения
  public final static int DEF_DATA_BYTE_READ_LIMIT = 2000;

  // кодировка строковых данных
  public final static String FR_STR_ENC = "Cp1251";
 
  

}