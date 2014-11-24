package th.cash.ui.sale.event;

import java.util.EventObject;
import java.util.Date;

public class SaveRepEvent extends EventObject
{
  public SaveRepEvent(Date rd, int pos_cnt)
  {
    super(rd);
  }
}