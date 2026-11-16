package com.cherubinivocabulary;

import module java.base;
import module java.logging;
import module javafx.controls;

/**
 * Tells the reader, calmly and in Italian, that something went wrong: the technical details stay
 * folded away for whoever has to look into it.
 */
final class Problems {
    private static final Logger LOG = Logger.getLogger(Problems.class.getName());
    // A problem that repeats, for example on every redraw, must not fill the screen with dialogs.
    private static final AtomicBoolean SHOWN = new AtomicBoolean();
    private static final String NOTHING_CHANGED = "Non è stato modificato nulla sul tuo computer.";

    private Problems() {}

    static void reportUnexpected() {
        Thread.setDefaultUncaughtExceptionHandler((_, error) -> {
            LOG.log(Level.SEVERE, "Unexpected error.", error);
            if (SHOWN.compareAndSet(false, true)) Platform.runLater(() -> dialog(
                    "Qualcosa è andato storto",
                    NOTHING_CHANGED + " Puoi continuare a usare il vocabolario; se il problema si ripete, chiudi e riapri l'app.",
                    error).showAndWait());
        });
    }

    static void reportStartupFailure(Throwable error) {
        LOG.log(Level.SEVERE, "The application failed to start.", error);
        dialog("Il vocabolario non si è aperto",
                NOTHING_CHANGED + " Prova a riaprire l'app; se il problema continua, scarica di nuovo lo zip dalla pagina del progetto.",
                error).showAndWait();
    }

    static Alert dialog(String header, String advice, Throwable error) {
        var alert = new Alert(Alert.AlertType.NONE, advice, new ButtonType("Chiudi", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Vocabolario Cherubini");
        alert.setHeaderText(header);
        alert.setGraphic(new ImageView(new Image(CherubiniApp.resource("icon.png"), 48, 48, true, true)));
        var details = new TextArea(stackTrace(error));
        details.setEditable(false);
        details.setPrefRowCount(12);
        alert.getDialogPane().setExpandableContent(details);
        // On the whole window, as for the main one: the theme's colours are defined on its root.
        alert.getDialogPane().getScene().getStylesheets().add(CherubiniApp.resource("app.css"));
        if (alert.getDialogPane().getScene().getWindow() instanceof Stage window) window.getIcons().add(new Image(CherubiniApp.resource("icon.png")));
        return alert;
    }

    private static String stackTrace(Throwable error) {
        var text = new StringWriter();
        error.printStackTrace(new PrintWriter(text));
        return text.toString();
    }
}
