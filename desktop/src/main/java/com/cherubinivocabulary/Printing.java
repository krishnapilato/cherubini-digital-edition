package com.cherubinivocabulary;

import module java.base;
import module javafx.graphics;

final class Printing {
    private static final Font HEADING = Font.font("IBM Plex Sans", 9);
    private static final Font LEMMA = Font.font("IBM Plex Sans SmBld", 12);
    private static final Font DEFINITION = Font.font("IBM Plex Sans", 11);
    private static final double GAP = 10;

    private Printing() {}

    static String print(List<Lemma> lemmas, Window owner) {
        if (Printer.getAllPrinters().isEmpty()) return "Nessuna stampante disponibile: controlla che una stampante sia installata e che il servizio di stampa del sistema sia attivo.";
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return "Nessuna stampante disponibile.";
        if (!job.showPrintDialog(owner)) return "Stampa annullata.";
        PageLayout layout = job.getJobSettings().getPageLayout();
        for (VBox page : pages(lemmas, layout.getPrintableWidth(), layout.getPrintableHeight())) {
            if (!job.printPage(layout, page)) {
                job.cancelJob();
                return "Stampa non riuscita.";
            }
        }
        if (!job.endJob()) return "Stampa non riuscita.";
        return "Stampa inviata: " + lemmas.size() + (lemmas.size() == 1 ? " voce." : " voci.");
    }

    private static List<VBox> pages(List<Lemma> lemmas, double width, double height) {
        List<VBox> pages = new ArrayList<>();
        VBox page = newPage(1, width);
        double used = page.prefHeight(width);
        for (Lemma lemma : lemmas) {
            TextFlow entry = entry(lemma, width);
            double needed = entry.prefHeight(width) + GAP;
            if (used + needed > height && page.getChildren().size() > 1) {
                pages.add(page);
                page = newPage(pages.size() + 1, width);
                used = page.prefHeight(width);
            }
            page.getChildren().add(entry);
            used += needed;
        }
        pages.add(page);
        return pages;
    }

    private static VBox newPage(int number, double width) {
        var heading = new Text("Vocabolario milanese–italiano di Francesco Cherubini · pagina " + number);
        heading.setFont(HEADING);
        heading.setFill(Color.web("#555b57"));
        var page = new VBox(GAP, heading);
        page.setPrefWidth(width);
        return page;
    }

    private static TextFlow entry(Lemma lemma, double width) {
        var headword = new Text(lemma.lemma() + "  ");
        headword.setFont(LEMMA);
        var definition = new Text(lemma.definition());
        definition.setFont(DEFINITION);
        var entry = new TextFlow(headword, definition);
        entry.setPrefWidth(width);
        entry.setMaxWidth(width);
        return entry;
    }
}
