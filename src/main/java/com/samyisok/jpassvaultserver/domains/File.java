package com.samyisok.jpassvaultserver.domains;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class File {
  private @Id @GeneratedValue Long id;

  // in Base64
  @Lob
  @Column(name = "file")
  private String file;

  @Column(name = "checksum")
  private String checksum;

  @CreatedDate
  private Instant createdDate;

  @LastModifiedDate
  private Instant modifiedDate;

  File() {
  }

  public File(String base64) {
    this.file = base64;
  }

  public File(String base64, String checksum) {
    this.file = base64;
    this.checksum = checksum;
  }

  /**
   * @return the id
   */
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public Long getId() {
    return id;
  }


  /**
   * @return the file
   */
  public String getFile() {
    return file;
  }

  /**
   * @param file the file to set
   */
  public void setFile(String file) {
    this.file = file;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */

  @Override
  public int hashCode() {
    // Persistent identity; unsaved instances fall back to identity so the
    // payload is never hashed.
    return id == null ? System.identityHashCode(this) : id.hashCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof File))
      return false;
    File other = (File) obj;
    return id != null && id.equals(other.id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */

  @Override
  public String toString() {
    return "File [id=" + id + "]";
  }

  /**
   * @return the createdDate
   */
  public Instant getCreatedDate() {
    return createdDate;
  }

  /**
   * @return the modifiedDate
   */
  public Instant getModifiedDate() {
    return modifiedDate;
  }
}
