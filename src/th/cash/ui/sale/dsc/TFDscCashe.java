package th.cash.ui.sale.dsc;

import java.util.Vector;

import th.common.db.VectorDataCashe;
import th.common.db.VRow;
import th.cash.model.FixedDsc;

public class TFDscCashe extends VectorDataCashe
{

    public final static int NAME      = 0;
    public final static int VALUE     = 1;
    public final static int KIND      = 2;
    // hidden data
    public final static int FDSC      = 3;
    
    public final static int NUM_COLS  = 5;

  public TFDscCashe()
  {
    super(5, 5);
  }

  public void initRowByDsc(VRow row, FixedDsc fd)
  {
    row.set(NAME, fd.getName());
    row.set(VALUE, fd.getDscValue());
    row.set(KIND, fd.getIsSum() != null && fd.getIsSum().booleanValue() ? "руб" : "%" );
    row.set(FDSC, fd);
  }

  public void refresh(Vector v) 
  {
    int i = 0;
    clear();
    if (v != null)
    {
      VRow row;
      for(i = 0; i < v.size(); i++)
      {
        row = createNewRow(NUM_COLS);
        initRowByDsc(row, (FixedDsc)v.get(i));
        add(row);
      }
    }
  }
  
  
}