package br.com.alocacao;

/**
 * Ponto de entrada da aplicação.
 * Requer apenas Java 17+; recomendado Java 21.
 */
public final class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try { port = Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
        }

        OperationService service = new OperationService();
        WebServer server = new WebServer(port, service);
        server.start();

        System.out.println("====================================================");
        System.out.println(" ALOC.OP - Alocação Operacional Java Web");
        System.out.println(" http://localhost:" + port);
        System.out.println(" Pressione Ctrl+C para encerrar.");
        System.out.println("====================================================");
    }
}
