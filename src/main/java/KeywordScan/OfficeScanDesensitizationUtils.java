package KeywordScan;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Describe: 扫描脱敏工具类
 * @Author: zml
 * @Date: 2020-4-27 11:25:30
 */
public class OfficeScanDesensitizationUtils {

    /**
     * 启动策略
     * @param fileType  文件类型
     * @param filePath  文件路径
     * @param sensitiveEntities 扫描关键字策略集合
     * @return
     */

    public static String startStrategy(String fileType, String filePath,List<SensitiveEntity> sensitiveEntities){


        PloyEntity ployEntity = new PloyEntity();
        // 制定扫描规则(如：扫描前100行)
//        ployEntity.setTop100(true);
        //规则指定，设置全部
        ployEntity.setAll(true);
        // 开始扫描
        List<SensitiveEntity> sensitives = scanFile(fileType, filePath, sensitiveEntities, ployEntity);

        if (sensitives.size() > 0){
            Map<String , Integer> resultMap = new HashMap<>();
            for (SensitiveEntity sensitiveEntity:sensitiveEntities) {
                String rules = sensitiveEntity.getRules();  //设置的策略规则
                resultMap.put(rules,0);  //初始化map集合
            }
            for (SensitiveEntity sensitiveEntity:sensitives) {
                String rules = sensitiveEntity.getRules();
                Integer integer = resultMap.get(rules);
                integer+=1;
                resultMap.put(rules,integer);
            }

            return "文件地址："+filePath+"  \r\n--->该文件中包含有有："+resultMap.toString();
        }
        return null;
    }

    /**
     * 扫描文件是否为涉敏文件
     */
    public static List<SensitiveEntity> scanFile(String fileType, String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        List<SensitiveEntity> sensitiveEntities = new ArrayList<SensitiveEntity>();
        try {
            if (fileType.toLowerCase().contains("doc")) {
                sensitiveEntities = wordScanFile(fileType, filePath, list, ployEntity);
            } else if (fileType.toLowerCase().contains("xls")) {
                sensitiveEntities = excelScanFile(fileType, filePath, list, ployEntity);
            } else if (fileType.toLowerCase().contains("txt")) {
                sensitiveEntities = txtScanFile(filePath, list, ployEntity);
            } else if (fileType.toLowerCase().contains("ppt")) {
                sensitiveEntities = pptScanFile(fileType, filePath, list, ployEntity);
            } else if (fileType.toLowerCase().contains("pdf")) {
//                System.out.println("扫描PDF类型文件");
                sensitiveEntities = pdfScanFile(filePath, list, ployEntity);
            }else {
                System.out.println("其他格式无法解析："+fileType.toLowerCase()+"文件地址："+filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("扫描文件是否为涉敏文件异常:{"+e.getMessage()+"}");
            return sensitiveEntities;
        }
        return sensitiveEntities;
    }

    /**
     * 计算发现策略扫描区间
     */
    private static void calculationDiscoveryStrategy(Map<String, Integer> mapDiscoveryStrategy, int size, PloyEntity ployEntity) {
        // 是否全文扫描
        if (ployEntity.isAll()) {
            mapDiscoveryStrategy.put("start", 0);
            mapDiscoveryStrategy.put("end", size);
        }
        else if (ployEntity.isCustomize()) {
            //是否为自定义
            mapDiscoveryStrategy.put("start", ployEntity.getStart());
            mapDiscoveryStrategy.put("end", ployEntity.getEnd() > size ? size : ployEntity.getEnd());
        } else if (ployEntity.isTop100()) {
            // 是否扫描前100行
            mapDiscoveryStrategy.put("start", 0);
            mapDiscoveryStrategy.put("end", 100 > size ? size : 100);
        } else if (ployEntity.isLast100()) {
            // 是否扫描后100行
            mapDiscoveryStrategy.put("start", 100 > size ? 0 : size - 100);
            mapDiscoveryStrategy.put("end", size);
        }
    }

    /**
     * 将所有类型转换为String
     */
    public static String getCellValString(Cell cell) {
        int cellType =cell.getCellType();
        String val = "";
        if (cellType==1) {
            val = cell.getStringCellValue();
        } else if (cellType==4) {
            val = String.valueOf(cell.getBooleanCellValue());
        } else if (cellType==0) {
            val = NumberToTextConverter.toText(cell.getNumericCellValue());
        }
        return val;
    }

    /**
     * 正则匹配字符串
     */
    private static Matcher matcherTxt(String regex, String str) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }


    private static List<SensitiveEntity> excelScanFile(String fileType, String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        if ("xls".equals(fileType)){
            return excelScanXlsFile(filePath,list,ployEntity);
        }else if("xlsx".equals(fileType)){
            return excelScanXlsxFile(filePath,list,ployEntity);
        }else {
            return new ArrayList<SensitiveEntity>();
        }
    }

    /**
     * Excel扫描文件是否为涉敏文件(xls)
     */
    private static List<SensitiveEntity> excelScanXlsFile(String filePath, List<SensitiveEntity> list,PloyEntity ployEntity){
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        try {

            XLS2CSV xls2csv = new XLS2CSV(new FileInputStream(filePath), -1,list);
            System.out.println("扫描文件"+filePath);
            xls2csv.process();

            matchedSensitive = xls2csv.getMatchedSensitive();
        }catch (OfficeXmlFileException e){
            excelScanXlsxFile(filePath,list,ployEntity);
        }catch (IOException e) {
            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件:出现异常:{"+e.getMessage()+"}");
            return matchedSensitive;
        }

        return matchedSensitive;
    }



    /**
     * Excel扫描文件是否为涉敏文件(xls)
     */
//    private static List<SensitiveEntity> excelScanXlsFile(String filePath, List<SensitiveEntity> list,PloyEntity ployEntity){
//        // 已匹配上的策略放这里
//        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
//        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();
//        try{
//            // 读取文件
//            HSSFWorkbook sheets = new HSSFWorkbook(new FileInputStream(new File(filePath)));
//            Matcher matcher;
//            // 循环所有sheet页
//            for (int sheetIndex = 0; sheetIndex < sheets.getNumberOfSheets(); sheetIndex++){
//                HSSFSheet sheetAt = sheets.getSheetAt(sheetIndex);
//                // 计算发现策略扫描区间
//                calculationDiscoveryStrategy(mapDiscoveryStrategy,sheetAt.getLastRowNum(),ployEntity);
//                int start = mapDiscoveryStrategy.get("start");
//                int end = mapDiscoveryStrategy.get("end");
//                System.out.println("扫描文件"+filePath+"从{"+start+"}行开始扫描到{"+end+"}行结束");
//                // 第一个sheet页所有行
//                for (int rowIndex = start; rowIndex <= end; rowIndex++) {
//                    HSSFRow row = sheetAt.getRow(rowIndex);
//                    if(row == null){
//                        continue;
//                    }
//                    for (int cellIndex = 0; cellIndex < row.getPhysicalNumberOfCells(); cellIndex++) {
//                        Cell cell = row.getCell(cellIndex);
//                        if(cell == null){
//                            continue;
//                        }
//                        // 获取表格内容
//                        String cellText = getCellValString(cell);
////                        System.out.println("表格文本内容:{"+cellText+"}");
//                        for (SensitiveEntity sensitiveEntity : list) {
//                            matcher = matcherTxt(sensitiveEntity.getRules(),cellText);
//                            if (matcher.find()) {
//                                while ((matcher = matcherTxt(sensitiveEntity.getRules(),cellText)).find()) {
//                                    String group = matcher.group(0);
//                                    // 打印涉敏信息
//                                    System.out.println("规则:{"+sensitiveEntity.getRules()+"}   涉敏信息:{"+group+"}");
//                                    cellText = cellText.replaceFirst(group,"****");
//                                }
//                                // 若集合中不存在改策略则加入集合
////                                if (!matchedSensitive.contains(sensitiveEntity)){
//                                matchedSensitive.add(sensitiveEntity);
////                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
////            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件异常:{"+e.getMessage()+"}");
//            return matchedSensitive;
//        }
//        return matchedSensitive;
//    }

    /**
     * Excel扫描文件是否为涉敏文件(xlsx)
     */
    private static List<SensitiveEntity> excelScanXlsxFile(String filePath, List<SensitiveEntity> list,PloyEntity ployEntity){
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();
        try{

            // 读取文件
            XSSFWorkbook sheets = new XSSFWorkbook(new FileInputStream(new File(filePath)));
//            SXSSFWorkbook swb = new SXSSFWorkbook(sheets,100);
//            XSSFWorkbook xssfWorkbook = swb.getXSSFWorkbook();
            Matcher matcher;
            // 循环所有sheet页
            for (int sheetIndex = 0; sheetIndex < sheets.getNumberOfSheets(); sheetIndex++){
                XSSFSheet sheetAt = sheets.getSheetAt(sheetIndex);
                // 计算发现策略扫描区间
                calculationDiscoveryStrategy(mapDiscoveryStrategy,sheetAt.getLastRowNum(),ployEntity);
                int start = mapDiscoveryStrategy.get("start");
                int end = mapDiscoveryStrategy.get("end");
                System.out.println("扫描文件"+filePath+"从{"+start+"}行开始扫描到{"+end+"}行结束");
                // 第一个sheet页所有行
                for (int rowIndex = start; rowIndex <= end; rowIndex++) {
                    XSSFRow row = sheetAt.getRow(rowIndex);
                    if(row == null){
                        continue;
                    }
                    for (int cellIndex = 0; cellIndex < row.getPhysicalNumberOfCells(); cellIndex++) {
                        XSSFCell cell = row.getCell(cellIndex);
                        if(cell == null){
                            continue;
                        }
                        // 获取表格内容
                        String cellText = getCellValString(cell);
//                        System.out.println("表格文本内容:{"+cellText+"}");
                        for (SensitiveEntity sensitiveEntity : list) {
                            matcher = matcherTxt(sensitiveEntity.getRules(),cellText);
                            if (matcher.find()) {
                                while ((matcher = matcherTxt(sensitiveEntity.getRules(),cellText)).find()) {
                                    String group = matcher.group(0);
                                    // 打印涉敏信息
                                    System.out.println("规则:{"+sensitiveEntity.getRules()+"}   涉敏信息:{"+group+"}");

                                    cellText = cellText.replaceFirst(group,"****");
                                }
                                // 若集合中不存在改策略则加入集合
//                                if (!matchedSensitive.contains(sensitiveEntity)){
                                    matchedSensitive.add(sensitiveEntity);
//                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
//            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件异常:{"+e.getMessage()+"}");
            return matchedSensitive;
        }
        return matchedSensitive;
    }

    /**
     * ward 扫描
     * @param fileType
     * @param filePath
     * @param list
     * @param ployEntity
     * @return
     */
    private static List<SensitiveEntity> wordScanFile(String fileType, String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        if ("doc".equals(fileType)){
            return wordScanDocFile(filePath,list,ployEntity);
        }else if("docx".equals(fileType)){
            return wordScanDocxFile(filePath,list,ployEntity);
        }else {
            return new ArrayList<SensitiveEntity>();
        }
    }

    /**
     * Word扫描文件是否为涉敏文件(doc)
     */
    private static List<SensitiveEntity> wordScanDocFile(String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();
        try{
            HWPFDocument doc = new HWPFDocument(new FileInputStream(new File(filePath)));
            Range range = doc.getRange();
            int rowNum = range.numParagraphs();
            // 计算发现策略扫描区间
            calculationDiscoveryStrategy(mapDiscoveryStrategy,rowNum,ployEntity);
            int start = mapDiscoveryStrategy.get("start");
            int end = mapDiscoveryStrategy.get("end");
            System.out.println("扫描文件"+filePath+"从{"+start+"}行开始扫描到{"+end+"}行结束");
            Matcher matcher;
            for (int rowIndex = start; rowIndex < end; rowIndex++) {
                String text = range.getParagraph(rowIndex).text();
//                System.out.println("文本内容:{"+text+"}");
                for (SensitiveEntity sensitiveEntity : list) {
                    String rule = sensitiveEntity.getRules();
                    if (matcherTxt(rule, text).find()) {
                        matcher = matcherTxt(rule, text);
                        if (matcher.find()) {
                            while ((matcher = matcherTxt(rule, text)).find()) {
                                String group = matcher.group(0);
                                // 打印涉敏信息
                                System.out.println("规则:{"+sensitiveEntity.getRules()+"}   涉敏信息:{"+group+"}");
                                // 不要去掉(while循环校验使用)
                                text = text.replaceFirst(group,"****");
                            }
                        }
                        // 若集合中不存在改策略则加入集合
//                        if (!matchedSensitive.contains(sensitiveEntity)){
                            matchedSensitive.add(sensitiveEntity);
//                        }
                    }
                }
            }
        }catch (Exception e){
//            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件异常:{"+e.getMessage()+"}");
            return matchedSensitive;
        }
        return matchedSensitive;
    }

    /**
     * Word扫描文件是否为涉敏文件(docx)
     */
    private static List<SensitiveEntity> wordScanDocxFile(String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();

        try{
            XWPFDocument doc = new XWPFDocument(new FileInputStream(new File(filePath)));
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            // 计算发现策略扫描区间
            calculationDiscoveryStrategy(mapDiscoveryStrategy,paragraphs.size(),ployEntity);
            int start = mapDiscoveryStrategy.get("start");
            int end = mapDiscoveryStrategy.get("end");
            System.out.println("扫描文件"+filePath+"从{"+start+"}行开始扫描到{"+end+"}行结束");
            Matcher matcher;
            for (int rowIndex = start; rowIndex < end; rowIndex++) {
//                System.out.println("runs文本内容:{"+paragraphs.get(rowIndex).getParagraphText()+"}");
                //遍历出来策略
                for (SensitiveEntity sensitiveEntity : list) {
                    //读取策略正则或者关键字
                    String rule = sensitiveEntity.getRules();

                    if (matcherTxt(rule,paragraphs.get(rowIndex).getParagraphText()).find()) {
                        String runText = paragraphs.get(rowIndex).getParagraphText();
                        matcher = matcherTxt(rule,runText);
                        if (matcher.find()) {
                            while ((matcher = matcherTxt(rule,runText)).find()) {
                                String group = matcher.group(0);
                                // 打印涉敏信息
                                System.out.println("规则:{"+sensitiveEntity.getRules()+"}    涉敏信息:{"+group+"}" +sensitiveEntity.getRulename());
                                // 不要去掉(while循环校验使用)
                                runText = runText.replaceFirst(group,"****");
                            }
                        }
//                        if (!matchedSensitive.contains(sensitiveEntity.getRulename())){
                            matchedSensitive.add(sensitiveEntity);
//                        }
                    }
                }
            }
        }catch (Exception e){
            return matchedSensitive;
        }
        return matchedSensitive;
    }


    private static List<SensitiveEntity> txtScanFile(String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        List<String> listStr = new ArrayList<String>();
        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            Matcher matcher;
            String lineStr = "";
            while ((lineStr = bufferedReader.readLine()) != null){
                listStr.add(lineStr);
            }
            // 计算发现策略扫描区间
            calculationDiscoveryStrategy(mapDiscoveryStrategy,listStr.size(),ployEntity);
            int start = mapDiscoveryStrategy.get("start");
            int end = mapDiscoveryStrategy.get("end");
            System.out.println("扫描文件"+filePath+"从{"+start+"}行开始扫描到{"+end+"}行结束");
            for (int rowIndex = start; rowIndex < end; rowIndex++) {
                lineStr = listStr.get(rowIndex);
//                System.out.println("文本内容:{"+lineStr+"}");
                for (SensitiveEntity sensitiveEntity : list) {
                    // 获取表格内容
                    matcher = matcherTxt(sensitiveEntity.getRules(),lineStr);
                    if (matcher.find()) {
                        while ((matcher = matcherTxt(sensitiveEntity.getRules(),lineStr)).find()) {
                            String group = matcher.group(0);
                            // 打印涉敏信息
                            System.out.println("规则:{"+sensitiveEntity.getRules()+"}    涉敏信息:{"+group+"}");
                            lineStr = lineStr.replaceFirst(group,"****");
                        }
                        // 若集合中不存在改策略则加入集合
//                        if (!matchedSensitive.contains(sensitiveEntity)){
                            matchedSensitive.add(sensitiveEntity);
//                        }
                    }
                }
            }
        }catch (Exception e){
//            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件异常:{"+e.getMessage()+"}");
            return matchedSensitive;
        }
        return matchedSensitive;
    }
    private static List<SensitiveEntity> pptScanFile(String fileType, String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();
        Matcher matcher;
        try {
            String[] texts = new String[]{};
            if ("ppt".equals(fileType)){
//                System.out.println("扫描文件类型为PPT");
                PowerPointExtractor extractor = new PowerPointExtractor(new FileInputStream(new File(filePath)));
                texts = extractor.getText().split("\n");

            }else if ("pptx".equals(fileType)){
//                System.out.println("扫描文件类型为PPTX");
                XSLFPowerPointExtractor xslfExtractor = new XSLFPowerPointExtractor(POIXMLDocument.openPackage(filePath));
                texts = xslfExtractor.getText().split("\n");
            }
            // 计算发现策略扫描区间
            calculationDiscoveryStrategy(mapDiscoveryStrategy,texts.length,ployEntity);
            int start = mapDiscoveryStrategy.get("start");
            int end = mapDiscoveryStrategy.get("end");
            System.out.println("扫描文件"+filePath+"从{"+start+"}行开始扫描到{"+end+"}行结束");
            for (int rowIndex = start; rowIndex < end; rowIndex++) {
                String lineStr = texts[rowIndex];
//                System.out.println("文本内容:{"+lineStr+"}");
                for (SensitiveEntity sensitiveEntity : list) {
                    // 获取表格内容
                    matcher = matcherTxt(sensitiveEntity.getRules(),lineStr);
                    if (matcher.find()) {
                        while ((matcher = matcherTxt(sensitiveEntity.getRules(),lineStr)).find()) {
                            String group = matcher.group(0);
                            // 打印涉敏信息
                            System.out.println("规则:{"+sensitiveEntity.getRules()+"}    涉敏信息:{"+group+"}");
                            lineStr = lineStr.replaceFirst(group,"****");
                        }
                        // 若集合中不存在改策略则加入集合
//                        if (!matchedSensitive.contains(sensitiveEntity)){
                            matchedSensitive.add(sensitiveEntity);
//                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件异常:{"+e.getMessage()+"}");
            return matchedSensitive;
        }
        return matchedSensitive;
    }


    /**
     *   PDF扫描文件是否为涉敏文件
     */


    private static List<SensitiveEntity> pdfScanFile(String filePath, List<SensitiveEntity> list, PloyEntity ployEntity) {
//        System.out.println("==============pdfScanFile==========");
        // 已匹配上的策略放这里
        List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();
        Map<String, Integer> mapDiscoveryStrategy = new HashMap<String, Integer>();
        Matcher matcher;
        try {
            // 获取PDF文件
            PdfReader pdfReader = new PdfReader(filePath);
            // 解析PDF文件
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
            // 计算发现策略扫描区间(PDF按页扫描暂定每页为30行)
            calculationDiscoveryStrategy(mapDiscoveryStrategy,pdfReader.getNumberOfPages()*30,ployEntity);
            int start = mapDiscoveryStrategy.get("start") / 30;
            int end = mapDiscoveryStrategy.get("end") / 30;
//            System.out.println("扫描文件"+filePath+"从{"+start+"}页开始扫描到{"+end+"}页结束");
            // 获取每页的文本内容
            for (int i = (start==0?1:start); i <= end; i++) {
                TextExtractionStrategy simpleTextExtractionStrategy = pdfReaderContentParser.processContent(i, new com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy());
                String resultantText = simpleTextExtractionStrategy.getResultantText();
//                System.out.println("PDF每页文本内容:{"+resultantText+"}");
                for (SensitiveEntity sensitiveEntity : list) {
                    // 判断是否匹配策略
                    matcher = matcherTxt(sensitiveEntity.getRules(),resultantText);
                    if (matcher.find()) {
                        while ((matcher = matcherTxt(sensitiveEntity.getRules(),resultantText)).find()) {
                            String group = matcher.group(0);
                            // 打印涉敏信息
//                            System.out.println("规则:{"+sensitiveEntity+"}    涉敏信息:{"+group+"}");
                            resultantText = resultantText.replaceFirst(group,"****");
                        }
                        // 若集合中不存在改策略则加入集合
//                        if (!matchedSensitive.contains(sensitiveEntity)){
                            matchedSensitive.add(sensitiveEntity);
//                        }
                    }
                }
            }
            pdfReader.close();
        }catch (Exception e){
//            System.out.println("扫描【{"+filePath+"}】文件是否为涉敏文件异常:{"+e.getMessage()+"}");
            return matchedSensitive;
        }
        return matchedSensitive;
    }

}