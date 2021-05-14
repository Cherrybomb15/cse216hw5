import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WordCounter {

    // The following are the ONLY variables we will modify for grading.
    // The rest of your code must run with no changes.
    public static final Path FOLDER_OF_TEXT_FILES  = Paths.get("C:\\Users\\Arthur\\IdeaProjects\\cse216hw5\\textfiles"); // path to the folder where input text files are located
    public static final Path WORD_COUNT_TABLE_FILE = Paths.get("C:\\Users\\Arthur\\IdeaProjects\\cse216hw5\\output\\file.txt"); // path to the output plain-text (.txt) file
    public static final int  NUMBER_OF_THREADS     = 25;                // max. number of threads to spawn

    public static void main(String... args) throws IOException, InterruptedException {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] textFiles = FOLDER_OF_TEXT_FILES.toFile().listFiles(filter);

        ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        TextFileManager[] operations = new TextFileManager[textFiles.length];


        int threadsPerOperations;
        int remainder;
        if (NUMBER_OF_THREADS <= textFiles.length)
        {
            threadsPerOperations = 1;
            remainder = 0;
        }
        else {
            threadsPerOperations = NUMBER_OF_THREADS / operations.length;
            remainder = NUMBER_OF_THREADS % operations.length;
        }

        for (int i = 0; i < operations.length; i++)
        {
            operations[i] = remainder > 0 ? new TextFileManager(textFiles[i],threadsPerOperations + 1)
                    : new TextFileManager(textFiles[i], threadsPerOperations);
            for (int j = 0; j < operations[i].threads; j++)
            {
                //System.out.println(operations[i]);
                service.execute(operations[i]);
            }
            remainder--;
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.HOURS);

        final String EMPTY = new String(new char[TextFileManager.largestString.intValue() + 1]);

        File out = WORD_COUNT_TABLE_FILE.toFile();
        if (!out.exists())
        {
            out.createNewFile();
        }
        FileWriter fw = new FileWriter(out.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(fw);

        bw.append(EMPTY);
        for (String a : TextFileManager.fileNames)
        {
            bw.append(a + EMPTY.substring(0,4));
        }
        bw.append("total");
        bw.append('\n');

        for (String word : TextFileManager.words)
        {
            bw.append(word);
            bw.append(EMPTY.substring(0, EMPTY.length() - word.length()));

            int sum = 0;
            for (String file : TextFileManager.fileNames)
            {

                Integer num = TextFileManager.globalMap.get(file).get(word);
                if (num == null)
                {
                    bw.append(0 + EMPTY.substring(0, file.length() + 3));
                }
                else
                {
                    bw.append(num + EMPTY.substring(0, file.length() - len(num) + 4));
                    sum += num;
                }
            }

            if (word.equals(TextFileManager.words.last()))
            {
                bw.append("" + sum);
            }
            else
            {
                bw.append(sum + "\n");
            }
        }

        bw.close();
    }

    public static int len(int x)
    {
        if (x == 0)
        {
            return 0;
        }
        else
        {
            return len(x / 10) + 1;
        }
    }
}