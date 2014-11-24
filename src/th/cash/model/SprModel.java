package th.cash.model;

import java.util.Vector;
import java.util.Collections;
import th.cash.model.GoodMain;
import th.cash.model.User;
import th.cash.model.SearchData;
import th.cash.model.CheckBarcode;
/**
 * Created by IntelliJ IDEA.
 * User: lazarev
 * Date: 15.11.2007
 * Time: 17:14:32
 * To change this template use File | Settings | File Templates.
 */
public class SprModel {

    private Vector goods;
    private Vector barcodes;
    private Vector users;
    private Vector taxes;
    private Vector roles;

    private Vector fdsc;
    private Vector autosch;
    private Vector amassdsc;
    private Vector dcards;
    
    private Settings settings;
    
    private CheckBarcode bc_checker;


    public SprModel(int max_goods, int max_bc)
    {
        goods = new Vector(max_goods, max_goods % 5); // 20% to increase
        barcodes = new Vector(max_bc, max_bc % 5);
        users = new Vector(30);
        taxes = new Vector(2);
        roles = new Vector();
        
        bc_checker = new CheckBarcode();
        settings = new Settings();

        fdsc = new Vector(5, 5);
        autosch = new Vector(5, 5);
        amassdsc = new Vector(5, 5);
        dcards = new Vector(5, 1000); // при использовани нужно обязательно пересматривать
                                      // параметры инициализации вектора
    }

    // быстродействие


    // ***********************************
    public Vector getGoodsVector()
    {
        return goods;
    }

    public Vector getBarcodesVector()
    {
        return barcodes;
    }
    
    public Vector getUsersVector()
    {
        return users;
    }

    public Vector getTaxVector()
    {
      return taxes;
    }

    public Settings getSettings()
    {
      return settings;
    }

    public Vector getRoles()
    {
      return roles;
    }
    // ---------------------------------------------
    public Vector getFDsc() // фиксированные скидки
    {
      return fdsc;
    }

    public Vector getAmassDsc() // скидки накопительные
    {
      return amassdsc;
    }

    public Vector getAutoSch()
    {
      return autosch;
    }

    public Vector getDscCards()
    {
      return dcards;
    }
    
        

    // ********************8
    public void addGoodMain(GoodMain g)
    {
        goods.add(g);
    }

    public void replceGoodMain(GoodMain g)
    {
      replaceObject(goods, g);
    }

    public void addBarcode(Barcode bc)
    {
        barcodes.add(bc);
    }

    public void replaceBarcode(Barcode bc)
    {
      replaceObject(barcodes, bc);
    }

    public void addUser(User user)
    {
        users.add(user);
    }

    public void replaceUser(User u)
    {
      replaceObject(users, u);
      findUserRole(u); // заменяем роль
    }

    public void addTax(Tax tax)
    {
      taxes.add(tax);
    }

    public void replaceTax(Tax t)
    {
      replaceObject(taxes, t);
    }

    public void addRole(Role r)
    {
      roles.add(r);
    }

    public void replaceRole(Role r)
    {
      replaceObject(roles, r);
    }

    public void addFDsc(FixedDsc fd)
    {
      fdsc.add(fd);
    }

    public void replaceFDsc(FixedDsc fd)
    {
      replaceObject(fdsc, fd);
    }

    public void addDCard(DscCard card)
    {
      dcards.add(card);
    }

    public void replaceDCard(DscCard card)
    {
      replaceObject(dcards, card);
    }

    public void addAmassDsc(AmassDsc d)
    {
      amassdsc.add(d);
    }

    public void replaceAmassDsc(AmassDsc d)
    {
      replaceObject(amassdsc, d);
    }

    public void addAutoSch(AutoSchem s)
    {
      autosch.add(s);
    }

    public void replaceAutoSch(AutoSchem s)
    {
      replaceObject(autosch, s);
    }

    // -------------------------------------------
    private void replaceObject(Vector v, Object o)
    {
      int index = Collections.binarySearch(v, o);
      if (index < 0) v.insertElementAt(o, -index - 1); else v.set(index, o);
    }

    public Role findUserRole(User u)
    {
      Role r = searchRole(u.getNp());
      if (r == null) r = new Role();
      u.setRole(r);
      return r;
    }

    // ******************8
    
    /**
     *  Поиск в векторе товаров по КОДУ
     */
    public GoodMain searchGoodMain(Integer id)
    {
        int index;
        GoodMain sgm = new GoodMain(id);
        index = Collections.binarySearch(goods, sgm);
        if (index >= 0)
            return (GoodMain)goods.get(index);
        else
            return null;
    }

    /**
     * Поиск в векторе штрих-кодов по строке ШТРИХ-КОДА, возвращает набор
     * элементов Barcode, один или несколько, тк. штрих-код может повторяться
     */
    public Vector searchBarcode(String bc)
    {
        int index = 0;
        Vector res = null;
        Barcode sbc = new Barcode(bc);

        // начальный поиск в списке ш/к (), по упорядоченному списку 
        index = Collections.binarySearch(barcodes, sbc/*, new BCComparator()*/);

        if (index >= 0)
        {
          // выбираем диапазон, первый элемен
          int i1 = index;
          while (i1 > 0 && ((Barcode)barcodes.get(i1-1)).getBarcode().compareTo(bc) == 0) i1--;

          // последний элемент
          int i2 = index;
          while (i2 < barcodes.size() && ((Barcode)barcodes.get(i2+1)).getBarcode().compareTo(bc) == 0) i2++;
        
  //        if (index >= 0)
  //        {
  //            res = new Vector();
  //            Barcode fbc;
  //            fbc = (Barcode)barcodes.get(index);
  //            res.add(fbc);
  //            // последовательный перебор следующих ш/к, т.к. в упорядоченном списке 
  //            // повторяющиеся позиции будут идти следом
  //            while (++index < barcodes.size() && (fbc = (Barcode)barcodes.get(index)).getBarcode().compareTo(bc) ==0)
  //              res.add(fbc);
  //        }

          // "собираем" элементы по найденным индекса, по порядку сортировки
          res = new Vector(5, 5);
          for (index = i1; index <= i2; index++) res.add(barcodes.get(index));
        }
        
        return res;
    }

    // тупо поиск позиции в векторе ш/к по коду товара 
    // sp - начальная позиция 
    public int searchNextBarcode(int sp, Integer good_id)
    {
      int i = sp;

      while (i < barcodes.size() && !good_id.equals(((Barcode)barcodes.get(i)).getGoodId())) i++;

      if (i == barcodes.size()) i = -1;

      return i;
    }
    
    // возвращает .... вектор GoodMain ?
    public Vector searchGood(SearchData sd, boolean by_code)
    {
        long s_time, e_time;

        
        Vector res = null;
        
        if (by_code)
        {
            GoodMain gm;
            Integer good_id = new Integer(sd.getDataForSearch());
            sd.setGoodId(good_id);
            gm = searchGoodMain(good_id);
            if (gm != null)
            {
                res = new Vector(1,0);
                res.add(gm);
            }
        } else
        {
            bc_checker.analyzeData(sd);
            if (sd.isWeight())
            {
                GoodMain gm = searchGoodMain(sd.getGoodId());
                if (gm != null)
                {
                    res = new Vector(1, 0);
                    res.add(gm);
                }
            } else
            {
                Vector bc_res = searchBarcode(sd.getDataForSearch());
//                System.out.println("BC count = " + bc_res.size());
                if (bc_res != null)
                {
                    Barcode barcode;
                    GoodMain gm;
                    res = new Vector(bc_res.size(), 0);
                    for (int i = 0; i < bc_res.size(); i++)
                    {
                        barcode = (Barcode)bc_res.get(i);
                        gm = searchGoodMain(barcode.getGoodId());
                        if (gm != null)
                            res.add(gm);
                    }
                } 
                sd.setBarcodes(bc_res);
            }
                
        }

        sd.setGoods(res);

        
        return res;
    }

    public Tax searchTax(Integer gtaxId)
    {
      Tax res = null;
      if (gtaxId != null)
      {
        int i = 0;
        Tax tax;
        while (i < taxes.size() && res == null)
        {
          tax = (Tax)taxes.get(i++);
          if (gtaxId.equals(tax.getGtaxId())) res = tax;
        }
      }
      return res;
    }

    public User searchUser(String pwd)
    {
      User res = null;

      int i = 0;
      while (i < users.size() && !pwd.equals(((User)users.get(i)).getPassword())) i++;

      if (i < users.size()) res = (User)users.get(i);
      return res;
    }

    public Role searchRole(Integer id)
    {
        Role res = null;

        int i = 0;
        while (i < roles.size() && !id.equals(((Role)roles.get(i)).getId())) i++;

        if (i < roles.size()) res = (Role)roles.get(i);

        return res;
    }

        
}
