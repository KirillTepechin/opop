package opopproto.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="head_id", nullable=false)
    private Head head;

    @ElementCollection
    @CollectionTable(name = "characteristic_errors", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "error", length = 50000)
    private List<String> characteristicErrors;
    @ElementCollection
    @CollectionTable(name = "fos_errors", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "error", length = 50000)
    private List<String> fosErrors;
    @ElementCollection
    @CollectionTable(name = "rpd_errors", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "error", length = 50000)
    private List<String> rpdErrors;

    boolean fosPackageFound = false;
    boolean rpdPackageFound = false;;
    boolean characteristicsFound = false;
    boolean characteristicsInRightExtension = true;
    boolean syllabusFound = false;
    boolean syllabusInRightExtension = true;

    @ElementCollection
    @CollectionTable(name = "fos_in_wrong_format", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "error", length = 50000)
    private List<String> fosInWrongFormat;
    @ElementCollection
    @CollectionTable(name = "rpd_in_wrong_format", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "error", length = 50000)
    private List<String> rpdInWrongFormat;
    @ElementCollection
    @CollectionTable(name = "unknown_documents", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "error", length = 50000)
    private List<String> unknownDocuments;

    boolean isValid;
    boolean isOk;

    private String docUuid;

    @CreationTimestamp
    private LocalDateTime creationDate;
}
