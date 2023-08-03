package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService
{

    void add(AddressBook addressBook);


    List<AddressBook> getByUserId();

    AddressBook getDefaultByUserId();

    void setDefaultById(AddressBook addressBook);

    AddressBook getById(Long id);

    void updateById(AddressBook addressBook);

    void deleteById(Long id);
}
