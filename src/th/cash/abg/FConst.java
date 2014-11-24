package th.cash.abg;



public interface FConst 
{

  public final static String    FILE_ENC = "US-ASCII";

  public final static char      FILL_CHAR = ' ';   // заполнитель полей

  public final static char      EXCH_CODE = 'p';

  public final static char      SIGN_OK = '1';
  public final static int       FIXED_AUTH_LEN = 200;   // фиксированная длина ответа

  public final static char      TR_SEPARATOR = ',';    // разделитель файла транзакций

  // values of StatusFlag
  public final static char      SF_OK = 'A';       
  public final static char      SF_DENY = 'N';

  // values of EntryMode
  public final static char      EM_KEY = 'K';
  public final static char      EM_READER = 'R';


  // типы транзакций (поле TransType, выборочно)
  public final static char      TT_SALE = '0';          // продажа товаров и услуг (как бы дебет нашего счета)
  public final static char      TT_CREDIT = '4';        // возврат денег на счет клиента
  public final static char      TT_OFFLINE_SALE = '6';  // введение транзакции после голосовой авторизации
  public final static char      TT_VOID = '9';          // отмена транзакции


  // f11
  public final static String    TID_VOID = "00";
  public final static String    TID_SALE_T2 = "01";
  public final static String    TID_CREDIT_TXN = "06";
  public final static String    TID_SALE_NO_T2 = "07";
  public final static String    TID_SALE_VOICE_AUTH = "09";

  // f14
  public final static String    CT_AM = "AM";
  public final static String    CT_DC = "DC";
  public final static String    CT_VI = "VI";
  public final static String    CT_EU = "EU";
  public final static String    CT_GS = "GS";


  // f20
  public final static char      EC_AUTH_CODE   = '0';
  public final static char      EC_CALL_ISSUER = '1';
  public final static char      EC_PICKUP_CARD = '2';
  public final static char      EC_CARD_ERROR  = '3';
  public final static char      EC_UNSUCCESS_CALL = '4';
  public final static char      EC_INVALID_TRANS = '5';
  public final static char      EC_NOT_AUTHORIZED = '6';
  public final static char      EC_BAD_FIELD = '7';
  public final static char      EC_COMM_ERROR = '8';
  public final static char      EC_SYSTEM_ERROR = '9';

  public final static String[]  RU_F20_COMENTS = 
  {
    "Получен авторизационный код",
    "Для проведения операций по данной карте необходимо позвонить в авторизационный центр",
    "Изъять карту",
    "Сервер обнаружил ошибку формата в поле <Номер карты>",
    "Авторизационный сервер не смог установить соединение с хостом",
    "Присланый запрос не может быть обработан",
    "Явный отказ в авторизации по карте",
    "Неверное значение в поле N",
    "При соединениии с хостом были обнаружены проблемы в канале связи",
    "При попытке выполнить запрос от кассы сервер обнаружил ошибку операционной системы"
  };
  

  public final static String    EOF_MARK = "/n";

  // Константа для прямого индексного доступа к параметрам строки атибутов
  public final static int[][]   FLD_IND = new int [][]
  {
    {0,  1},   // StatusFlag
    {1,  4},   // MsgN
    {5,  1},   // EntryMode
    {6,  19},  // CardNo
    {25, 4},   // ExpDate
    {29, 40},  // Track2Data
    {69, 12},  // Amount
    {81, 6},   // TransDate
    {87, 6},   // TransTime
    {93, 1},   // TransType
    {94, 2},   // TransId
    {96, 9},   // AuthCode
    {105, 16}, // Msg
    {121, 2},  // CardType
    {123, 3},  // TrmN
    {126, 1},  // Type
    {127, 8},  // TrmId
    {135, 4},  // TrmT
    {139, 15}, // MerchN
    {154, 1},  // ErrorCode
    {155, 1},  // SignOk
    {156, 1},  // IFCmd
    {157, 1},  // OldTrType
    {158, 20}, // Reserved1
    {178, 17}, // Reserved2
    {195, 2},  // RespCode 
    {197, 1}   // CntrlChar
    //{198, 2}  // none ....
  };


  public final static String[][] F26_DESCR = 
  {
    {"00","Approved or completed successfully","Approve"},
    {"01","refer to Card Issuer","Call bank"},
    {"03","Invalid merchant","Decline"},
    {"04","Pick up card","Capture card"},
    {"05","Do not honor","Decline"},
    {"12","Invalid transaction","Decline"},
    {"13","Invalid amount","Decline"},
    {"14","Invalid card number - no such number","Decline"},
    {"30","Format error","Decline"},
    {"33","Expired card","Capture card"},
    {"36","Restricted card","Capture card"},
    {"37","Card acceptor call Acquirer Security","Capture card"},
    {"39","No Account Found","Decline"},
    {"41","Lost card","Capture card"},
    {"43","Stolen card","Capture card"},
    {"51","Not sufficient funds","Decline"},
    {"54","Expired card","Decline"},
    {"57","Transaction not permitted to cardholder","Decline"},
    {"58","Transaction not permitted to terminal","Decline"},
    {"61","Exceeds withdrawal amount limit reached","Decline"},
    {"62","Restricted card","Decline"},
    {"65","Exceeds withdrawal frequency limit","Decline"},
    {"68","Response receive too later","Decline"},
    {"96","System malfunction","Decline"}
  };
}