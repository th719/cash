package th.cash.ui.sale;

public class UserException extends Exception 
{

  private boolean sound_signal = false;

  public UserException(String msg)
  {
    this(msg, false);
  }
  
  public UserException(String msg, boolean sig)
  {
    super(msg);
    sound_signal = sig;
  }
}