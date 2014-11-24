package th.cash.model;


import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Collections;
import java.util.Vector;
import java.util.Properties;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Comparator;

//import th.cash.log.LogUtil;
import th.cash.model.GoodMain;
import th.cash.model.User;

import org.apache.log4j.Logger;

/**
 * Контроллер модели (обновление строк, инициализация из базы)
 * User: lazarev
 * Date: 15.11.2007
 * Time: 19:41:27
 */
public class SprController {

    // data model object
    private SprModel model;

    // списки измененных объектов
    // товары  
    private LinkedList goodChanges;
    private LinkedList barcodeChanges;
    private LinkedList taxChanges;
    // пользователи
    private LinkedList userChanges;
//    private LinkedList roleChanges; // пока использование не оправдано, роли статичны
    // скидки и дисконтные карты
    private LinkedList fdscChanges;
    private LinkedList dcardChanges;
    private LinkedList amassDscChanges;
    private LinkedList autoDscChanges;
    // настройки программы, немного иной механизм
    private Properties paramChanges;
    private Vector params_to_remove;


    // spr update flags 
    private boolean f_goods_mdf = false;
    private boolean f_barcodes_mdf = false;
    private boolean f_taxes_mdf = false;

    private boolean f_users_mdf = false;
    private boolean f_roles_mdf = false;

    private boolean f_fdsc_mdf = false;
    private boolean f_dcard_mdf = false;
    private boolean f_amass_mdf = false;
    private boolean f_auto_mdf = false;

    private boolean f_param_mdf = false;

    // дата последнего обновления
    private Date last_update = null;
    

    // logger object
    protected final static String LOG_PREF = "MODEL"; 
    private Logger log = Logger.getLogger(LOG_PREF + '.' + this.getClass().getName());
    

    public SprController(SprModel model)
    {
        this.model = model;
        goodChanges = new LinkedList();
        barcodeChanges = new LinkedList();
        taxChanges = new LinkedList();

        userChanges = new LinkedList();
//        roleChanges = new LinkedList();

        fdscChanges = new LinkedList();
        dcardChanges = new LinkedList();
        amassDscChanges = new LinkedList();
        autoDscChanges = new LinkedList();

        paramChanges = new Properties();
        params_to_remove = new Vector(2, 5);
    }


   /**
    * обновление модели из таблиц БД (при старте программы)
    */
   public void refreshAllModel(Connection c) throws SQLException
   {
     refreshGoods(c);
     if (log.isDebugEnabled())
       log.debug("Goods cnt = " + model.getGoodsVector().size());

     refreshBarcodes(c);
     if (log.isDebugEnabled())
       log.debug("Barcodes cnt = " + model.getBarcodesVector().size());

     refreshRoles(c);     
     if (log.isDebugEnabled())
       log.debug("Roles cnt = " + model.getRoles().size());

     refreshUsers(c);
     if (log.isDebugEnabled())
       log.debug("Users cnt = " + model.getUsersVector().size());



     // к пользователям прицепляем роли ... 
     Vector users = model.getUsersVector();
     User user;
     for (int i = 0; i < users.size(); i++)
     {
       user = (User)users.get(i);
       model.findUserRole(user);
     }

     refreshTaxes(c);
     if (log.isDebugEnabled())
       log.debug("Taxes cnt = " + model.getTaxVector().size());
     
     refreshSettings(c);
     if (log.isDebugEnabled())
       log.debug("Settings cnt = " + model.getSettings().getAllSet().size());

     // discounts
     refreshFDsc(c);
     if (log.isDebugEnabled())
       log.debug("Fixwd discounts cnt = " + model.getFDsc().size());

     // added 05.11.2009
     refreshDCards(c);
     if (log.isDebugEnabled())
       log.debug("DiscountCards cnt = " + model.getDscCards().size());

     refreshAmassDsc(c);
     if (log.isDebugEnabled())
       log.debug("AmassDsc cnt = " + model.getAmassDsc().size());

     refreshAutoDsc(c);
     if (log.isDebugEnabled())
       log.debug("AutoSchem cnt = " + model.getAutoSch().size());
   }

    
    private final static String GOODS_SEL_QUERY = 
      "select good_id, gname, price, wc, tax_gr, max_dsc, adscs, sect from t_gm order by good_id";

    public void refreshGoods(Connection c) throws SQLException
    {
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(GOODS_SEL_QUERY);
        _refreshGoods(rs);
        rs.close();
        st.close();
        
        Collections.sort(model.getGoodsVector());
    }
    
    private final static String BARCODE_SEL_QUERY =
      "select barcode, good_id, price, koef from t_gb order by barcode, good_id";
    public void refreshBarcodes(Connection c) throws SQLException
    {
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(BARCODE_SEL_QUERY);
        _refreshBarcodes(rs);
        rs.close();
        st.close();

        Collections.sort(model.getBarcodesVector());
    }
    
    private final static String USER_SEL_QUERY =
      "select code, uname, np, pwd from t_user order by code";
    public void refreshUsers(Connection c) throws SQLException
    {
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(USER_SEL_QUERY);
        _refreshUsers(rs);
        rs.close();
        st.close();

        Collections.sort(model.getUsersVector());
    }

    private final static String NALOG_SEL_QUERY = 
      "select t.tax_id, t.tname, g.gtax_id, t.tpc, t.knum "+
      "from t_tax t, t_gtax g where t.tax_id = g.tax_id order by gtax_id"; // TODO

    public void refreshTaxes(Connection c) throws SQLException
    {
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(NALOG_SEL_QUERY);
        _refreshTaxes(rs);
        rs.close();
        st.close();

        Collections.sort(model.getTaxVector());
    }

    private final static String SET_SEL_QUERY = 
      "select set_id, set_val from t_set order by set_id";
    public void refreshSettings(Connection c) throws SQLException
    {
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(SET_SEL_QUERY);
      _refreshSettings(rs);  // temporary
      rs.close();
      st.close();

      model.getSettings().initDefProps(); 
    }

    private final static String ROLE_SEL_QUERY = 
      "select id,rname,flags from t_np order by id";

    public void refreshRoles(Connection c) throws SQLException
    {
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(ROLE_SEL_QUERY);
      _refreshRoles(rs);
      rs.close();
      st.close();

      Collections.sort(model.getRoles());
    }


    public final static String FDSC_SEL_QUERY = 
      "select d_id,name,is_dsc,dsc_kind,dsc_value,ct,cp,is_fc,is_fp from t_fd order by d_id";

    public void refreshFDsc(Connection c) throws SQLException
    {
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(FDSC_SEL_QUERY);
      _refreshFDsc(rs);
      rs.close();
      st.close();

      Collections.sort(model.getFDsc());
    }


    public final static String AMDSC_SEL_QUERY = 
      "select d_id,s_id,ds_name,name,dsc_value,ct,is_ss,ss,is_es,es,is_sr,sr,is_er,er "+
      "from t_dd order by s_id,d_id";

    private void refreshAmassDsc(Connection c) throws SQLException
    {
      Statement st = c.createStatement();

      ResultSet rs = st.executeQuery(AMDSC_SEL_QUERY);

      
      _refreshAmassDsc(rs);
      rs.close();
      st.close();

      Collections.sort(model.getAmassDsc());
      
    }

    public final static String DCARD_SEL_QUERY = 
      "select c_id,name,ct,a_sum,auto_id,is_fb,is_na,is_p,is_pay,is_ap,ms_id,rn,amass_id "+
      "from t_d_card order by c_id";

    private void refreshDCards(Connection c) throws SQLException
    {
      Statement st = c.createStatement();

      ResultSet rs = st.executeQuery(DCARD_SEL_QUERY);

      _refreshDCard(rs);
      rs.close();
      st.close();

      Collections.sort(model.getDscCards());
    }

    public final static String AMASS_DSC_SEL_QUERY = 
      "select s.s_id,s.name as s_name,s.is_uwc,d.d_id,d.name as d_name,d.is_dsc,d.dsc_kind,"+
      "d.dsc_value,d.ct,d.bd,d.ed,d.bt,d.et,d.ws,d.we,d.sa,d.ea,d.ss,d.es,d.apc,d.is_cs "+
      "from t_as s,t_ad d where s.s_id=d.s_id order by s.s_id,d.d_id";
      
    private void refreshAutoDsc(Connection c) throws SQLException
    {
      Statement st = c.createStatement();
      ResultSet rs;

      rs = st.executeQuery(AMASS_DSC_SEL_QUERY);

      _refreshAutoDsc(rs);

      rs.close();
      st.close();

      Collections.sort(model.getAutoSch());
    }
 

    // чтение курсоров
    // _______________
    private void _refreshGoods(ResultSet rs) throws SQLException
    {
        Vector goods = model.getGoodsVector();
        goods.clear();
        GoodMain gm;

        int ival, pcnt;
        double dval;
        Integer goodId;
        String  name;
        Double  price;
        Integer adscSchem;
        Boolean weightControl;
        Integer section;
        Double  maxDsc;
        Integer nalGroup;


        while (rs.next())
        {
            pcnt = 0;
            ival = rs.getInt(++pcnt);
            goodId = new Integer(ival);

            name = rs.getString(++pcnt);
            price = new Double(rs.getDouble(++pcnt));

            ival = rs.getInt(++pcnt);
            weightControl = ival == 1 ? Boolean.TRUE : Boolean.FALSE;

            ival = rs.getInt(++pcnt);
            nalGroup = rs.wasNull() ? null : new Integer(ival);

            dval = rs.getDouble(++pcnt);
            maxDsc = rs.wasNull() ? null : new Double(dval);

            ival = rs.getInt(++pcnt);
            adscSchem = rs.wasNull() ? null : new Integer(ival);

            ival = rs.getInt(++pcnt);
            section = rs.wasNull() ? null : new Integer(ival);

            gm = new GoodMain(goodId, name, price, adscSchem, weightControl, section, maxDsc, nalGroup);

            goods.add(gm);
        }
    }

    private void _refreshBarcodes(ResultSet rs) throws SQLException
    {
        Vector barcode = model.getBarcodesVector();
        barcode.clear();
        Barcode bc;
        int pcnt;
        String s;
        Integer good_id;
        Double price;
        Double koef;

        while (rs.next())
        {
            pcnt = 0;
            s = rs.getString(++pcnt);
            good_id = new Integer(rs.getInt(++pcnt));
            price = new Double(rs.getDouble(++pcnt));
            koef = new Double(rs.getDouble(++pcnt));

            bc = new Barcode(s, good_id, price, koef);
            barcode.add(bc);
        }
    }
    
    private void _refreshUsers(ResultSet rs) throws SQLException
    {
        Vector users = model.getUsersVector();
        users.clear();
        User user;
        
        int pcnt;
        Integer code;
        String name;
        Integer np;
        String pwd;        
        
        while (rs.next())
        {
            pcnt = 0;
            code = new Integer(rs.getInt(++pcnt));
            name = rs.getString(++pcnt);
            np = new Integer(rs.getInt(++pcnt));
            // TODO пароль кассира  нужно декодировать!
            pwd = rs.getString(++pcnt);
            user = new User(code, name, np, pwd);
            users.add(user);
        } 
    }

    private void _refreshTaxes(ResultSet rs) throws SQLException
    {
      Vector taxes = model.getTaxVector();
      taxes.clear();
      Tax tax;
      int pcnt;
      Integer taxId;
      String taxName;
      Integer gtaxId;
      Double  taxPc;   // ставка налога
      Integer kkmNum;  // номер в регистраторе
        
      while (rs.next())
      {
          pcnt = 0;
          taxId = new Integer(rs.getInt(++pcnt));
          taxName = rs.getString(++pcnt);
          gtaxId = new Integer(rs.getInt(++pcnt));
          taxPc  = new Double(rs.getDouble(++pcnt));
          kkmNum = new Integer(rs.getInt(++pcnt));
          tax = new Tax(taxId, taxName, gtaxId, taxPc, kkmNum);
          taxes.add(tax);
      } 
      
    }

    private void _refreshSettings(ResultSet rs) throws SQLException
    {

      model.getSettings().clear();

      String key, val;
      while (rs.next())
      {
        key = rs.getString(1);
        val = rs.getString(2);
        model.getSettings().putProperty(key, val);
      }
    }

    private void _refreshRoles(ResultSet rs) throws SQLException
    {
      Vector roles = model.getRoles();
      roles.clear();
      Role r;
      int pcnt;
      int ival;
      String name, flags;

      while (rs.next())
      {
        pcnt = 0;
        ival = rs.getInt(++pcnt);
        name = rs.getString(++pcnt);
        flags = rs.getString(++pcnt);

        r = new Role(new Integer(ival), name, flags);

        roles.add(r);
        
      }
    }


    private void _refreshFDsc(ResultSet rs) throws SQLException
    {
      Vector fdsc = model.getFDsc();
      fdsc.clear();
      FixedDsc fd;
      int pcnt;
      int id = 0, type, kind;
      boolean is_fc, is_fp;
      String name, ct, pref;
      double d;

      while (rs.next())
      {
        pcnt = 0;

        id = rs.getInt(++pcnt);
        name = rs.getString(++pcnt);

        type = rs.getInt(++pcnt);
        kind = rs.getInt(++pcnt);

        d = rs.getDouble(++pcnt);
        
        ct = rs.getString(++pcnt);
        pref = rs.getString(++pcnt);

        is_fc = rs.getInt(++pcnt) == 1;
        is_fp = rs.getInt(++pcnt) == 1;

        fd = new FixedDsc(new Integer(id), name, type, kind, new Double(d), ct, pref, new Boolean(is_fc), new Boolean(is_fp));

        fdsc.add(fd);
      }
    }


    private void _refreshDCard(ResultSet rs) throws SQLException
    {
      Vector cards = model.getDscCards();
      cards.clear();

      DscCard c;
      int pcnt;

      String c_id, name, ct;
      double a_sum;
      int auto_id, ms_id, rn, amass_id;
      boolean is_fb, is_na, is_p, is_pay, is_ap;

      while (rs. next())
      {
        pcnt = 0;

        c_id = rs.getString(++pcnt);
        name = rs.getString(++pcnt);
        ct = rs.getString(++pcnt);

        a_sum = rs.getDouble(++pcnt);
        auto_id = rs.getInt(++pcnt);

        is_fb = rs.getInt(++pcnt) == 1;
        is_na = rs.getInt(++pcnt) == 1;
        is_p  = rs.getInt(++pcnt) == 1;
        is_pay = rs.getInt(++pcnt) == 1;
        is_ap = rs.getInt(++pcnt) == 1;

        ms_id = rs.getInt(++pcnt);
        rn = rs.getInt(++pcnt);
        amass_id = rs.getInt(++pcnt);

        c = new DscCard(c_id, name, ct, new Double(a_sum), new Integer(auto_id), new Integer(amass_id),
          new Boolean(is_fb), new Boolean(is_na), new Boolean(is_p), 
          /*new Boolean(is_pay),*/ new Boolean(is_ap), /*new Integer(ms_id),*/
          new Integer(rn) );

        cards.add(c);
      }
    }

//      "select d_id,s_id,ds_name,name,dsc_value,ct,is_ss,ss,is_es,es,is_sr,sr,is_er,er from t_dd order by s_id,d_id";

    private void _refreshAmassDsc(ResultSet rs) throws SQLException
    {
      Vector amd = model.getAmassDsc();
      amd.clear();

      AmassDsc dsc;
      int pcnt;

      int d_id, s_id;
      String ds_name, name, ct;
      double dsc_value;
      double ss, es;
      int sr, er;
      boolean is_ss, is_es, is_sr, is_er;


      while (rs.next())
      {
        pcnt = 0;

        d_id = rs.getInt(++pcnt);
        s_id = rs.getInt(++pcnt);
        ds_name = rs.getString(++pcnt);
        name = rs.getString(++pcnt);

        dsc_value = rs.getDouble(++pcnt);
        ct = rs.getString(++pcnt);

        is_ss = rs.getInt(++pcnt) == 1;
        ss = rs.getDouble(++pcnt);
        is_es = rs.getInt(++pcnt) == 1;
        es = rs.getDouble(++pcnt);

        is_sr = rs.getInt(++pcnt) == 1;
        sr = rs.getInt(++pcnt);
        is_er = rs.getInt(++pcnt) == 1;
        er = rs.getInt(++pcnt);

        dsc = new AmassDsc(new Integer(d_id), name, ct, new Double(dsc_value), 
          new Boolean(is_ss), new Double(ss), new Boolean(is_es), new Double(es),
          new Boolean(is_sr), new Integer(sr), new Boolean(is_er), new Integer(er),
          new Integer(s_id), ds_name);

        amd.add(dsc);
      }
    }

//      "select s.s_id,s.name as s_name,s.is_uwc,d.d_id,d.name as d_name,d.is_dsc,d.dsc_kind,"+
//      "d.dsc_value,d.ct,d.bd,d.ed,d.bt,d.et,d.ws,d.we,d.sa,d.ea,d.ss,d.es,d.apc,d.is_cs "+
//      "from t_as s,t_ad d where s.s_id=d.s_id order by s.s_id,d.d_id";

    private void _refreshAutoDsc(ResultSet rs) throws SQLException
    {
      Vector _asv = model.getAutoSch();
      Vector _adv = null;
      AutoDsc ad;
      AutoSchem as;

      int pcnt;
      int s_id, prev_s_id, d_id;
      String s_name, d_name, ct;
      boolean is_uwc, is_dsc;
      int dtype, kind;
      double dv;
      Timestamp bd, ed;
      String bt, et;
      int ws, we;
      double sa, ea, ss, es;
      int apc;
      boolean is_cs;
      

      prev_s_id = Integer.MIN_VALUE;

      while (rs.next())
      {
        pcnt = 0;

        // читаем новую строку
        s_id = rs.getInt(++pcnt);
        s_name = rs.getString(++pcnt);
        is_uwc = rs.getInt(++pcnt) == 1;
        d_id = rs.getInt(++pcnt);
        d_name = rs.getString(++pcnt);
        is_dsc = (dtype = rs.getInt(++pcnt)) == 1;
        kind = rs.getInt(++pcnt);
        dv = rs.getDouble(++pcnt);
        ct = rs.getString(++pcnt);
        bd = rs.getTimestamp(++pcnt);
        ed = rs.getTimestamp(++pcnt);
        bt = rs.getString(++pcnt);
        et = rs.getString(++pcnt);
        ws = rs.getInt(++pcnt);
        we = rs.getInt(++pcnt);
        sa = rs.getDouble(++pcnt);
        ea = rs.getDouble(++pcnt);
        ss = rs.getDouble(++pcnt);
        es = rs.getDouble(++pcnt);
        apc = rs.getInt(++pcnt);
        is_cs = rs.getInt(++pcnt) == 1;

        if (prev_s_id != s_id)
        {
          // новая схема
          _adv = new Vector(5, 5);
          as = new AutoSchem(new Integer(s_id), s_name, new Boolean(is_uwc), _adv);
          _asv.add(as);
          prev_s_id = s_id;          
          
        }

        ad = new AutoDsc(new Integer(s_id), new Integer(d_id), d_name, dtype, 
          kind, new Double(dv), ct, bd, ed, bt, et, new Integer(ws), new Integer(we), 
          new Double(sa), new Double(ea), new Double(ss), new Double(es), new Boolean(is_cs));
        _adv.add(ad);
        
      }

      // линейный ResultSet превращаем в список
    }

    /**
     * добавление набора прав каждому пользователю
     */



    /** 
     * применение изменений справочников. В векторе data заменяются/добавляются
     * элементы из ch
     */
    private int applyVectorChanges(Vector data, LinkedList ch) 
    {
      int cnt = 0;

      synchronized(ch)
      {
        Object o;
        boolean has_elements = true;
        BCComparator cmp = new BCComparator();
        while (has_elements)
        {
          try
          {
            o = ch.removeFirst();
            if (o == null)
              data.clear();
            else
            {
              // added 15.01.09, ChComparator ...
              int i = Collections.binarySearch(data, o, cmp);
              if (i < 0) data.insertElementAt(o, -i - 1); else data.set(i, o);
              cnt++;
            }
          } catch (NoSuchElementException ex)
          {
            has_elements = false;
          }
        }
      }

      return cnt;
    }

    // новый метод обновления справочника товаров 
    private int applyGoodsChanges(LinkedList ch)
    {
      int cnt = 0;

      
      synchronized(ch)
      {
        boolean has_elements = true;
        boolean f_update = true;
        GoodMain gm;
        Object o;
        while (has_elements)
        {
          try
          {
            o = ch.removeFirst();
            if (o == null) 
            {
              model.getGoodsVector().clear();
              model.getBarcodesVector().clear();
              f_update = false;
            } else
            {
              gm = (GoodMain)o;
              model.replceGoodMain(gm);

              // делее нужно удалить все ш/к с этим кодом товаром
              // тупо линейный поиск (  
              if (f_update)
              {
                int i = 0;
                while ((i = model.searchNextBarcode(i, gm.getGoodId())) >=0)
                  model.getBarcodesVector().remove(i);
              }
              
            }
            
          } catch (NoSuchElementException ex)
          {
            has_elements = false;
          }
          
        }

      }


      return cnt;
    }

    /**
     * применить изменения для списка пользователей
     */
    private void applyUserChanges()
    {
      synchronized(userChanges)
      {
        Object o;
        boolean has_elements = true;
        while (has_elements)
        {
          try
          {
            o = userChanges.removeFirst();
            if (o == null) model.getUsersVector().clear(); else model.replaceUser((User)o); 
          } catch (NoSuchElementException ex)
          {
            has_elements = false;
          }
        }
      }
    }

    /**
     * применить изменение параметров
     */
    private void applyParamChanges()
    {
      synchronized(paramChanges)
      {
        Enumeration keys = paramChanges.propertyNames();
        Object _key, _val;
        while (keys.hasMoreElements())
        {
          _key = keys.nextElement();
          _val = paramChanges.get(_key);
          model.getSettings().putProperty((String)_key, (String)_val);
        }

        keys = params_to_remove.elements();
        while (keys.hasMoreElements())
        {
          _key = keys.nextElement();
          model.getSettings().removeProperty(_key);;
        }

        if (paramChanges.size() > 0)
        {
          model.getSettings().initDefProps(); 
          paramChanges.clear();  // очищаем список изменений
        }

        if (params_to_remove.size() > 0) params_to_remove.clear();
      }
    }

    /**
     * применить измнения по всем справочникам 
     */
    public void applyChanges()
    {
      f_goods_mdf = !goodChanges.isEmpty();
      f_barcodes_mdf = !barcodeChanges.isEmpty();
      f_taxes_mdf = !taxChanges.isEmpty();

//      f_roles_mdf = !roleChanges.isEmpty();
      f_users_mdf = !userChanges.isEmpty();

      f_fdsc_mdf = !fdscChanges.isEmpty();
      f_dcard_mdf = !dcardChanges.isEmpty();
      f_amass_mdf = !amassDscChanges.isEmpty();
      f_auto_mdf = !autoDscChanges.isEmpty();

      f_param_mdf = paramChanges.size() > 0;
      
      
      // товары и ш/к объединить в один метод 
      applyGoodsChanges(goodChanges);
      applyVectorChanges(model.getBarcodesVector(), barcodeChanges);
      // налоги
      applyVectorChanges(model.getTaxVector(), taxChanges);

//    роли через справочник не обновляются, только путем обновлений
//      applyVectorChanges(model.getRoles(), roleChanges);
      applyUserChanges();

      //  added 05.11.2009
      // скидки, дисконтные карты
      applyVectorChanges(model.getFDsc(), fdscChanges);
      applyVectorChanges(model.getDscCards(), dcardChanges);
      applyVectorChanges(model.getAmassDsc(), amassDscChanges);
      applyVectorChanges(model.getAutoSch(), autoDscChanges);

      applyParamChanges();

      if (isDataChanged())
      {
        last_update = new Date();
        if (log.isDebugEnabled()) log.info("applyChanges()");
      }
    }


    // признак изменеия пользователей
    public boolean isUsersChanged() { return f_users_mdf; }

    // признак изменения модели 
    public boolean isDataChanged() 
    {
      return 
        f_goods_mdf || f_barcodes_mdf || f_taxes_mdf || f_roles_mdf || f_users_mdf ||  
        f_fdsc_mdf || f_dcard_mdf || f_amass_mdf || f_auto_mdf || f_param_mdf; 
    }

    /**
     * ***********************************************************
     * методы, вызываемые регистрации измененых объектов модели
     * вызов из потока обмена ...
     * ***********************************************************
     */

    public void addGoodsChanges(GoodMain gm)
    {
      synchronized(goodChanges)
      {
        goodChanges.addLast(gm);
      }
    }

    public void addBarcodeChanges(Barcode b)
    {
      synchronized(barcodeChanges)
      {
        barcodeChanges.addLast(b);
      }
    }

    public void addUserChanges(User u)
    {
      synchronized(userChanges)
      {
        userChanges.addLast(u);
      }
    }

    public void addTaxChanges(Tax t)
    {
      synchronized(taxChanges)
      {
        taxChanges.addLast(t);
      }
    }

    public void addFDscChanges(FixedDsc d)
    {
      synchronized(fdscChanges)
      {
        fdscChanges.addLast(d);
      }
    }

    
    public void addDCardChanges(DscCard c)
    {
      synchronized(dcardChanges)
      {
        dcardChanges.addLast(c);
      }
    }

    public void addAmassDscChanges(AmassDsc d)
    {
      synchronized(amassDscChanges)
      {
        amassDscChanges.addLast(d);
      }
    }


    public void addAutoDscChanges(AutoDsc d)
    {
      synchronized(autoDscChanges)
      {
        autoDscChanges.addLast(d);
      }
    }

    public void addParamChanges(String key, String value)
    {
      synchronized(paramChanges)
      {
        if (value == null || "".equals(value))
          params_to_remove.add(key);
        else
          paramChanges.put(key, value);
      }
    }

}
