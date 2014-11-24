package th.cash.fr;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.comm.UnsupportedCommOperationException;
import javax.comm.CommPortIdentifier;
import javax.comm.CommPort;
import javax.comm.SerialPort;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import th.cash.fr.Hex;
import th.cash.fr.FrUtil;
import th.cash.fr.FrException;


// протокол v.1.4

public class FrDrv implements FrConst
{
  // common data buffer
  private final static int С_BUF_SIZE = 256;

  // COMM port streams
  private InputStream  port_is = null;
  private OutputStream port_os = null;

  // port
  private SerialPort serial_port = null;


  // data buffers
  private byte[] c_buf = new byte[С_BUF_SIZE]; // буффер для данных
  private byte[] b_buf = new byte[1];        // буффер для байтов запроса/ответа

  // текущее состояние ФР
  private int fr_state = S_NO_LINK;

  // данные полученные от устройства
  private byte rcmd = 0;
  private byte err_code = 0;
  private byte[] rpar = null;

  // данные для отправки устройству
  private byte scmd = 0;
  private byte[] spar = null;

  private int dev_spd = AVAIL_SPD[DEF_SPD_CODE];
  private int receive_timeout = DEF_RECEIVE_TIMEOUT;

  // ----------------------------------------------------

  // время ожидания между проверками байта во входном потоке
  private long wait_sleep_time = DEF_WAIT_TIME;// 10; // мс
  // число проверок чтения управляющего байта 
  private int  spec_byte_limit = DEF_SPEC_BYTE_READ_LIMIT;
  // число проверок чтения байтов данных из потока порта
  private int  data_byte_limit = DEF_DATA_BYTE_READ_LIMIT;
  // ----------------------------------------------------

  private long pwd_oper = 30;  // пароль оператора

  private boolean dbg = true; // режим отладки

  private String pref_dbg = "*** FrDrv: ";

  // appache log4j Logger

  private Logger log = Logger.getLogger("FR" + '.' + this.getClass().getName());

  public FrDrv(String port_name, int timeout) throws Exception
  {

    CommPortIdentifier cpid = CommPortIdentifier.getPortIdentifier(port_name);

    CommPort cp = cpid.open("FrDrv", timeout);

    if (cp instanceof SerialPort) serial_port = (SerialPort)cp;
      else throw new Exception("Запрошенное устройство не является последовательным портом");

  }

  // *************************************************************************
  //     Доступ к данным
  public void setDebug(boolean debug)
  {
    dbg = debug;
    log.setLevel(debug ? Level.DEBUG : Level.INFO);
  }


  public byte getErrorCode()
  {
    return err_code;
  }

  public boolean isCmdOk()
  {
    return err_code == 0;
  }

  public String getErrorDescr()
  {
    return FrErrors.decodeError(err_code);
  }

  public void setOperPwd(long pwd)
  {
    pwd_oper = pwd;
  }

  public byte[] getReplyParams()
  {
    return rpar;
  }
   

  /**
   * Инициализировать порт на заданной скорости обмена
   * @param spd
   * @throws IOException
   * @throws UnsupportedCommOperationException
   */
  public void init(int spd) throws IOException, UnsupportedCommOperationException
  {

    serial_port.setSerialPortParams(
      spd, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

    serial_port.enableReceiveTimeout(receive_timeout);
    
    // IOException
    port_is = serial_port.getInputStream();
    port_os = serial_port.getOutputStream();

  }

  /**
   * Найти скорость работы порта, и установить оптимальную ...
   * при отсутствии ошибки результат - короткий запрос состояния
   * @throws IOException
   * @throws UnsupportedCommOperationException
   */

  
  public void init() throws FrException
  {
    log.info( pref_dbg + "search device speed ..." );
    boolean err = true;
    Exception last_ex = null;
    int i = 0;
    int spd_id = 0;
    int num = INIT_SEQ.length;

    while (i < num && err)
    {
      try {
        spd_id = INIT_SEQ[i];
        dev_spd = AVAIL_SPD[spd_id];
        
        if (log.isDebugEnabled()) 
          log.debug( pref_dbg + "... trying " + dev_spd + "..." );

        init( dev_spd = AVAIL_SPD[spd_id] );
        //shortStateRequest();
        stateRequest();

        if (log.isDebugEnabled()) 
          log.debug( pref_dbg + "Ok" );
        
        err = false;
      } catch (Exception ex) // определенный тип ошибки
      {
        last_ex = ex;
        i++;
        err = true;

        if (log.isDebugEnabled()) 
          log.debug( pref_dbg + "Error" );
        
      }
    }


    if (err)
    {
      log.error( pref_dbg + "No available speed !!!" );

      FrException res_ex = new FrException("Фискальный регистратор не обнаружен", FrErrors.ERR_DEV_NOT_FINDED, 1);
      res_ex.setNestedException(last_ex);
      throw res_ex;  // устройство не работает ни на одной скорости
    }
    else
    {
      log.info( pref_dbg + "Ok. Device speed = " + dev_spd );

      // spd_id - номер режима в котором запрос прошел успешно
      // пытаемся выставить максимальную скорость
      int optimal_spd_id = MAX_SPD_CODE;
      if (spd_id != optimal_spd_id)
      {
        boolean optimal_set_ok = false;
        try
        {
          // пробуем максимальную скорость
          log.info( pref_dbg + "Trying to set maximum speed = " + AVAIL_SPD[optimal_spd_id]);
          
          setLinkParams((byte)optimal_spd_id, (byte)100);
          optimal_set_ok = true;
          init( AVAIL_SPD[optimal_spd_id] );
          //shortStateRequest();
          stateRequest();

          // все успешно - прописываем параметры
          dev_spd = AVAIL_SPD[optimal_spd_id];
          spd_id = optimal_spd_id;
          err = false;

          log.info( pref_dbg + "Success!");
        } catch (Exception ex)
        {
          last_ex = ex;
          err = true;
          log.warn( pref_dbg + "Error on maximum speed !");
        }

        // если на максимуме не пошло
        if (err)
        {
          try
          {
            log.info( pref_dbg + "Return to available " + dev_spd);
            init( dev_spd );
            //shortStateRequest();
            stateRequest();

            log.info( pref_dbg + "Ok. Work on " + dev_spd);
          } catch (Exception ex)
          {
             
            log.error( pref_dbg + "Error!!! Device unavailable.", ex);
            FrException new_ex = new FrException("Ошибка подключения фискального регистратора", FrErrors.ERR_DEV_NOT_FINDED);
            new_ex.setNestedException(ex);
            throw new_ex;
          }
        }
      }
    }
    
  }


  /**
   * Заполнить буффер данных сообщения
   * @param buf    - байтовый буффер
   * @param cmd    - код команды
   * @param params - параметры команды
   */
  private void makeMsg(byte[] buf, byte cmd, byte[] params)
  {
    byte cs;                       // контрольная сумма
    byte len = (byte)(1 + params.length);
    buf[0] = STX;                  // 0
    buf[1] = len;                  // длина тела сообщения (N)
    cs = buf[1];
    buf[2] = cmd;
    cs = (byte)(cs ^ buf[2]);
    for (byte i = 0; i < params.length; i++) 
    {
      buf[3 + i] = params[i];
      cs = (byte)(cs ^ buf[3 + i]);
    }
    buf[3 + params.length] = cs;
  }

  //********* разбор тела сообщения *************
  // TODO
  private void parseMsg(byte[] buf)
  {
    byte len = buf[1];
    rcmd = buf[2];
    err_code = len > 1 ? buf[3] : 0;
    rpar = len > 2 ? new byte[len - 2] : null;
    if (rpar != null)
    {
      for (int i = 0; i < rpar.length; i++)
        rpar[i] = buf[ i + 4];
    }
  }


  // *************************************************************************************

  /**
   * Ожидани и чтение специального байта подтверждения(ACK/NAK), данные в b_buf[0]
   * @param max_cnt - число попыток чтения
   * @return        - количество прочитанных байт (-1 - ошибка timeout)
   * @throws IOException
   */
  private int readSpecByte(int max_cnt) throws IOException
  {
    int res = -1;
    int cnt = 0; // read attemts number
    do {
      if (port_is.available() > 0)
        res = port_is.read(b_buf, 0, 1);
      else {
        try {Thread.currentThread().sleep(wait_sleep_time);} catch (InterruptedException ex) {}
          cnt++;
      }
    } while (res <= 0 && cnt < max_cnt);
//    if (dbg) printSpecByte(b_buf[0], true);
    if (log.isDebugEnabled()) printSpecByte(b_buf[0], true);
    return res;
  }

  /**
   * Ожидпние / чтения байтов данных сообщения (no debug)
   * @param buf  - массив буффера данных
   * @param offs - смещение в массиве, куда будут записаны прочитанные данные
   * @param ml   - максимальное количество байт из потока
   * @return     - количество прочитанных байт (-1 - ошибка timeout)
   * @throws IOException
   */
  private int readDataByte(byte[] buf, int offs, int ml) throws IOException
  {
    int res = -1;
    int cnt = 0; // read attemts number
    do
    {
      if (port_is.available() > 0)
        res = port_is.read(buf, offs, ml);
      else {
        try {Thread.currentThread().sleep(wait_sleep_time);} catch (InterruptedException ex) {}
        cnt++;
      }
    } while (res <= 0 && cnt < data_byte_limit);  /*6000 100  итого 20с*/ 

    return res;
  }

  /**
   * Запись контрольного байта протокола, используется b_buf
   * @param b - байт данных
   * @throws IOException
   */
  private void writeSpecByte(byte b) throws IOException
  {
    b_buf[0]= b;
    port_os.write(b_buf, 0, 1);
//    if (dbg) printSpecByte(b, false);
    if (log.isDebugEnabled()) printSpecByte(b, false);
  }

  /**
   * Запись данных сообщения из буффера
   * @param buf  - буффер данных
   * @param offs - смещение в буффере
   * @param ml   - количество байт для записи
   * @throws IOException
   */
  private void writeDataByte(byte[] buf, int offs, int ml) throws IOException
  {
    port_os.write(buf, offs, ml);
  }

  // *******************************************************************
  /**
   * Проверка текущего состояния ФР, просылка ENQ и анализ ответа
   * @return - код текущего состояния ФР (по протоколу)
   * @throws IOException
   */
  private int checkCurState() throws IOException
  {
    writeSpecByte(ENQ);
    int res = readSpecByte( spec_byte_limit ); // defined by const
    if (res < 0) fr_state = S_NO_LINK; else setFrState(b_buf[0]);
    return fr_state;
  }

  /**
   * Установить состояние по байту ответа на ENQ
   * @param answer - байт ответа
   */
  private void setFrState(byte answer)
  {
    switch (answer)
    {
      case ACK : fr_state = S_FR_SEND_DATA; break;
      case NAK : fr_state = S_FR_WAIT_CMD; break;
      default: fr_state = S_FR_UNKNOWN; 
    }
  }
  /**
   * Значение текущего состояния
   * @return - код значения
   */
  public int getFrState()
  {
    return fr_state;
  }

  /**
   * Получить строковое представление состояния по коду (для отладки)
   * @return - строка, соот. имени константы
   */
  public String getFrStateStr()
  {
    switch (fr_state)
    {
      case NO_LINK_READ : return "NO_LINK_READ";
      case S_FR_SEND_DATA : return "S_FR_SEND_DATA";
      case S_FR_WAIT_CMD : return "S_FR_WAIT_CMD";
      default : return "S_FR_UNKNOWN";
    }
  }

  // *******************************************************************
  // ответ устройства (корректный по протоколу), содержащий код ошибки, отличный от 0
  private final static int ERROR_REPLY = -4;


  private final static int MAX_SES_CNT = 3;
  private final static int MAX_READ_CNT = 3;
  private final static int MAX_WRITE_CNT = 3;
  /**
   * Выполнить сеанс обмена данными с ФР
   * @return - результат (по протоколу)
   * @throws IOException
   */
  private int _execSession() throws IOException
  {
//    if (dbg)
//      System.out.println("* _execSession() start");
    if (log.isDebugEnabled()) log.debug("* _execSession() start");

    // проверяем тек состояние фискальника
    checkCurState();
    if (getFrState() == S_NO_LINK || getFrState() == S_FR_UNKNOWN)
      return WRITE_ERR; //или Exception

//    if (dbg)
//      System.out.println("--- fr_state=" + getFrStateStr());


    int cycle_cnt = 0;
    int session_res = 0;
    boolean cur_cmd_sended = false;

    do {
      if (getFrState() == S_FR_SEND_DATA)
      {
//        if (dbg) System.out.println("** receive data start");
        if (log.isDebugEnabled()) log.debug("** receive data start");

        // принимаем данные
        int read_cnt = 0;
        int read_res;
        // ACK
        do {
          read_res = readMsg(c_buf);
          if (/*dbg*/ log.isDebugEnabled())
          {
            if (read_res == NO_LINK_READ)
              printTimeoutErr();
              // TODO
            else 
              print_buf();
          }
        } while (read_res == FrErrors.ERR_READ_DATA && read_cnt < MAX_READ_CNT);

        if (read_res == READ_OK)
        {
          // разбираем буффер c_buf
          parseMsg(c_buf);

          // debug print
//          if (dbg)
//          {
//            if (read_res == NO_LINK_READ)
//              printTimeoutErr();
//              // TODO
//            else printMsg(rcmd, rpar);
//          }

          // проверка состояния
          checkCurState();
          // анализ логического состояния
          if (cur_cmd_sended)
          {
            if (scmd != rcmd) read_res = FrErrors.ERR_READ_CMDC; else
             if (err_code != 0) read_res = ERROR_REPLY;
          }
        }

        if (read_res == FrErrors.ERR_READ_DATA) read_res = NO_LINK_READ; // ???

        session_res = read_res;

//        if (dbg)
//        {
//          System.out.println("** fr_state=" + getFrStateStr());
//          System.out.println("** receive data end =" + session_res);
//        }
        if (log.isDebugEnabled())
        {
          log.debug("** fr_state=" + getFrStateStr());
          log.debug("** receive data end =" + session_res);
        }

      } else
      {
        if (getFrState() == S_FR_WAIT_CMD)
        {
//          if (dbg)
//            System.out.println("** send data start");
          if (log.isDebugEnabled())
            log.debug("** send data start");
          // пишем данные
          int write_cnt = 0;
          int write_res;
          do {
//            if (dbg) printSendData(scmd,spar);
            if (log.isDebugEnabled()) printSendData(scmd,spar);

            write_res = writeMsg(c_buf);
          } while (write_res == WRITE_ERR && write_cnt < MAX_WRITE_CNT);

          if (write_res == WRITE_OK) // был ответ ACK
          {
            // переходим в состояние чтения данных (ответа)
            fr_state = S_FR_SEND_DATA;
            cur_cmd_sended = true; // команда передана и нужно ждать ответное сообщение
          }

          if (write_res == WRITE_ERR) write_res = NO_LINK_READ; // ???



          session_res = write_res;

//          if (dbg)
//          {
//            System.out.println("** fr_state=" + getFrStateStr());
//            System.out.println("** send data end =" + session_res);
//          }
          if (log.isDebugEnabled()) 
          {
            log.debug("** fr_state=" + getFrStateStr());
            log.debug("** send data end =" + session_res);
          }
        }
      }
    }
    while (session_res != NO_LINK_READ && getFrState() == S_FR_SEND_DATA && cycle_cnt < MAX_SES_CNT);

//    if (dbg)
//      System.out.println("* _execSession() end ="+session_res);
    if (log.isDebugEnabled())
      log.debug("* _execSession() end ="+session_res);
 
    return session_res;
  }


  private final static int READ_OK = 0;
  private final static int NO_LINK_READ = FrErrors.ERR_TIMEOUT_READ;
//  private final static int READ_ERR = FrErrors.ERR_READ_DATA;
  /**
   * Прочитать сообщение от ФР, с обратным подтверждением
   * @param buf   - буффер для полученного сообщения
   * @return - результат
   *   READ_OK  - чтение успешно, данные корректны
   *   READ_ERR - ошибка чтения, данные некорректы
   *   NO_LINK_READ - ошибка, превышен таймаут ответа
   * @throws IOException
   */
  private int readMsg(byte[] buf) throws IOException
  {
    byte cs, len;
    int offs = 0;
    int read_res;

    read_res = readDataByte(buf, offs, 1);
    if (read_res < 0) return NO_LINK_READ;

    offs += read_res;

    if (buf[0] == STX)
    {
      // читаем длину
      read_res = readDataByte(buf, offs, 1);
      if (read_res < 0) return NO_LINK_READ; //READ_ERR; // ?????????
      offs += read_res;
      len = buf[1];
      cs =  buf[1];

      // читаем само сообщение
      while (offs < len + 2)
      {
        read_res = readDataByte(buf, offs, 1);
        if (read_res < 0) return NO_LINK_READ;//READ_ERR; // ????????
        offs += read_res;
        cs = (byte) (cs ^ buf[offs - 1]);
      }

      // читаем контрольную сумму
      read_res = readDataByte(buf, offs, 1);
      if (read_res < 0) return NO_LINK_READ;//READ_ERR;  // ???????????
      offs += read_res;

      // если Control Sum совпадает
      if (buf[len + 2] == cs)
      {
        writeSpecByte(ACK);
        return READ_OK;
      }
      else
      {
        writeSpecByte(NAK);
        return FrErrors.ERR_READ_DATA;
      }

    } else return NO_LINK_READ;
  }

  private final static int WRITE_OK = 0;
  private final static int WRITE_ERR = -2;
  /**
   * Передача команды для ФР (no debug)
   * @param buf - буффер команды
   * @return
   *   WRITE_OK  - данные переданы и подтверждены ACK
   *   WRITE_ERR - данные не подтверждены (ответ NAK)
   *   NO_LINK_READ - ошибка, истек таймаут подтверждения
   * @throws IOException
   */
  private int writeMsg(byte[] buf) throws IOException
  {
    byte len = buf[1];
    int bcnt = 0;
    int read_res;
    int write_res = WRITE_OK;
    boolean has_answer = false;
    do
    {
      writeDataByte(buf, bcnt++, 1);
      if (port_is.available() > 0)
      {
        read_res = readSpecByte( spec_byte_limit );  // defined by const
        has_answer = true; // был ответ фискальника
        if (read_res < 0) write_res = NO_LINK_READ;
        if (b_buf[0] == NAK) write_res = WRITE_ERR;
        if (b_buf[0] == ACK) write_res = WRITE_OK;
      }
    } while (write_res == WRITE_OK && bcnt < len + 3 );

    if (write_res == WRITE_OK)
    { // если данные были записаны без ошибки
      // то разбираемся с ответом
      if (!has_answer) // если не бло ответа фискальника
      {
        // ждем подтверждения ACK
        read_res = readSpecByte( spec_byte_limit );
        if (read_res < 0) write_res = NO_LINK_READ;
        if (b_buf[0] == NAK) write_res = WRITE_ERR;
        if (b_buf[0] == ACK) write_res = WRITE_OK;
      }

      // все ли сообщение передано
      if (write_res == WRITE_OK && bcnt != len + 3) write_res = WRITE_ERR;
    }
    return write_res;
  }

  // **********************************************************************************
  /**
   * Открыть сеанс (пока ничего не делает)
   * @throws IOException
   */
  public void open() throws IOException
  {

  }
  /**
   * закрыть сеанс (порт)
   * @throws IOException
   */
  public void close() throws IOException
  {
    serial_port.close();
  }

  //************************************************************************************
  // DEBUG output functions
  /**
   * Печать сообщения
   * @param cmd  - код команды
   * @param args - параметры
   */

  private void printTimeoutErr()
  {
    log.debug("***<<< Timeout error!");
  }
  /**
   *
   * @param cmd  - код команды
   * @param args - параметры
   */
  private void printSendData(byte cmd, byte[] args)
  {
    log.debug("***>>> " + "cmd = " + Hex.byteToHexString(cmd) +
      "   arg = " + (args == null ? "null" : Hex.byteArrayToHexString(args)) );
  }
  /**
   * вывод прочитанного сообщения ФР
   * @param cmd  - код команды
   * @param args - аргументы
   */
  private void printReadData(byte cmd, byte[] args)
  {
    log.debug("***<<< " + "cmd = " + Hex.byteToHexString(cmd) +
      "   arg = " + (args == null ? "null" : Hex.byteArrayToHexString(args)) );
  }

  private void print_buf()
  {
    byte len = c_buf[1];
    log.debug("***<<< " + Hex.byteArrayToHexString(c_buf, 0, len + 3) );
  }

  // печать прием/передачи служебного байта
  /**
   * Вывод данных - спец. байт протокола
   * @param b        - байт
   * @param was_read - прочита (true), записан - false
   */
  private void printSpecByte(byte b, boolean was_read)
  {
    log.debug("***" + (was_read ? "<<<" : ">>>") + " " + getSpecByteStr(b));
  }

  /**
   * Строковое представление служебного байта протокола
   * @param b - байт
   * @return  - символьное представление
   */
  private String getSpecByteStr(byte b)
  {
      switch (b)
      {
          case ENQ : return "ENQ";
          case STX : return "STX";
          case ACK : return "ACK";
          case NAK : return "NAK";
          default : return Hex.byteToHexString(b);
      }
  }

  //************************************************************************************


  /**
   * Добавить байт данных в буффер
   * @param buf  - буффер
   * @param val  - байт
   * @param offs - смещение в буффере
   */
  private void byteToBuf(byte[] buf, byte val, int offs)
  {
//    buf[offs] = val;
    FrUtil.byteToBytes(buf, val, offs);
  }

  /**
   * Добавить байты целого числа в буффер
   * @param buf  - буффер
   * @param val  - число (int)
   * @param nb   - число байт (начиная с младшего)
   * @param offs - смещение в буффере
   */
  private void intToBuf(byte[] buf, int val, int nb, int offs)
  {
    FrUtil.intToBytes(buf, val, nb, offs);
  }

  /**
   * Добавить байты целого числа в буффер
   * @param buf  - буффер
   * @param val  - число (long)
   * @param nb   - число байт (начиная с младшего)
   * @param offs - смещение в буффере
   */
  private void longToBuf(byte[] buf, long val, int nb, int offs)
  {
    FrUtil.longToBytes(buf, val, nb, offs);
  }

  /**
   *
   * @param buf     - буффер
   * @param val     - строка
   * @param max_len - ограничение длины (будет скопировано не более max_len байт)
   * @param offs    - смещение в буффере
   * @throws FrException with incapsulate UnsupportedEncodingException
   */
  private void strToBuf(byte[] buf, String val, int max_len, int offs) throws FrException
  {
      FrUtil.strToBytes(buf, val, max_len, offs);
  }

  /**
   * Выполнить команду для ФР (со сборкой буффера данных)
   * @param cmd  - команда
   * @param args - аргументы
   * @return     - результат операции
   * @throws IOException
   */
  private int execCmd(byte cmd, byte[] args) throws FrException
  {
    scmd = cmd;
    spar = args;

    // сброс результатов предыдущей команды
    rcmd = 0;
    rpar = null;
    err_code = 0;


    makeMsg(c_buf, scmd, spar);

    int res = 0;
    try
    {
      res = _execSession();
    } catch (IOException ex)
    {
      FrException fex = new FrException("Ошибка в/в", FrErrors.ERR_IO_STREAM, 1);
      fex.setNestedException(ex);
      throw fex;
    }

    switch (res) {  
      case NO_LINK_READ : throw new FrException("Истек таймаут ожидания", FrErrors.ERR_TIMEOUT_READ, 1);
      case FrErrors.ERR_READ_DATA : throw new FrException("Ошибка чтения данных", FrErrors.ERR_READ_DATA, 1);
      case FrErrors.ERR_READ_CMDC : throw new FrException("Ошибка кода команды в ответе", FrErrors.ERR_READ_CMDC, 1);

      case ERROR_REPLY : throw FrErrors.createFrException(err_code);
    }

    return res;
  }

  /**
   * Выполнить команду для ФР (со сборкой буффера данных), отдельно пароль оператора
   * @param cmd  - команда
   * @param pwd  - пароль оператора
   * @param args - аргументы
   * @return     - результат операции
   * @throws IOException
   */
  private int execCmd(byte cmd, long pwd, byte[] args) throws FrException
  {
    int alen = args == null ? 0 : args.length;

    byte[] args2 = new byte[4 + alen];

    longToBuf(args2, pwd, 4, 0);

    if (args != null)
      for (int i = 0; i < alen; i++)
        args2[i + 4] = args[i];

    return execCmd(cmd, args2);
  }






  /*
   * **************************************************************************
   *                             Базовые команды
   * **************************************************************************
   * Здесь пока нет разбора параметров
   * Также нужны дополнительный запросы для получения контрольных данных
   * для кассовой программы
   */

  // 10h - Короткий запрос состояния ФР
  //  Команда:  10H. Длина сообщения: 5 байт. 
  //  ? Пароль оператора (4 байта) 
  //  Ответ:    10H. Длина сообщения: 16 байт. 
  public int shortStateRequest() throws FrException
  {
    return execCmd((byte)0x10, pwd_oper, null);
  }

  // 11h - Запрос состояния ФР
  //  Команда:  11H. Длина сообщения: 5 байт. 
  //  ? Пароль оператора (4 байта) 
  //  Ответ:    11H. Длина сообщения: 48 байт. 
  public int stateRequest() throws FrException
  {
    return execCmd((byte)0x11, pwd_oper, null);
  }

  //Печать жирной строки
  //   Команда:     12H. Длина сообщения: 26 байт.
  //        ? Пароль оператора (4 байта)
  //        ? Флаги (1 байт) Бит 0 ? контрольная лента, Бит 1 ? чековая лента.
  //        ? Печатаемые символы (20 байт)
  //   Ответ:       12H. Длина сообщения: 3 байта.
  //        ? Код ошибки (1 байт)
  //        ? Порядковый номер оператора (1 байт) 1...30

  public int printBoldString(String s, boolean kt, boolean ct) throws FrException
  {
    byte[] par = new byte[25];
    longToBuf(par, pwd_oper, 4, 0);
    int f = 0;
    if (kt) f = f | 0x00000001;
    if (ct) f = f | 0x00000002;
    byteToBuf(par, (byte)(f & 0x000000FF), 4);
    strToBuf(par, s, 20, 5);
    return execCmd((byte)0x12, par);
  }
  
  /**
   * 13H - Гудок
   *  Команда:  13H. Длина сообщения: 5 байт.
   *  ? Пароль оператора (4 байта)
   *  Ответ:    13H. Длина сообщения: 3 байта.
   *  ? Код ошибки (1 байт)
   *  ? Порядковый номер оператора (1 байт) 1?30
   *
   * @return
   * @throws IOException
   */
  public int beep() throws FrException
  {
    return execCmd((byte)0x13, pwd_oper, null);
  }

   /**
    *
    * 14H - Установка параметров обмена
    *    Команда:  14H. Длина сообщения: 8 байт.
    *    ? Пароль системного администратора (4 байта)
    *      15
    *      Спецификация
    *    ? Номер порта (1 байт) 0?255
    *    ? Код скорости обмена (1 байт) 0?6
    *   ? Тайм аут приема байта (1 байт) 0?255
    *  Ответ:    14H. Длина сообщения: 2 байта.
    *  ? Код ошибки (1 байт)
    * @param spd_code
    * @param byte_timeout
    * @return
    * @throws IOException
    */
  public int setLinkParams(byte spd_code, byte byte_timeout) throws FrException
  {
    byte port = 0;
    return execCmd((byte)0x14, pwd_oper, new byte[] {port, spd_code, byte_timeout});
  }

  // 15H - Чтение параметров отбмена
//    Команда:  15H. Длина сообщения: 6 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Номер порта (1 байт) 0?255 
//    Ответ:    15H. Длина сообщения: 4 байта. 
//    ? Код ошибки (1 байт) 
//    ? Код скорости обмена (1 байт) 0?6 
//    ? Тайм аут приема байта (1 байт) 0?255   
  public int getLinkParams() throws FrException
  {
    byte port = 0;
    return execCmd((byte)0x15, pwd_oper, new byte[] {port});
  }

//    Печать строки 
//    Команда:  17H. Длина сообщения: 46 байт. 
//    ? Пароль оператора (4 байта) 
//    ? Флаги (1 байт) Бит 0 ? контрольная лента, Бит 1 ? чековая лента. 
//    ? Печатаемые символы (40 байт) 
//    Ответ:    17H. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
  public int printString(String s, boolean kt, boolean ct) throws FrException
  {
    byte[] par = new byte[45];
    longToBuf(par, pwd_oper, 4, 0);
    int f = 0;
    if (kt) f = f | 0x00000001;
    if (ct) f = f | 0x00000002;
    byteToBuf(par, (byte)(f & 0x000000FF), 4);
    strToBuf(par, s, 40, 5);
    return execCmd((byte)0x17, par);
    
  }


  //Печать заголовка документа
  //   Команда:    18H. Длина сообщения: 37 байт.
  //        ? Пароль оператора (4 байта)
  //        ? Наименование документа (30 байт)
  //        ? Номер документа (2 байта)
  //   Ответ:      18H. Длина сообщения: 5 байт.
  //        ? Код ошибки (1 байт)
  //        ? Порядковый номер оператора (1 байт) 1...30
  //        ? Сквозной номер документа (2 байта)

  public int printDocHeader(String s, int doc_num) throws FrException
  {
    byte[] par = new byte[36];
    longToBuf(par, pwd_oper, 4, 0);
    strToBuf(par, s, 30, 4);
    intToBuf(par, doc_num, 2, 34);
    return execCmd((byte)0x18, par);
  }
  
//    Запрос денежного регистра 
//    Команда: 1AH. Длина сообщения: 6 байт.  
//    ? Пароль оператора (4 байта) 
//    ? Номер регистра (1 байт) 0? 255 
//    Ответ:   1AH. Длина сообщения: 9 байт. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
//    ? Содержимое регистра (6 байт) 
  public int getMoneyReg(byte reg_num) throws FrException
  {
    return execCmd((byte)0x1A, pwd_oper, new byte[] {reg_num});
  }

  

//    Запрос операционного регистра 
//    Команда: 1BH. Длина сообщения: 6 байт.  
//    ? Пароль оператора (4 байта) 
//    ? Номер регистра (1 байт) 0? 255 
//    Ответ:   1BH. Длина сообщения: 5 байт. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
//    ? Содержимое регистра (2 байта)   
  public int getOperReg(byte reg_num) throws FrException
  {
    return execCmd((byte)0x1B, pwd_oper, new byte[] {reg_num});
  }

  // 1Eh - Запись таблицы
//    Команда:  1EH. Длина сообщения: (9+X) байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Таблица (1 байт) 
//    ? Ряд (2 байта) 
//    ? Поле (1 байт) 
//    ? Значение (X байт) до 40 байт 
//    Ответ:    1EH. Длина сообщения: 2 байта. 
//    ? Код ошибки (1 байт)   

  // CHANGED 20.12.07  
  // CHANGED 23.12.07 Object data (String or Integer)
  public int setTabData(byte tab_num, int row_num, int field_num, Object data, int x) throws FrException
  {
    byte[] par = new byte[8 + x/*data.length()*/];
    longToBuf(par, pwd_oper, 4, 0);
    byteToBuf(par, tab_num, 4);
    intToBuf(par, row_num, 2, 5);
    intToBuf(par, field_num, 1, 7);
    if (data instanceof Integer)
      intToBuf(par, ((Integer)data).intValue(), x, 8);
    else
      strToBuf(par, data.toString(), x, 8);
    return execCmd((byte)0x1E, par);
    
  }

  // 1Fh - Чтение таблицы
//    Команда:  1FH. Длина сообщения: 9 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Таблица (1 байт) 
//    ? Ряд (2 байта) 
//    ? Поле (1 байт) 
//    Ответ:    1FH. Длина сообщения: (2+X) байт. 
//    ? Код ошибки (1 байт) 
//    ? Значение (X байт) до 40 байт   
  public int getTabData(byte tab_num, int row_num, int field_num) throws FrException
  {
    byte[] par = new byte[8];
    longToBuf(par, pwd_oper, 4, 0);
    byteToBuf(par, tab_num, 4);
    intToBuf(par, row_num, 2, 5);
    intToBuf(par, field_num, 1, 7);
    return execCmd((byte)0x1F, par);
  }

    // дополнительно (дата / время)
    public int perfDataTimeCmd(byte cmd, byte b1, byte b2, byte b3) throws FrException
    {
      byte[] par = new byte[7];
      longToBuf(par, pwd_oper, 4, 0);
      byteToBuf(par, b1, 4);
      byteToBuf(par, b2, 5);
      byteToBuf(par, b3, 6);
      return execCmd(cmd, par);
    }
  // 21H - Программирование времени
//    Команда:  21H. Длина сообщения: 8 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Время (3 байта) ЧЧ-ММ-СС 
//    Ответ:    21H. Длина сообщения: 2 байта. 
//    ? Код ошибки (1 байт)   
  public int setTime(byte hh, byte mm, byte ss) throws FrException
  {
    return perfDataTimeCmd((byte)0x21, hh, mm, ss);
  }
  public int setTime(byte[] tm) throws FrException
  {
    return setTime(tm[0], tm[1], tm[2]);
  }
  

  // 22H - Программирование даты
//    Команда:  22H. Длина сообщения: 8 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Дата (3 байта) ДД-ММ-ГГ 
//    Ответ:    22H. Длина сообщения: 2 байта. 
//    ? Код ошибки (1 байт) 
  public int setDate(byte dd, byte mm, byte yy) throws FrException
  {
    return perfDataTimeCmd((byte)0x22, dd, mm, yy);
  }
  public int setDate(byte[] dt) throws FrException
  {
    return setDate(dt[0], dt[1], dt[2]);
  }

  

  // 23H - Подтверждение программирования даты
//    Команда:  23H. Длина сообщения: 8 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Дата (3 байта) ДД-ММ-ГГ 
//    Ответ:    23H. Длина сообщения: 2 байта. 
//    ? Код ошибки (1 байт) 
  public int confirmDate(byte dd, byte mm, byte yy) throws FrException
  {
    return perfDataTimeCmd((byte)0x23, dd, mm, yy);
  }
  public int confirmDate(byte[] dt) throws FrException
  {
    return confirmDate(dt[0], dt[1], dt[2]);
  }


  //Отрезка чека
  //   Команда:     25H. Длина сообщения: 6 байт.
  //        ? Пароль оператора (4 байта)
  //        ? Тип отрезки (1 байт) ?0? ? полная, ?1? ? неполная
  //   Ответ:       25H. Длина сообщения: 3 байта.
  //        ? Код ошибки (1 байт)
  //        ? Порядковый номер оператора (1 байт) 1...30
  public int cutCheck(boolean full)  throws FrException
  {
    byte[] par = new byte[5];
    longToBuf(par, pwd_oper, 4, 0);
    byteToBuf(par, full ? (byte)0x00 : (byte)0x01, 4) ;
    return execCmd((byte)0x25, par);
  }

  // 28H - Открыть денежный ящик 
//    Команда:  28H. Длина сообщения: 6 байт. 
//    ? Пароль оператора (4 байта) 
//    ? Номер денежного ящика (1 байт) 0?255 
//    Ответ:    28H. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
  public int openMoneyBox(byte box_num) throws FrException
  {
    byte[] par = new byte[5];
    longToBuf(par, pwd_oper, 4, 0);
    byteToBuf(par, box_num, 4);
    return execCmd((byte)0x28, par);
  }

//  Протяжка
//     Команда:     29H. Длина сообщения: 7 байт.
//          ? Пароль оператора (4 байта)
//          ? Флаги (1 байт) Бит 0 ? контрольная лента, Бит 1 ? чековая лента, Бит 2 ?
//            подкладной документ.
//          ? Количество строк (1 байт) 1...255 ? максимальное количество строк
//            ограничивается размером буфера печати, но не превышает 255
//     Ответ:       29H. Длина сообщения: 3 байта.
//          ? Код ошибки (1 байт)
//          ? Порядковый номер оператора (1 байт) 1...30
  public int transportTape(int num_rows, boolean c_t) throws FrException
  {
    byte[] par = new byte[6];
    longToBuf(par, pwd_oper, 4, 0);
    int f = c_t ? 0x00000002 : 0x00000001;
    byteToBuf(par, (byte)(f & 0x000000FF), 4);
    intToBuf(par, num_rows, 1, 5);

    return execCmd((byte)0x29, par);
  }


//    Прерывание тестового прогона 
//    Команда: 2BH. Длина сообщения: 5 байт. 
//    ? Пароль оператора (4 байта) 
//    Ответ:   2BH. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
  public int interruptTest() throws FrException
  {
    return execCmd((byte)0x2B, pwd_oper, null);
  }

  // 2Dh - Запрос структуры таблицы
//      Команда: 2DH. Длина сообщения: 6 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Номер таблицы (1 байт) 
//    Ответ:   2DH. Длина сообщения: 45 байт. 
//    ? Код ошибки (1 байт) 
//    ? Название таблицы (40 байт) 
//    ? Количество рядов (2 байта) 
//    ? Количество полей (1 байт) 
  public int getTabStructure(byte tab_num) throws FrException
  {
    return execCmd((byte)0x2D, pwd_oper, new byte[]{tab_num});
    // TODO - разбор данных
  }

  // 2Eh - Запрос структуры поля
//    Команда:  2EH. Длина сообщения: 7 байт. 
//    ? Пароль системного администратора (4 байта) 
//    ? Номер таблицы (1 байт) 
//    ? Номер поля (1 байт) 
//    Ответ:    2EH. Длина сообщения: (44+X+X) байт. 
//    ? Код ошибки (1 байт) 
//    ? Название поля (40 байт) 
//    ? Тип поля (1 байт) ?0? ? BIN, ?1? ? CHAR 
//    ? Количество байт ? X (1 байт) 
//    ? Минимальное значение поля ? для полей типа BIN (X байт) 
//    ? Максимальное значение поля ? для полей типа BIN (X байт)   
  public int getFieldStructure(byte tab_num, byte field_num) throws FrException
  {
    return execCmd((byte)0x2E, pwd_oper, new byte[]{tab_num, field_num});
    // TODO - разбор данных
  }

//    Суточный отчет без гашения 
//    Команда:  40H. Длина сообщения: 5 байт. 
//    ? Пароль администратора или системного администратора (4 байта) 
//    Ответ:    40H. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 29, 30   
  public int makeXReport() throws FrException
  {
    return execCmd((byte)0x40, pwd_oper, null);
  }

  // 41h - Суточный отчет с гашением
//    Команда:  41H. Длина сообщения: 5 байт. 
//    ? Пароль администратора или системного администратора (4 байта) 
//    Ответ:    41H. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 29, 30 
  public int makeClrZReport() throws FrException
  {
    return execCmd((byte)0x41, pwd_oper, null);
  }

  // 50h - внесение
//    Команда:  50H. Длина сообщения: 10 байт. 
//    ? Пароль оператора (4 байта) 
//    ? Сумма (5 байт) 
//    Ответ:    50H. Длина сообщения: 5 байт. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
//    ? Сквозной номер документа (2 байта)
  public int payInOper(long sum) throws FrException
  {
    byte[] par = new byte[9];
    longToBuf(par, pwd_oper, 4, 0);
    longToBuf(par, sum, 5, 4);
    
    return execCmd((byte)0x50, par);
  }

  // 51h - выплата
//    Команда:  51H. Длина сообщения: 10 байт. 
//    ? Пароль оператора (4 байта) 
//    ? Сумма (5 байт) 
//    Ответ:    51H. Длина сообщения: 5 байт. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
//    ? Сквозной номер документа (2 байта) 
  public int payOutOper(long sum) throws FrException
  {
    byte[] par = new byte[9];
    longToBuf(par, pwd_oper, 4, 0);
    longToBuf(par, sum, 5, 4);
    
    return execCmd((byte)0x51, par);
  }


//    Команда:  80H, 81H, 82H, 84H. Длина сообщения: 60 байт. 
//    ? Пароль оператора (4 байта) 
//    ? Количество (5 байт) 0000000000?9999999999 
//    ? Цена (5 байт) 0000000000?9999999999 
//    ? Номер отдела (1 байт) 0?16 
//    ? Налог 1 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Налог 2 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Налог 3 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Налог 4 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Текст (40 байт) 
//    Ответ:    80H. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
    // дополнительно, позиция в чеке
    private int checkPosition(byte cmd, long quan, long price, int section, 
                               byte n1, byte n2, byte n3, byte n4, String text) throws FrException
    {
      byte[] par = new byte[59];

      longToBuf(par, pwd_oper, 4, 0);
      longToBuf(par, quan, 5, 4);
      longToBuf(par, price, 5, 9);
      intToBuf(par, section, 1, 14);
      byteToBuf(par, n1, 15);
      byteToBuf(par, n2, 16);
      byteToBuf(par, n3, 17);
      byteToBuf(par, n4, 18);
      strToBuf(par, text, 40, 19);
    
      return execCmd(cmd, par);
    }

  // 80h - Продажа
  public int salePosition(long quan, long price, int section, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return checkPosition((byte)0x80, quan, price, section, n1, n2, n3, n4, text);
  }

  // 81h - Покупка
  public int buyPosition(long quan, long price, int section, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return checkPosition((byte)0x81, quan, price, section, n1, n2, n3, n4, text);
  }

  // 82h - Возврат продажи         
  public int saleReturnPosition(long quan, long price, int section, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return checkPosition((byte)0x82, quan, price, section, n1, n2, n3, n4, text);
  }

  // 83h - Возврат покупки
  public int buyReturnPosition(long quan, long price, int section, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return checkPosition((byte)0x83, quan, price, section, n1, n2, n3, n4, text);
  }

  // 84h - строно
  public int cancelPosition(long quan, long price, int section, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return checkPosition((byte)0x84, quan, price, section, n1, n2, n3, n4, text);
  }

  // 85h - Закрытие чека
//    Команда:  85H. Длина сообщения: 71 байт. 
//    ? Пароль оператора (4 байта) 
//    ? Сумма наличных (5 байт) 0000000000?9999999999 
//    ? Сумма типа оплаты 2 (5 байт) 0000000000?9999999999 
//    ? Сумма типа оплаты 3 (5 байт) 0000000000?9999999999 
//    ? Сумма типа оплаты 4 (5 байт) 0000000000?9999999999 
//    ? Скидка в % на чек от 0 до 99,99 % (2 байта) 0000?9999 
//    ? Налог 1 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Налог 2 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Налог 3 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Налог 4 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа 
//    ? Текст (40 байт) 
//    Ответ:    85H. Длина сообщения: 8 байт. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
//    ? Сдача (5 байт) 0000000000?9999999999 
  public int closeCheck(long nal, long pt2, long pt3, long pt4, int dsc_pc, 
                          byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    byte[] par = new byte[70];

    longToBuf(par, pwd_oper, 4, 0);

    longToBuf(par, nal, 5, 4);
    longToBuf(par, pt2, 5, 9);
    longToBuf(par, pt3, 5, 14);
    longToBuf(par, pt4, 5, 19);
    intToBuf(par, dsc_pc, 2, 24);
    byteToBuf(par, n1, 26);
    byteToBuf(par, n2, 27);
    byteToBuf(par, n3, 28);
    byteToBuf(par, n4, 29);
    strToBuf(par, text, 40, 30);
    
    return execCmd((byte)0x85, par);
    // сдача в результате TODO
  }

/*
Скидка
Команда:  86H. Длина сообщения: 54 байт.
? Пароль оператора (4 байта)
? Сумма (5 байт) 0000000000?9999999999
? Налог 1 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Налог 2 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Налог 3 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Налог 4 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Текст (40 байт)
Ответ:    86H. Длина сообщения: 3 байта.
? Код ошибки (1 байт)
? Порядковый номер оператора (1 байт) 1?30
*/
    private int _dscPos(byte cmd, long sum, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
    {
        byte[] par = new byte[53];

        longToBuf(par, pwd_oper, 4, 0);
        longToBuf(par, sum, 5, 4);
        byteToBuf(par, n1, 9);
        byteToBuf(par, n2, 10);
        byteToBuf(par, n3, 11);
        byteToBuf(par, n4, 12);
        strToBuf(par, text, 40, 13);

        return execCmd(cmd, par);
    }

  public int discountPos(long sum, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return _dscPos((byte)0x86, sum, n1, n2, n3, n4, text);
  }


/*
Надбавка
Команда:  87H. Длина сообщения: 54 байт.
? Пароль оператора (4 байта)
? Сумма (5 байт) 0000000000?9999999999
? Налог 1 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Налог 2 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Налог 3 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Налог 4 (1 байт) ?0? ? нет, ?1???4? ? налоговая группа
? Текст (40 байт)
Ответ:    87H. Длина сообщения: 3 байта.
? Код ошибки (1 байт)
? Порядковый номер оператора (1 байт) 1?30
*/
  public int echargePos(long sum, byte n1, byte n2, byte n3, byte n4, String text) throws FrException
  {
    return _dscPos((byte)0x87, sum, n1, n2, n3, n4, text);
  }

  // 88h - Аннулирование чека
//      Команда:  88H. Длина сообщения: 5 байт. 
//    ? Пароль оператора (4 байта) 
//    Ответ:    88H. Длина сообщения: 3 байта. 
//    ? Код ошибки (1 байт) 
//    ? Порядковый номер оператора (1 байт) 1?30 
  public int calcelCheck() throws FrException
  {
    return execCmd((byte)0x88, pwd_oper, null);
  }

  // 8Dh - открыть чек (0 - чек продажи)
  public int openCheck(byte check_type) throws FrException
  {
    return execCmd((byte)0x8D, pwd_oper, new byte[]{check_type});
  }


//    Продолжение печати 
//    Команда: B0h. Длина сообщения: 5 байт.
//    ? Пароль оператора, администратора или системного администратора (4 байта)
//    Ответ: B0H. Длина сообщения: 3 байта.
//    ? Код ошибки (1 байт)
//    ? Порядковый номер оператора (1 байт) 1?30   
  public int continuePrint() throws FrException
  {
    return execCmd((byte)0xB0, pwd_oper, null);
  }



  

  
}