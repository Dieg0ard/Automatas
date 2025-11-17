package automatas.ui.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TransicionRow {

    private final StringProperty origen = new SimpleStringProperty();
    private final StringProperty simbolo = new SimpleStringProperty();
    private final StringProperty destino = new SimpleStringProperty();

    public TransicionRow(String origen, String simbolo, String destino) {
        this.origen.set(origen);
        this.simbolo.set(simbolo);
        this.destino.set(destino);
    }

    // ----------- PROPIEDADES USADAS POR JavaFX -----------
    public StringProperty origenProperty() {
        return origen;
    }

    public StringProperty simboloProperty() {
        return simbolo;
    }

    public StringProperty destinoProperty() {
        return destino;
    }

    // ----------- GETTERS -----------
    public String getOrigen() {
        return origen.get();
    }

    public String getSimbolo() {
        return simbolo.get();
    }

    public String getDestino() {
        return destino.get();
    }

    // ----------- SETTERS NECESARIOS PARA EDITAR CELDAS -----------
    public void setOrigen(String origen) {
        this.origen.set(origen);
    }

    public void setSimbolo(String simbolo) {
        this.simbolo.set(simbolo);
    }

    public void setDestino(String destino) {
        this.destino.set(destino);
    }
}
