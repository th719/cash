package th.cash.ui.sale;

import java.awt.Color;

public class TbColorSettins 
{
  private Color normalFg, normalBg, selectedFg, selectedBg;
  
  public TbColorSettins(Color normalFg, Color normalBg, Color selectedFg, Color selectedBg)
  {
    this.normalFg = normalFg;
    this.normalBg = normalBg;
    this.selectedFg = selectedFg;
    this.selectedBg = selectedBg;
  }

  public Color getNormalFg() { return normalFg; }
  public Color getNormalBg() { return normalBg; }

  public Color getSelectedFg() { return selectedFg; }
  public Color getSelectedBg() { return selectedBg; }
  
}