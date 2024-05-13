package opopproto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerateResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String profile;
    private String specialty;
    private String qualification;
    private String forms;
    private String startYear;

    private String docUuid;

    @ManyToOne
    @JoinColumn(name="head_id", nullable=false)
    private Head head;
}
