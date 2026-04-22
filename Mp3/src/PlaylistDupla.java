import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDupla {
    private NoMusica inicio;
    private NoMusica fim;
    private NoMusica atual;
    private int tamanho;

    public void adicionar(File musica) {
        if (musica == null) return;

        NoMusica novo = new NoMusica(musica);

        if (inicio == null) {
            inicio = fim = atual = novo;
        } else {
            fim.setProximo(novo);
            novo.setAnterior(fim);
            fim = novo;
        }
        tamanho++;
    }

    public boolean removerPorIndice(int indice) {
        NoMusica no = getNo(indice);
        if (no == null) return false;

        NoMusica ant = no.getAnterior();
        NoMusica prox = no.getProximo();

        if (ant != null) {
            ant.setProximo(prox);
        } else {
            inicio = prox;
        }

        if (prox != null) {
            prox.setAnterior(ant);
        } else {
            fim = ant;
        }

        if (atual == no) {
            atual = prox != null ? prox : ant;
        }

        tamanho--;
        if (tamanho == 0) {
            inicio = fim = atual = null;
        }

        return true;
    }

    public File getAtual() {
        return atual != null ? atual.getMusica() : null;
    }

    public File avancar() {
        if (atual != null && atual.getProximo() != null) {
            atual = atual.getProximo();
        }
        return getAtual();
    }

    public File voltar() {
        if (atual != null && atual.getAnterior() != null) {
            atual = atual.getAnterior();
        }
        return getAtual();
    }

    public boolean definirAtualPorIndice(int indice) {
        NoMusica no = getNo(indice);
        if (no == null) return false;
        atual = no;
        return true;
    }

    public int getIndiceAtual() {
        int i = 0;
        NoMusica aux = inicio;
        while (aux != null) {
            if (aux == atual) return i;
            aux = aux.getProximo();
            i++;
        }
        return -1;
    }

    public File getPorIndice(int indice) {
        NoMusica no = getNo(indice);
        return no != null ? no.getMusica() : null;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public void limpar() {
        inicio = fim = atual = null;
        tamanho = 0;
    }

    public List<File> paraLista() {
        List<File> lista = new ArrayList<>();
        NoMusica aux = inicio;
        while (aux != null) {
            lista.add(aux.getMusica());
            aux = aux.getProximo();
        }
        return lista;
    }

    private NoMusica getNo(int indice) {
        if (indice < 0 || indice >= tamanho) return null;

        NoMusica aux;
        int i;

        if (indice < tamanho / 2) {
            aux = inicio;
            i = 0;
            while (aux != null && i < indice) {
                aux = aux.getProximo();
                i++;
            }
        } else {
            aux = fim;
            i = tamanho - 1;
            while (aux != null && i > indice) {
                aux = aux.getAnterior();
                i--;
            }
        }
        return aux;
    }
}