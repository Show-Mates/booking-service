package com.intv.showmates.bs.repository;

import com.intv.showmates.bs.model.Sequence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
/**
 * @author NV
 * @version 1.0
 */
@Repository
public interface SequenceRepository extends MongoRepository<Sequence, String> {
}
