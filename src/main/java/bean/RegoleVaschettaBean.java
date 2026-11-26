package bean;

/**
 * JavaBean puro che trasporta le regole e le tariffe di composizione vaschette
 * dal livello applicativo/dominio verso le Boundary (GUI/CLI).
 */
public class RegoleVaschettaBean {

    private int maxGusti350g;
    private int maxGusti500g;
    private int maxGusti750g;
    private int maxGusti1000g;
    private double peso350g;
    private double peso500g;
    private double peso750g;
    private double peso1000g;
    private double prezzoSolaPannaAlKg;
    private double supplementoPanna;
    private double supplementoGranella;

    public RegoleVaschettaBean() {
    }

    public int getMaxGusti350g() {
        return maxGusti350g;
    }

    public void setMaxGusti350g(int maxGusti350g) {
        this.maxGusti350g = maxGusti350g;
    }

    public int getMaxGusti500g() {
        return maxGusti500g;
    }

    public void setMaxGusti500g(int maxGusti500g) {
        this.maxGusti500g = maxGusti500g;
    }

    public int getMaxGusti750g() {
        return maxGusti750g;
    }

    public void setMaxGusti750g(int maxGusti750g) {
        this.maxGusti750g = maxGusti750g;
    }

    public int getMaxGusti1000g() {
        return maxGusti1000g;
    }

    public void setMaxGusti1000g(int maxGusti1000g) {
        this.maxGusti1000g = maxGusti1000g;
    }

    public double getPeso350g() {
        return peso350g;
    }

    public void setPeso350g(double peso350g) {
        this.peso350g = peso350g;
    }

    public double getPeso500g() {
        return peso500g;
    }

    public void setPeso500g(double peso500g) {
        this.peso500g = peso500g;
    }

    public double getPeso750g() {
        return peso750g;
    }

    public void setPeso750g(double peso750g) {
        this.peso750g = peso750g;
    }

    public double getPeso1000g() {
        return peso1000g;
    }

    public void setPeso1000g(double peso1000g) {
        this.peso1000g = peso1000g;
    }

    public double getPrezzoSolaPannaAlKg() {
        return prezzoSolaPannaAlKg;
    }

    public void setPrezzoSolaPannaAlKg(double prezzoSolaPannaAlKg) {
        this.prezzoSolaPannaAlKg = prezzoSolaPannaAlKg;
    }

    public double getSupplementoPanna() {
        return supplementoPanna;
    }

    public void setSupplementoPanna(double supplementoPanna) {
        this.supplementoPanna = supplementoPanna;
    }

    public double getSupplementoGranella() {
        return supplementoGranella;
    }

    public void setSupplementoGranella(double supplementoGranella) {
        this.supplementoGranella = supplementoGranella;
    }
}
