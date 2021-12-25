package KeywordScan;

public class test {


    public static void main(String[] args) {
        Fileutil.ProgressBar progressBar = new Fileutil.ProgressBar();

        try {
            progressBar.printProgress();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
