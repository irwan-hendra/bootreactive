package com.example.demoreactive;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {

  private String txnId;
  private LocalDateTime startDtm;
  private LocalDateTime endDtm;

  private List<Student> students;
  private Double piResult;
}
