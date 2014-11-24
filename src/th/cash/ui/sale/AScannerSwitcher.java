package th.cash.ui.sale;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import jpos.JposException;
import jpos.Scanner;

import jpos.events.DataListener;

import org.apache.log4j.Logger;


public class AScannerSwitcher extends WindowAdapter 
{
  private DataListener sw_listener;
  private Scanner scanner;

  protected final static String LOG_PREF = "UI"; 
  private Logger log = Logger.getLogger(LOG_PREF + '.' + this.getClass().getName());
  
  public AScannerSwitcher(Scanner s, DataListener lsnr)
  {
    scanner = s;
    sw_listener = lsnr;
  }

//  public boolean setScannerDataEnabled(boolean b)
//  {
//    boolean res = true;
//    try
//    {
//      synchronized (scanner) { if (scanner != null) scanner.setDataEventEnabled(b); }
//    } catch (JposException ex)
//    {
//      LogUtil.error(ex);
//      res = false;
//    }
//    return res;
//  }

  public boolean setScannerEnabled(boolean b)
  {
    if (scanner == null) return false;
    boolean res = true;
    try
    {
      synchronized (scanner) { scanner.setDeviceEnabled(b); }
      if (log.isDebugEnabled())
        log.debug("AScannerSwitcher.setScannerEnabled(" + b + ")");       
    } catch (JposException ex)
    {
      log.error(ex.getMessage(), ex);
      res = false;
    }
    return res;
  }

  public void addDataListener()
  {
    if (scanner == null) return;
    synchronized (scanner)
    {
      scanner.addDataListener(sw_listener);

      if (log.isDebugEnabled())
        log.debug("AScannerSwitcher.addDataListener(" + /*sw_listener +*/ ")");
    }
  }

  public void removeDataListener()
  {
    if (scanner == null) return;
    synchronized (scanner)
    {
      scanner.removeDataListener(sw_listener);

      if (log.isDebugEnabled())
        log.debug("AScannerSwitcher.removeDataListener(" + /*sw_listener +*/ ")");
    }
  }

//  public void removeDataListener()
//  {
//    synchronized (scanner)
//    {
//      if (scanner != null) {
//        scanner.removeDataListener(sw_listener);
//
//        if (log.isDebugEnabled())
//          log.debug("AScannerSwitcher.removeDataListener(" + /*sw_listener +*/ ")");
//      }
//    }
//  }

//  public void enable()
//  {
//    try
//    {
//      synchronized (scanner) { scanner.setDataEventEnabled(true); }    
//    } catch (JposException ex)
//    {
//      log.error(ex.getMessage(), ex);
//    }
//  }
//
//  public void disable()
//  {
//    try
//    {
//      synchronized (scanner)  { scanner.setDataEventEnabled(false); }
//    } catch (JposException ex)
//    {
//      log.error(ex.getMessage(), ex);
//    }
//  }

  public void windowActivated(WindowEvent e)
  {
    addDataListener();
    log.debug("AScannerSwitcher.windowActivated()");
  }

  public void windowDeactivated(WindowEvent e)
  {
    removeDataListener();
    log.debug("AScannerSwitcher.windowDeactivated()");
  }
  
  
}