package th.cash.ui.setup;

import javax.swing.JPanel;
import java.util.Properties;

import th.cash.env.KKMEnv;

public abstract class TabSetPanel extends JPanel
{

//  public abstract void refreshData(Properties p);

  public abstract void refreshData(Properties p);

  public abstract boolean isDataChanged();

  // true if has changes
  public abstract String saveData(Properties p);

}