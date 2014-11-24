/*
 * TestConroller.java
 *
 * Created on 16 Ноябрь 2007 г., 19:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package th.cash.test;


import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.Vector;
import th.cash.model.GoodMain;
import th.cash.model.SprController;
import th.cash.model.SprModel;
import th.cash.model.SearchData;
/**
 *
 * @author lazarev
 */
public class TestConroller {
    
    /** Creates a new instance of TestConroller */
    public static void main(String[] args) {

      try {
        
        Driver dri = (Driver)Class.forName("org.postgresql.Driver").newInstance();
        DriverManager.registerDriver(dri);
      
        Connection conn;
        conn = DriverManager.getConnection("jdbc:postgresql://localhost/cash", "santon", "santon");
        conn.setAutoCommit(false);
        
        SprModel model = new SprModel(10000, 10000);
        
        SprController contr = new SprController(model);
        
        contr.refreshGoods(conn);
        contr.refreshBarcodes(conn);
        contr.refreshUsers(conn);
        System.out.println("Goods cnt =" + model.getGoodsVector().size());
        System.out.println("Barcodes cnt =" + model.getBarcodesVector().size());
        System.out.println("Users cnt =" + model.getUsersVector().size());

        
        Vector res = model.searchGood(new SearchData("080432400388"), false);
        
        if (res != null) 
        {
            GoodMain gm;
            for (int i = 0; i < res.size(); i++)
            {
              gm = (GoodMain)res.get(i);
              System.out.println("good name=" + gm.getName() + "  price =" + gm.getPrice());
            }
        }
        
        /*
        GoodMain gm;
        gm = model.searchGoodMain(new Integer(733));
        if (gm != null)
              System.out.println("good name=" + gm.getName() + "  price =" + gm.getPrice());
        */
        conn.close();
      } catch (Exception ex)
      {
          ex.printStackTrace();
      }
          
      
        
    }
    
}
