package com.kindredgames.wordclues.logic;

import android.content.Context;
import android.text.format.DateUtils;

import com.kindredgames.wordclues.CacheController;
import com.kindredgames.wordclues.util.KGLog;
import com.kindredgames.wordclues.util.SecurityUtils;
import com.kindredgames.wordclues.util.Utils;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GameGenerator {

    //static final String DEFAULT_LINES_SET_FILE = "english.txt";
    static final String DEFAULT_LINES_SET_FILE = "english.dic";
    static final String PLAYED_CLUES_CACHE = "played";

    static final int CLUES_SIZE = 5;
    static final int GAME_SIZE = 10;


    static final String DICTIONARY_ENCODING = Utils.ENCODING_UTF8;

    /**
     * A local "fast" cache implementation. May be the file or DB-based storage implementation.
     * It may be insecure for higher speed.
     */
    protected CacheController cache;

    /**
     * Array of arrays with full answer lines. Zero-index is answer word itself, all following elements are clues
     * Same answer can be in multiple lines.
     * Loading order is important and should persists through the application life
     */
    protected List<List<String[]>> lineSets;

    /**
     * Array with indexes in lines, built as setIndex * 100000 + lineIndex * 100 + clueIndex: slllcc
     * An answer gets as many items in the array as there are clues, so answers with more clues have more chances to be played.
     * After generating a game, clues get added to  the array, and appended to the list of played clues.
     * If new sets get added or re-added (user buys new sets or rights to play already played sets)
     * Disadvantage - impossible to synchronize played/playable sets between devices.
     * Also sequences will be different
     * Workaround: random generator may be seeded by user's account hash, then we need to store the current seed after every game generation
     *
     * The set gets generated from lines/lineSets, minus playedClues
     */
    protected HashSet<Integer> playedCluesHistory;

    /**
     * Array of pointers on strings (as keys), also used in playableDictionary.
     * Once answer is picked, use playableDictionary to pick clues from answer's list of clues indexes. Also delete 5 in total (answer set) entries for the same answer below or/and above
     * Clues indexes are also used for history
     */
    protected List<String> playableAnswers;

    /**
     * Global dictionary of all answers and clues, which can be played. Once less than MIN clues remains, the key-value pair and all entries in playableClues may be removed
     * Can be exported and imported directly? No. For historical purpose
     *
     */
    protected HashMap<String, List<Integer>> availableClues;

    private Context context;

    public GameGenerator(CacheController cache, Context context) {
        super();
        this.cache = cache;
        this.context = context;
        loadPlayableSets();
        initGameState(0);
    }

    /**
     * Loads at least the default set and any purchased
     */
    private boolean loadPlayableSets() {
        // Free default only for the very beginning
        lineSets = new ArrayList<List<String[]>>();
        List<String[]> lines = loadLines(DEFAULT_LINES_SET_FILE);
        lineSets.add(lines);
        // If need to load more lines, load them and add to the main lines
        return true;
    }

    protected List<String[]> loadLines(String fileName) {
        List<String[]> lines = new ArrayList<String[]>();
        String password = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBDg3nTQV+AoPS+RSK6td9/Vq47Jlm38SejLD1\n" +
                        "2N6tIJqz3+CxoXlqRHZ5YMhlkvYvhTNyjSArQGmBuFPEFintkvInD5bPwzZMVG+nAkafhCBUfHgm\n" +
                        "Qv7wQGGaRDiJgH3Q5TgyzQE5oGcN1C3zuYcl4WhwfJJUShIg33PmygLfWQIDAQA";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName), DICTIONARY_ENCODING));
            Date startTime = new Date();
            // do reading, usually loop until end of file reading
            String lineText = reader.readLine();
            while (lineText != null) {
                lineText = StringUtils.trim(SecurityUtils.symmetricDecrypt(lineText, password));
                password = lineText;
                String[] line = lineText.replaceAll(" ", "").split("=|,");
                if (line.length > CLUES_SIZE) {
                    lines.add(line); //0-index is answer
                } else {
                    KGLog.w("Too short (%d) clue line: %s", line.length, lineText);
                }
                lineText = reader.readLine();
            }
            Date endTime = new Date();
            KGLog.d("Dictionary loaded in %d ms", endTime.getTime() - startTime.getTime());
        } catch (Exception e) {
            KGLog.e("Error reading dictionary file %s: %s", fileName, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e2) {
                KGLog.e("Error closing file: %s", e2.getMessage());
            }
        }

        return lines;
    }

    private void initGameState(int trimHistoricalClues) {
        // We know that clues are played in CLUE_SIZE packs per answer, avoiding
        loadPlayedClues(trimHistoricalClues);
        generateAvailableAnswersAndClues();
        KGLog.d("Reloaded clues");
    }


    /**
     * Loads playable clues. If no clues exists, generates a new one, populating with all playable sets.
     * Use it to optionally trim history, as afterwards it has to be reloaded anyway.
     */
    private void loadPlayedClues(int trimHistoricalClues) {
        // There is only one file for that.
        playedCluesHistory = new HashSet<Integer>();
        String fileContents = cache.get(PLAYED_CLUES_CACHE, "");
        if (fileContents != null) {
            String[] clueTexts = fileContents.split("\n");
            if (trimHistoricalClues < 0) {
                trimHistoricalClues = 0;
            }
            for (int i = trimHistoricalClues; i < clueTexts.length; i++) {
                String clueText = clueTexts[i];
                if (clueText.length() > 0) {
                    try {
                        Integer clue = Integer.parseInt(clueText);
                        if (clue > 0) {
                            playedCluesHistory.add(clue);
                        }
                    } catch (NumberFormatException exc) {
                        KGLog.e("Error parsing clue id: %s", exc.toString());
                    }
                }
            }
            if (trimHistoricalClues > 0) {
                ArrayList<String> al;
                List<String> trimmedHistory = new ArrayList<String>(Arrays.asList(clueTexts));

                trimmedHistory.subList(0, Math.min(trimHistoricalClues, clueTexts.length)).clear(); // equivalent to removeRange()
                if (!Utils.isEmptyString(trimmedHistory.get(trimmedHistory.size() - 1))) {
                    trimmedHistory.add(""); // that to always have \n at the end
                }
                cache.set(PLAYED_CLUES_CACHE, Utils.join(trimmedHistory, "\n"), "");
            }
        }
    }

    private void generateAvailableAnswersAndClues() {
        playableAnswers = new ArrayList<String>();
        availableClues = new HashMap<String, List<Integer>>();

        for (int iSet = 0; iSet < lineSets.size(); iSet++) {
            List<String[]> lines = lineSets.get(iSet);
            for (int iLine = 0; iLine < lines.size(); iLine++) {
                String[] line = lines.get(iLine);
                String answer = line[0];

                List<Integer> clues = availableClues.get(answer);
                if (clues == null) {
                    clues = new ArrayList<Integer>();
                    availableClues.put(answer, clues);
                }
                for (int iClue = 1; iClue < line.length; iClue++) {
                    // we don't need clue's text here, we build a "reference" only
                    Integer clueIndex = new Integer(getClueIndexWithSet(iSet, iLine, iClue));
                    if (!playedCluesHistory.contains(clueIndex)) {
                        clues.add(clueIndex);
                        playableAnswers.add(answer);
                    }
                }
            }
        }
    }

    private void saveProgress(List<String> playedClues) {
        if (playedClues != null && playedClues.size() > 0) {
            String data = Utils.join(playedClues);
            cache.append(PLAYED_CLUES_CACHE, data, "");
        }
    }

    public String generateGamesJson(int gamesCount) {
        try {
            StringBuilder sb = new StringBuilder();
            List<String> playedClues = new ArrayList<String>();
            sb.append('[');
            int iGame = 0;
            checkEnoughAnswers(gamesCount * GAME_SIZE);
            while (iGame < gamesCount && playableAnswers.size() >= GAME_SIZE) {
                generateGame(sb, playedClues);
                if (sb.charAt(sb.length() - 1) == ',') {
                    // can happen when less than required games are available
                    sb.deleteCharAt(sb.length() - 1);
                }
                iGame++;
                if (iGame < gamesCount) {
                    sb.append(',');
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                // can happen when less than required games are available
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(']');
            saveProgress(playedClues);
            String json = sb.toString();
            KGLog.d("Generated json: %s", json);
            return json;
        } catch (Exception e) {
            KGLog.e("Error generating games: %s", e);
            return null;
        }
    }

    public String generateGameJson() {
        try {
            StringBuilder sb = new StringBuilder();
            List<String> playedClues = new ArrayList<String>();
            checkEnoughAnswers(GAME_SIZE);
            generateGame(sb, playedClues);
            saveProgress(playedClues);
            String json = sb.toString();
            KGLog.d("Generated json: %s", json);
            return json;
        } catch (Exception e) {
            KGLog.e("Error generating games: %s", e);
            return null;
        }
    }

    private void generateGame(StringBuilder sb, List<String> playedClues) {
        // Check if we have sufficient answers and clues
        sb.append("[");
        int iAnswer = 0;
        //TODO: add check if the same answer get played in the same game
        Random rnd = new Random();
        while (iAnswer < GAME_SIZE && playableAnswers.size() > 0) {
            int answerIndex = rnd.nextInt(playableAnswers.size());
            // alternatively position can be calculated within availableClues by navigating through keys - more calc intense vs. memory intense
            String answer = playableAnswers.get(answerIndex);
            List<Integer> clues = availableClues.get(answer);
            if (clues.size() >= CLUES_SIZE) {
                sb.append(String.format("{\"answer\":\"%s\",\"clues\":[", answer));
                for (int iClue = 0; iClue < CLUES_SIZE; iClue++) {
                    int clueIndex = rnd.nextInt(clues.size());
                    Integer clueSetIndex = clues.get(clueIndex);
                    String clue = getSetClue(clueSetIndex.intValue());
                    playedClues.add(String.format("%s\n", clueSetIndex));
                    sb.append(String.format("\"%s\"", clue));
                    if (iClue < CLUES_SIZE - 1) {
                        sb.append(String.format(","));
                    }
                    // remove used clue
                    clues.remove(clueIndex);
                    if (answerIndex >= 0 && answerIndex < playableAnswers.size()) {
                        playableAnswers.remove(answerIndex);
                        if (answerIndex >= playableAnswers.size() || playableAnswers.get(answerIndex) != answer) {
                            answerIndex--;
                            if (answerIndex < 0 || playableAnswers.get(answerIndex) != answer) {
                                answerIndex = playableAnswers.indexOf(answer);
                                if (answerIndex < 0 && (iClue < CLUES_SIZE - 1)) {
                                    // Unless this was the last clue, we have a problem if nothing is found
                                    KGLog.d("No more answers [%s](%d) in playableAnswers ", answer, iClue);
                                    answerIndex = -1;
                                }
                            }
                        }
                    }
                }
                sb.append("]}");
                iAnswer++;
                if (iAnswer < GAME_SIZE) {
                    sb.append(",");
                }
            } else {
                // remove the useless answer
                removeAnswer(answer);
            }
        }
        sb.append("]");
    }

    private void checkEnoughAnswers(int answersCount) {
        if (availableClues.size() < answersCount) {
            initGameState(answersCount * CLUES_SIZE * 5); // Clean enough clues for 5 more games, while providing some randomness
        }
    }

    private void releaseHistoryClues(int cluesCount) {
        initGameState(cluesCount);
    }

    private int getClueIndexWithSet(int setIndex, int lineIndex, int clueIndex) {
        return clueIndex + lineIndex * 100 + setIndex * 10000;
    }

    private String getSetClue(int index) {
        int clueIndex = index % 100;
        int lineIndex = (index / 100) % 1000;
        int setIndex = index / 100000;
        List<String[]> lines = lineSets.get(setIndex);
        String[] line = lines.get(lineIndex);
        String clue = line[clueIndex]; // 0 index, reserved for the answer, was already taken into account
        return clue;
    }

    private void removeAnswer(String answer) {
        playableAnswers.remove(answer);
        availableClues.remove(answer);
    }

    public int getSetsCount() {
        return lineSets.size();
    }

    public int getLinesCount(int setIndex) {
        return lineSets.get(setIndex).size();
    }

    public int getLineCluesCount(int setIndex, int lineIndex) {
        return lineSets.get(setIndex).get(lineIndex).length;
    }

    public String getLineClue(int setIndex, int lineIndex, int clueIndex) {
        return lineSets.get(setIndex).get(lineIndex)[clueIndex];
    }

}
