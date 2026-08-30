package br.com.alocacao;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

final class WebServer {
    private final HttpServer server;
    private final OperationService service;
    private final Html html;

    WebServer(int port, OperationService service) throws IOException {
        this.service=service; this.html=new Html(service);
        this.server=HttpServer.create(new InetSocketAddress(port),0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", this::handle);
    }
    void start(){server.start();}

    private void handle(HttpExchange ex) throws IOException {
        try {
            String path=ex.getRequestURI().getPath();String method=ex.getRequestMethod();Map<String,String> q=query(ex.getRequestURI().getRawQuery());
            if("GET".equals(method)&&"/styles.css".equals(path)){serveCss(ex);return;}
            if("GET".equals(method)){
                boolean known = Set.of("/", "/aguardando", "/alocar", "/pendentes", "/confirmacao", "/postos", "/historico", "/tv").contains(path);
                String page=switch(path){
                    case "/" -> html.dashboard();
                    case "/aguardando" -> html.waiting(q.get("posto"));
                    case "/alocar" -> html.allocation(q.get("id"));
                    case "/pendentes" -> html.pending();
                    case "/confirmacao" -> html.confirmation();
                    case "/postos" -> html.posts(q.getOrDefault("view","list"));
                    case "/historico" -> html.history();
                    case "/tv" -> html.tv();
                    default -> notFound();
                }; send(ex,known?200:404,"text/html; charset=utf-8",page);return;
            }
            if("POST".equals(method)) { Map<String,String> form=form(ex); handlePost(ex,path,form); return; }
            send(ex,405,"text/plain; charset=utf-8","Método não permitido");
        } catch(Exception err){err.printStackTrace();send(ex,500,"text/html; charset=utf-8","<h1>Erro interno</h1><pre>"+Html.e(err.toString())+"</pre>");}
    }

    private void handlePost(HttpExchange ex,String path,Map<String,String> f) throws IOException {
        switch(path){
            case "/acao/alocar" -> {service.addAllocationPending(f.get("employeeId"),f.get("postId"));redirect(ex,"/pendentes");}
            case "/acao/retirar-posto" -> {service.addRemovalPending(f.get("employeeId"),f.get("postId"));redirect(ex,"/postos");}
            case "/acao/remover-pendente" -> {service.removePending(f.getOrDefault("id",""));redirect(ex,"/pendentes");}
            case "/acao/descartar" -> {service.discardAll();redirect(ex,"/pendentes");}
            case "/acao/confirmar" -> {service.confirmAll();redirect(ex,"/confirmacao");}
            case "/acao/turno" -> {service.setShift(f.get("shift"));redirect(ex,"/");}
            case "/acao/restaurar" -> {service.restoreDemo();redirect(ex,"/");}
            default -> send(ex,404,"text/plain; charset=utf-8","Ação não encontrada");
        }
    }

    private void serveCss(HttpExchange ex) throws IOException {
        Path p=Paths.get("web","styles.css");if(!Files.exists(p)){send(ex,404,"text/plain","CSS não encontrado");return;}byte[] data=Files.readAllBytes(p);ex.getResponseHeaders().set("Content-Type","text/css; charset=utf-8");ex.sendResponseHeaders(200,data.length);try(OutputStream os=ex.getResponseBody()){os.write(data);}
    }
    private static Map<String,String> form(HttpExchange ex) throws IOException {return query(new String(ex.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));}
    private static Map<String,String> query(String raw){Map<String,String> m=new LinkedHashMap<>();if(raw==null||raw.isBlank())return m;for(String part:raw.split("&")){String[] kv=part.split("=",2);String k=decode(kv[0]),v=kv.length>1?decode(kv[1]):"";m.put(k,v);}return m;}
    private static String decode(String s){return URLDecoder.decode(s,StandardCharsets.UTF_8);}
    private static void redirect(HttpExchange ex,String target) throws IOException {ex.getResponseHeaders().set("Location",target);ex.sendResponseHeaders(303,-1);ex.close();}
    private static void send(HttpExchange ex,int status,String type,String text) throws IOException {byte[] data=text.getBytes(StandardCharsets.UTF_8);ex.getResponseHeaders().set("Content-Type",type);ex.getResponseHeaders().set("Cache-Control","no-store");ex.sendResponseHeaders(status,data.length);try(OutputStream os=ex.getResponseBody()){os.write(data);}}
    private static String notFound(){return "<!doctype html><html><head><meta charset='utf-8'><link rel='stylesheet' href='/styles.css'><title>404</title></head><body><div class='page'><div class='confirmation-card'><h1>404</h1><p>Página não encontrada.</p><a class='btn btn-primary top-gap' href='/'>Voltar ao Dashboard</a></div></div></body></html>";}
}
