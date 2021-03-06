package memsys.ui.devices;

import Module.Main.Transparent;
import com.WacomGSS.STU.ITabletHandler;
import com.WacomGSS.STU.Protocol.Capability;
import com.WacomGSS.STU.Protocol.DevicePublicKey;
import com.WacomGSS.STU.Protocol.EncodingMode;
import com.WacomGSS.STU.Protocol.EncryptionStatus;
import com.WacomGSS.STU.Protocol.Information;
import com.WacomGSS.STU.Protocol.InkingMode;
import com.WacomGSS.STU.Protocol.PenData;
import com.WacomGSS.STU.Protocol.PenDataEncrypted;
import com.WacomGSS.STU.Protocol.PenDataEncryptedOption;
import com.WacomGSS.STU.Protocol.PenDataOption;
import com.WacomGSS.STU.Protocol.PenDataTimeCountSequence;
import com.WacomGSS.STU.Protocol.PenDataTimeCountSequenceEncrypted;
import com.WacomGSS.STU.Protocol.ProtocolHelper;
import com.WacomGSS.STU.STUException;
import com.WacomGSS.STU.Tablet;
import com.WacomGSS.STU.UsbDevice;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import memsys.global.FTPFactory;
import static memsys.ui.devices.SignaturePad.cropMyImage;
import static memsys.ui.devices.SignaturePad.readImage;
import static memsys.ui.devices.SignaturePad.writeImage;
import memsys.ui.pmes.Attendance;

public class CurrentSignature extends javax.swing.JDialog {

    public static Attendance frmParent;
    public static String name;
    public static String memID;
    public static SignaturePad frmSignaturePad;
    JPanel imagePanel;
    BufferedImage signatureImage;
    private static final long serialVersionUID = 1L;
    Graphics gfx;

    public CurrentSignature(Attendance parent, boolean modal) {
        this.frmParent = parent;
        this.setModal(modal);
        initComponents();
        setLocationRelativeTo(this);
        lblname.setText(name + "' s");
        //insertpanel();
        // jPanel1.setVisible(false);
        //  cpanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Image", TitledBorder.LEADING, TitledBorder.TOP, null,
//                Color.BLACK));
//        cpanel.setVisible(false);
        // cpanel.setPreferredSize(new Dimension(300, 200));
    }

    public class SignatureDialog extends JDialog implements ITabletHandler {

        private static final long serialVersionUID = 1L;

        private Tablet tablet;
        private Capability capability;
        private Information information;

        // In order to simulate buttons, we have our own Button class that
        // stores the bounds and event handler.
        // Using an array of these makes it easy to add or remove buttons as
        // desired.
        private class Button {

            java.awt.Rectangle bounds; // in Screen coordinates
            String text;
            ActionListener click;

            void performClick() {
                click.actionPerformed(null);
            }

        }

        // The isDown flag is used like this:
        // 0 = up
        // +ve = down, pressed on button number
        // -1 = down, inking
        // -2 = down, ignoring
        private int isDown;

        private List<PenData> penData; // Array of data being stored. This can
        // be subsequently used as desired.

        private Button[] btns; // The array of buttons that we are emulating.

        private JPanel panel;

        private BufferedImage bitmap; // This bitmap that we display on the
        // screen.
        private EncodingMode encodingMode; // How we send the bitmap to the
        // device.
        private byte[] bitmapData; // This is the flattened data of the bitmap
        // that we send to the device.

        private Point2D.Float tabletToClient(PenData penData) {
            // Client means the panel coordinates.
            return new Point2D.Float((float) penData.getX()
                    * this.panel.getWidth() / this.capability.getTabletMaxX(),
                    (float) penData.getY() * this.panel.getHeight()
                    / this.capability.getTabletMaxY());
        }

        private Point2D.Float tabletToScreen(PenData penData) {
            // Screen means LCD screen of the tablet.
            return new Point2D.Float((float) penData.getX()
                    * this.capability.getScreenWidth()
                    / this.capability.getTabletMaxX(), (float) penData.getY()
                    * this.capability.getScreenHeight()
                    / this.capability.getTabletMaxY());
        }

        private Point clientToScreen(Point pt) {
            // client (window) coordinates to LCD screen coordinates.
            // This is needed for converting mouse coordinates into LCD bitmap
            // coordinates as that's
            // what this application uses as the coordinate space for buttons.
            return new Point(
                    Math.round((float) pt.getX()
                            * this.capability.getScreenWidth()
                            / this.panel.getWidth()), Math.round((float) pt
                    .getY()
                    * this.capability.getScreenHeight()
                    / this.panel.getHeight()));
        }

        private void pressOkButton() throws STUException {
            UpdateIMG();

            this.setVisible(false);
        }

        private void pressClearButton() throws STUException {
            clearScreen();
        }

        private void pressCancelButton() throws STUException {
            this.setVisible(false);
            this.penData = null;
        }

        private void clearScreen() throws STUException {
            this.tablet.writeImage(this.encodingMode, this.bitmapData);

            this.penData.clear();
            this.isDown = 0;
            this.panel.repaint();
        }

        public void dispose() {
            // Ensure that you correctly disconnect from the tablet, otherwise
            // you are
            // likely to get errors when wanting to connect a second time.
            if (this.tablet != null) {
                try {
                    this.tablet.setInkingMode(InkingMode.Off);
                    this.tablet.setClearScreen();
                } catch (Throwable t) {
                }
                this.tablet.disconnect();
                this.tablet = null;
            }

            super.dispose();
        }

        private void drawCenteredString(Graphics2D gfx, String text, int x,
                int y, int width, int height) {
            FontMetrics fm = gfx.getFontMetrics(gfx.getFont());
            int textHeight = fm.getHeight();
            int textWidth = fm.stringWidth(text);

            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - textHeight) / 2 + fm.getAscent();

            gfx.drawString(text, textX, textY);
        }

        private void drawInk(Graphics2D gfx, PenData pd0, PenData pd1) {
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            gfx.setColor(new Color(0, 0, 64, 255));
            gfx.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));

            Point2D.Float pt0 = tabletToClient(pd0);
            Point2D.Float pt1 = tabletToClient(pd1);
            Shape l = new Line2D.Float(pt0, pt1);
            gfx.draw(l);
        }

        private void drawInk(Graphics2D gfx) {
            PenData[] pd = new PenData[0];
            pd = this.penData.toArray(pd);
            for (int i = 1; i < pd.length; ++i) {
                if (pd[i - 1].getSw() != 0 && pd[i].getSw() != 0) {
                    drawInk(gfx, pd[i - 1], pd[i]);
                }
            }
        }

        // Pass in the device you want to connect to!
        public SignatureDialog(JDialog frame, UsbDevice usbDevice)
                throws STUException {
            super(frame, true);
            this.setLocation(new Point(0, 0));
            this.setLocationRelativeTo(frame);
            this.panel = new JPanel() {
                private static final long serialVersionUID = 1L;

                @Override
                public void paintComponent(Graphics gfx) {
                    super.paintComponent(gfx);
                    if (bitmap != null) {
                        Image rescaled = bitmap.getScaledInstance(
                                panel.getWidth(), panel.getHeight(),
                                Image.SCALE_SMOOTH);
                        gfx.drawImage(rescaled, 0, 0, null);
                        drawInk((Graphics2D) gfx);
                    }
                }
            };
            this.panel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    Point pt = clientToScreen(e.getPoint());
                    for (Button btn : SignatureDialog.this.btns) {
                        if (btn.bounds.contains(pt)) {
                            btn.performClick();
                            break;
                        }
                    }
                }
            });

            this.penData = new ArrayList<PenData>();

            try {
                this.tablet = new Tablet();
                // A more sophisticated applications should cycle for a few
                // times as the connection may only be
                // temporarily unavailable for a second or so.
                // For example, if a background process such as Wacom STU
                // Display
                // is running, this periodically updates a slideshow of images
                // to the device.

                int e = tablet.usbConnect(usbDevice, true);
                if (e == 0) {
                    this.capability = tablet.getCapability();
                    this.information = tablet.getInformation();
                } else {
                    throw new RuntimeException(
                            "Failed to connect to USB tablet, error " + e);
                }

                // Set the size of the client window to be actual size,
                // based on the reported DPI of the monitor.
                int screenResolution = this.getToolkit().getScreenResolution();

                Dimension d = new Dimension(this.capability.getTabletMaxX()
                        * screenResolution / 2540,
                        this.capability.getTabletMaxY() * screenResolution
                        / 2540);
                this.panel.setPreferredSize(d);
                this.setLayout(new BorderLayout());
                this.setResizable(false);
                this.add(this.panel);
                this.pack();

                this.btns = new Button[3];
                this.btns[0] = new Button();
                this.btns[1] = new Button();
                this.btns[2] = new Button();

                if (this.tablet.getProductId() != UsbDevice.ProductId_300) {
                    // Place the buttons across the bottom of the screen.

                    int w2 = this.capability.getScreenWidth() / 3;
                    int w3 = this.capability.getScreenWidth() / 3;
                    int w1 = this.capability.getScreenWidth() - w2 - w3;
                    int y = this.capability.getScreenHeight() * 6 / 7;
                    int h = this.capability.getScreenHeight() - y;

                    btns[0].bounds = new java.awt.Rectangle(0, y, w1, h);
                    btns[1].bounds = new java.awt.Rectangle(w1, y, w2, h);
                    btns[2].bounds = new java.awt.Rectangle(w1 + w2, y, w3, h);
                } else {
                    // The STU-300 is very shallow, so it is better to utilise
                    // the buttons to the side of the display instead.

                    int x = this.capability.getScreenWidth() * 3 / 4;
                    int w = this.capability.getScreenWidth() - x;

                    int h2 = this.capability.getScreenHeight() / 3;
                    int h3 = this.capability.getScreenHeight() / 3;
                    int h1 = this.capability.getScreenHeight() - h2 - h3;

                    btns[0].bounds = new java.awt.Rectangle(x, 0, w, h1);
                    btns[1].bounds = new java.awt.Rectangle(x, h1, w, h2);
                    btns[2].bounds = new java.awt.Rectangle(x, h1 + h2, w, h3);
                }
                btns[0].text = "OK";
                btns[1].text = "Clear";
                btns[2].text = "Cancel";
                btns[0].click = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            pressOkButton();
                        } catch (STUException e) {
                            // e
                        }
                    }
                };

                btns[1].click = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            pressClearButton();
                        } catch (STUException e) {
                            // e
                        }
                    }
                };

                btns[2].click = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            pressCancelButton();
                        } catch (STUException e) {
                            // e
                        }
                    }
                };

                byte encodingFlag = ProtocolHelper.simulateEncodingFlag(
                        this.tablet.getProductId(),
                        this.capability.getEncodingFlag());

                boolean useColor = ProtocolHelper
                        .encodingFlagSupportsColor(encodingFlag);

                // Disable color if the bulk driver isn't installed.
                // This isn't necessary, but uploading colour images with out
                // the driver
                // is very slow.
                useColor = useColor && this.tablet.supportsWrite();

                // Calculate the encodingMode that will be used to update the
                // image
                if (useColor) {
                    if (this.tablet.supportsWrite()) {
                        this.encodingMode = EncodingMode.EncodingMode_16bit_Bulk;
                    } else {
                        this.encodingMode = EncodingMode.EncodingMode_16bit;
                    }
                } else {
                    this.encodingMode = EncodingMode.EncodingMode_1bit;
                }

                // Size the bitmap to the size of the LCD screen.
                // This application uses the same bitmap for both the screen and
                // client (window).
                // However, at high DPI, this bitmap will be stretch and it
                // would be better to
                // create individual bitmaps for screen and client at native
                // resolutions.
                this.bitmap = new BufferedImage(
                        this.capability.getScreenWidth(),
                        this.capability.getScreenHeight(),
                        BufferedImage.TYPE_INT_RGB);
                {
                    Graphics2D gfx = bitmap.createGraphics();
                    gfx.setColor(Color.WHITE);
                    gfx.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());

                    double fontSize = (this.btns[0].bounds.getHeight() / 2.0); // pixels
                    gfx.setFont(new Font("Arial", Font.PLAIN, (int) fontSize));

                    // Draw the buttons
                    for (Button btn : this.btns) {
                        if (useColor) {
                            gfx.setColor(Color.LIGHT_GRAY);
                            gfx.fillRect((int) btn.bounds.getX(),
                                    (int) btn.bounds.getY(),
                                    (int) btn.bounds.getWidth(),
                                    (int) btn.bounds.getHeight());
                        }
                        gfx.setColor(Color.BLACK);
                        gfx.drawRect((int) btn.bounds.getX(),
                                (int) btn.bounds.getY(),
                                (int) btn.bounds.getWidth(),
                                (int) btn.bounds.getHeight());
                        drawCenteredString(gfx, btn.text,
                                (int) btn.bounds.getX(),
                                (int) btn.bounds.getY(),
                                (int) btn.bounds.getWidth(),
                                (int) btn.bounds.getHeight());
                    }

                    gfx.dispose();
                }

                // Now the bitmap has been created, it needs to be converted to
                // device-native
                // format.
                this.bitmapData = ProtocolHelper.flatten(this.bitmap,
                        this.bitmap.getWidth(), this.bitmap.getHeight(),
                        useColor);

                // If you wish to further optimize image transfer, you can
                // compress the image using
                // the Zlib algorithm.
                boolean useZlibCompression = false;

                if (!useColor && useZlibCompression) {
                    // m_bitmapData = compress_using_zlib(m_bitmapData); //
                    // insert compression here!
                    // m_encodingMode = EncodingMode.EncodingMode_1bit_Zlib;
                }

                // Add the delagate that receives pen data.
                this.tablet.addTabletHandler(this);

                // Initialize the screen
                clearScreen();

                // Enable the pen data on the screen (if not already)
                this.tablet.setInkingMode(InkingMode.On);
            } catch (Throwable t) {
                if (this.tablet != null) {
                    this.tablet.disconnect();
                    this.tablet = null;
                }
                throw t;
            }
        }

        public void onGetReportException(STUException e) {
            JOptionPane.showMessageDialog(this, "Error:" + e,
                    "Error (onGetReportException)", JOptionPane.ERROR_MESSAGE);
            this.tablet.disconnect();
            this.tablet = null;
            this.penData = null;
            this.setVisible(false);
        }

        public void onUnhandledReportData(byte[] data) {
        }

        public void onPenData(PenData penData) {
            Point2D pt = tabletToScreen(penData);

            int btn = 0; // will be +ve if the pen is over a button.
            {
                for (int i = 0; i < this.btns.length; ++i) {
                    if (this.btns[i].bounds.contains(pt)) {
                        btn = i + 1;
                        break;
                    }
                }
            }

            boolean isDown = (penData.getSw() != 0);

            // This code uses a model of four states the pen can be in:
            // down or up, and whether this is the first sample of that state.
            if (isDown) {
                if (this.isDown == 0) {
                    // transition to down
                    if (btn > 0) {
                        // We have put the pen down on a button.
                        // Track the pen without inking on the client.

                        this.isDown = btn;
                    } else {
                        // We have put the pen down somewhere else.
                        // Treat it as part of the signature.

                        this.isDown = -1;
                    }
                } else // already down, keep doing what we're doing!
                // draw
                if (!this.penData.isEmpty() && this.isDown == -1) {
                    // Draw a line from the previous down point to this down
                    // point.
                    // This is the simplist thing you can do; a more
                    // sophisticated program
                    // can perform higher quality rendering than this!
                    Graphics2D gfx = (Graphics2D) this.panel.getGraphics();
                    drawInk(gfx, this.penData.get(this.penData.size() - 1),
                            penData);
                    gfx.dispose();
                }

                // The pen is down, store it for use later.
                if (this.isDown == -1) {
                    this.penData.add(penData);
                }
            } else {
                if (this.isDown != 0) {
                    // transition to up
                    if (btn > 0) {
                        // The pen is over a button

                        if (btn == this.isDown) {
                            // The pen was pressed down over the same button as
                            // is was lifted now.
                            // Consider that as a click!
                            this.btns[btn - 1].performClick();
                        }
                    }
                    this.isDown = 0;
                } else {
                    // still up
                }

                // Add up data once we have collected some down data.
                if (!this.penData.isEmpty()) {
                    this.penData.add(penData);
                }
            }

        }

        public void onPenDataOption(PenDataOption penDataOption) {
            onPenData(penDataOption);
        }

        public void onPenDataEncrypted(PenDataEncrypted penDataEncrypted) {
            onPenData(penDataEncrypted.getPenData1());
            onPenData(penDataEncrypted.getPenData2());
        }

        public void onPenDataEncryptedOption(
                PenDataEncryptedOption penDataEncryptedOption) {
            onPenData(penDataEncryptedOption.getPenDataOption1());
            onPenData(penDataEncryptedOption.getPenDataOption2());
        }

        public void onPenDataTimeCountSequence(
                PenDataTimeCountSequence penDataTimeCountSequence) {
            onPenData(penDataTimeCountSequence);
        }

        public void onPenDataTimeCountSequenceEncrypted(
                PenDataTimeCountSequenceEncrypted penDataTimeCountSequenceEncrypted) {
            onPenData(penDataTimeCountSequenceEncrypted);
        }

        public void onEncryptionStatus(EncryptionStatus encryptionStatus) {
        }

        public void onDevicePublicKey(DevicePublicKey devicePublicKey) {
        }

        public PenData[] getPenData() {
            if (this.penData != null) {
                PenData[] arrayPenData = new PenData[0];
                return this.penData.toArray(arrayPenData);
            }
            return null;
        }

        public Information getInformation() {
            if (this.penData != null) {
                return this.information;
            }
            return null;
        }

        public Capability getCapability() {
            if (this.penData != null) {
                return this.capability;
            }
            return null;
        }

    }

    void UpdateIMG() {

        Transparent ti = new Transparent();
        try {
            ti.croppedNow();
        } catch (Exception ex) {
            Logger.getLogger(CurrentSignature.class.getName()).log(Level.SEVERE, null, ex);
        }
        String path = getSigPathConfig();
        BufferedImage img = readImage("img/croppedsig/img.png");
        writeImage(img, path + memID + ".png", "PNG");
    }

    public BufferedImage readImage(String fileLocation) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileLocation));
            //System.out.println("Image Read. Image Dimension: " + img.getWidth()
            // + "w X " + img.getHeight() + "h");
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return img;
    }

    public void writeImage(BufferedImage img, String fileLocation,
            String extension) {
        try {
            BufferedImage bi = img;
            File outputfile = new File(fileLocation);
            ImageIO.write(bi, extension, outputfile);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private Point2D.Float tabletToClient(PenData penData, Capability capability, JPanel panel) {
        // Client means the panel coordinates.
        return new Point2D.Float((float) penData.getX()
                * panel.getWidth() / capability.getTabletMaxX(),
                (float) penData.getY() * panel.getHeight()
                / capability.getTabletMaxY());
    }

//    private BufferedImage createImage(PenData[] penData, Capability capability, Information information) {
//        BufferedImage bi = new BufferedImage(capability.getScreenWidth(), capability.getScreenHeight(), BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = (Graphics2D) bi.getGraphics();
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
//        g.setColor(new Color(0, 0, 64, 255));
//        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//
//        for (int i = 1; i < penData.length; i++) {
//            PenData p1 = penData[i];
//            if (p1.getSw() != 0) {
//                Point2D.Float pt1 = tabletToClient(penData[i - 1], capability, imagePanel);
//                Point2D.Float pt2 = tabletToClient(penData[i], capability, imagePanel);
//                Shape l = new Line2D.Float(pt1, pt2);
//                g.draw(l);
//                
//                                       try {
//                if (ImageIO.write(bi, "png", new File("./img/capturedsig/img.png"))) {
//                    //System.out.println("-- saved");
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            }
//        }
//
//        return bi;
//
//    }
    private BufferedImage createImage(PenData[] penData, Capability capability, Information information) {
        BufferedImage bi = new BufferedImage(capability.getScreenWidth(), capability.getScreenHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g.setColor(new Color(0, 0, 64, 255));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        for (int i = 1; i < penData.length; i++) {
            PenData p1 = penData[i];
            if (p1.getSw() != 0) {
                Point2D.Float pt1 = tabletToClient(penData[i - 1], capability, cpanel);
                Point2D.Float pt2 = tabletToClient(penData[i], capability, cpanel);
                Shape l = new Line2D.Float(pt1, pt2);
                g.draw(l);

                try {
                    if (ImageIO.write(bi, "png", new File("img/capturedsig/img.png"))) {
                        // System.out.println("saved");
                        crop();
                        Transparent ti = new Transparent();
                        try {
                            ti.croppedNow();
                        } catch (Exception ex) {
                            Logger.getLogger(CurrentSignature.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        return bi;

    }

    void crop() {
        BufferedImage originalImage = readImage("img/capturedsig/img.png");
        try {
            BufferedImage processedImage = cropMyImage(originalImage, 240, 160, 0, 0);
            writeImage(processedImage, "img/croppedsig/img.png", "PNG");
        } catch (Exception ex) {
            Logger.getLogger(SignaturePad.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getSigPathConfig() {
        String Str = memsys.others.paths.getSigPathConfig();
        return Str;
    }

//    private void onGetSignature() throws STUException {
//        try {
//
//            com.WacomGSS.STU.UsbDevice[] usbDevices = UsbDevice.getUsbDevices();
//
//            if (usbDevices != null && usbDevices.length > 0) {
//                SignatureDialog signatureDialog = new SignatureDialog(this, usbDevices[0]);
//
//                signatureDialog.setVisible(true);
//
//                PenData[] penData = signatureDialog.getPenData();
//                if (penData != null && penData.length > 0) {
//                    // collected data!
//                    this.signatureImage = createImage(penData,
//                            signatureDialog.getCapability(),
//                            signatureDialog.getInformation());
//                    
//                    
//                  // UpdateIMG();
//
//                    
//                    cpanel.repaint();
//                }
//                // insertpanel();
//                signatureDialog.dispose();
//
//            } else {
//                throw new RuntimeException("No USB tablets attached");
//            }
//        } catch (RuntimeException e) {
//            e.getMessage();
//        }
//    }
    private void onGetSignature() {
        try {

            com.WacomGSS.STU.UsbDevice[] usbDevices = UsbDevice.getUsbDevices();

            if (usbDevices != null && usbDevices.length > 0) {
                SignatureDialog signatureDialog = new SignatureDialog(this,
                        usbDevices[0]);

                signatureDialog.setVisible(true);

                PenData[] penData = signatureDialog.getPenData();
                if (penData != null && penData.length > 0) {
                    // collected data!
                    this.signatureImage = createImage(penData,
                            signatureDialog.getCapability(),
                            signatureDialog.getInformation());
                    //    cpanel.repaint();
                }
                signatureDialog.dispose();

//                try {
//                    String path = getSigPathConfig();
//                    BufferedImage img = readImage("img/croppedsig/img.png");
//                    writeImage(img, path + memID + ".png", "PNG");
                    FTPFactory i = new FTPFactory();
                    i.FTPSaveImage("img/croppedsig/img.png", "img/signature/"+ memID + ".png");
                    showsig();
//                    JOptionPane.showMessageDialog(null, "Profile Picture Successfully Saved!");
//                } catch (Exception e) {
//                    JOptionPane.showMessageDialog(null, "Failed! Please check your network settings");
//                }
            } else {
                throw new RuntimeException("No USB tablets attached");
            }
        } catch (STUException e) {
            JOptionPane.showMessageDialog(this, e, "Error (STU)",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e, "Error (RT)",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void showFrmSP() {
        frmSignaturePad = new SignaturePad(this, true);
        frmSignaturePad.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        cmdEdit = new javax.swing.JButton();
        cmdExit = new javax.swing.JButton();
        lblname = new javax.swing.JLabel();
        capturer = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cmdEdit1 = new javax.swing.JButton();
        cpanel = new javax.swing.JPanel();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Current Signature");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        cmdEdit.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        cmdEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/edit.png"))); // NOI18N
        cmdEdit.setMnemonic('C');
        cmdEdit.setText("Bamboo");
        cmdEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdEditActionPerformed(evt);
            }
        });

        cmdExit.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        cmdExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/exit.png"))); // NOI18N
        cmdExit.setMnemonic('x');
        cmdExit.setText("Exit");
        cmdExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdExitActionPerformed(evt);
            }
        });

        lblname.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblname.setForeground(new java.awt.Color(102, 0, 102));
        lblname.setText("jLabel1");

        capturer.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Current Signature");

        cmdEdit1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        cmdEdit1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/edit.png"))); // NOI18N
        cmdEdit1.setMnemonic('C');
        cmdEdit1.setText("Wacom");
        cmdEdit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdEdit1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cpanelLayout = new javax.swing.GroupLayout(cpanel);
        cpanel.setLayout(cpanelLayout);
        cpanelLayout.setHorizontalGroup(
            cpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        cpanelLayout.setVerticalGroup(
            cpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 87, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(capturer, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(cmdExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cmdEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(cmdEdit1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblname)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(capturer, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmdEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdEdit1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdExit)
                        .addGap(5, 5, 5)
                        .addComponent(cpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdEditActionPerformed

        SignaturePad.memID = memID;
        //this.dispose();
        showFrmSP();


    }//GEN-LAST:event_cmdEditActionPerformed

    private void cmdExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_cmdExitActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        showsig();
    }//GEN-LAST:event_formWindowOpened

    public  void showsig() {
        lblname.setText(name + "' s");
        //String path = getSigPathConfig();
        // File file = new File("192.168.1.192/img/29754.jpg");
        // File file = new File(path + memID + ".png");
        FTPFactory i = new FTPFactory();
        i.FTPViewImage(i.GetFTPSigPath() + Integer.parseInt(memID) + ".png", capturer);
    }

    private void cmdEdit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdEdit1ActionPerformed
        onGetSignature();
    }//GEN-LAST:event_cmdEdit1ActionPerformed

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
            java.util.logging.Logger.getLogger(CurrentSignature.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CurrentSignature.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CurrentSignature.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CurrentSignature.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CurrentSignature dialog = new CurrentSignature(frmParent, true);
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
    private javax.swing.JLabel capturer;
    private javax.swing.JButton cmdEdit;
    private javax.swing.JButton cmdEdit1;
    private javax.swing.JButton cmdExit;
    private javax.swing.JPanel cpanel;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblname;
    // End of variables declaration//GEN-END:variables
}
