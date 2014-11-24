package th.cash.ui.sale;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// редактор цифер, как в касире
public class InputLabel extends JLabel implements KeyListener 
{
  private final static String DEF_MASK = "0.00";

  private StringBuffer inpBuf;
  private int dot_pos = 0;
  private boolean dot_typed  = false;
  private boolean clear_type = false; // режим набора с очисткой пердыдущего текста

  public InputLabel()
  {
    super("");
    clear();
  }

  private static Insets min_ins = new Insets(0, 0, 0, 0);
  
  public Insets getInsets()
  {
    return min_ins;
  }

  // переинициализация поля ввода
  private void initInputBuffer()
  {
    inpBuf = new StringBuffer(DEF_MASK);
    dot_pos = getDotPos();
    dot_typed = false;
  }

  private void displayInput()
  {
    setText(inpBuf.toString());
  }

  private int getDotPos()
  {
    return dot_pos = inpBuf.toString().indexOf(DOT);
  }

  private void shiftLeft(char c)
  {
    int i1, i2;
  }


  private final static char DOT = '.';
  private final static char ZERO = '0';
  private final static char PLUS = '+';

  private boolean isDotZero(char c) { return c == DOT || c == ZERO; }
  
  private void addChar(char c)
  {
    int len = inpBuf.length();

    if (len < inpBuf.capacity() || c == DOT)
    {
      getDotPos();

      int i;

      if (dot_typed)
      {
        if (c == DOT)
        {
          for(i = dot_pos + 1; i < len; i++)
            inpBuf.setCharAt(i - 1, inpBuf.charAt(i));
          inpBuf.setCharAt(len - 1, c);
          // added 19.12.07 {
          int j = 0;
          while (j < len - 2 && isDotZero(inpBuf.charAt(j))) j++;
          if (j > 0) inpBuf.delete(0, j);
          // }
        } else
        {
          if (len - 1 - dot_pos < 3)
            inpBuf.append(c);
        }
      } else
      {

        if (c == DOT)
        {
          int j = 0;
          while ( j < len && (inpBuf.charAt(j) == ZERO || inpBuf.charAt(j) == DOT)) j++;
          if (j == len) j--; // added
          if (j < len)
          {
            for(i = dot_pos + 1; i < len; i++)
              inpBuf.setCharAt(i - 1, inpBuf.charAt(i));
            inpBuf.setCharAt(len - 1, c);
            dot_typed = true;
            if (j > 0) inpBuf.delete(0, j - 1);
          }
        } else
        {
          int i1 = 0, i2 = 0; // последний 0 и не 0

          while (i2 < len && (inpBuf.charAt(i2) == ZERO || inpBuf.charAt(i2) == DOT))
          {
            if (inpBuf.charAt(i2) == ZERO) i1 = i2;
            i2++;
          }

          if (i2 > 0)
          { // сдвигаем влево в рамках маски
            int j1 = i1, j2 = i2;
            while (j2 < len)
            {
              while (inpBuf.charAt(j1) == DOT) j1++;
              inpBuf.setCharAt(j1, inpBuf.charAt(j2));
              j2++; j1++;
            }
            inpBuf.setCharAt(len - 1, c);
          } else
          {  // двигаем точку вправо;
            inpBuf.setCharAt(dot_pos, inpBuf.charAt(dot_pos + 1));
            inpBuf.setCharAt(dot_pos + 1, DOT);
            inpBuf.append(c);
          }
        }
      
      }
      displayInput();
    }
  }

  private void removeLast()
  {
    int len = inpBuf.length();
    if (len > 1)
    {
      getDotPos();
      if (dot_typed)
      {
        if (dot_pos == len - 1)
          inpBuf.deleteCharAt(dot_pos - 1);
        else 
          inpBuf.deleteCharAt(len - 1);
      } else
      {
        if (dot_pos > 1)
        {
          inpBuf.deleteCharAt(len - 1);
          inpBuf.setCharAt(dot_pos, inpBuf.charAt(dot_pos - 1));
          inpBuf.setCharAt(dot_pos - 1, DOT);
        } else
        {
          // сдвигаем вправо
          int i = 0;
          while (i < len && (inpBuf.charAt(i) == ZERO || inpBuf.charAt(i) == DOT)) i++;
//          System.out.println("i = "+i);
          if (i < len)
          {
            int i2 = len - 1;
            int i1 = i2 - 1;
            while (i <= i1)
            {
              while (inpBuf.charAt(i1) == DOT) i1--;
              inpBuf.setCharAt(i2, inpBuf.charAt(i1));
              i1--; i2--;
            }  
            inpBuf.setCharAt(i, ZERO);
          }
        }
      }
    }
    if (inpBuf.length() == 1) initInputBuffer();

    displayInput();
  }

  public void clear()
  {
    initInputBuffer();
    displayInput();
  }

  public void clearOld()
  {
    if (clear_type) { clear(); clear_type = false; }
  }

  // возвращает набранную строку
  public String getValue()
  {
    return inpBuf.toString();
  }

  // возвращает только цифры из поля ввода
  public String getValueAsInt()
  {
    StringBuffer res = new StringBuffer();
    int len = inpBuf.length();
    char c;
    for (int i = 0; i < len; i++)
    {
      c = inpBuf.charAt(i);
      if (Character.isDigit(c)) res.append(c);
    }
    return res.toString();
  }


  public void keyTyped(KeyEvent e)
  {
    char c = e.getKeyChar();
//    вариант, с очисткой по любой кнопке
//    if (clear_type) { clear(); clear_type = false; }
    if (c == KeyEvent.VK_BACK_SPACE)
    {
      clearOld();
      removeLast();
    } else
      if (Character.isDigit(c) || c == DOT || c == PLUS)
      {
        clearOld();
        if (c == PLUS)
        {
          addChar(ZERO); addChar(ZERO);
        } else
          addChar(e.getKeyChar());
      }
  }

  public void keyPressed(KeyEvent e)
  {
  }

  public void keyReleased(KeyEvent e)
  {
  }

  public void setClearTyping()
  {
    clear_type = true;
  }

  
}