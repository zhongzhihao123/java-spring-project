package com.aiplatform.cicd.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cicd_code_changes")
public class CodeChange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "exec_id", nullable = false) private Long execId;
    @Column(name = "commit_id", length = 64) private String commitId;
    @Column(length = 50) private String author;
    @Column(name = "commit_msg", length = 500) private String commitMsg;
    @Column(name = "commit_time") private LocalDateTime commitTime;
    @Column(name = "files_json", columnDefinition = "TEXT") private String filesJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExecId() { return execId; }
    public void setExecId(Long execId) { this.execId = execId; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCommitMsg() { return commitMsg; }
    public void setCommitMsg(String commitMsg) { this.commitMsg = commitMsg; }
    public LocalDateTime getCommitTime() { return commitTime; }
    public void setCommitTime(LocalDateTime commitTime) { this.commitTime = commitTime; }
    public String getFilesJson() { return filesJson; }
    public void setFilesJson(String filesJson) { this.filesJson = filesJson; }
}
