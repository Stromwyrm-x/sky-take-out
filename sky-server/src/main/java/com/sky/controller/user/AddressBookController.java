package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/addressBook")
public class AddressBookController
{
    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public Result add(@RequestBody AddressBook addressBook)
    {
        addressBookService.add(addressBook);
        return Result.success();
    }

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>>list()
    {
        List<AddressBook> addressBookList=addressBookService.getByUserId();
        return Result.success(addressBookList);
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook>getDefault()
    {
        AddressBook addressBook=addressBookService.getDefaultByUserId();
        return Result.success(addressBook);
    }

    /**
     * 设置默认地址
     * @return
     */
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook)
    {
        addressBookService.setDefaultById(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id)
    {
        AddressBook addressBook=addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public Result updateById(@RequestBody AddressBook addressBook)
    {
        addressBookService.updateById(addressBook);
        return Result.success();
    }

    /**
     * 微信小程序端有bug，传的url是.../?id=2
     * @param id
     * @return
     */
    @DeleteMapping("/")
    public Result deleteById(Long id)
    {
        addressBookService.deleteById(id);
        return Result.success();
    }


}
