
package com.domainsurvey.crawler.service.crawler.content.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.paukov.combinatorics3.Generator;

public class Ngrams {
    public static List<WordNgram> combineAll(List<String> monograms, int size) {
        int step = 4;

        Map<String, WordNgram> wordNgrams = new HashMap<>();

        for (int i = 0; i < monograms.size(); i++) {
            String currentMonogram = monograms.get(i);

            int fromIndex = i + 1;
            if (fromIndex == monograms.size()) {
                break;
            }
            int toIndex = fromIndex + step >= monograms.size() ? monograms.size() - 1 : fromIndex + step;

            List<String> currentMonogramsToCombine = monograms.subList(fromIndex, toIndex);

            if (currentMonogramsToCombine.contains(currentMonogram)) {
                for (int i1 = 0; i1 < currentMonogramsToCombine.size(); i1++) {
                    String currentMonogramToCombine = currentMonogramsToCombine.get(i1);

                    if (currentMonogramToCombine.toLowerCase().equals(currentMonogram)) {
                        currentMonogramsToCombine = currentMonogramsToCombine.subList(0, i1);
                    }
                }
            }

            List<List<String>> combinations = Generator
                    .combination(currentMonogramsToCombine)
                    .simple(size - 1)
                    .stream()
                    .peek(n -> n.add(0, currentMonogram))
                    .collect(Collectors.toList());

            for (int i1 = 0; i1 < combinations.size(); i1++) {
                List<String> combination = combinations.get(i1);
                if (combination.stream().distinct().count() == size) {
                    String ngramValue = String.join(" ", combination);

                    if (wordNgrams.containsKey(ngramValue)) {
                        if (i1 == 0) {
                            wordNgrams.get(ngramValue).exactTotal++;
                        } else {
                            wordNgrams.get(ngramValue).notExactTotal++;
                        }
                    } else {
                        wordNgrams.put(ngramValue, new WordNgram(size, ngramValue, i1 == 0 ? 1 : 0, i1 == 0 ? 0 : 1));
                    }
                }
            }
        }

        return wordNgrams.entrySet().stream().map(v -> v.getValue()).collect(Collectors.toList());
    }

    public static ArrayList<String> sanitiseToWords(String text) {
        return new ArrayList<>(Arrays.asList(text.split("\\s+")));
    }

    public static ArrayList<String> sanitiseTextAndWords(String text) {
        return sanitiseToWords(sanitiseToText(text));
    }

    public static String sanitiseToText(String text) {
        String[] characters = text.split("");

        StringBuilder sanitised = new StringBuilder();

        boolean onSpace = true;

        int xLastCharacter = text.length() - 1;
        for (int i = 0; i <= xLastCharacter; i++) {
            if (characters[i].matches("^[A-Za-z0-9$£%]$") || characters[i].matches(".*\\p{InCyrillic}.*")) {
                sanitised.append(characters[i]);
                onSpace = false;
            } else if (characters[i].equals("'") && i > 0 && i < xLastCharacter) {
                String surrounding = characters[i - 1] + characters[i + 1];
                if (surrounding.matches("^[A-Za-z]{2}$")) {
                    sanitised.append("'");
                    onSpace = false;
                }
            } else if (!onSpace && i != xLastCharacter) {
                sanitised.append(" ");
                onSpace = true;
            }
        }

        return sanitised.toString();
    }

    public static class Ngram {
        public String content;
        public boolean exact = true;

        public Ngram(String content) {
            this.content = content;
        }

        public Ngram(String content, boolean exact) {
            this.content = content;
            this.exact = exact;
        }

        @Override
        public String toString() {
            return String.format("content=%s, exact=%s", content, exact);
        }
    }
}