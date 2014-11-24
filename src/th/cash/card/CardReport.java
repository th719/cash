package th.cash.card;

import java.util.Properties;

public interface CardReport 
{

  public void setParams(Properties cfg);

  public int getTransCount();

  public boolean processReport();

  // что еще ?
  
}