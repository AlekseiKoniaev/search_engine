package main.service;

import main.model.Field;
import main.repository.FieldRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldService {
    
    private final FieldRepository fieldRepository;
    
    public FieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }
    
//    public void insertFields(List<Field> fields) {
//        fieldRepository.saveAll(fields);
//    }
    
    public List<Field> getAllFields() {
        Iterable<Field> fieldIterable = fieldRepository.findAll();
        List<Field> fieldList = new ArrayList<>();
        for (Field field : fieldIterable) {
            fieldList.add(field);
        }
        return fieldList;
    }
}
