package th.cash.ui.paym;

import java.io.FileFilter;
import java.io.File;

public class SPFilter implements FileFilter
{
  private String pref, suf;
  
  public SPFilter(String pref, String suf)
  {
    this.pref = pref;
    this.suf = suf;
  }

  public boolean accept(File f)
  {
    String fn = f.getName();
    return fn.startsWith(pref) && fn.endsWith(suf);
  }

}