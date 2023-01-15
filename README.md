# SrtFileParser

# Parse the google speech-to-text srt file and generate srt file with punctuations.
Read JSON and SRT files and generate a final well-formated SRT result file with punctuations.

# Requirements
* JDK 11 or above

## Features
- Read two input files JSON and SRT files.
- Generate one output file ({Timestamp}subtitlesResult.srt).


Step 1: Import this maven project to your favorable IDE, such as IntelliJ.

Step 2: Run this command "java UnicodeReadAndProcessFle {timestamp}"

Step 3: Check the output in "./output" folder, which is the same input files folder.


## Usage
```
usage: java UnicodeReadAndProcessFle [Timestamp]
Example:
java UnicodeReadAndProcessFle 2023-01-15_08_54_05.136536

```

# FAQ

- Q: The format is the input srt file does not have punctuation, why use Java instead of Python directly?
- A: Need to run another Java program to parse and add punctions. This step can be done in Python, but no time to implement in Pyton. Let me know if anyone needs me to do so. Python repository: https://github.com/wu8216/srtSubtitle/

- Q: Any sample vidoes with subtitles?
- A: Two Youtube channels with many videos:
-    Technology QA Channel: https://www.youtube.com/@TechnologyQA
-    Daily Journal Note Channel: https://www.youtube.com/@DailyJournalNote
