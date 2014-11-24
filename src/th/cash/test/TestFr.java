package th.cash.test;

import th.cash.fr.table.FrTable;
import th.cash.fr.*;
import th.cash.fr.state.*;
import th.cash.fr.doc.*;

import th.cash.ui.util.UserDlg;

import java.util.Date;

import java.util.*;

public class TestFr 
{
  public final static byte CHECK_TYPE = 0;
  public final static byte ZERO_BYTE = 0;

    private final static long CPRINT_TIMEOUT = 500;

  public static void main(String[] args)
  {
    
    try
    {

//        byte b = (byte)0xFE;
//        System.out.println("b="+b);
//        byte[] bf = new byte[4];
//        int i = b;
//        FrUtil.intToBytes(bf,i, 4, 0);
//        System.out.println("i=" + i);
//        System.out.println(Hex.byteArrayToHexString(bf)); 
//
//        int ival = 255;
//        System.out.println("ival=" + ival);
//    
//        byte[] data = new byte[1];//{(byte)0x00, (byte)0xFE};
//        FrUtil.intToBytes(data, ival, 1, 0);
//        System.out.println(Hex.byteArrayToHexString(data)); 
//
//
//        ival = FrUtil.intFromBytes(data, 0 , 1);
//        System.out.println("res=" + ival);
        
//      FrDrv fr = new FrDrv("COM1", 100);
//      fr.setDebug(true);
//      fr.init(115200);
//
//      
////      fr.init();
//      fr.open();
//      fr.setOperPwd(30);
//
//      fr.stateRequest();
//      StateA state = new FullStateFr();
//      state.decodeParams(fr.getReplyParams()); 
//      state.printAll();
      
//      fr.continuePrint();

//   System.out.println(System.currentTimeMillis());
//   System.out.println(new Date());
//
//   System.out.println(java.util.Calendar.getInstance().getTimeZone());


//now//sun.util.calendar.ZoneInfo[id="Europe/Moscow",offset=10800000,dstSavings=3600000,useDaylight=true,transitions=130,lastRule=java.util.SimpleTimeZone[id=Europe/Moscow,offset=10800000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=2,startMonth=2,startDay=-1,startDayOfWeek=1,startTime=7200000,startTimeMode=1,endMode=2,endMonth=9,endDay=-1,endDayOfWeek=1,endTime=7200000,endTimeMode=1]]
//win//sun.util.calendar.ZoneInfo[id="Europe/Moscow",offset=10800000,dstSavings=3600000,useDaylight=true,transitions=130,lastRule=java.util.SimpleTimeZone[id=Europe/Moscow,offset=10800000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=2,startMonth=2,startDay=-1,startDayOfWeek=1,startTime=7200000,startTimeMode=1,endMode=2,endMonth=9,endDay=-1,endDayOfWeek=1,endTime=7200000,endTimeMode=1]]

      int dd, mm, yy;

//      System.out.print("day=");
//      dd = System.in.r();
//
//      System.out.print("month=");
//      mm = System.in.read();
//
//      System.out.print("year=");
//      yy = System.in.read();

//      dd = Integer.parseInt(args[0]);
//      mm = Integer.parseInt(args[1]);
//      yy = Integer.parseInt(args[2]);
      
      

      FrKPrinter pr = new FrKPrinter("/dev/ttyS2", 100);
      pr.init();


      StateA state = pr.stateRequest();

      state.printAll();

      pr.makeClrZReport();

        Thread.currentThread().sleep(CPRINT_TIMEOUT); 

        // после печати документа, проверяем состояние (подрежим)
      
        int submode;
        do {

          state = pr.stateRequest();

          submode = state.getSubModeFr();
          System.out.println("State : " + state.getModeNum() + "  submode = " + submode);

          switch (submode)
          {
            case StateConst.SM_PAPER_OK : break;

            // если нет бумаги - проверяем датчики чековой и контрольной ленты            
            case StateConst.SM_ACTIV_NO_PAPER : case StateConst.SM_PASS_NO_PAPER : 
              String s = null; //_checkOpticalSensors(state);
              if (s != null)  System.out.println("Нет бумаги");;
            break;

            // если устройство ждет команду продолжения печати -
            case StateConst.SM_WAIT_REPEAT :          
              pr.continuePrint(); 
              Thread.currentThread().sleep(CPRINT_TIMEOUT); 
            break;

            // на остальные подрежимы - ожидание и повторный запрос состояния
            default :  
              Thread.currentThread().sleep(CPRINT_TIMEOUT);
            /*
            case 4 : case 5 :  // если идет печать, то ждем, ждем ...
              Thread.currentThread().sleep(CPRINT_TIMEOUT);
            break;
            */
          }
              
        } while (submode != StateConst.SM_PAPER_OK);  // added 04.08.11, 

        // дополнительно ожидание для задумчивых отчетов 
        Thread.currentThread().sleep(2 * CPRINT_TIMEOUT);  
            


//      pr.getFrDrv().setDate((byte)11, (byte)3, (byte)8) ;



//      System.out.println("Confirm date - " + dd + ":" + mm + ":" + yy + " ..."); 
//      
//      pr.getFrDrv().confirmDate((byte)dd, (byte)mm, (byte)yy) ;
//
//      System.out.println("Set current time...");  
//
//      pr.setDataTime(new Date());       

//     pr.getFrDrv().printDocHeader("bank check ", 6080);

//    FrTable tb = new FrTable((byte)4);
//    tb.initFromFr(((FrKPrinter)pr).getFrDrv());
//
//    int i, len = tb.getNumRows();
//    
//    for (i = len - 4; i < len; i++)
//       System.out.println(tb.getValueAt(i, 0));


//      StateA state = new FullStateFr();
//      state.decodeParams(pr.getReply()); 
//      state.printAll();



//      FrTable ft = new FrTable((byte)2);
//      ft.initFromFr(pr.getFrDrv());
//      ft.printAll();

//      pr.makeClrZReport();
//
//      int sm;
//      do 
//      {
//        Thread.currentThread().sleep(1000);
//        state = pr.stateRequest();
//        sm = state.getSubModeFr();
//        System.out.println("Fr mode = " + state.getModeNum() + "  submode = " + sm);
//
//        if (sm == 1 || sm == 2) UserDlg.showError(null, "Закончилась бумага"); else
//        if (sm == 3) pr.continuePrint();
//      } while (sm != 0);

//      pr.makeXReport();
//      pr.close();    

//      fr.calcelCheck();      
//      FullStateFr fs = new FullStateFr();
//      fs.initFromFr(fr);
//      fs.printAll();
////
//      FrTable ft = new FrTable((byte)2);
//      ft.initFromFr(fr);
//      ft.printAll();

//      fr.printString("печать тестовой строки___" , true, true) ;

//      TimeSync ts = new TimeSync();
//      ts.setTimeInCash(fr);
      
//      fr.beep();
//      fr.beep();
//      fr.beep();
//      fr.makeClrZReport();
//      fr.payInOper(10000);
  
//      Thread.currentThread().sleep(3000);

//      fr.makeClrZReport();
//      fr.payInOper(10000);


//    byte[] dt = new byte[3];
//    byte[] tm = new byte[3];
//    Date data = new Date();
//    FrUtil.dateToBytes(data, dt, tm);
//
//    fr.setTime(tm);
//    fr.setDate(dt);
//    fr.confirmDate(dt);
 
//      fr.setTabData((byte)4, 1, 1, "String 1", 40);
//      fr.setTabData((byte)4, 2, 1, "String 2", 40);
//      fr.setTabData((byte)4, 3, 1, "String 3", 40);
//      fr.setTabData((byte)4, 4, 1, "String 4", 40);
//      fr.setTabData((byte)4, 5, 1, "String 5", 40);
//      fr.setTabData((byte)4, 6, 1, "String 6", 40);
//      fr.setTabData((byte)4, 8, 1, "Строка 2", 40);


//      Thread.currentThread().sleep(3000);

//      fr.beep();

//      fr.setLinkParams((byte)1, (byte)100);


//      fr.openCheck(CHECK_TYPE);
/*  
        fr.getOperReg((byte)152);
        System.out.println("chek num =" + FrUtil.intFromBytes(fr.getReplyParams(), 1, 2)); 
 
      fr.salePosition(3000, 1540, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "14706 МИНЕРАЛЬНАЯ ВОДА МАЙСКАЯ ХРУСТАЛЬНАЯ 1.5Л ГАЗИРОВАННАЯ ПЛ/Б [РОССИЯ]");
      fr.salePosition(1000, 12540, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "16776       КОФЕ NESCAFE GOLD 95Г НАТУРАЛЬНЫЙ РАСТВОРИМЫЙ СУБЛИМИРОВАННЫЙ СТ/Б [РОССИЯ]");
      fr.salePosition(2000, 3300, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "66585 ЧАЙ МАЙСКИЙ ЗОЛОТЫЕ ЛЕПЕСТКИ 2Г*25 ЦЕЙЛОНСКИЙ ЧЕРНЫЙ В ПАКЕТИКАХ [РОССИЯ]");
      fr.salePosition(1000, 3360, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "67852       БИОЙОГУРТ ONKEN 100Г ЛЮКС 8%ЖИРНОСТЬ МАЛИНА АБРИКОС МАНГО [РОССИЯ]");
      fr.salePosition(2000, 3300, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "5900        ХЛЕБ ЖИТО РАЙПО 400Г [РОССИЯ]");
      fr.salePosition(3000, 3300, 0, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, ZERO_BYTE, "2200        ВЕТЧИНА РОЖДЕСТВЕНСКАЯ МИКОЯН 1КГ ВАРЕНАЯ [РОССИЯ]");
      fr.closeCheck(100000, 0, 0, 0, 500, ZERO_BYTE,ZERO_BYTE,ZERO_BYTE,ZERO_BYTE, "закрываем");
*/
//        fr.getOperReg((byte)152);
//        System.out.println("chek num =" + FrUtil.intFromBytes(fr.getReplyParams(), 1, 2)); 
      
//     fr.makeClrZReport();
//       fr.calcelCheck();

/*      
      FrTable tab;
      for (byte i = 1; i <= 9; i++)
      {
        tab = new FrTable(i);
       
        tab.initFromFr(fr);
        tab.printAll();
      }
*/

      
//      fr.close();
    

      
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  System.exit(0);
  }
}