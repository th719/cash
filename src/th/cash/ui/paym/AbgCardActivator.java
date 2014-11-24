package th.cash.ui.paym;

import java.util.Properties;
import java.util.Vector;

import java.text.SimpleDateFormat;

import th.cash.dev.FiscalPrinter;
import th.cash.card.CardActivator;

import th.cash.abg.*;

import th.cash.fr.doc.StrFormat;
import th.cash.fr.FrKPrinter;
import th.cash.fr.table.FrTable;

import th.cash.model.Settings;

import java.io.*;

import org.apache.log4j.Logger;
import java.text.*;

import java.sql.Connection;

import th.cash.fr.doc.*;

import java.util.Date;

import java.util.Collections;


public class AbgCardActivator implements CardActivator 
{
  // номер терминала, параметр обязательный 
  private String trm_num;

  // каталоги где находятся файлы транзакций и файлы отчетов
  // файлы могут иметь последовательную нумерацию 
  // причем данные в файл отчета дописываются АС
  private String tr_remote_dir;

  // каталог, где обрабатываются авторизационные файлы 
  private String auth_remote_dir; 

  // каталог для файлика с текущими транзакциями (для закрфтия дня по картам )
  private String cur_tr_dir;      

  // для уже обработанных отчетов локальный каталог 
  private String arch_dir;        

  // ----------------------------------------------------------------------

  // файл, в котором накапливаются транзакции смены
  // потом делается закрытие дня по банковским картам 
  private File tr_buf_file;       


  private Logger log = Logger.getLogger(/*"CARD"*/ "UI" + '.' + this.getClass().getName());
                                  

  public AbgCardActivator()
  {
  }

  public final static String P_TR_REMOTE_DIR = "tr_remote_dir";
  public final static String P_AUTH_REMOTE_DIR = "auth_remote_dir";
  public final static String P_ARCH_DIR = "arch_dir";

  public final static String P_CUR_TR_DIR = "cur_tr_dir";

  public void setParams(Properties p)
  {
    trm_num = p.getProperty(Settings.TRM_NUM);
    // здесь же настраиваются и каталоги ... 
    tr_remote_dir = p.getProperty(P_TR_REMOTE_DIR);
    auth_remote_dir = p.getProperty(P_AUTH_REMOTE_DIR);
    cur_tr_dir = p.getProperty(P_CUR_TR_DIR);
    arch_dir = p.getProperty(P_ARCH_DIR);
  }


//  public Object readCard()
//  {
//    CardInputDialog cid = new CardInputDialog(null);
//    cid.show();
//    return cid.isSelected() ? cid.getTrackData() : null;
//  }

  // запрос безналичной суммы 
  public Object requestSum(String card_data, double sum)
  {
    
    File f;
    AuthData req, ans;
    String fn_req, fn_ans;
    try
    {
      log.info("Card activation");
      fn_req = auth_remote_dir + File.separatorChar + "$int_i$." + trm_num;
      fn_ans = auth_remote_dir + File.separatorChar + "$int_o$." + trm_num;

      f = new File(fn_ans);
      if (f.exists()) f.delete();
      
      req = AuthData.createAuthData(null, card_data, FConst.TT_SALE, sum, trm_num);


      AuthData.writeToFile(req, fn_req);

      log.info("Request prepared:");
      log.info(req.getData());
      

      f = new File(fn_ans);

      int i = 0;

      while (i < 60 && !f.exists())
      {
        Thread.currentThread().sleep(500);
        i++;
      }

      if (f.exists()) 
      {
        log.info("Answer file exists");
        ans = AuthData.readFromFile(fn_ans);

        new File(fn_ans).delete();
        

        log.info("Answer:");
        log.info(ans.getData());

        String res = AuthData.checkAnswer(ans);


        if ("ok".equalsIgnoreCase(res))
        {
          log.info("Ok! " + ans.getValue(13));
        } else
        { 
          if (res != null)
          {
            log.error("ERR!" + AuthData.getErrHiddenComents(ans));
            return res;
          }
        }
      }
      else
      {
        ans = null;
        log.error("No answer file");
        new File(fn_req).delete();
      }

      //ans = req; // временно 

    } catch  (Exception ex)
    {
      // log error ? TODO
      ans = null;
      log.error(ex);
    }
    
    return ans;
  }




  // печать банковских чеков ...
  private final static int WCH_NUM = 36;

  public String printBankCheck(FiscalPrinter pr, Object p1) throws Exception
  {
    AuthData d = (AuthData)p1;
    String a1, a2, a3, a4; // параметры адреса терминала - откуда брать ?

    StrFormat fmt = new StrFormat();
    String sd1,sd2;

    sd1 = fmt.getSEString("T: " + d.getValue(17), ' ', "B:    " + d.getValue(15), WCH_NUM);
    sd2 = fmt.getSEString("M: " + d.getValue(19), ' ', "R:   " + d.getValue(2), WCH_NUM);

    String ci1, ci2;
    ci1 = fmt.getSEString(d.getValue(14), ' ', "(D)", WCH_NUM);

    String cnn = null, cdd = null;
    if (d.getCharValue(3) == FConst.EM_READER)
    {
      cnn = d.getValue(4); cdd = d.getValue(5);
    }
    if (cnn == null || cnn.length() < 16) cnn = "????????????????"; 
    if (cdd == null || cdd.length() < 4) cdd = "????";
    ci2 = fmt.getSEString("XXXX-XXXX-XXXX-" + cnn.substring(12, 16), ' ', cdd.substring(2, 4) + '/' + cdd.substring(0, 2), WCH_NUM);


    String  sum_s = fmt.getSEString("СУММА:", ' ', d.getValue(7).trim() + " RUB", 18);

    String auth_s = fmt.getSEString("Авторизовано:", ' ', d.getValue(12), WCH_NUM);

    SimpleDateFormat sdf_tr = new SimpleDateFormat("ddMMyyHHmmss");
    String tr_date = d.getValue(8) + d.getValue(9);
    java.util.Date date_from_tr = sdf_tr.parse(tr_date);
    

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy   HH:mm");
    String dt_s = fmt.getCenterString( sdf.format(date_from_tr), ' ', WCH_NUM);

    FrTable tb = new FrTable((byte)4);
    tb.initFromFr(((FrKPrinter)pr).getFrDrv());


    _printBankCheck(pr, sd1, sd2, ci1, ci2, sum_s, auth_s, dt_s, tb, fmt);

    _printBankCheck(pr, sd1, sd2, ci1, ci2, sum_s, auth_s, dt_s, tb, fmt);

    return null;

  }
   // печать 1 экземпляра
  private void _printBankCheck(FiscalPrinter pr, String pd1, String pd2, String ci_s1, String ci_s2, 
                               String sum_s, String auth_s, String date_s, FrTable tb, StrFormat fmt) throws Exception
  {


    pr.printString(pd1);
    pr.printString(pd2);

    pr.transportTape(1, true);

    pr.printString(ci_s1);
    pr.printString(ci_s2);
    
    pr.transportTape(1, true);

    pr.printBoldString("ОПЛАТА");    
    pr.printBoldString(sum_s);    

    pr.transportTape(1, true);

    pr.printString(auth_s);

    pr.transportTape(1, true);

//    pr.printString("ДЕБЕТУЙТЕ МОЙ СЧЕТ");
//    pr.printString("ПОДПИСЬ КЛИЕНТА:");
    
    pr.transportTape(2, true);
    pr.printString(fmt.getCenterString("", '_', WCH_NUM));
    pr.printString(fmt.getCenterString("( подпись кассира )", ' ', WCH_NUM));

    pr.transportTape(2, true);
    pr.printString(fmt.getCenterString("", '_', WCH_NUM));
    pr.printString(fmt.getCenterString("( подпись клиента )", ' ', WCH_NUM));


    pr.transportTape(1, true);
    pr.printString(fmt.getCenterString(date_s, ' ', WCH_NUM));
    
    pr.printString(fmt.getCenterString(" СПАСИБО ЗА ПОКУПКУ ", '-', WCH_NUM));
    pr.transportTape(1, true);

    pr.transportTape(4, true);

    pr.cutCheck(); // отрезка

    // клише для следующего документа 
    int i, len = tb.getNumRows();
    for (i = len - 4; i < len; i++)
      if (tb.getValueAt(i, 0) != null) pr.printString(tb.getValueAt(i, 0).toString());
    
    
  }
  

  /**
   * возвращает часть имени файла транзакций. Эта часть представляет собой
   * дату открытия файла транзакций, те дату начала смены по картам
   * YY_MM_DD_HH_MM
   */
  public String getBankShiftFn() throws IOException
  {
    File f = searchTrFile(cur_tr_dir, BFN_PREF);

    String res = null;

    if (f != null && f.exists())
    {
      res = f.getName();
      int len = res.length();
      res = res.substring(BFN_PREF.length(), len - 4);
    }
    return res;
  }

  public Date getBankShiftDate() throws ParseException, IOException
  {
    String s = getBankShiftFn();
    return s == null ? null : new SimpleDateFormat(TR_FILE_DATE_MASK).parse(s);
  }

  // подготовка файла для отчета по банковским картам
  // имя файла транзакций сожержит дату открытия смены (первой безналличной транзакции)
  // если файл отсутствует - то создаем новый 

  // поиск существующих файлов по шаблону
  private File searchTrFile(String dir, String pref) throws IOException
  {
    File tr_dir = new File(dir);
    File last_file = null;


    File[] bfs = tr_dir.listFiles(new SPFilter(pref, "." + trm_num));

    if (bfs != null && bfs.length > 0)
    {
      String s, cur_fn = "";
      
      for (int i = 0; i < bfs.length; i++)
      {
        s = bfs[i].getName();
        if (s.compareTo(cur_fn) > 0) { cur_fn = s; last_file = bfs[i]; }
      }

    }
    return last_file;
  }

 
  // создать новый файл транзакций или венруть ссылку на старый, уже открытый
  private File prepareTrFile() throws IOException
  {
    if (cur_tr_dir == null) return null;

    File last_file = searchTrFile(cur_tr_dir, BFN_PREF);

    if (last_file == null)
    {
      String dt_lbl = makeCurTimeMark();
      last_file = new File(cur_tr_dir + File.separator + BFN_PREF + dt_lbl + "." + trm_num);
    }


    return last_file;
  }


  private final static String TR_FILE_DATE_MASK = "yy_MM_dd_HH_mm";
  // суффикс для нумерации файла по текущему времени
  private String makeCurTimeMark()
  {
    SimpleDateFormat sdf_fl = new SimpleDateFormat(TR_FILE_DATE_MASK);
    return sdf_fl.format(new java.util.Date());
  }

  private String makeTrString(AuthData d) throws ParseException
  {
    String s_exp_date = d.getValue(5);
    String s_tr_date = d.getValue(8) + d.getValue(9);

    SimpleDateFormat fmt = new SimpleDateFormat("yyMM");
    SimpleDateFormat fmt_tr_date = new SimpleDateFormat("ddMMyyHHmmss");

    SimpleDateFormat fmt_tr_out = new SimpleDateFormat("yyyyMMdd");

    return d.getValue(15) + ',' + "xxxxxxxxxxxx" +d.getValue(4).substring(12) + ',' + 
      fmt_tr_out.format( fmt.parse(d.getValue(5)) ) + ',' +
      d.getValue(7) + ',' + d.getValue(12).substring(0, 6) + ',' +
      d.getValue(2) + ',' + fmt_tr_out.format( fmt_tr_date.parse(d.getValue(8) + d.getValue(9)) ) + ',' +
      d.getValue(9) + ',' + d.getValue(10);
      
  }

  // Запись данных банковской транзакции в строку транзакции
  public String saveBankTrans(Object d) 
  {
    String result = null;

    try
    {
      AuthData ans = (AuthData)d;
      String td = makeTrString(ans);

      tr_buf_file = prepareTrFile();

      log.info("Transaction file is " + tr_buf_file.getAbsolutePath());

      if (tr_buf_file != null)
      {
        if (!tr_buf_file.exists()) tr_buf_file.createNewFile();

        if (tr_buf_file.exists())
        {
          log.info("td=" + td);
          BufferedWriter tr_wr = new BufferedWriter(new FileWriter(tr_buf_file, true));
          tr_wr.write(td);
          tr_wr.newLine();
          tr_wr.flush();
          tr_wr.close();
        } else
          result = "Не удалось создать журнал банковских транзакций " + tr_buf_file.getCanonicalPath();
      } result = "Не определен файл журнала банковских транзакций";
    } catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
      result = "Ошибка при записи транзакции: " + ex.getMessage();
    }

    return result;
  }

  public static void copyFile(File src, File dst)
    throws IOException
  {
    BufferedInputStream is = new BufferedInputStream(
      new FileInputStream(src));

    BufferedOutputStream os = new BufferedOutputStream(
      new FileOutputStream(dst));

    int d;

    while ( (d = is.read()) != -1) os.write(d);

    is.close();
    os.close();
  }

  // закрыть смену по банковским картам
  // файлы отчетов обрабатываются и после этого удаляются из рабочего каталога обмена
  // дополнительно перемещение в локальный архив

  private final static String BFN_PREF = "_buf_tr_";
  private final static String CFN_PREF = "_cur_tr_";
  private final static String REQ_PREF = "R_";
  private final static String REP_PREF = "T_";
  private final static String CRP_PREF = "_cur_rep_";

  public String makeReport(Connection c, int kkm_num, int cashier_num, boolean log_tr_to_file) 
  {
    log.info("makeReport() start");
    System.out.println("makeReport() start");

    if (cur_tr_dir == null) return "Parameter for Report dir is null"; // фатальная ошибка, каталог отчета не указан

    String res = null; 


    boolean wait_for_report = false;
    boolean parse_report = false;
    File report_file = null;

   log.info("cur_tr_dir=" + cur_tr_dir);

    
    try
    {
      // ищем текущий файл транзакций, копию того, что находится на удаленном ресурсе    
      File in_proc_file = searchTrFile(cur_tr_dir, CFN_PREF);

      if (in_proc_file == null)
      {
        // подготовить файл транзакций для обработки
        File buf_file =  searchTrFile(cur_tr_dir, BFN_PREF);
        if (buf_file != null)
        {
          String dmark = buf_file.getName().substring(BFN_PREF.length());
          log.info("dmark = " + dmark);
          String nfn = REQ_PREF + dmark;
          File tr_file = new File(tr_remote_dir + File.separator + nfn);
        
          copyFile(buf_file, tr_file);  

          buf_file.renameTo(new File(cur_tr_dir + File.separator +CFN_PREF + dmark));

          wait_for_report = true;

          report_file = new File(tr_remote_dir + File.separator + REP_PREF + dmark);
          
        } else
         res = "Нет данных для закрытия смены."; // нет транзакций для отчета
        
      } else
      {
        // в локальном каталоге есть копия файла транзакций, который в данный момент на обработке
        // ищем файл запроса в каталоге АС
        File remote_req_file = searchTrFile(tr_remote_dir, REQ_PREF);
        if (remote_req_file == null || !remote_req_file.exists())
        {
          // если нет файла запроса ищем отчет
          File remote_rep_file = searchTrFile(tr_remote_dir, REP_PREF);
          if (remote_rep_file == null || !remote_rep_file.exists())
          {
            // ни отчета, ни запроса нет на удаленном сервере, ОШИБКА
            res = "Отчет АС не найден";
          } else
          {
            // есть отчет
            parse_report = true;
            report_file = remote_rep_file;
          }
        } else
        {
          // нужно продолжать ожидать ответ, пока появится отчет
          wait_for_report = true;
          String dmark = remote_req_file.getName();
          dmark = dmark.substring(dmark.length() - TR_FILE_DATE_MASK.length() - 4);
          report_file = new File(tr_remote_dir + File.separator + REP_PREF + dmark);

        }
        
      }


      if (wait_for_report)
      {
        int i = 0;
        int max_iter = 60;
        log.info("Waiting for report ...");

        while (i < max_iter && !report_file.exists())
        {
          Thread.currentThread().sleep(500);
          i++;
        }

        parse_report = i < max_iter;

        log.info(i < max_iter ? "Ok" : "No report ...");

        if (!parse_report)
          res = "Ошибка. Файл отчета АС отсутствует";
        
      }

      if (parse_report)
      {
        log.info("Parse report ...");
        String dmark = report_file.getName();
        dmark = dmark.substring(dmark.length() - 18);
        File local_report_file = new File(cur_tr_dir + File.separator + CRP_PREF + dmark);
        //copyFile(report_file, local_report_file);
        report_file.renameTo(local_report_file);


        // разбираем файл транзакций и файл отчета всесте и перегонаяем все это в кассовые транщакции
        File cur_tr_file = new File(cur_tr_dir + File.separator + CFN_PREF + dmark);

        log.info("Tr - " + cur_tr_file + "   rep_file - " + local_report_file);
 
        parseReport(cur_tr_file, local_report_file, c, kkm_num, cashier_num, log_tr_to_file); // TODO

        // оба файла переносим в архив
//        copyFile(local_report_file, new File(arch_dir + File.separator + REP_PREF + dmark));
//        copyFile(cur_tr_file, new File(arch_dir + File.separator + CFN_PREF + dmark));
        local_report_file.renameTo( new File(arch_dir + File.separator + REP_PREF + dmark));
        cur_tr_file.renameTo( new File(arch_dir + File.separator + REQ_PREF + dmark));
      }
    
        // файл идентифицируется временнОй меткой - и транзакции и отчет
        // сначала поиск тр. файла в уданном каталоге отчета, он может еще там лежать

        // потом поиск самого файла отчета, может быть обработан с прошлого раза

        // только после этого готовим текущий буфер к обработке

        // потом ожидаем ответа 


      
    } catch (Exception ex)
    {
      log.error(ex, ex);
      res = "Ошибка " + ex.getMessage();
    }
    return res;
  }

  // ЧИТАЕМ файл транзакций
  private void readBankTr(File f, Vector buf) throws IOException, ParseException
  {
    FileInputStream fis = new FileInputStream(f);
    BufferedReader is = new BufferedReader(new InputStreamReader(fis));

    String line = null;
    buf.clear();
    String trmn, cardno, expdate, samount, authcode, msgn, stransdate, stt;
    BankOper oper;
    double amount;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd,HHmmss");
    SimpleDateFormat edf = new SimpleDateFormat("yyyyMMdd");

    int i0, i;
    while ((line = is.readLine()) != null)
    {
      trmn       = line.substring(0, 3).trim();
      cardno     = line.substring(4, 23).trim();
      expdate    = line.substring(24, 32).trim();
      samount    = line.substring(33, 45).trim();
      amount = Double.parseDouble(samount);
      authcode   = line.substring(46, 52).trim();
      msgn       = line.substring(53, 57).trim();
      stransdate = line.substring(58, 73).trim(); // вместе дата и время
      stt =        line.substring(74, 75);

      oper = new BankOper(cardno, edf.parse(expdate), amount, authcode, sdf.parse(stransdate), stt.charAt(0));

      buf.add(oper);
    }
    
    fis.close();
  }

 
  private final static int[] RCW = new int[] {1, 4, 19, 4, 12, 9, 8, 8, 9, 15};
  // разбор отчета по картам
  private void readReport(File f, Vector buf) throws IOException, ParseException
  {
    FileInputStream fis = new FileInputStream(f);
    BufferedReader is = new BufferedReader(new InputStreamReader(fis));

    String line = null;
    buf.clear();
    char tt =  FConst.TT_SALE;
    String chn, cardnum, expdate,  samount, authcode, tdatatime, trm_num, merch_num;
    BankOper oper;
    int pcnt;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    SimpleDateFormat edf = new SimpleDateFormat("yyMM");


    while ((line = is.readLine()) != null)
    {
      if (line.indexOf("Transaction type:") > 0 && line.indexOf("DEBIT") > 0 )  tt = FConst.TT_SALE; else
      if (line.indexOf("Transaction type:") > 0 && line.indexOf("CREDIT") > 0) tt = FConst.TT_CREDIT; else
      if (line.length() == 96 && line.startsWith("*"));
      {
        pcnt = 1;
        pcnt++; chn       = line.substring(pcnt, pcnt += RCW[1]).trim(); 
        pcnt++; cardnum   = line.substring(pcnt, pcnt += RCW[2]).trim();
        pcnt++; expdate   = line.substring(pcnt, pcnt += RCW[3]).trim();
        pcnt++; samount   = line.substring(pcnt, pcnt += RCW[4]).trim();
        pcnt++; authcode  = line.substring(pcnt, pcnt += RCW[5]).trim();
        pcnt++; tdatatime = line.substring(pcnt, pcnt += RCW[6] + 1 + RCW[7]).trim();
        pcnt++; trm_num   = line.substring(pcnt, pcnt += RCW[8]).trim();
        pcnt++; merch_num = line.substring(pcnt, pcnt += RCW[9]).trim();

        Date td = sdf.parse(tdatatime);
        Date ed = edf.parse(expdate);

        oper = new BankOper(cardnum, ed, Double.parseDouble(samount), authcode, td, tt);
        oper.setCheckNum(Integer.parseInt(chn));
        buf.add(oper);

        // разделить по типам дебит и кредит
      } 
      // все не подходящее под условия строки данных просто отсеиваем ?

    }
  }

  // распихиваем данные о оплате по полям транзакции. Схема такова:
  // id      | default
  // t_d     | дата транзакции
  // t_t     | спец. тип транзакции
  // k_n     | номер кассы (как обычно)
  // ch_n    | номер чека (из отчета ?)
  // ca_n    | номер кассира
  // s_d     | номер карты (размер поля в таблице позволяет)
  // i_d     | тип транзакции
  // f1      | код авторизации как double
  // f2      | numeric(15,3)               | 
  // f3      | сумма операции
  private void parseReport(File trf, File repf, Connection c, int kkm_num, int cashier_num, boolean log_to_file) throws Exception
  {

    Vector trv = new Vector(100, 100);  // трназзакции из запроса
    readBankTr(trf, trv);
    log.info("cash tr count = " + trv.size()); 
    

    Vector repv = new Vector(100, 100);  // ответ АС
    readReport(repf, repv);
    log.info("report el count = " + trv.size()); 

    Collections.sort(trv); // по дате, затем авт. коду и тд... 
    Collections.sort(repv);

    log.info("Sort vectors"); 

    // Кассовые транзакции , соответствующие разобранным отчетам 
    Vector tr_to_save = new Vector(100, 100);

    int i, sz = trv.size();
    BankOper tr_oper, rep_oper;
    int sr = 0;
    for (i = 0; i < sz; i++)
    {  
      tr_oper = (BankOper)trv.get(i);

      sr = Collections.binarySearch(repv, tr_oper);

      // есть ли в отчете  подтверждение  или нет
      if (sr >= 0) rep_oper = (BankOper)repv.get(sr); else rep_oper = null; 


      Transaction t = new Transaction(-1, tr_oper.getTrDate(), Transaction.BN_TR, kkm_num, 
        rep_oper == null ? -1 : rep_oper.getCgeckNum(), cashier_num, tr_oper.getCardNo(), 
        Integer.parseInt(String.valueOf(tr_oper.getTType())), Double.parseDouble(tr_oper.getAuthCode()), 0, tr_oper.getAmount(), null, null);

      tr_to_save.add(t);
      
    }
    log.info("Compare ok, tr count = " + trv.size());

    sz = repv.size();
    // а это для левых транзакций , которых нет в исходном файле 
    for (i = 0; i < sz; i++)
    {
      rep_oper = (BankOper)repv.get(i);
      sr = Collections.binarySearch(trv, rep_oper);

      if (sr < 0)
      {
        // нонсенс! транзакция которую мы даже не запрашивали !
        Transaction t = new Transaction(-1, rep_oper.getTrDate(), Transaction.BN_TR, kkm_num, 
          rep_oper.getCgeckNum(), cashier_num, rep_oper.getCardNo(), 
          -Integer.parseInt(String.valueOf(rep_oper.getTType())), Double.parseDouble(rep_oper.getAuthCode()), 0, rep_oper.getAmount(), null, null);
        
        tr_to_save.add(t);
      }
    }

    log.info("tr to_save count = " + tr_to_save.size()); 
    
        
    if (tr_to_save != null && tr_to_save.size() > 0)
    {
      DbConverter.saveTrVector(c, tr_to_save, log_to_file);
      c.commit();
    }

  }
  
}