package dev.studycompanion;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvTransfer {
    private CsvTransfer() {}

    public static void exportCards(Path path, List<Flashcard> cards) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("deck,front,back,due_date,interval_days,repetitions,ease,reviews\n");
            for (Flashcard card : cards) {
                writer.write(csv(card.getDeck()) + "," + csv(card.getFront()) + "," + csv(card.getBack()) + ","
                        + csv(card.getDueDate().toString()) + "," + card.getIntervalDays() + ","
                        + card.getRepetitions() + "," + card.getEase() + "," + card.getReviews() + "\n");
            }
        }
    }

    public static void importCards(Path path, FlashcardStore store) throws IOException {
        List<List<String>> records;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { records = parse(reader); }
        if (records.isEmpty() || records.get(0).size() < 3 || !records.get(0).get(0).equals("deck")
                || !records.get(0).get(1).equals("front") || !records.get(0).get(2).equals("back")) {
            throw new IOException("Expected deck,front,back CSV columns");
        }
        for (int i=1; i<records.size(); i++) {
            List<String> fields=records.get(i);
            if(fields.size()<3) throw new IOException("CSV row "+(i+1)+" has fewer than three columns");
            if(!fields.get(0).isBlank()&&!fields.get(1).isBlank()&&!fields.get(2).isBlank())
                store.add(Flashcard.create(fields.get(0),fields.get(1),fields.get(2)));
        }
    }

    private static String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }

    private static List<List<String>> parse(Reader reader) throws IOException {
        List<List<String>> records=new ArrayList<>(); List<String> row=new ArrayList<>(); StringBuilder field=new StringBuilder();
        boolean quoted=false; int value;
        while((value=reader.read())!=-1){char c=(char)value;
            if(c=='"'){
                if(quoted){reader.mark(1);int next=reader.read();if(next=='"')field.append('"');else{quoted=false;if(next!=-1)reader.reset();}}
                else if(field.length()==0)quoted=true;else throw new IOException("Unexpected quote in CSV field");
            }else if(c==','&&!quoted){row.add(field.toString());field.setLength(0);}
            else if((c=='\n'||c=='\r')&&!quoted){
                if(c=='\r'){reader.mark(1);int next=reader.read();if(next!='\n'&&next!=-1)reader.reset();}
                row.add(field.toString());field.setLength(0);if(!(row.size()==1&&row.get(0).isEmpty()))records.add(row);row=new ArrayList<>();
            }else field.append(c);
        }
        if(quoted)throw new IOException("Unclosed quote in CSV file");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());records.add(row);}
        return records;
    }
}
