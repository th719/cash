package th.cash.fr.table;

import java.util.Vector;
import java.io.IOException;

import th.cash.fr.FrDrv;
import th.cash.fr.FrUtil;
import th.cash.fr.Hex;

//import th.cash.log.LogUtil;
import th.cash.fr.FrException;

import org.apache.log4j.Logger;

// таблица регистратора
// значиния типа Integer или String
public class FrTable 
{

  private byte num;
  private String name;
  private int numRows = 0;
  private int numCols = 0;

  // структура и данные
  private Vector field_descr = null;   // описания полей
  private Vector rows = null;          // строки в таблице
  private Vector changed_cells = null; // набор измененных строк

  protected final static String LOG_PREF = "FR"; 
  protected Logger log = Logger.getLogger(LOG_PREF + '.' + this.getClass().getName());
  
  // только номер, остальное читается из фискальника
  public FrTable(byte tab_num)
  {
    num = tab_num;
  }

  // только чтение
  public byte getNum()
  {
    return num;
  }

  public String getName()
  {
    return name;
  }

  public int getNumRows()
  {
    return numRows;
  }

  public int getNumCols()
  {
    return numCols;
  }

  // прочитать / сохранить
  public void initFromFr(FrDrv fr) throws FrException
  {
    readTabStructure(fr);
    initDataVectors();
    readFieldDescr(fr);
    readTabData(fr);
    changed_cells = null;
  }

  public void saveToFr(FrDrv fr) throws FrException
  {
    if (changed_cells != null && changed_cells.size() > 0)
    {
      int i = 0;
      int res = 0;
      boolean cmd_res = true;
      CellIndex cind;
      Object val;
      int cnt = 0;

      while (i < changed_cells.size() && res == 0 && cmd_res )
      {
        cind = (CellIndex)changed_cells.get(i);
        val = getValueAt(cind.getRowIndex(), cind.getColIndex());
        res = fr.setTabData(getNum(), cind.getRowIndex() + 1, cind.getColIndex() + 1, 
          val, getFieldDescr(cind.getColIndex()).getSize());
        cmd_res = fr.isCmdOk();
        if (log.isDebugEnabled())
          log.debug("Tab[" + getNum() + "].[" + (cind.getRowIndex() + 1) +"].[" + (cind.getColIndex() + 1) + "] = " + val);
        if (res == 0 && cmd_res) cnt++;
        i++;
      }
      if (log.isDebugEnabled())
        log.debug("FrTable.saveToFr() Table[" + getNum() + "] records saved " + cnt);
    } else
      if (log.isDebugEnabled())
        log.debug("FrTable.saveToFr() Table[" + getNum() + "] no changed cells");

    changed_cells = null;
  }

  // чтения описаний полей
  public TabFieldDescr getFieldDescr(int colIndex)
  {
    return (TabFieldDescr)field_descr.get(colIndex);
  }
  

  // доступ к атрибутам
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    return ((Vector)rows.get(rowIndex)).get(columnIndex);
  }

  public void setValueAt(Object val, int rowIndex, int columnIndex)
  {
    if (val == null) return; // значение null не допустимо
    Object old_val = getValueAt(rowIndex, columnIndex);
    if (!val.equals(old_val))
    {
      ((Vector)rows.get(rowIndex)).set(columnIndex, val);
      if (changed_cells == null) changed_cells = new Vector();
      changed_cells.add(new CellIndex(rowIndex, columnIndex));
    }
  }

  private void prStr(String s)
  {
    if (log.isDebugEnabled()) log.debug(s);
  }

  public void printAll()
  {
    prStr("******************* TABLE STRUCTURE ****************");
    prStr("[" + getNum() + "] " + getName());
    prStr("rows = " + numRows + "  cols = " + numCols);
    prStr("{{{{{{{{{{{{{{{{{{{ columng definition:");
    TabFieldDescr d;
    for (int i = 0; i < field_descr.size(); i++)
    {
      d = (TabFieldDescr)field_descr.get(i);
      prStr("[" + d.getNum() + "] '" + d.getName() + 
        "' (" + (d.getType() == TabFieldDescr.BIN_TYPE ? "BIN" : "CHAR") + ")" +
        " size=" + d.getSize() + " min=" + d.getMinVal() + " max=" + d.getMaxVal());
    }
    prStr("}}}}}}}}}}}}}}}}}}} columng definition:");

    prStr("                    table data");

    Vector row;
    int i, j;
    for (i = 0; i < numRows; i++)
    {
      row = (Vector)rows.get(i);
      for (j = 0; j < numCols; j++)
      {
        prStr(row.get(j) + "\t");
      }
    }
    
    prStr("****************************************************");
  }

  // ********************************************************************
  private void initDataVectors()
  {
    field_descr = new Vector(numCols, 0);
    rows = new Vector(numRows, 0);
  }

  private void readTabStructure(FrDrv fr) throws FrException
  {
    int res;
    res = fr.getTabStructure(num);
    if (res == 0 && fr.isCmdOk())
    {
      byte[] data = fr.getReplyParams();
      name = th.cash.fr.FrUtil.strFromBytes(data, 0, data.length - 3);
      numRows = th.cash.fr.FrUtil.intFromBytes(data, data.length - 3, 2);
      numCols = th.cash.fr.FrUtil.intFromBytes(data, data.length - 1, 1);
    }
  }
  
  private void readFieldDescr(FrDrv fr) throws FrException
  {
    int res = 0;
    byte i = 1;
    int offs;
    boolean cmd_res = true;
    byte[] data;

    String dname;
    byte dtype;
    int dsize;
    int minv, maxv;

    TabFieldDescr descr;
    
    while ( i <= numCols && res == 0 && cmd_res)
    {
      res = fr.getFieldStructure(getNum(), i); // нумерация полей начинается с 1
      cmd_res = fr.isCmdOk();
      if (res == 0 && cmd_res)
      {
        data = fr.getReplyParams();
//        System.out.print(Hex.byteArrayToHexString(data));     
        offs = 0;
        dname = th.cash.fr.FrUtil.strFromBytes(data, offs, 40);
        offs += 40;
        dtype = th.cash.fr.FrUtil.byteFromBytes(data, offs);
        offs += 1;
        dsize = th.cash.fr.FrUtil.byteFromBytes(data, offs);
        offs += 1;
        minv = th.cash.fr.FrUtil.intFromBytes(data, offs, dsize);
        offs += dsize;
        maxv = th.cash.fr.FrUtil.intFromBytes(data, offs, dsize);

        descr = new TabFieldDescr(i, dname, dsize, dtype, minv, maxv);

        field_descr.add(descr);
      }
      i++;
    }
  }

  private void readTabData(FrDrv fr) throws FrException
  {
    Object val;

    int res = 0;
    boolean cmd_res = true;
    byte[] data;

    TabFieldDescr descr;

    Vector row;
    int i = 1;
    int j = 1;


    while ( i <= numRows && res == 0 && cmd_res)
    {
      j = 1;
      row = new Vector(numCols);
      while ( j <= numCols && res == 0 && cmd_res)
      {
        res = fr.getTabData(num, i, j);
        cmd_res = fr.isCmdOk();
        if (res == 0 && cmd_res)
        {
          data = fr.getReplyParams();
          descr = getFieldDescr(j - 1);

          if (descr.getType() == descr.BIN_TYPE)
             val = new Integer(th.cash.fr.FrUtil.intFromBytes(data, 0, descr.getSize()));
            else
             val = th.cash.fr.FrUtil.strFromBytes(data, 0, descr.getSize());

          row.add(val);
        }
        
        j++;
      }
      rows.add(row);
      i++;
    }
  }
  
}