package KeywordScan;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class FileScanClient {



//    public static void main(String[] args) throws Exception {
//        ClientBean clientBean = new ClientBean();
//        Socket socket = new Socket("192.168.1.154",6111);
//        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//        String localHost = getIpAddress();
//        clientBean.setIp(localHost);
//        List<String> data = Fileutil.scanStart();
//        clientBean.setFilePathName(data);
//        System.out.println("运行完毕");
//        oos.writeObject(clientBean);
//        oos.writeObject(null);
//        oos.flush();
//        oos.close();
//        socket.close();
//    }


    public static void main(String[] args) {
        ClientBean clientBean = new ClientBean();
        String localHost = getIpAddress();
        clientBean.setIp(localHost);
        List<String> data = Fileutil.scanStart();
        clientBean.setFilePathName(data);
        if (clientBean!=null){
            generateRreport(clientBean);
        }


    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip ;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }
    /**
     * 将接受到的数据生成报告
     */
    private static void generateRreport(ClientBean clientBean){
        String fileName = clientBean.getIp();
        List<String> filePathName = clientBean.getFilePathName();
        File desktopDir = FileSystemView.getFileSystemView() .getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath();
        desktopPath=fileName+"敏感信息报告.docx";
        System.out.println(desktopPath);

        try {
            FileOutputStream out = new FileOutputStream(desktopPath); //创建输出流
            XWPFDocument doc = new XWPFDocument(); //创建word文件
            XWPFParagraph p1 = doc.createParagraph(); //创建段落
            XWPFRun r1 = p1.createRun(); //创建段落文本
            for (String str: filePathName) {
                p1.setAlignment(ParagraphAlignment.LEFT);
                r1.setText(str); //设置文本
                r1.addCarriageReturn();
                r1.setText("---------------------------------------------------");
                r1.addCarriageReturn();
            }
            r1.setFontFamily("仿宋");
            r1.setFontSize(15);// 字体大小
            doc.write(out);  //输出
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
