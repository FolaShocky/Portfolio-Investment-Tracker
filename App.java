import com.sun.xml.internal.ws.message.stream.StreamMessage;
import components.GraphPanel;
import database.DatabaseLogic;
import market.Stock;
import market.StockItem;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.*;
import java.sql.Date;
import java.time.Month;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class App extends JFrame {
   private List<Stock> stocks;
   private static final String INITIAL_TEXT = "Enter Quantity bought: ";
   private JTextField txtQuantity = new JTextField(INITIAL_TEXT);
   private String[] days = IntStream.rangeClosed(1,31).mapToObj(num -> {
       if(num < 10)
          return "0" + num;
       else
          return  "" + num;
   }).collect(Collectors.toList()).toArray(new String[]{});

   private String[] months = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private List<Integer>integerList = IntStream.rangeClosed(1,months.length).collect(ArrayList::new,ArrayList::add,ArrayList::addAll);
   private int currentYear = LocalDate.now().getYear();
   private String[] years = IntStream.rangeClosed(currentYear-3,currentYear).mapToObj(num -> num + "").collect(ArrayList::new,ArrayList::add,ArrayList::addAll).toArray(new String[]{});
   private String[] symbolList = new String[]{"AMZN","AAPL","FB","NFLX","NKE","GOOGL","TSLA","GM","DIA"};
   private JComboBox<String> jComboBoxDay = new JComboBox<>(days);
   private JComboBox<String> jComboBoxMonth = new JComboBox<>(months);
   private JComboBox<String> jComboBoxYear = new JComboBox<>(years);
   private JComboBox<String> symbolCombo = new JComboBox<>(symbolList);
   private JLabel label = new JLabel("output here");
   private DatabaseLogic db = new DatabaseLogic();
   private GraphPanel BL;
   private JList jList;
   private JLabel ur;
   private Map<String,Integer> indexMonthMap = new TreeMap<>();
   private Map<String,Month>dayMap = new LinkedHashMap<>();

   private static int monthMapIndex = 0;
   private static int monthDayIndex = 0;
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMMyyyy");
    App() throws HeadlessException {
        super("Portfolio Tracker");
        BiConsumer<List<Integer>,String[]> biConsumer = (intList, stringList)-> intList.forEach(num -> {
            indexMonthMap.put(stringList[monthMapIndex],num);
            monthMapIndex++;
        });/*Used to allow for accurate retrieval of selected combobox in item. Returns the corresponding monthMapIndex e.g August = 8, March = 3 or November = 11*/
        for(String month: months){
            dayMap.put(month,Month.values()[monthDayIndex]);
            monthDayIndex++;
        }
        biConsumer.accept(integerList,months);
        // Window pre-setup goes here
        setSize(700, 600);
        stocks = Collections.synchronizedList(new ArrayList<>());
        db.createNewDatabase();
        db.createTable(DatabaseLogic.TABLE_TRANSACTIONS);
        txtQuantity.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(!txtQuantity.getText().matches("\\d+"))
                    txtQuantity.setText("");
            }
            @Override
            public void focusLost(FocusEvent e) {
                if(!txtQuantity.getText().matches("\\d+"))
                    txtQuantity.setText(INITIAL_TEXT);
            }
        });

        EventQueue.invokeLater(() -> {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            //creates 4 Jpanels for everything to go on, as well as giving them a visible border for now 
            JPanel UL = new JPanel();
            JPanel miniUL = new JPanel();
            JPanel miniUL2 = new JPanel();
            UL.setLayout(new BoxLayout(UL, BoxLayout.PAGE_AXIS));
            //JLabel ul = new JLabel("Enter info here");
            //JTextField EnterSymbol = new JTextField("Enter symbol text");
            //JTextField DateBought = new JTextField("Enter Date bought Please use format DD/MM/YYYY");

            JButton jButtonAdd = new JButton("Add");
            JButton jButtonDelete = new JButton("Delete");
            miniUL.add(label);
            miniUL.add(jComboBoxDay);
            miniUL.add(jComboBoxMonth);
            miniUL.add(jComboBoxYear);
            miniUL2.add(symbolCombo);
            UL.setBorder(BorderFactory.createLineBorder(Color.black));
            //ul.setHorizontalAlignment(SwingConstants.CENTER);
            //UL.add(ul);
            //UL.add(EnterSymbol);
            //UL.add(DateBought);
            UL.add(miniUL2);
            UL.add(miniUL);
            UL.add(txtQuantity);
            jButtonAdd.addActionListener(new AddListener(this));
            jButtonDelete.addActionListener(listener -> {//DO NOT CHANGE THE FORMAT FOR THE DISPLAY OF INFORMATION TO USER AS THIS WILL ADVERSELY AFFECT LOGIC THAT FOLLOWS
                    if(jList.getSelectedValue()!=null) {
                        String dateDelimiter = "-";
                        String[] selectedEntryValues = String.valueOf(jList.getSelectedValue()).split(" ");
                        String oldSymbol = selectedEntryValues[1];
                        String[] dateOfInvestment = selectedEntryValues[selectedEntryValues.length - 1].split(dateDelimiter);
                        int oldYear = Integer.parseInt(dateOfInvestment[0]);
                        int oldMonth = Integer.parseInt(dateOfInvestment[1]);
                        int oldDay = Integer.parseInt(dateOfInvestment[2]);
                        StringBuilder parsedStringBuilder = new StringBuilder();
                        for (int i = 0; i < selectedEntryValues[0].length(); i++) {
                            if (Character.isDigit(selectedEntryValues[0].charAt(i)))
                                parsedStringBuilder.append(selectedEntryValues[0].charAt(i));
                        }
                        int oldQuantity = Integer.parseInt(parsedStringBuilder.toString());
                        db.setQuantity(oldQuantity);
                        db.setDateOfInvestment(String.valueOf(Date.valueOf(LocalDate.of(oldYear, oldMonth, oldDay))));
                        db.setSymbol(oldSymbol);
                        db.deleteRecord(DatabaseLogic.TABLE_TRANSACTIONS);
                        getInventory();
                    }
                   else
                       JOptionPane.showMessageDialog(this,"Please enter select an entry to delete");
            });
            Arrays.asList(jComboBoxMonth,jComboBoxYear).forEach(field -> {
                field.addActionListener(listener -> {
                    String selectedMonth = String.valueOf(jComboBoxMonth.getSelectedItem());
                    int selectedYear = Integer.valueOf(String.valueOf(jComboBoxYear.getSelectedItem()));
                    boolean isLeapYear = selectedYear % 4 == 0;
                    if(selectedYear % 100 == 0 && selectedYear % 400 != 0)
                        isLeapYear = false;
                    try(IntStream intStream = IntStream.rangeClosed(1,dayMap.get(selectedMonth).maxLength())) {
                        jComboBoxDay.removeAllItems();
                        intStream.mapToObj(num -> {
                            if(num <= 9)
                                return "0" + num;
                            else
                                return String.valueOf(num);
                        }).forEach(num -> jComboBoxDay.addItem(num));
                        if(selectedMonth.equals("Feb")){
                            if(!isLeapYear){
                                int itemCount = jComboBoxDay.getItemCount();
                                jComboBoxDay.removeItemAt(itemCount-1);
                            }
                        }
                    }
                });
            });
            Arrays.asList(jButtonAdd,jButtonDelete).forEach(UL::add);
            //this sets how much of the frame each panel is going to take up, as well as stretching them to fill that all/otted size
            gbc.gridx =0;
            gbc.gridy =0;
            gbc.gridheight = 4;
            gbc.gridwidth = 4;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 3;
            gbc.weighty =1;
            getContentPane().add(UL, gbc);
            BL = new GraphPanel();
            BL.setBorder(BorderFactory.createLineBorder(Color.black));
            gbc.gridx = 0;
            gbc.gridy = 5;
            getContentPane().add(BL, gbc);
            JPanel UR = new JPanel();
            ur = new JLabel("Inventory value: £0.00");
            UR.setBorder(BorderFactory.createLineBorder(Color.black));
            UR.add(ur);
            gbc.gridx = 5;
            gbc.gridy = 0;
            getContentPane().add(UR,gbc);
            jList = new JList<String>();
            jList.setBorder(BorderFactory.createLineBorder(Color.black));
            gbc.gridx = 5;
            gbc.gridy = 5;
            getContentPane().add(jList,gbc);
            getInventory();
            center();
            setVisible(true);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        });
    }
    private void deletionLogic(){
        String dateDelimiter = "-";
        String[] selectedEntryValues = String.valueOf(jList.getSelectedValue()).split(" ");
        String oldSymbol = selectedEntryValues[1];
        String[] dateOfInvestment = selectedEntryValues[selectedEntryValues.length - 1].split(dateDelimiter);
        int oldYear = Integer.parseInt(dateOfInvestment[0]);
        int oldMonth = Integer.parseInt(dateOfInvestment[1]);
        int oldDay =  Integer.parseInt(dateOfInvestment[2]);
        StringBuilder parsedStringBuilder = new StringBuilder();
        for( int i = 0; i< selectedEntryValues[0].length();i++){
            if(Character.isDigit(selectedEntryValues[0].charAt(i)))
                parsedStringBuilder.append(selectedEntryValues[0].charAt(i));
        }
        int oldQuantity = Integer.parseInt(parsedStringBuilder.toString());
        db.setQuantity(oldQuantity);
        db.setDateOfInvestment(String.valueOf(Date.valueOf(LocalDate.of(oldYear, oldMonth,oldDay))));
        db.setSymbol(oldSymbol);
        db.deleteRecord(DatabaseLogic.TABLE_TRANSACTIONS);
        getInventory();
    }
    private void getInventory() {
        // initialise the inventory of stocks to display
        stocks.clear();
        List<String> list = new ArrayList<>();
        double value = 0;
        BL.clear();
        try(Connection con = DriverManager.getConnection(DatabaseLogic.getURL());
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + DatabaseLogic.TABLE_TRANSACTIONS)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Stock stock = new Stock(rs.getString("Symbol"));
                stocks.add(stock);
                BL.withData(stock);
                List<StockItem> h = stock.getMonth();
                if (h.size() > 0) {
                    double close = h.get(h.size() - 1).getClose();

                    list.add(String.format("%,dx %s @ %,.2f: £%,.2f %s",
                            rs.getInt(DatabaseLogic.DB_FIELD_QUANTITY),
                            stock.symbol(),
                            close,
                            close * rs.getInt(DatabaseLogic.DB_FIELD_QUANTITY),
                            rs.getString(DatabaseLogic.DB_FIELD_DATE_OF_INVESTMENT))
                    );
                    value += close * rs.getInt(DatabaseLogic.DB_FIELD_QUANTITY);
                }
            }
        }
        catch (SQLException e) {
            System.out.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
        jList.setListData(list.toArray());

        ur.setText(String.format("Inventory value: £%,.2f", value));

        repaint();
    }
    private List<Integer> populateList(int first,int last){
        List<Integer>integerList;
        try(IntStream intStream = IntStream.rangeClosed(first,last)){
            integerList = intStream.collect(ArrayList::new,ArrayList::add,ArrayList::addAll);
        }
        return integerList;
    }

    private void center() {
        Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) (displaySize.getWidth() - getWidth()) / 2,
                (int) (displaySize.getHeight() - getHeight()) / 2);
    }
    class AddListener implements ActionListener{
        App theApp;
        public AddListener(App app){ theApp = app;}
        @Override
        public void actionPerformed(ActionEvent e) {
            String check = txtQuantity.getText();
            final String MESSAGE = "You have already added data from at least 5 different companies.\n Please delete some.";
            boolean number = true;
            for(char c: check.toCharArray()){
                if (!Character.isDigit(c)){
                    number = false;
                    //txtQuantity.setText("");
                }
            }
            if(!number) {
                JOptionPane.showMessageDialog(null,"Error, only numbers accepted for txtQuantity field");
            }
            else {
                // TODO: send string to database insert function
                int selectedQuantity;
                try {
                    selectedQuantity = Integer.valueOf(String.valueOf(txtQuantity.getText()));
                }
                catch (Exception err) {
                    selectedQuantity = 0;
                }
                int selectedDay = Integer.parseInt(String.valueOf(jComboBoxDay.getSelectedItem()));
                int selectedMonth = indexMonthMap.get(String.valueOf(jComboBoxMonth.getSelectedItem()));
                int selectedYear = Integer.parseInt(String.valueOf(jComboBoxYear.getSelectedItem()));
                String selectedSymbol = String.valueOf(symbolCombo.getSelectedItem());
                LocalDate date = LocalDate.of(selectedYear,selectedMonth,selectedDay);
                db.setDateOfInvestment(date.toString());
                db.setQuantity(selectedQuantity);
                db.setSymbol(selectedSymbol);
                int retrievedCompanyCount = db.retrieveAllCompanies(DatabaseLogic.TABLE_TRANSACTIONS);
                if(retrievedCompanyCount!= -1){
                    if(retrievedCompanyCount >= 5 && db.getCompanyList().contains(selectedSymbol))
                        db.createRecord(DatabaseLogic.TABLE_TRANSACTIONS);
                    else if(retrievedCompanyCount < 5)
                        db.createRecord(DatabaseLogic.TABLE_TRANSACTIONS);
                    else
                        JOptionPane.showMessageDialog(theApp,MESSAGE);
                }
                db.setCompanyList(new ArrayList<>());
                getInventory();
            }
        }
    }
}

