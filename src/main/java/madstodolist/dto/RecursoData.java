package madstodolist.dto;

import java.io.Serializable;
import java.util.Objects;

// Data Transfer Object for Recurso
public class RecursoData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nombre;
    private Long usuarioId;  // ID of the associated Usuario

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Override equals and hashCode based on id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecursoData that = (RecursoData) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
