package com.smartlibrary.mapper;

import com.smartlibrary.entity.BookListItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.Collections;
import java.util.List;

@Mapper
public interface BookListItemMapper {
    default List<BookListItem> selectByListId(Long listId) {
        return Collections.emptyList();
    }
    default void deleteByListId(Long listId) {
    }
}
