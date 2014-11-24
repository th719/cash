package th.cash.model;


import java.util.Date;

import javax.swing.SwingUtilities;

import th.cash.ui.sale.UpdateLocker;
import th.cash.ui.sale.UsersListDisplayable;

// функционал для потока, который применяет обновления справочника
// для модели данных

public class SprUpdater implements Runnable
{
  private final static int T_PAUSE = 500;  // ms
  private final static int T_SLEEP = 1500; // ms 

  //
  private SprController contr;
  private SprModel model;

  // обновление элемента интерфейса со списком ползователь
  private UsersListDisplayable observer; 
  private boolean box_notify = false;

  // блокировщик обновления (при открытом чеке)
  private UpdateLocker locker;   
  private boolean lock_enabled = true;

//  private Date last_update = null;

  //
  private boolean stop = false;
  
  public SprUpdater(SprController c, SprModel m, 
                    UsersListDisplayable obs, boolean notify,
                    UpdateLocker lck, boolean l_en
                    )
  {
    contr = c;
    model = m;
    observer = obs;
    setBoxNotify(notify);
    setLocker(locker, l_en);
  }

  public void run()
  {
    try{

      while (!stop)
      {
        Thread.currentThread().sleep(T_PAUSE);

        work();
            
        Thread.currentThread().sleep(T_SLEEP);
      }
    } catch (InterruptedException ex)
    {
      stop = true;
    }
  }

  public void work()
  {

    if (lock_enabled)    
    {
      // применить изменения для модели, с блокировкой
      synchronized (locker)
      {
        if (locker.isLocked()) return; else apply();
      }
    } else
    {
      apply();  // без блокировки
    }

  }

  private void apply()
  {
    contr.applyChanges();

    if (contr.isDataChanged()) // регистрируем дату последнего обновления 
    {
      //last_update = new Date();
      //System.out.println("--- SprUpdater " + last_update);

      // оповестить UI со списком пользователей
      if (box_notify && contr.isUsersChanged())
        SwingUtilities.invokeLater(new Runnable() 
        {
          public void run()
          {   
            observer.setUsers(model.getUsersVector());
          }
        });

    }
  }

  public void setBoxNotify(boolean b) { box_notify = observer != null && b; }

  public void setLockEnabled(boolean b) { lock_enabled = locker != null && b; }

  public void setLocker(UpdateLocker uu, boolean en)
  {
    locker = uu; setLockEnabled(en);
  }
  

  public synchronized void setStopped(boolean b) { stop = b; }
}