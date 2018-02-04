package net.rack;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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

/**
 * @author e155742
 *
 */
@SuppressWarnings("serial")
public class MyFrame extends JFrame implements Runnable, KeyListener, MouseListener {
  public static final String FONT_NAME     = "Sans";
  public static final int    FONT_STYLE    = Font.BOLD;
  public static final int    FONT_SIZE     = 108;
  public static final int    LOG_FONT_SIZE = 16;        // 右側の記録テーブルのフォントサイズ
  public static final int    BUTTON_WIDTH  = 150;
  public static final int    BUTTON_HEIGHT = 50;
  public static final String ZERO_TIME     = "0:00.000";

  private Container  contentPane           = getContentPane();
  private JTextField lastLastTimeTextField = new JTextField(ZERO_TIME); // タイム表示上段
  private JTextField lastTimeTextField     = new JTextField(ZERO_TIME); // タイム表示中段
  private JTextField timeTextField         = new JTextField(ZERO_TIME); // タイム表示下段

  private DefaultTableModel tableModel = new DefaultTableModel(new String[] { "", "" }, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
      return false; // テーブル内編集不可
    }
  };

  private JTable timeLogTable = new JTable(tableModel);                    // タイム表示横
  private Font   font         = new Font(FONT_NAME, FONT_STYLE, FONT_SIZE);

  private long    startTime      = 0;
  private int     logCount       = 0;
  private boolean isRunning      = false;
  private int     updateInterval = 77;

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
    addKeyListener(this);
  }

  public void changeFontSize(int fontSize) {
    font = new Font(FONT_NAME, FONT_STYLE, fontSize);
    lastLastTimeTextField.setFont(font);
    lastTimeTextField.setFont(font);
    timeTextField.setFont(font);
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
    menu.addMouseListener(this);

    JMenu fontSizeMenu = new JMenu("フォントサイズ");
    JMenu intervalMenu = new JMenu("更新頻度");

    JMenuItem allResetItem = new JMenuItem(new AbstractAction("完全リセット") {
      // @Override
      public void actionPerformed(ActionEvent e) {
        resetEvent();
        startTime = 0;
        sb = new StringBuilder();
        df = new DecimalFormat();
        lastLastTimeTextField.setText(ZERO_TIME);
        lastTimeTextField.setText(ZERO_TIME);
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
    fontSizeItemList.add(fontSizeItem(216));
    fontSizeItemList.add(fontSizeItem(180));
    fontSizeItemList.add(fontSizeItem(144));
    fontSizeItemList.add(fontSizeItem(108));
    fontSizeItemList.add(fontSizeItem(72));
    fontSizeItemList.get(3).setSelected(true);

    ButtonGroup fonsSizeGroup = new ButtonGroup();
    for (JMenuItem fontSizeItemLists : fontSizeItemList) {
      setMargin(fontSizeItemLists);
      fonsSizeGroup.add(fontSizeItemLists);
      fontSizeMenu.add(fontSizeItemLists);
    }

    // 更新頻度
    List<JMenuItem> updateItemList = new ArrayList<JMenuItem>();
    updateItemList.add(updateItem(37, "速い"));
    updateItemList.add(updateItem(77, "普通"));
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
      buttons.addMouseListener(this);
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

    List<JTextField> textFieldList = new ArrayList<JTextField>();
    textFieldList.add(lastLastTimeTextField);
    textFieldList.add(lastTimeTextField);
    textFieldList.add(timeTextField);
    for (JTextField textFields : textFieldList) {
      textFields.setAlignmentY(JComponent.CENTER_ALIGNMENT);
      textFields.setHorizontalAlignment(JTextField.CENTER);
      textFields.setPreferredSize(new Dimension(0, 0)); // フォントはみ出しても中央に配置
      textFields.setEditable(false); // テキストフィールドの編集不可
      textFields.setOpaque(false);
      textFields.setFont(font);
      panel.add(textFields);
      textFields.addMouseListener(this);
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
    timeLogTable.addMouseListener(this);
    scrollPane.addMouseListener(this); // 念のため
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
    timeTextField.setText(str);
  }

  public void startEvent() {
    if (isRunning) {
      return;
    }
    startTime = System.nanoTime();
    isRunning = true;
    Thread thread = new Thread(MyFrame.this);
    thread.start();
  }

  public void goalEvent() {
    if (!isRunning) {
      return;
    }
    long goalTime = System.nanoTime();
    isRunning = false;
    long runTime = (goalTime - startTime);
    String timeStr = timeFormatting(runTime);
    setTimeTextField(timeStr);
    lastLastTimeTextField.setText(lastTimeTextField.getText());
    lastTimeTextField.setText(timeStr);
    tableModel.addRow(new Object[] { ++logCount, timeStr });
    int n = timeLogTable.convertRowIndexToView(tableModel.getRowCount() - 1);
    Rectangle r = timeLogTable.getCellRect(n, 0, true);
    timeLogTable.scrollRectToVisible(r);
  }

  public void resetEvent() {
    isRunning = false;
    setTimeTextField(ZERO_TIME);
  }

  // @Override
  public void keyPressed(KeyEvent e) {
  }

  // @Override
  public void keyReleased(KeyEvent e) {
  }

  // @Override
  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar() == '/') {
      startEvent();
    } else if (e.getKeyChar() == '*') {
      goalEvent();
    }
  }

  // @Override
  public void mouseEntered(MouseEvent e) {
  }

  // @Override
  public void mouseExited(MouseEvent e) {
  }

  // @Override
  public void mouseClicked(MouseEvent e) {
    requestFocus();
  }

  // @Override
  public void mousePressed(MouseEvent e) {
    requestFocus();
  }

  // @Override
  public void mouseReleased(MouseEvent e) {
    requestFocus();
  }

  /**
   * タイマーの表示をリアルタイムで更新する。
   */
  // @Override
  public void run() {
    while (isRunning) {
      setTimeTextField(timeFormatting(System.nanoTime() - startTime));
      try {
        Thread.sleep(updateInterval);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
