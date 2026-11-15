package dev.studycompanion;

import java.time.LocalDate;
import java.util.UUID;

public class Flashcard {
    private final String id;
    private String deck;
    private String front;
    private String back;
    private LocalDate dueDate;
    private int intervalDays;
    private int repetitions;
    private double ease;
    private int reviews;

    public Flashcard(String id, String deck, String front, String back, LocalDate dueDate,
                     int intervalDays, int repetitions, double ease, int reviews) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.deck = deck; this.front = front; this.back = back; this.dueDate = dueDate;
        this.intervalDays = Math.max(0, intervalDays); this.repetitions = Math.max(0, repetitions);
        this.ease = Math.max(1.3, ease); this.reviews = Math.max(0, reviews);
    }

    public static Flashcard create(String deck, String front, String back) {
        return new Flashcard(null, deck, front, back, LocalDate.now(), 0, 0, 2.5, 0);
    }

    public void edit(String deck, String front, String back) { this.deck=deck; this.front=front; this.back=back; }
    public void review(ReviewScheduler.Rating rating) { ReviewScheduler.apply(this, rating); }
    void schedule(LocalDate due, int interval, int reps, double newEase) {
        dueDate=due; intervalDays=interval; repetitions=reps; ease=Math.max(1.3,newEase); reviews++;
    }
    public String getId(){return id;} public String getDeck(){return deck;} public String getFront(){return front;}
    public String getBack(){return back;} public LocalDate getDueDate(){return dueDate;} public int getIntervalDays(){return intervalDays;}
    public int getRepetitions(){return repetitions;} public double getEase(){return ease;} public int getReviews(){return reviews;}
    public boolean isDue(){return !dueDate.isAfter(LocalDate.now());}
}
