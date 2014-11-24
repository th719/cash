package th.cash.ui.setup;


import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import th.cash.model.Settings;

// параметры загрузки / выгрузки справочников
public class ExchSetPanel extends TabSetPanel
{
//  private JTextField jtf_kkm_num;
  
  private JTextField jtf_sprLoadFile, jtf_sprLoadFlag;
  private JTextField jtf_repUnloadFile, jtf_repUnloadFlag;
  private JCheckBox  jcb_loadEnabled, jcb_unloadEnabled;

  private String kkm_num, sprLoadFile, sprLoadFlag, repUnloadFile, repUnloadFlag;

  private boolean loadEnabled, unloadEnabled;

  public ExchSetPanel()
  {

    init();
  }

  private void init()
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    JPanel p, tv;

    FormLayout fl;
    CellConstraints cc = new CellConstraints();

//    FormLayout fl = new FormLayout("p, 2dlu, p", "p");
//    p = new JPanel(fl);
//
//    CellConstraints cc = new CellConstraints();
//
////    p.add(new JLabel("N кассы"), cc.xy(1, 1));
////    p.add(jtf_kkm_num = new JTextField(10), cc.xy(3, 1));
//
//    p.setBorder(BorderFactory.createTitledBorder("-"));
//    add(p);


    fl = new FormLayout("p, 2dlu, p", "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
    p = new JPanel(fl);


    p.add(jcb_loadEnabled = new JCheckBox("Разрешить загрузку"), cc.xywh(1, 1, 3, 1));

    p.add(new JLabel("Файл справочника"), cc.xy(1, 3));
    p.add(jtf_sprLoadFile = new JTextField(40), cc.xy(3, 3));

    p.add(new JLabel("Флаг загрузки"), cc.xy(1, 5));
    p.add(jtf_sprLoadFlag = new JTextField(40), cc.xy(3, 5));


    p.add(jcb_unloadEnabled = new JCheckBox("Разрешить выгрузку"), cc.xywh(1, 7, 3, 1));

    p.add(new JLabel("Файл отчета"), cc.xy(1, 9));
    p.add(jtf_repUnloadFile = new JTextField(40), cc.xy(3, 9));

    p.add(new JLabel("Флаг выгрузки"), cc.xy(1, 11));
    p.add(jtf_repUnloadFlag = new JTextField(40), cc.xy(3, 11));

    p.setBorder(BorderFactory.createTitledBorder("Обмен данными"));

    add(p);

    add(Box.createVerticalGlue());
    add(Box.createVerticalBox());
    
  }

  public boolean isDataChanged()
  {
    boolean res = jcb_loadEnabled.isSelected() || jcb_unloadEnabled.isSelected();

    String s;
//    s = jtf_kkm_num.getText().trim();
//    res = res || !s.equals(kkm_num);

    s = jtf_sprLoadFile.getText().trim();
    res = res || !s.equals(sprLoadFile);

    s = jtf_sprLoadFlag.getText().trim();
    res = res || !s.equals(sprLoadFlag);

    s = jtf_repUnloadFile.getText().trim();
    res = res || !s.equals(repUnloadFile);

    s = jtf_repUnloadFlag.getText().trim();
    res = res || !s.equals(repUnloadFlag);

    return res;
  }

  private boolean getBoolProperty(String val) { return "true".equalsIgnoreCase(val); }

  public void refreshData(Properties p)
  {
    // ------------------------
    kkm_num = p.getProperty(Settings.FM_CASH_NUM);
    loadEnabled = getBoolProperty( p.getProperty(Settings.UI_LOAD_ENABLED) );
    unloadEnabled = getBoolProperty( p.getProperty(Settings.UI_UNLOAD_ENABLED) );

    sprLoadFile = p.getProperty(Settings.UI_LOAD_FILE);
    sprLoadFlag = p.getProperty(Settings.UI_LOAD_FLAG);
    repUnloadFile = p.getProperty(Settings.UI_UNLOAD_FILE);
    repUnloadFlag = p.getProperty(Settings.UI_UNLOAD_FLAG);

    // ------------------------
//    jtf_kkm_num.setText(kkm_num);

    jcb_loadEnabled.setSelected(loadEnabled);
    jcb_unloadEnabled.setSelected(unloadEnabled);

    jtf_sprLoadFile.setText(sprLoadFile);
    jtf_sprLoadFlag.setText(sprLoadFlag);
    jtf_repUnloadFile.setText(repUnloadFile);
    jtf_repUnloadFlag.setText(repUnloadFlag);
  }

  public String saveData(Properties p)
  {
//    String s = jtf_kkm_num.getText().trim();
//
//    if ("".equals(s)) return false;
//
//    try { Integer.parseInt(s); } catch (Exception ex) { return false; }

   

    p.setProperty(Settings.UI_LOAD_ENABLED, String.valueOf(jcb_loadEnabled.isSelected()));
    p.setProperty(Settings.UI_UNLOAD_ENABLED, String.valueOf(jcb_unloadEnabled.isSelected()));
    
    p.setProperty(Settings.UI_LOAD_FILE, jtf_sprLoadFile.getText());
    p.setProperty(Settings.UI_LOAD_FLAG, jtf_sprLoadFlag.getText());
    p.setProperty(Settings.UI_UNLOAD_FILE, jtf_repUnloadFile.getText());
    p.setProperty(Settings.UI_UNLOAD_FLAG, jtf_repUnloadFlag.getText());

    return null;
  }

  
}