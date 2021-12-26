package KeywordScan;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.swing.filechooser.FileSystemView;
import java.io.*;

import static java.lang.Thread.sleep;

public class test {


    public static void main(String[] args) throws InterruptedException, IOException {
//        try {
//            File writeName = new File("output.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
//            try (FileWriter writer = new FileWriter(writeName,true);
//                 BufferedWriter out = new BufferedWriter(writer)
//            ) {
//                for(int i =0;i<10;i++) {
//                    out.write("我会写入文件啦1\r\n"); // \r\n即为换行
//                    out.write("我会写入文件啦2\r\n"); // \r\n即为换行
//                    out.flush(); // 把缓存区内容压入文件
//                    System.out.println("写入一句");
//                    sleep(1000);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for(int i =0;i<10;i++){
//            XWPFDocument doc = new XWPFDocument(); //创建一个段落
//            XWPFParagraph para = doc.createParagraph(); //一个XWPFRun代表具有相同属性的一个区域：一段文本
//            XWPFRun run = para.createRun();
//            OutputStream os = null; //把doc输出到输出流
//            run.setBold(true); // 加粗
//            run.setText("加粗的内容");
//            run = para.createRun();
//            run.setColor("FF0000");
//            run.setText("红色的字。");
//
//            try {
//                os = new FileOutputStream("192.168.1.5敏感信息报告.docx",true);
//                System.out.println("输出一句");
//                doc.write(os);
//                os.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            finally {
//                os.close();
//            }
//            sleep(1000);
//        }
        Fileutil.ProgressBar progressBar = new Fileutil.ProgressBar(500);

        try {
            progressBar.printProgress();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        testss();

//
//        File desktopDir = FileSystemView.getFileSystemView() .getHomeDirectory();
//        String desktopPath = desktopDir.getAbsolutePath();
//        System.out.println(desktopPath);
    }
    /**
     * 读入TXT文件
     */
    public static void readFile() {
        String pathname = "input.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        //不关闭文件会导致资源的泄露，读写文件都同理
        //Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入TXT文件
     */
    public static void writeFile() {

    }

    public static void testss(){
        int num=333;
        System.out.print("hello,world!");
        String nChar = Fileutil.ProgressBar.getNChar(String.valueOf(num).length(), '\b');
        System.out.println(nChar);

    }


}
