/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memsys.ui.reconnection;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import memsys.global.DBConn.MainDBConn;
import memsys.global.FunctionFactory;
import memsys.global.myFunctions;
import memsys.ui.main.ParentWindow;

/**
 *
 * @author LESTER JP CADIZ
 */
public class CreateNewRequest extends javax.swing.JDialog {

    public static NewDiscoReco frmParent;
    public static String AcctNo, Nym, Stat, OrderBy, TownCode, RouteCode, NearAcctNos;
    static Statement stmt;
    static DefaultTableCellRenderer cellAlignCenterRenderer = new DefaultTableCellRenderer();
    static DefaultTableCellRenderer cellAlignRightRenderer = new DefaultTableCellRenderer();
    static DefaultTableModel model, model2, model3, model4;
    static DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    public static CreateNewRequest frmCreateNewRequest;
    static int typeid, areaid, RouteSeqNo;
    public static SearchBillDeposit frmSearchBillDeposit;
    static String nowDate = FunctionFactory.GetSystemNowDateString();
    static String nowDate2 = FunctionFactory.getSystemNowDateTimeString();

    public CreateNewRequest(NewDiscoReco parent, boolean modal) {
        this.frmParent = parent;
        this.setModal(modal);
        initComponents();
        setLocationRelativeTo(this);
        PopulateCMBType();
        GetOldAcctCode();
        this.setTitle("Create New Request: " + Nym + " Account No.:" + AcctNo + "/" + TownCode + "-" + RouteCode + "-" + RouteSeqNo + " - " + Stat);
        jPanel1.setVisible(false);
        lblpayments.setVisible(false);
        jscrollpayments.setVisible(false);
        lblbd.setVisible(false);
        txtbd.setVisible(false);
        cmdbd.setVisible(false);
        jscrolladditems.setVisible(false);
        lbladditems.setVisible(false);
        cmdadditem.setVisible(false);
        txtrecommendation.setVisible(false);
        lblreccomendation.setVisible(false);
        lbltotal.setVisible(false);
        txttotal.setVisible(false);

        TableColumn idClmn2 = tblpayments.getColumn("id");
        idClmn2.setMaxWidth(0);
        idClmn2.setMinWidth(0);
        idClmn2.setPreferredWidth(0);

        TableColumn idClmn3 = tbladditems.getColumn("id");
        idClmn3.setMaxWidth(0);
        idClmn3.setMinWidth(0);
        idClmn3.setPreferredWidth(0);

        tblpayments.setCellSelectionEnabled(false);
        tblpayments.setRowSelectionAllowed(true);
        tblpayments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblpayments.setSelectionBackground(new Color(153, 204, 255));
        tblpayments.setSelectionForeground(Color.BLACK);

        tblMonth.setCellSelectionEnabled(false);
        tblMonth.setRowSelectionAllowed(true);
        tblMonth.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMonth.setSelectionBackground(new Color(153, 204, 255));
        tblMonth.setSelectionForeground(Color.BLACK);

        txtremarks.setVisible(false);

        PopulateCmbOffice();

        populateTBLNearaAcctNo();
    }

    public static void rsAddConnLog(int acctno, String remarks, int statid, int uid, String nowDate, String note) {
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "INSERT INTO connLogTBL (AcctNo, Remarks, StatusID, UserID, TransDate, Note) VALUES (" + acctno + ",'" + remarks + "'," + statid + "," + uid + ",'" + nowDate + "','" + note + "')";

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void GetOldAcctCode() {
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "SELECT TownCode, RouteCode, RouteSeqNo FROM Consumer WHERE AcctNo=" + AcctNo;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            while (rs.next()) {
                TownCode = rs.getString(1);
                RouteCode = rs.getString(2);
                RouteSeqNo = rs.getInt(3);
            }
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void populateTBLNearaAcctNo() {
        Connection conn = MainDBConn.getConnection();
        String createString;

        int tseq = RouteSeqNo + 50;
        int bseq = RouteSeqNo - 50;

        createString = "SELECT CONCAT(AcctNo,'/',TownCode, '-', RouteCode,'-', RouteSeqNo) as acctno, AcctName, AcctAddress FROM Consumer WHERE TownCode='" + TownCode + "' AND RouteCode='" + RouteCode + "' AND RouteSeqNo>" + bseq + " AND RouteSeqNo<" + tseq + "";

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);
            model4 = (DefaultTableModel) tblMonth.getModel();

            cellAlignCenterRenderer.setHorizontalAlignment(0);
            cellAlignRightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

            tblMonth.setRowHeight(21);

            // tblMonth.getColumnModel().getColumn(2).setCellRenderer(cellAlignRightRenderer);
            tblMonth.setColumnSelectionAllowed(false);
            model4.setNumRows(0);

            while (rs.next()) {
                model4.addRow(new Object[]{false, rs.getString(1), rs.getString(2), rs.getString(3)});

            }

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void PopulateCmbOffice() {
        cmboffice.addItem(new Item2(1, "Main Office"));
        cmboffice.addItem(new Item2(2, "Guihulngan Office"));
        cmboffice.addItem(new Item2(3, "Bais Office"));
    }

    private void AddNewRequest() {
        AddRequest();
        AddPayments();

        if (IsAcctExist(Integer.parseInt(AcctNo)) == false) {
            AddToConnTBL(Integer.parseInt(AcctNo));
        }

        rsAddConnLog(Integer.parseInt(AcctNo), "Request for Re-connection", 12, ParentWindow.getUserID(), nowDate2, "");
        frmParent.populateRequestTBL();
        this.dispose();
        JOptionPane.showMessageDialog(this, "Request Successfully Created!");
    }

    class Item2 {

        private int id;
        private String description;

        public Item2(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String toString() {
            return description;
        }
    }

    public void showFrmSearchBillDeposit() {
        frmSearchBillDeposit = new SearchBillDeposit(this, true);
        frmSearchBillDeposit.setVisible(true);
    }

    public void TagBillDeposit(String billdeposit) {
        txtbd.setText(billdeposit);
        ((DefaultTableModel) tblpayments.getModel()).removeRow(2);
        CalculateTotalPayments();
    }

    public static int GetCurrentMeterSN(int AcctNo) {
        int mtrsn = 0;
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "SELECT MeterSN FROM ConsumerMeter WHERE AcctNo=" + AcctNo;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            while (rs.next()) {
                mtrsn = rs.getInt(1);
            }
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
        return mtrsn;
    }

    private static int GetMaxID() {
        int max = 0;
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "SELECT MAX(DiscoRecoID) FROM discoRecoTransTBL";
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            while (rs.next()) {
                max = rs.getInt(1);
            }
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
        return max;
    }

    private void AddPayments() {
        int rows = ((DefaultTableModel) tblpayments.getModel()).getRowCount();
        for (int i = 0; i < rows; i++) {
            String payid = (String) ((DefaultTableModel) tblpayments.getModel()).getValueAt(i, 0);
            //System.out.println(payid);
            String amnt = (String) ((DefaultTableModel) tblpayments.getModel()).getValueAt(i, 2);
            AddPaymentItem(Integer.parseInt(payid), Double.parseDouble(amnt.replace(",", "")));
        }
    }

    private void AddPaymentItem(int pid, double amnt) {
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "INSERT INTO discoRecoTransPaymentTBL (DiscoRecoID, Amount, PaymentTypeID) "
                + "VALUES (" + GetMaxID() + ",'" + amnt + "'," + pid + ")";

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public void AddRequest() {
        String rem = "";
        if (typeid == 2) {
            if (o1.isSelected() == true) {
                rem = "Needs Inpection";
            } else if (o2.isSelected() == true) {
                rem = "Ready for Energization";
            } else if (o3.isSelected() == true) {
                rem = "Return KWHM & SDW";
            } else if (o4.isSelected() == true) {
                rem = txtremarks.getText();
            }
        }

        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "INSERT INTO discoRecoTransTBL ("
                + "AcctNo, "
                + "TypeID, "
                + "TransDate, "
                + "status, "
                + "CurrentMeterSN, "
                + "BillDeposit, "
                + "Remarks, "
                + "Recommendation, "
                + "UserID, "
                + "ContactNo, "
                + "DeliquentORs, "
                + "NearAcctNo, "
                + "DeliquentMonths, "
                + "OrderForAction, OfficeFlg) "
                + "VALUES ('"
                + AcctNo + "',"
                + typeid + ",'"
                + nowDate + "',"
                + 12 + ","
                + GetCurrentMeterSN(Integer.parseInt(AcctNo)) + ",'"
                + txtbd.getText() + "','"
                + rem + "','"
                + txtrecommendation.getText() + "', "
                + ParentWindow.getUserID() + ", '"
                + txtcn.getText() + "', '"
                + txtDOR.getText() + "', '"
                + NearAcctNos + "', '"
                + txtDM.getText() + "', '"
                + lblorder.getText() + "', " + areaid
                + ")";

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public void CalculateTotalPayments() {
        int r = tblpayments.getRowCount();
        int c;
        c = 0;
        double totalAmnt = 0;
        while (c < r + 1) {
            try {
                String amntStr = tblpayments.getValueAt(c, 2).toString();
                double amnt = Double.parseDouble(amntStr);

                totalAmnt = totalAmnt + amnt;

            } catch (Exception e) {
                e.getStackTrace();
            }
            c++;
        }
        txttotal.setText(myFunctions.amountFormat(totalAmnt));
    }

    public void PopulateCMBType() {
        cmbtype.addItem("--SELECT--");
        Connection conn = MainDBConn.getConnection();
        String createString = "";

        if (null != Stat) {
            switch (Stat) {
                case "Connected":
                    createString = "SELECT TypeID, TypeDesc FROM discoRecoTransTypeTBL WHERE TypeID<>2;";
                    break;
                case "Disconnected":
                    createString = "SELECT TypeID, TypeDesc FROM discoRecoTransTypeTBL WHERE TypeID=2;";
                    break;
            }
        }

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            while (rs.next()) {
                cmbtype.addItem(new Item(rs.getInt(1), rs.getString(2)));
            }

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error RSQuery/004: Query Select All Areas!");
        }
    }

    boolean IsAcctExist(int acctno) {
        boolean found = false;

        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "SELECT * FROM connTBL WHERE AcctNo='" + acctno + "'";

        int rc = 0;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            while (rs.next()) {
                rc++;
            }

            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.getStackTrace();
        }

        if (rc > 0) {
            found = true;
        }

        return found;
    }

    public static void AddToConnTBL(int acctno) {
        // MDI.UserID
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "INSERT INTO connTBL(AcctNo, "
                + "TownCode, "
                + "RouteCode, "
                + "AcctCode, "
                + "RouteSeqNo, "
                + "ClassCode, "
                + "AcctName, "
                + "AcctAddress, "
                + "MembershipID, "
                + "UserID, "
                + "TransDate, BrgyCode, Status) "
                + "SELECT AcctNo, TownCode, RouteCode, AcctCode, RouteSeqNo, ClassCode, AcctName, AcctAddress, MembershipID, " + ParentWindow.getUserID() + ",'" + nowDate2 + "', BrgyCode, 12 "
                + "FROM Consumer WHERE AcctNo='" + acctno + "'";
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            //JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void LoadPayments() {
        if (typeid == 4) {

        } else {
            populateTBLPayments();
            CalculateTotalPayments();
        }
    }

    public void populateTBLPayments() {
        Connection conn = MainDBConn.getConnection();
        String createString;

        createString = "SELECT t.PaymentTypeID, t.PaymentDesc, d.Amount FROM discoRecoPaymentDefaultTBL d"
                + " INNER JOIN discoRecoPaymentTypeTBL t ON d.PaymentTypeID=t.PaymentTypeID"
                + " WHERE d.TypeID=" + typeid;

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);
            model = (DefaultTableModel) tblpayments.getModel();

            cellAlignCenterRenderer.setHorizontalAlignment(0);
            cellAlignRightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

            tblpayments.setRowHeight(29);

            tblpayments.getColumnModel().getColumn(2).setCellRenderer(cellAlignRightRenderer);
            tblpayments.setColumnSelectionAllowed(false);
            model.setNumRows(0);

            int cnt = 0;
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
                cnt++;
            }
            if (cnt != 0) {
                tblpayments.setRowSelectionInterval(0, 0);
            }
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    class Item {

        private int id;
        private String description;

        public Item(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String toString() {
            return description;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        o1 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        o2 = new javax.swing.JRadioButton();
        o3 = new javax.swing.JRadioButton();
        o4 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMonth = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtcn = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtremarks = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtDOR = new javax.swing.JTextField();
        txtDM = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cmboffice = new javax.swing.JComboBox();
        lblorder = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtDM1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        cmbtype = new javax.swing.JComboBox();
        lblpayments = new javax.swing.JLabel();
        jscrolladditems = new javax.swing.JScrollPane();
        tbladditems = new javax.swing.JTable();
        lblbd = new javax.swing.JLabel();
        lbladditems = new javax.swing.JLabel();
        cmdbd = new javax.swing.JButton();
        txtbd = new javax.swing.JTextField();
        jscrollpayments = new javax.swing.JScrollPane();
        tblpayments = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        cmdCancel = new javax.swing.JButton();
        cmdCreate = new javax.swing.JButton();
        lblreccomendation = new javax.swing.JLabel();
        txtrecommendation = new javax.swing.JTextField();
        txttotal = new javax.swing.JLabel();
        lbltotal = new javax.swing.JLabel();
        cmdadditem = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create New Request");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(o1);
        o1.setText("Needs Inpection");
        o1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                o1ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setText("Remarks");

        buttonGroup1.add(o2);
        o2.setSelected(true);
        o2.setText("Ready for Energization");
        o2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                o2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(o3);
        o3.setText("Return KWHM & SDW");
        o3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                o3ActionPerformed(evt);
            }
        });

        buttonGroup1.add(o4);
        o4.setText("Others");
        o4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                o4ActionPerformed(evt);
            }
        });

        tblMonth.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "Account No.", "Account Name", "Address"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblMonth);
        if (tblMonth.getColumnModel().getColumnCount() > 0) {
            tblMonth.getColumnModel().getColumn(2).setHeaderValue("Account Name");
            tblMonth.getColumnModel().getColumn(3).setHeaderValue("Address");
        }
        tblMonth.getColumnModel().getColumn(0).setMaxWidth(20);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Nearest Neighbor Consumer Account");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Contact No.");

        txtremarks.setColumns(20);
        txtremarks.setRows(5);
        jScrollPane3.setViewportView(txtremarks);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Deliquent Period");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Deliquent Paid Under O.R. No. ");

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel9.setText("Ordered for action by");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setText("Office where consumer applied");

        cmboffice.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmboffice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbofficeActionPerformed(evt);
            }
        });

        lblorder.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblorder.setForeground(new java.awt.Color(51, 102, 0));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setText("Landmark");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtcn, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmboffice, 0, 326, Short.MAX_VALUE)
                            .addComponent(lblorder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(o1)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)
                                    .addComponent(o3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(o2)
                                            .addComponent(o4, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(42, 42, 42)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDOR, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtDM, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDM1)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(o1)
                    .addComponent(o2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(o3)
                    .addComponent(o4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDM, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDM1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(4, 4, 4)
                .addComponent(txtDOR, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtcn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cmboffice, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel9)
                    .addComponent(lblorder, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cmbtype.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmbtype.setMaximumRowCount(15);
        cmbtype.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbtypeActionPerformed(evt);
            }
        });

        lblpayments.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblpayments.setText("Payment Details");

        tbladditems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "QTY", "Description", "Amount"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jscrolladditems.setViewportView(tbladditems);
        if (tbladditems.getColumnModel().getColumnCount() > 0) {
            tbladditems.getColumnModel().getColumn(0).setResizable(false);
        }
        tbladditems.getColumnModel().getColumn(2).setPreferredWidth(300);

        lblbd.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblbd.setText("Bill Deposit ");

        lbladditems.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lbladditems.setText("Additional Items");

        cmdbd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/search.png"))); // NOI18N
        cmdbd.setMnemonic('k');
        cmdbd.setText("Check");
        cmdbd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdbdActionPerformed(evt);
            }
        });

        txtbd.setEnabled(false);

        tblpayments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "Payment Item", "Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jscrollpayments.setViewportView(tblpayments);
        if (tblpayments.getColumnModel().getColumnCount() > 0) {
            tblpayments.getColumnModel().getColumn(0).setResizable(false);
        }
        tblpayments.getColumnModel().getColumn(1).setPreferredWidth(300);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Type of Request");

        cmdCancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/exit.png"))); // NOI18N
        cmdCancel.setMnemonic('C');
        cmdCancel.setText("Cancel");
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });

        cmdCreate.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        cmdCreate.setMnemonic('r');
        cmdCreate.setText("Create");
        cmdCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCreateActionPerformed(evt);
            }
        });

        lblreccomendation.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblreccomendation.setText("Recommedation");

        txttotal.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txttotal.setForeground(new java.awt.Color(0, 102, 0));
        txttotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        txttotal.setText("0.00");
        txttotal.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lbltotal.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lbltotal.setForeground(new java.awt.Color(0, 102, 0));
        lbltotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbltotal.setText("Total");
        lbltotal.setToolTipText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblbd)
                        .addComponent(lblpayments)
                        .addComponent(lblreccomendation))
                    .addComponent(lbladditems))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jscrollpayments)
                    .addComponent(cmbtype, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtbd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(cmdCreate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmdCancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                                .addComponent(lbltotal, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cmdbd, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txttotal, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(txtrecommendation)
                    .addComponent(jscrolladditems, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(cmbtype, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jscrollpayments, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(lblpayments))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbladditems)
                    .addComponent(jscrolladditems, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblbd)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtbd, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmdbd)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtrecommendation, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblreccomendation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmdCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbltotal))
                    .addComponent(cmdCreate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txttotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        cmdadditem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        cmdadditem.setMnemonic('r');
        cmdadditem.setText("Add Item");
        cmdadditem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdadditemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmdadditem)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdadditem))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCreateActionPerformed
        int col = 1;
        int cntr = 0;
        String nas = "";
        int rows = ((DefaultTableModel) tblMonth.getModel()).getRowCount();
        for (int i = 0; i < rows; i++) {
            String na = (String) ((DefaultTableModel) tblMonth.getModel()).getValueAt(i, col);
            String nan = (String) ((DefaultTableModel) tblMonth.getModel()).getValueAt(i, 2);
            String selTF = tblMonth.getValueAt(i, 0).toString();
            if ("true".equals(selTF)) {
                cntr++;
                if (cntr == 1) {
                    nas = na + ":" + nan;
                } else {
                    nas = nas + ", " + na + ":" + nan;
                }
            }
        }
        if (cntr == 0) {
            JOptionPane.showMessageDialog(this, "No record is selected! Please select a record from the table!");
        } else {
            NearAcctNos = nas;
            //System.out.println(nas);
            if (o4.isSelected() == true) {
                if (txtremarks.getText().isEmpty() == true) {
                    JOptionPane.showMessageDialog(this, "Please fill-up the remarks");
                } else {
                    if (txtDM.getText().isEmpty() == true || txtDOR.getText().isEmpty() == true) {
                        JOptionPane.showMessageDialog(this, "Please fill-up the required fields");
                    } else {
                        AddNewRequest();
                    }
                }
            } else {
                if (txtDM.getText().isEmpty() == true || txtDOR.getText().isEmpty() == true) {
                    JOptionPane.showMessageDialog(this, "Please fill-up the required fields");
                } else {
                    AddNewRequest();
                }
            }
        }
    }//GEN-LAST:event_cmdCreateActionPerformed

    private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_cmdCancelActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated

    }//GEN-LAST:event_formWindowActivated

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void cmbtypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbtypeActionPerformed
        try {
            Item item = (Item) cmbtype.getSelectedItem();
            typeid = item.getId();

            if (typeid == 1) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(true);
                txtbd.setVisible(true);
                cmdbd.setVisible(true);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(true);
                lblreccomendation.setVisible(true);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 2) {
                jPanel1.setVisible(true);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(true);
                txtbd.setVisible(true);
                cmdbd.setVisible(true);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(false);
                //lblreccomendation.setVisible(false);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 3) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(false);
                txtbd.setVisible(false);
                cmdbd.setVisible(false);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(false);
                lblreccomendation.setVisible(false);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 4) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(false);
                jscrollpayments.setVisible(false);
                lblbd.setVisible(false);
                txtbd.setVisible(false);
                cmdbd.setVisible(false);
                jscrolladditems.setVisible(true);
                lbladditems.setVisible(true);
                cmdadditem.setVisible(true);
                txtrecommendation.setVisible(false);
                lblreccomendation.setVisible(false);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 5) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(true);
                txtbd.setVisible(true);
                cmdbd.setVisible(true);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(true);
                lblreccomendation.setVisible(true);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 6) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(true);
                txtbd.setVisible(true);
                cmdbd.setVisible(true);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(true);
                lblreccomendation.setVisible(true);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 7) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(true);
                txtbd.setVisible(true);
                cmdbd.setVisible(true);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(true);
                lblreccomendation.setVisible(true);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            } else if (typeid == 8) {
                jPanel1.setVisible(false);
                lblpayments.setVisible(true);
                jscrollpayments.setVisible(true);
                lblbd.setVisible(true);
                txtbd.setVisible(true);
                cmdbd.setVisible(true);
                jscrolladditems.setVisible(false);
                lbladditems.setVisible(false);
                cmdadditem.setVisible(false);
                txtrecommendation.setVisible(true);
                lblreccomendation.setVisible(true);
                lbltotal.setVisible(true);
                txttotal.setVisible(true);
            }

            LoadPayments();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_cmbtypeActionPerformed

    private void cmdadditemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdadditemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmdadditemActionPerformed

    private void cmdbdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdbdActionPerformed
        SearchBillDeposit.acctnym = Nym;
        showFrmSearchBillDeposit();
    }//GEN-LAST:event_cmdbdActionPerformed

    private void o4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_o4ActionPerformed
        txtremarks.setVisible(true);
        txtremarks.requestFocus();
    }//GEN-LAST:event_o4ActionPerformed

    private void o1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_o1ActionPerformed
        txtremarks.setVisible(false);
    }//GEN-LAST:event_o1ActionPerformed

    private void o2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_o2ActionPerformed
        txtremarks.setVisible(false);
    }//GEN-LAST:event_o2ActionPerformed

    private void o3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_o3ActionPerformed
        txtremarks.setVisible(false);
    }//GEN-LAST:event_o3ActionPerformed

    private void cmbofficeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbofficeActionPerformed
        OrderBy = "";
        System.out.println(areaid);
        try {
            Item2 item = (Item2) cmboffice.getSelectedItem();
            areaid = item.getId();

            if (areaid == 1) {
                OrderBy = "ENGR. RONALD B. VISAGAS";
            } else if (areaid == 2) {
                OrderBy = "ENGR. SILVERIO P. ACASIO";
            } else if (areaid == 3) {
                OrderBy = "ENGR. ALDWIN R. GRAVADOR";
            }

            lblorder.setText(OrderBy);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_cmbofficeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CreateNewRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CreateNewRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CreateNewRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CreateNewRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CreateNewRequest dialog = new CreateNewRequest(frmParent, true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cmboffice;
    private javax.swing.JComboBox cmbtype;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdCreate;
    private javax.swing.JButton cmdadditem;
    private javax.swing.JButton cmdbd;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jscrolladditems;
    private javax.swing.JScrollPane jscrollpayments;
    private javax.swing.JLabel lbladditems;
    private javax.swing.JLabel lblbd;
    private javax.swing.JLabel lblorder;
    private javax.swing.JLabel lblpayments;
    private javax.swing.JLabel lblreccomendation;
    private javax.swing.JLabel lbltotal;
    private javax.swing.JRadioButton o1;
    private javax.swing.JRadioButton o2;
    private javax.swing.JRadioButton o3;
    private javax.swing.JRadioButton o4;
    private javax.swing.JTable tblMonth;
    private javax.swing.JTable tbladditems;
    private javax.swing.JTable tblpayments;
    private javax.swing.JTextField txtDM;
    private javax.swing.JTextField txtDM1;
    private javax.swing.JTextField txtDOR;
    private javax.swing.JTextField txtbd;
    private javax.swing.JTextField txtcn;
    private javax.swing.JTextField txtrecommendation;
    private javax.swing.JTextArea txtremarks;
    private javax.swing.JLabel txttotal;
    // End of variables declaration//GEN-END:variables
}
