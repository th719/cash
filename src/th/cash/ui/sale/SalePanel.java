/*
 * SalePanel.java
 *
 * Created on 19 ������ 2007 �., 13:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package th.cash.ui.sale;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.sql.SQLException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import jpos.JposException;

import jpos.events.DataEvent;
import jpos.events.DataListener;

import org.apache.log4j.Logger;

import th.cash.env.KKMEnv;
import th.cash.fr.FrException;
import th.cash.fr.doc.*;
import th.cash.fr.err.FrErrHandler;
import th.cash.fr.err.StateFrEvent;
import th.cash.fr.err.StateFrListener;
import th.cash.fr.state.StateA;
import th.cash.model.*;
import th.cash.ui.util.UserDlg;

import th.cash.model.FixedDsc;
import th.cash.ui.sale.dsc.FDscSelDialog;

import th.cash.card.*;

import th.common.db.VRow;
import th.common.db.VectorDataModel;
import th.common.ui.table.TableScroller;
import th.common.ui.table.TableSearch;
import th.common.util.RMath;

import th.cash.dev.CustDisplay;

import th.cash.ui.paym.CardInputDialog;
import th.cash.fr.state.*;
/**
 * ���� ������������ ����, ������� ������
 * @author lazarev
 */
public class SalePanel extends JPanel implements ListSelectionListener, DataListener, StateFrListener, UpdateLocker {

    // **************************    
    private Font def_font, labels_font, values_font, large_val_font, edit_font, sum_font, quan_font, table_font;
    
    private JLabel jl_urole;                      // �������� ������ ����
    private JLabel jl_user;                       // ��� ������������
    private JLabel jl_text_weight, jl_weight;     // ��� (�����/��������)
    private JLabel jl_text_quan, jl_quantity;     // ���������� (�����/��������)
    
    private JLabel jl_oper_type;                  // ��������� ����
    
//    private JLabel jl_text_check_num1, jl_text_check_num2, jl_check_num;    // ����� ���� 
    private JLabel jl_text_check_num, jl_check_num; // ����� ���� (�����/��������)
    private JLabel jl_dsc_percent;                // ������� ������ �� ��� (������ ��������)
    private JLabel jl_text_sum1, jl_text_sum2, jl_sum;  // ����. ���� (����� � 2 ������) (����� �� �������)
    
    // ��� �������������� ������
    private JLabel jl_text_code, jl_code;         // ��� ������  
    private JLabel jl_text_gname, jl_gname;       // �������� ������
    private JLabel jl_text_bc, jl_barcode;        // ����� - ���

//    private JLabel jl_text_dsc_percent;
//    private JLabel jl_text_dsc_sum, jl_dsc_sum;      // ����� ������
//    private JLabel jl_text_full_sum, jl_full_sum;     // ����� ��� ������
    private JLabel jl_text_fsum_and_dsc, jl_fsum_and_dsc; // ������ ������ ����� ���� � ������ �� ���


    // ��� ������ ���������
    private JLabel jl_knum;
    private JLabel jl_ver, jl_bld;
    
    private JLabel jl_log_roll;
    private JLabel jl_check_roll;
    private JLabel jl_eklz;
    
    private JLabel jl_msg;          // ��������� � ������ ���������
    private JLabel jl_time;         // ��� ����������� ���������� �������


    private InputLabel jl_input;     // ���� �����

    // ************  ��������� ����������� ��������� **************
    private Double quan        = null;             // ���������� 
    private Integer check_num  = null;             // �����   
//    private Double dsc_pc      = null;             // ������� ������ �� ���
    private FixedDsc fixed_dsc = null;
    private Double weight      = null;             // ��� ������� �������
    private Double itog        = null;             // ����. ���� ����   
    

    private NumberFormat inp_quan_fmt, out_quan_fmt, money_fmt, dsc_pc_fmt;
    
    private JTable table;
    private TbColorSettins tb_colors;
    
    private DocCashe cashe, view;
    private VectorDataModel model;

    private Window owner;

    private FDoc curDoc = null;

    
    private KKMEnv cashEnv;

    // ---------------------------
    private boolean doc_locking = false;   // ��� ���������� ��������� ���� (UpdateLocker)
    
    
    private final static String[] COL_NAMES = {" ","N","������������", "���-��", "����", "�����"};
    private final static Class[] COL_CLASSES = new Class[]{Integer.class, Integer.class, String.class, Double.class, Double.class, Double.class};
    private final static int[] COL_WIDTH = new int[] {24, 36, 800, 100, 120, 140};

    private AScannerSwitcher scan_switch; // ������������� ������� �������� �/�

    private TimerRunnable timer;

    private FrErrHandler ehandler;
    private ProcessShow pr_show;

    protected final static String UI_LOG_PREF = "UI"; 
    private Logger log = Logger.getLogger(UI_LOG_PREF + '.' + this.getClass().getName());

    private JDialog gmDialog = null;
    private GMSelectPanel gmPanel = null;

    private FDscSelDialog fdsc_dialog = null;

    // ��������� ������ ������������ ����
    private boolean prev_eklz_full = false;

    /** Creates a new instance of SalePanel */
    public SalePanel() {
      init();
    }

    // ������������� ������� ��� ��������� ��������� ����������
    private void initFonts()
    {
      JLabel tmp = new JLabel();

      // UI default
      def_font = tmp.getFont();

      labels_font    = def_font.deriveFont((float)14);
      values_font    = def_font.deriveFont((float)20);
      edit_font      = def_font.deriveFont((float)82);
     
      large_val_font = def_font.deriveFont((float)26);
      quan_font      = def_font.deriveFont((float)40);

      sum_font       = def_font.deriveFont((float)32);
//      table_font = new Font("Dialog", Font.BOLD, 13);
      table_font     = def_font.deriveFont(Font.BOLD, 14);
    }

    private void installFonts()
    {
      jl_urole.setFont(labels_font);
      jl_user.setFont(values_font);

      jl_text_weight.setFont(labels_font);
      jl_weight.setFont(values_font);

      jl_text_quan.setFont(quan_font);
      jl_quantity.setFont(quan_font);

      jl_input.setFont(edit_font);

      jl_oper_type.setFont(sum_font);
  //      jl_text_check_num.setFont(labels_font);
//      jl_text_check_num1.setFont(labels_font);
//      jl_text_check_num2.setFont(labels_font);

      jl_text_check_num.setFont(labels_font);
      jl_check_num.setFont(sum_font);
      
//      jl_text_dsc_percent.setFont(labels_font);

      jl_dsc_percent.setFont(large_val_font);

  //      jl_text_sum.setFont(labels_font);
//      jl_text_sum1.setFont(labels_font);
//      jl_text_sum2.setFont(labels_font);

      jl_sum.setFont(sum_font);

      table.setFont(table_font);
    }

    public void relativeFontsSize(Dimension ss)
    {
      JLabel tmp = new JLabel();

      // UI default
      def_font = tmp.getFont();

      labels_font    = def_font.deriveFont(cFH(ss.height, 14));
      values_font    = def_font.deriveFont(cFH(ss.height, 20));
      edit_font      = def_font.deriveFont(cFH(ss.height, 82));
     
      large_val_font = def_font.deriveFont(cFH(ss.height, 26));
      quan_font      = def_font.deriveFont(cFH(ss.height, 40));

      sum_font       = def_font.deriveFont(cFH(ss.height, 32));
//      table_font = new Font("Dialog", Font.BOLD, 13);
      table_font     = def_font.deriveFont(Font.BOLD, cFH(ss.height, 14));

      installFonts();
    }

    private float cFH(int h, int dfs)
    {
      return ((float)h * dfs / 480);
    }
    
    // init UI interface
    private void init()
    {

      // controls & labels
      jl_urole = new JLabel();
      jl_user = new JLabel();
        
      jl_text_weight = new JLabel("�����");
      jl_weight = new JLabel();
        
      jl_text_quan = new JLabel("X");
      jl_quantity = new JLabel();
        
      jl_input = new InputLabel();
        
      jl_oper_type = new JLabel();

      jl_text_check_num = new JLabel("N");
//      jl_text_check_num1 = new JLabel("�����");
//      jl_text_check_num2 = new JLabel("����");
      jl_check_num = new JLabel();
        
//      jl_text_dsc_percent = new JLabel();
      jl_dsc_percent = new JLabel();
        
//      jl_text_sum = new JLabel("����.����");
      jl_text_sum1 = new JLabel("����.");
      jl_text_sum2 = new JLabel("����");
      jl_sum = new JLabel();


      // ����������� ������� � ����
      jl_text_code = new JLabel("���:");
      jl_code = new JLabel();         // ��� ������ 
      jl_text_gname = new JLabel("�����:");
      jl_gname = new JLabel();        // �������� ������
      jl_text_bc = new JLabel("�/�:");
      jl_barcode = new JLabel();      // ����� - ���
//      jl_text_dsc_sum = new JLabel("����� ������:");
//      jl_dsc_sum = new JLabel();      // ����� ������
//      jl_text_full_sum = new JLabel("����� ��� ������:");
//      jl_full_sum = new JLabel();     // ����� ��� ������
      jl_text_fsum_and_dsc = new JLabel("�����/������:");
      jl_fsum_and_dsc = new JLabel();     // ����� ��� ������
        
      // ��� ������ ���������
      jl_knum = new JLabel();
      jl_ver = new JLabel();
      jl_bld = new JLabel();
      
      jl_log_roll = new JLabel();
      jl_check_roll = new JLabel();
      jl_eklz = new JLabel();
      jl_msg = new JLabel();
      jl_time = new JLabel();
        
      // table init
      cashe = new DocCashe();
      view = new DocCashe();
      model = new VectorDataModel(cashe, view, COL_NAMES, COL_CLASSES);
      model.setEditable(false);
      table = new JTable(model);

      // RENDERERS        
      TableColumnModel tcm = table.getColumnModel();

      
      TypeRenderer rtype  = new TypeRenderer(Position.CANCEL_POS);
      StringColoredRenderer rid    = new StringColoredRenderer();
      StringColoredRenderer rname  = new StringColoredRenderer();
      DoubleColoredRenderer rquan  = new DoubleColoredRenderer(3);
      DoubleColoredRenderer rmoney = new DoubleColoredRenderer(2);

//      Color n_fg = table.getForeground();
//      Color n_bg = table.getBackground();
//      Color s_fg = Color.white;
//      Color s_bg = Color.darkGray;
      tb_colors = new TbColorSettins(table.getForeground(), table.getBackground(), Color.white, Color.darkGray);

//      rtype.setNormSelColors(n_fg, n_bg, n_fg, n_bg);
//      rid.setNormSelColors(n_fg, n_bg, n_fg, n_bg);
//      rname.setNormSelColors(n_fg, n_bg, s_fg, s_bg);
//      rquan.setNormSelColors(n_fg, n_bg, s_fg, s_bg);
//      rmoney.setNormSelColors(n_fg, n_bg, s_fg, s_bg);

      rtype.setColors(tb_colors);
      rid.setColors(tb_colors);
      rname.setColors(tb_colors);
      rquan.setColors(tb_colors);
      rmoney.setColors(tb_colors);

      rid.setHorizontalAlignment(rid.RIGHT);
      
      tcm.getColumn(0).setCellRenderer(rtype);
      tcm.getColumn(1).setCellRenderer(rid);
      tcm.getColumn(2).setCellRenderer(rname);
      tcm.getColumn(3).setCellRenderer(rquan);
      tcm.getColumn(4).setCellRenderer(rmoney);
      tcm.getColumn(5).setCellRenderer(rmoney);

        
      TableScroller scroller = new TableScroller(table);
      scroller.setRelatedColWidth(COL_WIDTH);     

      // 
      initFonts();
      installFonts();

      table.setRowHeight(table.getRowHeight() + 3);
//      table.setSelectionBackground(Color.darkGray);
//      table.setSelectionForeground(Color.white);
        
      // layout & place elements
      JPanel topPanel, botPanel;
      JPanel p, p1;

      topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

      // row 1
      p = new JPanel(new RelFlowLayout(new int[]{3, 2, 3}));
        
      p1 = new JPanel();
      p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
      p1.setBorder(BorderFactory.createEtchedBorder());
      p1.add(jl_urole);
      p1.add(jl_user);
      jl_urole.setAlignmentX(CENTER_ALIGNMENT);
      jl_user.setAlignmentX(CENTER_ALIGNMENT);

      p.add(p1); 
        
      p1 = new JPanel();
      p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
      p1.setBorder(BorderFactory.createEtchedBorder());
      p1.add(jl_text_weight);
      p1.add(jl_weight);

      p.add(p1);
        
      p1 = new JPanel();
//      p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
//      p1.setBorder(BorderFactory.createEtchedBorder());
//      p1.add(jl_quantity);
      p1.setLayout(new BorderLayout(0, 3));  
      p1.add(jl_quantity, BorderLayout.WEST);
      p1.add(jl_text_quan, BorderLayout.EAST);
      p1.setBorder(BorderFactory.createEtchedBorder());

      p.add(p1);
        
      topPanel.add(p);
        
      // row 2
      p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
      p.add(jl_input);

      topPanel.add(p);
        
      p = new JPanel(new RelFlowLayout(new int[]{9, 5, 5, 10}));
       
      p1 = new JPanel();
      p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
      p1.setBorder(BorderFactory.createEtchedBorder());
      jl_oper_type.setAlignmentX(CENTER_ALIGNMENT);
      jl_oper_type.setAlignmentY(CENTER_ALIGNMENT);
      p1.add(jl_oper_type);
        
      p.add(p1);
        
      p1 = new JPanel();
//      p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
//      p1.setBorder(BorderFactory.createEtchedBorder());
//      p1.add(jl_text_check_num);
//      p1.add(jl_check_num);

      p1.setLayout(new BorderLayout(3, 0));
      p1.setBorder(BorderFactory.createEtchedBorder());
      p1.add(jl_text_check_num, BorderLayout.WEST);
      JPanel p2;
//        p2 = new JPanel();
//        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
//        jl_text_check_num1.setAlignmentX(CENTER_ALIGNMENT);
//        jl_text_check_num2.setAlignmentX(CENTER_ALIGNMENT);
//        p2.add(Box.createVerticalStrut(2));
//        p2.add(jl_text_check_num1);
//        p2.add(Box.createVerticalGlue());
//        p2.add(jl_text_check_num2);
//        p2.add(Box.createVerticalStrut(2));
//        p2.setAlignmentY(CENTER_ALIGNMENT);
//      p1.add(p2, BorderLayout.WEST) ;

      p1.add(jl_check_num, BorderLayout.EAST);


             
      p.add(p1);
        
      p1 = new JPanel();
      p1.setLayout(new BorderLayout());
      p1.setBorder(BorderFactory.createEtchedBorder());
//      p1.add(jl_text_dsc_percent);
      p1.add(jl_dsc_percent, BorderLayout.WEST);
      
      p.add(p1);


      p1 = new JPanel();
//      p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
//      p1.setBorder(BorderFactory.createEtchedBorder());
//      p1.add(jl_text_sum);
//      p1.add(jl_sum);

      p1.setLayout(new BorderLayout(5 , 0));
      p1.setBorder(BorderFactory.createEtchedBorder());
        //JPanel p2 = new JPanel();
        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        jl_text_sum1.setAlignmentX(CENTER_ALIGNMENT);
        jl_text_sum2.setAlignmentX(CENTER_ALIGNMENT);

        p2.add(Box.createVerticalStrut(2));
        p2.add(jl_text_sum1);
        p2.add(Box.createVerticalGlue());
        p2.add(jl_text_sum2);
        p2.add(Box.createVerticalStrut(2));
        p2.setAlignmentY(CENTER_ALIGNMENT);
      p1.add(p2, BorderLayout.WEST);
      p1.add(jl_sum, BorderLayout.EAST);
      

      p.add(p1);

      topPanel.add(p);

      botPanel = new JPanel();
      botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.Y_AXIS));
      p1 = new JPanel();
      FormLayout fl;
      CellConstraints cc;

//       fl = new FormLayout("l:p, 2dlu, l:max(50dlu;pref), 4dlu, l:p, 2dlu, l:m:g", "p, 2dlu, p");

       fl = new FormLayout("l:p, 1dlu, l:max(50dlu;pref), 2dlu, l:p, 1dlu, l:m:g, 2dlu, l:p, 1dlu, r:max(80dlu;pref)", "p, 2dlu, p");

       p1.setLayout(fl);
       cc = new CellConstraints();

       p1.add(jl_text_code, cc.xy(1, 1));
       p1.add(jl_code, cc.xy(3, 1));

       p1.add(jl_text_bc, cc.xy(5, 1));
       p1.add(jl_barcode, cc.xy(7, 1));

       p1.add(jl_text_fsum_and_dsc, cc.xy(9, 1));
       p1.add(jl_fsum_and_dsc, cc.xy(11, 1));

       p1.add(jl_text_gname, cc.xy(1, 3));
       p1.add(jl_gname, cc.xywh(3, 3, 9, 1));
       
       botPanel.add(p1);

       fl = new FormLayout("p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 4dlu, l:m:g, 4dlu, r:p", "p");
       p1 = new JPanel(fl);

       p1.add(jl_knum,       cc.xy(1, 1));
       p1.add(jl_ver,        cc.xy(3, 1));
       p1.add(jl_bld,        cc.xy(5, 1));
       p1.add(jl_log_roll,   cc.xy(7, 1));
       p1.add(jl_check_roll, cc.xy(9, 1));
       p1.add(jl_eklz,       cc.xy(11, 1));
       p1.add(jl_msg,        cc.xy(13, 1));
       p1.add(jl_time,       cc.xy(15, 1));
       
       p1.setBorder(BorderFactory.createEtchedBorder());
       botPanel.add(p1);

       setLayout(new BorderLayout());        
       add(topPanel, BorderLayout.NORTH);
       add(scroller, BorderLayout.CENTER);
       add(botPanel, BorderLayout.SOUTH);


       initFormat();

       // �������
       // new !!! added 15.02.2008
       initActions();

       table.addKeyListener(jl_input);

       table.getSelectionModel().addListSelectionListener(this);
       table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

       timer = new TimerRunnable(jl_time);
       new Thread(timer, "UIClock").start();

       //
       ehandler = new FrErrHandler()
       {
         public Logger getLog() { return log; }
         public void showError(String msg) { SalePanel.this.showError(msg, null); }
         public void showError(Exception ex) { SalePanel.this.showError(null, ex);}
         public void showError(String msg, Exception ex) { SalePanel.this.showError(msg, ex);}
         public boolean showQuestion(String msg) {return UserDlg.showQuestion(SalePanel.this, msg);}
       };

       ehandler.addStateFrListener(this);

       pr_show = new ProgressFrame("�������������");

       ehandler.setProgress(pr_show);
    }

    // ���������, �� ������� ��������� ��������
    public void setOwner(Window w)
    {
      owner = w;

      addKeyListener(jl_input); 

      owner.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e) { 
          handleActionEvent(new ActionEvent(SalePanel.this, ActionEvent.ACTION_PERFORMED, EXIT));
        }
      });
      // added 19.05.2010
//      owner.addWindowListener(scan_switch);
    }



    private void initFormat()
    {
      DecimalFormatSymbols ds = new DecimalFormatSymbols(Locale.ENGLISH);
      ds.setDecimalSeparator('.');
      ds.setGroupingSeparator('\'');
      ds.setGroupingSeparator(' ');

      inp_quan_fmt = new DecimalFormat();
      ((DecimalFormat)inp_quan_fmt).setDecimalFormatSymbols(ds);
      inp_quan_fmt.setMaximumFractionDigits(3);
      inp_quan_fmt.setGroupingUsed(true);

      out_quan_fmt = new DecimalFormat();
      ((DecimalFormat)out_quan_fmt).setDecimalFormatSymbols(ds);
      out_quan_fmt.setMinimumFractionDigits(3);
      out_quan_fmt.setGroupingUsed(true);

      money_fmt = new DecimalFormat();
      ((DecimalFormat)money_fmt).setDecimalFormatSymbols(ds);
      money_fmt.setMinimumFractionDigits(2);
      money_fmt.setGroupingUsed(true);

      dsc_pc_fmt = new DecimalFormat();
      ((DecimalFormat)dsc_pc_fmt).setDecimalFormatSymbols(ds);
      dsc_pc_fmt.setMaximumFractionDigits(2);
      dsc_pc_fmt.setGroupingUsed(false);
      
    }


    /**
     * ***********************************************************************
     *                              ACTIONS 
     * 
     */
    // ������������� ����� ������
    private final static String OPEN           = "open";
    private final static String REG_QUANTITY   = "regQuantity";
    private final static String BY_CODE        = "byCode";
    private final static String BY_BARCODE     = "byBarcode";
    private final static String STORNO_POS     = "stornoPos";
    private final static String REPEAT_POS     = "repeatPos";
    private final static String CLOSE_CHECK    = "closeCheck";
    private final static String CANCEL_CHECK   = "cancelCheck";
    private final static String RETURN_CHECK   = "returnCheck";

    private final static String IN_PAY         = "inPay";
    private final static String OUT_PAY        = "outPay";
    private final static String BANK_REPORT    = "bankReport";
    private final static String Z_REPORT       = "zReport";
    private final static String X_REPORT       = "xReport";
    private final static String RESET          = "reset";
    
    private final static String LOCK           = "lock";
    private final static String OPEN_MONEY_BOX = "openMoneyBox";
    private final static String PR_ITOG        = "prItog";
    private final static String BN_PAY         = "bnPay"; 
    private final static String FIXED_DSC      = "fixedDsc";     // 08.10.09
    private final static String CANCEL_DSC     = "cancelDsc";
    private final static String EXIT           = "exitProgram";

    // ������� - actionCommand, actionKey, actionName
    private final static String[][] ACTION_DEF = 
    {
      { OPEN,           null,            "Open" },
      { REG_QUANTITY,   "MULTIPLY",      "����������"},
      { BY_CODE,        "F5",            "����������� �� ����"},
      { BY_BARCODE,     "F6",            "����������� �� �/�"},
      { STORNO_POS,     "SUBTRACT",      "������"},
      { REPEAT_POS,     "control Y",     "������ �������"},
      { CLOSE_CHECK,    "ENTER",         "�������� ����"},
      { CANCEL_CHECK,   "control F5",    "������ ����"},
      { RETURN_CHECK,   "alt F2",        "�������"},
      { IN_PAY,         "shift F7",      "��������"},
      { OUT_PAY,        "shift F8",      "�������"},
      { BANK_REPORT,    "F9",            "����� �� ����. ������"},
      { Z_REPORT,       "control F9",    "Z-�����"},
      { X_REPORT,       "shift F6",      "X-�����"},
      { RESET,          "F12",           "�����"},
      { LOCK,           "alt F1",        "����."},
      { OPEN_MONEY_BOX, "control F6",    "�������� ��������� �����"},
      { PR_ITOG,        "DIVIDE",        "����. ����"},
      { BN_PAY,         "control ENTER", "��� ������ 2"},
      { CANCEL_DSC,     "alt F9",        "������ ������"},
      { FIXED_DSC,      "shift F2",      "����.������"},
      { EXIT,           "ESCAPE",        "�����"}
    };


    private void initActions()
    {
       // ������� ������ ��� ������� ������ ��������� �������
       Properties kpr = new Properties();
       try
       {
         kpr.load(getClass().getResourceAsStream("/hot_keys.cfg"));
         if (log.isDebugEnabled())
           log.debug("Use keyboard configuration from file");
       } catch (Exception ex) {
         kpr.clear();
         if (log.isDebugEnabled())
           log.debug("Use default keyboard configuration");
       }

      // ��������� ��������� ��������� ��� �������
      table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
      table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");


      for (int i = 0; i < ACTION_DEF.length; i++)
        createInstallAction(i, kpr);
      
    }

    // ������� ������� �� ������� ACTION_DEF
    private Action createInstallAction(int index, Properties keycfg)
    {
      
      String[] adef = ACTION_DEF[index];

      String cmd = adef[0];
      String sks = keycfg.getProperty(cmd, adef[1]);
      String name = adef[2];

      Action res = new AbstractAction(name) { 
        public void actionPerformed(ActionEvent e) 
        {
        // added 14.02.2008 (temporary ...)
//          synchronized (SalePanel.this) { handleActionEvent(e); }
          handleActionEvent(e);
        }
      };

      res.putValue(AbstractAction.ACTION_COMMAND_KEY, cmd);

      KeyStroke ks = sks == null ? null : KeyStroke.getKeyStroke(sks);

      if (ks != null)
      {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, cmd);
        getActionMap().put(cmd, res);
      }

      return res;
    }

    // ����� �� ����� �������
    private int getCmdIndex(String cmd)
    {
      int res = -1;
      int i = 0, len = ACTION_DEF.length;
      while (i < len && !cmd.equals(ACTION_DEF[i][0])) i++;

      if (i < len) res = i;

      return res;
    }




    /**************************************************************************
     * Handle errors
     */
//    private void showError(String msg)
//    {
//      log.error(msg);
//      UserDlg.showError(this, msg);
//    }
//
//    private void showError(Exception ex)
//    {
//      log.error(ex.getMessage(), ex);
//      UserDlg.showError(this, ex.getMessage());
//    }


    private final static int ERR_INCORRECT_STATE = -1000;
    // ����������� ������
    // msg
    private void showError(String msg, Exception ex)
    {  // modified 12.04.10
      String stext = null;
      
      if (msg != null)  log.error(stext = msg);
      if (ex != null)
      {
        String s = (ex instanceof FrException) ? "[" + ((FrException)ex).getErrorCode() + "]" : "";
        if (stext == null) stext = s + ex.getMessage();
        log.error(s + ex.getMessage(), ex);
      }

      if (stext == null) stext = "������";
      
//      SwingUtilities.invokeLater(new TextMsgRunnable(stext));  
      UserDlg.showError(this, stext);
    }

    // ����� ����� ��������� ������
    // ��������� - ����������� ���������� ������ ...
    // �� ����������� ������ �� ��� �� ������ ����������� ���������� ������
    private boolean handleError(Exception ex)
    {
      return handleError(null, ex);
    }
//     + MSG_SALE_MODE_NOT_AVAIL
    private boolean handleError(String msg, Exception ex)
    {
      boolean res = true;
      
      if (ex instanceof UserException) 
      {
        showError(ex.getMessage(), null);  
        
      }else
      
      if (ex instanceof PermException) showError(ex.getMessage(), null);   else
      
      if (ex instanceof FrException) {
        boolean state_ok = ((FrException)ex).getErrorCode() != ERR_INCORRECT_STATE;
        showError(null, ex);  res = state_ok;
      } else

      if (ex instanceof SQLException) { 
        showError("������ ������ � ��!" + '\n' + MSG_SALE_MODE_NOT_AVAIL, ex); 
        res = false; 
      } 

      else
        showError(null, ex);

      return res;
    }

    class TextMsgRunnable implements Runnable
    {
      private String msg;
      
      TextMsgRunnable(String msg)
      {
        this.msg = msg;
      }

      public void run()
      {
        UserDlg.showError(SalePanel.this, msg);
      }
    }
//
//    class ExcRunnable implements Runnable {
//
//      private Exception ex;
//      ExcRunnable(Exception ex)
//      {
//        this.ex = ex;
//      }
//
//      public void run()
//      {
//        SalePanel.this.showError(null, ex);
//      }
//    }
//
//    class MsgRunnable implements Runnable {
//
//      private String msg;
//      
//      MsgRunnable(String msg)
//      {
//        this.msg = msg;
//      }
//
//      public void run()
//      {
//        SalePanel.this.showError(msg, null);
//      }
//    }

    /**
     * *************************************************************************
     * *************************************************************************
     * *************************************************************************
     */
    // ��������� ������� ������� �/�
    // DataListener implementation
    public void dataOccurred(DataEvent p0)
    {
      try
      {
        if (log.isDebugEnabled()) log.debug("SalePanel.dataOccurred()");
        
        byte[] scan_data;
        synchronized (cashEnv.getScanner())
        {
          scan_data = cashEnv.getScanner().getScanDataLabel();
          //scan_switch.setScannerEnabled(false);
        }

        if (bc_accept)
          SwingUtilities.invokeLater(new RegBarcode(new String(scan_data)));

      } catch (JposException ex)
      {
        //SwingUtilities.invokeLater(new ExcRunnable(ex)); 12.04,10
        SwingUtilities.invokeLater(new TextMsgRunnable(ex.getMessage()));
      }
    }

    // Runnable ��� ��������� ������� ������ �/�  
    class RegBarcode implements Runnable{

      private String bs;
      RegBarcode(String s)
      {
        bs = s;
      }

      public void run() 
      {
        bc_accept = false;
        do_reg_by_barcode(bs, false);
        //scan_switch.setScannerEnabled(true);  // �������� ������� �� ��������
        bc_accept = true;
      }
    }

    private void initScanSwitcher()
    {
      scan_switch = new AScannerSwitcher(cashEnv.getScanner(), this);
    }

    public AScannerSwitcher getScannerSwitcher()
    {
      return scan_switch;
    }

    
    /**
     * *************************************************************************
     */


    /**
     * *************************************************************************
     * *************************************************************************
     * *************************************************************************
     */

    // TODO
    // ����� ����� ���������� ���������� ����������� ��������� !!!
    private void initFiscalDoc(int type) throws Exception
    {
      // *************************************
      // TODO temporary !!!
      // ��������� ���������� ������������ !!!
      // removed 14.02.2008
      //cashEnv.getSprUpdater().work();
    
      if (curDoc != null) return ;

      curDoc = createFDoc(type);

      if (curDoc != null)
      {
        itog = null;
        displayItog();
        
        displayCheckNum();
        jl_oper_type.setText(curDoc.getTypeName());
      }
    }

    private FDoc createFDoc(int type) throws Exception
    {
      FDoc res = null;
      switch (type)
      {
        case FDoc.FD_SALE_TYPE : res = new SaleCheck(); break;
        case FDoc.FD_RETURN_TYPE : res = new ReturnCheck(); break;
        case FDoc.FD_PAY_IN_TYPE : res = new InPay(); break;
        case FDoc.FD_PAY_OUT_TYPE : res = new OutPay(); break;
        case FDoc.FD_ZREPORT_TYPE : res = new ZReport(); break;
        case FDoc.FD_XREPORT_TYPE : res = new XReport(); break;
        default : res = null;
      }

      if (res != null)
      {

        int dn = initDocNum();

        res.setDocNum(dn);

        res.setCashNum(cashEnv.getCashNumber());

        res.open();

        // added 08.02.2008 
        synchronized (this) { doc_locking = true; } // ������������ ��� ����� ���������
      }
      return res;
    }
    

    private Double parseInput(String s) throws ParseException
    {
      Double d = null;
      Number res = inp_quan_fmt.parse(s);
      if (res instanceof Double) 
        d = (Double)res;
      else
        if (res instanceof Long)
          d = new Double(((Long)res).doubleValue());
      return d;
    }


    // * *************************************************************************

    // ����� ����� ��� �������� ����������� ���������
    private void closeFiscalDoc(FDoc doc) throws Exception
    {
      if (doc == null) return;

      // �������� ��������� ����� ������� ���������
      ehandler.checkStateBeforeClosing(cashEnv.getFiscalPrinter(), doc);

      // ��������� ��������
      if ( !(doc instanceof Check && ((Check)doc).isCanceled())) 
        doc.close(getCurUser());

      // �������� � ��������� ���������� ����� � ����. ����������
      ehandler.printDoc(cashEnv.getFiscalPrinter(), doc);  // ��� �������� ����� ���� �������

      // ����� ���������� ��� ������ � � ������
      doc.save(cashEnv.getTrConnection(), cashEnv.getSprModel().getSettings().isLogTransToFile());

      // added 08.02.2008
      synchronized (this) { doc_locking = false; }
    }

    /**
     * *************************************************************************
     * *************************************************************************
     * *************************************************************************
     */

    private boolean bc_accept = false;
    private boolean in_handle = false; 
    // ��������� ���� �������, ������������ ����� ActionEvent 
    // �������� ���� �� ������� OPEN, �������� - �� ������ exit
    protected synchronized void handleActionEvent(ActionEvent e)
    {
//    in_handle - ������� ��������� ������ ... � ������ ������ ���������������, � ����� - �����
      if (in_handle) return;
      in_handle = true;

      String cmd = e.getActionCommand();

      if (!OPEN.equals(cmd))
      {
        bc_accept = false;  // by flag
        //scan_switch.disable(); // testing !

        // disable scanner  !!!!!!
        if (LOCK.equals(cmd) || EXIT.equals(cmd))
        {
          scan_switch.setScannerEnabled(false);
          scan_switch.removeDataListener();
        }
      }

      if (log.isDebugEnabled())
      {
        log.debug("SalePanel.handleActionEvent() start");
        log.debug("Action command = " + cmd);       
      }

      boolean exit = false;

      if (OPEN.equals(cmd)) exit = do_open(); else
      if (REG_QUANTITY.equals(cmd)) do_reg_quantity(); else
      if (BY_CODE.equals(cmd)) do_reg_by_code(); else
      if (BY_BARCODE.equals(cmd)) do_reg_by_barcode(jl_input.getValueAsInt(), true); else
      if (STORNO_POS.equals(cmd)) do_storno_pos(); else
      if (REPEAT_POS.equals(cmd)) do_repeat_pos(); else
      if (CLOSE_CHECK.equals(cmd)) do_close_check(false); else
      if (CANCEL_CHECK.equals(cmd)) do_cancel_check(); else
      if (RETURN_CHECK.equals(cmd)) do_return_check(); else
      
      if (IN_PAY.equals(cmd)) do_in_pay(); else
      if (OUT_PAY.equals(cmd)) do_out_pay(); else
      if (BANK_REPORT.equals(cmd)) do_bank_report(); else
      if (Z_REPORT.equals(cmd)) do_zreport(); else
      if (X_REPORT.equals(cmd)) do_xreport(); else
      if (RESET.equals(cmd)) do_reset_input(); else

      if (OPEN_MONEY_BOX.equals(cmd)) do_open_moneybox(); else

      if (PR_ITOG.equals(cmd)) do_show_promitog(); else

      if (BN_PAY.equals(cmd)) do_close_check(true); else

      if (FIXED_DSC.equals(cmd)) do_fixed_dsc(); else

      if (CANCEL_DSC.equals(cmd)) do_cancel_dsc(); else

      if (LOCK.equals(cmd)) exit = true; else 
      if (EXIT.equals(cmd)) 
        exit = UserDlg.showQuestion(this, "����� �� ������ �����������?"); 
      else ;
      
      
      if (log.isDebugEnabled()) log.debug("SalePanel.handleActionEvent() stop");

      if (exit) 
      {
        if (owner.isVisible()) owner.hide();
      } else
      {
        bc_accept = true;

        // enable scanner !!!!!!!
        if (OPEN.equals(cmd)|| EXIT.equals(cmd))
        {
          scan_switch.addDataListener();
          scan_switch.setScannerEnabled(true);
        }
        //scan_switch.enable(); // testing !
        
      } 
      
      in_handle = false;
    }


    // 
    public boolean firstLogin()
    {
      handleActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OPEN));
      return owner.isVisible();
    }

    // ���������� �������� 
    private void displayCheck(FDoc doc)
    {
      if (doc != null)
      {
        if (doc instanceof Check)
        {
          // clear view
          view.clear();
          cashe.clear();
          
          Vector ch_pos = ((Check)doc).getPositions();
          if (ch_pos != null && ch_pos.size() > 0)
          {
            VRow row;
            Position p;
            int i = 0;
            while (i < ch_pos.size())
            {
              p = (Position)ch_pos.get(i);
              row = view.createNewRow_();
              view.initRowByPos(row, p); 
              model.insert(row);
                
              i++;
            }

            view.renumber();
            model.fireTableDataChanged();
            TableSearch.selectRow(table, view.size() - 1);
          } 
        }          
        // display check info
        itog = new Double(doc.getSum());
        //displayItog();
        check_num = new Integer(doc.getDocNum());
        displayCheckNum();
        jl_oper_type.setText(doc.getTypeName());

      } else
      {
        itog = new Double(0);
        // clear view
        view.clear();
        cashe.clear();
        model.fireTableDataChanged();
        jl_oper_type.setText(null);
      }

      displayItog();
    }
    

    private void do_reg_quantity()
    {

      if (log.isDebugEnabled()) log.debug("do_reg_quantity()");

      try
      {
     
        Double new_quan = parseInput( jl_input.getValue() );

        double q = new_quan.doubleValue();  

        if (q < 0.001)
          throw new UserException("���������� ������ ���� ������ ����");

        Settings set = cashEnv.getSprModel().getSettings();
        
        double max_quan = (isFracQuan(new_quan)) ? set.getMaxDoubleQuan() : set.getMaxIntQuan();

        if (q > max_quan)
          throw new UserException("�������� ���������� ������� ������");


        quan = new_quan;
        displayQuantity();
 
      } catch (Exception ex)
      {
        handleError(ex);
      } finally 
      {
        jl_input.clear();
      }
    }

    // ����� �� ������� ���������� ������� � �����
    private void showOnDisplay(String str1, double d1, String str2, double d2)
    {
      CustDisplay cd = cashEnv.getCustomerDisplay();
      if ( cd != null)
      {
        String s1, s2;
        StrFormat sfmt = new StrFormat();
        s1 = sfmt.getSEString(str1, ' ', money_fmt.format(d1), cd.getStrLen());
        s2 = sfmt.getSEString(str2, ' ', money_fmt.format(d2), cd.getStrLen());
        cd.setText(true, s1, s2);
      }
    }

    // ����� ��������� � �������� � ����� ������
    private void showInSaleTextOnDisplay()
    {
      CustDisplay cd = cashEnv.getCustomerDisplay();

      if (cd != null)
      {
        Settings set = getCashEnv().getSprModel().getSettings();
        StrFormat fmt = new StrFormat();
        cd.setText(true,  
           fmt.getCenterString(set.getAwayCdText1(), ' ', cd.getStrLen()), 
           fmt.getCenterString(set.getAwayCdText2(), ' ', cd.getStrLen()));
      }
    }


    /**
     * *************************************************************************
     * �������� ���������� ������������ �� ��������� ��������
     * 
     */
    // ����� ����� ���� �� ������ � ������ ���������� ������� ��� �������� (Check)
    private CheckPerm getCheckPerm(FDoc doc)
    {
      if (doc != null && !(doc instanceof Check)) return null;
      return doc == null || doc != null && doc instanceof SaleCheck ? 
        getCurUser().getRole().getSaleCheckPerm() : getCurUser().getRole().getReturnCheckPerm();
    }

    private void checkPayPermissions(int doc_type) throws UserException
    {
      boolean avail = false;
      switch (doc_type)
      {
        case FDoc.FD_PAY_IN_TYPE : avail = getCurUser().getRole().isInPay(); break;
        case FDoc.FD_PAY_OUT_TYPE : avail = getCurUser().getRole().isOutPay(); break;
      }
      if (!avail) throw new PermException();
    }

    private void checkReportPermissions(int doc_type) throws UserException
    {
      boolean avail = false;
      switch (doc_type)
      {
        case FDoc.FD_ZREPORT_TYPE : avail = getCurUser().getRole().isZReport(); break;
        case FDoc.FD_XREPORT_TYPE : avail = getCurUser().getRole().isXReport(); break;
      }
      if (!avail) throw new PermException();
    }

    private void checkBankReportPerm() throws UserException
    {
      boolean avail = false;

      avail = getCurUser().getRole().isCloseDay(); 

      if (!avail) throw new PermException();
    }

    private void checkSaleRule(GoodMain gm) throws UserException
    {
      Vector gr = cashEnv.getSprModel().getSettings().getGoodRules();

//      log.info("checkSaleRule  gm = " + gm.getGoodId() + "  rule = " + gr);

      if (gr != null && gr.size() > 0)
      {
        String emsg = null;
        int i = 0;
        SecSaleRule rule;
          
        while ( i < gr.size())
        {
          rule = (SecSaleRule)gr.get(i++);
          if (!rule.isSaleEnabled(gm))  // ����� �� ������ ������� ����� ������� ������
            throw new UserException("[" + gm.getGoodId() + "] " + gm.getName() + '\n' + rule.getComent());
        }
      }
    }
    // *************************************************************************
    

    /**
     * ����������� ������ �� ����
     */
    private void do_reg_by_code()
    {

      if (log.isDebugEnabled()) log.debug("do_reg_by_code()");

      try
      {
        // ������� ����� �� �������� � �����
        CheckPerm cp = getCheckPerm(curDoc);

        // �������� ����� �� �������� ���� ������� 
        if (!cp.isOpen()) throw new PermException();

        // �������� ����� ����� ���� ������
        if (!cp.isTypeCode()) throw new PermException();
        
        // ����� �� ���� �� jl_input
        String in_str = jl_input.getValueAsInt();

        SearchData sd = new SearchData(in_str);

        Vector res = null;

        try {
          res = cashEnv.getSprModel().searchGood(sd, true);
        } catch (NumberFormatException ex)
        {
          throw new UserException("������������ ��� ������");
        }

        // ���� �� ������� - ������
        if (res == null || res.size() == 0)
          throw new UserException("����� �� ������ �� ���� '"+ sd.getGoodId() + "'", true);
        

        GoodMain g = (GoodMain)res.get(0);
        boolean frac_quan = isFracQuan(quan);

        if (frac_quan && !g.getWeightControl().booleanValue())
          throw new UserException("�������� ���� �������� ���������� ��� ������");


        checkSaleRule(g);
          
        Tax t = null;

        // ���� ����� ��� ������
        if (g.getNalGroup() != null)
          t = cashEnv.getSprModel().searchTax(g.getNalGroup());

        // ��� ���� (������� / �������)
        int doc_type = curDoc == null ? FDoc.FD_SALE_TYPE : curDoc.getTypeId();
        
        // �������������� �������
        Integer pos_type = doc_type == FDoc.FD_RETURN_TYPE ? Position.SALE_RET_POS : Position.SALE_POS;
        Position p = new Position(pos_type, quan, g, null, t);

        // ��������� ����� ����
        doSumControl((Check)curDoc, p);        

        // �������������� �������� �������
        if (curDoc == null) initFiscalDoc(FDoc.FD_SALE_TYPE);

        // ����� � ���� � ���������� 
        regInDbAndDisplayPos(p, g.getWeightControl().booleanValue());

        t = null; g = null; sd = null; // 

      } catch (Exception ex)
      {
        handleError(ex);
        jl_input.clear(); 
      }
      
    }

    // �������� �� ���������� ����� ����
    private void doSumControl(Check doc, Position p) throws UserException
    {
      double _sum;
      _sum = doc == null ? 0 : doc.getSum();
      if (_sum + p.getSum().doubleValue() > cashEnv.getSprModel().getSettings().getMaxCheckSum())
        throw new UserException("����� ���� ������� ������");
    }

    // ������������ ������� � ������� (��) � ���������� � ����������
    private void regInDbAndDisplayPos(Position p, boolean pos_weight) throws Exception
    {
      // ����������� ����� �������
      int new_num = view.getNumPos();
      p.setNum(new Integer(new_num));

      // ��������� ������� � ����
      ((Check)curDoc).addPos(cashEnv.getTrConnection(), p, getCurUser());

      // update table
      VRow row = view.createNewRow_();
      view.initRowByPos(row, p); 
      model.insert(row);
      int sz = view.size(); // ���������� �����
      model.fireTableRowsInserted(sz - 1, sz - 1);
      TableSearch.selectRow(table, sz - 1);

      // ���� ����
      itog = new Double(curDoc.getSum());
      displayItog();

      // ��� - ��� �����������, ����� �� GoodMain.isWeightControl()
      weight = pos_weight ? p.getQuantity() : null;
      displayWeight();

      do_reset_input(); 

      // ���. ��� �������
      showOnDisplay(p.getGname(), p.getSum().doubleValue(), "����. ����", curDoc.getSum());
      
    }

    private boolean isFracQuan(Double d)
    {
      if (d == null) return false;
      return Math.round(d.doubleValue()) != d.doubleValue();
    }

    private Double calkBcQuan(Double koef) 
    {
      return new Double(RMath.round(quan.doubleValue() * koef.doubleValue(), 3));
    }

    /**
     * ����������� ������ �� �����-����
     * typed - ������� ����, ��� �/� ������ �������
     */
    private void do_reg_by_barcode(String bc, boolean typed)
    {

      if (log.isDebugEnabled()) log.debug("do_reg_by_barcode()");

      try
      {
        CheckPerm cp = getCheckPerm(curDoc);

        // �������� ����� �� �������� ���� ������� 
        if (!cp.isOpen()) throw new PermException();

        // �������� ����� ������/����������� �/�
        if (!(typed && cp.isTypeBc() || !typed && cp.isScanCode())) throw new PermException();


        // ����� �� �/�
        SearchData sd = new SearchData(bc);

        Vector res = cashEnv.getSprModel().searchGood(sd, false);

        // ���� ������ �� ������� - exception
        if (res == null || res.size() == 0) 
          throw new UserException("����� �� ������ �� �/� '" + bc + "'", true);


        // ����� � ������ �����������
        GoodMain g;
        Barcode b = null;
        Tax t = null;

        // 12.01.09 (����������� ����� ������, ���� �� �/� ������� 2 � �����) 
//        log.info("res.size()=" + res.size());
        if (res.size() == 1)
          g = (GoodMain)res.get(0);
        else
          g = userSelectGM("�����-��� '" + bc + "', �������� �����:", res); // ����� ������ �� ����, ���� �/� ���������

        if (g != null)          
        {

          // changed 28.02.2008 (�������� ������� �/�)
          // �������� ���������� ��� �������� ������
          boolean frac_quan = sd.isWeight() || isFracQuan(quan);

          if (frac_quan && !g.getWeightControl().booleanValue())
            throw new UserException("�������� ���� �������� ���������� ��� ������");

          checkSaleRule(g);


          // ������������ � �����������
          if (sd.isWeight()) 
          {
            // ������� �����
            quan = sd.getQuantity();
          } else
          {
            // ������� �����
            if (sd.getBarcodes() != null) 
            {
              b = (Barcode)sd.getBarcodes().get(0);
              quan = calkBcQuan(b.getKoef());
            }
          }

          // ���� ����� ��� ������
          if (g.getNalGroup() != null)
            t = cashEnv.getSprModel().searchTax(g.getNalGroup());

          int doc_type = curDoc == null ? FDoc.FD_SALE_TYPE : curDoc.getTypeId();

          // ������������ ������� � ���������
          Integer pos_type = doc_type == FDoc.FD_RETURN_TYPE ? Position.SALE_RET_POS : Position.SALE_POS;
          Position p = new Position(pos_type, quan, g, b, t);

          // �������� ����� ����
          doSumControl((Check)curDoc, p);

          // ������� �������� - �������, ���� ������ �� ���������������� 
          if (curDoc == null) initFiscalDoc(FDoc.FD_SALE_TYPE);

          // ����� � ���� � ����������
          regInDbAndDisplayPos(p, g.getWeightControl().booleanValue());
        } else do_reset_input();

        t = null; g = null; b = null; sd = null;//
      } 
      catch (Exception ex)
      {
        handleError(ex);
        if (typed) jl_input.clear(); // clear input (User exception)
      }
    }

    /**
     * ������������ ��� ��������
     */
    private void do_return_check()
    {
      if (log.isDebugEnabled()) log.debug("do_return_check()");

      if (curDoc != null) return;

      try
      {
        if (getCurUser().getRole().getReturnCheckPerm().isOpen())
        {
          initFiscalDoc(FDoc.FD_RETURN_TYPE);
        } else
          throw new PermException();
      } catch (Exception ex)
      {
        handleError(ex);
      }
      
    }

    private boolean sumToLow(Double d) { return d == null ? true : sumToLow(d.doubleValue()); }
    private boolean sumToLow(double d) { return Math.abs(d) < 0.01;  }

    /**
     * ������� ��� �������/��������
     *   bn_key_pressed - ������� ����, ��� ��� ����� ��������� ������� �� �������
     *   false - ������ ��������� �� ������ ����� + ���������� (���������), 
     */
    private PaymentSelector paySelector = null;
    private CardActivator cardActivator = null;

    // ������� ������������� ������� ... 
    private PaymentSelector getPaymentSelector() throws Exception
    {
      if (paySelector == null)
      {
        Class c = Class.forName("th.cash.ui.paym.PaymentSelectDialog");
        paySelector = (PaymentSelector)c.getConstructors()[0].newInstance(new Object[] {owner, "control Enter", log});
      }
      return paySelector;  
    }

    private CardActivator getCardActivator() throws Exception
    {
      if (cardActivator == null)
      {
        Class c = Class.forName("th.cash.ui.paym.AbgCardActivator");
        cardActivator = (CardActivator)c.newInstance();
        cardActivator.setParams(cashEnv.getSprModel().getSettings().getAllSet());
      }
      return cardActivator;
    }
    
    private void do_close_check(boolean bn_key_pressed)
    {
      if (log.isDebugEnabled()) log.debug("do_close_check()");

      if (curDoc == null) return;
      
      try
      {
        CheckPerm cp = getCheckPerm(curDoc);

        if (cp.isClose())
        {
          if (sumToLow(curDoc.getSum())) 
            throw new UserException("�������� �������� ���� ���������");
        
//          Double in_sum = parseInput(jl_input.getValue());
          // changed 21.01.08
          Double in_sum = checkInputSum(false);    // �����, ��������� �������������


          
          // ��������� �� ����� �������� �� �������
          boolean money_nal = !sumToLow(in_sum);

          boolean credit_enabled = cashEnv.getSprModel().getSettings().isCreditPayEnabled()
            && getCurUser().getRole().isBn();
          //log.info("credit_enabled=" + credit_enabled);

          boolean fin_closing = true; // ��������� �������� ���� 

          // ���� ��������� ������ ���������� �� �������
          if (bn_key_pressed)
          {
            fin_closing = credit_enabled && getPaymentSelector().selPaymentTypes(curDoc.getSum(), 0, curDoc.getSum());
            if (fin_closing)
            {
              curDoc.setNalSum(getPaymentSelector().getNalSum());
              curDoc.setBNSum(getPaymentSelector().getBnSum());
            }
          }
          else
          {
        
            if (money_nal)
            {
              if (curDoc.getSum() > in_sum.doubleValue())
              {  // �������� �� ������� 
                if (credit_enabled)
                { // ������� ����� ��������� �� ������� 
                  fin_closing = getPaymentSelector().selPaymentTypes(
                    curDoc.getSum(), in_sum.doubleValue(), curDoc.getSum() - in_sum.doubleValue());
                  if (fin_closing)
                  {
                    curDoc.setNalSum(getPaymentSelector().getNalSum());
                    curDoc.setBNSum(getPaymentSelector().getBnSum());
                  }
                } else
                  throw new UserException("�� ������� ����� ��� ������");
              }
              else
                // �������� ������ ��� ����� ���� -> �����
                curDoc.setNalSum(in_sum.doubleValue());
            } else // ��� �����
              curDoc.setNalSum(curDoc.getSum());
          }


          // ����� � �������� �������������, ��������� � fin_closing � curDoc 
          if (!fin_closing) return; // TODO - ���������, ��������� �� ...

          Object btr_data = null;
          if (curDoc.getBnSum() > 0)
          {
            CardInputDialog cid = new CardInputDialog(JOptionPane.getFrameForComponent(this));
            cid.setLocationRelativeTo(cid.getOwner());
            cid.show();
            if (cid.isSelected())
            {

              pr_show.setTitle("������ �����������");
              pr_show.setText("����������� ������ �����������...");
              pr_show.show();

              btr_data =  getCardActivator().requestSum(cid.getTrackData(), curDoc.getBnSum());

              pr_show.hide();
              
              if (btr_data == null)
                throw new UserException("�������� ����������� ������ �� ���������!"); else
              if (btr_data instanceof String)
                throw new Exception((String)btr_data);
              ((SaleCheck)curDoc).setAuthCode(btr_data.toString());
            } else
              throw new UserException("�������� ���� ��������");
          }


          // TODO - �������� ���� ������� ������ ������
          closeFiscalDoc(curDoc);

          Thread.currentThread().sleep(500);

          if (curDoc.getBnSum() > 0)
          {
            getCardActivator().saveBankTrans(btr_data);

            getCardActivator().printBankCheck(cashEnv.getFiscalPrinter(), btr_data);
            
          }
          



          if (((Check)curDoc).isCanceled())
          {
            jl_oper_type.setText("��� �������");
            if (cashEnv.getCustomerDisplay() != null)
              cashEnv.getCustomerDisplay().setText(true, "", "��� �������");
          } else
          {
            // ����������� 
            if (curDoc.getChange() > 0/*money_nal*/)        
            {
              jl_input.setText(money_fmt.format(curDoc.getChange()));
              jl_oper_type.setText("�����");
            } else
            {
              jl_input.setText(money_fmt.format(curDoc.getSum()));
              jl_oper_type.setText("�����");
            }

            // TODO - �� ������ ���� �����
            showOnDisplay("�����", curDoc.getChange(), "����. ����", curDoc.getSum());
          }

          // ����� ������ ��� ������ ����� ������
          jl_input.setClearTyping();
        
          //do_reset_input();
          do_clear_check_det();

          itog = null;
          fixed_dsc = null;
          displayItog();
          weight = null;
          displayWeight();
          curDoc = null;
          displayDscPc();
          
            
        } else throw new PermException();

      } catch (Exception ex)
      {
        handleError(ex);
      }
      
    }

    private void do_cancel_check()
    {
      if (log.isDebugEnabled()) log.debug("do_cancel_check()");

      if (curDoc == null) return;

      try
      {
        if (curDoc instanceof Check) 
        {
          CheckPerm cp = getCheckPerm(curDoc);

          if (cp.isCancel())
          {
            ((Check)curDoc).cancel(getCurUser());
            closeFiscalDoc(curDoc);

            do_reset_input();
            do_clear_check_det();

            jl_oper_type.setText("������ ����");
          
            curDoc = null;
          } else throw new PermException();
        }
      } catch (Exception ex)
      {
        handleError(ex);
      }
    }


    /**
     * *********************************************************************
     * ���������� ��������: �������� / �������
     * *********************************************************************
     */ 
    private void do_in_pay() { _do_pay(FDoc.FD_PAY_IN_TYPE); }

    private void do_out_pay() { _do_pay(FDoc.FD_PAY_OUT_TYPE); }
    
    private void _do_pay(int dt)
    {
      if (curDoc != null) return;
      
      try
      {
        checkPayPermissions(dt);

        jl_input.clearOld();

        Double in_sum = checkInputSum(true);
        if (in_sum != null)
        {
          initFiscalDoc(dt);
          curDoc.setSum(in_sum.doubleValue());

          closeFiscalDoc(curDoc);

          // display
          jl_input.setText(money_fmt.format(curDoc.getSum()));
          jl_input.setClearTyping();
        }
      } catch (Exception ex)
      {
        handleError(ex);
      } finally
      {
        curDoc = null;
      }
    }


    /**
     * *********************************************************************
     * ����� �� ���������� ������
     * *********************************************************************
     */

     private void do_bank_report()
     {
       try
       {
         checkBankReportPerm();
         if (!UserDlg.showQuestion(this, "��������� �������� ����� �� ���������� ������?")) return;

         pr_show.setTitle("�������� ����� �� ������");
         pr_show.setText("����������� ������ ...");
         pr_show.show();


         Settings set = getCashEnv().getSprModel().getSettings();
         String res = getCardActivator().makeReport(getCashEnv().getTrConnection(), 
           set.getCashNumber().intValue(), getCurUser().getCode().intValue(), set.isLogTransToFile());

         pr_show.hide();


         if (res == null)
           UserDlg.showInfo(this, "�������� ����� �� ���������� ������\n������� ���������!");
         else
           throw new Exception(res);
       } catch (Exception ex)
       {
         pr_show.hide();
         handleError(ex);
       }
     }

    /**
     * *********************************************************************
     * ������: Z-����� / X-�����
     * *********************************************************************
     */ 
    // Z-����� �������������� � "�����" ���������� curDoc !
    private void do_zreport() { _do_report(FDoc.FD_ZREPORT_TYPE); }

    // X-�����, ���� ��� curDoc
    private void do_xreport() { _do_report(FDoc.FD_XREPORT_TYPE); }

    private void _do_report(int dt)
    {
      try
      {
        // �������� ����������
        checkReportPermissions(dt);

        boolean is_zrep = dt == FDoc.FD_ZREPORT_TYPE;

        // ������������� ������������
        if (!UserDlg.showQuestion(this, is_zrep ? "����� ����� � ��������?" : "����� ����� � ��� �������?")) return;

        // �������������� �������� 
        FDoc rep = createFDoc(dt);

        displayCheckNum();
        jl_oper_type.setText(rep.getTypeName());

        if (is_zrep)
        {
          pr_show.setTitle(rep.getTypeName());
          pr_show.setText("��������� ������ � ��������...");
          pr_show.show();
        }

        // ������
        closeFiscalDoc(rep);
        rep = null;

        // ������� ������������� ������� ����� Z-������
        String sync_res = null;
        // ** changed 23.08.11
        if (is_zrep && ehandler.getFrState().getModeNum() == StateConst.M_CLOSED_SHIFT) 
        {
          // ������������� ������� � ����������
          sync_res = ehandler.timeSynchronization(cashEnv.getFiscalPrinter(), cashEnv.getSprModel().getSettings().getTimeDifSec());
        }

        // ��������� ����� � ��������� ��������� (���� ������� ����)
        reinitDocNum(); 

        if (is_zrep) pr_show.hide();

        // ���� ���� ������ ������������� �������, �� ���������� ���������
        // TODO - ����������� ������!
        if (sync_res != null) 
          throw new UserException(sync_res);
//  12.04.10      showError(sync_res);

      } catch (Exception ex)
      {
        pr_show.hide();
        handleError(ex); // showError(ex);
      } 

      // ��������� ��� ������ �� ���������� ������ 
      try
      {
        Settings set = getCashEnv().getSprModel().getSettings();
        if (set.isCreditPayEnabled())
        {
          Date tr_sh_date = getCardActivator().getBankShiftDate();
          if (tr_sh_date != null)
            //handleActionEvent(new ActionEvent(SalePanel.this, ActionEvent.ACTION_PERFORMED, BANK_REPORT));
            do_bank_report();
          
        }
      } catch (Exception ex)
      {
        handleError(ex); 
      } 
    }

    // ������������ � ���������, ������� ��� ������ �����, ������ ����� ����������
    private void reinitDocNum() throws Exception
    {
      if (curDoc != null)
      {
        /// �������� � ������� ���� ...
        int new_num = initDocNum();
        curDoc.setDocNum(new_num);
        displayCheckNum();
        jl_oper_type.setText(curDoc.getTypeName());
      }
    }


    /**
     * *********************************************************************
     * C�����. �������� ������ � ���� �������
     * *********************************************************************
     */
    private void do_storno_pos()
    {

      if (log.isDebugEnabled()) log.debug("do_storno_pos()");

      if (curDoc == null) return;

      try 
      {
//        if (curDoc instanceof SaleCheck)
//        {
          if (getCheckPerm(curDoc).isStorno())
          {
            int sr = table.getSelectedRow();
            if (sr >= 0 && sr < view.size())
            {
              // ����� ����� ��������� ����� ������������ ��� ���������� ������!!!
        
              VRow row = (VRow)view.get(sr);
              Integer type = (Integer)row.get(DocCashe.OPER_TYPE);
              if (!Position.CANCEL_POS.equals(type))
              {
                Position p = (Position)row.get(DocCashe.POS_DATA);

                // ���������� � ���������
                ((Check)curDoc).stornoPos(cashEnv.getTrConnection(), p, getCurUser());

                // ��������� ��� ����������
                p.setType(Position.CANCEL_POS);
                row.set(DocCashe.OPER_TYPE, Position.CANCEL_POS);
                p.setNum(null);
                row.set(DocCashe.NUM_POS, null);
            
                view.renumber();
             
                model.fireTableRowsUpdated(0, view.size() - 1);

                itog = new Double(curDoc.getSum());
                displayItog();

                // ���. ��� �������
                showOnDisplay("������ - " + p.getGname(), p.getSum().doubleValue(), "����. ����", curDoc.getSum());
            
              }
            }
          } else throw new PermException();
//        }
      } catch (Exception ex)
      {
        handleError(ex);
      } 
    }

    /**
     * ����� �������
     */
    private void do_repeat_pos()
    {
      if (curDoc == null) return;

      try
      {
        CheckPerm perm = getCheckPerm(curDoc);
        if (perm.isRepeat()) // �������� ����������
        {
          // ���������� ������� �������
          Position prev = null;
          int num_pos = view.size();
          
          if (num_pos > 0)
          {
            VRow row = (VRow)view.get(num_pos - 1);
            prev = (Position)row.get(DocCashe.POS_DATA );
            if (Position.CANCEL_POS.equals(prev.getType())) prev = null;
          }

          if (prev != null) 
          {
            // ����� ���������� �������
            Position p = new Position(prev);

            // �������� ����� ����
            doSumControl((Check)curDoc, p);

            // ������ � ���� � �����������
            regInDbAndDisplayPos(p, false);
          }
          
        } else throw new PermException();
        
      } catch (Exception ex)
      {
        handleError(ex);
      }
    }



    /**
     * ������� �������� ����, �������� ������� ������� � ����������� �����������
     */
    private void do_open_moneybox()
    {
      try
      {
        if (getCurUser().getRole().isMoneybox())
        {
          cashEnv.getFiscalPrinter().openMoneyBox();
          if (cashEnv.getSprModel().getSettings().isMoneyboxMark())
          {
            int check_n = check_num == null ? 0 : check_num.intValue();
            DbConverter.saveTransaction(cashEnv.getTrConnection(), 
              Transaction.createOpenMoneyBox(cashEnv.getCashNumber(), check_n, getCurUser().getCode().intValue()), 
              cashEnv.getSprModel().getSettings().isLogTransToFile());
          }
        } else throw new PermException();
      } catch (Exception ex)
      {
        handleError(ex);
      }
    }

    /**
     * ���������� ������������� ���� ����
     */ 
    private void do_show_promitog()
    {
      if (itog != null) {
        jl_input.setText(money_fmt.format(itog));
        jl_oper_type.setText("����. ����");
      }
    }

    /**
     * ������� ������������� ������ �� ��� 
     */
    private void do_fixed_dsc()
    {
      if (log.isDebugEnabled()) log.debug("do_fixed_dsc()");  // TODO remove Info

      if (cashEnv.getSprModel().getSettings().isDiscountsEnabled())
      {
        try
        {
          if (!getCurUser().getRole().isFixedDsc()) throw new PermException();
        
          if (curDoc != null && curDoc instanceof Check /*SaleCheck*/ && fixed_dsc == null)
          {

            fdsc_dialog = new FDscSelDialog((javax.swing.JFrame)owner);


            fixed_dsc = fdsc_dialog.refresh(cashEnv.getSprModel().getFDsc()) ;

            if (fixed_dsc != null)
            {
              log.debug("Set check discount " + fixed_dsc.getDscValue());
              ((Check)curDoc).setFixedCheckDiscount(cashEnv.getTrConnection(), fixed_dsc, getCurUser());
              itog = new Double(curDoc.getSum());
              displayDscPc();
              displayItog();
            }
          }
        } catch (Exception ex)
        {
          handleError(ex);
        }
      }
    }

    /**
     * �������� ������ 
     */
    private void do_cancel_dsc()
    {
      if (log.isDebugEnabled()) log.debug("do_cancel_dsc()");  // TODO remove Info

      try
      {
        if (!getCurUser().getRole().isFixedDsc()) throw new PermException();

        if (curDoc != null && curDoc instanceof SaleCheck && fixed_dsc != null)
        {

          fixed_dsc = null;
          log.debug("Cancel check discount !" );
          ((Check)curDoc).cancelCheckDsc();
          itog = new Double(curDoc.getSum());
          displayDscPc();
          displayItog();
        }
      } catch (Exception ex)
      {
        handleError(ex);
      }
    }

    // *************************************************************************
    // ��������� ��������/������������� �����������, �������� ����
    // ���������� �� firstLogin() ����� ����� ���������� �������
    private boolean do_open()
    {
      boolean err = false;
      boolean exit = false;
      do
      {
        try
        {
          // ��������� ������� �������������� ���������  
          curDoc = DbConverter.createFDocFromBuf(cashEnv.getTrConnection(), cashEnv.getSprModel());

          // ������������� �������� ���������� (������������ ProcessShow)
          ehandler.initFiscalPrinterDevice(cashEnv.getFiscalPrinter());

          // ��������� ���������� ����� �������������
          //StateA state = ehandler.getFrState();

          // �������� ���������, �������� �� ��� "�����������" ���� ��������
          // ���� �� ���������� ������ ���������� - sr - �������� ������
          String sr = ehandler.checkAndFixState(cashEnv.getFiscalPrinter());

          if (sr == null) 
          {

            // ���� ���� ������ �������� � ����� ��������� ����
            if (curDoc != null && curDoc instanceof Check)  
            {
              // �������� ���
              if (ehandler.getFrState().getModeNum() == 8)
              {
                ((Check)curDoc).cancel(getCurUser());
                //cashEnv.getFiscalPrinter().calcelCheck();
                curDoc.save(cashEnv.getTrConnection(), cashEnv.getSprModel().getSettings().isLogTransToFile());
                curDoc = null;
                            
              } else
              {
                int dn = initDocNum();
                curDoc.setDocNum(dn);
              }
            } 

            // ����������� ����� ������������� ������ ����������
            fixed_dsc = null;
            displayDscPc();
            // ���������� ������� ���
            displayCheck(curDoc);


            // set user name to print in check
            cashEnv.getFiscalPrinter().setUserName(getCurUser().getName());

            // UI - display user name
            displayUser();

            
            err = false;
            owner.show();
          

            ehandler.initFRTables(cashEnv.getFiscalPrinter());
            //state = cashEnv.getFiscalPrinter().stateRequest();

          } else 
            throw new FrException(sr , ERR_INCORRECT_STATE);

          showInSaleTextOnDisplay();

          // ����� ��������� � ��������� ���������
          if (getCashEnv().getCustomerDisplay() != null)
          {
            Settings set = getCashEnv().getSprModel().getSettings();
            StrFormat fmt = new StrFormat();
            getCashEnv().getCustomerDisplay().setText(true,  
               fmt.getCenterString(set.getAwayCdText1(), ' ', 20), 
               fmt.getCenterString(set.getAwayCdText2(), ' ',20));
          }

            
        } catch (Exception ex)
        {
          err = true;

//          String emsg = ex.getMessage();
//          
//          if (ex instanceof SQLException)
//          {
//            exit = true;
//            emsg = "������ ������ � ����� ������!" + MSG_SALE_MODE_NOT_AVAIL;
//          }
//
//          if (ex instanceof FrException)
//            if (((FrException)ex).getErrorCode() == -1000)
//            {
//              exit = true;
//              emsg = ex.getMessage() + MSG_SALE_MODE_NOT_AVAIL;
//            } 
//
//          showError(emsg, ex);

          exit = !handleError(null, ex);
  
          if (!exit)
            exit = !UserDlg.showQuestion(owner, "��������� ������� ����������� ��?");
          
        }
      } while (err && !exit);

      return exit;
    }

    private final static String MSG_SALE_MODE_NOT_AVAIL = "\n����� ����������� ������ ����������!";



    // ��������� ������������ ��������� �����
    private Double checkInputSum(boolean check_zero) throws UserException
    {
      Double res = null;
      try
      {
        Double in_sum = parseInput(jl_input.getValue());
        
        if (check_zero && in_sum.doubleValue() < 0.01)
          throw new UserException("����� ������ ���� ������ ����");
//          showError("����� ������ ���� ������ ����");
        else if (in_sum.doubleValue() > cashEnv.getSprModel().getSettings().getMaxMoneySum()) 
          throw new UserException("����� ������� ������");
//          showError("����� ������� ������");
        else 
          res = in_sum;

      } catch (ParseException ex)
      {
        throw new UserException("������������ �������� ��� �����");
//        showError("������������ �������� ��� �����");
      }

      return res;      
    }


    private void do_reset_input()
    {
      quan = new Double(1);
      displayQuantity();
      jl_input.clear();
    }

    private void do_clear_check_det()
    {
      view.clear();
      cashe.clear();
      model.fireTableDataChanged();
    }


    // *************************************************************************
    private String truncForFr(String s)
    {
      return s == null ? null : s.substring(0, Math.min(s.length(), 40));
    }

    // *************************************************************************

    private void displayQuantity()
    {
      jl_quantity.setText( quan == null ? null : out_quan_fmt.format(quan) );
    }

    private void displayWeight()
    {
      jl_weight.setText( weight == null ? null : out_quan_fmt.format(weight) );
    }

    private void displayCheckNum()
    {
      jl_check_num.setText( check_num == null ? null : check_num.toString() );
    }

    private void displayDscPc()
    {

      String s = null;

      if (fixed_dsc != null)
      {
        boolean is_dsc = fixed_dsc.getIsDsc() != null && fixed_dsc.getIsDsc().booleanValue();
        boolean is_sum = fixed_dsc.getIsSum() != null && fixed_dsc.getIsSum().booleanValue();
        s = (is_dsc ? "��" : "��") + " " + dsc_pc_fmt.format(fixed_dsc.getDscValue()) + (is_sum ? "���" : "%");
      }

      jl_dsc_percent.setText(s);  
    }

    private void displayItog()
    {
      jl_sum.setText( itog == null ? null : money_fmt.format(itog) );
      // �� ������ ������ ���������� ����� ��� ������ � ������
      // ���� ������� �� ������� �� ������� � �������� ������ ��� ����
      String s = null;
      if (curDoc != null && curDoc instanceof Check)
        s = money_fmt.format(((Check)curDoc).getCheckPosSum()) + "/" + 
            money_fmt.format(((Check)curDoc).getCheckDscSum());
      jl_fsum_and_dsc.setText(s);      
    }

    private void displayUser()
    {
      User u = getCurUser(); 
      jl_urole.setText( u == null ? null : u.getRole().getName());
      jl_user.setText( u == null ? null : u.getName() );
    }

    // *************************************************************************

    public KKMEnv getCashEnv()
    {
      return cashEnv;
    }

    public void setCashEnv(KKMEnv env)
    {
      cashEnv = env;
      ehandler.setEnv(env);
      initScanSwitcher();
      initData();
      displayData();
      // version info
      jl_knum.setText("K:" + String.valueOf(env.getCashNumber()));
      jl_ver.setText("V:" + (env.getVersion() == null ? "?" : env.getVersion()));
      jl_bld.setText("B:" + (env.getBuild() == null ? "?" : env.getBuild()));

      // added 12.02.2008
      cashEnv.getSprUpdater().setLocker(this, true);
    }

    private User getCurUser()
    {
      return getCashEnv().getCurrentUser();
    }

    // *************************************************************************

    // ������������� ���� ���������
    public void initData()
    {
      quan = new Double(1);
      weight = null;
//      dsc_pc = null; 
      itog = null;
      fixed_dsc = null;

      curDoc = null;
    }

    public void displayData()
    {
      displayQuantity();
      displayCheckNum();
      displayDscPc();
      displayItog();
      displayWeight();
      displayUser();
    }

    public int initDocNum() throws Exception
    {
      int res = cashEnv.getFiscalPrinter().getLastDocNum() + 1;
      check_num = new Integer(res);
//      displayCheckNum();
      return res;
    }

  // ListSelectionListener implementation
  public void valueChanged(ListSelectionEvent e)
  {
    int ind = table.getSelectedRow();
    VRow row = null;
    if (ind >= 0 && ind < view.size())
     row = (VRow)view.get(ind);
    displayInInfoPanel(row);
  }

   // �������������� ������ (�������� ��� ������� ������� )
   private void displayInInfoPanel(VRow row)
   {
     if (row == null)
     {
       jl_code.setText(null);
       jl_gname.setText(null);
       jl_barcode.setText(null);
//       jl_full_sum.setText(null);
//       jl_fsum_and_dsc.setText(null);
       
     } else
     {
       Object val;
       val = row.get(DocCashe.POS_DATA);
       Position p = (Position)val;

       jl_code.setText(p.getGoodId() == null ? null : p.getGoodId().toString());

       val = row.get(DocCashe.GNAME);
       jl_gname.setText(val == null ? null : val.toString());

       jl_barcode.setText(p.getBarcode() == null ? null : p.getBarcode());

//       jl_full_sum.setText(p.getSum() == null ? null : money_fmt.format( p.getSum() ));
//       jl_fsum_and_dsc.setText(null);
     }
   }
  

  // StateFrListener implementation
  // ���������� ������ ��������� ... 
  public void stateReceived(StateFrEvent e)
  {
    StateA state = e.getState();
    jl_check_roll.setText("��");
    boolean wsc = state.isFFrRollCheck();
    boolean osc = state.isFFrOptSensorCheck();
    jl_check_roll.setForeground( wsc && osc ? Color.white : Color.red );

    jl_log_roll.setText("��");
    boolean wsl = state.isFFrRollLog();
    boolean osl = state.isFFrOptSensorLog();
    jl_log_roll.setForeground( wsl && osl ? Color.white : Color.red );
     

    boolean has_eklz = state.isFFrEKLZ();
    boolean eklz_full = state.isFFrEKLZFull();
    jl_eklz.setText( has_eklz ? "����" : "" );
    jl_eklz.setForeground( eklz_full ? Color.red : Color.white );


    // ���������, 30.03.09, ���������� ����������� ���������� ����, ��������������
    // �������������� ���� �����, � ������ ��������� ��������� ��
    if (cashEnv.getSprModel().getSettings().isDiagnEKLZFull() && has_eklz)
    {
      if (!prev_eklz_full && eklz_full)
      {
        try
        {
          DbConverter.saveTransaction(cashEnv.getTrConnection(), 
            Transaction.createEKLZFull(cashEnv.getCashNumber()), 
            cashEnv.getSprModel().getSettings().isLogTransToFile());
          prev_eklz_full = eklz_full;
        } catch (SQLException ex)
        {
          log.warn(ex.getMessage());
        }
      }
    }
  }


  // UpdateLocker implementation
  public boolean isLocked()
  {
    return doc_locking;
  }

  // added 12.01.09 
  // ������������ �������� ����� �� ������
  // res - ������ ��������� GoodMain
  private GoodMain userSelectGM(String msg, Vector res)
  {
    if (gmDialog == null)
    {
      gmDialog = new JDialog((javax.swing.JFrame)owner, "����� ������", true);

      gmPanel = new GMSelectPanel(gmDialog);
      gmPanel.initTable(tb_colors, table_font);

      gmDialog.setContentPane(gmPanel);
      Dimension d = getSize();
      gmDialog.setSize(d.width * 90 / 100, d.height * 90 / 100);
      gmDialog.setLocationRelativeTo(owner);
    }
    gmPanel.refreshData(msg, res);

    gmDialog.show();


    return gmPanel.getSelectedGM();
  }
    
}
