package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService
{
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public void add(AddressBook addressBook)
    {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> getByUserId()
    {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook>addressBookList=addressBookMapper.selectByUserId(userId);
        return addressBookList;
    }

    @Override
    public AddressBook getDefaultByUserId()
    {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook=addressBookMapper.selectDefaultByUserId(userId);
        return addressBook;
    }

    @Override
    public void setDefaultById(AddressBook addressBook)
    {
        //1.将该用户所有地址的isDefault设置为0
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.updateByUserId(addressBook);

        //2.将该id下的地址的isDefault设置为1
        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public AddressBook getById(Long id)
    {
        AddressBook addressBook=addressBookMapper.selectById(id);
        return addressBook;
    }

    @Override
    public void updateById(AddressBook addressBook)
    {
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public void deleteById(Long id)
    {
        addressBookMapper.deleteById(id);
    }


}
