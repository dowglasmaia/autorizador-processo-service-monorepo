package spike.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.envers.Audited;

/** Entidade descartável do spike (ADR-05): mapeia a tabela real {@code procedimento} para provar a V2. */
@Entity
@Audited
@Table(name = "procedimento")
public class SpikeProcedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_procedimento", nullable = false)
    private Integer numeroProcedimento;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    protected SpikeProcedimento() {
    }

    public SpikeProcedimento(Integer numeroProcedimento) {
        this.numeroProcedimento = numeroProcedimento;
    }

    public Long getId() {
        return id;
    }
}
