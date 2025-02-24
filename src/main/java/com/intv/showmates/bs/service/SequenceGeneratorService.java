package com.intv.showmates.bs.service;

import com.intv.showmates.bs.model.Sequence;
import com.intv.showmates.bs.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author NV
 * @version 1.0
 */
@Service
public class SequenceGeneratorService {

    @Autowired
    private SequenceRepository sequenceRepository;

    private static final String SEQUENCE_NAME = "booking_sequence";

    public String generateUserId() {
        Sequence sequence = sequenceRepository.findById(SEQUENCE_NAME)
                .orElseGet(this::createNewSequence);

        int currentSeq = sequence.getSeq();

        String generatedId = String.format("booking_%04d", currentSeq);

        sequence.setSeq(currentSeq + 1);

        sequenceRepository.save(sequence);

        return generatedId;
    }

    private Sequence createNewSequence() {
        Sequence sequence = new Sequence();
        sequence.setId(SEQUENCE_NAME);
        sequence.setSeq(1);
        return sequenceRepository.save(sequence);
    }
}
