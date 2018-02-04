package net.rack;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * @author e155742
 *
 */
@SuppressWarnings("serial")
public class MyFrame extends JFrame implements Runnable, NativeKeyListener {
  public static final String FONT_NAME               = "Sans";
  public static final int    FONT_STYLE              = Font.BOLD;
  public static final int    DEFAULT_FONT_SIZE_INDEX = 1;
  public static final int[]  FONT_SIZE               = { 72, 108, 144, 180, 216 };
  public static final int    LOG_FONT_SIZE           = 16;                        // 右側の記録テーブルのフォントサイズ
  public static final int    BUTTON_WIDTH            = 150;
  public static final int    BUTTON_HEIGHT           = 50;

  Timer timer = new Timer();

  private Container contentPane = getContentPane();

  private JTextField[] timeTextField = { new JTextField(Timer.ZERO_TIME),
                                         new JTextField(Timer.ZERO_TIME),
                                         new JTextField(Timer.ZERO_TIME) };

  private DefaultTableModel tableModel = new DefaultTableModel(new String[] { "", "" }, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
      return false; // テーブル内編集不可
    }
  };

  private JTable    timeLogTable         = new JTable(tableModel); // タイム表示横
  private Font      font                 = new Font(FONT_NAME, FONT_STYLE, FONT_SIZE[DEFAULT_FONT_SIZE_INDEX]);
  private int       logCount             = 0;
  private int       updateInterval       = 77;

  public MyFrame(String title, int widht, int eight) {
    setTitle(title);
    setSize(widht, eight);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    createMenubar();
    showTimer();
    createButtons();
    createTable();
    setFocusable(true);

    final Logger jNativeHookLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    if (jNativeHookLogger.getLevel() != Level.WARNING) {
      synchronized (jNativeHookLogger) {
        jNativeHookLogger.setLevel(Level.WARNING);
      }
    }
    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    GlobalScreen.addNativeKeyListener(this);
  }

  public void changeFontSize(int fontSize) {
    font = new Font(FONT_NAME, FONT_STYLE, fontSize);
    for (JTextField timeTextFields : timeTextField) {
      timeTextFields.setFont(font);
    }
  }

  private void setMargin(JMenu menu) {
    menu.setBorder(BorderFactory.createCompoundBorder(menu.getBorder(), BorderFactory.createEmptyBorder(3, 0, 3, 0)));
  }

  private void setMargin(JMenuItem menu) {
    menu.setBorder(BorderFactory.createCompoundBorder(menu.getBorder(), BorderFactory.createEmptyBorder(3, 0, 3, 0)));
  }

  /**
   * フォントサイズの変更
   * 
   * @param size
   *          - フォントサイズ
   */
  private JRadioButtonMenuItem fontSizeItem(final int size) {
    return new JRadioButtonMenuItem(new AbstractAction(size + "pt") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        changeFontSize(size);
      }
    });
  }

  /**
   * 更新頻度の変更
   *
   * @param time
   *          - 更新間隔
   * @param itemName
   *          - メニューアイテムでの表示名
   */
  private JRadioButtonMenuItem updateItem(final int time, String itemName) {
    return new JRadioButtonMenuItem(new AbstractAction(itemName) {
      // @Override
      public void actionPerformed(ActionEvent e) {
        updateInterval = time;
      }
    });
  }

  /**
   * メニューバーを作成する。<br>
   * メニューは「メニュー」のみ。アイテムは「設定」「完全リセット」「終了」の3つ。<br>
   * 設定は「フォントサイズ」、「タイマーの更新頻度」が設定できる。
   */
  private void createMenubar() {
    JMenuBar menubar = new JMenuBar();
    JMenu menu = new JMenu("メニュー");
    menubar.add(menu);

    JMenu fontSizeMenu = new JMenu("フォントサイズ");
    JMenu intervalMenu = new JMenu("更新頻度");

    JMenuItem allResetItem = new JMenuItem(new AbstractAction("完全リセット") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        resetEvent();
        sb = new StringBuilder();
        df = new DecimalFormat();
        for (int i = 1; i < timeTextField.length; i++) {
          timeTextField[i].setText(Timer.ZERO_TIME);
        }
        tableModel.setRowCount(0);
        logCount = 0;
      }
    });

    JMenuItem closeItem = new JMenuItem(new AbstractAction("終了") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });

    setMargin(menu);
    setMargin(fontSizeMenu);
    setMargin(intervalMenu);
    setMargin(allResetItem);
    setMargin(closeItem);

    menu.add(fontSizeMenu);
    menu.add(intervalMenu);
    menu.add(allResetItem);
    menu.add(closeItem);

    // フォントサイズ
    List<JMenuItem> fontSizeItemList = new ArrayList<JMenuItem>();
    for (int fontSizes : FONT_SIZE) {
      fontSizeItemList.add(fontSizeItem(fontSizes));
    }
    fontSizeItemList.get(DEFAULT_FONT_SIZE_INDEX).setSelected(true);

    ButtonGroup fonsSizeGroup = new ButtonGroup();
    for (JMenuItem fontSizeItemLists : fontSizeItemList) {
      setMargin(fontSizeItemLists);
      fonsSizeGroup.add(fontSizeItemLists);
      fontSizeMenu.add(fontSizeItemLists);
    }

    // 更新頻度
    List<JMenuItem> updateItemList = new ArrayList<JMenuItem>();
    updateItemList.add(updateItem(37,  "速い"));
    updateItemList.add(updateItem(77,  "普通"));
    updateItemList.add(updateItem(117, "遅い"));
    updateItemList.get(1).setSelected(true);

    ButtonGroup intervalGroup = new ButtonGroup();
    for (JMenuItem updateItemLists : updateItemList) {
      setMargin(updateItemLists);
      intervalGroup.add(updateItemLists);
      intervalMenu.add(updateItemLists);
    }

    setJMenuBar(menubar);
  }

  /**
   * ボタンを作成する。<br>
   * 「スタート」「ゴール(ストップ)」「リセット」の3つ。
   */
  private void createButtons() {
    JPanel panel = new JPanel();
    JButton startButton = new JButton(new AbstractAction("スタート (/)") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        startEvent();
      }
    });

    JButton goalButton = new JButton(new AbstractAction("ゴール (*)") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        goalEvent();
      }
    });

    JButton resetButton = new JButton(new AbstractAction("リセット") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        resetEvent();
      }
    });

    List<JButton> buttonList = new ArrayList<JButton>();
    buttonList.add(startButton);
    buttonList.add(goalButton);
    buttonList.add(resetButton);
    for (JButton buttons : buttonList) {
      buttons.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
      panel.add(buttons);
    }
    panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    contentPane.add(panel, BorderLayout.SOUTH);
  }

  /**
   * タイムを表示する。<br>
   * こいつが画面上のメイン。
   */
  private void showTimer() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    for (int i = timeTextField.length - 1; i >= 0; i--) {
      timeTextField[i].setAlignmentY(JComponent.CENTER_ALIGNMENT);
      timeTextField[i].setHorizontalAlignment(JTextField.CENTER);
      timeTextField[i].setPreferredSize(new Dimension(0, 0)); // フォントはみ出しても中央に配置
      timeTextField[i].setEditable(false); // テキストフィールドの編集不可
      timeTextField[i].setOpaque(false);
      timeTextField[i].setFont(font);
      panel.add(timeTextField[i]);
    }
    contentPane.add(panel, BorderLayout.CENTER);
  }

  /**
   * 過去のタイムを記憶するためのテーブル
   */
  private void createTable() {
    JPanel panel = new JPanel();
    JScrollPane scrollPane = new JScrollPane(timeLogTable);
    scrollPane.setPreferredSize(new Dimension(135, 320));
    timeLogTable.setFont(new Font(FONT_NAME, Font.PLAIN, LOG_FONT_SIZE));
    timeLogTable.getColumnModel().getColumn(0).setPreferredWidth(30);
    panel.add(scrollPane);
    contentPane.add(panel, BorderLayout.EAST);
  }

  private DecimalFormat df = new DecimalFormat();
  private StringBuilder sb = new StringBuilder();

  /**
   * 与えられたナノ秒を 分:秒.ミリ秒 のフォーマットの文字列にして返す。
   * 
   * @param time
   *          - ナノ秒
   * @return m:ss.000
   */
  synchronized public String timeFormatting(long time) {
    time = Math.round(time / 1000000.0); // 引数をdouble型にするから.0は外すな
    long miliSec = (time % 1000);
    long sec = (time / 1000) % 60;
    long min = ((time / 1000) / 60);
    sb.delete(0, sb.length());
    df.applyPattern("00");
    sb.append(min + ":" + df.format(sec) + ".");
    df.applyPattern("000");
    sb.append(df.format(miliSec));
    return sb.toString();
  }

  /**
   * 現在タイマー(下のやつ)に文字列をセット。<br>
   * synchronizedメソッド
   * 
   * @param str
   *          - timeTextFieldにセットしたい文字列
   */
  synchronized public void setTimeTextField(String str) {
    timeTextField[0].setText(str);
  }

  public void startEvent() {
    if (timer.getIsRunning()) {
      return;
    }
    timer.starter();
    Thread thread = new Thread(MyFrame.this);
    thread.start();
  }

  public void goalEvent() {
    if (!timer.getIsRunning()) {
      return;
    }
    timer.stopper();
    String timeStr = timeFormatting(timer.getRunTime());
    setTimeTextField(timeStr);
    for (int i = timeTextField.length - 1; i > 0; i--) {
      timeTextField[i].setText(timeTextField[i - 1].getText());
    }

    tableModel.addRow(new Object[] { ++logCount, timeStr });
    int n = timeLogTable.convertRowIndexToView(tableModel.getRowCount() - 1);
    Rectangle r = timeLogTable.getCellRect(n, 0, true);
    timeLogTable.scrollRectToVisible(r);
  }

  public void resetEvent() {
    timer.reset();
    setTimeTextField(Timer.ZERO_TIME);
  }

  private boolean pressLeftShiftKey  = false;
  private boolean pressRightShiftKey = false;

  // @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    int keyCode = e.getKeyCode();

    if (keyCode == NativeKeyEvent.VC_SHIFT_L) { // シフトキー
      pressLeftShiftKey = true;
      return;
    } else if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT_R) {
      pressRightShiftKey = true;
      return;
    }

    if (keyCode == NativeKeyEvent.VC_KP_DIVIDE || keyCode == NativeKeyEvent.VC_SLASH) { // スタートキー (/)
      startEvent();
    } else if (keyCode == NativeKeyEvent.VC_KP_MULTIPLY
           || ((pressLeftShiftKey || pressRightShiftKey) && keyCode == NativeKeyEvent.VC_SEMICOLON)) { // ゴールキー (*)
      goalEvent();
    }
  }

  // @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
    int keyCode = e.getKeyCode();
    if (keyCode == NativeKeyEvent.VC_SHIFT_L) {
      pressLeftShiftKey = false;
    } else if (keyCode == NativeKeyEvent.VC_SHIFT_R) {
      pressRightShiftKey = false;
    }
  }

  // @Override
  public void nativeKeyTyped(NativeKeyEvent e) {
  }

  /**
   * タイマーの表示をリアルタイムで更新する。
   */
  // @Override
  public void run() {
    while (timer.getIsRunning()) {
      setTimeTextField(timeFormatting(System.nanoTime() - timer.getStartTime()));
      try {
        Thread.sleep(updateInterval);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
