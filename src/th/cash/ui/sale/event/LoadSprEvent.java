package th.cash.ui.sale.event;
import java.util.EventObject;
import java.util.Date;

public class LoadSprEvent extends EventObject
{
  public LoadSprEvent(Date ld, int cnt)
  {
    super(ld);
  }
}