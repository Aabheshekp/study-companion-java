package dev.studycompanion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StudyCompanionApp extends JFrame {
    private final FlashcardStore store=new FlashcardStore();
    private final DefaultListModel<Flashcard> model=new DefaultListModel<>();
    private final JList<Flashcard> list=new JList<>(model);
    private final JTextField search=new JTextField();
    private final JLabel front=new JLabel("Choose a due card or add a new one.",SwingConstants.CENTER);
    private final JTextArea back=new JTextArea(5,32);
    private final JLabel stats=new JLabel(" ");
    private Flashcard reviewing; private boolean revealed;

    public StudyCompanionApp(){super("Study Companion");setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1080,700);setLocationRelativeTo(null);setLayout(new BorderLayout());
        try{store.load();}catch(IOException e){error("Cannot load cards",e);}
        add(buildMenu(),BorderLayout.NORTH);add(buildSidebar(),BorderLayout.WEST);add(buildReview(),BorderLayout.CENTER);refresh();}

    private JMenuBar buildMenu(){JMenuBar bar=new JMenuBar();JMenu file=new JMenu("File");JMenuItem imp=new JMenuItem("Import CSV…"),exp=new JMenuItem("Export CSV…"),exit=new JMenuItem("Exit");imp.addActionListener(e->importCsv());exp.addActionListener(e->exportCsv());exit.addActionListener(e->dispose());file.add(imp);file.add(exp);file.addSeparator();file.add(exit);bar.add(file);return bar;}
    private JPanel buildSidebar(){JPanel p=new JPanel(new BorderLayout(8,8));p.setPreferredSize(new Dimension(330,500));p.setBorder(new EmptyBorder(16,16,16,10));JLabel title=new JLabel("Your library");title.setFont(title.getFont().deriveFont(Font.BOLD,20f));p.add(title,BorderLayout.NORTH);JPanel middle=new JPanel(new BorderLayout(6,6));search.setToolTipText("Search decks and card text");search.addActionListener(e->refresh());middle.add(search,BorderLayout.NORTH);list.setCellRenderer((l,v,i,s,f)->{JLabel label=new JLabel(v.getDeck()+"  ·  "+v.getFront());label.setOpaque(true);label.setBorder(new EmptyBorder(8,7,8,7));label.setBackground(s?new Color(220,232,255):Color.WHITE);return label;});list.addListSelectionListener(e->{if(!e.getValueIsAdjusting())selectCard(list.getSelectedValue());});middle.add(new JScrollPane(list),BorderLayout.CENTER);JPanel buttons=new JPanel(new GridLayout(1,2,6,0));JButton add=new JButton("Add card"),edit=new JButton("Edit selected");add.addActionListener(e->editCard(null));edit.addActionListener(e->editCard(list.getSelectedValue()));buttons.add(add);buttons.add(edit);p.add(middle,BorderLayout.CENTER);p.add(buttons,BorderLayout.SOUTH);return p;}
    private JPanel buildReview(){JPanel p=new JPanel(new BorderLayout(12,16));p.setBorder(new EmptyBorder(22,22,22,22));JPanel top=new JPanel(new BorderLayout());JLabel heading=new JLabel("Review session");heading.setFont(heading.getFont().deriveFont(Font.BOLD,24f));top.add(heading,BorderLayout.WEST);top.add(stats,BorderLayout.EAST);p.add(top,BorderLayout.NORTH);front.setFont(front.getFont().deriveFont(Font.BOLD,23f));front.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210,220,234)),new EmptyBorder(25,25,25,25)));front.setOpaque(true);front.setBackground(Color.WHITE);p.add(front,BorderLayout.CENTER);JPanel bottom=new JPanel(new BorderLayout(8,8));back.setLineWrap(true);back.setWrapStyleWord(true);back.setEditable(false);back.setVisible(false);bottom.add(back,BorderLayout.CENTER);JPanel controls=new JPanel(new FlowLayout(FlowLayout.CENTER,8,5));JButton reveal=new JButton("Reveal answer"),again=new JButton("Again"),hard=new JButton("Hard"),good=new JButton("Good"),due=new JButton("Start due review");reveal.addActionListener(e->reveal());again.addActionListener(e->rate(ReviewScheduler.Rating.AGAIN));hard.addActionListener(e->rate(ReviewScheduler.Rating.HARD));good.addActionListener(e->rate(ReviewScheduler.Rating.GOOD));due.addActionListener(e->startDue());controls.add(due);controls.add(reveal);controls.add(again);controls.add(hard);controls.add(good);bottom.add(controls,BorderLayout.SOUTH);p.add(bottom,BorderLayout.SOUTH);return p;}

    private void refresh(){String q=search.getText().toLowerCase();List<Flashcard> cards=store.cards().stream().filter(c->(c.getFront()+" "+c.getBack()+" "+c.getDeck()).toLowerCase().contains(q)).sorted(Comparator.comparing(Flashcard::getDeck).thenComparing(Flashcard::getFront)).collect(Collectors.toList());model.clear();cards.forEach(model::addElement);long due=store.cards().stream().filter(Flashcard::isDue).count();stats.setText("Due: "+due+"   ·   Cards: "+store.cards().size());}
    private void selectCard(Flashcard card){if(card==null)return;reviewing=card;revealed=false;front.setText("<html><div style='text-align:center'>"+escape(card.getFront())+"</div></html>");back.setText(card.getBack());back.setVisible(false);}
    private void startDue(){Flashcard card=store.cards().stream().filter(Flashcard::isDue).min(Comparator.comparing(Flashcard::getDueDate)).orElse(null);if(card==null){front.setText("No cards are due. Add a card or come back later.");reviewing=null;return;}selectCard(card);}
    private void reveal(){if(reviewing==null){startDue();return;}revealed=true;back.setVisible(true);revalidate();repaint();}
    private void rate(ReviewScheduler.Rating rating){if(reviewing==null||!revealed){JOptionPane.showMessageDialog(this,"Reveal the answer before rating this card.");return;}reviewing.review(rating);save();startDue();refresh();}
    private void editCard(Flashcard card){JTextField deck=new JTextField(card==null?"General":card.getDeck()),q=new JTextField(card==null?"":card.getFront());JTextArea a=new JTextArea(card==null?"":card.getBack(),6,34);a.setLineWrap(true);a.setWrapStyleWord(true);JPanel form=new JPanel(new GridLayout(0,1,4,4));form.add(new JLabel("Deck"));form.add(deck);form.add(new JLabel("Front / question"));form.add(q);form.add(new JLabel("Back / answer"));form.add(new JScrollPane(a));int result=JOptionPane.showConfirmDialog(this,form,card==null?"Add flashcard":"Edit flashcard",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(result!=JOptionPane.OK_OPTION)return;if(deck.getText().isBlank()||q.getText().isBlank()||a.getText().isBlank()){JOptionPane.showMessageDialog(this,"Deck, front, and back are required.");return;}if(card==null)store.add(Flashcard.create(deck.getText().trim(),q.getText().trim(),a.getText().trim()));else card.edit(deck.getText().trim(),q.getText().trim(),a.getText().trim());save();refresh();}
    private void importCsv(){JFileChooser chooser=new JFileChooser();if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)return;try{CsvTransfer.importCards(chooser.getSelectedFile().toPath(),store);save();refresh();}catch(IOException e){error("Import failed",e);}}
    private void exportCsv(){JFileChooser chooser=new JFileChooser();if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;try{CsvTransfer.exportCards(chooser.getSelectedFile().toPath(),store.cards());}catch(IOException e){error("Export failed",e);}}
    private void save(){try{store.save();}catch(IOException e){error("Cannot save cards",e);}}
    private void error(String title,Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),title,JOptionPane.ERROR_MESSAGE);}
    private static String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>");}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new StudyCompanionApp().setVisible(true));}
}
