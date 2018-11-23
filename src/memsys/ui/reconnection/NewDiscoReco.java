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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import memsys.global.DBConn.MainDBConn;
import memsys.global.FunctionFactory;
import memsys.global.ReportFactory;
import memsys.global.myDataenvi;
import memsys.global.myFunctions;
import memsys.ui.main.ParentWindow;

/**
 *
 * @author LESTER JP CADIZ
 */
public class NewDiscoReco extends javax.swing.JInternalFrame {

    static Statement stmt;
    static DefaultTableCellRenderer cellAlignCenterRenderer = new DefaultTableCellRenderer();
    static DefaultTableModel model, model2;
    static DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    public static CreateNewRequest frmCreateNewRequest;
    static String nowDate = FunctionFactory.GetSystemNowDateString();
      static String nowDate2 = FunctionFactory.getSystemNowDateTimeString();

    public NewDiscoReco() {
        initComponents();
        model = (DefaultTableModel) tbl.getModel();
        model2 = (DefaultTableModel) tblrequest.getModel();

        tbl.setCellSelectionEnabled(false);
        tbl.setRowSelectionAllowed(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setSelectionBackground(new Color(153, 204, 255));
        tbl.setSelectionForeground(Color.BLACK);

        tblrequest.setCellSelectionEnabled(false);
        tblrequest.setRowSelectionAllowed(true);
        tblrequest.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblrequest.setSelectionBackground(new Color(153, 204, 255));
        tblrequest.setSelectionForeground(Color.BLACK);

        TableColumn idClmn2 = tblrequest.getColumn("type");
        idClmn2.setMaxWidth(0);
        idClmn2.setMinWidth(0);
        idClmn2.setPreferredWidth(0);

        TableColumn idClmn3 = tblrequest.getColumn("oid");
        idClmn3.setMaxWidth(0);
        idClmn3.setMinWidth(0);
        idClmn3.setPreferredWidth(0);
        
        TableColumn idClmn4 = tblrequest.getColumn("acctno");
        idClmn4.setMaxWidth(0);
        idClmn4.setMinWidth(0);
        idClmn4.setPreferredWidth(0);

        populateRequestTBL();
    }

    public void showFrmNewRequest() {
        frmCreateNewRequest = new CreateNewRequest(this, true);
        frmCreateNewRequest.setVisible(true);
    }

    String GetBillDeposit(int DRID) {
        String bd = "";

        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "SELECT BillDeposit FROM discoRecoTransTBL WHERE DiscoRecoID=" + DRID;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            while (rs.next()) {
                bd = rs.getString(1);
            }

            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
        return bd;
    }

    public void populateTBL() {
        Connection conn = MainDBConn.getConnection();
        String createString = "";

        createString = "SELECT c.AcctNo, AcctName, AcctAddress ,MSCode "
                + "FROM Consumer c INNER JOIN ConsumerMeter cm ON c.AcctNo=cm.AcctNo "
                + "INNER JOIN Meter m ON cm.MeterSN=m.MeterSN "
                + "WHERE c.acctname LIKE '%" + txtsearch.getText() + "%' OR c.AcctNo like '" + txtsearch.getText() + "%' "
                + "ORDER BY AcctName ";

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            //renderer.setHorizontalAlignment(0);
            tbl.setRowHeight(29);
            tbl.setColumnSelectionAllowed(false);
            model.setNumRows(0);

            int cnt = 0;
            while (rs.next()) {
                String Stat = "";
                if (rs.getInt(4) == 4) {
                    Stat = "Connected";
                } else if (rs.getInt(4) == 5) {
                    Stat = "Disconnected";
                }
                model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), Stat});
                cnt++;
            }
            if (cnt != 0) {
                tbl.setRowSelectionInterval(0, 0);
            }
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public void populateRequestTBL() {
        Connection conn = MainDBConn.getConnection();
        String createString = "SELECT t.DiscoRecoID, CONCAT(t.AcctNo,'/',c.TownCode,'-',c.RouteCode,'-',c.RouteSeqNo) as AcctNo, c.AcctName, c.AcctAddress, tt.TypeDesc, u.FullName, t.TypeID, t.OfficeFlg, t.AcctNo "
                + "FROM discoRecoTransTBL t "
                + "INNER JOIN Consumer c ON t.AcctNo=c.AcctNo "
                + "INNER JOIN discoRecoTransTypeTBL tt ON t.TypeID=tt.TypeID "
                + "INNER JOIN Users u ON t.UserID=u.UserID "
                + "WHERE t.status=12 "
                + "GROUP BY t.DiscoRecoID, t.AcctNo, c.AcctName, c.AcctAddress, tt.TypeDesc, u.FullName, c.TownCode, c.RouteCode, c.RouteSeqNo, t.TypeID, t.OfficeFlg";

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);

            tblrequest.setRowHeight(29);
            tblrequest.setColumnSelectionAllowed(false);
            model2.setNumRows(0);

            while (rs.next()) {
                String office = "";
                if (rs.getInt(8) == 1) {
                    office = "MAIN OFFICE";
                } else if (rs.getInt(8) == 2) {
                    office = "GUI OFFICE";
                }
                if (rs.getInt(8) == 3) {
                    office = "BAIS OFFICE";
                }

                model2.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), office, rs.getString(8), rs.getString(9)});
            }
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtsearch = new org.jdesktop.swingx.JXSearchField();
        jToolBar1 = new javax.swing.JToolBar();
        cmdCosting = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        cmdprint = new javax.swing.JButton();
        cmdApproved = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        cmdPreview2 = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        cmdPreview1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblrequest = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("New Request for Disco/Reco/Others");

        txtsearch.setLayoutStyle(org.jdesktop.swingx.JXSearchField.LayoutStyle.MAC);
        txtsearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtsearchActionPerformed(evt);
            }
        });

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToolBar1.setInheritsPopupMenu(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(500, 63));
        jToolBar1.setMinimumSize(new java.awt.Dimension(500, 63));

        cmdCosting.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdCosting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/add.png"))); // NOI18N
        cmdCosting.setMnemonic('C');
        cmdCosting.setText("     Create  Request    ");
        cmdCosting.setFocusable(false);
        cmdCosting.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cmdCosting.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdCosting.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdCosting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCostingActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdCosting);
        jToolBar1.add(jSeparator2);

        cmdprint.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdprint.setForeground(new java.awt.Color(0, 153, 255));
        cmdprint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/printer.png"))); // NOI18N
        cmdprint.setMnemonic('x');
        cmdprint.setText("       Print      ");
        cmdprint.setFocusable(false);
        cmdprint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdprint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdprint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdprintActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdprint);

        cmdApproved.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdApproved.setForeground(new java.awt.Color(0, 102, 153));
        cmdApproved.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/employer.png"))); // NOI18N
        cmdApproved.setMnemonic('S');
        cmdApproved.setText("    Send to Approval    ");
        cmdApproved.setFocusable(false);
        cmdApproved.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cmdApproved.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdApproved.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdApproved.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdApprovedActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdApproved);
        jToolBar1.add(jSeparator3);

        cmdPreview2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdPreview2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/remove.png"))); // NOI18N
        cmdPreview2.setMnemonic('w');
        cmdPreview2.setText("       Cancel Request      ");
        cmdPreview2.setFocusable(false);
        cmdPreview2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cmdPreview2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPreview2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdPreview2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdPreview2ActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdPreview2);
        jToolBar1.add(jSeparator4);

        cmdPreview1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdPreview1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/exit.png"))); // NOI18N
        cmdPreview1.setMnemonic('w');
        cmdPreview1.setText("    Exit    ");
        cmdPreview1.setFocusable(false);
        cmdPreview1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cmdPreview1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdPreview1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdPreview1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdPreview1ActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdPreview1);

        tbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Account No", "Account Name", "Account Address", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl.setToolTipText("");
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                tblMouseMoved(evt);
            }
        });
        jScrollPane1.setViewportView(tbl);
        //set column width
        tbl.getColumnModel().getColumn(0).setMaxWidth(80);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(300);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(300);

        tblrequest.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Request No.", "Account No", "Account Name", "Account Address", "Type of Request", "CWDO", "type", "Office", "oid", "acctno"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblrequest.setToolTipText("");
        tblrequest.getTableHeader().setReorderingAllowed(false);
        tblrequest.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                tblrequestMouseMoved(evt);
            }
        });
        jScrollPane2.setViewportView(tblrequest);
        tblrequest.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblrequest.getColumnModel().getColumn(3).setPreferredWidth(200);
        tblrequest.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblrequest.getColumnModel().getColumn(4).setPreferredWidth(180);
        tblrequest.getColumnModel().getColumn(5).setPreferredWidth(180);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Pending/Returned Request");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 980, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtsearch, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(txtsearch, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdCostingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCostingActionPerformed
        int col = 0; //set column value to 0
        int row = tbl.getSelectedRow(); //get value of selected value

        //trap user incase if no row selected
        if (tbl.isRowSelected(row) != true) {
            JOptionPane.showMessageDialog(this, "No record selected! Please a record from the list!");
            return;
        } else {
            String AcctNO = tbl.getValueAt(row, col).toString();
            String AcctNym = tbl.getValueAt(row, 1).toString();
            String Stat = tbl.getValueAt(row, 3).toString();
            CreateNewRequest.AcctNo = AcctNO;
            CreateNewRequest.Nym = AcctNym;
            CreateNewRequest.Stat = Stat;
            showFrmNewRequest();
        }
    }//GEN-LAST:event_cmdCostingActionPerformed

    private void cmdPreview1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPreview1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_cmdPreview1ActionPerformed

    private void tblMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMouseMoved

    }//GEN-LAST:event_tblMouseMoved

    private void txtsearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtsearchActionPerformed
        populateTBL();
    }//GEN-LAST:event_txtsearchActionPerformed

    private void cmdprintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdprintActionPerformed
        int col = 0; //set column value to 0
        int row = tblrequest.getSelectedRow(); //get value of selected value

        //trap user incase if no row selected
        if (tblrequest.isRowSelected(row) != true) {
            JOptionPane.showMessageDialog(this, "No record selected! Please select a record from the list!");
        } else {
            String id = tblrequest.getValueAt(row, col).toString();
            String tid = tblrequest.getValueAt(row, 6).toString();
            String ofis = tblrequest.getValueAt(row, 7).toString();
            String oid = tblrequest.getValueAt(row, 8).toString();

            String bd = GetBillDeposit(Integer.parseInt(id));
            System.out.print(bd);
            String lbl = "";
            if (bd.isEmpty() == true) {
                lbl = "";
            } else {
                lbl = " - Bill Deposit under O.R. No.: ";
            }

            int officeID = Integer.parseInt(oid);

            String OrderBy = "";
            if (officeID == 1) {
                OrderBy = "TSD Manager";
            } else {
                OrderBy = "Area Office Engineer";
            }

            if ("2".equals(tid)) {
                ReportFactory.rptReconnection(Integer.parseInt(id), lbl, ofis, OrderBy);
            }
        }
    }//GEN-LAST:event_cmdprintActionPerformed

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
    
    private void cmdApprovedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdApprovedActionPerformed
        int col = 0; //set column value to 0
        int row = tblrequest.getSelectedRow(); //get value of selected value

        //trap user incase if no row selected
        if (tblrequest.isRowSelected(row) != true) {
            JOptionPane.showMessageDialog(this, "No record selected! Please select a record from the list!");
        } else {
            String id = tblrequest.getValueAt(row, col).toString();
            String acctno = tblrequest.getValueAt(row, 9).toString();

            int x = myFunctions.msgboxYesNo("This request will now be send to ISM for approval!");
            switch (x) {
                case 0:
                    myDataenvi.rsUpdateConnStat(Integer.parseInt(acctno), 13);
                    myDataenvi.rsUpdateRequest(Integer.parseInt(id), 13);
                    rsAddConnLog(Integer.parseInt(acctno), "Re-Connection Request for Approval", 13, ParentWindow.getUserID(), nowDate2, "");
                    int uid = ParentWindow.getUserID();
                    myDataenvi.rsAddConnLog(Integer.parseInt(id), "Send to ISM for Approval", 13, uid, nowDate, "");
                    populateRequestTBL();
                    JOptionPane.showMessageDialog(this, "Record has been successfully sent!");
                    break;
                case 1:
                    break;
                case 2:
                    this.dispose(); //exit window
                    break;
                default:
            }

        }
    }//GEN-LAST:event_cmdApprovedActionPerformed

    private void tblrequestMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblrequestMouseMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_tblrequestMouseMoved

    private void cmdPreview2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPreview2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmdPreview2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdApproved;
    private javax.swing.JButton cmdCosting;
    private javax.swing.JButton cmdPreview1;
    private javax.swing.JButton cmdPreview2;
    private javax.swing.JButton cmdprint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTable tbl;
    private javax.swing.JTable tblrequest;
    private org.jdesktop.swingx.JXSearchField txtsearch;
    // End of variables declaration//GEN-END:variables
}
