package com.smartlibrary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlibrary.entity.BookList;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookListMapper extends BaseMapper<BookList> {
}
