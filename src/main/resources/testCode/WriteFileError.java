
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 *  * 向服务器写文件（植入危险程序）
 */
public class Main {

    public static void main(String[] args) throws IOException {

        String userDir = System.getProperty("user.dir"); //获取项目名

        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";

        String errorProgram = "java -version";
        Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
        System.out.println("写木马成功，你完了哈哈");


    }
}
