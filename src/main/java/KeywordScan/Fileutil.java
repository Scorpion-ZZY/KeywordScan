package KeywordScan;

import com.sun.glass.ui.Size;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static KeywordScan.FileScanClient.getIpAddress;
import static java.lang.Thread.sleep;
import static jdk.nashorn.internal.objects.ArrayBufferView.length;

/**
 * 文件工具类
 */
public class Fileutil {

	/**
	 * 运行进度条工具
	 */
	public static class ProgressBar {



		private int index = 0;
		private int maxUnm=0;
		private String finish; //完成
		private String unFinish; //未完成


		// 进度条粒度
		private final int PROGRESS_SIZE = 50;
		private int BITE = 0;


		public ProgressBar(int maxUnm){
			this.maxUnm=maxUnm;
			this.BITE=maxUnm/50;
		}
		public static String getNChar(int num, char ch){
			StringBuilder builder = new StringBuilder(); //可变字符串
			for(int i = 0; i < num; i++){
				builder.append(ch);
			}
			return builder.toString();
		}

		public void printProgress() throws InterruptedException {
			System.out.print("检查进度↓: ");
			finish = getNChar(index / BITE, '█');
			unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
			String target = String.format("%d/%d├%s%s┤", index,maxUnm, finish, unFinish);
			System.out.print(target);

			while (index <= maxUnm){
				finish = getNChar(index / BITE, '█');
				unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
				target = String.format("%d/%d├%s%s┤", index,maxUnm, finish, unFinish);//%3d 三位数 %%百分比 %s字符串
				System.out.print(getNChar(target.length(), '\b'));
				System.out.print(target);

				index++;
			}
		}
	}

	/**
	 * 获取全部盘符
	 * @return
	 */
	public static File[] getSystemRoot(){
		File[] roots = File.listRoots();
		return roots;
	}

	/**
	 * 调用dos命令行 扫描目标文件
	 * @param system
	 */
	public static List<String>   searchFileType(String system) {
		//创建Process类
		Process process = null;
		String fileNameIp = getIpAddress(); //获取当前系统IP
		String desktopPath=fileNameIp+"敏感信息报告.txt";
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(desktopPath, true));
			Date date = new Date();
			SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
			bufferedWriter.write("本次写入时间："+dateFormat.format(date)+"\r\n");
			bufferedWriter.flush();// 把缓存区内容压入文件
			System.out.println("根据当前IP创建报告文件："+desktopPath);
			System.out.println("读取需要查找的关键字设置策略.....");
			//建立链接
			BufferedReader brSensitive = new BufferedReader(new InputStreamReader(new FileInputStream("1.txt"),"UTF-8"));
			List<SensitiveEntity> sensitiveEntities = new ArrayList<>();
			String str ;
			int num =1;
			while ((str = brSensitive.readLine()) != null){
				SensitiveEntity sensitiveEntity = new SensitiveEntity();
				sensitiveEntity.setRulename("策略"+num);
				sensitiveEntity.setRules(str);
				sensitiveEntity.setNode("扫描包含"+str+"的文件");
				sensitiveEntities.add(sensitiveEntity);
				num++;
			}
			System.out.print("设置扫描策略完成，开始扫描一下包含一下关键字的文件：");
			for (SensitiveEntity sensitiveEntity:sensitiveEntities
			) {
				System.out.print(sensitiveEntity.getRulename()+":"+sensitiveEntity.getRules()+";");

			}
			System.out.println();//换行
		StringBuilder buffer = new StringBuilder();
		int z = 0;
			if (system.toLowerCase().contains("windows")) {
//				buffer.append("*.xls"); //设置要搜索文件格式
				buffer.append("*.doc,*.txt,*.ppt,*.pdf,*.xls"); //设置要搜索文件格式
				File[] fileroots = File.listRoots(); //获取全部盘符

				for (int i = 0; i < fileroots.length; ++i) {
					List<String> lists = new ArrayList<String>();
					int amount=1;
					File[] f = fileroots[i].listFiles();
					if ((f == null) || (f.length <= 0))
						continue;
					String[] command = {"cmd.exe", "/c",
							"for /r " + fileroots[i] + " %i in (" + buffer + ") do @echo %i"};
					process = Runtime.getRuntime().exec(command);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
					String line = "";
					System.out.print("以扫描到"+fileroots[i]+"盘文件：  "+amount);
					while ((line = bufferedReader.readLine()) != null) {

						System.out.print(ProgressBar.getNChar(String.valueOf(amount).length(), '\b'));
						System.out.print(amount);
						lists.add(line);
						amount++;
//						String[] split = line.split("\\.");
//						String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], line,sensitiveEntities);

					}
					bufferedReader.close();
					System.out.println();

					for (String sss: lists) {
						String[] split = sss.split("\\.");
						String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], sss,sensitiveEntities); //文件内容匹配关键字规则
						//如果返回的字符串为空 就代表当前文件没有匹配到关键字 不为空则匹配到了
						if (s !=null){
							bufferedWriter.write(s);
							bufferedWriter.write("\r\n---------------------------------------------\r\n");
							bufferedWriter.flush();
						}
					}
//					while ((line = bufferedReader.readLine()) != null) {
//						System.out.println(line);
//						String[] split = line.split("\\.");
//						String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], line,sensitiveEntities);
//						if (s !=null){
//							lists.add(s);
//						}
//					}
				}
				bufferedWriter.close();
				return null;
			}
			if (system.toLowerCase().contains("linux")) {
				buffer.append("*.doc,*.txt,*.ppt,*.pdf,*.xls");
				String[] command = {"/bin/sh", "-c", "find / -regex '.*\\.doc.*\\|.*\\.txt|.*\\.ppt.*\\|.*\\.pdf\\|.*\\.xls.*\\|' 2>/dev/null" };
				List<String> lists = new ArrayList<String>();
				process = Runtime.getRuntime().exec(command);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
				String line = "";
				int amount=1;
				while ((line = bufferedReader.readLine()) != null) {

					System.out.print(ProgressBar.getNChar(String.valueOf(amount).length(), '\b'));
					System.out.print(amount);
					lists.add(line);
					amount++;
//						String[] split = line.split("\\.");
//						String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], line,sensitiveEntities);

				}
				bufferedReader.close();
				System.out.println();

				for (String sss: lists) {
					String[] split = sss.split("\\.");
					String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], sss,sensitiveEntities); //文件内容匹配关键字规则
					//如果返回的字符串为空 就代表当前文件没有匹配到关键字 不为空则匹配到了
					if (s !=null){
						bufferedWriter.write(s);
						bufferedWriter.write("\r\n---------------------------------------------\r\n");
						bufferedWriter.flush();
					}
				}
//				while ((line = bufferedReader.readLine()) != null) {
//					String[] split =  line.split("\\.");
//
//					String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], line,sensitiveEntities);
//					if (s !=null){
//						bufferedWriter.write(s);
//						bufferedWriter.write("\r\n---------------------------------------------\r\n");
//						bufferedWriter.flush();
//					}
////					if (s !=null){
////						lists.add(s);
////					}
//				}
				bufferedWriter.close();
				return lists;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

			return null;
	}

}