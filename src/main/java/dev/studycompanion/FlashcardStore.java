package dev.studycompanion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FlashcardStore {
    private final Path path;
    private final List<Flashcard> cards = new ArrayList<>();
    public FlashcardStore() {
        String configured=System.getenv("STUDY_COMPANION_DATA");
        path=configured==null||configured.isBlank()?Path.of("data","cards.tsv"):Path.of(configured);
    }
    public List<Flashcard> cards(){return cards;}
    public void load() throws IOException {
        cards.clear();
        if(!Files.exists(path)){seed(); save(); return;}
        try(BufferedReader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){
            String line; int number=0;
            while((line=reader.readLine())!=null){number++; if(line.isBlank()||line.startsWith("#"))continue;
                String[] f=line.split("\t",-1);
                if(f.length!=9) throw new IOException("Invalid card record at line "+number);
                try{cards.add(new Flashcard(dec(f[0]),dec(f[1]),dec(f[2]),dec(f[3]),LocalDate.parse(f[4]),Integer.parseInt(f[5]),Integer.parseInt(f[6]),Double.parseDouble(f[7]),Integer.parseInt(f[8])));}
                catch(RuntimeException ex){throw new IOException("Invalid card values at line "+number,ex);}
            }
        }
    }
    public void save() throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temp=path.resolveSibling(path.getFileName()+".tmp");
        try(BufferedWriter writer=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)){
            writer.write("# Study Companion TSV v1\n");
            for(Flashcard c:cards){writer.write(String.join("\t",enc(c.getId()),enc(c.getDeck()),enc(c.getFront()),enc(c.getBack()),c.getDueDate().toString(),Integer.toString(c.getIntervalDays()),Integer.toString(c.getRepetitions()),Double.toString(c.getEase()),Integer.toString(c.getReviews())));writer.newLine();}
        }
        try{Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(IOException unsupported){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}
    }
    public void add(Flashcard card){cards.add(card);}
    public void remove(String id){cards.removeIf(c->c.getId().equals(id));}
    private void seed(){
        cards.add(Flashcard.create("Computer Science","What is the time complexity of binary search?","O(log n), because the search interval is halved at each comparison."));
        cards.add(Flashcard.create("Computer Science","What does ACID stand for in database transactions?","Atomicity, Consistency, Isolation, and Durability."));
        cards.add(Flashcard.create("Biology","Where does glycolysis take place?","In the cytosol of the cell."));
        cards.add(Flashcard.create("Biology","What is the role of mRNA?","It carries a copy of genetic instructions from DNA to the ribosome for protein synthesis."));
    }
    private static String enc(String v){return Base64.getUrlEncoder().withoutPadding().encodeToString(v.getBytes(StandardCharsets.UTF_8));}
    private static String dec(String v){return new String(Base64.getUrlDecoder().decode(v),StandardCharsets.UTF_8);}
}
