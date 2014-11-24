package th.cash.ui.setup;
import java.util.Properties;

// сохраненные настройки ФР
public class FrSetPanel extends TabSetPanel
{
  public FrSetPanel()
  {
    init();
  }

  private void init()
  {
    
  }

  public boolean isDataChanged()
  {
    return false;
  }

  public void refreshData(Properties p)
  {
  }

  public String saveData(Properties p)
  {
    return null;
  }
}