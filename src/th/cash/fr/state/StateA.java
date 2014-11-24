package th.cash.fr.state;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

//import th.cash.log.LogUtil;
import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.FrException;

import org.apache.log4j.Logger;

// ����������� �����, ������ � ��������, �����������, �������
// ����� ��������...
public abstract class StateA implements StateConst  // added interface StateConst
{

  protected byte   operNum;            // ���������� ����� ��������� (1 ����) 1?30
  protected int    flagsFr;            // ����� �� (2 �����) 
  protected byte   modeFr;             // ����� �� (1 ����) 
  protected byte   subModeFr;          // �������� �� (1 ����)

  protected int    modeNum, modeStatus; // ����������� - ����� � ������ ������

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
  // ��������� ����� �����
  private boolean getIntFlag(int fb, int bit_num)
  {
    int mask = 1 << bit_num;
    return (fb & mask) > 0;
  }

  //*********************************************************************
  // flagsFr
  // 0 - ����� ������������� ������� (0 ? ���, 1 ? ����) 
  public boolean isFFrRollLog()
  {
    return getIntFlag(flagsFr, 0);
  }
  // 1 - ����� ������� ����� (0 ? ���, 1 ? ����) 
  public boolean isFFrRollCheck()
  {
    return getIntFlag(flagsFr, 1);
  }
  // 2 - ������� ������ ����������� ��������� (0 ? ���, 1 ? ��) 
  public boolean isFFrTopSensorPd()
  {
    return getIntFlag(flagsFr, 2);
  }
  // 3 - ������ ������ ����������� ��������� (0 ? ���, 1 ? ��)
  public boolean isFFrBotSensorPd()
  {
    return getIntFlag(flagsFr, 3);
  }
  // 4 - ��������� ���������� ����� (0 ? 0 ������, 1 ? 2 �����)
  public boolean isFFrDecimalDotPos()
  {
    return getIntFlag(flagsFr, 4);
  }
  // 5 - ���� (0 ? ���, 1 ? ����) 
  public boolean isFFrEKLZ()
  {
    return getIntFlag(flagsFr, 5);
  }
  // 6 - ���������� ������ ������������� ������� (0 ? ������ ���, 1 ? ������ ����) 
  public boolean isFFrOptSensorLog()
  {
    return getIntFlag(flagsFr, 6);
  }
  // 7 -  ���������� ������ ������� ����� (0 ? ������ ���, 1 ? ������ ����) 
  public boolean isFFrOptSensorCheck()
  {
    return getIntFlag(flagsFr, 7);
  }
  // 8 - ����� ������������ ����������� ����� (0 ? ������, 1 ? ������) 
  public boolean isFFrLeverPrintLog()
  {
    return getIntFlag(flagsFr, 8);
  }
  // 9 -  ����� ������������ ������� ����� (0 ? ������, 1 ? ������) 
  public boolean isFFrLeverPrintCheck()
  {
    return getIntFlag(flagsFr, 9);
  }
  // 10 - ������ ������� �� (0 ? �������, 1 ? �������) 
  public boolean isFFrCoverOpened()
  {
    return getIntFlag(flagsFr, 10);
  }
  // 11 - �������� ���� (0 ? ������, 1 ? �����) 
  public boolean isFFrMoneyBoxOpened()
  {
    return getIntFlag(flagsFr, 11);
  }
  // 12 - ����� ������� ������� �������� (0 ? ���, 1 ? ��) 
  public boolean isFFrRightPrintSensor()
  {
    return getIntFlag(flagsFr, 12);
  }
  // 13 - ����� ������ ������� �������� (0 ? ���, 1 ? ��) 
  public boolean isFFrLeftPrintSensor()
  {
    return getIntFlag(flagsFr, 13);
  }
  // 14 -  ���� ����� ��������� (0 ? ���, 1 ? ��) 
  public boolean isFFrEKLZFull()
  {
    return getIntFlag(flagsFr, 14);
  }
  // 15 - ����������� �������� ���������� (0 ? ���������� ��������, 1 ? ����������� ��������) [��� ��� ��� ����] 
  public boolean isFFrHighPrecisionQuantity()
  {
    return getIntFlag(flagsFr, 15);
  }

  //************************************************************************
  // �������� ������:
  // "������� � ������� ������."
  public boolean isMFrPrinterOk()
  {
    return modeNum == 0;
  }

  // "�������� �����, 24 ���� �� ���������."
  public boolean isMFrOpenedShiftOk()
  {
    return modeNum == 2;
  }

  // "�������� �����, 24 ���� ���������."
  public boolean isMFrOpenedShiftExceed()
  {
    return modeNum == 3;
  }

  // "�������� �����."
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
      case 0  : return "������� � ������� ������.";
      case 1  : return "������ ������."; 
      case 2  : return "�������� �����, 24 ���� �� ���������."; 
      case 3  : return "�������� �����, 24 ���� ���������.";
      case 4  : return "�������� �����.";
      case 5  : return "���������� �� ������������� ������ ���������� ����������.";
      case 6  : return "�������� ������������� ����� ����.";
      case 7  : return "���������� ��������� ��������� ���������� �����.";
      case 8  : return "�������� ��������";
      case 9  : return "����� ���������� ���������������� ���������. � ���� ����� ��� ��������� �� "+
                       "��������� �������, ���� ����������� ���������� � ����������������� ��� ���.";
      case 10 : return "�������� ������.";
      case 11 : return "������ ������� ���. ������.";
      case 12 : return "������ ��ޣ�� ����.";
      case 13 : return "������ � ���������� ���������� ����������";
      case 14 : return "������ ����������� ���������.";
      case 15 : return "���������� ���������� �������� �����������.";
      default : return "����������� ����� ������";
    }
  }

  public String getModeStatusStr()
  {
    switch (modeNum)
    {
      case 8  :
        switch (modeStatus)
        {
          case 0 : return "�������.";
          case 1 : return "�������."; 
          case 2 : return "������� �������.";
          case 3 : return "������� �������.";         
          default : return "";
        }
      case 13 :
        switch (modeStatus)
        {
          case 0 : return "������� (������).";
          case 1 : return "������� (������)."; 
          case 2 : return "������� ������� (������).";
          case 3 : return "������� ������� (������).";         
          default : return "";
        }
      case 14 :
        switch (modeStatus)
        {
          case 0 : return "�������� ��������.";
          case 1 : return "�������� � ����������������."; 
          case 2 : return "����������������.";
          case 3 : return "������.";         
          case 4 : return "������ ���������.";         
          case 5 : return "������ ���������.";         
          case 6 : return "�������� ����������.";         
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
      case 0 : return "������ ���� - �� �� � ���� ������ �������� - ����� ��������� �� ����� �������, "+
        "��������� � ������� �� ��� ���������, ������ �������� �������� � ������� ������.";
      case 1 : return "��������� ���������� ������ - �� �� � ���� ������ �������� - �� ��������� �� "+
        "����� �������, ��������� � ������� �� ��� ���������, ������ �������� �������� �� "+
        "���������� ������.";
      case 2 : return "�������� ���������� ������ - �� � ���� ������ �������� - ��������� ������ "+
        "�������, �� ��������� � �������. ������� �� ����� ��������� ������ � �������� 3.";
      case 3 : return "����� ��������� ���������� ������ - �� ���� ������� ����������� ������. ����� "+
        "����� ��������� �������, �� ��������� � �������.";
      case 4 : return "���� ������ �������� ������ ���������� ������� - �� �� ��������� �� ����� "+
        "�������, ��������� � �������, ����� ������� ���������� ������.";
      case 5 : return "���� ������ �������� - �� �� ��������� �� ����� �������, ��������� � �������.";
      default : return "����������� ��������";
    }
  }


  protected final static char TAB = '\t';

  // debug printing
  protected void printFlags()
  {
    debug("{************************ ����� **************************");
    debug("����� ������������� �������" + TAB + isFFrRollLog());
    debug("����� ������� �����" + TAB + isFFrRollCheck());
    debug("������� ������ ����������� ���������" + TAB + isFFrTopSensorPd()); 
    debug("������ ������ ����������� ���������" + TAB + isFFrBotSensorPd());
    debug("��������� ���������� �����" + TAB + isFFrDecimalDotPos());
    debug("���� (0 ? ���, 1 ? ����)" + TAB + isFFrEKLZ());
    debug("���������� ������ ������������� ������� (0 ? ������ ���, 1 ? ������ ����)" + TAB + isFFrOptSensorLog());
    debug("���������� ������ ������� ����� (0 ? ������ ���, 1 ? ������ ����)" + TAB + isFFrOptSensorCheck());
    debug("����� ������������ ����������� ����� (0 ? ������, 1 ? ������)" + TAB + isFFrLeverPrintLog());
    debug("����� ������������ ������� ����� (0 ? ������, 1 ? ������)" + TAB + isFFrLeverPrintCheck());
    debug("������ ������� �� (0 ? �������, 1 ? �������)" + TAB + isFFrCoverOpened());
    debug("�������� ���� (0 ? ������, 1 ? �����)" + TAB + isFFrMoneyBoxOpened());
    debug("����� ������� ������� �������� (0 ? ���, 1 ? ��)" + TAB + isFFrRightPrintSensor());
    debug("����� ������ ������� �������� (0 ? ���, 1 ? ��)" + TAB + isFFrLeftPrintSensor());
    debug("���� ����� ��������� (0 ? ���, 1 ? ��)" + TAB + isFFrEKLZFull());
    debug("����������� �������� ���������� (0 ? ���������� ��������, 1 ? ����������� ��������)" + TAB + isFFrHighPrecisionQuantity());
    debug("************************ ����� **************************}");
  }

  protected void printModes()
  {
    debug("{************************ ������ **************************");

    debug("�����:" + TAB + getModeNum() + " (" + getModeNumStr() + ")");

    debug("��������:" + TAB + getSubModeFr() + " (" + getSubModeFrStr() + ")");

    debug("������:" + TAB + getModeStatus() + " (" + getModeStatusStr() + ")" );
    
    debug("************************ ������ **************************}");
  }

  
  public void printAll(StringBuffer sb)
  {
    log_buf = sb;

    printAll();

    log_buf = null;
  }

  // ����� ��� StringBuffer ��� Logger
  protected void debug(String s)
  {
    if (log.isDebugEnabled()) log.debug(s);
    if (log_buf != null) { log_buf.append(s); log_buf.append('\n'); }
  }
  
}