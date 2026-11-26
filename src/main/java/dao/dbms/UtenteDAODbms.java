package dao.dbms;

import dao.ArchivioMemoria;
import dao.ConnectionManager;
import dao.UtenteDAO;
import frostly.exception.FrostlyException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.MaestroGelataio;

/**
 * Implementazione DBMS di UtenteDAO.
 * Gestisce l'interazione con la tabella 'utenti' e sincronizza la cache in memoria.
 */
public class UtenteDAODbms implements UtenteDAO {

    private final ArchivioMemoria cache = ArchivioMemoria.getJdbcCacheInstance();

    @Override
    public MaestroGelataio getMaestroGelataio() {
        sincronizzaUtentiDaDatabase();
        return cache.getMaestroGelataio();
    }

    @Override
    public List<Dipendente> caricaTuttiDipendenti() {
        sincronizzaUtentiDaDatabase();
        return cache.getDipendenti();
    }

    @Override
    public Dipendente trovaDipendentePerEmail(String email) {
        sincronizzaUtentiDaDatabase();
        return cache.trovaDipendentePerEmail(email);
    }

    @Override
    public List<Cliente> caricaTuttiClienti() {
        sincronizzaUtentiDaDatabase();
        return cache.getClienti();
    }

    @Override
    public Cliente trovaClientePerEmail(String email) {
        sincronizzaUtentiDaDatabase();
        return cache.trovaClientePerEmail(email);
    }

    @Override
    public void eliminaDipendente(String email) throws FrostlyException {
        Connection conn = ConnectionManager.getInstance().getConnection();
        try {
            try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM emergenze WHERE email_richiedente = ?")) {
                s1.setString(1, email);
                s1.executeUpdate();
            }
            try (PreparedStatement s2 = conn.prepareStatement("DELETE FROM turni WHERE email_dipendente = ?")) {
                s2.setString(1, email);
                s2.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM utenti WHERE email = ? AND ruolo = 'DIPENDENTE'")) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }
            cache.eliminaDipendente(email);
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nell'eliminazione del dipendente da database.", e);
        }
    }

    @Override
    public void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo)
            throws FrostlyException {
        sincronizzaUtentiDaDatabase();

        Connection conn = ConnectionManager.getInstance().getConnection();
        String insertSql = "INSERT INTO utenti (email, nome, cognome, password, ruolo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, email);
            stmt.setString(2, nome);
            stmt.setString(3, cognome);
            stmt.setString(4, password);
            stmt.setString(5, ruolo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nella registrazione dell'utente su database.", e);
        }
        cache.registraNuovoUtente(nome, cognome, email, password, ruolo);
    }

    @Override
    public void aggiornaConfigurazioneDipendente(String email,
                                                 model.ConfigurazioneOreDipendente configurazioneOre,
                                                 model.DisponibilitaDipendente disponibilita) throws FrostlyException {
        Dipendente corrente = cache.trovaDipendentePerEmail(email);
        aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita,
                corrente != null ? corrente.getMansioni() : null,
                corrente != null ? corrente.isAttivo() : Boolean.TRUE);
    }

    @Override
    public void aggiornaConfigurazioneDipendente(String email,
                                                 model.ConfigurazioneOreDipendente configurazioneOre,
                                                 model.DisponibilitaDipendente disponibilita,
                                                 java.util.Set<model.MansioneDipendente> mansioni,
                                                 Boolean attivo) throws FrostlyException {
        Dipendente corrente = cache.trovaDipendentePerEmail(email);
        aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita, mansioni, attivo,
                corrente != null ? corrente.getMaxTurniSettimanali() : null);
    }

    @Override
    public void aggiornaConfigurazioneDipendente(String email,
                                                 model.ConfigurazioneOreDipendente configurazioneOre,
                                                 model.DisponibilitaDipendente disponibilita,
                                                 java.util.Set<model.MansioneDipendente> mansioni,
                                                 Boolean attivo,
                                                 Integer maxTurniSettimanali) throws FrostlyException {
        Connection conn = ConnectionManager.getInstance().getConnection();
        String sql = "UPDATE utenti SET modalita_ore = ?, ore_minime = ?, ore_massime = ?, "
                + "tolleranza_sotto = ?, tolleranza_sopra = ?, mansioni = ?, attivo = ?, "
                + "max_turni_settimanali = ?, disponibilita_config = ? "
                + "WHERE email = ? AND ruolo = 'DIPENDENTE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (configurazioneOre != null && configurazioneOre.isConfigurato()) {
                stmt.setString(1, configurazioneOre.getModalita().name());
                stmt.setDouble(2, configurazioneOre.getOreMinime());
                stmt.setDouble(3, configurazioneOre.getOreMassime());
                stmt.setDouble(4, configurazioneOre.getTolleranzaSotto());
                stmt.setDouble(5, configurazioneOre.getTolleranzaSopra());
            } else {
                stmt.setString(1, model.ModalitaMonteOre.NON_CONFIGURATO.name());
                stmt.setNull(2, java.sql.Types.DOUBLE);
                stmt.setNull(3, java.sql.Types.DOUBLE);
                stmt.setDouble(4, 0.0);
                stmt.setDouble(5, 0.0);
            }
            stmt.setString(6, model.MansioneDipendente.serializza(mansioni));
            stmt.setBoolean(7, attivo == null || attivo);
            if (maxTurniSettimanali != null && maxTurniSettimanali > 0) {
                stmt.setInt(8, maxTurniSettimanali);
            } else {
                stmt.setNull(8, java.sql.Types.INTEGER);
            }
            stmt.setString(9, disponibilita != null ? disponibilita.serializza() : null);
            stmt.setString(10, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new FrostlyException("Errore JDBC nell'aggiornamento configurazione ore/disponibilità dipendente.", e);
        }
        cache.aggiornaConfigurazioneDipendente(email, configurazioneOre, disponibilita, mansioni, attivo, maxTurniSettimanali);
    }

    private void sincronizzaUtentiDaDatabase() {
        String query = "SELECT email, nome, cognome, password, ruolo, modalita_ore, ore_minime, ore_massime, "
                + "tolleranza_sotto, tolleranza_sopra, mansioni, attivo, max_turni_settimanali, disponibilita_config FROM utenti";
        try {
            Connection conn = ConnectionManager.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                java.util.List<Dipendente> dipendentiDb = new java.util.ArrayList<>();
                java.util.List<Cliente> clientiDb = new java.util.ArrayList<>();
                while (rs.next()) {
                    String ruolo = rs.getString("ruolo");
                    String email = rs.getString("email");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String pass = rs.getString("password");
                    if ("DIPENDENTE".equalsIgnoreCase(ruolo)) {
                        Dipendente dip = new Dipendente(nome, cognome, email, pass);
                        String mod = rs.getString("modalita_ore");
                        Object minObj = rs.getObject("ore_minime");
                        Object maxObj = rs.getObject("ore_massime");
                        Object tsObj = rs.getObject("tolleranza_sotto");
                        Object tpObj = rs.getObject("tolleranza_sopra");
                        Double min = minObj != null ? ((Number) minObj).doubleValue() : null;
                        Double max = maxObj != null ? ((Number) maxObj).doubleValue() : null;
                        Double ts = tsObj != null ? ((Number) tsObj).doubleValue() : 0.0;
                        Double tp = tpObj != null ? ((Number) tpObj).doubleValue() : 0.0;
                        dip.setConfigurazioneOre(model.ConfigurazioneOreDipendente.daDatabase(mod, min, max, ts, tp));
                        dip.setMansioni(model.MansioneDipendente.deserializza(rs.getString("mansioni")));
                        Object attObj = rs.getObject("attivo");
                        dip.setAttivo(attObj == null || rs.getBoolean("attivo"));
                        Object maxTurniObj = rs.getObject("max_turni_settimanali");
                        if (maxTurniObj != null) {
                            int mt = ((Number) maxTurniObj).intValue();
                            if (mt > 0) {
                                dip.setMaxTurniSettimanali(mt);
                            }
                        }
                        dip.setDisponibilita(model.DisponibilitaDipendente.deserializza(rs.getString("disponibilita_config")));
                        dipendentiDb.add(dip);
                    } else if ("CLIENTE".equalsIgnoreCase(ruolo)) {
                        clientiDb.add(new Cliente(nome, cognome, email, pass));
                    } else if ("MAESTRO_GELATAIO".equalsIgnoreCase(ruolo)) {
                        cache.setMaestroGelataio(new MaestroGelataio(nome, cognome, email, pass));
                    }
                }
                cache.impostaUtentiDaDb(dipendentiDb, clientiDb);
            }
        } catch (FrostlyException | SQLException ignored) {
            // Mantiene i dati base della cache in caso di fallimento connessione
        }
    }
}
