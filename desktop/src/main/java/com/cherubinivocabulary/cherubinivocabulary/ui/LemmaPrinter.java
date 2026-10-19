package com.cherubinivocabulary.cherubinivocabulary.ui;

import module java.base;
import module javafx.controls;

import com.cherubinivocabulary.cherubinivocabulary.model.Lemma;

final class LemmaPrinter {
    enum Outcome { PRINTED, NO_PRINTER, FAILED }

    private static final Font HEADING_FONT = Font.font("Segoe UI", 9);
    private static final Font LEMMA_FONT = Font.font("Georgia", FontWeight.BOLD, 12);
    private static final Font DEFINITION_FONT = Font.font("Georgia", 11);
    private static final double ENTRY_GAP = 10;

    private LemmaPrinter() {
    }

    static Outcome print(String heading, List<Lemma> entries) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return Outcome.NO_PRINTER;
        PageLayout layout = job.getJobSettings().getPageLayout();
        for (VBox page : paginate(heading, entries, layout.getPrintableWidth(), layout.getPrintableHeight())) {
            if (!job.printPage(layout, page)) {
                job.cancelJob();
                return Outcome.FAILED;
            }
        }
        return job.endJob() ? Outcome.PRINTED : Outcome.FAILED;
    }

    private static List<VBox> paginate(String heading, List<Lemma> entries, double width, double height) {
        List<VBox> pages = new ArrayList<>();
        VBox page = newPage(heading, pages.size() + 1, width);
        double used = page.prefHeight(width);

        for (Lemma lemma : entries) {
            TextFlow entry = entry(lemma, width);
            double entryHeight = entry.prefHeight(width) + ENTRY_GAP;
            if (used + entryHeight > height && page.getChildren().size() > 1) {
                pages.add(page);
                page = newPage(heading, pages.size() + 1, width);
                used = page.prefHeight(width);
            }
            page.getChildren().add(entry);
            used += entryHeight;
        }
        pages.add(page);
        return pages;
    }

    private static VBox newPage(String heading, int number, double width) {
        var title = new Text("Vocabolario Milanese–Italiano di Francesco Cherubini · " + heading + " · pagina " + number);
        title.setFont(HEADING_FONT);
        title.setFill(Color.web("#555555"));
        title.setWrappingWidth(width);

        var page = new VBox(ENTRY_GAP, title);
        page.setPrefWidth(width);
        return page;
    }

    private static TextFlow entry(Lemma lemma, double width) {
        var headword = new Text(lemma.lemma() + "  ");
        headword.setFont(LEMMA_FONT);
        headword.setFill(Color.web("#c62828"));

        var definition = new Text(lemma.definition().strip());
        definition.setFont(DEFINITION_FONT);

        var entry = new TextFlow(headword, definition);
        entry.setPrefWidth(width);
        entry.setMaxWidth(width);
        return entry;
    }
}
