package th.cash.model;

public class CheckPerm 
{
  private boolean f_open, f_type_code, f_scan_bc, f_type_bc, f_storno, f_repeat, f_cancel, f_close;
  
  public CheckPerm(boolean open, boolean type_code, boolean scan_bc, boolean type_bc, 
                   boolean storno, boolean repeat, boolean cancel, boolean close)
  {
    f_open = open;
    f_type_code = type_code;
    f_scan_bc = scan_bc;
    f_type_bc = type_bc;
    f_storno = storno;
    f_repeat = repeat;
    f_cancel = cancel;
    f_close = close;
  }

  public boolean isOpen() { return f_open; }
  public boolean isTypeCode() { return f_type_code; }
  public boolean isScanCode() { return f_scan_bc; }
  public boolean isTypeBc() { return f_type_bc; }
  public boolean isStorno() { return f_storno; }
  public boolean isRepeat() { return f_repeat; }
  public boolean isCancel() { return f_cancel; }
  public boolean isClose() { return f_close; }
  
}