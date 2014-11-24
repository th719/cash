package th.cash.fr.state;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.FrException;

public class ShortStateFr extends StateA
{

//  private byte   operNum;            // Порядковый номер оператора (1 байт) 1?30
//  private int    flagsFr;            // Флаги ФР (2 байта) 
//  private byte   modeFr;             // Режим ФР (1 байт) 
//  private byte   subModeFr;          // Подрежим ФР (1 байт)
  private byte operInCheck;          // Количество операций в чеке (1 байт)
  private byte reserveBatVoltage;    // Напряжение резервной батареи (1 байт)
  private byte powerVoltage;         // Напряжение источника питания (1 байт)
  private byte errCodeFp;            // Код ошибки ФП (1 байт)
  private byte errCodeEKLZ;          // Код ошибки ЭКЛЗ (1 байт)
  


  public void initFromFr(FrDrv fr) throws FrException
  {
    int res;
    res = fr.shortStateRequest();
    if (res == 0 && fr.isCmdOk())
      decodeParams(fr.getReplyParams());
  }

  public void decodeParams(byte[] data) throws FrException
  {
    operNum = th.cash.fr.FrUtil.byteFromBytes(data, 0);
    flagsFr = th.cash.fr.FrUtil.intFromBytes(data, 1, 2);
    setModeFr(th.cash.fr.FrUtil.byteFromBytes(data, 3)); // !!!
    subModeFr = th.cash.fr.FrUtil.byteFromBytes(data, 4);
    operInCheck = th.cash.fr.FrUtil.byteFromBytes(data, 5);
    reserveBatVoltage = th.cash.fr.FrUtil.byteFromBytes(data, 6);
    powerVoltage = th.cash.fr.FrUtil.byteFromBytes(data, 7);
    errCodeFp = th.cash.fr.FrUtil.byteFromBytes(data, 8);
    errCodeEKLZ = th.cash.fr.FrUtil.byteFromBytes(data, 9);
  }
  

  public void printAll()
  {
    debug("{******************** Короткий запрос состояния ФР ***********************");
    debug("Номер оператора" + TAB + operNum);

    printFlags();
    printModes();
    
    debug("operInCheck" + TAB + operInCheck);
    debug("Напр. резервной батареи" + TAB + (0x000000FF & reserveBatVoltage));
    debug("Напряжение питания" + TAB + (0x000000FF & powerVoltage));
    debug("Код ошибки ФП" + TAB + errCodeFp);

    debug("Код ошибки ЭКЛЗ" + TAB + errCodeEKLZ);
    debug("******************** Короткий запрос состояния ФР ***********************}");
     
  }
  
}