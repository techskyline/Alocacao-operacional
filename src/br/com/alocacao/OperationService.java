package br.com.alocacao;

import br.com.alocacao.Models.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

final class OperationService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final Path stateFile = Paths.get("data", "alocacao-state.bin");
    private final List<Post> posts = List.of(
        new Post("machine", "Máquina 1", 1, 1, 3, "Operador de Máquina"),
        new Post("conveyor", "Esteira M1", 4, 6, 8, null),
        new Post("table", "Mesa M1", 1, 2, 2, null),
        new Post("toco", "Toco M1", 1, 1, 1, null),
        new Post("pallet", "Transpaleteira", 1, 1, 1, "Treinamento de Transpaleteira")
    );
    private AppState state;

    OperationService() {
        this.state = load();
    }

    synchronized AppState state() { return state; }
    List<Post> posts() { return posts; }

    synchronized Employee employee(String id) {
        return state.employees.stream().filter(e -> e.id.equals(id)).findFirst().orElse(null);
    }
    Post post(String id) { return posts.stream().filter(p -> p.id.equals(id)).findFirst().orElse(null); }

    synchronized Map<String,Integer> counts() {
        Map<String,Integer> result = emptyCounts();
        for (Employee e : state.employees) if (e.status == EmployeeStatus.ALLOCATED && e.postId != null) result.merge(e.postId, 1, Integer::sum);
        return result;
    }

    synchronized Map<String,Integer> projectedCounts() {
        Map<String,Integer> result = counts();
        for (Pending p : state.pending) {
            int current = result.getOrDefault(p.postId, 0);
            result.put(p.postId, p.kind == PendingKind.REMOVE ? Math.max(0, current - 1) : current + 1);
        }
        return result;
    }

    synchronized List<Employee> waiting(String postId) {
        Post target = post(postId);
        return state.employees.stream().filter(e -> e.status == EmployeeStatus.WAITING)
            .filter(e -> state.pending.stream().noneMatch(p -> p.employeeId.equals(e.id)))
            .filter(e -> target == null || e.skills.contains(target.name))
            .collect(Collectors.toList());
    }

    synchronized List<Employee> assigned(String postId) {
        return state.employees.stream().filter(e -> e.status == EmployeeStatus.ALLOCATED && Objects.equals(e.postId, postId)).collect(Collectors.toList());
    }

    synchronized boolean addAllocationPending(String employeeId, String postId) {
        Employee e = employee(employeeId); Post p = post(postId);
        if (e == null || p == null || e.status != EmployeeStatus.WAITING) return false;
        if (state.pending.stream().anyMatch(x -> x.employeeId.equals(employeeId))) return false;
        int projected = projectedCounts().getOrDefault(postId, 0);
        if (!e.skills.contains(p.name) || projected >= p.maximum) return false;
        state.pending.add(new Pending("alloc-"+employeeId+"-"+postId+"-"+System.nanoTime(), employeeId, postId, now(), PendingKind.ALLOCATE));
        touch(); return true;
    }

    synchronized boolean addRemovalPending(String employeeId, String postId) {
        Employee e = employee(employeeId); Post p = post(postId);
        if (e == null || p == null || e.status != EmployeeStatus.ALLOCATED || !Objects.equals(e.postId, postId)) return false;
        if (state.pending.stream().anyMatch(x -> x.employeeId.equals(employeeId))) return false;
        state.pending.add(new Pending("remove-"+employeeId+"-"+postId+"-"+System.nanoTime(), employeeId, postId, now(), PendingKind.REMOVE));
        touch(); return true;
    }

    synchronized void removePending(String id) { state.pending.removeIf(p -> p.id.equals(id)); save(); }
    synchronized void discardAll() { state.pending.clear(); save(); }

    synchronized void confirmAll() {
        if (state.pending.isEmpty()) return;
        Map<String,Integer> before = counts();
        Map<String,Integer> after = projectedCounts();
        String time = now();
        List<ConfirmedChange> confirmed = new ArrayList<>();
        for (Pending p : new ArrayList<>(state.pending)) {
            Employee e = employee(p.employeeId);
            if (e == null) continue;
            if (p.kind == PendingKind.REMOVE) { e.status = EmployeeStatus.WAITING; e.postId = null; }
            else { e.status = EmployeeStatus.ALLOCATED; e.postId = p.postId; }
            confirmed.add(new ConfirmedChange(p.employeeId, p.postId, p.kind, time, before.getOrDefault(p.postId,0), after.getOrDefault(p.postId,0)));
            state.history.add(new HistoryEntry(UUID.randomUUID().toString(), time, p.employeeId,
                p.kind == PendingKind.REMOVE ? "Retirada do posto" : "Alocação aplicada", p.postId, "Responsável do setor"));
        }
        state.lastConfirmed = confirmed;
        state.pending.clear();
        state.lastUpdated = time;
        save();
    }

    synchronized void setShift(String value) { state.shift = ShiftName.from(value); touch(); }
    synchronized void restoreDemo() { state = initial(); save(); }

    int compatibleWaiting(Post p) { return waiting(p.id).size(); }
    String statusFor(int count, Post p) {
        if (count < p.minimum) return "critical";
        if (count < p.planned) return "partial";
        if (count > p.planned) return "above";
        return "covered";
    }
    String statusLabel(String status) {
        return switch (status) { case "critical" -> "CRÍTICO"; case "partial" -> "PARCIAL"; case "above" -> "ACIMA DO PLANEJADO"; default -> "COBERTO"; };
    }

    private Map<String,Integer> emptyCounts() { Map<String,Integer> m = new LinkedHashMap<>(); for (Post p : posts) m.put(p.id,0); return m; }
    private String now() { return LocalTime.now().format(TIME); }
    private void touch() { state.lastUpdated = now(); save(); }

    private AppState load() {
        try {
            if (Files.exists(stateFile)) try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(stateFile))) { return (AppState) in.readObject(); }
        } catch (Exception ex) { System.err.println("Estado salvo incompatível; carregando demonstração: " + ex.getMessage()); }
        AppState s = initial();
        this.state = s;
        save();
        return s;
    }
    private void save() {
        try {
            Files.createDirectories(stateFile.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(stateFile))) { out.writeObject(state); }
        } catch (IOException ex) { System.err.println("Não foi possível salvar o estado: " + ex.getMessage()); }
    }

    private AppState initial() {
        AppState s = new AppState();
        s.employees.add(new Employee("carlos", "Carlos Eduardo", "18294", "Operador de Máquina", EmployeeStatus.WAITING, "06:04", null, "Máquina 1", "Esteira M1", "Mesa M1", "Toco M1"));
        s.employees.add(new Employee("lucas", "Lucas Pereira", "12345", "Operador de Produção", EmployeeStatus.WAITING, "06:07", null, "Esteira M1", "Mesa M1", "Toco M1"));
        s.employees.add(new Employee("joao", "João Pedro", "30912", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:02", "toco", "Toco M1", "Transpaleteira"));
        s.employees.add(new Employee("ana", "Ana Beatriz", "27401", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:11", "pallet", "Transpaleteira", "Treinamento de Transpaleteira"));
        s.employees.add(new Employee("mariana", "Mariana Alves", "19873", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:05", "conveyor", "Esteira M1", "Mesa M1"));
        s.employees.add(new Employee("rafael", "Rafael Costa", "22018", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:08", "conveyor", "Esteira M1", "Toco M1"));
        s.employees.add(new Employee("beatriz", "Beatriz Souza", "16542", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:03", "conveyor", "Esteira M1", "Mesa M1"));
        s.employees.add(new Employee("pedro", "Pedro Henrique", "28800", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:10", "table", "Mesa M1", "Esteira M1"));
        s.employees.add(new Employee("camila", "Camila Rocha", "30114", "Operador de Produção", EmployeeStatus.ALLOCATED, "06:06", "table", "Mesa M1"));
        s.employees.add(new Employee("diego", "Diego Martins", "17604", "Operador de Produção", EmployeeStatus.ABSENT, null, null, "Esteira M1", "Mesa M1"));
        s.lastUpdated = "06:42"; s.shift = ShiftName.MANHA;
        return s;
    }
}
