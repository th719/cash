package th.cash.dev;

import java.io.IOException;

public interface CustDisplay 
{
//  public CustDisplay(String port_name, int timeout);

  public void setText(boolean clear, String s1, String s2);

  public void close() throws IOException;

  public int getStrLen();
}