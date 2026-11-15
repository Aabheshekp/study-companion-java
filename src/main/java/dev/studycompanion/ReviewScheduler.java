package dev.studycompanion;

import java.time.LocalDate;

/** A small transparent SM-2-inspired scheduler for personal study. */
public final class ReviewScheduler {
    public enum Rating { AGAIN, HARD, GOOD }
    private ReviewScheduler() {}

    static void apply(Flashcard card, Rating rating) {
        int reps=card.getRepetitions(); int interval=card.getIntervalDays(); double ease=card.getEase();
        switch (rating) {
            case AGAIN -> { reps=0; interval=1; ease=Math.max(1.3,ease-0.2); }
            case HARD -> { interval=Math.max(1,(int)Math.ceil(Math.max(1,interval)*1.35)); ease=Math.max(1.3,ease-0.05); reps++; }
            case GOOD -> { interval= reps==0 ? 1 : reps==1 ? 3 : Math.max(1,(int)Math.ceil(interval*ease)); reps++; }
        }
        card.schedule(LocalDate.now().plusDays(interval), interval, reps, ease);
    }
}
