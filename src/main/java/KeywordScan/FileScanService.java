//package filescan;
//
//import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.apache.poi.xwpf.usermodel.XWPFParagraph;
//import org.apache.poi.xwpf.usermodel.XWPFRun;
//
//import javax.swing.filechooser.FileSystemView;
//import java.io.*;
//import java.net.*;
//import java.util.List;
//
//

//public class FileScanService extends Thread{
////        public static void main(String[] args) {
////
////            server();
////        }
//
//
//        private Socket socket;
//        String clientIp;
//        public FileScanService(Socket socket) {
//            this.socket = socket;
//        }
//
//
//        @Override
//        public void run() {
//            try {
//                clientIp = getClientIp(socket);
//                System.out.println(clientIp+"上线");
//                // 可以利用套接字获取输出流、输入流
//                ObjectInputStream oos = new ObjectInputStream(socket.getInputStream());
//
//                ClientBean clientBean = null;
//                Object obj = null;
//                //如果为null就读取到文件结尾了
//                //读取结束标志位：是再次读取一个对象，判定对象是否是空，如果不为null继续读取，如果为null停止读取
//                try {
//                    while ((obj = oos.readObject()) != null) {
//                        ////反序列化读取得到Person
//                        clientBean = (ClientBean) obj;
//
//                    }
//                }catch (EOFException e){
//                    System.out.println(clientIp+"检查信息完成，开始生产报告。。");
//                }finally {
//                    if (clientBean!=null){
//                    generateRreport(clientBean);
//                    }
//                }
//
//
//                System.out.println(clientIp+"生成完毕");
//                oos.close();
//                socket.close();
//            } catch (SocketException e){
//                System.out.println(clientIp+"下线了");
//            }catch (IOException e) {
//                // TODO Auto-generated catch block
//
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//    /**
//     * 将接受到的数据生成报告
//     */
//    private static void generateRreport(ClientBean clientBean){
//        String fileName = clientBean.getIp();
//        List<String> filePathName = clientBean.getFilePathName();
//        File desktopDir = FileSystemView.getFileSystemView() .getHomeDirectory();
//            String desktopPath = desktopDir.getAbsolutePath();
//            desktopPath+="\\consequence\\"+fileName+"敏感信息报告.docx";
//            System.out.println(desktopPath);
//
//            try {
//                FileOutputStream out = new FileOutputStream(desktopPath); //创建输出流
//                XWPFDocument doc = new XWPFDocument(); //创建word文件
//                XWPFParagraph p1 = doc.createParagraph(); //创建段落
//                XWPFRun r1 = p1.createRun(); //创建段落文本
//                for (String str: filePathName) {
//                    p1.setAlignment(ParagraphAlignment.LEFT);
//                    r1.setText(str); //设置文本
//                    r1.addCarriageReturn();
//                    r1.setText("---------------------------------------------------");
//                    r1.addCarriageReturn();
//                }
//                r1.setFontFamily("仿宋");
//                r1.setFontSize(15);// 字体大小
//                doc.write(out);  //输出
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        /**
//         * 服务器端程序的编写
//         */
//        public static void server() {
//            try {
//                InetAddress localHost = InetAddress.getLocalHost();
//                System.out.println(localHost);
//                // 创建服务器端套接字，绑定到6000的端口上
//                ServerSocket ss = new ServerSocket(6111,1000,localHost);
//                //当前为一个客户服务，要为多个客户同时服务加while循环监听客户端请求
//                while (true) {
//                    // 调用accept方法就会阻塞，直到客户端有连接请求到来的时候。
//                    // 它会返回一个套接字，然后就可以利用这个套接字与客户端进行数据通信。
//                    Socket s = ss.accept();
//                    int i = Thread.activeCount();
//                    System.out.println("当前运行线程数"+i);
//                    //一旦有一个连接请求到来，接收连接请求，返回套接字。
//                    //然后创建启动一个新的线程,将套接字传递给线程,为客户端进行服务
//                    //Run执行完毕，当前线程也就终止了
//                    new FileScanService(s).start();
//                }
//                //ss.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//    public static String getClientIp(Socket s){
//        SocketAddress remoteSocketAddress = s.getRemoteSocketAddress();
////        System.out.println(remoteSocketAddress);
//        String s1 = remoteSocketAddress.toString();
//        int i = s1.indexOf(":");
//        return s1.substring(1,i);
//    }
//
//}
