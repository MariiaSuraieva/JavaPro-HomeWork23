package com.example.demowithtests.service;

import com.example.demowithtests.domain.Employee;
import com.example.demowithtests.repository.Repository;
import com.example.demowithtests.util.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@org.springframework.stereotype.Service
public class ServiceBean implements Service {

    private final Repository repository;

    @SneakyThrows
    @Override
    public Employee create(Employee employee) {
        if (repository.findEmployeeByEmail(employee.getEmail()) == null) {
            if (employee.getEmail() == null) {
                throw new EmailAbsentException();
            }
            return repository.save(employee);
        }
        throw new CopyDataException();
    }

    @Override
    public List<Employee> getAll() {
        if(repository.findAll().size()>0) {
            if (repository.findAll().size() == repository.findEmployeeByIsDeletedIsTrue().size()) {
                throw new ListWasDeletedException();
            }
            return repository.findAll();
        }throw new ListHasNoAnyElementsException();

    }

    @Override
    public Employee getById(String id) {
        try {
            Integer employeeId = Integer.parseInt(id);
            var employee = repository.findById(employeeId)
                    .orElseThrow(IdIsNotExistException::new);
            if (employee.getIsDeleted()) {
                throw new ResourceWasDeletedException();
            }
            return employee;
        } catch (NumberFormatException exception) {
            throw new WrongDataException();
        }
    }

    @SneakyThrows
    @Override
    public Employee updateById(Integer id, Employee employee) {
        return repository.findById(id)
                .map(entity -> {
                    entity.setName(employee.getName());
                    entity.setEmail(employee.getEmail());
                    entity.setCountry(employee.getCountry());
                    return repository.save(entity);
                })
                .orElseThrow(UserIsNotExistException::new);
    }

    @Override
    public void removeById(Integer id) {
        var employee = repository.findById(id)
                .orElseThrow(IdIsNotExistException::new);
        if(employee.getDeleted()) throw new UserAlreadyDeletedException();
        employee.setIsDeleted(true);
        repository.save(employee);
    }

    @Override
    public void removeAll() {

        if(repository.findAll().size()>0) {
            if (repository.findAll().size() == repository.findEmployeeByIsDeletedIsTrue().size()) {
                throw new ListWasDeletedException();
            }
            List<Employee> base = repository.findAll();
            for(Employee employee: base){
                employee.setIsDeleted(true);
            }
        }throw new ListHasNoAnyElementsException();


    }


    public void mailSender(List<String> emails, String text) {
        log.info("Emails sended");
    }

    @Override
    public List<Employee> sendEmailByCountry(String country, String text) {
        List<Employee> employees = repository.findEmployeeByCountry(country);
        mailSender(getterEmailsOfEmployees(employees), text);
        return employees;
    }

    public List<Employee> sendEmailByCity(String citiesString, String text) {
        String[] citiesArray = citiesString.split(",");
        List<String> citiesList = Arrays.asList(citiesArray);
        List<Employee> employees = new ArrayList<>();
        for(String city:citiesList){
             List<Employee> employeesByCity = repository.findEmployeeByAddresses(city);
             employees.addAll(employeesByCity);
        }
        mailSender(getterEmailsOfEmployees(employees), text);
        return employees;
    }


    private static List<String> getterEmailsOfEmployees(List<Employee> employees) {
        List<String> emails = new ArrayList<>();
        for (Employee employee : employees) {
            emails.add(employee.getEmail());
        }
        return emails;
    }
}
