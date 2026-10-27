module com.cherubinivocabulary {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires java.logging;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires org.xerial.sqlitejdbc;

    opens com.cherubinivocabulary to javafx.fxml, javafx.graphics;
}