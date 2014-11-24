package th.cash.fr.state;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.FrException;

public class ShortStateFr extends StateA
{

//  private byte   operNum;            // ���������� ����� ��������� (1 ����) 1?30
//  private int    flagsFr;            // ����� �� (2 �����) 
//  private byte   modeFr;             // ����� �� (1 ����) 
//  private byte   subModeFr;          // �������� �� (1 ����)
  private byte operInCheck;          // ���������� �������� � ���� (1 ����)
  private byte reserveBatVoltage;    // ���������� ��������� ������� (1 ����)
  private byte powerVoltage;         // ���������� ��������� ������� (1 ����)
  private byte errCodeFp;            // ��� ������ �� (1 ����)
  private byte errCodeEKLZ;          // ��� ������ ���� (1 ����)
  


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
    debug("{******************** �������� ������ ��������� �� ***********************");
    debug("����� ���������" + TAB + operNum);

    printFlags();
    printModes();
    
    debug("operInCheck" + TAB + operInCheck);
    debug("����. ��������� �������" + TAB + (0x000000FF & reserveBatVoltage));
    debug("���������� �������" + TAB + (0x000000FF & powerVoltage));
    debug("��� ������ ��" + TAB + errCodeFp);

    debug("��� ������ ����" + TAB + errCodeEKLZ);
    debug("******************** �������� ������ ��������� �� ***********************}");
     
  }
  
}