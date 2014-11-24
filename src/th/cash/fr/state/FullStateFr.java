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

// ���������� ��������� �� (����� �� ������ ���������)
public class FullStateFr extends StateA
{
  // TODO - add date/time fields
//  private byte   operNum;            // ���������� ����� ��������� (1 ����) 1?30
  private String verSoftFr;          // ������ �� �� (2 �����)
  private int    buildSoftFr;        // ������ �� �� (2 �����) 
  private byte[] dateSoftFr;         // ���� �� �� (3 �����) ��-��-��
  private byte   frNum;              // ����� � ���� (1 ����)
  private int    curDocNum;          // �������� ����� �������� ��������� (2 �����) 
//  private int    flagsFr;            // ����� �� (2 �����) 
//  private byte   modeFr;             // ����� �� (1 ����) 
//  private byte   subModeFr;          // �������� �� (1 ����)
  private byte   portFr;             // ���� �� (1 ����) 
  private String verSofrFp;          // ������ �� �� (2 �����)
  private int    buildSoftFp;        // ������ �� �� (2 �����) 
  private byte[] dateSoftFp;         // ���� �� �� (3 �����) ��-��-�� 
  private byte[] curDt;              // ���� (3 �����) ��-��-��
  private byte[] curTm;              // ����� (3 �����) ��-��-��
    private Date curDate;              // �� �� � ������� java.util.Date  
  private byte   flagsFp;            // ����� �� (1 ����)
  private long   serNumber;          // ��������� ����� (4 �����)
  private int    lastCloseShift;     // ����� ��������� �������� ����� (2 �����)
  private int    freeRecordsFp;      // ���������� ��������� ������� � �� (2 �����)
  private int    numFiscals;         // ���������� ��������������� (������������) (1 ����)
  private int    numAccesibleFiscals;// ���������� ���������� ��������������� (������������) (1 ����)
  private long   inn;                // ��� (6 ����)                                   


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
    debug("{********************* ������ ������ ��������� �� ***********************");
    debug("����� ���������" + TAB + operNum);
    debug("������ �� ��" + TAB + verSoftFr);
    debug("������ �� ��" + TAB + buildSoftFr);
    debug("���� �� ��" + TAB +  getDTStr(dateSoftFr, '-'));
    debug("����� ��" + TAB + frNum);
    debug("����� ���. ���." + TAB + curDocNum);

    printFlags();
    printModes();

    debug("���� ��" + TAB + portFr);
    debug("������ �� ��" + TAB + verSofrFp);
    debug("������ �� ��" + TAB + buildSoftFp);

    debug("���� �� ��" + TAB +  getDTStr(dateSoftFp, '-'));
    debug("���. ����" + TAB +  getDTStr(curDt, '-'));
    debug("�����" + TAB +  getDTStr(curTm, ':'));

    
    debug("�������� �����" + TAB + serNumber);
    debug("��������� �������� �����" + TAB + lastCloseShift);
    debug("��������� ������� � ��" + TAB + freeRecordsFp);
    debug("����� ������������" + TAB + numFiscals);

    debug("������ ������������" + TAB + numAccesibleFiscals);
    debug("���" + TAB + inn);
    debug("********************* ������ ������ ��������� �� ***********************}");
   
  }
}