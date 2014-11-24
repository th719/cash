package th.cash.fr.state;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Arrays;
import java.text.ParseException;

import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.Hex;

//import th.cash.log.LogUtil;
import th.cash.fr.FrException;

// дескриптор состояния ФР (ответ на запрос состояния)
public class FullStateFr extends StateA
{
  // TODO - add date/time fields
//  private byte   operNum;            // Порядковый номер оператора (1 байт) 1?30
  private String verSoftFr;          // Версия ПО ФР (2 байта)
  private int    buildSoftFr;        // Сборка ПО ФР (2 байта) 
  private byte[] dateSoftFr;         // Дата ПО ФР (3 байта) ДД-ММ-ГГ
  private byte   frNum;              // Номер в зале (1 байт)
  private int    curDocNum;          // Сквозной номер текущего документа (2 байта) 
//  private int    flagsFr;            // Флаги ФР (2 байта) 
//  private byte   modeFr;             // Режим ФР (1 байт) 
//  private byte   subModeFr;          // Подрежим ФР (1 байт)
  private byte   portFr;             // Порт ФР (1 байт) 
  private String verSofrFp;          // Версия ПО ФП (2 байта)
  private int    buildSoftFp;        // Сборка ПО ФП (2 байта) 
  private byte[] dateSoftFp;         // Дата ПО ФП (3 байта) ДД-ММ-ГГ 
  private byte[] curDt;              // Дата (3 байта) ДД-ММ-ГГ
  private byte[] curTm;              // Время (3 байта) ЧЧ-ММ-СС
    private Date curDate;              // то же в формате java.util.Date  
  private byte   flagsFp;            // Флаги ФП (1 байт)
  private long   serNumber;          // Заводской номер (4 байта)
  private int    lastCloseShift;     // Номер последней закрытой смены (2 байта)
  private int    freeRecordsFp;      // Количество свободных записей в ФП (2 байта)
  private int    numFiscals;         // Количество перерегистраций (фискализаций) (1 байт)
  private int    numAccesibleFiscals;// Количество оставшихся перерегистраций (фискализаций) (1 байт)
  private long   inn;                // ИНН (6 байт)                                   


  public void initFromFr(FrDrv fr) throws FrException
  {
    int res;
    res = fr.stateRequest();
    if (res == 0 && fr.isCmdOk())
      decodeParams(fr.getReplyParams());
  }

  public void decodeParams(byte[] data) throws FrException
  {
    operNum = th.cash.fr.FrUtil.byteFromBytes(data, 0);
    verSoftFr = th.cash.fr.FrUtil.strFromBytes(data, 1, 2);
    buildSoftFr = th.cash.fr.FrUtil.intFromBytes(data, 3, 2);
    //5,3                            
    dateSoftFr = FrUtil.getSelBytes(data, 3, 5);
    frNum = th.cash.fr.FrUtil.byteFromBytes(data, 8);
    curDocNum = th.cash.fr.FrUtil.intFromBytes(data, 9, 2);
    flagsFr = th.cash.fr.FrUtil.intFromBytes(data, 11, 2);
    setModeFr(th.cash.fr.FrUtil.byteFromBytes(data, 13)); // !!!
    subModeFr = th.cash.fr.FrUtil.byteFromBytes(data, 14);
    portFr = th.cash.fr.FrUtil.byteFromBytes(data, 15);
    verSofrFp = th.cash.fr.FrUtil.strFromBytes(data, 16, 2);
    buildSoftFp = th.cash.fr.FrUtil.intFromBytes(data, 18, 2);
    
    dateSoftFp = FrUtil.getSelBytes(data, 3, 20);
    curDt = FrUtil.getSelBytes(data, 3, 23);
    curTm = FrUtil.getSelBytes(data, 3, 26);
    curDate = FrUtil.dateFromBytes(curDt, curTm);
    // 19, 3
    // 22, 3
    // 25, 3
    flagsFp = th.cash.fr.FrUtil.byteFromBytes(data, 29);
    serNumber = th.cash.fr.FrUtil.longFromBytes(data, 30, 4);
    lastCloseShift = th.cash.fr.FrUtil.intFromBytes(data, 34, 2);
    freeRecordsFp = th.cash.fr.FrUtil.intFromBytes(data, 36, 2);
    numFiscals = th.cash.fr.FrUtil.byteFromBytes(data, 38);
    numAccesibleFiscals = th.cash.fr.FrUtil.byteFromBytes(data, 39);
    inn = th.cash.fr.FrUtil.longFromBytes(data, 40, 6);
   
  }
  

  

  //*********************************************************************
  // get properties
  public int getBuildSoftFp()
  {
    return buildSoftFp;
  }

  public int getBuildSoftFr()
  {
    return buildSoftFr;
  }

  public int getCurDocNum()
  {
    return curDocNum;
  }

  public byte getFrNum()
  {
    return frNum;
  }

  public int getFreeRecordsFp()
  {
    return freeRecordsFp;
  }

  public long getInn()
  {
    return inn;
  }

  public int getLastCloseShift()
  {
    return lastCloseShift;
  }

  public int getNumAccesibleFiscals()
  {
    return numAccesibleFiscals;
  }

  public int getNumFiscals()
  {
    return numFiscals;
  }

  public byte getOperNum()
  {
    return operNum;
  }

  public byte getPortFr()
  {
    return portFr;
  }

  public long getSerNumber()
  {
    return serNumber;
  }

  public String getVerSofrFp()
  {
    return verSofrFp;
  }

  public String getVerSoftFr()
  {
    return verSoftFr;
  }

  public Date getCurDate()
  {
    return curDate;
  }



  public void printAll()
  {
    debug("{********************* Полный запрос состояния ФР ***********************");
    debug("Номер оператора" + TAB + operNum);
    debug("Версия ПО ФР" + TAB + verSoftFr);
    debug("Сборка ПО ФР" + TAB + buildSoftFr);
    debug("Дата ПО ФР" + TAB +  getDTStr(dateSoftFr, '-'));
    debug("Номер ФР" + TAB + frNum);
    debug("Номер тек. док." + TAB + curDocNum);

    printFlags();
    printModes();

    debug("Порт ФР" + TAB + portFr);
    debug("Версия ПО ФП" + TAB + verSofrFp);
    debug("Сборка ПО ФП" + TAB + buildSoftFp);

    debug("Дата ПО ФП" + TAB +  getDTStr(dateSoftFp, '-'));
    debug("Тек. дата" + TAB +  getDTStr(curDt, '-'));
    debug("Время" + TAB +  getDTStr(curTm, ':'));

    
    debug("Серийный номер" + TAB + serNumber);
    debug("Последняя закрытая смена" + TAB + lastCloseShift);
    debug("Свободных записей в ФП" + TAB + freeRecordsFp);
    debug("Число фискализаций" + TAB + numFiscals);

    debug("Ресурс фискализаций" + TAB + numAccesibleFiscals);
    debug("ИНН" + TAB + inn);
    debug("********************* Полный запрос состояния ФР ***********************}");
   
  }
}