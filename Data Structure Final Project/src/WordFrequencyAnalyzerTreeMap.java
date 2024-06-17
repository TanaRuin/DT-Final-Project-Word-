

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class WordFrequencyAnalyzerTreeMap extends JFrame {

    private static Map<String, Integer> TotalWordCount = new TreeMap<>();
    private static List<String> sentences = new ArrayList<>();

    public static void main(String[] args) {
        boolean loop = true;
        Scanner scanner = new Scanner(System.in);

        while (loop) {
            try {
                System.out.println("Choose one of the options: ");
                System.out.println("1. Insert user inputted sentences");
                System.out.println("2. Text File");
                System.out.println("3. View all words");
                System.out.println("4. Edit Sentence");
                System.out.println("5. Remove Sentence");
                System.out.println("6. Export Results to CSV");
                System.out.println("7. Display Bar Chart");
                System.out.println("8. Exit");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline left-over
                //different choices
                switch (choice) {
                    case 1:
                        measureTimeElapsed("Insert user inputted sentences", () -> {
                            System.out.println("Enter your text:");
                            String userInput = scanner.nextLine();
                            //spliting sentences based on the punctuation mark
                            String[] splitSentences = userInput.split("(?<=[.!?])\\s*(?=\\p{L})|\\n+");
                            //adding all sentences to array list
                            sentences.addAll(Arrays.asList(splitSentences));
                            //storing word count in map
                            Map<String, Integer> userWordCount = countWordFrequency(userInput);
                            //printing frequency of words
                            printWordFrequency(userWordCount);
                            //adding word count to total word count
                            addWordCountToTotal(userWordCount);
                        });
                        break;
                    case 2:
                        measureTimeElapsed("Text File", () -> {
                            System.out.println("Enter file path:");
                            String filePath = scanner.nextLine();
                            String fileContent = null;
                            try {
                                fileContent = FileProcessor.readFile(filePath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            //spliting sentences based on the punctuation mark
                            String[] splitSentences = fileContent.split("(?<=[.!?])\\s*(?=\\p{L})|\\n+");
                            //adding all sentences to array list
                            sentences.addAll(Arrays.asList(splitSentences));
                            //storing word count in map
                            Map<String, Integer> fileWordCount = countWordFrequency(fileContent);
                            //printing frequency of words
                            printWordFrequency(fileWordCount);
                            //adding word count to total word count
                            addWordCountToTotal(fileWordCount);
                        });

                        break;
                    case 3:
                        measureTimeElapsed("View all words", () -> printWordFrequency(TotalWordCount));
                        break;
                    case 4:
                        measureTimeElapsed("Edit Sentence", () -> editSentence(scanner));
                        break;
                    case 5:
                        measureTimeElapsed("Remove Sentence", () -> removeSentence(scanner));
                        break;
                    case 6:
                        measureTimeElapsed("Export Results to CSV", () -> {
                            System.out.println("Enter file path to export:");
                            String exportFilePath = scanner.nextLine();
                            try {
                                CSVExporter.exportResults(exportFilePath, TotalWordCount);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("Export successful to: " + exportFilePath);
                        });
                        break;
                    case 7:
                        measureTimeElapsed("Display Bar Chart", () -> ChartDisplay.displayChart(TotalWordCount));
                        break;
                    case 8:
                        loop = false;
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer choice.");
                scanner.nextLine();  // Consume the invalid input
            }
        }

        scanner.close(); // Close the scanner when done
    }

    private static void measureTimeElapsed(String operationName, Runnable operation) {

        // Measure start time
        long startTime = System.nanoTime();

        // Execute the operation
        operation.run();

        // Measure end time
        long endTime = System.nanoTime();


        // Calculate elapsed time
        double elapsedTimeinMs = (endTime - startTime) / 1_000_000.0;


        // Printing the results
        System.out.printf("Operation: %s, Elapsed Time: %.2f ms,", operationName, elapsedTimeinMs);
    }
    //method for editing the sentence
    private static void editSentence(Scanner scanner) {
        //checking if sentences array list is empty
        if (sentences.isEmpty()) {
            System.out.println("No sentences to edit.");
            return;
        }
        else {
            System.out.println("Choose a sentence to edit:");
            for (int i = 0; i < sentences.size(); i++) {
                String sentence = sentences.get(i);
                System.out.println((i + 1) + ". " + sentence.trim());
            }
        }

        int choice;
        while (true) {
            System.out.print("Enter the sentence number to edit: ");
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > sentences.size()) {
                    System.out.println("Invalid choice. Please enter a valid sentence number.");
                } else {
                    break; // Exit the loop if the choice is valid
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }

        System.out.println("Enter the new sentence:");
        String newSentence = scanner.nextLine();

        // Update the sentence in the sentences list and totalWordCount
        String oldSentence = sentences.get(choice - 1);

        // Update the total word count before replacing the sentence
        Map<String, Integer> wordCountToRemove = countWordFrequency(oldSentence);
        removeWordCountFromTotal(wordCountToRemove);

        // Replace the sentence
        sentences.set(choice - 1, newSentence);

        // Update the cumulative word count with the new sentence
        Map<String, Integer> wordCountToAdd = countWordFrequency(newSentence);
        addWordCountToTotal(wordCountToAdd);

        System.out.println("Sentence edited successfully.");
    }

    private static void removeSentence(Scanner scanner) {
        //checking if sentence is empty
        if (sentences.isEmpty()) {
            System.out.println("No sentences to remove.");
            return;
        }
        else {
            System.out.println("Choose a sentence to remove:");
            for (int i = 0; i < sentences.size(); i++) {
                String sentence = sentences.get(i);
                System.out.println((i + 1) + ". " + sentence.trim());
            }
        }

        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline left-over from nextInt()

        int index = 0;
        //iterating over all sentences
        for (String sentence : sentences) {
            {
                if (!sentence.isEmpty()) {  // Ignore empty sentences
                    index++;
                    //removing sentence and word count from total if the sentence is chosen
                    if (index == choice) {
                        Map<String, Integer> wordCountToRemove = countWordFrequency(sentence);
                        sentences.remove(sentence);
                        removeWordCountFromTotal(wordCountToRemove);
                        System.out.println("Sentence removed.");
                        return;
                    }
                }
            }
        }

        System.out.println("Invalid choice.");
    }
    //method for counting the frequency of words
    private static Map<String, Integer> countWordFrequency(String text) {
        Map<String, Integer> wordCount = new TreeMap<>();
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        return wordCount;
    }
    //method for printing the frequency of words
    private static void printWordFrequency(Map<String, Integer> wordCount) {
        wordCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
    //methods for adding the word count to the total
    private static void addWordCountToTotal(Map<String, Integer> wordCount) {
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            TotalWordCount.put(entry.getKey(), TotalWordCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }
    //method for removing the word count from the total
    private static void removeWordCountFromTotal(Map<String, Integer> wordCount) {
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            TotalWordCount.put(word, TotalWordCount.getOrDefault(word, 0) - count);
            if (TotalWordCount.get(word) <= 0) {
                TotalWordCount.remove(word);
            }
        }
    }
}

