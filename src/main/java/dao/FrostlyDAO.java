package dao;

import frostly.exception.FrostlyException;
import java.util.List;
import model.Cliente;
import model.Dipendente;
import model.Emergenza;
import model.Gusto;
import model.Ingrediente;
import model.MaestroGelataio;
import model.Notifica;
import model.OrdineCliente;
import model.RichiestaGustoPersonalizzato;
import model.Turno;

public interface FrostlyDAO {

    MaestroGelataio getMaestroGelataio();

    void registraNuovoUtente(String nome, String cognome, String email, String password, String ruolo)
            throws FrostlyException;

    List<Dipendente> caricaTuttiDipendenti();

    Dipendente trovaDipendentePerEmail(String email);

    List<Cliente> caricaTuttiClienti();

    Cliente trovaClientePerEmail(String email);

    List<Turno> caricaTuttiTurni() throws FrostlyException;

    void salvaTurno(Turno turno) throws FrostlyException;

    List<Emergenza> caricaTutteEmergenze();

    void salvaEmergenza(Emergenza emergenza);

    List<Ingrediente> caricaTuttiIngredienti();

    Ingrediente trovaIngredientePerNome(String nome);

    void aggiornaIngrediente(Ingrediente ingrediente);

    List<Gusto> caricaTuttiGusti();

    Gusto trovaGustoPerNome(String nome);

    void salvaGustoMenu(Gusto gusto) throws FrostlyException;

    void rimuoviGustoMenu(String nomeGusto) throws FrostlyException;

    List<RichiestaGustoPersonalizzato> caricaTutteRichiesteGusto();

    void salvaRichiestaGusto(RichiestaGustoPersonalizzato richiesta);

    List<OrdineCliente> caricaTuttiOrdini();

    void salvaOrdine(OrdineCliente ordine);

    List<Notifica> caricaNotifichePerUtente(String emailDestinatario);

    void inviaNotifica(Notifica notifica);
}
