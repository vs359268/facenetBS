package com.facenet.repository;

import com.facenet.bean.FaceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface FaceDataRepository extends JpaRepository<FaceData,Integer> {
    FaceData findFirstByUsername(String username);
    FaceData findFirstByUsernameAndPassword(String username,String password);
}
