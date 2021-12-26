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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class FileScanClient {


    public static void main(String[] args) {
        long starTime=System.currentTimeMillis();

//        ClientBean clientBean = new ClientBean();
//        String localHost = getIpAddress();
//        clientBean.setIp(localHost);
//        List<String> data = scanStart();
          scanStart();
//        clientBean.setFilePathName(data);

        long endTime=System.currentTimeMillis();
        long Time=endTime-starTime;
        System.out.println(Time);
//
//        if (clientBean!=null){
//            generateRreport(clientBean);
//        }
    }

    /**
     * 开始扫描 返回扫描结果
     * @return
     */
    public static List<String> scanStart(){
        Properties properties = System.getProperties();
        String property = properties.getProperty("os.name");
        List<String> data =new ArrayList<String>();
        if (property.toLowerCase().contains("windows")){
            data= Fileutil.searchFileType("windows");

        }else {
            data= Fileutil.searchFileType("linux");
        }
        return data;
    }

    /**
     * 获取当前IP
     * @return
     */
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
     * 暂时搁置不用
     */
    private static void generateRreport(ClientBean clientBean){
        String fileName = clientBean.getIp();
        List<String> filePathName = clientBean.getFilePathName();
        File desktopDir = FileSystemView.getFileSystemView() .getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath(); //获取桌面路径  暂时没用 下面生产在当前目录下更方便
        desktopPath=fileName+"敏感信息报告.docx";
        System.out.println(desktopPath);

        try {
            FileOutputStream out = new FileOutputStream(desktopPath); //创建输出流
            XWPFDocument doc = new XWPFDocument(); //创建word文件
            XWPFParagraph p1 = doc.createParagraph(); //创建段落
            XWPFRun r1 = p1.createRun(); //创建段落文本
            for (String str: filePathName) {
                p1.setAlignment(ParagraphAlignment.LEFT); //追加在文档末尾处写入
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
