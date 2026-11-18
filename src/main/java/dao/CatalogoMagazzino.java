package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singola sorgente di verita' (Single Source of Truth) per tutte le giacenze del magazzino:
 * - The e Tisane (Bustine)
 * - Cioccolate Calde (Bustine)
 * - Bevande (Bottiglie)
 * - Pasticceria e Panna (Porzioni)
 * - Materie Prime Gelateria e Pasticceria (Kg, Litri, Pezzi)
 */
public final class CatalogoMagazzino {

    public record ElementoMagazzino(
            String nome,
            double quantitaIniziale,
            double sogliaMinima,
            String unitaMisura,
            String categoria
    ) {
        public ElementoMagazzino(String nome, double quantitaIniziale, double sogliaMinima, String unitaMisura) {
            this(nome, quantitaIniziale, sogliaMinima, unitaMisura, model.Ingrediente.inferisciCategoria(nome, unitaMisura));
        }
    }

    private static final List<ElementoMagazzino> ELEMENTI = creaCatalogoMagazzino();

    private CatalogoMagazzino() {
        // Utility class
    }

    public static List<ElementoMagazzino> getElementi() {
        return Collections.unmodifiableList(ELEMENTI);
    }

    private static List<ElementoMagazzino> creaCatalogoMagazzino() {
        List<ElementoMagazzino> lista = new ArrayList<>();

        // Materie Prime Gelato & Laboratorio (Kg, Litri, Pezzi)
        lista.add(new ElementoMagazzino("Latte Fresco Alta Qualita", 35.0, 10.0, "Litri"));
        lista.add(new ElementoMagazzino("Panna Fresca d'Alpeggio", 25.0, 8.0, "Litri"));
        lista.add(new ElementoMagazzino("Zucchero Semolato Italiano", 40.0, 15.0, "Kg"));
        lista.add(new ElementoMagazzino("Uova Fresche da Allevamento", 60.0, 20.0, "Pezzi"));
        lista.add(new ElementoMagazzino("Pasta Pistacchio di Bronte DOP", 8.0, 2.5, "Kg"));
        lista.add(new ElementoMagazzino("Nocciola Piemonte IGP", 7.0, 2.0, "Kg"));
        lista.add(new ElementoMagazzino("Cioccolato Fondente Ecuador 75%", 10.0, 3.0, "Kg"));
        lista.add(new ElementoMagazzino("Cacao Amaro Monorigine", 8.0, 2.5, "Kg"));
        lista.add(new ElementoMagazzino("Mandorle di Avola", 6.0, 2.0, "Kg"));
        lista.add(new ElementoMagazzino("Polpa di Mango Alphonso", 1.5, 2.0, "Kg")); // 1.5 kg per il test scorta insufficiente
        lista.add(new ElementoMagazzino("Fragole Fresche di Bosco", 8.0, 3.0, "Kg"));
        lista.add(new ElementoMagazzino("Limoni di Sorrento IGP", 8.0, 2.5, "Kg"));
        lista.add(new ElementoMagazzino("Farina per Crepes e Dolci", 15.0, 5.0, "Kg"));
        lista.add(new ElementoMagazzino("Burro Fresco di Latteria", 15.0, 4.0, "Kg"));
        lista.add(new ElementoMagazzino("Nutella Ferrero", 10.0, 3.0, "Kg"));
        lista.add(new ElementoMagazzino("Granella di Pistacchio di Bronte DOP", 5.0, 1.5, "Kg"));
        lista.add(new ElementoMagazzino("Confettura Extra di Albicocche", 10.0, 3.0, "Kg"));
        lista.add(new ElementoMagazzino("Confettura di Frutti di Bosco", 10.0, 3.0, "Kg"));

        // Categoria The & Tisane (Unità: Bustine)
        lista.add(new ElementoMagazzino("Tisana Zenzero e Limone", 40.0, 15.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana ai Frutti Rossi", 35.0, 15.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Verde Matcha Giapponese", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Nero Earl Grey Bergamotto", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Oolong Imperiale", 25.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Bianco Silver Needle", 20.0, 8.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Verde Gunpowder alla Menta", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Nero English Breakfast", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Chai Speziato Indiano", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Rooibos Vaniglia e Mandorla", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Verde Jasmine al Gelsomino", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Nero Darjeeling Grand Cru", 25.0, 8.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Bancha Fiorito", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Te Verde Sencha Makoto", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Relax Camomilla e Miele", 40.0, 15.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Digestiva Finocchio e Anice", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Depurativa Tarassaco e Betulla", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Balsamica Timo ed Eucalipto", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Vitalita Ginseng e Guarana", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Fiori d'Arancio e Melissa", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Rosa Canina e Ibisco", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Curcuma Zenzero e Cannella", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Menta Piperita e Liquirizia", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Mirtillo e Lavanda", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Tisana Sogni d'Oro Passiflora e Tiglio", 40.0, 15.0, "Bustine"));

        // Categoria Cioccolate Calde (Unità: Bustine)
        lista.add(new ElementoMagazzino("Cioccolata Calda Fondente", 45.0, 15.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda Bianca", 40.0, 15.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda al Latte", 45.0, 15.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda al Gianduia e Nocciole", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda al Peperoncino", 25.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda Arancia e Cannella", 30.0, 10.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda al Pistacchio di Bronte", 35.0, 12.0, "Bustine"));
        lista.add(new ElementoMagazzino("Cioccolata Calda al Caramello Salato", 30.0, 10.0, "Bustine"));

        // Categoria Caffetteria (Materie Prime del Bar)
        lista.add(new ElementoMagazzino("Caffe in Grani 100% Arabica", 15.0, 4.0, "Kg", "Caffetteria"));
        lista.add(new ElementoMagazzino("Caffe al Ginseng Solubile", 5.0, 1.5, "Kg", "Caffetteria"));
        lista.add(new ElementoMagazzino("Orzo Solubile Tostato", 5.0, 1.5, "Kg", "Caffetteria"));
        lista.add(new ElementoMagazzino("Liquori per Caffe Corretto (Grappa e Sambuca)", 6.0, 2.0, "Bottiglie", "Caffetteria"));

        // Categoria Bevande (Unità: Bottiglie)
        lista.add(new ElementoMagazzino("Acqua Naturale 500ml", 120.0, 30.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Acqua Frizzante 500ml", 96.0, 30.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Coca Cola 33cl", 60.0, 20.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Fanta 33cl", 48.0, 15.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Succo di Mela Biologico", 36.0, 12.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Succo di Pesca", 36.0, 12.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Succo all'Albicocca", 36.0, 12.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Succo ai Frutti di Bosco", 36.0, 12.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Spremuta d'Arancia", 30.0, 10.0, "Bottiglie"));
        lista.add(new ElementoMagazzino("Centrifugato Mela, Carota e Zenzero", 25.0, 10.0, "Bottiglie"));

        // Biscotti (Unità: Porzioni)
        lista.add(new ElementoMagazzino("Cantucci alle Mandorle", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Baci di Dama", 30.0, 10.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Canestrelli Liguri", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Occhi di Bue all'Albicocca", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Frollini con Gocce di Cioccolato", 30.0, 10.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Amaretti Morbidi di Sassello", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Zaletti Veneziani", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Ricciarelli di Siena IGP", 20.0, 6.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Diamanti alla Vaniglia Bourbon", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Biscotti Cacao e Sale di Cervia", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Krumiri Artigianali", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Frollini alle Nocciole Piemonte", 25.0, 8.0, "Porzioni"));

        // Crostate (Unità: Porzioni)
        lista.add(new ElementoMagazzino("Crostata alla Marmellata", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Visciole e Ricotta Romana", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata alla Nutella", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Crema Pasticcera e Pinoli", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Albicocche di Vignola", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Fichi e Noci", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Tradizionale al Limone", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Pere e Cioccolato Fondente", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Pesche e Amaretti", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata ai Frutti di Bosco", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Fragoline di Nemi", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Esotica Mango e Frutto della Passione", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Lamponi e Crema Diplomatica", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Mirtilli Neri Selvatici", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Agrumi di Sicilia", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Morbida Mele e Cannella", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata More di Rovo e Ribes", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Frutti Rossi e Crema Pistacchio", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Kiwi e Crema Pasticcera", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Fichi Freschi e Miele", 15.0, 5.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Fondente Ecuador e Peperoncino", 12.0, 4.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Frangipane al Pistacchio di Bronte", 12.0, 4.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Cremino Caramello Salato e Noci Pecan", 12.0, 4.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Zabaione e Cioccolato Ruby", 12.0, 4.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Delizia al Limone e Meringa", 12.0, 4.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Crostata Cheesecake ai Mirtilli e Rosmarino", 12.0, 4.0, "Porzioni"));

        // Panna Fresca (Unità: Porzioni)
        lista.add(new ElementoMagazzino("Panna Fresca 100g", 30.0, 10.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Panna Fresca 200g", 25.0, 8.0, "Porzioni"));
        lista.add(new ElementoMagazzino("Panna Fresca 300g", 20.0, 5.0, "Porzioni"));

        return lista;
    }

    public static void popolaTabellaIngredienti(Connection conn, Statement stmt) throws SQLException {
        stmt.executeUpdate("DELETE FROM ingredienti WHERE nome IN ("
                + "'Caffe Espresso', 'Cappuccino', 'Affogato al Caffe', 'Caffe Macchiato', 'Caffe Marocchino', "
                + "'Caffe Corretto', 'Caffe al Ginseng', 'Caffe d\\'Orzo', 'Caffe Shakerato', 'Latte Macchiato')");

        String checkSql = "SELECT COUNT(*) FROM ingredienti WHERE nome = ?";
        String insertSql = "INSERT INTO ingredienti (nome, quantita_disponibile, soglia_minima, unita_misura, categoria) VALUES (?, ?, ?, ?, ?)";
        String updateCatSql = "UPDATE ingredienti SET categoria = ? WHERE nome = ? AND (categoria IS NULL OR categoria = '' OR categoria = 'Laboratorio & Basi')";

        try (PreparedStatement psCheck = conn.prepareStatement(checkSql);
             PreparedStatement psIns = conn.prepareStatement(insertSql);
             PreparedStatement psUpdCat = conn.prepareStatement(updateCatSql)) {

            for (ElementoMagazzino e : ELEMENTI) {
                psCheck.setString(1, e.nome());
                boolean esiste = false;
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        esiste = true;
                    }
                }
                if (esiste) {
                    psUpdCat.setString(1, e.categoria());
                    psUpdCat.setString(2, e.nome());
                    psUpdCat.executeUpdate();
                } else {
                    psIns.setString(1, e.nome());
                    psIns.setDouble(2, e.quantitaIniziale());
                    psIns.setDouble(3, e.sogliaMinima());
                    psIns.setString(4, e.unitaMisura());
                    psIns.setString(5, e.categoria());
                    psIns.executeUpdate();
                }
            }
        }
    }
}
