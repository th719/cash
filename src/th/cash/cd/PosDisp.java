package th.cash.cd;
import java.io.IOException;
import th.cash.dev.CustDisplay;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.comm.UnsupportedCommOperationException;
import javax.comm.CommPortIdentifier;
import javax.comm.CommPort;
import javax.comm.SerialPort;

import java.text.SimpleDateFormat;
import java.util.Date;

// "драйвер" Pos дисплея, Firich, PosLab ...

// 12.11.2010 доработка ... переделать под конкретный протокол  CD5220 
public class PosDisp extends Thread implements CustDisplay 
{
  //
  private final static byte CLEAR_CMD = 0x0C;

  private final static byte OWWR_MODE = 0x11;

  private final static byte BRT_SET = 0x2A;
  
  private final static byte FILL_BYTE = 0x20;
  private final static byte ESC_BYTE = 0x1B;
  private final static byte TEXT_MODE = 0x51;
  private final static byte LINE1_BYTE = 0x41;
  private final static byte LINE2_BYTE = 0x42;
  private final static byte FIN_BYTE = 0x0D;
  
  private final static String DEV_ENC = "Cp866";
  private final static int STR_LEN = 20;
  private final static long SLEEP_TIME = 300; //ms

  // port
  private SerialPort serial_port = null; 

  // COMM port streams
  private InputStream  port_is = null;
  private OutputStream port_os = null;

  private byte[] buf;
  private boolean clear = true;
  private boolean cont = true;
  private boolean data_set = false;
  

  public PosDisp(String port_name, int speed, int timeout) throws Exception
  {
    CommPortIdentifier cpid = CommPortIdentifier.getPortIdentifier(port_name);

    if (cpid.isCurrentlyOwned()) 
      throw new IOException("Port is currently in use "+port_name);

    CommPort cp = cpid.open("PosDisp", timeout);

    if (cp instanceof SerialPort) serial_port = (SerialPort)cp;
      else throw new Exception("Запрошенное устройство не является последовательным портом");

    serial_port.setSerialPortParams(
      speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

    port_is = serial_port.getInputStream();
    port_os = serial_port.getOutputStream();

//    port_os.write(OWWR_MODE);
//    port_os

    buf = new byte[2 * STR_LEN];
  }

//
//  private void sendBuf() throws IOException
//  {
//    boolean refresh;
//    byte[] cbuf;
//
//    synchronized (buf)
//    {
//      if (!data_set) return; //!!! выход, если данные уже отображены
//      cbuf = new byte[2 * STR_LEN];
//      System.arraycopy(buf, 0, cbuf, 0, 2 * STR_LEN);
//    }
//
//    if (clear) port_os.write(CLEAR_CMD);
//    port_os.write(cbuf);
//
//    synchronized (buf)
//    {
//      data_set = false;
//    }
//  }

  private void sendBuf() throws IOException
  {
    boolean refresh;
    byte[] cbuf;

    synchronized (buf)
    {
      if (!data_set) return; //!!! выход, если данные уже отображены
      cbuf = new byte[2 * (STR_LEN + 4)];

      cbuf[0] = ESC_BYTE;
      cbuf[1] = TEXT_MODE;
      cbuf[2] = LINE1_BYTE;
      System.arraycopy(buf, 0, cbuf, 3, STR_LEN); // первую строку
      cbuf[STR_LEN + 3] = FIN_BYTE;

      // line 2
      cbuf[STR_LEN + 4] = ESC_BYTE;
      cbuf[STR_LEN + 5] = TEXT_MODE;
      cbuf[STR_LEN + 6] = LINE2_BYTE;
      System.arraycopy(buf, STR_LEN, cbuf, STR_LEN + 7, STR_LEN); // первую строку
      cbuf[2 * STR_LEN + 7] = FIN_BYTE;
      
    }

    if (clear) port_os.write(CLEAR_CMD);
    port_os.write(cbuf);
//    System.out.println("data sended");

    synchronized (buf)
    {
      data_set = false;
    }
  }
  
  private void addStrToBuf(String s, int offs, int len)
  {
    byte[] bs;
    try {
      bs = s.getBytes(DEV_ENC);
    } catch (UnsupportedEncodingException ex)
    {
      bs = "Unknown encoding".getBytes();
    }
    int i = 0;
    while (i < bs.length) buf[offs + i] = bs[i++];
    while (i < len) buf[offs + i++] = FILL_BYTE;
  }

  public void setText(boolean clr_scr, String s1, String s2)
  {
    synchronized (buf)
    {
      addStrToBuf(s1, 0, STR_LEN);
      addStrToBuf(s2, STR_LEN, STR_LEN);
//      this.clear = clear;
      data_set = true;
      //cont = true;
      this.clear = clr_scr;
    }
  }

  public void run()
  {
    try
    {
      boolean refresh;
      while (cont) 
      {
//        synchronized (data_set) { refresh = data_set; }

//        if (refresh)
//        {
          try { sendBuf(); /*cont = false; 18.01.11 */} catch (IOException ex) {ex.printStackTrace(); /*added*/}
//        }
//        System.out.println("waiting ...");
        sleep(SLEEP_TIME);
      }
    } catch (InterruptedException iex)
    {
      cont = false;
    }
  }


  public void close() throws IOException
  {
    cont = false;
    interrupt();
    serial_port.close();
  }

  public int getStrLen()
  {
    return STR_LEN;
  }

// -Dgnu.io.rxtx.SerialPorts=/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:/dev/ttyUSB0:/dev/ttyUSB1
  public static void main(String[] args)
  {
    PosDisp pd = null;
    try
    {
      String port = "/dev/ttyS2";
//      String port = "/dev/ttyS0";
      pd = new PosDisp(port, 9600, 2000);
 
      SimpleDateFormat sdfm = new SimpleDateFormat("HH:mm:ss");
      
      SimpleDateFormat sdfy = new SimpleDateFormat("dd MMMMMM yyyy");

      String s1, s2;
      Date d;

      s1 = sdfy.format(new Date());
      s2 = "";

      pd.start();    
      pd.setText(true, s1, s2);
//      pd.run();

      while (true)
      {
        d = new Date(); 
//        s1 = sdfy.format(d);
        s2 = sdfm.format(d);
        pd.setText(false, s1, s2);
//        pd.run();
        System.out.println("CT:" + s1 + ' ' + s2);
        sleep(1000);
      }
      
        
      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally 
    {
      if (pd != null) try {  pd.close();} catch (IOException ioe) { ioe.printStackTrace(); }
    }
  }
}