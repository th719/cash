package th.cash.ui.sale;

public class PermException extends UserException 
{
  public PermException()
  {
    this("�������� ��������� � ����������");
  }

  public PermException(String msg)
  {
    super(msg);
  }
}