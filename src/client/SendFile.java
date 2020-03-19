package client;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;


public class SendFile extends JFrame {
    public static int SEND = 0;
    int mode;
    File f;
    SendFile pt;
    InetAddress hostip;
    long filesize;
    DatagramSocket sock;
    JLabel img = new JLabel("", SwingConstants.CENTER);
    JProgressBar jpb = new JProgressBar();
    byte[] b;
    SocketAddress sa;
    Thread MyThread = new Thread() {
        public void run() {
            DatagramPacket p;
            String s = "";
            try {
                if (mode == SEND) {
                    while (s.startsWith("GETPIC") == false) {
                        b = new byte[65507];
                        p = new DatagramPacket(b, 65507);
                        sock.setSoTimeout(0);
                        sock.receive(p);
                        sa = p.getSocketAddress();
                        s = new String(b);
                    }
                    img.setText("connected to:" + sa);
                    s = f.getName() + ":" + filesize + "$$";
                    p = new DatagramPacket(s.getBytes(), s.length(), sa);
                    sock.send(p);
                    int l, sendCount;
                    boolean failed;
                    sock.setSoTimeout(65507);
                    FileInputStream fi = new FileInputStream(f);
                    l = 1;
                    img.setText("Sending image");
                    for (int i = 0; i < filesize; ) {
                        failed = false;
                        b = new byte[65507];
                        l = fi.read(b);
                        sendCount = 0;
                        do {
                            p = new DatagramPacket(b, l, sa);
                            sock.send(p);
                            sendCount++;
                            Thread.sleep(80);
                            try {
                                sock.receive(p);
                                s = new String(b);
                                if (s.contains("ACK") == false)
                                    throw new Exception();
                            } catch (Exception ex) {
                                failed = true;
                            }
                        } while (failed && sendCount < 5);
                        if (sendCount < 5) {
                            i += l;
                            jpb.setValue(i * 100 / (int) filesize);
                            jpb.setString(jpb.getValue() + " %");
                        } else {
                            JOptionPane.showMessageDialog(null, "Client is not receiving");
                            System.exit(0);
                        }
                    }
                    fi.close();
                    img.setText("Sending complete");
                }

            } catch (Exception ex) {
            }
        }
    };

    public SendFile(int mod, File fi, String ip) {
        mode = mod;
        try {
            if (mod == 0) {
                sock = new DatagramSocket(33780);
                f = fi;
                filesize = f.length();
                img.setText("<HTML><b>Waiting for a connection.<br/>IP: " + InetAddress.getLocalHost().getHostAddress() + "</b></HTML>");
            }
            MyThread.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        File ftemp;
//       ftemp = new File("arhat.jpeg");
//        new SendFile(0,ftemp,"");
        JFileChooser jfc = new JFileChooser();
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        FileFilter ff = new FileNameExtensionFilter("Image file", "jpg", "png", "bmp", "jpeg", "gif");
        jfc.addChoosableFileFilter(ff);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setMultiSelectionEnabled(false);
        jfc.setDialogTitle("Select a file to send");
        //ftemp = new File("arhat.jpeg");
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            ftemp = jfc.getSelectedFile();
            new SendFile(0, ftemp, "");
        }
    }


}