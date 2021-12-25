package KeywordScan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * 用sax解析xls 格式文档 转成csv格式
 *
 *
 */



public class XLS2CSV implements HSSFListener {

    private int minColumns;
    private POIFSFileSystem fs;
    private PrintStream output;


    public List<ArrayList<String>> getData() {
        return data;
    }

    // 当前行
    private int curRow = 0;

    // 存储行记录的容器
    private List<String> rowlist = new ArrayList<String>();

    //样式记录容器
    private List<String> rowType = new ArrayList<String>();
    @SuppressWarnings("unchecked")
//    private ArrayList boundSheetRecords = new ArrayList();

    private String sheetName;
    //Excel数据
    private List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

    private int lastRowNumber;
    private int lastColumnNumber;

    private int currentSheetChildPage = 1;
    /** Should we output the formula, or the value it has? */
    private boolean outputFormulaValues = true;

    /** For parsing Formulas */
    private SheetRecordCollectingListener workbookBuildingListener;
    private HSSFWorkbook stubWorkbook;

    // Records we pick up as we process
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;

    /** So we known which sheet we're on */
    private int sheetIndex = -1;
    private BoundSheetRecord[] orderedBSRs;
    private List boundSheetRecords = new ArrayList<String>();

    // For handling formulas with string results
    private int nextRow;
    private int nextColumn;
    private boolean outputNextStringRecord;

    private static List<SensitiveEntity> sensitiveEntities;

    private static List<SensitiveEntity> matchedSensitive = new ArrayList<SensitiveEntity>();


    public void setSensitiveEntities(List<SensitiveEntity> sensitiveEntities) {
        this.sensitiveEntities = sensitiveEntities;
    }

    public List<SensitiveEntity> getMatchedSensitive() {
        return matchedSensitive;
    }

    public int getLastRowNumber() {
        return lastRowNumber;
    }

    //    public static void main(String[] args) throws Exception {
//
//
//        XLS2CSV xls2csv = new XLS2CSV(new FileInputStream("C:\\副本1565833417646808 (1).xls"), -1);
//        xls2csv.process();
//        List<ArrayList<String>> data2 = xls2csv.getData();
//        for (ArrayList<String> arrayList : data2) {
//                System.out.println(arrayList);
//        }
//    }



    /**
     * Creates a new XLS -> CSV converter
     * @param fs The POIFSFileSystem to process
     * @param output The PrintStream to output the CSV to
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public XLS2CSV(POIFSFileSystem fs, PrintStream output, int minColumns,List<SensitiveEntity> list) {
        this.fs = fs;
        this.output = output;
        this.minColumns = minColumns;
        this.sensitiveEntities =list;
    }
    /**
     * Creates a new XLS -> CSV converter
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public XLS2CSV(InputStream is, int minColumns,List<SensitiveEntity> list) throws IOException, FileNotFoundException,OfficeXmlFileException {
        this(
                new POIFSFileSystem(is),
                System.out,
                minColumns,
                list
        );
    }
    /**
     * Initiates the processing of the XLS file to CSV
     */
    public void process() throws IOException {
        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        formatListener = new FormatTrackingHSSFListener(listener);

        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();

        if(outputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
        }
        factory.processWorkbookEvents(request, fs);

    }

    /**
     * Main HSSFListener method, processes events, and outputs the
     *  CSV as the file is processed.
     */
    @Override
    public void processRecord(Record record) {
        int thisRow = -1;
        int thisColumn = -1;
        String thisStr = null;
        String value = null;


        switch (record.getSid()) {
            //---------add start---------
            case FontRecord.sid://字体记录
                /*FontRecord font = (FontRecord) record;
                
                short boldWeight = font.getBoldWeight();
                short fontHeight = font.getFontHeight();
                short colorPaletteIndex = font.getColorPaletteIndex();
                cellStyle = "style='";index++;
                cellStyle += "font-weight:" + boldWeight + ";"; // 
                cellStyle += "font-size: " + fontHeight / 2 + "%;"; // 
    */            break;
            case FormatRecord.sid://单元格样式记录
                /*FormatRecord format = (FormatRecord) record;*/
                break;
            case ExtendedFormatRecord.sid://扩展单元格样式记录
                /*ExtendedFormatRecord extendedFormat = (ExtendedFormatRecord) record;
                short borderTop = extendedFormat.getBorderTop();
                short borderRight = extendedFormat.getBorderRight();
                short borderBottom = extendedFormat.getBorderBottom();
                short leftBorderPaletteIdx = extendedFormat.getLeftBorderPaletteIdx();
                
                short alignment = extendedFormat.getAlignment();
                short verticalAlignment = extendedFormat.getVerticalAlignment();
                
                index++;
                alignStyle = "align='" + convertAlignToHtml(alignment) + "' ";
                alignStyle += "valign='" + convertVerticalAlignToHtml(verticalAlignment) + "' ";// 
                
                StringBuffer sb = new StringBuffer();
                sb.append(getBorderStyle(0, borderTop));
                sb.append(getBorderStyle(1, borderRight));
                sb.append(getBorderStyle(2, borderBottom));
                sb.append(getBorderStyle(3, leftBorderPaletteIdx));
                cellStyle += sb.toString();*/
                break;
            //---------add end---------
            case BoundSheetRecord.sid://遍历所有boundSheetRecord,每个sheet对应一个boundSheetRecord
                boundSheetRecords.add(record);
                break;
            case BOFRecord.sid://type=5为workbook的开始
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    // 如果有需要，则建立子工作薄
                    if (workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }

                    sheetIndex++;
                    if (orderedBSRs == null) {
                        orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                    }
                    sheetName = orderedBSRs[sheetIndex].getSheetname();
                    /*if(currentSheetIndex!=-1 && sheetIndex > currentSheetIndex){
                        if(data.size()>0){
                            String writeSheetName = orderedBSRs[sheetIndex-1].getSheetname();
                            String sheetDir = dirPath + "/" + writeSheetName;
                            String htmlPath = sheetDir + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                                    + writeSheetName + "_" + currentSheetChildPage + ".html";
                            writeHtml(writeSheetName, htmlPath);
                            data.clear();
                            currentSheetChildPage=1;
                        }
                    }
                    currentSheetIndex = sheetIndex;*/
                }
                break;

            case EOFRecord.sid:
                /*if(sheetIndex!=-1){
                    if(data.size()>0){
                        String sheetDir = dirPath + "/_a"+ (sheetIndex+1) + "-" + sheetName;
                        String htmlPath = sheetDir + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                                + sheetName + "_" + currentSheetChildPage + ".html";
                        boolean writeHtml = writeHtml(orderedBSRs[sheetIndex].getSheetname(), htmlPath);
                        data.clear();
                        if(writeHtml) currentSheetChildPage++;
                    }
                }*/
                currentSheetChildPage=1;
                break;
            case SSTRecord.sid://存储了xls所有文本单元格值，通过索引获取
                sstRecord = (SSTRecord) record;
                break;

            case BlankRecord.sid:
                BlankRecord brec = (BlankRecord) record;
                thisRow = brec.getRow();
                thisColumn = brec.getColumn();
                thisStr = "";
                rowlist.add(thisColumn, thisStr);

                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            case BoolErrRecord.sid: // 单元格为布尔类型
                BoolErrRecord berec = (BoolErrRecord) record;
                thisRow = berec.getRow();
                thisColumn = berec.getColumn();
                thisStr = berec.getBooleanValue() + "";
                rowlist.add(thisColumn, thisStr);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;

            case FormulaRecord.sid: // 单元格为公式类型
                FormulaRecord frec = (FormulaRecord) record;
                thisRow = frec.getRow();
                thisColumn = frec.getColumn();
                if (outputFormulaValues) {
                    if (Double.isNaN(frec.getValue())) {
                        // Formula result is a string
                        // This is stored in the next record
                        outputNextStringRecord = true;
                        nextRow = frec.getRow();
                        nextColumn = frec.getColumn();
                    } else {
                        thisStr = formatListener.formatNumberDateCell(frec);
                    }
                } else {
                    thisStr = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression()) + '"';
                }
                rowlist.add(thisColumn, thisStr);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            case StringRecord.sid:// 单元格中公式的字符串
                if (outputNextStringRecord) {
                    // String for formula
                    StringRecord srec = (StringRecord) record;
                    thisStr = srec.getString();
                    thisRow = nextRow;
                    thisColumn = nextColumn;
                    outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord lrec = (LabelRecord) record;
                curRow = thisRow = lrec.getRow();
                thisColumn = lrec.getColumn();
                value = lrec.getValue().trim();
                value = value.equals("") ? " " : value;
                this.rowlist.add(thisColumn, value);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            case LabelSSTRecord.sid: // 单元格为字符串类型
                LabelSSTRecord lsrec = (LabelSSTRecord) record;
                curRow = thisRow = lsrec.getRow();
                thisColumn = lsrec.getColumn();
                if (sstRecord == null) {
                    rowlist.add(thisColumn, " ");
                    //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                } else {
                    value = sstRecord.getString(lsrec.getSSTIndex()).toString().trim();
                    value = value.equals("") ? " " : value;
                    rowlist.add(thisColumn, value);
                    //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                }
                break;
            case NumberRecord.sid: // 单元格为数字类型
                NumberRecord numrec = (NumberRecord) record;
                curRow = thisRow = numrec.getRow();
                thisColumn = numrec.getColumn();
                value = formatListener.formatNumberDateCell(numrec).trim();
                value = value.equals("") ? " " : value;
                // 向容器加入列值
                rowlist.add(thisColumn, value);
                //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
                break;
            default:
                break;
        }

        // 遇到新行的操作
        if (thisRow != -1 && thisRow != lastRowNumber) {
            lastColumnNumber = -1;
        }

        // 空值的操作
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            curRow = thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            rowlist.add(thisColumn, " ");
            //rowType.add(thisColumn,cellStyle + "' " + alignStyle);
        }

        // 更新行和列的值
        if (thisRow > -1)
            lastRowNumber = thisRow;
        if (thisColumn > -1)
            lastColumnNumber = thisColumn;

        // 行结束时的操作
        if (record instanceof LastCellOfRowDummyRecord) {
            if (minColumns > 0) {
                // 列值重新置空
                if (lastColumnNumber == -1) {
                    lastColumnNumber = 0;
                }
            }
            lastColumnNumber = -1;

            // 每行结束时， 调用getRows() 方法(打印内容)
            //rowReader.getRows(sheetIndex, curRow, rowlist);

            ArrayList<String> list = new ArrayList<String>();
//            System.out.println("这里面是啥？"+rowlist.toString());
            panduan(rowlist.toString());
            list.addAll(rowlist);
            data.add(list);
               /* if(data.size()==2000){
                    String sheetDir = dirPath + "/_a"+ (sheetIndex+1)+ "-" + sheetName;
                    String htmlPath = sheetDir + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                            + sheetName + "_" + currentSheetChildPage + ".html";
                    boolean writeHtml = writeHtml(orderedBSRs[sheetIndex].getSheetname(), htmlPath);
                    data.clear();
                    if(writeHtml) currentSheetChildPage++;
                }*/
                /*List<String> styleList = new ArrayList<>();
                styleList.addAll(rowType);
                styleData.add(styleList);
    */
            // 清空容器
            rowlist.clear();
        }
    }

    private static void panduan(String cellText){
//        SensitiveEntity sensitiveEntity1 = new SensitiveEntity();
//        sensitiveEntity1.setRulename("策略1");
//        sensitiveEntity1.setRules("0?(13|14|15|18|17)[0-9]{9}");
//        sensitiveEntity1.setNode("扫描包含手机号的文件");
//        SensitiveEntity sensitiveEntity2 = new SensitiveEntity();
//        sensitiveEntity2.setRulename("策略2");
//        sensitiveEntity2.setRules("收益率");
//        sensitiveEntity2.setNode("扫描收益率关键字");
//        SensitiveEntity sensitiveEntity3 = new SensitiveEntity();
//        sensitiveEntity3.setRulename("策略3");
//        sensitiveEntity3.setRules("\\d{17}[\\d|x]|\\d{15}");
//        sensitiveEntity3.setNode("扫描包含身份证号的文件");
//        SensitiveEntity sensitiveEntity4 = new SensitiveEntity();
//        sensitiveEntity4.setRulename("策略4");
//        sensitiveEntity4.setRules("10.3\\.\\d{1,3}\\.\\d{1,3}");
//        sensitiveEntity4.setNode("扫描包含内网IP的文件");
//        SensitiveEntity sensitiveEntity5 = new SensitiveEntity();
//        sensitiveEntity5.setRulename("策略5");
//        sensitiveEntity5.setRules("(账户|账号|密码|用户名|chinalife|clpc|administrator|Administrator)");
//        sensitiveEntity5.setNode("扫描包含账户密码关键字");
//        List<SensitiveEntity> sensitiveEntities = Arrays.asList(sensitiveEntity1, sensitiveEntity2,sensitiveEntity3,sensitiveEntity4,sensitiveEntity5);

        Matcher matcher;
        for (SensitiveEntity sensitiveEntity : sensitiveEntities) {
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
    /**
     * 正则匹配字符串
     */
    private static Matcher matcherTxt(String regex, String str) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

}