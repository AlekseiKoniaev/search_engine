package main.service;

import main.model.Field;

import java.util.List;

public interface FieldService {
    
    void insertField(Field field);
    void insertFields(List<Field> fields);
    List<Field> getAllFields();
    
}
