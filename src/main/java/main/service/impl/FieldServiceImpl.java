package main.service.impl;

import main.model.Field;
import main.repository.FieldRepository;
import main.service.FieldService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class FieldServiceImpl implements FieldService {
    
    @Autowired
    private FieldRepository fieldRepository;
    
    
    @Override
    public void insertField(Field field) {
        fieldRepository.save(field);
    }
    
    @Override
    public void insertFields(List<Field> fields) {
        fields.forEach(field -> fieldRepository.save(field));
    }
    
    @Override
    public List<Field> getAllFields() {
        Iterable<Field> fieldIterable = fieldRepository.findAll();
        List<Field> fieldList = new ArrayList<>();
        for (Field field : fieldIterable) {
            fieldList.add(field);
        }
        return fieldList;
    }
}
