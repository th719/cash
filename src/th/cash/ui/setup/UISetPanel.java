package th.cash.ui.setup;

import java.util.Properties;
import javax.swing.BoxLayout;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

import javax.swing.Box;
import javax.swing.BorderFactory;

import th.cash.model.Settings;


public class UISetPanel extends TabSetPanel
{

  private JTextField jtf_kkm_num;
  private JCheckBox jcb_use_weight_sensors;
  private JTextField jtf_max_int_quan;
  private JTextField jtf_max_double_quan;
  private JTextField jtf_max_money_sum;
  private JTextField jtf_max_check_sum;

  private String kkm_num;

  public UISetPanel()
  {
    init();
  }

  private void init()
  {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    FormLayout fl = new FormLayout("p, 2dlu, p", "p");
    JPanel p;
    p = new JPanel(fl);

    CellConstraints cc = new CellConstraints();

    p.add(new JLabel("N кассы"), cc.xy(1, 1));
    p.add(jtf_kkm_num = new JTextField(10), cc.xy(3, 1));

    p.setBorder(BorderFactory.createTitledBorder("Основные настройки"));
    add(p);
    
  }

  public boolean isDataChanged()
  {
    boolean res = false;
    
    String s;
    s = jtf_kkm_num.getText().trim();
    res = res || !s.equals(kkm_num);


    return res;
  }

  public String saveData(Properties p)
  {
    String res = null;
  
    String s = jtf_kkm_num.getText().trim();

    if ("".equals(s)) return "Необходимо указать номер кассы!";

    int k_n;
    try { k_n = Integer.parseInt(s); } catch (Exception ex) { return "Некорректно задан номер кассы"; }

    if (k_n < 1 || k_n > 1000000) return "Некорректно задан номер кассы";

    p.setProperty(Settings.P_CASH_NUM, jtf_kkm_num.getText());

    return res;
  }

  public void refreshData(Properties p)
  {
    // ------------------------
    kkm_num = p.getProperty(Settings.P_CASH_NUM);
    // ------------------------
    jtf_kkm_num.setText(kkm_num);

  }
  
  
}