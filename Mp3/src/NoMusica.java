import java.io.File;

public class NoMusica {
    private File musica;
    private NoMusica anterior;
    private NoMusica proximo;

    public NoMusica(File musica) {
        this.musica = musica;
    }

    public File getMusica() {
        return musica;
    }

    public NoMusica getAnterior() {
        return anterior;
    }

    public void setAnterior(NoMusica anterior) {
        this.anterior = anterior;
    }

    public NoMusica getProximo() {
        return proximo;
    }

    public void setProximo(NoMusica proximo) {
        this.proximo = proximo;
    }
}