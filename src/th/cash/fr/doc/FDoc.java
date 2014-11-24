/*
 * FDoc.java
 *
 * Created on 21 ������ 2007 �., 16:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package th.cash.fr.doc;

import java.util.Date;
import java.util.Vector;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.ResultSet;

import th.cash.fr.doc.Position;

import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.FrException;

import th.cash.dev.FiscalPrinter;

import th.cash.model.SprModel;
import th.cash.model.Tax;
import th.cash.model.User;

/**
 * ����� - ������ ����������� ���������
 * @author lazarev
 */
public abstract class FDoc {
    
    // ���������
    public final static int FD_SALE_TYPE    = 1;
    public final static int FD_RETURN_TYPE  = 2;

    public final static int FD_PAY_IN_TYPE  = 3;
    public final static int FD_PAY_OUT_TYPE = 4;

    public final static int FD_ZREPORT_TYPE = 5;
    public final static int FD_XREPORT_TYPE = 6;

    //
    protected final static String[] TYPE_NAMES = 
      {"�������", "�������", "��������", "�������", "Z-�����","X-�����"};

    // ��� ��������� (�������/�������/�������/�������/Z-�����)
    protected int     type_fd;  
    protected String  tname_fd; 
    
    protected int     cashNum;        // ����� �����
    protected int     docNum;           // ����� ���������
    protected Date    openDate;       // ���� ���� (������)
    protected Date    closeDate;      // ���� �������� (������������) ���������
    
    protected int     cashierId;    // ��� � ��� �������
    protected String  cashierName;  //
    

    // --------------------------------------------------------
    // ���� ����
    protected double sum = 0;       // ����� ������� (�� ��������!) 
    protected double nalSum = 0;    // ����� ��������� ...
    protected double change = 0;    // �����

    protected double bnSum = 0;     // ����� �� ������� ...

    protected Vector transactions = null;
    


    /** type FDoc in Constructor */
    public void setDocType(int type)
    {
        type_fd = type;
        tname_fd = TYPE_NAMES[type_fd - 1];
    }
        
  // ********************************************************************
    
    public void open() 
    {
      openDate = new Date();
      transactions = new Vector();
    }

    // �������������� �������� !!! �����
    public void close(User u) 
    {
      closeDate = new Date();
      setCashierId(u.getCode().intValue());
      setCashierName(u.getName());
    }

    public abstract void print(FiscalPrinter fp) throws Exception;

    // ��������� ��� ���������� � ������ ������ COMMITED !!!

    public void save(Connection c, boolean tr_log) throws SQLException
    {
      DbConverter.saveFDoc(c, this, tr_log);
    }


  
  // **************************************************************************
  public int getCashNum()
  {
    return cashNum;
  }

  public void setCashNum(int newCashNum)
  {
    cashNum = newCashNum;
  }


  public int getCashierId()
  {
    return cashierId;
  }

  public void setCashierId(int newCashierId)
  {
    cashierId = newCashierId;
  }

  public String getCashierName()
  {
    return cashierName;
  }

  public void setCashierName(String newCashierName)
  {
    cashierName = newCashierName;
  }

  public void setDocNum(int num)
  {
    docNum = num;
  }

  public int getDocNum()
  {
    return docNum;
  }

  public Date getCloseDate()
  {
    return closeDate;
  }

  public Date getOpenDate()
  {
    return openDate;
  }

  public double getSum()
  {
    return sum;
  }

  public void setSum(double d)
  {
    this.sum = d;
  }

  public double getNalSum()
  {
    return nalSum;
  }

  public double getBnSum()
  {
    return bnSum;
  }

  public void setNalSum(double d)
  {
    this.nalSum = d;
    change = nalSum + bnSum - sum;
  }

  public void setBNSum(double d)
  {
    this.bnSum = d;
    change = nalSum + bnSum - sum;
  }

  public double getChange()
  {
    return change;
  }

  public String getTypeName()
  {
    return tname_fd;
  }

  public int getTypeId() {return type_fd; }
}
