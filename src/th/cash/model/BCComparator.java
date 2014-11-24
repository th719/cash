package th.cash.model;

import java.util.Comparator;

public class BCComparator implements Comparator
{
  /**
  * дополнительный классс для поиска, учитывая реализацию интерфейса 
  * Comparable классами Barcode, GoodMain ..., сделано для поиска по 
  * нескольким ключам (список штрих-кодов)
  */
  public int compare(Object o1, Object o2)
  {
    return ((Comparable)o1).compareTo(o2);
  }

}