package th.cash.model;

import java.util.Comparator;

public class BCComparator implements Comparator
{
  /**
  * �������������� ������ ��� ������, �������� ���������� ���������� 
  * Comparable �������� Barcode, GoodMain ..., ������� ��� ������ �� 
  * ���������� ������ (������ �����-�����)
  */
  public int compare(Object o1, Object o2)
  {
    return ((Comparable)o1).compareTo(o2);
  }

}