package com.facenet.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by sh
 * 2020/3/4.
 */
@Data
@Entity
@Table(name = "face_data")
@Builder
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class FaceData {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;
    @Column(name="face_username")
    private String username;
    @Column(name="face_password")
    private String password;
    @Column(name="face_name")
    private String name;
    @Column(name="face_data",columnDefinition = "text")
    private String data;


    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date updateTime;
}
