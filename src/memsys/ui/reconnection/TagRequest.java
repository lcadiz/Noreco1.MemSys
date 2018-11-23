/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memsys.ui.reconnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import memsys.global.DBConn.MainDBConn;
import memsys.global.FunctionFactory;
import memsys.global.myDataenvi;
import static memsys.global.myDataenvi.rsAddConnLog;
import memsys.global.myFunctions;
import memsys.ui.main.ParentWindow;

/**
 *
 * @author engkoicadiz
 */
public class TagRequest extends javax.swing.JDialog {

    public static ForAction frmParent;
    public static int acctno, id;
    static Statement stmt;
    static String nowDate = FunctionFactory.getSystemNowDateTimeString();
    static String nowDate2 = FunctionFactory.getSystemNowDateTimeString();
    

    public TagRequest(ForAction parent, boolean modal) {
        this.frmParent = parent;
        this.setModal(modal);
        initComponents();
        setLocationRelativeTo(this);
        setdates();
    }

    void setdates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date theDate = null;
        try {
            theDate = sdf.parse(nowDate);
        } catch (ParseException e) {
        }
        //txtdate.setDateFormatString(nowDate);
        txtdate.setDate(theDate);
        //  txtend.setDate(theDate);
    }

    
    
    public static void rsUpdateRequest(int id, int Stat) {
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "UPDATE DiscoRecoTransTBL SET "
                + "status='" + Stat + "' WHERE DiscoRecoID=" + id;

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Ok = new javax.swing.JButton();
        Ok1 = new javax.swing.JButton();
        txtdate = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Tag Request");

        Ok.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        Ok.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/earn.png"))); // NOI18N
        Ok.setText("Tag as Acted");
        Ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkActionPerformed(evt);
            }
        });

        Ok1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        Ok1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/exit.png"))); // NOI18N
        Ok1.setText("Cancel");
        Ok1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Ok1ActionPerformed(evt);
            }
        });

        txtdate.setDateFormatString("yyyy/MM/dd ");
        txtdate.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N

        jLabel1.setText("Date Installed:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Ok1))
                    .addComponent(txtdate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtdate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Ok)
                    .addComponent(Ok1))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkActionPerformed
            int x = myFunctions.msgboxYesNo("This request will now be tag as acted!");
            switch (x) {
                case 0:
                    myDataenvi.rsUpdateConnStat(acctno, 15);
                    myDataenvi.rsUpdateRequest(id, 15);
                    rsAddConnLog(acctno, "Re-Connection Request Acted", 15, ParentWindow.getUserID(), nowDate2, "");
                    int uid = ParentWindow.getUserID();
                    myDataenvi.rsAddConnLog(id, "Request Acted", 15, uid, nowDate, "");
                    frmParent.populateRequestTBL();
                    JOptionPane.showMessageDialog(this, "Record has been successfully sent!");
                    break;
                case 1:
                    break;
                case 2:
                    this.dispose(); //exit window
                    break;
                default:
            }
    }//GEN-LAST:event_OkActionPerformed

    private void Ok1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Ok1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_Ok1ActionPerformed

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
            java.util.logging.Logger.getLogger(TagRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TagRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TagRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TagRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TagRequest dialog = new TagRequest(frmParent, true);
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
    private javax.swing.JButton Ok;
    private javax.swing.JButton Ok1;
    private javax.swing.JLabel jLabel1;
    private com.toedter.calendar.JDateChooser txtdate;
    // End of variables declaration//GEN-END:variables
}
