package KeywordScan;

import java.io.Serializable;
import java.util.List;

public class ClientBean implements Serializable {

    //目标IP
    private String ip;
    //目标文件路径名称

    private List<String> filePathName;



    public ClientBean() {
    }

    public ClientBean(String ip, List<String> filePathName) {
        this.ip = ip;
        this.filePathName = filePathName;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<String> getFilePathName() {
        return filePathName;
    }

    public void setFilePathName(List<String> filePathName) {
        this.filePathName = filePathName;
    }

    @Override
    public String toString() {
        return "clientBean{" +
                "ip='" + ip + '\'' +
                ", filePathName=" + filePathName +
                '}';
    }
}
