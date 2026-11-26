package bean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.ConfigurazioneGenerazioneTurni;

/**
 * Rappresenta una proposta temporanea di pianificazione settimanale generata dal motore.
 * Non modifica i dati ufficiali finche' l'amministratore non conferma esplicitamente.
 */
public class PropostaTurniSettimanaBean {

    private LocalDate lunediSettimana;
    private ConfigurazioneGenerazioneTurni configurazioneUsata;
    private String improntaVersioneOriginale;
    private List<TurnoBean> turniProposti = new ArrayList<>();
    private List<TurnoBean> turniOriginaliSettimana = new ArrayList<>();
    private List<RiepilogoOreDipendenteBean> riepiloghiDipendenti = new ArrayList<>();
    private int postiRichiestiTotali;
    private int postiCopertiTotali;
    private int turniAggiunti;
    private int turniRimossi;
    private int turniMantenuti;
    private List<String> turniScoperti = new ArrayList<>();
    private List<String> avvisi = new ArrayList<>();
    private List<String> suggerimenti = new ArrayList<>();
    private List<String> dipendentiEsclusiNonConfigurati = new ArrayList<>();
    private int fasceCompletamenteCoperte;
    private int fasceParzialmenteScoperte;
    private int fasceSenzaPersonale;
    private boolean coperturaChiusuraOk = true;
    private String riepilogoChiusura = "";
    private boolean haViolazioniInderogabili;

    public LocalDate getLunediSettimana() {
        return lunediSettimana;
    }

    public void setLunediSettimana(LocalDate lunediSettimana) {
        this.lunediSettimana = lunediSettimana;
    }

    public ConfigurazioneGenerazioneTurni getConfigurazioneUsata() {
        return configurazioneUsata;
    }

    public void setConfigurazioneUsata(ConfigurazioneGenerazioneTurni configurazioneUsata) {
        this.configurazioneUsata = configurazioneUsata;
    }

    public String getImprontaVersioneOriginale() {
        return improntaVersioneOriginale;
    }

    public void setImprontaVersioneOriginale(String improntaVersioneOriginale) {
        this.improntaVersioneOriginale = improntaVersioneOriginale;
    }

    public List<TurnoBean> getTurniProposti() {
        return turniProposti;
    }

    public void setTurniProposti(List<TurnoBean> turniProposti) {
        this.turniProposti = turniProposti != null ? new ArrayList<>(turniProposti) : new ArrayList<>();
    }

    public List<TurnoBean> getTurniOriginaliSettimana() {
        return turniOriginaliSettimana;
    }

    public void setTurniOriginaliSettimana(List<TurnoBean> turniOriginaliSettimana) {
        this.turniOriginaliSettimana = turniOriginaliSettimana != null ? new ArrayList<>(turniOriginaliSettimana) : new ArrayList<>();
    }

    public List<RiepilogoOreDipendenteBean> getRiepiloghiDipendenti() {
        return riepiloghiDipendenti;
    }

    public void setRiepiloghiDipendenti(List<RiepilogoOreDipendenteBean> riepiloghiDipendenti) {
        this.riepiloghiDipendenti = riepiloghiDipendenti != null ? new ArrayList<>(riepiloghiDipendenti) : new ArrayList<>();
    }

    public int getPostiRichiestiTotali() {
        return postiRichiestiTotali;
    }

    public void setPostiRichiestiTotali(int postiRichiestiTotali) {
        this.postiRichiestiTotali = postiRichiestiTotali;
    }

    public int getPostiCopertiTotali() {
        return postiCopertiTotali;
    }

    public void setPostiCopertiTotali(int postiCopertiTotali) {
        this.postiCopertiTotali = postiCopertiTotali;
    }

    public double getPercentualeCopertura() {
        if (postiRichiestiTotali <= 0) {
            return 100.0;
        }
        return Math.min(100.0, Math.round((postiCopertiTotali * 1000.0) / postiRichiestiTotali) / 10.0);
    }

    public int getTurniAggiunti() {
        return turniAggiunti;
    }

    public void setTurniAggiunti(int turniAggiunti) {
        this.turniAggiunti = turniAggiunti;
    }

    public int getTurniRimossi() {
        return turniRimossi;
    }

    public void setTurniRimossi(int turniRimossi) {
        this.turniRimossi = turniRimossi;
    }

    public int getTurniMantenuti() {
        return turniMantenuti;
    }

    public void setTurniMantenuti(int turniMantenuti) {
        this.turniMantenuti = turniMantenuti;
    }

    public List<String> getTurniScoperti() {
        return turniScoperti;
    }

    public void setTurniScoperti(List<String> turniScoperti) {
        this.turniScoperti = turniScoperti != null ? new ArrayList<>(turniScoperti) : new ArrayList<>();
    }

    public List<String> getAvvisi() {
        return avvisi;
    }

    public void setAvvisi(List<String> avvisi) {
        this.avvisi = avvisi != null ? new ArrayList<>(avvisi) : new ArrayList<>();
    }

    public List<String> getSuggerimenti() {
        return suggerimenti;
    }

    public void setSuggerimenti(List<String> suggerimenti) {
        this.suggerimenti = suggerimenti != null ? new ArrayList<>(suggerimenti) : new ArrayList<>();
    }

    public List<String> getDipendentiEsclusiNonConfigurati() {
        return dipendentiEsclusiNonConfigurati;
    }

    public void setDipendentiEsclusiNonConfigurati(List<String> dipendentiEsclusiNonConfigurati) {
        this.dipendentiEsclusiNonConfigurati = dipendentiEsclusiNonConfigurati != null
                ? new ArrayList<>(dipendentiEsclusiNonConfigurati) : new ArrayList<>();
    }

    public int getFasceCompletamenteCoperte() {
        return fasceCompletamenteCoperte;
    }

    public void setFasceCompletamenteCoperte(int fasceCompletamenteCoperte) {
        this.fasceCompletamenteCoperte = fasceCompletamenteCoperte;
    }

    public int getFasceParzialmenteScoperte() {
        return fasceParzialmenteScoperte;
    }

    public void setFasceParzialmenteScoperte(int fasceParzialmenteScoperte) {
        this.fasceParzialmenteScoperte = fasceParzialmenteScoperte;
    }

    public int getFasceSenzaPersonale() {
        return fasceSenzaPersonale;
    }

    public void setFasceSenzaPersonale(int fasceSenzaPersonale) {
        this.fasceSenzaPersonale = fasceSenzaPersonale;
    }

    public boolean isCoperturaChiusuraOk() {
        return coperturaChiusuraOk;
    }

    public void setCoperturaChiusuraOk(boolean coperturaChiusuraOk) {
        this.coperturaChiusuraOk = coperturaChiusuraOk;
    }

    public String getRiepilogoChiusura() {
        return riepilogoChiusura;
    }

    public void setRiepilogoChiusura(String riepilogoChiusura) {
        this.riepilogoChiusura = riepilogoChiusura != null ? riepilogoChiusura : "";
    }

    public boolean isHaViolazioniInderogabili() {
        return haViolazioniInderogabili;
    }

    public void setHaViolazioniInderogabili(boolean haViolazioniInderogabili) {
        this.haViolazioniInderogabili = haViolazioniInderogabili;
    }
}
