package edu.eci.arsw.blacklistvalidator;

import java.util.ArrayList;
import java.util.List;

public class SearchState {

    private final int alarmCount;
    private final List<Integer> blacklistOccurrences;
    private int checkedListsCount;

    public SearchState(int alarmCount) {
        this.alarmCount = alarmCount;
        this.blacklistOccurrences = new ArrayList<>();
        this.checkedListsCount = 0;
    }

    public synchronized boolean shouldContinue() {
        return blacklistOccurrences.size() < alarmCount;
    }

    public synchronized boolean registerOccurrence(int serverNumber) {
        if (blacklistOccurrences.size() >= alarmCount) {
            return false;
        }

        blacklistOccurrences.add(serverNumber);

        return blacklistOccurrences.size() < alarmCount;
    }

    public synchronized void registerCheckedList() {
        checkedListsCount++;
    }

    public synchronized int getOccurrencesCount() {
        return blacklistOccurrences.size();
    }

    public synchronized int getCheckedListsCount() {
        return checkedListsCount;
    }

    public synchronized List<Integer> getOccurrences() {
        return new ArrayList<>(blacklistOccurrences);
    }
}