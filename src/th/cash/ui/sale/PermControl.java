package th.cash.ui.sale;

import th.cash.fr.doc.*;

import th.cash.model.User;
import th.cash.model.CheckPerm;

public class PermControl 
{
  public PermControl()
  {
  }

    private CheckPerm getCheckPerm(User u, FDoc doc)
    {
      if (doc != null && !(doc instanceof Check)) return null;
      return doc == null || doc != null && doc instanceof SaleCheck ? 
        u.getRole().getSaleCheckPerm() : u.getRole().getReturnCheckPerm();
    }

    // *** for check ***
    public void checkOpenPerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isOpen()) throw new PermException();
    }

    public void checkTypeCodePerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isTypeCode()) throw new PermException();
    }

    public void checkScanCodePerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isScanCode()) throw new PermException();
    }

    public void checkTypeBcPerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isTypeBc()) throw new PermException();
    }

    public void checkStornoPerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isStorno()) throw new PermException();
    }

    public void checkRepeatPerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isRepeat()) throw new PermException();
    }
    
    public void checkCancelPerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isCancel()) throw new PermException();
    }

    public void checkClosePerm(User u, FDoc doc) throws PermException
    {
      if (!getCheckPerm(u, doc).isClose()) throw new PermException();
    }


    // for pay operations
    public void payPerm(User u, int doc_type) throws PermException
    {
      boolean avail = false;
      switch (doc_type)
      {
        case FDoc.FD_PAY_IN_TYPE : avail = u.getRole().isInPay(); break;
        case FDoc.FD_PAY_OUT_TYPE : avail = u.getRole().isOutPay(); break;
      }
      if (!avail) throw new PermException();
    }

    // for reports
    public void reportPerm(User u, int doc_type) throws PermException
    {
      boolean avail = false;
      switch (doc_type)
      {
        case FDoc.FD_ZREPORT_TYPE : avail = u.getRole().isZReport(); break;
        case FDoc.FD_XREPORT_TYPE : avail = u.getRole().isXReport(); break;
      }
      if (!avail) throw new PermException();
    }
  
}