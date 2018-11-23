/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memsys.ui.overide;

import memsys.global.DBConn.MainDBConn;
import memsys.ui.process.IssueCO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author LESTER
 */
public class override_payment extends javax.swing.JDialog {

    public static IssueCO frmParent;
    static Statement stmt;
    public static int acctno;

    public override_payment(IssueCO parent, boolean modal) {
        this.frmParent = parent;
        this.setModal(modal);
        initComponents();
        setLocationRelativeTo(this);
        getRootPane().setDefaultButton(cmdo);
    }

    int GetTransID(int orno) {
        int tID = 0;
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString
                = "    SELECT transID FROM "
                + " 	  ("
                + "             SELECT CollectionTrans.TransID AS transID, ORNo "
                + "             FROM CollectionTrans "
                + "             INNER JOIN CollectionMisc ON CollectionTrans.TransID = CollectionMisc.TransID "
                + "                 WHERE ORNo="+orno+" "
               //   + "    			AND CollectionMisc.COAID=31   "
                + "   			AND CollectionTrans.TransID  "
                + "                     NOT IN (SELECT TransID FROM CollectionTransCancelled) "
                + "       UNION  "
                + " 	 (       "
                + "             SELECT  ORTrans.ORtransID as transID, ORNo "
                + "   		FROM ORTrans  "
                + "   		INNER JOIN ORTransDetail ON ORTrans.ORTransID = ORTransDetail.ORTransID  "
                + "  		    WHERE ORNo="+orno+" "
               // + "                     AND ORTransDetail.COAID=31   "
                + "   			AND ORTrans.ORTransID NOT IN (SELECT ORTransID  "
                + "   			FROM ORTransCancelled)  "
                + "   			AND ORTrans.ORTransID NOT IN (SELECT ORTransID  "
                + "                     FROM ORTrans  "
                + "   			WHERE ORTransID IN (SELECT ORTransID FROM CollectionTrans)) "
                + "        ))  DATATBL "
                + "        WHERE ORNo='"+orno+"' "
                + "        GROUP BY transID ";
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(createString);
            while (rs.next()) {
                tID = rs.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
        return tID;
    }

    public static void PostTransID(int tid) {
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "update costingTBL set TransID=" + tid + " WHERE AcctNo=" + acctno;

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createString);
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
     public static void UpdateOverrideFlag(int tid) {
        Connection conn = MainDBConn.getConnection();
        String createString;
        createString = "update connTBL set override='OVERRIDE' WHERE AcctNo=" + acctno;

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

        jLabel1 = new javax.swing.JLabel();
        txtor = new javax.swing.JTextField();
        cmdo = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Override Payment");
        setResizable(false);

        jLabel1.setText("OR No.");

        txtor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtorActionPerformed(evt);
            }
        });
        txtor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtorKeyPressed(evt);
            }
        });

        cmdo.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/payslip.png"))); // NOI18N
        cmdo.setText("Override");
        cmdo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdoActionPerformed(evt);
            }
        });

        cmdCancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/exit.png"))); // NOI18N
        cmdCancel.setText("Cancel");
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmdo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdCancel))
                    .addComponent(txtor))
                .addGap(85, 85, 85))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(txtor, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cmdCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmdo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(26, 26, 26))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtorActionPerformed

    }//GEN-LAST:event_txtorActionPerformed

    private void txtorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtorKeyPressed

    }//GEN-LAST:event_txtorKeyPressed

    private void cmdoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdoActionPerformed

        if (txtor.getText().isEmpty() == true) {
            JOptionPane.showMessageDialog(this, "OR No. is required!");
        } else {
            int transid = GetTransID(Integer.parseInt(txtor.getText()));
//            if (transid == 0) {
//                JOptionPane.showMessageDialog(this, "OR No. not found!");
//            } else {
                PostTransID(transid);
                UpdateOverrideFlag(transid);
                this.dispose();
                JOptionPane.showMessageDialog(this, "Account successfully override!");
                frmParent.populateTBL();
//            }
        }
    }//GEN-LAST:event_cmdoActionPerformed

    private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_cmdCancelActionPerformed

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
            java.util.logging.Logger.getLogger(override_payment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(override_payment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(override_payment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(override_payment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                override_payment dialog = new override_payment(frmParent, true);
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
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField txtor;
    // End of variables declaration//GEN-END:variables
}
