import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class TextFileManager implements Runnable {
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> globalMap = new ConcurrentHashMap<>();
    public static ConcurrentSkipListSet<String> fileNames = new ConcurrentSkipListSet<String>();
    public static ConcurrentSkipListSet<String> words = new ConcurrentSkipListSet<String>();
    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
    private AtomicInteger threadExitCounter = new AtomicInteger(0);
    private AtomicInteger threadEnterCounter = new AtomicInteger(0);
    public static AtomicInteger largestString = new AtomicInteger(0); //Integer keeping the value of the largest string for spacing
    private String[] strings; //The strings we will be operating on
    private String fileName; //Keeps the name of the file being operated on
    public int threads; //The number of threads we will be taking

    public TextFileManager(File file, int threads) throws IOException {
        fileName = file.getName();
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String str;
        while ((str = br.readLine()) != null)
        {
            builder.append(str);
            builder.append(' ');
        }
        br.close();

        strings = builder.toString().toLowerCase().split("([^a-z']+)'*\\1*");

        this.threads = strings.length < threads ? strings.length : threads; //We don't need more threads than Strings available...
    }

    public String toString()
    {
        return fileName;
    }

    public void go(int tNumber)
    {
        boolean first = tNumber == 0;
        int ratio = strings.length / threads;
        int remainder = strings.length % threads;
        if (ratio == 0) {ratio = 1; remainder = 0;}
        int start = first ? 0 : tNumber * (ratio) + (remainder);
        int end = first ? start + (ratio) + (remainder) : start + (ratio);

        //System.out.println(start + ", " + end + "| Filename: " + fileName);

        for (int i = start; i < end && i < strings.length; i++)
        {
            int ln = strings[i].length();
            largestString.getAndUpdate(operand -> Math.max(ln, operand));
            map.compute(strings[i], (k,v) -> v == null ? 1 : v + 1);
            words.add(strings[i]);
        }

        if (threadExitCounter.incrementAndGet() == threads)
        {
            String fileN = fileName.substring(0, fileName.length() - 4);
            globalMap.put(fileN, map);
            fileNames.add(fileN);
            //System.out.println("Thread number " + threadEnterCounter + " at " + fileName);
        }
    }

    @Override
    public void run() {
        int num = threadEnterCounter.getAndIncrement();
        Thread.currentThread().setName("" + num);
        if (num < threads)
        {
            go(num);
        }
    }
}
