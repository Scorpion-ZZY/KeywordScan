package KeywordScan;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 文件工具类
 */
public class Fileutil {

	public static List<String> scanStart(){
		Properties properties = System.getProperties();
		String property = properties.getProperty("os.name");
		List<String> data =new ArrayList<String>();
		if (property.toLowerCase().contains("windows")){
			data= searchFileType("windows");

		}else {
			data= searchFileType("linux");
		}
		return data;
	}

	/**
	 * 运行进度条工具
	 */
	public static class ProgressBar {

		private int index = 0;
		private String finish;
		private String unFinish;


		// 进度条粒度
		private final int PROGRESS_SIZE = 50;
		private int BITE = 2;

		private String getNChar(int num, char ch){
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < num; i++){
				builder.append(ch);
			}
			return builder.toString();
		}

		public void printProgress() throws InterruptedException {
			System.out.print("Progress:");

			finish = getNChar(index / BITE, '█');
			unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');
			String target = String.format("%3d%%[%s%s]", index, finish, unFinish);
			System.out.print(target);

			while (index <= 100){
				finish = getNChar(index / BITE, '█');
				unFinish = getNChar(PROGRESS_SIZE - index / BITE, '─');

				target = String.format("%3d%%├%s%s┤", index, finish, unFinish);
				System.out.print(getNChar(PROGRESS_SIZE + 6, '\b'));
				System.out.print(target);

				Thread.sleep(50);
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

		System.out.println("开始扫描。。。。。。。。。。");

		try
		{
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
			System.out.println("设置扫描策略完成，开始扫描一下包含一下关键字的文件：");
			for (SensitiveEntity sensitiveEntity:sensitiveEntities
			) {
				System.out.print(sensitiveEntity.getRulename()+":"+sensitiveEntity.getRules()+";");
			}
		StringBuilder buffer = new StringBuilder();
		int z = 0;
			if (system.toLowerCase().contains("windows")) {
//				buffer.append("*.xls"); //设置要搜索文件格式
				buffer.append("*.doc,*.txt,*.ppt,*.pdf,*.xls"); //设置要搜索文件格式
				File[] fileroots = File.listRoots(); //获取全部盘符
				List<String> lists = new ArrayList<String>();
				for (int i = 0; i < fileroots.length; ++i) {
					File[] f = fileroots[i].listFiles();
					if ((f == null) || (f.length <= 0))
						continue;
					String[] command = {"cmd.exe", "/c",
							"for /r " + fileroots[i] + " %i in (" + buffer + ") do @echo %i"};
					process = Runtime.getRuntime().exec(command);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
					String line = "";

					while ((line = bufferedReader.readLine()) != null) {
						String[] split = line.split("\\.");
						String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], line,sensitiveEntities);
						if (s !=null){
							lists.add(s);
						}
					}
				}
				return lists;
			}
			if (system.toLowerCase().contains("linux")) {
				buffer.append("*.doc,*.txt,*.ppt,*.pdf,*.xls");
				String[] command = {"/bin/sh", "-c", "find / -regex '.*\\.doc.*\\|.*\\.txt|.*\\.ppt.*\\|.*\\.pdf\\|.*\\.xls.*\\|' 2>/dev/null" };
				List<String> lists = new ArrayList<String>();
				process = Runtime.getRuntime().exec(command);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					String[] split = line.split("\\.");
					String s = OfficeScanDesensitizationUtils.startStrategy(split[split.length - 1], line,sensitiveEntities);
					if (s !=null){
						lists.add(s);
					}
				}
				return lists;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
			return null;
	}

}