package th.cash.ui.setup;



import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import th.cash.fr.table.FrTable;
import th.cash.fr.table.TabFieldDescr;

public class FrSetTableModel extends DefaultTableModel//implements TableModel 
{

  private FrTable src_tab = null; // исходные данные
  private boolean rotated = false; // признак поворота таблицы (для удобства редактирования)
  private boolean editable = true;

  public FrSetTableModel()
  {
    this(null);
  }

  public FrSetTableModel(FrTable src)
  {
    setSrcTable(src);
  }

  public void setSrcTable(FrTable src)
  {
    src_tab = src;
    rotated = src == null ? false : src.getNum() == (byte)1 || src.getNum() == (byte)8;
    fireTableStructureChanged();
  }

  public int getRowCount()
  {
    if (src_tab == null) return 0;
    return rotated ? src_tab.getNumCols() : src_tab.getNumRows();
  }

  public int getColumnCount()
  {
    if (src_tab == null) return 0;
    return 1 + (rotated ? src_tab.getNumRows() + 1 : src_tab.getNumCols());
  }

  public String getColumnName(int columnIndex)
  {
    if (columnIndex == 0) return "N"; else
    if (rotated)
    {
      if (columnIndex == 1) return "Назначение"; else return "Значение " + String.valueOf(columnIndex - 1);
    } else
    {
      return src_tab.getFieldDescr(columnIndex - 1).getName();
    }
  }

  public Class getColumnClass(int columnIndex)
  {
    if (columnIndex == 0) return Integer.class; else
    if (rotated)
    {
      if (columnIndex == 1) return String.class; else return Integer.class;
    } else
    {
      return src_tab.getFieldDescr(columnIndex - 1).isBinType() ? Integer.class : String.class;
    }
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return editable && (rotated ? columnIndex > 1 : columnIndex > 0);
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if (columnIndex == 0) return new Integer(rowIndex + 1);
    if (rotated)
    {
      if (columnIndex == 1)
        return src_tab.getFieldDescr(rowIndex).getName();
      else
        return src_tab.getValueAt(columnIndex - 2, rowIndex);
    } else
    {
      return src_tab.getValueAt(rowIndex, columnIndex - 1);
    }
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    String s = checkValue(aValue, rowIndex, columnIndex);
    if (s != null) throw new RuntimeException(s);
    if (columnIndex == 0) return;
    if (rotated)
    {
      if (columnIndex == 1)
        return;
      else
        src_tab.setValueAt(aValue, columnIndex - 2, rowIndex);
    } else
    {
      src_tab.setValueAt(aValue, rowIndex, columnIndex - 1);
    }
  }

  // проверка корректности устанавливаемого значения
  public String checkValue(Object aValue, int rowIndex, int columnIndex)
  {
    String res = null;
    TabFieldDescr descr = null;

    if (columnIndex > 0)
    {
      if (rotated)
      {
        if (columnIndex > 1)
          descr = src_tab.getFieldDescr(rowIndex);
      } else
      {
        descr = src_tab.getFieldDescr(columnIndex - 1);
      }
    }
    if (descr != null)
    {
      if (descr.isBinType())
      {
        if (aValue == null) res = "Значение параметра отсутствует"; else
        {
          Integer ival = (Integer)aValue;
          int i = ival.intValue();
          if (i < descr.getMinVal() || i > descr.getMaxVal())
            res = "Значение '" + i + "' вне допустимого диапазона [" + descr.getMinVal() + ".." + descr.getMaxVal() + "]";
        }
      } else
      {
        if (aValue != null)
        {
          if (((String)aValue).length() > descr.getSize())
            res = "Длина строки '" + (String)aValue + "'\n" + " превышена. (Максимум " + descr.getSize() +" симв.)";
        }
      }
    }
      
    return res;
  }

//  public void addTableModelListener(TableModelListener l)
//  {
//  }
//
//  public void removeTableModelListener(TableModelListener l)
//  {
//  }
}