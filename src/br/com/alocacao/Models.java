package br.com.alocacao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

final class Models {
    private Models() {}

    enum EmployeeStatus { ABSENT, WAITING, ALLOCATED }
    enum PendingKind { ALLOCATE, REMOVE }
    enum ShiftName { MANHA("Manhã"), TARDE("Tarde"), NOITE("Noite");
        private final String label;
        ShiftName(String label) { this.label = label; }
        String label() { return label; }
        static ShiftName from(String value) {
            if (value == null) return MANHA;
            for (ShiftName s : values()) if (s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) return s;
            return MANHA;
        }
    }

    static final class Employee implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, name, registration, role, arrivalTime, postId;
        EmployeeStatus status;
        List<String> skills = new ArrayList<>();
        Employee(String id, String name, String registration, String role, EmployeeStatus status, String arrivalTime, String postId, String... skills) {
            this.id=id; this.name=name; this.registration=registration; this.role=role; this.status=status; this.arrivalTime=arrivalTime; this.postId=postId;
            this.skills.addAll(List.of(skills));
        }
    }

    static final class Post implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, name, requirement;
        int minimum, planned, maximum;
        Post(String id, String name, int minimum, int planned, int maximum, String requirement) {
            this.id=id; this.name=name; this.minimum=minimum; this.planned=planned; this.maximum=maximum; this.requirement=requirement;
        }
    }

    static final class Pending implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, employeeId, postId, createdAt;
        PendingKind kind;
        Pending(String id, String employeeId, String postId, String createdAt, PendingKind kind) {
            this.id=id; this.employeeId=employeeId; this.postId=postId; this.createdAt=createdAt; this.kind=kind;
        }
    }

    static final class HistoryEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        String id, time, employeeId, action, postId, responsible;
        HistoryEntry(String id, String time, String employeeId, String action, String postId, String responsible) {
            this.id=id; this.time=time; this.employeeId=employeeId; this.action=action; this.postId=postId; this.responsible=responsible;
        }
    }

    static final class ConfirmedChange implements Serializable {
        private static final long serialVersionUID = 1L;
        String employeeId, postId, time;
        PendingKind kind;
        int beforeCount, afterCount;
        ConfirmedChange(String employeeId, String postId, PendingKind kind, String time, int beforeCount, int afterCount) {
            this.employeeId=employeeId; this.postId=postId; this.kind=kind; this.time=time; this.beforeCount=beforeCount; this.afterCount=afterCount;
        }
    }

    static final class AppState implements Serializable {
        private static final long serialVersionUID = 2L;
        List<Employee> employees = new ArrayList<>();
        List<Pending> pending = new ArrayList<>();
        List<HistoryEntry> history = new ArrayList<>();
        List<ConfirmedChange> lastConfirmed = new ArrayList<>();
        String lastUpdated = "06:42";
        ShiftName shift = ShiftName.MANHA;
    }
}
