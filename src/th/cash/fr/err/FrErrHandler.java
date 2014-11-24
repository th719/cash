package th.cash.fr.err;


import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Date;
import java.text.SimpleDateFormat;

import th.cash.dev.FiscalPrinter;
import th.cash.fr.*;
import th.cash.fr.doc.*;
import th.cash.fr.state.*;

import th.cash.env.KKMEnv;
import th.cash.model.Settings;

import th.cash.ui.sale.ProcessShow;
import th.cash.ui.sale.UserException;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
// ����� ��� ��������� ������ ����������� ������������
// �������� � ��������� �����, ��������� ������ � ��������� ���������� 
// ��� ������ ����� ���������
public abstract class FrErrHandler 
{
  private StateA state = null;
  private FrException last_exc = null;
  private int init_cnt = 0;
  private boolean use_weight_sensors = false;

  private KKMEnv env;
  // ui element
  private ProcessShow psh;


  public void initFiscalPrinterDevice(FiscalPrinter fp) throws Exception
  {
    try
    {
      
      psh.setTitle("������������� ��");
      psh.setText("����� ����������...");
      psh.show();
      
      fp.init();

      // �������������� ������ ���������  
      state = new FullStateFr();
      state.decodeParams(fp.getReply());
      state.printAll();
      
      fireStateReceived(new StateFrEvent(state));

      init_cnt++;

    } finally
    {
      psh.hide();
    }
  }

  // �������� ������� �������������
  public void resetInitCnt() { init_cnt = 0; }

  // ����� ������ ������������� ����� ������ ��� ������ (����������� ���������)
  public void initFRTables(FiscalPrinter fp) throws FrException
  {
    if (init_cnt == 1 && state != null && !inArr(A_NOT_SET_TABLES, state.getModeNum()))
    {
      psh.setText("��������� ������...");
      initFiscalPrinterParams(fp);
    }
  }

  // ���������������� ������ �� (������, ����� � ����) (���� ���� ����������� ������ � db cash)
  private void initFiscalPrinterParams(FiscalPrinter fp) throws FrException
  {
    FrKPrinter pr = (FrKPrinter)fp;

    Settings set = env.getSprModel().getSettings();

    if (getLog().isDebugEnabled())
      getLog().debug("*** Program fr tables ...");
    _copyPropToFp(pr, set, set.getFrModes(), Integer.class, 1);

    _copyPropToFp(pr, set, set.getFrCheckText(), String.class, 40);
  }

  // ���������������� ����� �� ������
  private void _copyPropToFp(FrKPrinter fp, Settings set, Properties pr, Class data_class, int sz) throws FrException
  {
    Enumeration keys;

    keys = pr.keys();

    String key, prop;
    Object val;
    int[] tidx;

//    StrFormat sfmt = null;
//    if (data_class.equals(String.class)) sfmt = new StrFormat();
    
    while (keys.hasMoreElements())
    {
      key = keys.nextElement().toString();
      prop = pr.getProperty(key);

      if (data_class.equals(Integer.class))
        val = new Integer(prop);
      else
        val = prop;

//        val = sfmt.getCenterString(prop, ' ', sz); // ����� � ����(�� ������)
        
      tidx = set.getTabRowCol(key);

      if (getLog().isDebugEnabled()) 
        getLog().debug("*** Set tab[" + tidx[0] + "]." + tidx[1] + "." + tidx[2] + " = " + val);

      fp.setTabCell(tidx[0], tidx[1], tidx[2], val, sz);
    }
  }



  private final static int[] NORMAL_MODES = new int[] {2, 3, 4};
  private final static int[] FIXABLE_MODES = new int[] {8, 6, 10};

  // ����� �������� ��������� ��������� ����� ������� ����������� ������
  private final static long CPRINT_TIMEOUT = 500;

  // �������� � ������������� ��������� ��. ���� ���������� ������ ����������� - 
  // �� ���������� ������ � ��������� ������� (����� ����� ����� �����������)
  // ����������  ����� ��������� ������������� ���������� (SalePanel.do_open())
  // TODO - �������� ��������� �������������� �� ������ WARN
  public String checkAndFixState(FiscalPrinter fp) throws Exception
  {
    String res = null;   // ��������� � ������������ ���������
    boolean err = false; // ������ � �������� ��������� ���������
    int fm;              // ����� ������

    do
    {
      try
      {
        fm = state.getModeNum();
        if (inArr(NORMAL_MODES, fm) && state.getSubModeFr() != 3) // ???
        // TODO   ����� ���������� ������� ���� ��� � ������������ ��������� 
        // ����� ������� ����� ���������� ������ ���������
        {
          if (!err && getLog().isDebugEnabled())
            getLog().debug("Initial state Ok: " + state.getModeNumStr());

          err = false;

          if (!err && fm == 4)
          {
            res = timeSynchronization(fp, env.getSprModel().getSettings().getTimeDifSec()); // �������������� ������������� ������� ��� �������� �����
            err = res != null;
          }
        } else
        {
          if (!err)   
            getLog().warn("Incorrect state: [" + state.getModeNum() + ',' + state.getSubModeFr() +"] " + 
                                                 state.getModeNumStr() + ',' + state.getSubModeFrStr());

          if (inArr(FIXABLE_MODES, fm))
          {
            switch (fm)
            {
              case 8 : getLog().warn("Cancel check ..."); fp.calcelCheck(); break;
              case 6 : getLog().warn("Time confirmation ..."); timeSynchronization(fp, env.getSprModel().getSettings().getTimeDifSec()); break;
              case 10: getLog().warn("Interrupt test ..."); fp.interruptTest();  break;
            }
            getLog().warn("Ok");

            stateRequest(fp);

          } else
          if (state.getSubModeFr() == 3 && inArr(new int[]{2, 3, 4, 12}, fm))
          {
            getLog().warn("Continue printing ..."); 
            fp.continuePrint();
            Thread.currentThread().sleep(CPRINT_TIMEOUT);
            getLog().warn("Ok");
            stateRequest(fp);
          } else
          {
            res = "������������ ��������� ����������� ������������:\n" + state.getModeNumStr();
            getLog().error("NOT FIXED!!! :" + res);
          }

          err = false;        
          
        }
  
        
      } catch (FrException ex)
      {
        // �������������� ������ ���������� ������
        if (inArr(FR_NO_PAPER_ERR, ex.getErrorCode()))
        {
          err = true;
          showError(noPaperText(ex/*last_exc*/.getMessage())); // 26.08.11
          stateRequest(fp);
        } 
        else
        // � ����������� ������ ... ����� �������� �����
        if (FrErrors.ERR_FR_WAIT_CONTINUE_CMD == ex.getErrorCode())
        {
          err = true;
          getLog().error(ex.getMessage());
          fp.continuePrint();
          getLog().warn("CMD - continue printing");
          Thread.currentThread().sleep(CPRINT_TIMEOUT);
          stateRequest(fp); // ��������� ������ ���������
        }
        else throw ex;
        
      }

    } while (err && res == null);

    return res;    
  }
  
  
  

  // ��� ������ ���� ����� ������ :-)
//  private void cancelCheckRollControl(FiscalPrinter fp) throws Exception
//  {
//    boolean err = false;
//    do
//    { 
//      try
//      {
//        fp.calcelCheck(); 
//        err = false;
//      } catch (FrException ex)
//      {
//        if (inArr(FR_NO_PAPER_ERR, ex.getErrorCode()))
//        {
//          err = true;
//          showError(noPaperText(last_exc.getMessage()));
//        } 
//        else
//        if (FrErrors.ERR_FR_WAIT_CONTINUE_CMD == ex.getErrorCode())
//        {
//          err = true;
//          fp.continuePrint();
//        }
//        else throw ex;
//      }
//    } while (err);
//  }

  //  *************************************************************************

    // �������� ��������� �� ����� ��������� ����������� ���������
    public void checkStateBeforeClosing(FiscalPrinter fp, FDoc doc) throws Exception
    {
      state = stateRequest(fp);  

      Date cur_date = new Date();

      if (checkTime((FullStateFr)state, cur_date, env.getSprModel().getSettings().getWorkLocalDif()))
        throw new UserException("��������� ����������� ������� �� � ��!\n" + createDateErrMsg((FullStateFr)state, cur_date));
      
      if (env == null ? use_weight_sensors : env.getSprModel().getSettings().isUseWeightSensors())
        checkWeightSensors(state);
      else
        checkOpticalSensors(state);

      if (doc instanceof ZReport) checkModeAvail(A_Z_MODES, state, doc); else
      if (doc instanceof XReport) checkModeAvail(A_X_MODES, state, doc); else
      if (doc instanceof Pay) checkModeAvail(A_PAY_MODES, state, doc); else
      if (doc instanceof Check) {
        if (state.getModeNum() == 3)
          throw new UserException(state.getModeNumStr() + ".\n" + "�������� ����� - ������� Z-����� !!!");
        else
          checkModeAvail(A_CHECK_MODES, state, doc);
      }
    }

    private boolean checkTime(FullStateFr st, Date cur_date, int max_dif) 
    {
      if (cur_date == null)  cur_date = new Date();
      
      long dif = Math.abs(cur_date.getTime() - st.getCurDate().getTime()) / 1000;

      return dif > max_dif;
    }

    private final static String ET_NO_CHECK_ROLL = "����������� ������� �����!";
    private final static String ET_NO_LOG_ROLL = "����������� ���������� �����!";

    
    private String _checkWeightSensors(StateA st) 
    {
      StringBuffer sb = new StringBuffer();
      if (!st.isFFrRollCheck()) sb.append(ET_NO_CHECK_ROLL);
      if (!st.isFFrRollLog()) 
      {
        if (sb.length() > 0) sb.append('\n');
        sb.append(ET_NO_LOG_ROLL);
      }
      return sb.length() > 0 ? sb.toString() : null;
    }

    private void checkWeightSensors(StateA st) throws UserException
    {
      String s = _checkWeightSensors(st);
      if (s != null) throw new UserException(s);
    }

    private String _checkOpticalSensors(StateA st)
    {
      StringBuffer sb = new StringBuffer();
      if (!st.isFFrOptSensorCheck()) sb.append(ET_NO_CHECK_ROLL);
      if (!st.isFFrOptSensorLog()) 
      {
        if (sb.length() > 0) sb.append('\n');
        sb.append(ET_NO_LOG_ROLL);
      }
      return sb.length() > 0 ? sb.toString() : null;
    }

    private void checkOpticalSensors(StateA st) throws UserException
    {
      String s = _checkOpticalSensors(st);
      if (s != null) throw new UserException(s);
    }

    private void checkModeAvail(int[] am, StateA st, FDoc doc) throws UserException
    {
      if (!inArr(am, st.getModeNum()))
        throw new UserException("�������� \'" + doc.getTypeName() + "\' ���������� � ������ ������:\n" + state.getModeNumStr());
    }

    // inner constants
    private final static int[] FR_NO_PAPER_ERR = new int[] {FrErrors.ERR_NO_CHECK_ROLL, FrErrors.ERR_NO_LOG_ROLL};
    private final static int[] A_NOT_SET_TABLES = new int[] {1,8};
    private final static int[] A_Z_MODES = new int[] {2,3};
    private final static int[] A_X_MODES = new int[] {2,3,4,};
    private final static int[] A_PAY_MODES = new int[] {2,3,4,7,9};
    private final static int[] A_CHECK_MODES = new int[] {2, 4, 7, 8, 9};

    private transient ArrayList stateFrListeners = new ArrayList(2);
    
    private boolean inArr(int[] arr, int val)
    {
      int i = 0;
      int len = arr.length;
      while (i < len && arr[i] != val) i++;
      return i < len;
    }
  

  private String noPaperText(String err_text)
  {
    return "������ �� : " + err_text + "!\n�������� ������ � ������� <������> ��� �����������!";
  }

  private StateA stateRequest(FiscalPrinter fp) throws Exception
  {
    state = fp.stateRequest();
    fireStateReceived(new StateFrEvent(state)); // ���������� � ����� ���������
    return state;
  }

  // ��������� ������ �������� ���������
  // �������� - ������ ������ (����������� ���������), ����� � ����������� ... 
  
  public boolean handlePrintError(FiscalPrinter pr, FDoc doc, FrException ex)
  {
    last_exc = ex;
    boolean res = false; // ��������� ��������� ������ (������� / ���)
    boolean lflag = true; // ������� ����������� ����� ��������� ������
    int code;

    psh.hide();

    while (lflag)
    {
      try
      {
        code = last_exc.getErrorCode();
        //getLog().error("[" + code + "] " + last_exc.getMessage(), last_exc);
        
        switch (code)
        {
          // ----------------------------------------------------------------
          // � ����������� �� ���� ������ ��������������� ��������� ��������:

          // ����������� ������� ��� ����������� �����
          case FrErrors.ERR_NO_CHECK_ROLL:
          case FrErrors.ERR_NO_LOG_ROLL :

            // ��� ����� - ����������� ������ � ��������� ������ ��������� � ����� ���������� ...
            showError(noPaperText(last_exc.getMessage()), last_exc);
            state = stateRequest(pr);
            
            break;

          // ������ ����� � �����������
          case FrErrors.ERR_DEV_NOT_FINDED :
          case FrErrors.ERR_TIMEOUT_READ :
          case FrErrors.ERR_IO_STREAM :
          case FrErrors.ERR_READ_DATA :
          case FrErrors.ERR_READ_CMDC :

            showError("������ �� : ��� ����� � �����������!", last_exc);
            if (showQuestion("��������� ������� ����������� ��?"))
            {
              initFiscalPrinterDevice(pr);
            } else
            {
              res = false; lflag = false; // ����� - ������ �� ����������
            }
            break;

          // �� ������� ���������� � �����   
          case FrErrors.ERR_NO_SUCH_NAL_MONEY :
          
            if (doc instanceof ReturnCheck)
            {
              showError(ex);

              if ( showQuestion("�������� ��� �������� ?") )
              {
                ((ReturnCheck)doc).cancel(env.getCurrentUser());
                pr.calcelCheck();
                res = true;
              }
            } 
            lflag = false;

            break;

          // �������� ������� ����������� ������
          case FrErrors.ERR_FR_WAIT_CONTINUE_CMD :
            showError(last_exc);
            pr.continuePrint();
            Thread.currentThread().sleep(CPRINT_TIMEOUT);
            break;

          // ���� ������ ���������� �������
          case FrErrors.ERR_PRINTING_PROCESS :
            showError(last_exc);
            break;

          default : res = false; lflag = false; 
        }

        // ����������� ��������� ������
        if (lflag)
        {
          stateRequest(pr); // added 28.01.2009
          // ���� �� ������� ������� ����������� ������ 
          if (state.getSubModeFr() == (byte)3) 
          {
            pr.continuePrint();
            Thread.currentThread().sleep(CPRINT_TIMEOUT);
          }
          // ���� ���� ������������� �������� - ������������
          if (doc != null) doc.print(pr); 
          res = true; lflag = false;
        }

      } catch (FrException ex2)
      {
        last_exc = ex2;
      } catch (Exception ex333)
      {
        // hiden
        getLog().error(ex333.getMessage(), ex333);
      }
    }

    if (res) last_exc = null;
    return res;
  }





  // ������ ��������� � ���������� ������������ ���������� ��
  // changed 29.01.2009
  // changed 04.08.2011
  public void printDoc(FiscalPrinter fp, FDoc doc) throws Exception
  {
    try
    { // *** ���������� ������ ***
      doc.print(fp);

      // ��� ������� ������ �����, ������ ������� � ����� ��������� 
      if (doc instanceof Report)
      {
        Thread.currentThread().sleep(CPRINT_TIMEOUT); 

        // ����� ������ ���������, ��������� ��������� (��������)
      
        int submode;
        do {

          stateRequest(fp);

          submode = state.getSubModeFr();

          switch (submode)
          {
            case StateConst.SM_PAPER_OK : break;

            // ���� ��� ������ - ��������� ������� ������� � ����������� �����            
            case StateConst.SM_ACTIV_NO_PAPER : case StateConst.SM_PASS_NO_PAPER : 
              String s = _checkOpticalSensors(state);
              if (s != null)  showError(noPaperText(s));
            break;

            // ���� ���������� ���� ������� ����������� ������ -
            case StateConst.SM_WAIT_REPEAT :          
              fp.continuePrint(); 
              Thread.currentThread().sleep(CPRINT_TIMEOUT); 
            break;

            // �� ��������� ��������� - �������� � ��������� ������ ���������
            default :  
              Thread.currentThread().sleep(CPRINT_TIMEOUT);
            /*
            case 4 : case 5 :  // ���� ���� ������, �� ����, ���� ...
              Thread.currentThread().sleep(CPRINT_TIMEOUT);
            break;
            */
          }
              
        } while (submode != StateConst.SM_PAPER_OK );  // added 04.08.11, 

        // ������������� �������� ��� ���������� ������� 
        Thread.currentThread().sleep(2 * CPRINT_TIMEOUT);  
        stateRequest(fp); // ��������� ������� ���������
      }      
            
    } catch (FrException ex)
    {
      if (!handlePrintError(fp, doc, ex)) throw getLastException();
    } 

  }


  // ���������� ������� ����� �� �����������
  // 26.01.2009 ��������� ����������� ������ �� ���� �� ������ ��������� �����������
  // ������ �������������� ������ ������
  // ��������� - ���� null, ��������� ������� �������, not null - ����� ��������� �� ������
  public String timeSynchronization(FiscalPrinter fp, int time_dif)
  {
    String emsg = null;
    Date cur_date = new Date();
    try
    { 
//      long dif = 0;
//
//      if (state != null &&  state instanceof FullStateFr)
//        dif = Math.abs(cur_date.getTime() - ((FullStateFr)state).getCurDate().getTime()) / 1000;
//
//      if (dif > time_dif)
//      {
//        fp.setDataTime(cur_date, state);  
//        getLog().info("Time syncronisation (" + cur_date +") Ok");
//      } else
//        getLog().info("No syncronisation, dif " + dif);

      if (state != null &&  state instanceof FullStateFr)
      {
        if (checkTime((FullStateFr)state, cur_date, time_dif))
        {
          fp.setDataTime(cur_date, state);  
          getLog().info("Time synchronization (" + cur_date +") Ok");
        } else
          getLog().info("No synchronization");
      }

    } catch (Exception ex)
    {
      getLog().error("Time syncronisation Error", ex); // �������� � ������

      // rem uved 12.04.10
//      emsg = "������ ������������� ������� ����������� ������������.\n" +
//      "�������: '" + ex.getMessage() + "'\n";
      // ���� ������ � ����������� ������� (����� ������� �� �����)
      if (ex instanceof FrException && 
        ((FrException)ex).getErrorCode() == FrErrors.ERR_CURDATE_LESS_DATE_FM);
      {
        // added here 12.04.10
        emsg = "������ ������������� ������� ����������� ������������.\n" +
              "�������: '" + ex.getMessage() + "'\n" + 
               createDateErrMsg((FullStateFr)state, cur_date);
      }    
//      {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//        emsg = emsg + "���� ��: " + sdf.format(cur_date) + "   ���� ��: " + sdf.format(((FullStateFr)state).getCurDate()) + "\n"+
//        "������������� ���������. ���� ������ ���������� -\n���������� � ������ ����������� ���������";
//      }

      //showError(emsg);
    }
    return emsg;
  }

  private String createDateErrMsg(FullStateFr st, Date cur_date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm");
    return "���� ��: " + sdf.format(cur_date) + "   ���� ��: " + sdf.format(((FullStateFr)state).getCurDate()) + "\n"+
     "������������� ���������. ���� ������ ���������� -\n���������� � ������ ����������� ���������";
  }




  // ************************************************************************** 
  // get / set
  public void setEnv(KKMEnv e) { env = e; }

  public void setProgress(ProcessShow p) { psh = p; } 

  public StateA getFrState() { return state; }

  public FrException getLastException() { return last_exc; }
  
  // ����� UI ����������, ��� ��������� ������
//  public abstract void showMessage(String msg);

  public abstract void showError(String msg);

  public abstract void showError(Exception ex);

  public abstract void showError(String msg, Exception ex);

  public abstract boolean showQuestion(String msg);

  public abstract Logger getLog();


  // events for state listeners
  public synchronized void addStateFrListener(StateFrListener l)
  {
    if (!stateFrListeners.contains(l))
    {
      stateFrListeners.add(l);
    }
  }

  public synchronized void removeStateFrListener(StateFrListener l)
  {
    stateFrListeners.remove(l);
  }

  protected void fireStateReceived(StateFrEvent e)
  {
    List listeners = (List)stateFrListeners.clone();
    int count = listeners.size();

    for (int i = 0;i < count;i++)
    {
      ((StateFrListener)listeners.get(i)).stateReceived(e);
    }
  }

  
}
