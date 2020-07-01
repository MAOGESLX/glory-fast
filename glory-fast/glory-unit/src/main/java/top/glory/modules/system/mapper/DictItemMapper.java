package top.glory.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.glory.modules.system.entity.SysDict;
import top.glory.modules.system.entity.SysDictItem;

import java.util.List;

/**
 * @Description: 用户管理
 * @Author: 春秋
 * @Date: 2020年4月15日
 */
@Mapper
public interface DictItemMapper extends BaseMapper<SysDictItem> {

    List<SysDictItem> getDictItemList(SysDict dict);
}
