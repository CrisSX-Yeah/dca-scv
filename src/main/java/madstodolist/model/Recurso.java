package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "recursos")
public class Recurso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nombre;

    @NotNull
    // Many-to-One relationship between Recursos and Usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Default constructor required by JPA/Hibernate
    public Recurso() {}

    // Constructor to associate Recurso with a Usuario
    public Recurso(Usuario usuario, String nombre) {
        this.nombre = nombre;
        setUsuario(usuario); // Adds this recurso to the user's recursos list
    }

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

    public Usuario getUsuario() {
        return usuario;
    }

    // Method to set the relationship with Usuario
    public void setUsuario(Usuario usuario) {
        if (this.usuario != usuario) {
            this.usuario = usuario;
            usuario.addRecurso(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recurso recurso = (Recurso) o;

        if (id != null && recurso.id != null)
            return Objects.equals(id, recurso.id);
        return nombre.equals(recurso.nombre) &&
                usuario.equals(recurso.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, usuario);
    }
}
