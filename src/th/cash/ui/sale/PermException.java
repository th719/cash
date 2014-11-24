package th.cash.ui.sale;

public class PermException extends UserException 
{
  public PermException()
  {
    this("Операция запрещена в настройках");
  }

  public PermException(String msg)
  {
    super(msg);
  }
}