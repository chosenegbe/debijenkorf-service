package com.debijenkorf.service.debijenkorfservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Profile("!local")
public class LogDb {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long logId;
    private Date timeStamp ;
    private String level;
    private String message;
}