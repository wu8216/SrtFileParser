package com.share.srt.parse;

import com.share.srt.util.FileHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UnicodeReadAndProcessFle {

    private static final Logger logger = LoggerFactory.getLogger(UnicodeReadAndProcessFle.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");
    private static final DateFormat DATE_TIME_ONLY_FORMAT = new SimpleDateFormat("HH:mm:ss,SSS");
    private static final String DEFAULT_DATE_STRING = "2023-01-01T";
    private static Date DEFAULT_DATE;
    private static final String TIME_DIFF_LONG = "TIME_DIFF_LONG";
    private static final String TAG = " --> ";

    static {
        try {
            DEFAULT_DATE = DATE_FORMAT.parse(DEFAULT_DATE_STRING + "00:00:01,000");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static int INDEX = 1;

    public static void main(String[] args) {

        String timeStamp = "2023-01-15_08_54_05.136536";
        if (args.length > 0) {
            timeStamp = args[0];
        }

        combineFiles(timeStamp);
    }
    public static void combineFiles(String timeStamp) {
//        String timeStamp = "2023-01-13_16_57_39.110531";
        String path = "C:\\Users\\Richard\\PycharmProjects\\srtSubtitle\\output\\";
        String subtitles = "subtitles";
        //2023-01-13_16_57_39.110531subtitles
        String jsonFileName =  path + timeStamp + subtitles + ".json";
        String srtFileName = path + timeStamp + subtitles + ".srt";  //"D:\\_a\\srt\\subtitles.srt";
        String srtResultFileName = path + timeStamp + subtitles + "Result.srt";  //"D:\\_a\\srt\\subtitles.srt";

        Map<String, Long> timeGapMap = new LinkedHashMap<>();
        int sentenceCount = countSentencesInFile(srtFileName, timeGapMap);
        Long timeGapLong = 0L;
        if(timeGapMap.containsKey(TIME_DIFF_LONG)) {
            timeGapLong = timeGapMap.get(TIME_DIFF_LONG);
        }

        List<String> dataList = cutEvenForSrt(jsonFileName, sentenceCount);
        combineToSrtFile(srtFileName, srtResultFileName, dataList, timeGapLong);
    }

    public static List<String> cutEvenForSrt(String jsonFileName, int expectedCount) {
        int cutCount = 29;
        int maxTry = 10;
        List<String> dataList;
        int tryCount = 1;

        dataList = cutEvenForSrtForOneTry(jsonFileName, cutCount);
        while (dataList.size() != expectedCount) {
            if (dataList.size()  > expectedCount) {
                cutCount++;
            } else {
                cutCount--;
            }
            dataList = cutEvenForSrtForOneTry(jsonFileName, cutCount);
            logger.info("Try number of count is {} for cutCount {}.", tryCount++, cutCount);
            if(tryCount > maxTry) {
                break;
            }
        }
        return dataList;
    }

    public static List<String> cutEvenForSrtForOneTry(String jsonFileName, int cutCount) {
        Map<String, String> sentenceMap = new LinkedHashMap<>();
        readUnicodeDataFromFile(jsonFileName, sentenceMap);
        List<String> resultList = new ArrayList<>();

        StringBuilder combinedKeys = new StringBuilder();
        for (Map.Entry<String, String> entry : sentenceMap.entrySet()) {
            String key = entry.getKey();
            String punctuation = entry.getValue();

            if (combinedKeys.length() > cutCount) {
                resultList.add(combinedKeys.toString());
                combinedKeys = new StringBuilder(key + punctuation);
            } else {
                combinedKeys.append(key).append(punctuation);
            }
        }

        if (StringUtils.isNotBlank(combinedKeys.toString())) {
            resultList.add(combinedKeys.toString());
        }

        return resultList;
    }

    public static void combineToSrtFile(String srtFileName, String srtResultFileName, List<String> sentenceList, Long timeGapLong) {
        StringBuilder mSb = new StringBuilder();
        try (FileReader fr = new FileReader(srtFileName, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fr)) {

            String str;
            int index = 2;
            int sentenceIndex = 0;
            while ((str = reader.readLine()) != null) {
                if (index%4 == 0) {
                    if (sentenceIndex < sentenceList.size()) {
                        mSb.append(sentenceList.get(sentenceIndex++)).append("\n");
                    } else {
                        mSb.append(sentenceList.get(sentenceList.size() - 1)).append("\n");
                    }
                    index++;
                    continue;
                }
                if ((index+1)%4 == 0) { //00:00:43,300 --> 00:00:50,500
                    long MS_PER_SECOND = 1000;
                    if (timeGapLong > MS_PER_SECOND && str.contains(TAG)) {
                        String fullDateString;  //00:00:43,300 --> 00:00:50,500
                        Date startDate;
                        Date endDate;
                        fullDateString = str.substring(0, str.indexOf(TAG)).trim();
                        fullDateString = DEFAULT_DATE_STRING + fullDateString;
                        startDate = DATE_FORMAT.parse(fullDateString);
                        Date newStartDate = new Date(startDate.getTime() - timeGapLong);

                        fullDateString = str.substring(str.indexOf(TAG) + TAG.length()).trim();
                        fullDateString = DEFAULT_DATE_STRING + fullDateString;
                        endDate = DATE_FORMAT.parse(fullDateString);
                        Date newEndDate = new Date(endDate.getTime() - timeGapLong);

                        String newTimeLine = DATE_TIME_ONLY_FORMAT.format(newStartDate) + TAG + DATE_TIME_ONLY_FORMAT.format(newEndDate);
                        mSb.append(newTimeLine).append("\n");
                    } else {
                        mSb.append(str).append("\n");
                    }
                    index++;
                    continue;

                }

                mSb.append(str).append("\n");
                index++;
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        String content = mSb.toString();
        FileHelper.writeToFileUtf8(srtResultFileName, content);
    }

    public static int countSentencesInFile(String srtFileName, Map<String, Long> timeGapMap) {
        int count = 0;
        try (FileReader fr = new FileReader(srtFileName, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fr)) {

            String line;
            String fullDateString;  //00:00:43,300 --> 00:00:50,500
            Date startDate;
            long timeDiffLong;
            while ((line = reader.readLine()) != null) {
                count++;
                if (count == 2 && line.contains(TAG)) {
                    fullDateString = line.substring(0, line.indexOf(TAG)).trim();
                    fullDateString = DEFAULT_DATE_STRING + fullDateString;
                    startDate = DATE_FORMAT.parse(fullDateString);
                    timeDiffLong = startDate.getTime() - DEFAULT_DATE.getTime();
                    timeGapMap.put(TIME_DIFF_LONG, timeDiffLong);
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        int sentenceCount = count/4;
        logger.info("The sentenceCount is {}.", sentenceCount);
        return sentenceCount;
    }

    // Java 11, adds charset to FileReader
    public static void readUnicodeDataFromFile(String fileName, Map<String, String> sentenceMap) {

        try (FileReader fr = new FileReader(fileName, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fr)) {

            String str;
            String tag = "transcript:";

            while ((str = reader.readLine()) != null) {
                if (str.contains(tag)) {
                    parseOneLine(str.substring(str.indexOf(tag) + tag.length()).trim(), sentenceMap);
                }
            }

            logger.info("The sentenceMap size is {}.", sentenceMap.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseOneLine(String line, Map<String, String> sentenceMap) {
        if (StringUtils.isBlank(line)) {
            return;
        }

        line = line.replaceAll("\"", "");

        int i;
        String punctuations = "，。？！；：,.?!;:";
        StringBuilder mSb = new StringBuilder();
        String sentence;
        for (i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (punctuations.contains(c+"")) {
                sentence = mSb.toString();
                mSb = new StringBuilder();
                if (sentenceMap.containsKey(sentence)) {
                    sentence = sentence + "|" + (INDEX++);
                }
                sentenceMap.put(sentence, c+"");
            } else {
                mSb.append(c);
            }
        }
    }

}