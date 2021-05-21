package com.samyisok.jpassvaultserver.domains;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
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

  @CreatedDate
  private Instant createdDate;

  @LastModifiedDate
  private Instant modifiedDate;

  File() {
  }

  public File(String base64) {
    this.file = base64;
  }

  /**
   * @return the id
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    return result;
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
    if (file == null) {
      if (other.file != null)
        return false;
    } else if (!file.equals(other.file))
      return false;
    return true;
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
