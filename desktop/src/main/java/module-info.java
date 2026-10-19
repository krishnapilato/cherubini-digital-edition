module com.cherubinivocabulary.cherubinivocabulary {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires java.logging;
    requires org.xerial.sqlitejdbc;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;

    opens com.cherubinivocabulary.cherubinivocabulary to javafx.graphics;
    opens com.cherubinivocabulary.cherubinivocabulary.ui to javafx.fxml;
}
