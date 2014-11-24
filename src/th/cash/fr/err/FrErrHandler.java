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
// класс для обработки ошибок фискального регистратора
// вынесено в отдельный класс, поскольку ошибки и параметры специфичны 
// для данной серии устройств
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
      
      psh.setTitle("Инициализация ФР");
      psh.setText("Поиск устройства...");
      psh.show();
      
      fp.init();

      // инициализируем объект состояния  
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

  // сбросить счетчик инициализаций
  public void resetInitCnt() { init_cnt = 0; }

  // после первой инициализации пишем данные для таблиц (сохраненные настройки)
  public void initFRTables(FiscalPrinter fp) throws FrException
  {
    if (init_cnt == 1 && state != null && !inArr(A_NOT_SET_TABLES, state.getModeNum()))
    {
      psh.setText("Настройка таблиц...");
      initFiscalPrinterParams(fp);
    }
  }

  // программирование таблиц ФР (Режимы, Текст в чеке) (если есть сохраненные данные в db cash)
  private void initFiscalPrinterParams(FiscalPrinter fp) throws FrException
  {
    FrKPrinter pr = (FrKPrinter)fp;

    Settings set = env.getSprModel().getSettings();

    if (getLog().isDebugEnabled())
      getLog().debug("*** Program fr tables ...");
    _copyPropToFp(pr, set, set.getFrModes(), Integer.class, 1);

    _copyPropToFp(pr, set, set.getFrCheckText(), String.class, 40);
  }

  // программирование одной из таблиц
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

//        val = sfmt.getCenterString(prop, ' ', sz); // текст в чеке(по центру)
        
      tidx = set.getTabRowCol(key);

      if (getLog().isDebugEnabled()) 
        getLog().debug("*** Set tab[" + tidx[0] + "]." + tidx[1] + "." + tidx[2] + " = " + val);

      fp.setTabCell(tidx[0], tidx[1], tidx[2], val, sz);
    }
  }



  private final static int[] NORMAL_MODES = new int[] {2, 3, 4};
  private final static int[] FIXABLE_MODES = new int[] {8, 6, 10};

  // время ожидания допечатки документа после команды продолжения печати
  private final static long CPRINT_TIMEOUT = 500;

  // проверка и корректировка состояния ФР. Если дальнейшая работа недопустима - 
  // то возвращает строку с описанием причины (далее кассу нужно блокировать)
  // Вызывается  после процедуры инициализации устройства (SalePanel.do_open())
  // TODO - добавить подробное журналирование на уровне WARN
  public String checkAndFixState(FiscalPrinter fp) throws Exception
  {
    String res = null;   // сообщение о некорректном состоянии
    boolean err = false; // ошибка в процессе коррекции состояния
    int fm;              // номер режима

    do
    {
      try
      {
        fm = state.getModeNum();
        if (inArr(NORMAL_MODES, fm) && state.getSubModeFr() != 3) // ???
        // TODO   Кроме корректных режимов есть еще и некорректные подрежимы 
        // нужно сделать более корректным анализ состояния
        {
          if (!err && getLog().isDebugEnabled())
            getLog().debug("Initial state Ok: " + state.getModeNumStr());

          err = false;

          if (!err && fm == 4)
          {
            res = timeSynchronization(fp, env.getSprModel().getSettings().getTimeDifSec()); // дополнительная синхронизация времени при закрытой смене
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
            res = "Некорректное состояние фискального регистратора:\n" + state.getModeNumStr();
            getLog().error("NOT FIXED!!! :" + res);
          }

          err = false;        
          
        }
  
        
      } catch (FrException ex)
      {
        // обрабатывается ошибка отсутствия бумаги
        if (inArr(FR_NO_PAPER_ERR, ex.getErrorCode()))
        {
          err = true;
          showError(noPaperText(ex/*last_exc*/.getMessage())); // 26.08.11
          stateRequest(fp);
        } 
        else
        // и продолжения печати ... после заправки ленты
        if (FrErrors.ERR_FR_WAIT_CONTINUE_CMD == ex.getErrorCode())
        {
          err = true;
          getLog().error(ex.getMessage());
          fp.continuePrint();
          getLog().warn("CMD - continue printing");
          Thread.currentThread().sleep(CPRINT_TIMEOUT);
          stateRequest(fp); // повторный запрос состояния
        }
        else throw ex;
        
      }

    } while (err && res == null);

    return res;    
  }
  
  
  

  // для отмены чека нужна бумага :-)
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

    // проверка состояния ФР перед закрытием фискального документа
    public void checkStateBeforeClosing(FiscalPrinter fp, FDoc doc) throws Exception
    {
      state = stateRequest(fp);  

      Date cur_date = new Date();

      if (checkTime((FullStateFr)state, cur_date, env.getSprModel().getSettings().getWorkLocalDif()))
        throw new UserException("Критичное расхождение времени ПК и ФР!\n" + createDateErrMsg((FullStateFr)state, cur_date));
      
      if (env == null ? use_weight_sensors : env.getSprModel().getSettings().isUseWeightSensors())
        checkWeightSensors(state);
      else
        checkOpticalSensors(state);

      if (doc instanceof ZReport) checkModeAvail(A_Z_MODES, state, doc); else
      if (doc instanceof XReport) checkModeAvail(A_X_MODES, state, doc); else
      if (doc instanceof Pay) checkModeAvail(A_PAY_MODES, state, doc); else
      if (doc instanceof Check) {
        if (state.getModeNum() == 3)
          throw new UserException(state.getModeNumStr() + ".\n" + "Закройте смену - снимите Z-ОТЧЕТ !!!");
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

    private final static String ET_NO_CHECK_ROLL = "Закончилась чековая лента!";
    private final static String ET_NO_LOG_ROLL = "Закончилась котрольная лента!";

    
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
        throw new UserException("Операция \'" + doc.getTypeName() + "\' недоступна в данном режиме:\n" + state.getModeNumStr());
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
    return "Ошибка ФР : " + err_text + "!\nВставьте БУМАГУ и нажмите <ОПЛАТА> для продолжения!";
  }

  private StateA stateRequest(FiscalPrinter fp) throws Exception
  {
    state = fp.stateRequest();
    fireStateReceived(new StateFrEvent(state)); // оповещение о новом состоянии
    return state;
  }

  // обработка ошибки закрытия документа
  // например - ошибка печати (регистрации документа), связь с устройством ... 
  
  public boolean handlePrintError(FiscalPrinter pr, FDoc doc, FrException ex)
  {
    last_exc = ex;
    boolean res = false; // результат обработки ошибки (успешно / нет)
    boolean lflag = true; // признак продолжения цикла обработки ошибки
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
          // в зависимости от кода ошибки рассматриваются следующие варианты:

          // закончилась чековая или контрольная лента
          case FrErrors.ERR_NO_CHECK_ROLL:
          case FrErrors.ERR_NO_LOG_ROLL :

            // нет ленты - продолжение печати и повторная печать документа с места прерывания ...
            showError(noPaperText(last_exc.getMessage()), last_exc);
            state = stateRequest(pr);
            
            break;

          // ошибка связи с устройством
          case FrErrors.ERR_DEV_NOT_FINDED :
          case FrErrors.ERR_TIMEOUT_READ :
          case FrErrors.ERR_IO_STREAM :
          case FrErrors.ERR_READ_DATA :
          case FrErrors.ERR_READ_CMDC :

            showError("Ошибка ФР : Нет связи с устройством!", last_exc);
            if (showQuestion("Повторить попытку подключения ФР?"))
            {
              initFiscalPrinterDevice(pr);
            } else
            {
              res = false; lflag = false; // выход - ошибка не исправлена
            }
            break;

          // не хватает наличности в кассе   
          case FrErrors.ERR_NO_SUCH_NAL_MONEY :
          
            if (doc instanceof ReturnCheck)
            {
              showError(ex);

              if ( showQuestion("Отменить чек возврата ?") )
              {
                ((ReturnCheck)doc).cancel(env.getCurrentUser());
                pr.calcelCheck();
                res = true;
              }
            } 
            lflag = false;

            break;

          // ожидание команды продолжения печати
          case FrErrors.ERR_FR_WAIT_CONTINUE_CMD :
            showError(last_exc);
            pr.continuePrint();
            Thread.currentThread().sleep(CPRINT_TIMEOUT);
            break;

          // идет печать предыдущей команды
          case FrErrors.ERR_PRINTING_PROCESS :
            showError(last_exc);
            break;

          default : res = false; lflag = false; 
        }

        // продолжение обработки ошибки
        if (lflag)
        {
          stateRequest(pr); // added 28.01.2009
          // если ФР ожидает команду продолжения печати 
          if (state.getSubModeFr() == (byte)3) 
          {
            pr.continuePrint();
            Thread.currentThread().sleep(CPRINT_TIMEOUT);
          }
          // если есть незавершенный документ - допечатываем
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





  // печать документа с обработкой некорректных подрежимов ФР
  // changed 29.01.2009
  // changed 04.08.2011
  public void printDoc(FiscalPrinter fp, FDoc doc) throws Exception
  {
    try
    { // *** собственно печать ***
      doc.print(fp);

      // для отчетов делаем паузу, ожидая переход в новое состояние 
      if (doc instanceof Report)
      {
        Thread.currentThread().sleep(CPRINT_TIMEOUT); 

        // после печати документа, проверяем состояние (подрежим)
      
        int submode;
        do {

          stateRequest(fp);

          submode = state.getSubModeFr();

          switch (submode)
          {
            case StateConst.SM_PAPER_OK : break;

            // если нет бумаги - проверяем датчики чековой и контрольной ленты            
            case StateConst.SM_ACTIV_NO_PAPER : case StateConst.SM_PASS_NO_PAPER : 
              String s = _checkOpticalSensors(state);
              if (s != null)  showError(noPaperText(s));
            break;

            // если устройство ждет команду продолжения печати -
            case StateConst.SM_WAIT_REPEAT :          
              fp.continuePrint(); 
              Thread.currentThread().sleep(CPRINT_TIMEOUT); 
            break;

            // на остальные подрежимы - ожидание и повторный запрос состояния
            default :  
              Thread.currentThread().sleep(CPRINT_TIMEOUT);
            /*
            case 4 : case 5 :  // если идет печать, то ждем, ждем ...
              Thread.currentThread().sleep(CPRINT_TIMEOUT);
            break;
            */
          }
              
        } while (submode != StateConst.SM_PAPER_OK );  // added 04.08.11, 

        // дополнительно ожидание для задумчивых отчетов 
        Thread.currentThread().sleep(2 * CPRINT_TIMEOUT);  
        stateRequest(fp); // обновляем текущее состояние
      }      
            
    } catch (FrException ex)
    {
      if (!handlePrintError(fp, doc, ex)) throw getLastException();
    } 

  }


  // установить текущее время на фискальнике
  // 26.01.2009 добавлена возможность отката на дату из отчета состояния фискальника
  // ошибки обрабатываются внутри метода
  // результат - если null, коррекция времени успешна, not null - текст сообщения об ошибке
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
      getLog().error("Time syncronisation Error", ex); // подробно в журнал

      // rem uved 12.04.10
//      emsg = "Ошибка синхронизации времени Фискального регистратора.\n" +
//      "Причина: '" + ex.getMessage() + "'\n";
      // если ошибка в расхождении времени (сброс времени на компе)
      if (ex instanceof FrException && 
        ((FrException)ex).getErrorCode() == FrErrors.ERR_CURDATE_LESS_DATE_FM);
      {
        // added here 12.04.10
        emsg = "Ошибка синхронизации времени Фискального регистратора.\n" +
              "Причина: '" + ex.getMessage() + "'\n" + 
               createDateErrMsg((FullStateFr)state, cur_date);
      }    
//      {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//        emsg = emsg + "Дата ПК: " + sdf.format(cur_date) + "   Дата ФР: " + sdf.format(((FullStateFr)state).getCurDate()) + "\n"+
//        "Перезагрузите компьютер. Если ошибка повторится -\nОбратитесь в службу технической поддержки";
//      }

      //showError(emsg);
    }
    return emsg;
  }

  private String createDateErrMsg(FullStateFr st, Date cur_date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm");
    return "Дата ПК: " + sdf.format(cur_date) + "   Дата ФР: " + sdf.format(((FullStateFr)state).getCurDate()) + "\n"+
     "Перезагрузите компьютер. Если ошибка повторится -\nОбратитесь в службу технической поддержки";
  }




  // ************************************************************************** 
  // get / set
  public void setEnv(KKMEnv e) { env = e; }

  public void setProgress(ProcessShow p) { psh = p; } 

  public StateA getFrState() { return state; }

  public FrException getLastException() { return last_exc; }
  
  // вызов UI интерфейса, при обработке ошибки
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
