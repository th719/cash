package th.cash.ui.util;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class UserDlg 
{
  public final static String[] YES_NO = {"Да", "Нет"};
  public final static String[] YES_NO_CANCEL = {"Да", "Нет", "Отмена"};
//  public final static String[] OK_CANCEL = {"Ok", "Отмена"};
  public final static String SAVE_QW = "Дaнные были изменены. Сохранить?";
  public final static String SAVE_INF = "Запись в базу прошла успешно";
  private final static String QW_TEXT = "Вопрос";
  private final static String ERR_TEXT = "Ошибка";
  private final static String INF_TEXT = "Информация";

  // ***************************************************************
  public static void setArrowFocusManager()
  {
    DefaultFocusManager.setCurrentManager(new DefaultFocusManager()
    {
      public void processKeyEvent(Component c, KeyEvent e)
      {
        if (e.getModifiers() == 0)
        {
//          if (e.getKeyCode() == e.VK_RIGHT ) e.setKeyCode(e.VK_TAB); else 
//          if (e.getKeyCode() == e.VK_LEFT ) {e.setKeyCode(e.VK_TAB); e.setModifiers(e.SHIFT_MASK); }
          if (e.getKeyCode() == e.VK_RIGHT || e.getKeyCode() == e.VK_LEFT)
          {
            int mdf = e.getKeyCode() == e.VK_LEFT ? e.SHIFT_MASK : 0;
            KeyEvent ne = new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), mdf, e.VK_TAB, '\t' );
            e = ne;
          }
        }
        super.processKeyEvent(c, e);
      }
    });
    
  }
  
  public static void setButtonInputMap()
  {
    UIManager.getDefaults().put(
	    "Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
                         "ENTER", "pressed",
                "released ENTER", "released"
     }));
    
  }
  
  public static boolean showQuestion(Component owner, String mes)
  {
    return 
      JOptionPane.showOptionDialog(owner, mes, QW_TEXT,
        JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,
        null ,YES_NO ,YES_NO[0]) == 0;
        
//    MessageDialog dialog = new MessageDialog(owner, QW_TEXT, mes, YES_NO, 0, JOptionPane.QUESTION_MESSAGE);
//    dialog.setLocationRelativeTo(dialog.getOwner()); 
//    dialog.show();
//
//    boolean res = dialog.getSelectedIndex() == 0;
//
//    dialog.dispose();
//    return res;
  }

  public static int showCancelQuestion(Component owner, String mes)
  {
    return 
      JOptionPane.showOptionDialog(owner, mes, QW_TEXT,
        JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,
        null ,YES_NO_CANCEL ,YES_NO_CANCEL[0]);
  }

  public static int showSaveQuestion(Component owner)
  {
    return showCancelQuestion(owner, SAVE_QW);
  }

  // *******************************************************************
//
//  public static Object show(Component owner)
//  {
//    JOptionPane.showInternalInputDialog(owner)
//  }


  // *******************************************************************
  // диалог для выбора одно варианта из списка
  // return selected variant number or -1, if cancel pressed
//  public static int showSelectDialog(Component owner, String mes, String title, String[] opt, int def)
//  {
//    SelectOptionDialog d;
//    if (owner instanceof Dialog) 
//      d = new SelectOptionDialog((Dialog)owner, title, mes, opt, def);
//    else
//      d = new SelectOptionDialog(JOptionPane.getFrameForComponent(owner), title, mes, opt, def);
//
//    d.show();
//
//    return d.getSelectedIndex();
//  }

  // *******************************************************************
  public static void showError(Component owner, String mes)
  {
    JOptionPane.showMessageDialog(owner,mes, ERR_TEXT, JOptionPane.ERROR_MESSAGE);
  }

  public static void showSaveConfirm(Component owner)
  {
    JOptionPane.showConfirmDialog(owner, SAVE_INF, INF_TEXT, JOptionPane.INFORMATION_MESSAGE);
  }

  public static void showInfo(Component owner, String mes)
  {
    JOptionPane.showMessageDialog(owner, mes, INF_TEXT, JOptionPane.INFORMATION_MESSAGE);
  }


}