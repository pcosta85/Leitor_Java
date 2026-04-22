import java.io.*;
import java.util.Properties;

public class ConfiguracaoUtil {
    private static final String CONFIG_FILE = "config.txt";

    public static void salvar(double volume, boolean shuffle, int repeat, int indiceAtual) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            writer.write("volume=" + volume);
            writer.newLine();
            writer.write("shuffle=" + shuffle);
            writer.newLine();
            writer.write("repeat=" + repeat);
            writer.newLine();
            writer.write("indiceAtual=" + indiceAtual);
            writer.newLine();
        }
    }

    public static Properties carregar() throws IOException {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split("=", 2);
                if (partes.length == 2) {
                    props.setProperty(partes[0], partes[1]);
                }
            }
        }
        return props;
    }
}